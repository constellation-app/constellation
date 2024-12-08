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
package au.gov.asd.tac.constellation.views.timeline.components;

import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import au.gov.asd.tac.constellation.views.timeline.GraphManager;
import au.gov.asd.tac.constellation.views.timeline.TimelinePanel;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.openide.util.NbBundle.Messages;

/**
 * The <code>TimelineChart</code> is a JavaFX based component that can be used for mapping temporal data.
 * <p>
 * The x-axis represents time, and the y-axis represents a field of interest that temporal data is classified against
 * such as names, IDs etc.
 * <p>
 * The underlying chart is a JavaFX <code>XYChart</code> that has been extended to incorporate temporal aspects and
 * mouse handling events. The mouse handling events are responsible for tasks such as zooming the timeline, shifting the
 * timeline and selecting a datetime range.
 *
 * @see XYChart
 *
 * @author betelgeuse
 */
@Messages({
    "DateFormat=d MMM yyyy  HH:mm"
})
public class TimelineChart extends XYChart<Number, Number> {
    // Time durations based on millis:

    private static final long MILLI = 1;
    private static final long SECOND = 1000 * MILLI;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long WEEK = 7 * DAY;
    private static final long MONTH = 30 * DAY;
    private static final double YEAR = 365.25 * DAY;
    private static final double DECADE = 10.0 * YEAR;
    private static final double CENTURY = 10.0 * DECADE;
    // JavaFX Components:
    private final TimelinePanel parent;
    private final TimelineChart timeline;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final Tooltip tooltip;
    private final Rectangle selection;
    // Attributes and Instance Variables:
    private long lowestObservedDisplayPos = Long.MAX_VALUE;
    private long highestObservedDisplayPos = Long.MIN_VALUE;
    private double mouseOrigin = 0.0;
    private double mouseDistanceFromOrigin = 0.0;
    private double tickUnit = 1.0;
    private int minorTicks = 1;
    private SimpleDateFormat tickDate;
    private TimeZone currentTimezone = new SimpleTimeZone(0, "GMT");
    private boolean selectedOnly = false;
    private double lowerTimeExtent = 0;
    private double upperTimeExtent = 0;
    private String currentTime = null;
    private final StringProperty lowerTimeExtentProperty = new SimpleStringProperty();
    private final StringProperty upperTimeExtentProperty = new SimpleStringProperty();
    private boolean isSelecting = false;
    private boolean firstaxisUpdate = false;

