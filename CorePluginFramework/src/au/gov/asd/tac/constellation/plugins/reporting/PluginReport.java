/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.reporting;

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A PluginReport is created each time a plugin is executed. It stores
 * information about the execution of the plugin such as start and stop time,
 * the parameters that the were passed to the plugin during execution, and any
 * errors that were thrown during execution.
 * <p>
 * If the plugin executed any sub-plugins on its own thread then reports
 * representing these plugin executions are attached as child reports.
 * <p>
 * PluginReports are stored in a {@link GraphReport} which holds all
 * PluginReports executed on a specified graph.
 *
 * @author sirius
 */
public class PluginReport {

    private final List<PluginReportListener> listeners = new ArrayList<>();

    private final GraphReport graphReport;
    private final String pluginName;
    private final String pluginDescription;

    private final long startTime;
    private long stopTime = -1;

    private String executionStage = PluginExecutionStageConstants.WAITING;
    private final List<String> runningStateLog = new ArrayList<>();
    private final List<String> messageLog = new ArrayList<>();
    
    private int currentStep = 1;
    private int totalSteps = -1;

    private Throwable error = null;

    private final List<PluginReport> childReports = Collections.synchronizedList(new ArrayList<>());
    private final List<PluginReport> uChildReports = Collections.unmodifiableList(childReports);

    private final String[] tags;

    private final int position;
    private boolean isUndone = false;
    
    public PluginReport(final GraphReport graphReport, final Plugin plugin) {
        this.graphReport = graphReport;
        this.pluginName = plugin.getName();
        this.pluginDescription = plugin.getDescription();
        this.startTime = System.currentTimeMillis();
        this.tags = plugin.getTags();
        this.position = graphReport.getPluginReports().size();

        graphReport.includeTags(this);
    }

