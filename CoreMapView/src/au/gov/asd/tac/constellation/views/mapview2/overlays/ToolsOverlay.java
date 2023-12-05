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

import au.gov.asd.tac.constellation.utilities.geospatial.Distance;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MarkerUtilities;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 * Overlay that has measuring and drawing functionality
 *
 * @author altair1673
 */
public class ToolsOverlay extends AbstractOverlay {

    // Flags for the different mdoes available in the tools overlay
    private final BooleanProperty drawingEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty measureEnabled = new SimpleBooleanProperty(false);

    private static final String DISABLED_STRING = "Disabled";
    private static final String ENABLED_STRING = "Enabled";

    private final Label measureToggleText = new Label(DISABLED_STRING);

    private static final double LOCATION_Y_OFFSET = 149;
    private final String[] units = {"km", "nmi", "mi"};
    private int unitSelected = 0;

    private final Label measureUnitText = new Label(units[unitSelected]);

    /**
     * Set up the UI
     *
     * @param positionX - x coordinate of pane
     * @param positionY - y coordinate of pane
     */
    public ToolsOverlay(final double positionX, final double positionY) {
        super(positionX, positionY);

        // Set up the ui for main border pane
        overlayPane.setPrefHeight(height);
        overlayPane.setPrefWidth(width);
        overlayPane.setMinWidth(width);
        overlayPane.setMaxWidth(width);
        overlayPane.setMinHeight(height);
        overlayPane.setMaxHeight(height);

        final Label measureText = new Label("Measure");
        measureText.setTextFill(Color.WHITE);

        measureToggleText.setTextFill(Color.WHITE);
        measureToggleText.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        measureToggleText.setOnMouseClicked((final MouseEvent event) -> {
            if (!drawingEnabled.get()) {
                measureEnabled.set(!measureEnabled.get());
                measureToggleText.setText(measureEnabled.get() ? ENABLED_STRING : DISABLED_STRING);
            }

            event.consume();
        });

        measureUnitText.setTextFill(Color.WHITE);
        measureUnitText.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        measureUnitText.setOnMouseClicked((final MouseEvent event) -> {
            if (!drawingEnabled.get()) {
                ++unitSelected;

                if (unitSelected > units.length - 1) {
                    unitSelected = 0;
                }
                measureUnitText.setText(units[unitSelected]);
            }
            event.consume();
        });

        final Label drawLabelText = new Label("Draw");
        drawLabelText.setTextFill(Color.WHITE);

        final Label drawToggleText = new Label(DISABLED_STRING);
        drawToggleText.setTextFill(Color.WHITE);
        drawToggleText.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        drawToggleText.setOnMouseClicked((final MouseEvent event) -> {
            if (!measureEnabled.get()) {
                drawingEnabled.set(!drawingEnabled.get());
                drawToggleText.setText(drawingEnabled.get() ? ENABLED_STRING : DISABLED_STRING);
            }

            event.consume();
        });

        final String drawDescription = " > Click on the map to draw a\npoint marker.\n"
                + " > Click on the map while\nholding shift to\nbegin drawing a circle\n"
                + "  marker, click again with\nor without shift to complete it.\n"
                + " > Click on the map while\nholding control to\nbegin drawing a\n polygon"
                + "  marker, continue\nclicking while holding control\nto draw edges,"
                + "  then\nrelease control and click\n once more to complete it.\n"
                + " > Click on a drawn marker\n to remove it.";

        final Label description = new Label(drawDescription);
        description.setMinWidth(width);
        description.setMaxWidth(width);

        drawingEnabled.addListener((o, oldVal, newVal) -> {
            if (drawingEnabled.get()) {
                overlayPane.setMinHeight(overlayPane.getHeight() * 4.5);
                overlayPane.setMaxHeight(overlayPane.getHeight() * 4.5);
                gridPane.add(description, 0, 2, 7, 3);
            } else {
                overlayPane.setMinHeight(height);
                overlayPane.setMaxHeight(height);
                gridPane.getChildren().removeIf(node -> GridPane.getColumnIndex(node) == 0 && GridPane.getRowIndex(node) == 2);
            }
        });

        final Label drawSymbol = new Label("+");
        drawSymbol.setTextFill(Color.WHITE);
        drawSymbol.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        gridPane.add(measureText, 0, 0);
        gridPane.add(measureToggleText, 1, 0);
        gridPane.add(measureUnitText, 2, 0);

        gridPane.add(drawLabelText, 0, 1);
        gridPane.add(drawToggleText, 1, 1);
        gridPane.add(drawSymbol, 2, 1);
    }

    /**
     * Displays the distance from one mouse location to another in the specified
     * unit
     *
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    public void setDistanceText(final double startX, final double startY, final double endX, final double endY) {
        // Calculate lattitude and longitude from coordinates
        final double startLon = MarkerUtilities.xToLong(startX, MapView.MIN_LONG, MapView.MAP_VIEWPORT_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG);
        final double endLon = MarkerUtilities.xToLong(endX, MapView.MIN_LONG, MapView.MAP_VIEWPORT_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG);

        final double startLat = MarkerUtilities.yToLat(startY + LOCATION_Y_OFFSET, MapView.MAP_VIEWPORT_WIDTH, MapView.MAP_VIEWPORT_HEIGHT);
        final double endLat = MarkerUtilities.yToLat(endY + LOCATION_Y_OFFSET, MapView.MAP_VIEWPORT_WIDTH, MapView.MAP_VIEWPORT_HEIGHT);

        double distance = 0;

        // Calculate distance depending on unit of measure
        if ("km".equals(measureUnitText.getText())) {
            distance = Distance.Haversine.estimateDistanceInKilometers(startLat, startLon, endLat, endLon);
        } else if ("mi".equals(measureUnitText.getText())) {
            distance = Distance.Haversine.estimateDistanceInMiles(startLat, startLon, endLat, endLon);
        } else if ("nmi".equals(measureUnitText.getText())) {
            distance = Distance.Haversine.estimateDistanceInNauticalMiles(startLat, startLon, endLat, endLon);
        }
        
        final DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        measureToggleText.setText(df.format(distance));
    }

    /**
     * Toggles the measurement text from disabled to enabled and vice versa
     */
    public void resetMeasureText() {
        if (measureEnabled.get()) {
            measureToggleText.setText(ENABLED_STRING);
        } else {
            measureToggleText.setText(DISABLED_STRING);
        }
    }

    public BooleanProperty getDrawingEnabled() {
        return drawingEnabled;
    }

    public BooleanProperty getMeasureEnabled() {
        return measureEnabled;
    }
}
