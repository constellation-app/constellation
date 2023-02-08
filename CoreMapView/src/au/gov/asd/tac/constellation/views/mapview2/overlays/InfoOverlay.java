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

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.tac.constellation.views.mapview2.utillities.MarkerUtilities;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

/**
 *
 * @author altair1673
 */
public class InfoOverlay extends AbstractOverlay {


    private Label lonText = null;
    private Label latText = null;

    private final double locationYOffset = 149;

    /**
     * Set up the UI
     *
     * @param positionX
     * @param positionY
     */
    public InfoOverlay(double positionX, double positionY) {
        super(positionX, positionY);

        width = 200;
        overlayPane.setPrefWidth(width);
        overlayPane.setMaxWidth(width);
        overlayPane.setMinWidth(width);

        gridPane.add(new Label("Location"), 0, 0);

        overlayPane.setCenter(gridPane);

        lonText = new Label("0.000°");
        latText = new Label("0.000°");

        gridPane.add(lonText, 2, 0);
        gridPane.add(latText, 1, 0);

    }

    /**
     * Update the location UI
     *
     * @param x
     * @param y
     */
    public void updateLocation(double x, double y) {
        y += locationYOffset;

        double lon = MarkerUtilities.XToLong(x, MapView.minLong, MapView.mapWidth, MapView.maxLong - MapView.minLong);
        double lat = MarkerUtilities.YToLat(y, MapView.mapWidth, MapView.mapHeight);

        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.CEILING);

        lonText.setText(df.format(lon) + "°");
        latText.setText(df.format(lat) + "°");
    }

}
