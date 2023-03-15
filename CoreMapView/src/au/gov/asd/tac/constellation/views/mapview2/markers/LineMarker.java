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
package au.gov.asd.tac.constellation.views.mapview2.markers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MarkerUtilities;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Line marker to connect markers together
 *
 * @author altair1673
 */
public class LineMarker extends AbstractMarker {
    private final double lat1;
    private final double lon1;
    private final double lat2;
    private final double lon2;

    private double x1;
    private double y1;

    private double x2;
    private double y2;


    public LineMarker(final MapView parent, final int markerID, final int id, final double lattitude1, final double longitude1, final double lattitude2, final double longitude2, final double xOffset, final double yOffset) {
        super(parent, markerID, id, xOffset, yOffset, AbstractMarker.MarkerType.LINE_MARKER);

        lat1 = lattitude1;
        lon1 = longitude1;
        lat2 = lattitude2;
        lon2 = longitude2;

        markerPath.setStroke(Color.BLACK);
        markerPath.setStrokeWidth(1);

        // Set event handlers for the line marker
        markerPath.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {

                if (!isSelected) {
                    markerPath.setStroke(Color.ORANGE);
                }
                e.consume();
            }
        });

        markerPath.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {

                if (!isSelected) {
                    markerPath.setStroke(Color.BLACK);
                }

                e.consume();
            }
        });

        markerPath.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                isSelected = true;
                markerPath.setStroke(Color.BLUE);
                parent.addMarkerIdToSelectedList(markerID, idList, false);
                e.consume();
            }
        });

    }

    /**
     * Set the position of the marker in x and y from geo coordinates
     *
     * @param mapWidth
     * @param mapHeight
     */
    @Override
    public void setMarkerPosition(final double mapWidth, final double mapHeight) {
        x1 = MarkerUtilities.longToX(lon1, MapView.MIN_LONG, mapWidth, MapView.MAX_LONG - MapView.MIN_LONG);
        y1 = MarkerUtilities.latToY(lat1, mapWidth, mapHeight);

        x1 += xOffset;
        y1 -= yOffset;

        x2 = MarkerUtilities.longToX(lon2, MapView.MIN_LONG, mapWidth, MapView.MAX_LONG - MapView.MIN_LONG);
        y2 = MarkerUtilities.latToY(lat2, mapWidth, mapHeight);

        x2 += xOffset;
        y2 -= yOffset;

        String path = "M " + x1 + ", " + y1 + " Z L " + x2 + "," + y2 + " z";

        markerPath.setContent(path);
    }

    public double getStartX() {
        return x1;
    }

    public double getStartY() {
        return y1;
    }

    public double getEndX() {
        return x2;
    }

    public double getEndY() {
        return y2;
    }
}