    /**
     * Adds a new listener to this plugin report. The listener will be notified
     * when the plugin report changes. Common changes include an update to the
     * plugin's current message, an update to the plugins progress, or when the
     * plugin finishes.
     *
     * @param listener the listener to add.
     */
    public synchronized void addPluginReportListener(final PluginReportListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove an existing plugin report listener from this plugin report.
     *
     * @param listener the listener to remove.
     */
    public synchronized void removePluginReportListener(final PluginReportListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that this plugin report has changed. This has to be
     * manually called by processes that change the plugin report as the plugin
     * report does not do this automatically. This is to allow a process to make
     * several changes and only notify the listeners once for efficiency.
     */
    public synchronized void firePluginReportChangedEvent() {
        listeners.forEach(listener -> listener.pluginReportChanged(this));
    }

    /**
     * Return the graph report that holds this plugin report.
     *
     * @return the graph report that holds this plugin report.
     */
    public GraphReport getGraphReport() {
        return graphReport;
    }

    /**
     * Record that the plugin has finished execution.
     */
    public void stop() {
        this.stopTime = System.currentTimeMillis();
        this.currentStep = 1;
        this.totalSteps = 0;
    }

    /**
     * Returns the time that this plugin started execution.
     *
     * @return the time that this plugin started execution.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the time that this plugin stopped execution or -1 if this plugin
     * is still running.
     *
     * @return the time that this plugin stopped execution or -1 if this plugin
     * is still running.
     */
    public long getStopTime() {
        return stopTime;
    }

    /**
     * Returns the name of this plugin.
     *
     * @return the name of this plugin.
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Returns the description of this plugin.
     *
     * @return the description of this plugin.
     */
    public String getPluginDescription() {
        return pluginDescription;
    }

    /**
     * Returns the current message from this plugin.
     *
     * @return the current message from this plugin.
     */
    public String getLastMessage() {
        if ((executionStage.equals(PluginExecutionStageConstants.COMPLETE) || executionStage.equals(PluginExecutionStageConstants.STOPPED)) && !this.messageLog.isEmpty()){
            return this.messageLog.get(this.messageLog.size()-1);
        } else {
            return this.runningStateLog.isEmpty() ? "" : this.runningStateLog.get(this.runningStateLog.size()-1);
        }
    }
    
    /**
     * Returns the all messages from this plugin.
     *
     * @return the completed messages from this plugin.
     */
    public String getAllMessages() {
        if ((executionStage.equals(PluginExecutionStageConstants.COMPLETE) || executionStage.equals(PluginExecutionStageConstants.STOPPED)) && !this.messageLog.isEmpty()){
            return String.join(System.lineSeparator(), this.messageLog);
        } else {
            return this.runningStateLog.isEmpty() ? "" : String.join(System.lineSeparator(), this.runningStateLog);
        }
    }
    
    /**
     * Sets the current message from this plugin.
     *
     * @param message the new message.
     */
    public void addMessage(final String message) {
        this.messageLog.add(message);
        this.runningStateLog.add(message);
    }

    /**
     * Returns the current step of its execution that the plugin is up to. A
     * plugin describes its progress by providing a current step and a total
     * steps value.
     *
     * @return the current step of its execution that the plugin is up to.
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Set the current step of the plugins execution.
     *
     * @param currentStep the new current step.
     */
    public void setCurrentStep(final int currentStep) {
        this.currentStep = currentStep;
    }

    /**
     * Returns the total steps for the plugins execution. The total steps
     * represents the number of tasks that the plugin must complete to finish
     * execution. The plugin should also set the current step value which
     * indicates how many of these tasks have been completed.
     *
     * @return the total steps for the plugins execution.
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Sets the total steps for the plugins execution. The total steps
     * represents the number of tasks that the plugin must complete to finish
     * execution. The plugin should also set the current step value which
     * indicates how many of these tasks have been completed.
     *
     * @param totalSteps the new total steps.
     */
    public void setTotalSteps(final int totalSteps) {
        this.totalSteps = totalSteps;
    }

    /**
     * Returns the throwable that has been thrown during plugin execution or
     * null if no error has occurred.
     *
     * @return the throwable that has been thrown during plugin execution or
     * null if no error has occurred.
     */
    public Throwable getError() {
        return error;
    }

    /**
     * Sets the throwable that has been thrown during plugin execution. The
     * default value is null.
     *
     * @param error the new error.
     */
    public void setError(final Throwable error) {
        this.error = error;
        if (error instanceof InterruptedException){
            this.messageLog.add("Error: Plugin was interrupted.");
        } else if (error instanceof PluginException){
            this.messageLog.add("Error: " + error.getMessage());
        } else {
            this.messageLog.add("Error: An unkown error occcured.");
        }
        this.setExecutionStage(PluginExecutionStageConstants.STOPPED);
    }

    /**
     * Returns the tags associated with this plugin.
     *
     * @return the tags associated with this plugin.
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Returns True if there is a "LOW LEVEL" tag present, False otherwise.
     *
     * @return True if there is a "LOW LEVEL" tag present, False otherwise.
     */
    public boolean hasLowLevelTag() {
        for (final String tag : tags) {
            if (PluginTags.LOW_LEVEL.equals(tag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the position that this plugin report holds in the list of plugin
     * reports held by its graph report.
     *
     * @return the position that this plugin report holds in the list of plugin
     * reports held by its graph report.
     */
    public int getPosition() {
        return position;
    }

    /**
     * A convenience method that returns true only if the specified collection
     * of tags contains all tags of this plugin.
     *
     * @param filteredTags the collection of tags.
     *
     * @return true if the specified collection of tags contains all tags of
     * this plugin.
     */
    public boolean containsAllTags(final Collection<String> filteredTags) {
        for (String tag : tags) {
            if (!filteredTags.contains(tag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A convenience method that returns true only if the specified collection
     * of tags contains at least one of the tags of this plugin.
     *
     * @param allowedTags the collection of tags.
     *
     * @return true if the specified collection of tags contains at least one of
     * the tags of this plugin.
     */
    public boolean containsAnyTag(final Collection<String> allowedTags) {
        for (String tag : tags) {
            if (allowedTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "PluginReport{" + "graphReport=" + graphReport + ", pluginName=" + pluginName + ", pluginDescription=" + pluginDescription + ", startTime=" + startTime + ", stopTime=" + stopTime + ", message=" + this.getLastMessage() + ", currentStep=" + currentStep + ", totalSteps=" + totalSteps + '}';
    }

    /**
     * Adds a child report to this plugin report. A child report indicates that
     * during this plugin execution, another plugin was called synchronously on
     * the same thread.
     *
     * @param plugin the plugin that needs a new plugin report.
     *
     * @return the newly created PluginReport that represents the plugin.
     */
    public PluginReport addChildReport(final Plugin plugin) {
        PluginReport childReport = new PluginReport(graphReport, plugin);
        childReports.add(childReport);
        listeners.stream().forEach(listener -> listener.addedChildReport(this, childReport));
        return childReport;
    }

    /**
     * Returns the list of child plugin reports for this plugin report. A child
     * plugin report indicates that another plugin was called synchronously on
     * the same thread during the execution of the plugin represented by this
     * plugin report.
     *
     * @return a list of all the plugin reports that are children of this plugin
     * report.
     */
    public List<PluginReport> getUChildReports() {
        return uChildReports;
    }
    
    /**
     * Returns the list messages sent to the plugin report. during its life
     * Note that this method prints all messages and thus the number of messaged 
     * sent to the Plugin reporter should be appropriate.
     *
     * @return a string representing a list of all the report messages sent to
     * this report.
     */
    public String getReportLog() {
        if (executionStage.equals(PluginExecutionStageConstants.COMPLETE)){
            return this.messageLog.isEmpty() ? this.getLastMessage() : logToString(this.messageLog);
        } else if (!executionStage.equals(PluginExecutionStageConstants.STOPPED)){
            return logToString(this.runningStateLog);
        } else {
            return this.getLastMessage();
        }
    }
    
    /**
     * Converts a Collection of Strings to a single string with new line separators
     * between each string in the collection. 
     * 
     * @param log a collection of strings
     *
     * @return a string representing a list of all the messages in the log.
     */
    public String logToString(final Collection<String> log){
        final StringBuilder bld = new StringBuilder();
        
        for (int i = 0; i < log.size(); ++i) {
            if (i != 0){
                bld.append(SeparatorConstants.NEWLINE);
            }
            bld.append(log.toArray()[i]);
        }
        
        return bld.toString();
    }
    
    /**
     * Sets the message to be displayed to the user
     * to indicate the execution stage of the plugin.
     * These messages do not persist after plugin execution.
     * 
     * @param message 
     */
    public void setRunningStateMessage(final String message) {
        this.runningStateLog.add(message);
    }
    
    /**
     * Gets the most recent message indicating the execution stage of the plugin.
     * These messages will persist after plugin execution.
     * 
     * @return A string indicating the stage of execution
     */
    public String getRunningStateMessage() {
        return this.runningStateLog.isEmpty() ? "" : this.runningStateLog.get(this.runningStateLog.size()-1);
    }
    
    /**
     * Set the stage of execution as a string using PluginExecutionStageContants.
     * This attribute controls the messages displayed in the plugin reporter.
     * Setting an execution stage of waiting or running will cause runningState Logs to be shown.
     * Setting an execution stage of finished, stopped will cause messageLogs to be shown.
     * 
     * @param  executionStage representing the stage of execution of the plugin.
     */       
    public void setExecutionStage(final String executionStage) {
        if (!PluginExecutionStageConstants.STOPPED.equals(this.executionStage)) {
            this.executionStage = executionStage;
        } 
    }
    
    /**
     * Gets the current execution stage of the plugin as a string. 
     * 
     * @return Current execution stage of the plugin
     */  
    public final String getExecutionStage() {
        return this.executionStage;
    }

    public boolean isUndone() {
        return isUndone;
    }

    public void setUndone(final boolean isUndone) {
        this.isUndone = isUndone;
    }
}


