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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * The OverviewPanel is a JavaFX component used to visualise the full extent of temporal data on the given graph.
 * <p>
 * The overview chart is composed of three major visual elements:
 * <ol>
 * <li>The overview histogram;</li>
 * <li>The pov (point of view); and,</li>
 * <li>Labels indicating the temporal extent of the data being visualised.</li>
 * </ol>
 *
 * @see Pane
 *
 * @author betelgeuse
 */
public class OverviewPanel extends Pane {
    // Private Globals:

    // OverviewPanel components:
    private final AreaChart<Number, Number> histogram;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private final Rectangle pov;
    // Panes:
    private final AnchorPane innerPane = new AnchorPane();
    private TimelineTopComponent coordinator;
    private double lowestTimeExtent;
    private double highestTimeExtent;
    private double range;

    /**
     * Constructs a new OverviewPanel instance.
     *
     * @param coordinator the component that will hold this panel.
     */
    public OverviewPanel(final TimelineTopComponent coordinator) {
        super();

        this.coordinator = coordinator;

        // Create JFX components:
        histogram = createHistogram();
        pov = createPOV();

        // Organise the inner pane:
        //  Histogram:
        AnchorPane.setTopAnchor(histogram, 0.0);
        AnchorPane.setBottomAnchor(histogram, 5.0);
        AnchorPane.setLeftAnchor(histogram, 0.0);
        AnchorPane.setRightAnchor(histogram, 0.0);
        //  POV:
        AnchorPane.setTopAnchor(pov, 0.0);

        // Add all components to the inner pane:
        innerPane.getChildren().addAll(histogram, pov);

        innerPane.prefWidthProperty().bind(this.widthProperty());
        innerPane.prefHeightProperty().bind(this.heightProperty());

        this.getChildren().add(innerPane);
        this.setOnScroll(coordinator::zoomFromOverview);

    }

    /**
     * Sets the coordinating <code>TimelineTopComponent</code> for this panel.
     *
     * @param parent The <code>TimelineTopComponent</code> that is to be used to coordinate the timeline and histogram.
     *
     * @see TimelineTopComponent
     */
    public void setParent(final TimelineTopComponent parent) {
        this.coordinator = parent;
    }

