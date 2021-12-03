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

import au.gov.asd.tac.constellation.views.timeline.GraphManager;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author betelgeuse
 */
public class Interaction extends Group {

    private final Vertex vertexA;
    private final Vertex vertexB;
    private final Transaction transaction;

    public Interaction(final Vertex vertexA, final Vertex vertexB, final Transaction transaction,
            final boolean isShowingLabels) {
        this.vertexA = vertexA;
        this.vertexB = vertexB;

        // Determine the 'top' and 'bottom' vertices for display:
        if (vertexA.getDisplayPos() > vertexB.getDisplayPos()) {
            vertexA.setNorthernVertex(true, isShowingLabels);
            vertexB.setNorthernVertex(false, isShowingLabels);
        } else {
            vertexA.setNorthernVertex(false, isShowingLabels);
            vertexB.setNorthernVertex(true, isShowingLabels);
        }

        this.transaction = transaction;

        setAutoSizeChildren(false);
        getChildren().addAll(vertexA, vertexB, transaction);

        transaction.toBack();

        // Brings the interaction under the mouse to the front:
        setOnMouseEntered((final MouseEvent t) -> {
            final Interaction current = (Interaction) t.getSource();
            current.toFront();
            current.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK,
                    20.0, 0.85, 0.0, 0.0)); // Very dark and wide shadow to combat effects of cluttered ui.
        });
        setOnMouseExited((final MouseEvent t) -> {
            final Interaction current = (Interaction) t.getSource();
            current.setEffect(null);
        });

        setOnMouseClicked((final MouseEvent t) -> {
            GraphManager.getDefault().select(vertexA.getVertexID(), vertexB.getVertexID(), transaction.getTransactionID(), t.isControlDown());
            t.consume();
        });
    }

    public Vertex getTopVertex() {
        return vertexA;
    }

    public Vertex getBottomVertex() {
        return vertexB;
    }

    public void update(final double topVertY, final double bottomVertY) {
        vertexA.setLayoutY(topVertY);
        vertexB.setLayoutY(bottomVertY);
        transaction.setBeginningAndEnd(topVertY, bottomVertY);
    }
}
