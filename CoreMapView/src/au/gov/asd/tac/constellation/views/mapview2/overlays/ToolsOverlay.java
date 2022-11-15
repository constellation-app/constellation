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
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.tac.constellation.views.mapview2.utillities.MarkerUtilities;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 *
 * @author altair1673
 */
public class ToolsOverlay extends AbstractOverlay {

    final private BorderPane overlayPane;

    final private GridPane gridPane;

    private BooleanProperty drawingEnabled = new SimpleBooleanProperty(false);
    private BooleanProperty measureEnabled = new SimpleBooleanProperty(false);

    private Label measureToggleText = new Label("Disabled");

    private int height = 75;
    private int width = 150;

    private boolean isShowing = false;

    private final String[] units = {"km", "nmi", "mi"};
    private int unitSelected = 0;

    private Label measureUnitText = new Label(units[unitSelected]);

    public ToolsOverlay() {
        super();

        overlayPane = new BorderPane();
        gridPane = new GridPane();

        overlayPane.setCenter(gridPane);

        Label measureText = new Label("Measure");
        measureText.setTextFill(Color.WHITE);

        measureToggleText.setTextFill(Color.WHITE);
        measureToggleText.setBackground(Background.fill(Color.BLACK));

        measureToggleText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!drawingEnabled.get()) {
                    measureEnabled.set(!measureEnabled.get());

                    if (measureEnabled.get()) {
                        measureToggleText.setText("Enabled");
                    } else {
                        measureToggleText.setText("Disabled");
                    }
                }

                event.consume();
            }
        });

        measureUnitText.setTextFill(Color.WHITE);
        measureUnitText.setBackground(Background.fill(Color.BLACK));

        measureUnitText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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

        Label drawToggleText = new Label("Disabled");
        drawToggleText.setTextFill(Color.WHITE);
        drawToggleText.setBackground(Background.fill(Color.BLACK));

        drawToggleText.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (!measureEnabled.get()) {
                    drawingEnabled.set(!drawingEnabled.get());

                    if (drawingEnabled.get()) {
                        drawToggleText.setText("Enabled");
                    } else {
                        drawToggleText.setText("Disabled");
                    }
                }

                event.consume();
            }
        });

        Label drawSymbol = new Label("+");
        drawSymbol.setTextFill(Color.WHITE);
        drawSymbol.setBackground(Background.fill(Color.BLACK));

        gridPane.add(measureText, 0, 0);
        gridPane.add(measureToggleText, 1, 0);
        gridPane.add(measureUnitText, 2, 0);

        gridPane.add(drawLabelText, 0, 1);
        gridPane.add(drawToggleText, 1, 1);
        gridPane.add(drawSymbol, 2, 1);

        gridPane.setPadding(new Insets(0, 0, 0, 10));

        gridPane.setVgap(10);
        gridPane.setHgap(10);

        overlayPane.setPrefHeight(height);
        overlayPane.setPrefWidth(width);
        overlayPane.setMinWidth(width);
        overlayPane.setMaxWidth(width);
        overlayPane.setMinHeight(height);
        overlayPane.setMaxHeight(height);

        overlayPane.setBackground(Background.fill(new Color(0.224, 0.239, 0.278, 1.0)));

        overlayPane.setTranslateX(815);
        overlayPane.setTranslateY(20);

    }

    public void setDistanceText(double startX, double startY, double endX, double endY) {

        /*centerY += 149;
        double centerYLat = super.YToLat(centerY, 1010.33, 1224);
        double centerXLon = super.XToLong(centerX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);*/
        startY += 149;
        endY += 149;

        double startLon = MarkerUtilities.XToLong(startX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
        double endLon = MarkerUtilities.XToLong(endX, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);

        double startLat = MarkerUtilities.YToLat(startY, 1010.33, 1224);
        double endLat = MarkerUtilities.YToLat(endY, 1010.33, 1224);

        double distance = 0;

        if (measureUnitText.getText().equals("km")) {
            distance = Math.round(Distance.Haversine.estimateDistanceInKilometers(startLat, startLon, endLat, endLon));
        } else if (measureUnitText.getText().equals("mi")) {
            distance = Math.round(Distance.Haversine.estimateDistanceInMiles(startLat, startLon, endLat, endLon));
        } else if (measureUnitText.getText().equals("nmi")) {
            distance = Math.round(Distance.Haversine.estimateDistanceInNauticalMiles(startLat, startLon, endLat, endLon));
        }
        
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        measureToggleText.setText(df.format(distance));
    }

    public void resetMeasureText() {
        measureToggleText.setText("Enabled");
    }

    public boolean getIsShowing() {
        return isShowing;
    }

    public void setIsShowing(boolean showing) {
        isShowing = showing;
    }

    public BorderPane getOverlayPane() {
        return overlayPane;
    }

    public BooleanProperty getDrawingEnabled() {
        return drawingEnabled;
    }

    public BooleanProperty getMeasureEnabled() {
        return measureEnabled;
    }
}