    // <editor-fold defaultstate="collapsed" desc="POV Extent">
    /**
     * Given a time extent, updates the POV dimensions to represent the extent of time observed on the histogram
     * component.
     *
     * @param lowerBound The lower time bound that needs to be represented on the POV component.
     * @param upperBound The upper time extent that needs to be represented on the POV component.
     */
    public void setExtentPOV(double lowerBound, double upperBound) {
        if (lowerBound < lowestTimeExtent) {
            lowerBound = lowestTimeExtent;

            final double currentUpperBound = ((pov.getX() + pov.getWidth()) / this.getWidth() * range) + lowestTimeExtent;
            if (upperBound < currentUpperBound) {
                upperBound = currentUpperBound;
            }
        }
        if (upperBound > highestTimeExtent) {
            upperBound = highestTimeExtent;

            final double currentLowerBound = (pov.getX() / this.getWidth() * range) + lowestTimeExtent;
            if (lowerBound > currentLowerBound) {
                lowerBound = currentLowerBound;
            }
        }

        final double normalLowerBound = lowerBound - lowestTimeExtent;
        double percentage = normalLowerBound / range;
        final double lowX = this.getWidth() * percentage;

        final double normalUpperBound = upperBound - lowestTimeExtent;
        percentage = normalUpperBound / range;
        final double highX = this.getWidth() * percentage;

        final double povWidth = highX - lowX;

        pov.setX(lowX);
        pov.setWidth(povWidth);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Populate Data">
    /**
     * Reads the temporal data from the given graph and creates a histogram view of the data.
     *
     * @param graph The graph to get the temporal data from.
     * @param datetimeAttribute The label of the attribute to retrieve temporal data from.
     * @param lowestTimeExtent The lowestTimeExtent observed temporal value.
     * @param highestTimeExtent The highestTimeExtent observed temporal value.
     * @param zoneId the time zone id.
     * @param isFullRefresh is a full refresh needed.
     * @param selectedOnly only show selected items.
     */
    public void populateHistogram(final ReadableGraph graph, final String datetimeAttribute,
            final double lowestTimeExtent, final double highestTimeExtent, final boolean isFullRefresh,
            final boolean selectedOnly) {
        final int transactionCount = graph.getTransactionCount();
        final int datetimeAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, datetimeAttribute);
        final int selectedTransAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        range = highestTimeExtent - lowestTimeExtent;
        this.lowestTimeExtent = lowestTimeExtent;
        this.highestTimeExtent = highestTimeExtent;

        final int intervals = 100; // Map the histogram to 100 unique points. This acts as a percentage of the
        // full time extent represented on the particular dateTimeAttr.
        final double intervalLength = range / intervals;

        xAxis.setTickUnit(intervals / 10);

        final int[] items = new int[intervals + 1];
        final int[] itemsSelected = new int[intervals + 1];

        // Check to see if we actually have a datetime attribute:
        if (datetimeAttributeId != Graph.NOT_FOUND) {
            final XYChart.Series<Number, Number> totalSeries = new XYChart.Series<>();
            final XYChart.Series<Number, Number> selectedSeries = new XYChart.Series<>();

            for (int i = 0; i < transactionCount; i++) {
                final int transactionID = graph.getTransaction(i);
                final String datetimeAttributeType = graph.getAttributeType(datetimeAttributeId);
                final Object datetimeAttributeDefault = graph.getAttributeDefaultValue(datetimeAttributeId);
                final Object datetimeAttributeValue = graph.getObjectValue(datetimeAttributeId, transactionID);

                if (TimelineTopComponent.SUPPORTED_DATETIME_ATTRIBUTE_TYPES.contains(datetimeAttributeType)
                        && datetimeAttributeValue != null && !datetimeAttributeValue.equals(datetimeAttributeDefault)
                        && ((selectedTransAttributeId != Graph.NOT_FOUND && graph.getBooleanValue(selectedTransAttributeId, transactionID)) || !selectedOnly)) {
                    long transactionValue = graph.getLongValue(datetimeAttributeId, transactionID);

                    // Dates are represented as days since epoch, whereas datetimes are represented as milliseconds since epoch
                    if (datetimeAttributeType.equals(DateAttributeDescription.ATTRIBUTE_NAME)) {
                        transactionValue = transactionValue * TemporalConstants.MILLISECONDS_IN_DAY;
                    }

                    int interval = 0;
                    if (intervalLength > 0) {
                        interval = ((int) Math.round((transactionValue - lowestTimeExtent) / intervalLength));
                    }

                    if (items[interval] > 0) {
                        items[interval]++;
                    } else {
                        items[interval] = 1;
                    }

                    // Work out if we have selected values for the current interval:
                    if (selectedTransAttributeId != Graph.NOT_FOUND && graph.getBooleanValue(selectedTransAttributeId, transactionID)) {
                        if (itemsSelected[interval] > 0) {
                            itemsSelected[interval]++;
                        } else {
                            itemsSelected[interval] = 1;
                        }
                    }
                }
            }
            // Create the data points that will represent the temporal data volumes.
            for (int i = 0; i <= intervals; i++) {
                if (isFullRefresh) {
                    totalSeries.getData().add(new XYChart.Data<>(i, items[i]));
                }
                selectedSeries.getData().add(new XYChart.Data<>(i, itemsSelected[i]));
            }

            ObservableList<XYChart.Series<Number, Number>> data = histogram.getData();
            if (data == null) {
                data = FXCollections.observableArrayList(totalSeries, selectedSeries);
                histogram.setData(data);
            } else {
                if (isFullRefresh) {
                    histogram.getData().clear();
                    histogram.getData().addAll(totalSeries, selectedSeries);
                } else {
                    //causes flickers on remove
                    if (selectedOnly) {
                        histogram.getData().clear();
                        histogram.getData().add(new XYChart.Series<>());
                    } else if (histogram.getData().size() > 1) {
                        histogram.getData().remove(1);
                    }
                    histogram.getData().add(selectedSeries);
                }
            }
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(intervals);

            setExtentPOV(coordinator.getTimelineLowerTimeExtent(), coordinator.getTimelineUpperTimeExtent());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Histogram Component">
    private AreaChart<Number, Number> createHistogram() {
        // Create the axes:
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);

        // Create the histogram:
        final AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setPadding(new Insets(0.0, 0.0, 20.0, 0.0));
        chart.setAnimated(true);

        // Hide non-relevant chart elements:
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        // Set the min height so that elements can be sized to whatever level necessary:
        chart.setMinHeight(0D);

        // Return the newly created histogram:
        return chart;
    }
    // </ editor-fold>

    // <editor-fold defaultstate="collapsed" desc="POV Component">
    /**
     * Helper method that creates and styles a POV object.
     *
     * The POV object is a styled rectangle that is used to indicate the currently observed time range (aka time extent)
     * on the timeline. It can also be used to quickly interact with the time extent.
     *
     * @return A formatted POV object.
     */
    private Rectangle createPOV() {
        final Rectangle rect = new Rectangle(135, 25, 60, 1);

        // Bind the height of the POV to the Height of the histogram:
        rect.yProperty().bind(histogram.heightProperty());
        rect.heightProperty().bind(innerPane.prefHeightProperty());
        rect.setManaged(true);

        // Style the rectangle:
        rect.setStroke(Color.DODGERBLUE);
        rect.setStrokeWidth(2D);
        final LinearGradient gradient
                = new LinearGradient(0.0, 0.0, 0.0, 0.5, true, CycleMethod.NO_CYCLE, new Stop[]{
            new Stop(0, Color.LIGHTBLUE.darker()),
            new Stop(1, Color.TRANSPARENT)
        });
        rect.setFill(gradient);
        rect.setSmooth(true);

        // Round the edges of the rectangle:
        rect.setArcWidth(5.0);
        rect.setArcHeight(5.0);

        // Set the POV mouse event handlers:
        final POVMouseEventHandler handler = new POVMouseEventHandler(rect);
        rect.setOnMouseMoved(handler);
        rect.setOnMousePressed(handler);
        rect.setOnMouseDragged(handler);
        rect.setOnMouseReleased(handler);

        // Make the POV object the top-most object on this panel:
        rect.toFront();

        return rect;
    }

    /**
     * Helper method that clears all data off the histogram component.
     */
    public void clearHistogram() {
        if (histogram.getData() != null) {
            histogram.getData().clear();
        }
    }

    /**
     * Helper method that clears all data off the histogram component.
     *
     * @param isPartialClear only remove the selected dataset.
     */
    public void clearHistogram(final boolean isPartialClear) {
        if (histogram.getData() != null) {
            if (isPartialClear) {
                // Remove only the 'selected' dataset which is at the 1st index location:
                if (histogram.getData().size() > 1) {
                    for (int i = 1; i < histogram.getData().size(); i++) {
                        histogram.getData().remove(i);
                    }
                }
            } else {
                histogram.getData().clear();
                histogram.setData(null);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="POV Listeners">
    /**
     * Private class that implements mouse controls for the POV component in order to capture events like clicks and
     * drags and drops.
     */
    private class POVMouseEventHandler implements EventHandler<MouseEvent> {

        private static final double BUFFER = 2.0;
        // Local reference to the POV component:
        private final Rectangle rect;
        private double origin = 0.0;
        private boolean isResizingLeft = false;
        private boolean isResizingRight = false;

        /**
         * Constructs a new POVMouseEventHandler.
         *
         * @param rect The POV object that this handler will be used for.
         */
        public POVMouseEventHandler(final Rectangle rect) {
            this.rect = rect;
        }

        @Override
        public void handle(final MouseEvent t) {
            if (t.getEventType() == MouseEvent.MOUSE_MOVED) {
                handleResizing(t);
            } else if (t.getEventType() == MouseEvent.MOUSE_PRESSED) {
                origin = t.getX(); // Set the origin on all mouse presses.
                handleResizing(t);
            } else if (t.getEventType() == MouseEvent.MOUSE_RELEASED) {
                handleRelease();
            } else if (t.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                handleDragging(t);
            }
        }

        /**
         * Helper method that is used to determine if resizing operations need to be handled.
         *
         * This method changes the cursor to inform the user that resizing can occur.
         *
         * @param t The triggered mouse event.
         *
         * @see MouseEvent
         */
        private void handleResizing(final MouseEvent t) {
            // Determine if the cursor is currently hovering over a border
            if ((rect.getX() - BUFFER) <= t.getX() && t.getX() <= (rect.getX() + BUFFER)) { // left border
                rect.setCursor(Cursor.W_RESIZE);

                // Switch on resizing left flags if is a primary mouse:
                if (t.isPrimaryButtonDown()) {
                    isResizingLeft = true;
                    isResizingRight = false;
                }
            } else if ((rect.getX() + rect.getWidth() - BUFFER) <= t.getX()
                    && t.getX() <= (rect.getX() + rect.getWidth() + BUFFER)) { // right border
                rect.setCursor(Cursor.E_RESIZE);

                // Switch on resizing right flags if is a primary mouse click:
                if (t.isPrimaryButtonDown()) {
                    isResizingLeft = false;
                    isResizingRight = true;
                }
            } else { // Not hovering over a border
                rect.setCursor(Cursor.NONE);
            }
        }

        /**
         * Helper method called when releasing mouse press events occur.
         *
         * @see MouseEvent
         */
        private void handleRelease() {
            // Switch off resizing flags:
            isResizingLeft = isResizingRight = false;
        }

        /**
         * Helper method called when dragging the mouse events occur.
         *
         * @param t The triggered mouse event.
         *
         * @see MouseEvent
         */
        private void handleDragging(final MouseEvent t) {
            // Resize POV if necessary:
            if (isResizingLeft || isResizingRight) {
                resizePOV(t);
            } else { // If we aren't resizing we are dragging:
                // only drag on 'x' press:
                performDrag(t);
            }
        }

        /**
         * Method that performs calculations for resize operations on the POV object, and updates the coordinator with
         * new time extents.
         *
         * Note: These updates are what causes the timeline's time extent to be changed to match the POV's extent.
         *
         * @param t The triggered mouse event.
         *
         * @see MouseEvent
         */
        private void resizePOV(final MouseEvent t) {
            // Determine any change in the mouse position:
            final double delta = origin - t.getX();

            // Determine if the cursor is currently hovering over a border:
            if (isResizingLeft) { // left border
                final double width = rect.getWidth() + delta;
                final double x = rect.getX() - delta;

                // Only resize if the new width is not too small, and it won't go off the left hand boundary:
                if (width >= 2.0 * BUFFER && x > 0.0) {
                    rect.setWidth(width);
                    rect.setX(x);

                    // Update the timeline's extents:
                    final long newLowerTimeExtent = (long) (((x / histogram.getWidth()) * range) + lowestTimeExtent);
                    final long newUpperTimeExtent = (long) ((((x + width) / histogram.getWidth()) * range) + lowestTimeExtent);

                    // Update the timeline with the new extents:
                    coordinator.setExtents(newLowerTimeExtent, newUpperTimeExtent);

                    // Update the origin as we have had some movement:
                    origin = t.getX();
                }
            } else if (isResizingRight) { // right border
                final double width = rect.getWidth() - delta;

                // Only resize if the new width is not too small, and it won't go off the right hand boundary:
                if (width >= 2.0 * BUFFER && ((rect.getX() + width) < histogram.getWidth())) {
                    rect.setWidth(width);

                    // Update the timeline's extents:
                    final long newLowerTimeExtent = (long) (((rect.getX() / histogram.getWidth()) * range) + lowestTimeExtent);
                    final long newUpperTimeExtent = (long) ((((rect.getX() + width) / histogram.getWidth()) * range) + lowestTimeExtent);

                    // Update the timeline with the new time extents:
                    coordinator.setExtents(newLowerTimeExtent, newUpperTimeExtent);

                    // Update the origin as we have had some movement:
                    origin = t.getX();
                }
            }
        }

        /**
         * Method that performs calculations for drag operations on the POV object, and updates the coordinator with new
         * time extents.
         *
         * Note: These updates are what causes the timeline's time extent to be changed to match the POV's extent.
         *
         * @param t The triggered mouse event.
         *
         * @see MouseEvent
         */
        private void performDrag(final MouseEvent t) {
            // Determine any change in the mouse position:
            final double delta = origin - t.getX();
            final double newX = rect.getX() - delta;

            // Only perform drag if it is within the bounds of the histogram:
            if (newX >= 0.0 && (newX + rect.getWidth()) < histogram.getWidth()) {
                rect.setX(newX);

                // Update the timeline's extents:
                final long newLowerTimeExtent = (long) (((newX / histogram.getWidth()) * range) + lowestTimeExtent);
                final long newUpperTimeExtent = (long) ((((newX + rect.getWidth()) / histogram.getWidth()) * range) + lowestTimeExtent);
                coordinator.setExtents(newLowerTimeExtent, newUpperTimeExtent);

                // Update the origin as we have had some movement:
                origin = t.getX();
            }
        }
    }

    // </editor-fold>
    // </editor-fold>
}
