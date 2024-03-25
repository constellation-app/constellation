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

import au.gov.asd.tac.constellation.views.timeline.GraphManager;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import org.openide.util.NbBundle.Messages;

/**
 * A
 * <code>Cluster</code> object is a JFX object that represents a temporal
 * cluster of events that is graphically displayed on a
 * <code>TimelineChart</code>. the blue bars... you see on timeline.
 *
 * @see TimelineChart
 *
 * @author betelgeuse
 */
@Messages({
    "EventLabel=events"
})
public class Cluster extends Group {

    private static final double MIN_WIDTH = 14.0; // 14px min width of clusters.
    private static final double MIN_HEIGHT = 40.0; // 40px min height of clusters.
    private final ClusterRectangle rect;
    private final Label lblCount;
    private int count;
    private int selectedCount;
    private final boolean anyNodesSelected;
    private long lowerBound;
    private long upperBound;
    private long lowerDisplayPos;
    private long upperDisplayPos;
    private boolean isDragging = false;

    // <editor-fold defaultstate="collapsed" desc="Mouse Handler">
    /**
     * The mouse event handler for <code>Cluster</code> objects.
     * <p>
     * Is responsible for events such as mouse entering, exiting, or clicking on
     * a <code>Cluster</code> object.
     *
     * @see Cluster
     */
    private final EventHandler<Event> clusterMouseHandler = new EventHandler<Event>() {
        @Override
        public void handle(final Event t) {
            if (t instanceof MouseEvent me) {
                if (me.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    final Cluster current = (Cluster) rect.getParent();
                    current.toFront();
                    current.setEffect(new DropShadow());
                } else if (me.getEventType() == MouseEvent.MOUSE_EXITED) {
                    final Cluster current = (Cluster) rect.getParent();
                    current.setEffect(null);
                } else if (me.getEventType() == MouseEvent.MOUSE_CLICKED && !isDragging) {
                    if (me.getButton() == MouseButton.PRIMARY) {
                        GraphManager.getDefault().selectAllInRange(lowerBound,
                                upperBound, me.isControlDown(), false, false);
                        t.consume();
                    }
                } else if (me.getEventType() == MouseEvent.MOUSE_CLICKED) {
                    isDragging = false;
                } else if (me.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    isDragging = true;
                }
            }
        }
    };
    // </editor-fold>

    /**
     * Creates a new <code>Cluster</code> instance.
     * <p>
     * This involves the creation of both a label and rectangle which are
     * grouped to show the extents of and number of events represented by a
     * given <code>Cluster</code> object.
     *
     * @param lowerBound The lower bound of time. (The first x-coordinate).
     * @param upperBound The upper bound of time. (The last x-coordinate).
     * @param lowerDisplayPos The bottom display position. (The bottom
     * y-coordinate).
     * @param upperDisplayPos The top display position. (The top y-coordinate).
     * @param count The number of events the cluster represents.
     * @param selectedCount The number of events represented by this cluster,
     * which are also 'selected' on the graph.
     * @param anyNodesSelected ???
     */
    public Cluster(final long lowerBound, final long upperBound, final long lowerDisplayPos, final long upperDisplayPos, 
            final int count, final int selectedCount, final boolean anyNodesSelected) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowerDisplayPos = lowerDisplayPos;
        this.upperDisplayPos = upperDisplayPos;
        this.count = count;
        this.selectedCount = selectedCount;
        this.anyNodesSelected = anyNodesSelected;

        // Create the components:
        rect = new ClusterRectangle();
        getChildren().add(rect);

        setCursor(Cursor.DEFAULT);

        if (selectedCount > 0) {
            lblCount = new Label(String.valueOf(selectedCount) + " / " + count + " " + Bundle.EventLabel());
        } else {
            lblCount = new Label(String.valueOf(count) + " " + Bundle.EventLabel());
        }

        // Bind the count labels height to the rectangle's:
        lblCount.maxHeightProperty().bind(rect.heightProperty());
        lblCount.prefHeightProperty().bind(rect.heightProperty());
        lblCount.setRotate(270.0);
        // Format the count label:
        lblCount.setTextFill(Color.LIGHTGREY);
        lblCount.setTextAlignment(TextAlignment.CENTER);
        lblCount.setAlignment(Pos.CENTER);
        lblCount.setEllipsisString("...");

        lblCount.setMouseTransparent(true);

        getChildren().add(lblCount);

        // Layer the components:
        rect.toBack();

