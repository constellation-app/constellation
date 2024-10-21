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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;

/**
 * Implementations of the PluginInteraction interface provide a
 * platform-independent way for plugins to interact with the UI of
 * Constellation.
 *
 * @author sirius
 */
public interface PluginInteraction {

    /**
     * Returns true if the plugin is to be run in interactive mode.
     *
     * @return true if the plugin is to be run in interactive mode.
     */
    boolean isInteractive();

    /**
     * Signals to the user that the current graph is busy.
     *
     * @param graphId the id of the graph.
     * @param busy True to indicate that the graph is busy, false to unset.
     */
    void setBusy(String graphId, boolean busy);

    /**
     * Returns the message that is currently being displayed to the user.
     *
     * @return the message that is currently being displayed to the user.
     */
    public String getCurrentMessage();

    /**
     * Signals to the user the current progress of the plugin with a message that persists after the plugin has completed.
     *
     * @param currentStep the current step the plugin is currently performing.
     * @param totalSteps the total number of steps the plugin must perform
     * before completion.
     * @param message a message describing the step the plugin is currently
     * performing.
     * @param cancellable is the plugin able to be canceled at this time?
     * @param parameters set of parameters for this plugin if any.
     * @param selected total number of selected items on graph.
     * @throws InterruptedException if the plugin has been canceled.
     */
    void setProgress(final int currentStep, final int totalSteps, final String message, final boolean cancellable, final PluginParameters parameters, final int selected) throws InterruptedException;
      /**
     * Signals to the user the current progress of the plugin with a message that persists after the plugin has completed.
     *
     * @param currentStep the current step the plugin is currently performing.
     * @param totalSteps the total number of steps the plugin must perform
     * before completion.
     * @param message
     * @param cancellable is the plugin able to be canceled at this time?
     * @param parameters set of parameters for this plugin if any.
     * @throws InterruptedException if the plugin has been canceled.
     */
    void setProgress(final int currentStep, final int totalSteps, final String message, final boolean cancellable, final PluginParameters parameters) throws InterruptedException;
      /**
     * Signals to the user the current progress of the plugin with a message that persists after the plugin has completed.
     *
     * @param currentStep the current step the plugin is currently performing.
     * @param totalSteps the total number of steps the plugin must perform
     * before completion.
     * @param cancellable is the plugin able to be canceled at this time?
     * @throws InterruptedException if the plugin has been canceled.
     */
    void setProgress(final int currentStep, final int totalSteps, final boolean cancellable) throws InterruptedException;
    
    /**
     * Signals to the user the current progress of the plugin with a message that persists after the plugin has completed.
     *
     * @param currentStep the current step the plugin is currently performing.
     * @param totalSteps the total number of steps the plugin must perform
     * before completion.
     * @param cancellable is the plugin able to be canceled at this time?
     * @param message
     * @throws InterruptedException if the plugin has been canceled.
     */
    void setProgress(final int currentStep, final int totalSteps, final String message, final boolean cancellable) throws InterruptedException;
    
       /**
     * Signals to the user the current execution stage of the plugin
     * providing a message that does not persist after the plugin has completed.
     * Should be used to represent the high level execution of the plugin.
     * i.e. Navigation through methods, of read(), query(), edit() and execute().
     * 
     * @param currentStep the current step the plugin is currently performing.
     * @param totalSteps the total number of steps the plugin must perform
     * before completion.
     * @param executionStage current executionStrate of the plugin
     * @param message a message describing the step the plugin is currently
     * performing.
     * @param cancellable is the plugin able to be canceled at this time?
     * @throws InterruptedException if the plugin has been canceled.
     */
    void setExecutionStage(final int currentStep, final int totalSteps, final String executionStage, final String message, final boolean cancellable) throws InterruptedException;
    
    /**
     * Sends a notification message to the user. Depending on the notification
     * level, the framework may choose to notify the user in a different way,
     * such as through status updates or dialog boxes.
     *
     * @param level the notification level.
     * @param message the message to be sent to the user.
     */
    void notify(final PluginNotificationLevel level, final String message);

    /**
     * Present a confirmation message to the user.
     *
     * @param message A message to present to the user.
     *
     * @return true if the user answered affirmatively, false otherwise.
     */
    boolean confirm(final String message);

    /**
     * Presents a dialog box to the user showing an auto-generated form for the
     * specified parameters. This allows the user to set the values for these
     * parameters.
     *
     * @param promptName the name of the dialog box.
     * @param parameters the parameters to be displayed and edited.
     * @param helpID the help ID of the plugin.
     * @return true if the user selected "OK" or false if the user selected
     * "Cancel".
     */
    boolean prompt(final String promptName, final PluginParameters parameters, final String helpID);
    
    /**
     * Presents a dialog box to the user showing an auto-generated form for the
     * specified parameters.This allows the user to set the values for these
     * parameters.
     *
     * @param promptName the name of the dialog box.
     * @param parameters the parameters to be displayed and edited.
     * @param disclaimer the disclaimer text for interactively executed plugins.
     * @param helpID the help ID of the plugin.
     * @return true if the user selected "OK" or false if the user selected
     * "Cancel".
     */
    boolean prompt(final String promptName, final PluginParameters parameters, final String disclaimer, final String helpID);
}