    // <editor-fold defaultstate="collapsed" desc="Timeline Event Handlers">
    private final EventHandler<Event> timelineMouseHandler = new EventHandler<Event>() {
        @Override
        public void handle(final Event t) {
            final double range = upperTimeExtent - lowerTimeExtent;
            final double width = parent.getWidth();

            if (t instanceof MouseEvent me) {
                final double mouseX = me.getX();

                // Check for double click to remove selection
                if (me.getEventType() == MouseEvent.MOUSE_CLICKED && me.getClickCount() == 2) {
                    GraphManager.getDefault().selectAllInRange(Long.MAX_VALUE, Long.MAX_VALUE, false, true, false);
                }
                // Reserve primary button for selection events:
                if (!me.isPrimaryButtonDown()) {
                    // Change the mouse cursor for the timeline:
                    if (me.getEventType() == MouseEvent.MOUSE_ENTERED) {
                        parent.setCursor(Cursor.CROSSHAIR);

                        // Recognise mouse clicks and register the origin for drag operations:    
                    } else if (me.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        mouseOrigin = me.getX();

                        // Handle scrolling back and forth:
                    } else if (me.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                        final double amount = range / width;
                        final double delta = mouseOrigin - mouseX;

                        lowerTimeExtent += (delta * amount);
                        upperTimeExtent += (delta * amount);

                        parent.coordinator.setExtents(lowerTimeExtent, upperTimeExtent);

                        // Update variables based on current mouse pointer position:
                        mouseOrigin = mouseX;

                        // Get the time under the cursor and place it in a tooltip:
                    } else if (me.getEventType() == MouseEvent.MOUSE_MOVED) {
                        final double pos = lowerTimeExtent + ((mouseX * (upperTimeExtent - lowerTimeExtent)) / width);

                        final SimpleDateFormat sdf = new SimpleDateFormat(Bundle.DateFormat() + "  ");
                        sdf.setTimeZone(currentTimezone);
                        currentTime = sdf.format(new Date((long) pos));

                        tooltip.setText(currentTime);
                        Tooltip.install(timeline, tooltip);
                    } else if (isSelecting) {
                        // select everything under the selection box:
                        final double amount = range / width;
                        final double lte = lowerTimeExtent + (long) (amount * selection.getLayoutX());
                        final double ute = lowerTimeExtent + (long) (amount * (selection.getLayoutX() + selection.getWidth()));
                        if (selection.getWidth() >= 1) {
                            GraphManager.getDefault().selectAllInRange((long) lte, (long) ute,
                                    me.isControlDown(), true, selectedOnly);
                        }

                        // Hide the selection tool:
                        selection.toBack();
                        selection.setWidth(0);
                        selection.setLayoutX(0);
                        isSelecting = false;
                        mouseDistanceFromOrigin = 0;
                    }
                } else {
                    // We are starting a drag based temporal selection:
                    if (me.getEventType() == MouseEvent.MOUSE_PRESSED) {
                        mouseOrigin = mouseX;
                        isSelecting = true;

                        selection.setLayoutX(mouseOrigin);
                        selection.setWidth(0);

                        // Determine if we are doing a drag based selection:
                    } else if (me.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                        final double delta = mouseOrigin - mouseX;
                        mouseDistanceFromOrigin += delta;

                        selection.toFront();

                        if (mouseDistanceFromOrigin < 0) {
                            selection.setLayoutX(mouseX + mouseDistanceFromOrigin);
                            selection.setWidth(-mouseDistanceFromOrigin);
                        } else {
                            selection.setLayoutX(mouseX);
                            selection.setWidth(mouseDistanceFromOrigin);
                        }

                        // Update variables based on current mouse pointer position:
                        mouseOrigin = mouseX;
                    }
                }

                // Consume the event so that no other listeners are interrupted:
                t.consume();
            }
        }
    };
    // </editor-fold>

    /**
     * Constructs a new <code>TimelineChart</code> component given a parent panel and axes.
     *
     * @param parent the panel containing this chart.
     * @param xAxis the x axis.
     * @param yAxis the y axis.
     *
     * @see TimelinePanel
     * @see NumberAxis
     */
    public TimelineChart(final TimelinePanel parent, final Axis<Number> xAxis, final NumberAxis yAxis) {
        super(xAxis, yAxis);

        this.parent = parent;
        timeline = this;

        this.xAxis = (NumberAxis) xAxis;
        this.yAxis = yAxis;
        tooltip = createTooltip();

        formatAxes();
        // Create the selection box:
        selection = createSelectionRectange();
        selection.setStroke(Color.SILVER);
        selection.setStrokeWidth(2D);
        final LinearGradient gradient
                = new LinearGradient(0.0, 0.0, 0.0, 0.75, true, CycleMethod.NO_CYCLE, new Stop[]{
            new Stop(0, Color.LIGHTGREY),
            new Stop(1, Color.GREY.darker())
        });
        selection.setFill(gradient);
        selection.setSmooth(true);

        // Round the edges of the rectangle:
        selection.setArcWidth(5.0);
        selection.setArcHeight(5.0);
        getChildren().add(selection);

        // Install event handlers:
        //  Handles zooming for the timeline:
        setOnScroll((final ScrollEvent t) -> {
            final double mouseX = t.getX();
            performZoom(t, mouseX);
            t.consume();
        });
        //  Recognise mouse presses:
        setOnMousePressed(timelineMouseHandler);
        //  Handles scrolling back and forth:

        setOnMouseDragged(timelineMouseHandler);
        //  Handles determination of time under the mouse cursor, and sets tooltips accordingly:

        setOnMouseMoved(timelineMouseHandler);
        //  Handles the change of the mouse cursor for the timeline:

        setOnMouseEntered(timelineMouseHandler);
        // Handles the release of selection events:

        setOnMouseReleased(timelineMouseHandler);
        setOnMouseClicked(timelineMouseHandler);

        // Style the timeline:
        setAnimated(true);
        setLegendVisible(false);
        setVerticalZeroLineVisible(false);
    }

