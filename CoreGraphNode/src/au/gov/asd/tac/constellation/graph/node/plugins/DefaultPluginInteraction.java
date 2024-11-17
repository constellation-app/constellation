/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.DEFAULT_OPTION;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Cancellable;

/**
 * Interface for plugins that work in an interactive mode.
 * 
 * This class allows for plugins to interact with constellations reporting mechanisms, the {@link NotifyDisplayer} and the {@link PluginReporter}.
 * Capabilities for terminating plugins are also available in the class due to the need from the relevant reporting mechanism.
 * 
 * <p>{@link PluginReporter} interactions can be facilitated using {@code setProgressTimestamp()}, {@code setProgress()} and {@code setExecutionStage()}.</p> 
 * 
 * <p>{@link NotifyDisplayer} interactions can be facilitated using {@code confirm()} and {@code notify()}.</p> 
 * <p>The following is a summary of the various visual presentations based on the supplied {@link PluginNotificationLevel}:</p>
 * <ul>
 * <li>{@code PluginNotificationLevel.FATAL} and
 * {@code PluginNotificationLevel.ERROR} type messages will have a dialog
 * presented</li>
 * <li>{@code PluginNotificationLevel.WARNING} will have a balloon notification
 * popup</li>
 * <li>{@code PluginNotificationLevel.INFO} will have the message shown in the
 * applications status notification area which is to the bottom left area</li>
 * <li>{@code PluginNotificationLevel.DEBUG} messages will be logged if the FINE
 * log to this class in enabled</li>
 * </ul>
 * 
 * @author capricornunicorn123
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

    private static final String LOGGING_FORMAT = "{0}: {1}";
    private static final String STRING_FORMAT = "%s: %s";

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
                result.append(SeparatorConstants.COLON);
                result.append(" ");
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
    public void setProgressTimestamp(final boolean addTimestamp) throws InterruptedException {

        if (pluginReport != null && addTimestamp) {
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",  Locale.getDefault());
            final Date date = new Date(pluginReport.getStartTime());
            pluginReport.addMessage("Time: " + format.format(date));
        }
    }
    
    @Override
    public void setProgress(final int currentStep, final int totalSteps,
            final String message, final boolean cancellable) throws InterruptedException {       
        setProgress(currentStep, totalSteps, message, cancellable, null, -1);
    }

    @Override
    public void setProgress(final int currentStep, final int totalSteps,
            final String message, final boolean cancellable,
            final PluginParameters params) throws InterruptedException {
        setProgress(currentStep, totalSteps, message, cancellable, params, -1);
    }
    
    @Override
    public void setProgress(final int currentStep, final int totalSteps,
            final String message, final boolean cancellable,
            final PluginParameters params, final int selected) throws InterruptedException {

        if (pluginReport != null) {
            final StringBuilder builder = new StringBuilder();            

            if (params != null) {
                final Map<String, PluginParameter<?>> parameters = params.getParameters();
                for (final String key : parameters.keySet()) {
                    builder.append(String.format("%s : %s \n", parameters.get(key).getName(), parameters.get(key).getStringValue()));
                }
                pluginReport.addMessage('\n' + builder.toString());
            }
            if (selected > -1) {
                pluginReport.addMessage("Selected count: " + selected);
            }
            pluginReport.addMessage(message);
        }
        currentMessage = message;
        
        this.setProgress(currentStep, totalSteps, cancellable);
    }

    @Override
    public void setProgress(final int currentStep, final int totalSteps, final boolean cancellable) throws InterruptedException {     
        if (pluginReport != null) {
            pluginReport.setCurrentStep(currentStep);
            pluginReport.setTotalSteps(totalSteps);
            pluginReport.firePluginReportChangedEvent();
        }

        // Allow the plugin to be interrupted
        if (cancellable && Thread.interrupted()) {
            throw new InterruptedException();
        }
        // If the plugin is indeterminate...
        if (totalSteps < 0) {

            if (progress == null) {
                progress = ProgressHandle.createHandle(createProgressTitle(), this);
                progress.start();
                timer = new Timer();
                progress.progress(timer.getTime() + " " + currentMessage);
                timer.start();
            } else {
                progress.switchToIndeterminate();
                progress.progress(timer.getTime() + " " + currentMessage);
            }
            
        // If the plugin has finished....    
        } else if (currentStep > totalSteps) {

            if (progress != null) {
                timer.interrupt();
                progress.finish();
                progress = null;
            }
            
        // If the plugin is determinate...
        } else {

            if (progress == null) {
                progress = ProgressHandle.createHandle(createProgressTitle(), this);
                progress.start();
                timer = new Timer();
                progress.switchToDeterminate(totalSteps);
                progress.progress(timer.getTime() + " " + currentMessage, currentStep);
                timer.start();
            } else {
                progress.switchToDeterminate(totalSteps);
                progress.progress(timer.getTime() + " " + currentMessage, currentStep);
            }

        }
    }
    
    
    @Override
    public void setExecutionStage(final int currentStep, final int totalSteps, final String executionStage, final String message, final boolean cancellable) throws InterruptedException {
        if (pluginReport != null) {
            pluginReport.setExecutionStage(executionStage);
            pluginReport.setRunningStateMessage(message);
        }
        currentMessage = message;
        this.setProgress(currentStep, totalSteps, cancellable);
    }
    
    @Override
    public void notify(final PluginNotificationLevel level, final String message) {
        final String title = pluginManager.getPlugin().getName();
        switch (level) {
            case FATAL -> {
                NotifyDisplayer.display(new NotifyDescriptor(
                        "Fatal Error:\n" + message,
                        title,
                        DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION
                ));
                LOGGER.log(Level.SEVERE, LOGGING_FORMAT, new Object[]{title, message});
            }

            case ERROR -> {
                NotifyDisplayer.display(new NotifyDescriptor(
                        "Error:\n" + message,
                        title,
                        DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION
                ));
                LOGGER.log(Level.SEVERE, LOGGING_FORMAT, new Object[]{title, message});
            }

            case WARNING -> {
                NotificationDisplayer.getDefault().notify(title,
                        UserInterfaceIconProvider.WARNING.buildIcon(16, ConstellationColor.DARK_ORANGE.getJavaColor()),
                        message,
                        null
                );
                LOGGER.log(Level.WARNING, LOGGING_FORMAT, new Object[]{title, message});
            }

            case INFO -> {
                final String statusText = String.format(STRING_FORMAT, title, message);
                final Message statusMessage = StatusDisplayer.getDefault().setStatusText(statusText, 10);
                statusMessage.clear(5000);
                LOGGER.log(Level.INFO, LOGGING_FORMAT, new Object[]{title, message});
            }

            case DEBUG -> LOGGER.log(Level.FINE, LOGGING_FORMAT, new Object[]{title, message});

            default -> {
                // Do Nothing
            }
        }
    }

    @Override
    public boolean confirm(final String message) {
        final NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                message,
                NotifyDescriptor.QUESTION_MESSAGE
        );

        descriptor.setOptions(new Object[]{
            NotifyDescriptor.YES_OPTION,
            NotifyDescriptor.NO_OPTION
        });

        try {
            return NotifyDisplayer.displayAndWait(descriptor).get()
                    == NotifyDescriptor.YES_OPTION;
        } catch (ExecutionException ex) {
            LOGGER.log(Level.SEVERE, "Failed to open confirm dialog", ex);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
            Thread.currentThread().interrupt();
        }

        return false;
    }

    @Override
    public boolean cancel() {
        pluginManager.getPluginThread().interrupt();
        return true;
    }
  
    @Override
    public boolean prompt(final String promptName, final PluginParameters parameters, final String interaction, final String helpID) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Plugins should not be run on the EDT!");
        }

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(promptName, parameters, interaction, helpID);

        if (!parameters.hasMultiLineStringParameter()) {
            dialog.showAndWait();
        } else {
            dialog.showAndWaitNoFocus();
        }

        return dialog.isAccepted();
    }
    
    @Override
    public boolean prompt(final String promptName, final PluginParameters parameters, final String helpID) {
        return prompt(promptName, parameters, null, helpID);
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
