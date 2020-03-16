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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.DEFAULT_OPTION;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * interface for plugins that work in an interactive mode
 *
 * @author sirius
 */
public class DefaultPluginInteraction implements PluginInteraction, Cancellable {

    private static final Logger LOGGER = Logger.getLogger(DefaultPluginInteraction.class.getName());
    private static final String PLUGIN_INTERACTION_THREAD_NAME = "Plugin Interaction";
    private final PluginManager pluginManager;
    private final PluginReport pluginReport;
    private ProgressHandle progress;
    private String currentMessage;
    private Timer timer = null;

    public DefaultPluginInteraction(final PluginManager pluginManager, PluginReport pluginReport) {
        this.pluginManager = pluginManager;
        this.pluginReport = pluginReport;
    }

    @Override
    public boolean isInteractive() {
        return pluginManager.isInteractive();
    }

    @Override
    public void setBusy(final String graphId, final boolean busy) {
        GraphNode graphNode = pluginManager.getGraphNode();
        if (graphNode != null) {
            graphNode.makeBusy(busy);
        }
    }

    private String createProgressTitle() {
        StringBuilder result = new StringBuilder();

        GraphNode graphNode = pluginManager.getGraphNode();
        if (graphNode != null) {
            String graphName = graphNode.getName();
            if (graphName != null) {
                result.append(graphName);
                result.append(":  ");
            }
        }

        result.append(pluginManager.getPlugin().getName());

        return result.toString();
    }

    @Override
    public String getCurrentMessage() {
        return currentMessage;
    }

    @Override
    public void setProgress(final int currentStep, final int totalSteps, final String message, final boolean cancellable) throws InterruptedException {

        if (pluginReport != null) {
            pluginReport.setCurrentStep(currentStep);
            pluginReport.setTotalSteps(totalSteps);
            pluginReport.setMessage(message);
            pluginReport.firePluginReportChangedEvent();
        }

        currentMessage = message;

        // Allow the plugin to be interrupted
        if (cancellable && Thread.interrupted()) {
            throw new InterruptedException();
        }

        // If the plugin has finished....
        if (currentStep > totalSteps) {

            if (progress != null) {
                timer.interrupt();
                progress.finish();
                progress = null;
            }

            // If the plugin is indeterminate...
        } else if (totalSteps <= 0) {

            if (progress == null) {
                progress = ProgressHandle.createHandle(createProgressTitle(), this);
                progress.start();
                timer = new Timer();
                progress.progress(timer.getTime() + " " + message);
                timer.start();
            } else {
                progress.switchToIndeterminate();
                progress.progress(timer.getTime() + " " + message);
            }

            // If the plugin is determinate...
        } else {

            if (progress == null) {
                progress = ProgressHandle.createHandle(createProgressTitle(), this);
                progress.start();
                timer = new Timer();
                progress.switchToDeterminate(totalSteps);
                progress.progress(timer.getTime() + " " + message, currentStep);
                timer.start();
            } else {
                progress.switchToDeterminate(totalSteps);
                progress.progress(timer.getTime() + " " + message, currentStep);
            }

        }
    }

    @Override
    public void notify(final PluginNotificationLevel level, final String message) {
        final String title = pluginManager.getPlugin().getName();
        switch (level) {
            case FATAL:
                SwingUtilities.invokeLater(() -> {
                    final NotifyDescriptor ndf = new NotifyDescriptor(
                            "Fatal error:\n" + message,
                            title,
                            DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[]{NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION
                    );
                    DialogDisplayer.getDefault().notify(ndf);
                });
                LOGGER.severe(String.format("%s: %s", title, message));
                break;

            case ERROR:
                SwingUtilities.invokeLater(() -> {
                    final NotifyDescriptor nde = new NotifyDescriptor(
                            "Error:\n" + message,
                            title,
                            DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[]{NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION
                    );
                    DialogDisplayer.getDefault().notify(nde);
                });
                LOGGER.severe(String.format("%s: %s", title, message));
                break;

            case WARNING:
                NotificationDisplayer.getDefault().notify(title,
                        UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                        message,
                        null
                );
                LOGGER.warning(String.format("%s: %s", title, message));
                break;

            case INFO:
                final Message statusMessage = StatusDisplayer.getDefault().setStatusText(String.format("%s: %s", title, message), 10);
                statusMessage.clear(5000);
                LOGGER.info(String.format("%s: %s", title, message));
                break;

            case DEBUG:
                LOGGER.fine(String.format("%s: %s", title, message));
                break;
        }
    }

    @Override
    public boolean confirm(final String message) {
        final int[] result = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.QUESTION_MESSAGE);
                descriptor.setOptions(new Object[]{NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION});
                Integer option = (Integer) DialogDisplayer.getDefault().notify(descriptor);
                result[0] = option;
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0] == 0;
    }

    @Override
    public boolean cancel() {
        pluginManager.getPluginThread().interrupt();
        return true;
    }

    @Override
    public boolean prompt(final String promptName, final PluginParameters parameters) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Plugins should not be run on the EDT!");
        }

        boolean result = false;

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(promptName, parameters);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            result = true;
        }

        return result;
    }

    private class Timer extends Thread {

        public long startTime = System.currentTimeMillis();

        public String getTime() {
            long now = System.currentTimeMillis();
            long interval = (now - startTime) / 1000;

            long seconds = interval % 60;
            interval /= 60;

            long hours = interval % 60;
            interval /= 60;

            long days = interval;

            StringBuilder result = new StringBuilder();
            result.append(days < 10 ? "0" + days : days);
            result.append(':');
            result.append(hours < 10 ? "0" + hours : hours);
            result.append(':');
            result.append(seconds < 10 ? "0" + seconds : seconds);

            return result.toString();
        }

        @Override
        public void run() {
            setName(PLUGIN_INTERACTION_THREAD_NAME);
            try {
                while (true) {
                    Thread.sleep(1000 - (System.currentTimeMillis() % 1000));

                    if (progress == null) {
                        return;
                    }

                    progress.progress(getTime() + " " + currentMessage);
                }
            } catch (InterruptedException ex) {
            }
        }
    }
}
