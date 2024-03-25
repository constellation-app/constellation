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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.attribute.io.AbstractGraphIOProvider;
import java.time.ZoneId;

/**
 * This class holds the state variables used by the
 * <code>TimelineTopComponent</code>.
 * <p>
 * It also implements <code>AbstractGraphIOProvider</code> so it can read and
 * write itself to the graph. (Note: The actual implementation of this passes
 * these duties off to a helper class).
 *
 * @see TimelineTopComponent
 * @see AbstractGraphIOProvider
 *
 * @author betelgeuse
 */
public class TimelineState {

    /**
     * The name of the graph attribute used to hold the instance.
     */
    private int exclusionState = 0;
    private boolean showingLabels = false;
    private boolean showingSelectedOnly;
    private double lowerTimeExtent;
    private double upperTimeExtent;
    private String dateTimeAttr;
    private String nodeLabelsAttr;
    private ZoneId timeZone;

    /**
     * Construct a new <code>TimelineState</code>.
     */
    public TimelineState() {
        lowerTimeExtent = 0;
        upperTimeExtent = 0;
        showingSelectedOnly = false;
    }

    /**
     * Constructs a new <code>TimelineState</code> instance.
     *
     * @param lowerTimeExtent The lower time extent from the timeline window
     * view.
     * @param upperTimeExtent The upper time extent from the timeline window
     * view.
     * @param isDimming Whether or not dimming is enabled for the timeline
     * window on this graph.
     * @param isShowingSelectedOnly should only the selected items be shown.
     * @param dateTimeAttr The datetime attribute used to get temporal values
     * for the timeline.
     * @param isShowingLabels Whether or not node labels are being shown on the
     * timeline window.
     * @param nodeLabelsAttr The attribute being shown for node labels (if any).
     * @param timeZone the time zone to use when showing events.
     */
    public TimelineState(final double lowerTimeExtent, final double upperTimeExtent,
            final int exclusionState, final boolean showingSelectedOnly,final String dateTimeAttr,
            final boolean showingLabels, final String nodeLabelsAttr, final ZoneId timeZone) {
        this.lowerTimeExtent = lowerTimeExtent;
        this.upperTimeExtent = upperTimeExtent;
        this.exclusionState = exclusionState;
        this.showingSelectedOnly = showingSelectedOnly;
        this.dateTimeAttr = dateTimeAttr;
        this.showingLabels = showingLabels;
        this.nodeLabelsAttr = nodeLabelsAttr;
        this.timeZone = timeZone;
    }

    /**
     * Returns whether or not the timeline is currently dimming events outside
     * its 'view'.
     *
     * @return 1 if dimming external events, 2 if hiding external events, else
     * show external events
     */
    public int getExclusionState() {
        return exclusionState;
    }

    /**
     * Sets whether or not the timeline should dim events outside its 'view'.
     *
     * @param isDimming 1 if dimming external events, 2 if hiding external
     * events, else show external events
     */
    public void setExclusionState(final int exclusionState) {
        this.exclusionState = exclusionState;
    }

    /**
     * Are only selected transactions being shown.
     *
     * @return True if only selected transactions being shown, false otherwise.
     */
    public boolean isShowingSelectedOnly() {
        return showingSelectedOnly;
    }

    /**
     * Sets whether or not only selected transactions are being shown.
     *
     * @param isShowingSelectedOnly True to only show selected transactions,
     * false to show all transactions.
     */
    public void setShowingSelectedOnly(final boolean showingSelectedOnly) {
        this.showingSelectedOnly = showingSelectedOnly;
    }

    /**
     * Returns whether or not the timeline should display node labels.
     *
     * @return <code>true</code> if node labels are to be shown,
     * <code>false</code> if not.
     */
    public boolean isShowingNodeLabels() {
        return showingLabels;
    }

    /**
     * Sets whether or not the timeline should display labels on nodes.
     *
     * @param isShowingNodeLabels <code>true</code> if node labels are being
     * displayed, <code>false</code> if not.
     */
    public void setShowingNodeLabels(final boolean showingNodeLabels) {
        this.showingLabels = showingNodeLabels;
    }

    /**
     * Returns the lower time extent that the timeline window is currently
     * showing.
     *
     * @return The lower time extent of the timeline.
     */
    public double getLowerTimeExtent() {
        return lowerTimeExtent;
    }

    /**
     * Set the lower time extent that the timeline window is currently showing.
     *
     * @param lowerTimeExtent The lower time extent of the timeline window.
     */
    public void setLowerTimeExtent(final double lowerTimeExtent) {
        this.lowerTimeExtent = lowerTimeExtent /*+ (timeZone == null ? 0 : TimeZone.getTimeZone(timeZone).getRawOffset())*/;
    }

    /**
     * Returns the upper time extent that the timeline window is currently
     * showing.
     *
     * @return the upper time extent of the timeline.
     */
    public double getUpperTimeExtent() {
        return upperTimeExtent;
    }

    /**
     * Sets the upper time extent that the timeline window is currently showing.
     *
     * @param upperTimeExtent The upper time extent of the timeline window.
     */
    public void setUpperTimeExtent(final double upperTimeExtent) {
        this.upperTimeExtent = upperTimeExtent /*+ (timeZone == null ? 0 : TimeZone.getTimeZone(timeZone).getRawOffset())*/;
    }

    /**
     * Returns the datetime attribute that is used for retrieving temporal
     * values from the graph.
     *
     * @return The datetime attribute to be used for temporal data.
     */
    public String getDateTimeAttr() {
        return dateTimeAttr;
    }

    /**
     * Sets the datetime attribute that is used for retrieving temporal values
     * from the graph.
     *
     * @param dateTimeAttr The datetime attribute to be used for temporal data.
     */
    public void setDateTimeAttr(final String dateTimeAttr) {
        this.dateTimeAttr = dateTimeAttr;
    }

    /**
     * Returns the attribute label used for displaying labels on the nodes on
     * the timeline window.
     *
     * @return The attribute to be used for displaying on nodes for the
     * timeline.
     */
    public String getNodeLabelsAttr() {
        return nodeLabelsAttr;
    }

    /**
     * Sets the attribute label used for displaying labels on the nodes on the
     * timeline window.
     *
     * @param nodeLabelsAttr The attribute to be used for displaying on nodes
     * for the timeline.
     */
    public void setNodeLabelsAttr(final String nodeLabelsAttr) {
        this.nodeLabelsAttr = nodeLabelsAttr;
    }

    /**
     * Returns the time-zone in which the timeline is currently displaying
     * times.
     *
     * @return The timeline's current time-zone.
     */
    public ZoneId getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time-zone in which the timeline should display times.
     *
     * @param timeZone The time-zone which the timeline should use.
     */
    public void setTimeZone(final ZoneId timeZone) {
        this.timeZone = timeZone;
    }
}
