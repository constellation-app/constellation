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
package au.gov.asd.tac.constellation.views.mapview2.markers;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import javafx.scene.input.MouseEvent;

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


    public LineMarker(final MapView parent, final int markerID, final int id, final double lattitude1, final double longitude1, final double lattitude2, final double longitude2) {
        super(parent, markerID, id, AbstractMarker.MarkerType.LINE_MARKER);
        lat1 = lattitude1;
        lon1 = longitude1;
        lat2 = lattitude2;
        lon2 = longitude2;

        markerPath.setStroke(MapDetails.MARKER_STROKE_PAINT_GRADIENT);
        markerPath.setStrokeWidth(30 * MapDetails.MARKER_LINE_WIDTH * parent.getScaledMapLineWidth());

        // Set event handlers for the line marker
        markerPath.setOnMouseEntered(e -> {
            if (!isSelected) {
                markerPath.setStroke(MapDetails.MARKER_STROKE_PAINT_HIGHLIGHT_GRADIENT);
            }
            e.consume();
        });

        markerPath.setOnMouseExited((final MouseEvent e) -> {
            if (!isSelected) {
                markerPath.setStroke(MapDetails.MARKER_STROKE_PAINT_GRADIENT);
            }

            e.consume();
        });

        markerPath.setOnMouseClicked((final MouseEvent e) -> {
            isSelected = true;
            markerPath.setStroke(MapDetails.MARKER_STROKE_PAINT_SELECTED_GRADIENT);
            parent.addMarkerIdToSelectedList(markerID, idList, false);
            e.consume();
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
        x1 = MapConversions.lonToMapX(lon1);
        y1 = MapConversions.latToMapY(lat1);

        x2 = MapConversions.lonToMapX(lon2);
        y2 = MapConversions.latToMapY(lat2);

        final String path = "M " + x1 + SeparatorConstants.COMMA + " " + y1 + " L " + x2 + SeparatorConstants.COMMA + y2 + " z";

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

    @Override
    public void scaleMarker(final double scalingFactor) {
    }
}