        // Set handlers:
        rect.setOnMouseEntered(clusterMouseHandler);
        rect.setOnMouseExited(clusterMouseHandler);
        rect.setOnMouseClicked(clusterMouseHandler);
        rect.setOnMouseDragged(clusterMouseHandler);
        rect.setOnMouseReleased(clusterMouseHandler);
    }

    /**
     * Returns the lower time extent.
     *
     * @return The lower time extent.
     */
    public long getLowerBound() {
        return lowerBound;
    }

    /**
     * Sets the lower time extent.
     *
     * @param lowerBound The lower time extent to be set.
     */
    public void setLowerBound(final long lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * Returns the upper time extent.
     *
     * @return The upper time extent.
     */
    public long getUpperBound() {
        return upperBound;
    }

    /**
     * Sets the upper time extent.
     *
     * @param upperBound The upper time extent to be set.
     */
    public void setUpperBound(final long upperBound) {
        this.upperBound = upperBound;
    }

    /**
     * Gets the lower display (y) position.
     *
     * @return The lower display position.
     */
    public long getLowerDisplayPos() {
        return lowerDisplayPos;
    }

    /**
     * Sets the lower display (y) position.
     *
     * @param lowerDisplayPos The lower display position to be set.
     */
    public void setLowerDisplayPos(final long lowerDisplayPos) {
        this.lowerDisplayPos = lowerDisplayPos;
    }

    /**
     * Gets the upper display (y) position.
     *
     * @return The upper display position.
     */
    public long getUpperDisplayPos() {
        return upperDisplayPos;
    }

    /**
     * Sets the upper display (y) position.
     *
     * @param upperDisplayPos The upper display position to be set.
     */
    public void setUpperDisplayPos(final long upperDisplayPos) {
        this.upperDisplayPos = upperDisplayPos;
    }

    /**
     * Gets the count of events represented by this <code>Cluster</code>.
     *
     * @return The count of events covered.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count of events represented by this <code>Cluster</code>.
     *
     * @param count The count of events covered.
     */
    public void setCount(final int count) {
        this.count = count;
    }

    /**
     * Gets the count of events (transactions only) that are currently
     * 'selected' on the graph.
     *
     * @return The count of selected events.
     */
    public int getSelectedCount() {
        return selectedCount;
    }

    public boolean isSelected() {
        return selectedCount == count;
    }

    /**
     * Sets the count of events (transactions only) that are currently
     * 'selected' on the graph.
     *
     * @param selectedCount The count of selected events.
     */
    public void setSelectedCount(final int selectedCount) {
        this.selectedCount = selectedCount;
    }

    /**
     * Sets the bounds of the <code>Cluster</code> object.
     *
     * @param width The width of the <code>Cluster</code> in pixels.
     * @param height The height of the <code>Cluster</code> in pixels.
     */
    public void setBounds(double width, double height) {
        if (width <= MIN_WIDTH) {
            width = MIN_WIDTH; // Set a minimum width of 10px so that clusters are always 'seen'.
        }
        if (height <= MIN_HEIGHT) {
            height = MIN_HEIGHT;
        }

        rect.setHeight(height);
        rect.setWidth(width);

        // Layout the count label:
        double rectMidpoint = rect.getLayoutX() + (width / 2);
        lblCount.setPrefWidth(height);
        lblCount.setLayoutX(rectMidpoint - (height / 2));
    }

    /**
     * The actual rectangle that represents the extent and view of events.
     */
    private class ClusterRectangle extends Rectangle {

        /**
         * Constructs a new <code>ClusterRectangle</code> instance.
         */
        public ClusterRectangle() {
            super(0, 0, 0, 40);

            // Round the edges of the rectangle:
            setArcWidth(5.0);
            setArcHeight(5.0);

            // Make slightly transparent:
            setOpacity(0.65);

            // Set a gradient for the percentage selected:
            final float percentage = 1 - (float) selectedCount / (float) count;

            if (anyNodesSelected && percentage == 1) {
                setStrokeWidth(3);
                setStroke(Color.YELLOW.deriveColor(0, 1, 1, 0.5));
            } else {
                setStrokeWidth(0);
            }

            // 'Blue' or unselected items represent 0% of the total:
            if (percentage == 0) {
                setFill(Color.RED.darker());
            } else {
                final LinearGradient gradient = new LinearGradient(0.0, 0.0, 0.0, percentage, true, CycleMethod.NO_CYCLE, new Stop[]{
                    new Stop(percentage, Color.DODGERBLUE),
                    new Stop(1, Color.RED.darker())
                });

                setFill(gradient);
            }
        }
    }
}
