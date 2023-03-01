/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.utilities.threadpool.UniversalThreadPool;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private final ExecutorService pluginExecutor = UniversalThreadPool.getThreadPool().getDefaultPluginEnvPool();

    private static final String GRAPH_NULL_WARNING_MESSAGE = "{0} plugin was executed on a graph which was null";

    @Override
    public Future<?> executePluginLater(final Graph graph, final Plugin plugin,
            final PluginParameters parameters, final boolean interactive,
            final List<Future<?>> async, final PluginSynchronizer synchronizer) {

        if (graph == null) {
            LOGGER.log(Level.INFO, GRAPH_NULL_WARNING_MESSAGE, plugin.getName());
        }

        final ThreadConstraints parentConstraints = ThreadConstraints.getConstraints();
        return getPluginExecutor().submit(() -> {
            Thread.currentThread().setName(THREAD_POOL_NAME);

            // If a Future has been specified, don't do anything until the Future has completed.
            // A typical use-case is an arrangement followed by a camera reset: obviously doing the reset before the
            // vertices have been relocated is not sensible.
            if (async != null) {
                for (final Future<?> future : async) {
                    if (future != null) {
                        try {
                            future.get();
                        } catch (final InterruptedException ex) {
                            LOGGER.log(Level.SEVERE, "Execution interrupted", ex);
                            Thread.currentThread().interrupt();
                        } catch (final ExecutionException ex) {
                            LOGGER.log(Level.SEVERE, "Execution Exception", ex);
                        }
                    }
                }
            }

            final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
            final boolean alwaysSilent = callingConstraints.isAlwaysSilent() || callingConstraints.getSilentCount() > 0;

            PluginReport currentReport = null;
            final GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
            // a graph report can have multiple plugin reports ... a plugin report can have multiple child plugin reports
            if (graphReport != null) {     
                final PluginReport parentThreadReport = parentConstraints.getCurrentReport();
                final PluginReport existingReport = callingConstraints.getCurrentReport();
                if (parentThreadReport == null && existingReport == null) {
                    currentReport = graphReport.addPluginReport(plugin);
                } else if (existingReport != null) {
                    currentReport = existingReport.addChildReport(plugin);
                } else if (parentThreadReport != null) {
                    currentReport = parentThreadReport.addChildReport(plugin);
                }
                callingConstraints.setCurrentReport(currentReport);            
            }

            try {
                ConstellationLogger.getDefault().pluginStarted(plugin, parameters, graph);
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }

            final PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, synchronizer);
            final PluginGraphs graphs = new DefaultPluginGraphs(manager);
            final PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);

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
                    final ThreadConstraints calledConstraints = ThreadConstraints.getConstraints();
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
            } catch (final InterruptedException ex) {
                auditPluginError(plugin, ex);
                reportException(plugin.getName(), interaction, currentReport, null, ex);
                Thread.currentThread().interrupt();
            } catch (final PluginException ex) {
                auditPluginError(plugin, ex);
                reportException(plugin.getName(), interaction, currentReport, ex.getNotificationLevel(), ex);
            } catch (final Exception ex) {
                auditPluginError(plugin, ex);
                reportException(plugin.getName(), interaction, currentReport, PluginNotificationLevel.ERROR, ex);
            } finally {
                if (currentReport != null) {
                    currentReport.stop();
                    callingConstraints.setCurrentReport(null);
                    currentReport.firePluginReportChangedEvent();
                }

                try {
                    ConstellationLogger.getDefault().pluginStopped(plugin, parameters);
                } catch (final Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }

            return null;
        });
    }

    @Override
    public Object executePluginNow(final Graph graph, final Plugin plugin,
            final PluginParameters parameters, final boolean interactive)
            throws InterruptedException, PluginException {

        if (graph == null) {
            LOGGER.log(Level.FINE, GRAPH_NULL_WARNING_MESSAGE, plugin.getName());
        }

        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        final int silentCount = callingConstraints.getSilentCount();
        final boolean alwaysSilent = callingConstraints.isAlwaysSilent();
        callingConstraints.setSilentCount(0);
        callingConstraints.setAlwaysSilent(alwaysSilent || silentCount > 0);

        final GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
        PluginReport parentReport = null;
        PluginReport currentReport = null;
        if (graphReport != null) {
            parentReport = callingConstraints.getCurrentReport();
            if (parentReport == null) {
                currentReport = graphReport.addPluginReport(plugin);
            } else {
                currentReport = parentReport.addChildReport(plugin);
            }
            callingConstraints.setCurrentReport(currentReport);
        }

        final PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, null);
        final PluginGraphs graphs = new DefaultPluginGraphs(manager);
        final PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);

        try {
            ConstellationLogger.getDefault().pluginStarted(plugin, parameters, graph);
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }

        try {
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
        } catch (final InterruptedException ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, null, ex);
            Thread.currentThread().interrupt();
            throw ex;
        } catch (final PluginException ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, ex.getNotificationLevel(), ex);
            throw ex;
        } catch (final Exception ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, PluginNotificationLevel.ERROR, ex);
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
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }

        return null;
    }

    @Override
    public Object executeEditPluginNow(final GraphWriteMethods graph,
            final Plugin plugin, final PluginParameters parameters,
            final boolean interactive) throws InterruptedException, PluginException {

        if (graph == null) {
            LOGGER.log(Level.FINE, GRAPH_NULL_WARNING_MESSAGE, plugin.getName());
        }

        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        final int silentCount = callingConstraints.getSilentCount();
        final boolean alwaysSilent = callingConstraints.isAlwaysSilent();
        callingConstraints.setSilentCount(0);
        callingConstraints.setAlwaysSilent(alwaysSilent || silentCount > 0);

        final GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
        PluginReport parentReport = null;
        PluginReport currentReport = null;
        if (graphReport != null) {
            parentReport = callingConstraints.getCurrentReport();
            if (parentReport == null) {
                currentReport = graphReport.addPluginReport(plugin);
            } else {
                currentReport = parentReport.addChildReport(plugin);
            }
            callingConstraints.setCurrentReport(currentReport);
        }
        
        final PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, null);
        final PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);
        try {
            ConstellationLogger.getDefault().pluginStarted(plugin, parameters, GraphNode.getGraph(graph != null ? graph.getId() : null));
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }

        try {
            plugin.run(graph, interaction, parameters);
        } catch (final InterruptedException ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, null, ex);
            Thread.currentThread().interrupt();
            throw ex;
        } catch (final PluginException ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, ex.getNotificationLevel(), ex);
            throw ex;
        } catch (final Exception ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, PluginNotificationLevel.ERROR, ex);
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
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
        return null;
    }

    @Override
    public Object executeReadPluginNow(final GraphReadMethods graph,
            final Plugin plugin, final PluginParameters parameters,
            final boolean interactive) throws InterruptedException, PluginException {

        if (graph == null) {
            LOGGER.log(Level.FINE, GRAPH_NULL_WARNING_MESSAGE, plugin.getName());
        }

        final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();
        final int silentCount = callingConstraints.getSilentCount();
        final boolean alwaysSilent = callingConstraints.isAlwaysSilent();
        callingConstraints.setSilentCount(0);
        callingConstraints.setAlwaysSilent(alwaysSilent || silentCount > 0);

        final GraphReport graphReport = graph == null ? null : GraphReportManager.getGraphReport(graph.getId());
        PluginReport parentReport = null;
        PluginReport currentReport = null;
        if (graphReport != null) {
            parentReport = callingConstraints.getCurrentReport();
            if (parentReport == null) {
                currentReport = graphReport.addPluginReport(plugin);
            } else {
                currentReport = parentReport.addChildReport(plugin);
            }
            callingConstraints.setCurrentReport(currentReport);
        }

        final PluginManager manager = new PluginManager(DefaultPluginEnvironment.this, plugin, graph, interactive, null);
        final PluginInteraction interaction = new DefaultPluginInteraction(manager, currentReport);
        try {
            ConstellationLogger.getDefault().pluginStarted(plugin, parameters, GraphNode.getGraph(graph != null ? graph.getId() : null));
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }

        try {
            plugin.run(graph, interaction, parameters);
        } catch (final InterruptedException ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, null, ex);
            Thread.currentThread().interrupt();
            throw ex;
        } catch (final PluginException ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, ex.getNotificationLevel(), ex);
            throw ex;
        } catch (final Exception ex) {
            auditPluginError(plugin, ex);
            reportException(plugin.getName(), interaction, currentReport, PluginNotificationLevel.ERROR, ex);
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
            } catch (final Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
        return null;
    }

    protected ExecutorService getPluginExecutor() {
        return pluginExecutor;
    }

    private void auditPluginError(final Plugin plugin, final Throwable error) {
        try {
            ConstellationLogger.getDefault().pluginError(plugin, error);
        } catch (final Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }
    }

    /**
     * Report the exception to the user using the {@code notify} method, set the
     * current report to its error state and log the {@code Exception}
     *
     * @param pluginName the name of the {@code Plugin} being executed
     * @param interaction the PluginInteraction object to report to
     * @param currentReport the current report
     * @param level the level of the exception
     * @param ex the exception
     */
    private void reportException(final String pluginName,
            final PluginInteraction interaction, final PluginReport currentReport,
            final PluginNotificationLevel level, final Exception ex) {
        if (currentReport != null) {
            currentReport.setError(ex);
        }

        if (ex instanceof InterruptedException) {
            final String message = String.format("Plugin cancelled: %s", pluginName);
            interaction.notify(PluginNotificationLevel.INFO, message);
            LOGGER.log(Level.INFO, message, ex);
        } else if (ex instanceof PluginException) {
            interaction.notify(level, ex.getLocalizedMessage());
            LOGGER.log(Level.INFO, String.format("Plugin exception caught in %s", pluginName), ex);
        } else {
            final String message = String.format("Unexpected exception caught in %s", pluginName);
            switch (level) {
                case FATAL:
                case ERROR:
                    LOGGER.log(Level.SEVERE, message, ex);
                    break;
                case WARNING:
                    LOGGER.log(Level.WARNING, message, ex);
                    break;
                case INFO:
                    LOGGER.log(Level.INFO, message, ex);
                    break;
                case DEBUG:
                    LOGGER.log(Level.FINE, message, ex);
                    break;
                default:
                    break;
            }
        }
    }
}
