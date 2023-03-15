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


        Label measureText = new Label("Measure");
        measureText.setTextFill(Color.WHITE);

        measureToggleText.setTextFill(Color.WHITE);
        measureToggleText.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        measureToggleText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (!drawingEnabled.get()) {
                    measureEnabled.set(!measureEnabled.get());

                    if (measureEnabled.get()) {
                        measureToggleText.setText(ENABLED_STRING);
                    } else {
                        measureToggleText.setText(DISABLED_STRING);
                    }
                }

                event.consume();
            }
        });

        measureUnitText.setTextFill(Color.WHITE);
        measureUnitText.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        measureUnitText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (!drawingEnabled.get()) {
                    ++unitSelected;

                    if (unitSelected > units.length - 1) {
                        unitSelected = 0;
                    }
                    measureUnitText.setText(units[unitSelected]);
                }
                event.consume();
            }
        });

        Label drawLabelText = new Label("Draw");
        drawLabelText.setTextFill(Color.WHITE);

        Label drawToggleText = new Label(DISABLED_STRING);
        drawToggleText.setTextFill(Color.WHITE);
        drawToggleText.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        drawToggleText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (!measureEnabled.get()) {
                    drawingEnabled.set(!drawingEnabled.get());

                    if (drawingEnabled.get()) {
                        drawToggleText.setText(ENABLED_STRING);
                    } else {
                        drawToggleText.setText(DISABLED_STRING);
                    }
                }

                event.consume();
            }
        });

        final String drawDescription = " > Click on the map to draw a point marker.\n"
                + " > Click on the map while holding\n shift to begin drawing a circle"
                + "  marker, click again with or without shift to complete it.\n"
                + " > Click on the map while holding\n control to begin drawing a polygon"
                + "  marker, continue clicking while holding control to draw edges,"
                + "  then release control and click\n once more to complete it.\n"
                + " > Click on a drawn marker to remove it.";

        Label descriptionLabel = new Label(drawDescription);
        descriptionLabel.setScaleX(0.5);
        descriptionLabel.setScaleY(0.5);

        drawingEnabled.addListener((o, oldVal, newVal) -> {
            if (drawingEnabled.get()) {
                gridPane.add(descriptionLabel, 0, 2, 3, 3);
            } else {
                gridPane.getChildren().removeIf(node -> GridPane.getColumnIndex(node) == 0 && GridPane.getRowIndex(node) == 2);
            }
        });

        Label drawSymbol = new Label("+");
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
    public void setDistanceText(final double startX, double startY, final double endX, double endY) {
        startY += LOCATION_Y_OFFSET;
        endY += LOCATION_Y_OFFSET;

        // Calculate lattitude and longitude from coordinates
        double startLon = MarkerUtilities.xToLong(startX, MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG);
        double endLon = MarkerUtilities.xToLong(endX, MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG);

        double startLat = MarkerUtilities.yToLat(startY, MapView.MAP_WIDTH, MapView.MAP_HEIGHT);
        double endLat = MarkerUtilities.yToLat(endY, MapView.MAP_WIDTH, MapView.MAP_HEIGHT);

        double distance = 0;

        // Calculate distance depending on unit of measure
        if ("km".equals(measureUnitText.getText())) {
            distance = Distance.Haversine.estimateDistanceInKilometers(startLat, startLon, endLat, endLon);
        } else if ("mi".equals(measureUnitText.getText())) {
            distance = Distance.Haversine.estimateDistanceInMiles(startLat, startLon, endLat, endLon);
        } else if ("nmi".equals(measureUnitText.getText())) {
            distance = Distance.Haversine.estimateDistanceInNauticalMiles(startLat, startLon, endLat, endLon);
        }
        
        DecimalFormat df = new DecimalFormat("#.###");
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