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

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author betelgeuse
 */
@Messages({
    "Arrow=←"
})
public final class Vertex extends Group {

    private final int vertexID;
    private int displayPos;
    private boolean isSelected = false;
    private boolean selectedWithTransaction = false;
    private boolean isNorthernVertex = true;
    private final String label;

    private final Triangle triangle;
    private Label lblVertexLabel;

    public Vertex(final int vertexID, final int displayPos, final String label,
            final Color vertexColor, final boolean isSelected, final boolean selectedWithTransaction, final boolean isShowingLabel) {
        triangle = new Triangle(vertexColor, isSelected);

        this.label = label;
        if (isShowingLabel && label != null) {
            lblVertexLabel = new Label(Bundle.Arrow() + this.label);
        }

        this.vertexID = vertexID;
        this.displayPos = displayPos;

        if (label != null) {
            Tooltip.install(this, new Tooltip(this.label));
        }

        setCursor(Cursor.DEFAULT);

        setNorthernVertex(isNorthernVertex, isShowingLabel);

        this.isSelected = isSelected;
        this.selectedWithTransaction = selectedWithTransaction;
        updateSelectedVisualIndication();

        if (isShowingLabel && label != null) {
            getChildren().addAll(triangle, lblVertexLabel);
        } else {
            getChildren().add(triangle);
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    private void updateSelectedVisualIndication() {
        if (isSelected) {
            final Color highlightColor = selectedWithTransaction ? Color.RED : Color.YELLOW;
            triangle.setEffect(new DropShadow(BlurType.GAUSSIAN, highlightColor, 15.0, 0.45, 0.0, 0.0));
        } else {
            // Remove the selection effect:
            triangle.setEffect(null);
        }
    }

    public boolean isNorthernVertex() {
        return isNorthernVertex;
    }

    public void setNorthernVertex(final boolean isNorthernVertex, final boolean isShowingLabel) {
        this.isNorthernVertex = isNorthernVertex;

        triangle.setRotate(this.isNorthernVertex ? 180.0 : 0.0);
        triangle.setTranslateY(this.isNorthernVertex ? -11.0 : 0.0);

        if (isShowingLabel && label != null) {
            lblVertexLabel.setLayoutX(8.0);
            lblVertexLabel.setLayoutY(this.isNorthernVertex ? -15.0 : -2.0);
        }
    }

    public int getDisplayPos() {
        return displayPos;
    }

    public int getVertexID() {
        return vertexID;
    }

    public void setDisplayPos(final int displayPos) {
        this.displayPos = displayPos;
    }

    private class Triangle extends Polygon {

        public Triangle(final Color vertexColor, final boolean isSelected) {
            super(new double[]{
                -8, 0,
                0, 10,
                8, 0
            }); // Create a triangular polygon shape.

            setFill(vertexColor);
        }

        public void setVertexColor(final Color vertexColor) {
            setFill(vertexColor);
        }

        public void getVertexColor(final Color vertexColor) {
            this.setFill(vertexColor);
        }
    }
}
