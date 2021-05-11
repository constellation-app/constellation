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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
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
 * Interface for plugins that work in an interactive mode
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

    private static final String STRING_STRING_FORMAT = "%s: %s";

    public DefaultPluginInteraction(final PluginManager pluginManager, final PluginReport pluginReport) {
        this.pluginManager = pluginManager;
        this.pluginReport = pluginReport;
    }
    
    public PluginReport getPluginReport() {
        return pluginReport;
    }
    
    public ProgressHandle getProgress() {
        return progress;
    }

    public Timer getTimer() {
        return timer;
    }
    
    @Override
    public boolean isInteractive() {
        return pluginManager.isInteractive();
    }

    @Override
    public void setBusy(final String graphId, final boolean busy) {
        final GraphNode graphNode = pluginManager.getGraphNode();
        if (graphNode != null) {
            graphNode.makeBusy(busy);
        }
    }

    protected String createProgressTitle() {
        final StringBuilder result = new StringBuilder();

        final GraphNode graphNode = pluginManager.getGraphNode();
        if (graphNode != null) {
            final String graphName = graphNode.getName();
            if (graphName != null) {
                result.append(graphName);
                result.append(": ");
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
                LOGGER.severe(String.format(STRING_STRING_FORMAT, title, message));
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
                LOGGER.severe(String.format(STRING_STRING_FORMAT, title, message));
                break;

            case WARNING:
                NotificationDisplayer.getDefault().notify(title,
                        UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                        message,
                        null
                );
                LOGGER.warning(String.format(STRING_STRING_FORMAT, title, message));
                break;

            case INFO:
                final Message statusMessage = StatusDisplayer.getDefault().setStatusText(String.format(STRING_STRING_FORMAT, title, message), 10);
                statusMessage.clear(5000);
                LOGGER.info(String.format(STRING_STRING_FORMAT, title, message));
                break;

            case DEBUG:
                LOGGER.fine(String.format(STRING_STRING_FORMAT, title, message));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean confirm(final String message) {
        final int[] result = new int[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                final NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.QUESTION_MESSAGE);
                descriptor.setOptions(new Object[]{NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION});
                result[0] = (int) DialogDisplayer.getDefault().notify(descriptor);
            });
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (final InvocationTargetException ex) {
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
        if (!parameters.hasMultiLineStringParameter()) {
            dialog.showAndWait();
        } else {
            dialog.showAndWaitNoFocus();
        }
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            result = true;
        }

        return result;
    }

    protected class Timer extends Thread {

        private final long startTime = System.currentTimeMillis();

        public String getTime() {
            return getTime(startTime, System.currentTimeMillis());
        }
        
        protected String getTime(final long startTime, final long endTime) {
            // getTime(long, long) was added to allow unit testing to occur so in practice we would not expect this situation to occur
            // it should still be handled just in case though
            // if not for the fact that this function version was added purely to allow testing, we would throw an exception here
            if (startTime < 0 || endTime < 0 || startTime > endTime) {
                return "00:00:00";
            }
            
            long interval = (endTime - startTime) / 1000;

            final long seconds = interval % 60;
            interval /= 60;

            final long minutes = interval % 60;
            interval /= 60;

            final long hours = interval;

            final StringBuilder result = new StringBuilder();
            result.append(hours < 10 ? "0" + hours : hours);
            result.append(SeparatorConstants.COLON);
            result.append(minutes < 10 ? "0" + minutes : minutes);
            result.append(SeparatorConstants.COLON);
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
                    
                    if ("Finished".equalsIgnoreCase(currentMessage)) {
                        return;
                    }
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