    public void performZoom(final ScrollEvent se, final double mouseX) {
        // Zoom on scrolling events:
        if (se.getEventType() != ScrollEvent.SCROLL) {
            return;
        }

        final double range = upperTimeExtent - lowerTimeExtent;
        final double width = parent.getWidth();

        // Register the event as a ScrollEvent:
        // Determine the scale here:
        final double quantum = range / 10.0;
        final double mousePosInRange = lowerTimeExtent + range * (mouseX / width); // Midpoint of the lower and upper, adjusted for position of mouse

        // We are zooming in:
        if (se.getDeltaY() > 0D) {
            // Only zoom in if we haven't reached the minimum size:
            if (quantum >= 0.5) {
                lowerTimeExtent = (mousePosInRange - ((mousePosInRange - lowerTimeExtent) * 0.9)); // Zoom in by 10%
                upperTimeExtent = (mousePosInRange + ((upperTimeExtent - mousePosInRange) * 0.9));

                // update the scope window:
                parent.coordinator.setExtents(lowerTimeExtent, upperTimeExtent);
            }
        } else if (quantum <= YEAR * 10D) { // We are zooming out:
            lowerTimeExtent = (mousePosInRange - ((mousePosInRange - lowerTimeExtent) * 1.1)); // Zoom out by 10%
            upperTimeExtent = (mousePosInRange + ((upperTimeExtent - mousePosInRange) * 1.1));

            // update the scope window:
            parent.coordinator.setExtents(lowerTimeExtent, upperTimeExtent);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Axes Look and Feel">
    /**
     * Formats the axes to set the required look and feel of a timeline rather than a generic chart which axes are
     * originally suited for.
     */
    private void formatAxes() {
        // Format the yAxis:
        yAxis.setSide(Side.LEFT);
        yAxis.setAnimated(true);
        yAxis.setAutoRanging(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setForceZeroInRange(false);

        // Format the xAxis and provide seed values:
        xAxis.setSide(Side.TOP);
        xAxis.setAnimated(true);
        xAxis.setAutoRanging(false);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(true);
        xAxis.setLowerBound(0D);
        xAxis.setUpperBound(1D);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(final Number object) {
                final Date date = new Date(object.longValue());

                // set the tick intervals:
                xAxis.setTickUnit(tickUnit);

                xAxis.setMinorTickCount(minorTicks);

                final SimpleDateFormat extentFormatter = new SimpleDateFormat(Bundle.DateFormat());
                extentFormatter.setTimeZone(currentTimezone);

                lowerTimeExtentProperty.setValue(extentFormatter.format(new Date((long) lowerTimeExtent)));
                upperTimeExtentProperty.setValue(extentFormatter.format(new Date((long) upperTimeExtent)));

                if (tickDate != null) {
                    tickDate.setTimeZone(currentTimezone);
                    return tickDate.format(date);
                }

                // We don't have a valid timeline
                return "";
            }

            @Override
            public Number fromString(final String string) {
                // Ignore as we won't be going back from a string.
                throw new UnsupportedOperationException();
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Time Properties">
    /**
     * Provides access to the lower time extent property.
     *
     * @return lower extent of time property.
     */
    public StringProperty lowerTimeExtentProperty() {
        return lowerTimeExtentProperty;
    }

    /**
     * Provides access to the upper time extent property.
     *
     * @return upper extent of time property.
     */
    public StringProperty upperTimeExtentProperty() {
        return upperTimeExtentProperty;
    }
    // </editor-fold>

    /**
     * Publishes data to the <code>TimelineChart</code> instance.
     *
     * @param series The data to be published. (Temporal data containing interactions).
     * @param lowestObservedDisplayPos Sets the lowest yAxis value.
     * @param highestObservedDisplayPos Sets the highest yAxis value.
     */
    public void populate(final XYChart.Series<Number, Number> series, final long lowestObservedDisplayPos, final long highestObservedDisplayPos,
            final boolean selectedOnly, final ZoneId zoneId) {
        this.selectedOnly = selectedOnly;
        this.currentTimezone = TimeZone.getTimeZone(zoneId);
        this.lowestObservedDisplayPos = lowestObservedDisplayPos;
        this.highestObservedDisplayPos = highestObservedDisplayPos;
        yAxis.setLowerBound(this.lowestObservedDisplayPos);
        yAxis.setUpperBound(this.highestObservedDisplayPos);
        // Populate the timeline with the data from the graph:
        final ObservableList<XYChart.Series<Number, Number>> data = FXCollections.observableArrayList(series);
        this.setData(data);
    }

    // <editor-fold defaultstate="collapsed" desc="Range Determination Functions">
    /**
     * Returns the current lower time extent displayed by the timeline.
     *
     * @return The lower time extent.
     */
    public double getLowerExtent() {
        return lowerTimeExtent;
    }

    /**
     * Returns the current upper time extent displayed by the timeline.
     *
     * @return The upper time extent.
     */
    public double getUpperExtent() {
        return upperTimeExtent;
    }

    /**
     * Given a lower and upper time extent, sets the timeline's view to the corresponding pov.
     * <p>
     * This method performs the conversion from time values to the actual pixel values, and also handles padding of the
     * timeline to create a sliding window for the POV.
     *
     * @param lowerTimeExtent The lower time value to set the left of the timeline window to.
     * @param upperTimeExtent The upper time value to set the right of the timeline window to.
     */
    public void setExtents(final double lowerTimeExtent, final double upperTimeExtent) {
        determineRange((long) lowerTimeExtent, (long) upperTimeExtent, timeline.getWidth());

        this.lowerTimeExtent = lowerTimeExtent;
        this.upperTimeExtent = upperTimeExtent;

        final double px = this.getWidth() / (upperTimeExtent - lowerTimeExtent);

        final double floor = Math.floor(lowerTimeExtent / tickUnit);
        final double msPadding = (lowerTimeExtent - floor * tickUnit);

        xAxis.setLowerBound((lowerTimeExtent - msPadding));
        xAxis.setUpperBound((upperTimeExtent + msPadding));
        this.setPadding(new Insets(0, -msPadding * px, 0, -msPadding * px));
    }

    /**
     * Helper method that determines the labelling requirements of the time axis based on the magnitude of time
     * represented by the lower and upper bounds.
     *
     * @param lowerBound The lower time value of the timeline window.
     * @param upperBound The upper time value of the timeline window.
     * @param width The width of the timeline.
     */
    private void determineRange(final long lowerBound, final long upperBound, final double width) {
        // Determine the range for the given number of pixels:
        final double intervals = width / 250; // no more than 250px blocks

        // Determine the total amountY of millis that we are working with:
        final double range = (upperBound - lowerBound) / intervals;

        // Set the appropriate date format, tick units and minorTick amounts
        //  for any given range:
        if (range < MILLI * 5) { // 1 milli blocks
            tickDate = new SimpleDateFormat(TemporalConstants.MILLISEC_FORMAT);
            tickUnit = MILLI;
            minorTicks = 1;
        } else if (range < MILLI * 10) { // 5 milli blocks
            tickDate = new SimpleDateFormat(TemporalConstants.MILLISEC_FORMAT);
            tickUnit = MILLI * 5.0;
            minorTicks = 5;
        } else if (range < MILLI * 50) { // 10 milli blocks
            tickDate = new SimpleDateFormat(TemporalConstants.MILLISEC_FORMAT);
            tickUnit = MILLI * 10.0;
            minorTicks = 5;
        } else if (range < MILLI * 100) { // 50 milli blocks
            tickDate = new SimpleDateFormat(TemporalConstants.SEC_MILLISEC_FORMAT);
            tickUnit = MILLI * 50.0;
            minorTicks = 5;
        } else if (range < MILLI * 500) { // 100 milli blocks
            tickDate = new SimpleDateFormat(TemporalConstants.SEC_MILLISEC_FORMAT);
            tickUnit = MILLI * 100.0;
            minorTicks = 5;
        } else if (range < SECOND) { // 500 Milli blocks
            tickDate = new SimpleDateFormat(TemporalConstants.SEC_MILLISEC_FORMAT);
            tickUnit = SECOND / 2.0;
            minorTicks = 5;
        } else if (range < SECOND * 5) { // 1 Second blocks
            tickDate = new SimpleDateFormat(TemporalConstants.SEC_MILLISEC_FORMAT);
            tickUnit = SECOND;
            minorTicks = 5;
        } else if (range < SECOND * 30) { // 5 Second blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_SEC_FORMAT);
            tickUnit = SECOND * 5.0;
            minorTicks = 5;
        } else if (range < MINUTE) { // 30 second blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_SEC_FORMAT);
            tickUnit = SECOND * 30.0;
            minorTicks = 3;
        } else if (range < MINUTE * 5) { // 1 minute blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_SEC_FORMAT);
            tickUnit = MINUTE;
            minorTicks = 6;
        } else if (range < MINUTE * 10) { // 5 minute blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_SEC_FORMAT);
            tickUnit = MINUTE * 5.0;
            minorTicks = 5;
        } else if (range < MINUTE * 30) { // 10 minute blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_SEC_FORMAT);
            tickUnit = MINUTE * 10.0;
            minorTicks = 5;
        } else if (range < HOUR) { // 30 minute blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_FORMAT);
            tickUnit = MINUTE * 30.0;
            minorTicks = 4;
        } else if (range < HOUR * 2) { // 1 hour blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_FORMAT);
            tickUnit = HOUR;
            minorTicks = 6;
        } else if (range < HOUR * 12) { // 2 hour blocks
            tickDate = new SimpleDateFormat(TemporalConstants.HOUR_MIN_FORMAT);
            tickUnit = HOUR * 2.0;
            minorTicks = 2;
        } else if (range < DAY) { // half day blocks
            tickDate = new SimpleDateFormat(TemporalConstants.DAY_MONTH_HOUR_MIN_FORMAT);
            tickUnit = HOUR * 12.0;
            minorTicks = 6;
        } else if (range < WEEK) { // 1 day blocks
            tickDate = new SimpleDateFormat(TemporalConstants.DAY_MONTH_FORMAT);
            tickUnit = DAY;
            minorTicks = 4;
        } else if (range < MONTH) { // 1 week blocks
            tickDate = new SimpleDateFormat(TemporalConstants.DAY_MONTH_FORMAT);
            tickUnit = WEEK;
            minorTicks = 7;
        } else if (range < MONTH * 6) { // 1 month blocks
            tickDate = new SimpleDateFormat(TemporalConstants.DAY_MONTH_FORMAT);
            tickUnit = MONTH;
            minorTicks = 4;
        } else if (range < YEAR) { // 6 month blocks
            tickDate = new SimpleDateFormat(TemporalConstants.MONTH_YEAR_FORMAT);
            tickUnit = MONTH * 6.0;
            minorTicks = 6;
        } else if (range < YEAR * 2) { // 1 year blocks
            tickDate = new SimpleDateFormat(TemporalConstants.MONTH_YEAR_FORMAT);
            tickUnit = YEAR;
            minorTicks = 4;
        } else if (range < YEAR * 5) { // 2 year blocks
            tickDate = new SimpleDateFormat(TemporalConstants.MONTH_YEAR_FORMAT);
            tickUnit = YEAR * 2;
            minorTicks = 4;
        } else if (range < DECADE) { // 5 year blocks
            tickDate = new SimpleDateFormat(TemporalConstants.YEAR_FORMAT);
            tickUnit = YEAR * 5;
            minorTicks = 5;
        } else if (range < CENTURY) { // 10 year blocks
            tickDate = new SimpleDateFormat(TemporalConstants.YEAR_FORMAT);
            tickUnit = DECADE;
            minorTicks = 5;
        } else { // 100 year blocks
            tickDate = new SimpleDateFormat(TemporalConstants.YEAR_FORMAT);
            tickUnit = CENTURY;
            minorTicks = 5;
        }
        xAxis.setMinorTickCount(minorTicks);
        xAxis.setTickUnit(tickUnit);
    }
    // </editor-fold>

    /**
     * Helper method that creates a tooltip.
     *
     * @return A newly created tooltip.
     */
    private Tooltip createTooltip() {
        return new Tooltip();
    }

    /**
     * Helper method that creates a selection rectangle.
     *
     * @return A newly created rectangle to be used for selections.
     */
    private Rectangle createSelectionRectange() {
        final Rectangle newSelection = new Rectangle(0, 0, 0, 0);
        newSelection.heightProperty().bind(yAxis.heightProperty());
        newSelection.layoutYProperty().bind(yAxis.layoutYProperty());
        newSelection.setFill(Color.WHITE);
        newSelection.setStroke(Color.BLACK);
        newSelection.setOpacity(0.4);
        newSelection.setMouseTransparent(true);
        newSelection.toBack();

        return newSelection;
    }

    // <editor-fold defaultstate="collapsed" desc="Chart Methods">
    @Override
    protected void dataItemAdded(final Series<Number, Number> series, final int itemIndex, final Data<Number, Number> item) {
        final Node prospective;
        if (item.getExtraValue() instanceof Interaction interaction) {
            prospective = interaction;
        } else {
            prospective = (Cluster) item.getExtraValue();
        }
        item.setNode(prospective);

        if (shouldAnimate()) {
            prospective.setOpacity(0);
            getPlotChildren().add(prospective);

            // fade in new child
            final FadeTransition ft = new FadeTransition(Duration.millis(500), prospective);
            ft.setToValue(1);
            ft.play();
        } else {
            getPlotChildren().add(prospective);
        }
    }

    @Override
    protected void dataItemRemoved(final Data<Number, Number> item, final Series<Number, Number> series) {
        final Node child = item.getNode();

        if (shouldAnimate()) {
            // fade out old item:
            final FadeTransition ft = new FadeTransition(Duration.millis(500), child);
            ft.setToValue(0);
            ft.setOnFinished((final ActionEvent actionEvent) -> getPlotChildren().remove(child));
            ft.play();
        } else {
            getPlotChildren().remove(child);
        }
    }

    @Override
    protected void dataItemChanged(final Data<Number, Number> item) {
        // currently not needed.
    }

    @Override
    protected void seriesAdded(final Series<Number, Number> series, int seriesIndex) {
        // Handle any data already in series:
        for (int j = 0; j < series.getData().size(); j++) {
            final Data<Number, Number> item = series.getData().get(j);

            final Node prospective;
            if (item.getExtraValue() instanceof Interaction interaction) {
                prospective = interaction;
            } else {
                prospective = (Cluster) item.getExtraValue();
            }

            item.setNode(prospective);

            if (shouldAnimate()) {
                prospective.setOpacity(0);
                getPlotChildren().add(prospective);

                final FadeTransition ft = new FadeTransition(Duration.millis(500), prospective);
                ft.setToValue(1);
                ft.play();
            } else {
                getPlotChildren().add(prospective);
            }
        }
    }

    @Override
    protected void seriesRemoved(final Series<Number, Number> series) {
        final Node child = series.getNode();
        if (shouldAnimate()) {
            // fade out old item:
            final FadeTransition ft = new FadeTransition(Duration.millis(500), child);
            ft.setToValue(0);
            ft.setOnFinished((final ActionEvent actionEvent) -> getPlotChildren().clear());
            ft.play();
        } else {
            getPlotChildren().clear();
        }
    }

    /**
     * This method manually lays out all of the interactions and clusters based on properties such as when they occurred
     * (x axis), and their respective display positions (y axis).
     */
    @Override
    protected void layoutPlotChildren() {
        // We have nothing to layout if no data is present
        if (getData() == null) {
            return;
        }

        final double observedDisplayPosDifference = (double) highestObservedDisplayPos - lowestObservedDisplayPos;
        final double amountY = observedDisplayPosDifference == 0 ? yAxis.getHeight() : yAxis.getHeight() / (highestObservedDisplayPos - lowestObservedDisplayPos);

        final double timeExtentDifference = upperTimeExtent - lowerTimeExtent;
        final double amountX = timeExtentDifference == 0 ? this.getWidth() : this.getWidth() / (upperTimeExtent - lowerTimeExtent);

        // Update all node positions:
        for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {
            final Series<Number, Number> series = getData().get(seriesIndex);
            final Iterator<Data<Number, Number>> iter = getDisplayedDataIterator(series);

            while (iter.hasNext()) {
                final Data<Number, Number> item = iter.next();
                double x = getXAxis().getDisplayPosition(getCurrentDisplayedXValue(item));
                final Object itemNode = item.getExtraValue();

                if (itemNode instanceof Interaction interaction) {
                    final double topVertY = interaction.getTopVertex().getDisplayPos();
                    final double bottomVertY = interaction.getBottomVertex().getDisplayPos();

                    double topY = (amountY * (topVertY - lowestObservedDisplayPos));
                    double bottomY = (amountY * (bottomVertY - lowestObservedDisplayPos));

                    // Correct sizing so that it is at least 20px between vertices:
                    if (topY - bottomY < 25.0) {
                        // try to position from the bottomY as a first preference, but if it is too close to the
                        // bottom of the yAxis, then use the top vertex:
                        if (bottomY - 25.0 > yAxis.getLayoutY()) {
                            bottomY -= 25.0;
                        } else {
                            topY += 25.0;
                        }
                    }

                    // Set the new coordinates of the vertices:
                    interaction.update(topY, bottomY);
                    interaction.setLayoutX(x);
                    interaction.setLayoutY(0); // We are doing everything manually, so make sure we do all manual operations from the origin.
                } else {
                    final Cluster cluster = (Cluster) itemNode;
                    double width = (amountX * cluster.getUpperBound()) - (amountX * cluster.getLowerBound());

                    final double height = (amountY * cluster.getUpperDisplayPos()
                            - (amountY * cluster.getLowerDisplayPos()));

                    // Adjust the size of clusters when they are going off the 'edge' of the timeline extent
                    // in order to keep labels centered.
                    if (x < getLayoutX() - getPadding().getLeft() - 5) {
                        width -= -getPadding().getLeft() - x;
                        x = -getPadding().getLeft() - 5;
                    }
                    if (x + width > (parent.getWidth() - getPadding().getLeft())) {
                        width = parent.getWidth() - x - getPadding().getLeft();
                    }
                    cluster.setLayoutX(x);
                    cluster.setLayoutY(0);
                    cluster.setBounds(width, height);
                }
            }
        }
    }

    /**
     * This is called when the range has been invalidated and we need to update it.
     *
     * If the axis are auto-ranging then we compile a list of all data that the given axis has to plot and call
     * invalidateRange() on the axis passing it that data.
     */
    @Override
    protected void updateAxisRange() {
        //THIS IS ONE EPIC DIRTY FIX FOR A WEIRD BUG IN JAVAFX!!
        // turning axis.autoranging to false causes immense lag. but this method
        // needs it to be false... so we set it to false
        // then run this method once and set it to true then never run this method again. It seems to work.
        if (!firstaxisUpdate) {
            firstaxisUpdate = true;
            final Axis<Number> xa = getXAxis();
            final Axis<Number> ya = getYAxis();
            final List<Number> xData = xa.isAutoRanging() ? new ArrayList<>() : null;
            final List<Number> yData = ya.isAutoRanging() ? new ArrayList<>() : null;

            if (xData != null || yData != null) {
                final ObservableList<XYChart.Series<Number, Number>> list = getData();
                if (list != null) {
                    for (final Series<Number, Number> series : list) {
                        for (final Data<Number, Number> data : series.getData()) {
                            if (xData != null) {
                                xData.add(data.getXValue());
                            }
                            if (yData != null && data.getExtraValue() instanceof Interaction) {
                                final Interaction intr = (Interaction) data.getExtraValue();
                                if (intr != null) {
                                    yData.add(intr.getTopVertex().getDisplayPos());
                                    yData.add(intr.getBottomVertex().getDisplayPos());
                                } else {
                                    yData.add(data.getYValue());
                                }
                            }
                        }
                    }
                }
                if (xData != null) {
                    xa.invalidateRange(xData);
                }
                if (yData != null) {
                    ya.invalidateRange(yData);
                }
            }
            this.xAxis.setAutoRanging(true);
            this.yAxis.setAutoRanging(true);
        }
    }
    // </editor-fold>
}
