/*
 * Copyright 2010-2019 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.graph.node.plugins;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginEnvironment;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginSynchronizer;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLogger;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReport;
import au.gov.asd.tac.constellation.plugins.reporting.GraphReportManager;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Instance that supports and manages the running environment for a plugin.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginEnvironment.class)
public class DefaultPluginEnvironment extends PluginEnvironment {

    private static final Logger LOGGER = Logger.getLogger(DefaultPluginEnvironment.class.getName());

    private static final String THREAD_POOL_NAME = "Default Plugin Environment";

    private final ExecutorService pluginExecutor = Executors.newCachedThreadPool();

    @Override
    public Future<?> executePluginLater(final Graph graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive, final List<Future<?>> async, final PluginSynchronizer synchronizer) {

        return pluginExecutor.submit(new Callable<Object>() {
            @Override
            public Object call() {
                Thread.currentThread().setName(THREAD_POOL_NAME);

                // If a Future has been specified, don't do anything until the Future has completed.
                // A typical use-case is an arrangement followed by a camera reset: obviously doing the reset before the
                // vertices have been relocated is not sensible.
                if (async != null) {
                    for (Future<?> future : async) {
                        if (future != null) {
                            try {
                                future.get();
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }

                final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
                final boolean alwaysSilent = callingConstraints.isAlwaysSilent() || callingConstraints.getSilentCount() > 0;

                PluginReport currentReport = null;
                GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
                if (graphReport != null) {
                    currentReport = graphReport.addPluginReport(plugin);
                    callingConstraints.setCurrentReport(currentReport);
                }

                try {
                    ConstellationLogger.getDefault().pluginStarted(plugin, parameters, graph);
                } catch (Exception ex) {
                }

                PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, synchronizer);
                PluginGraphs graphs = new DefaultPluginGraphs(manager);
                PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);

                try {
                    if (parameters != null) {
                        plugin.updateParameters(graph, parameters);
                    }
                    if (interactive && parameters != null) {
                        if (interaction.prompt(plugin.getName(), parameters)) {
                            ThreadConstraints calledConstraints = ThreadConstraints.getConstraints();
                            calledConstraints.setAlwaysSilent(alwaysSilent);
                            try {
                                plugin.run(graphs, interaction, parameters);
                            } finally {
                                calledConstraints.setAlwaysSilent(false);
                                calledConstraints.setSilentCount(0);
                                if (synchronizer != null) {
                                    synchronizer.finished();
                                }
                            }
                        }
                    } else {
                        ThreadConstraints calledConstraints = ThreadConstraints.getConstraints();
                        calledConstraints.setAlwaysSilent(alwaysSilent);
                        try {
                            plugin.run(graphs, interaction, parameters);
                        } finally {
                            calledConstraints.setAlwaysSilent(false);
                            calledConstraints.setSilentCount(0);
                            if (synchronizer != null) {
                                synchronizer.finished();
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    auditPluginError(plugin, ex);
                    interaction.notify(PluginNotificationLevel.INFO, "Plugin Cancelled: " + plugin.getName());
                    if (currentReport != null) {
                        currentReport.setError(ex);
                    }
                } catch (PluginException ex) {
                    auditPluginError(plugin, ex);
                    interaction.notify(ex.getNotificationLevel(), ex.getMessage());
                    ex.printStackTrace();
                    if (currentReport != null) {
                        currentReport.setError(ex);
                    }
                } catch (Exception ex) {
                    auditPluginError(plugin, ex);
                    final String msg = String.format("Unexpected non-plugin exception caught in %s.executePluginLater();\n", DefaultPluginEnvironment.class.getName());
                    LOGGER.log(Level.WARNING, msg, ex);
                    if (currentReport != null) {
                        currentReport.setError(ex);
                    }
                } finally {
                    if (currentReport != null) {
                        currentReport.stop();
                        callingConstraints.setCurrentReport(null);
                        currentReport.firePluginReportChangedEvent();
                    }

                    try {
                        ConstellationLogger.getDefault().pluginStopped(plugin, parameters);
                    } catch (Exception ex) {
                    }
                }

                return null;
            }
        });
    }

    @Override
    public Object executePluginNow(final Graph graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive) throws InterruptedException, PluginException {

        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        final int silentCount = callingConstraints.getSilentCount();
        final boolean alwaysSilent = callingConstraints.isAlwaysSilent();
        callingConstraints.setSilentCount(0);
        callingConstraints.setAlwaysSilent(alwaysSilent || silentCount > 0);

        GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
        PluginReport parentReport = null, currentReport = null;
        if (graphReport != null) {
            parentReport = callingConstraints.getCurrentReport();
            if (parentReport == null) {
                currentReport = graphReport.addPluginReport(plugin);
            } else {
                currentReport = parentReport.addChildReport(plugin);
            }
            callingConstraints.setCurrentReport(currentReport);
        }

        try {
            ConstellationLogger.getDefault().pluginStarted(plugin, parameters, graph);
        } catch (Exception ex) {
        }

        try {
            PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, null);
            PluginGraphs graphs = new DefaultPluginGraphs(manager);
            PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);
            if (parameters != null) {
                plugin.updateParameters(graph, parameters);
            }
            if (interactive && parameters != null) {
                if (interaction.prompt(plugin.getName(), parameters)) {
                    plugin.run(graphs, interaction, parameters);
                }
            } else {
                plugin.run(graphs, interaction, parameters);

            }
        } catch (Exception ex) {
            auditPluginError(plugin, ex);
            if (currentReport != null) {
                currentReport.setError(ex);
            }
            throw ex;
        } finally {
            callingConstraints.setSilentCount(silentCount);
            callingConstraints.setAlwaysSilent(alwaysSilent);

            if (currentReport != null) {
                currentReport.stop();
                callingConstraints.setCurrentReport(parentReport);
                currentReport.firePluginReportChangedEvent();
            }

            try {
                ConstellationLogger.getDefault().pluginStopped(plugin, parameters);
            } catch (Exception ex) {
            }
        }

        return null;
    }

    @Override
    public Object executeEditPluginNow(final GraphWriteMethods graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive) throws InterruptedException, PluginException {
        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        final int silentCount = callingConstraints.getSilentCount();
        final boolean alwaysSilent = callingConstraints.isAlwaysSilent();
        callingConstraints.setSilentCount(0);
        callingConstraints.setAlwaysSilent(alwaysSilent || silentCount > 0);

        final GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
        PluginReport parentReport = null, currentReport = null;
        if (graphReport != null) {
            parentReport = callingConstraints.getCurrentReport();
            if (parentReport == null) {
                currentReport = graphReport.addPluginReport(plugin);
            } else {
                currentReport = parentReport.addChildReport(plugin);
            }
            callingConstraints.setCurrentReport(currentReport);
        }

        try {
            ConstellationLogger.getDefault().pluginStarted(plugin, parameters, GraphNode.getGraph(graph != null ? graph.getId() : null));
        } catch (Exception ex) {
        }

        try {
            final PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, null);
            final PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);

            plugin.run(graph, interaction, parameters);
        } catch (Exception ex) {
            auditPluginError(plugin, ex);
            if (currentReport != null) {
                currentReport.setError(ex);
            }
            throw ex;
        } finally {
            callingConstraints.setSilentCount(silentCount);
            callingConstraints.setAlwaysSilent(alwaysSilent);

            if (currentReport != null) {
                currentReport.stop();
                callingConstraints.setCurrentReport(parentReport);
                currentReport.firePluginReportChangedEvent();
            }

            try {
                ConstellationLogger.getDefault().pluginStopped(plugin, parameters);
            } catch (Exception ex) {
            }

        }
        return null;
    }

    @Override
    public Object executeReadPluginNow(final GraphReadMethods graph, final Plugin plugin, final PluginParameters parameters, final boolean interactive) throws InterruptedException, PluginException {

        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        final int silentCount = callingConstraints.getSilentCount();
        final boolean alwaysSilent = callingConstraints.isAlwaysSilent();
        callingConstraints.setSilentCount(0);
        callingConstraints.setAlwaysSilent(alwaysSilent || silentCount > 0);

        GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
        PluginReport parentReport = null, currentReport = null;
        if (graphReport != null) {
            parentReport = callingConstraints.getCurrentReport();
            if (parentReport == null) {
                currentReport = graphReport.addPluginReport(plugin);
            } else {
                currentReport = parentReport.addChildReport(plugin);
            }
            callingConstraints.setCurrentReport(currentReport);
        }

        try {
            ConstellationLogger.getDefault().pluginStarted(plugin, parameters, GraphNode.getGraph(graph != null ? graph.getId() : null));
        } catch (Exception ex) {
        }

        try {
            PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, null);
            PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);

            plugin.run(graph, interaction, parameters);

        } catch (Exception ex) {
            auditPluginError(plugin, ex);
            if (currentReport != null) {
                currentReport.setError(ex);
            }
            throw ex;
        } finally {
            callingConstraints.setSilentCount(silentCount);
            callingConstraints.setAlwaysSilent(alwaysSilent);

            if (currentReport != null) {
                currentReport.stop();
                callingConstraints.setCurrentReport(parentReport);
                currentReport.firePluginReportChangedEvent();
            }

            try {
                ConstellationLogger.getDefault().pluginStopped(plugin, parameters);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private void auditPluginError(Plugin plugin, Throwable error) {
        try {
            ConstellationLogger.getDefault().pluginError(plugin, error);
        } catch (Exception ex) {
        }
    }
}
