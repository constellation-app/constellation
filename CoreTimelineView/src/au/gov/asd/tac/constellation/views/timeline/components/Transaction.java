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
package au.gov.asd.tac.constellation.views.timeline.components;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

/**
 * A JavaFX component that is used to represent either directed or undirected
 * transactions on the graph.
 *
 * Note: This component is only the line that represents transactions on the
 * <code>TimelineChart</code>.
 *
 * @author betelgeuse
 */
public final class Transaction extends Group {

    public static final int UNDIRECTED = 0;
    public static final int DIRECTED_DOWN = 1;
    public static final int DIRECTED_UP = 2;
    public static final int BIDIRECTIONAL = 3;

    private final int transactionID;
    private final Color transactionColor;
    private final String transactionLabel;

    private boolean isSelected = false;

    private final Line transactionLine = new Line();
    private ArrowHead north = null;
    private ArrowHead south = null;

    /**
     * Creates a new 'directionless' transaction.
     *
     * @param transactionID The id of the transaction from the graph.
     * @param transactionColor The color of the transaction from the graph.
     * @param transactionLabel The label of the transaction from the graph.
     * @param isSelected Whether the transaction is currently selected on the
     * graph.
     */
    public Transaction(final int transactionID, final Color transactionColor,
            final String transactionLabel, final boolean isSelected) {
        this.setAutoSizeChildren(false);

        this.transactionID = transactionID;
        this.transactionColor = transactionColor;
        this.transactionLabel = transactionLabel;

        transactionLine.setStroke(transactionColor);

        setCursor(Cursor.DEFAULT);

        if (transactionLabel != null) {
            Tooltip.install(this, new Tooltip(transactionLabel));
        }

        setSelected(isSelected);
        getChildren().add(transactionLine);
    }

    /**
     * Creates a new 'directed' transaction.
     *
     * @param transactionID The id of the transaction from the graph.
     * @param transactionColor The color of the transaction from the graph.
     * @param transactionLabel The label of the transaction from the graph.
     * @param directionality The direction of the transaction (ie NORTH or
     * SOUTH)
     * @param isSelected Whether the transaction is currently selected on the
     * graph.
     */
    public Transaction(final int transactionID, final Color transactionColor,
            final String transactionLabel, final int directionality, final boolean isSelected) {
        this.setAutoSizeChildren(false);

        this.transactionID = transactionID;
        this.transactionColor = transactionColor;
        this.transactionLabel = transactionLabel;

        transactionLine.setStroke(transactionColor);
        transactionLine.setStrokeWidth(2.0);

        setCursor(Cursor.DEFAULT);

        Tooltip.install(this, new Tooltip(transactionLabel));

        if (directionality == DIRECTED_UP || directionality == BIDIRECTIONAL) {
            north = new ArrowHead(transactionColor, ArrowHead.NORTH);
        }
        if (directionality == DIRECTED_DOWN || directionality == BIDIRECTIONAL) {
            south = new ArrowHead(transactionColor, ArrowHead.SOUTH);
        }

        getChildren().add(transactionLine);

        if (north != null) {
            getChildren().add(north);
        }
        if (south != null) {
            getChildren().add(south);
        }

        setSelected(isSelected);
    }

    /**
     * The pixel offsets of the beginning and end of the transaction line.
     * <p>
     * This is based on the display position of the associated vertices for this
     * transaction.
     *
     * @param topOffset The upper location of the transaction. Represents the
     * topmost point of the line.
     * @param bottomOffset The bottom location of the transaction. Represents
     * the bottommost point of the line.
     */
    public void setBeginningAndEnd(final double topOffset, final double bottomOffset) {
        transactionLine.setStartY(topOffset - 2);
        transactionLine.setEndY(bottomOffset + 5);

        // If a northern arrowhead exists:
        if (north != null) {
            // Position the north arrowhead against the northern vertex:
            north.setLayoutY(bottomOffset + 10);
        }
        // If a southern arrowhead exists:
        if (south != null) {
            // Position the south arrowhead against the southern vertex:
            south.setLayoutY(topOffset - 16);
        }
    }

    /**
     * Sets the color of the transaction.
     *
     * @param transactionColor The color of the transaction from the graph.
     */
    public void setTransactionColor(final Color transactionColor) {
        transactionLine.setStroke(transactionColor);
    }

    /**
     * Gets the current color of the transaction.
     *
     * @return The current color of the transaction.
     */
    public Color getTransactionColor() {
        return (Color) transactionLine.getStroke();
    }

    /**
     * Gets the selected state of the timeline.
     *
     * @return The selected state.
     */
    public boolean isSelected() {
        return isSelected;
    }

    public int getTransactionID() {
        return transactionID;
    }

    /**
     * Updates the selected state of this timeline, and if <code>true</code>,
     * visually updates the transaction with a 'glow'.
     *
     * @param selected Whether the transaction is currently selected.
     */
    public void setSelected(final boolean selected) {
        isSelected = selected;

        if (isSelected) {
            this.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.RED, 15.0, 0.45, 0.0, 0.0));
        } else {
            // Remove the selection effect:
            this.setEffect(null);
        }
    }

    /**
     * Helper class that creates a JFX polygon used to represent directionality
     * of transactions.
     *
     * @see Polygon
     */
    private class ArrowHead extends Polygon {

        protected static final int NORTH = 1;
        protected static final int SOUTH = 2;

        public ArrowHead(final Color color, final int direction) {
            super(new double[]{
                -4, 0,
                0, 5,
                4, 0
            });

            setFill(color);
            setRotate(direction == NORTH ? 180.0 : 0.0);
        }
    }
}
