/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.tooltip;

import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author sirius
 */
public class TooltipPane extends AnchorPane {

    private Pane tooltipNode = null;
    private boolean enabled = true;

    public TooltipPane() {
        setStyle("-fx-background-color: transparent;");
        mouseTransparentProperty().set(true);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void showTooltip(Pane node, double x, double y) {

        if (tooltipNode != node) {
            if (tooltipNode != null) {
                tooltipNode.setManaged(true);
                getChildren().remove(tooltipNode);
            }
            tooltipNode = node;
            tooltipNode.setManaged(false);
            getChildren().add(tooltipNode);
        }

        Point2D p = sceneToLocal(x, y);
        double left = p.getX();
        double top = p.getY();

        AnchorPane.setLeftAnchor(node, left);
        AnchorPane.setTopAnchor(node, top);
    }

    public void hideTooltip() {
        if (tooltipNode != null) {
            getChildren().remove(tooltipNode);
            tooltipNode.setManaged(true);
            tooltipNode = null;
        }
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();

        if (tooltipNode != null) {
            double left = AnchorPane.getLeftAnchor(tooltipNode);
            double top = AnchorPane.getTopAnchor(tooltipNode);
            tooltipNode.autosize();
            double right = left + tooltipNode.getWidth();
            double bottom = top + tooltipNode.getHeight();

            if (right > getWidth()) {
                left -= right - getWidth();
            }
            if (bottom > getHeight()) {
                top -= bottom - getHeight();
            }

            if (left < 0) {
                right += left;
                left = 0;
            }

            if (top < 0) {
                bottom += top;
                top = 0;
            }
            tooltipNode.resize(right - left, bottom - top);
            tooltipNode.setLayoutX(left);
            tooltipNode.setLayoutY(top);
        }
    }
}
