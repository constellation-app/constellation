/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.overlays;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 * Parent marker for all overlay classes
 *
 * @author altair1673
 */
public class AbstractOverlay {

    // The main pane to hold all UI elements
    protected BorderPane overlayPane = null;
    protected GridPane gridPane = null;

    protected int height = 75;
    protected int width = 175;

    protected boolean showing = false;

    public AbstractOverlay(final double positionX, final double positionY) {

        overlayPane = new BorderPane();
        gridPane = new GridPane();

        overlayPane.setCenter(gridPane);

        // Set up the ui for main border pane
        overlayPane.setPrefHeight(height);
        overlayPane.setPrefWidth(width);
        overlayPane.setMinWidth(width);
        overlayPane.setMaxWidth(width);
        overlayPane.setMinHeight(height);
        overlayPane.setMaxHeight(height);

        overlayPane.setTranslateX(positionX);
        overlayPane.setTranslateY(positionY);

        gridPane.setPadding(new Insets(0, 0, 0, 10));

        gridPane.setVgap(10);
        gridPane.setHgap(10);

        overlayPane.setOpacity(0.95);
        overlayPane.setBackground(new Background(new BackgroundFill(new Color(0.224, 0.239, 0.278, 1.0), null, null)));

        overlayPane.setVisible(false);
    }

    public BorderPane getOverlayPane() {
        return overlayPane;
    }

    public boolean isShowing() {
        return showing;
    }

    public void setIsShowing(final boolean showing) {
        this.showing = showing;
    }

    /**
     * Toggle whether overlay is visible
     */
    public void toggleOverlay() {
        showing = !showing;
        overlayPane.setVisible(!overlayPane.isVisible());
    }
}
