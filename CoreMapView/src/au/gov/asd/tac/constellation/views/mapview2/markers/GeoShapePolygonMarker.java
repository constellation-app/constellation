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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Pair;

/**
 *
 * @author altair1673
 */
public class GeoShapePolygonMarker extends AbstractMarker {

    private static final Logger LOGGER = Logger.getLogger(GeoShapePolygonMarker.class.getName());
    private final double geoShapeYOffset = -148.5;

    private final Map<String, Pair<Polygon, List<Integer>>> geoShapes = new HashMap<>();

    public GeoShapePolygonMarker(MapView parent, int markerID, int nodeId, double xOffset, double yOffset) {
        super(parent, markerID, nodeId, xOffset, yOffset, MarkerType.POLYGON_MARKER);
    }

    public void addGeoShape(final String coordinateList, final int elementID) {
        final Polygon geoShape = new Polygon();
        geoShapes.put(coordinateList, new Pair(geoShape, Collections.emptyList()));
        geoShapes.get(coordinateList).getValue().add(elementID);
        final String[] longLatValues = coordinateList.replace("[", "").replace("]", "").split(",");

        for (int j = 0; j + 1 < longLatValues.length; j = j + 2) {
            //LOGGER.log(Level.SEVERE, "Coordinates: " + longLatValues[j] + ", " + longLatValues[j + 1]);
            geoShape.getPoints().addAll(MarkerUtilities.longToX(Double.parseDouble(longLatValues[j]), MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG) - 0.0625,
                    MarkerUtilities.latToY(Double.parseDouble(longLatValues[j + 1]), MapView.MAP_WIDTH, MapView.MAP_HEIGHT) + geoShapeYOffset);
        }

        geoShape.setFill(Color.ORANGE);


    }

    public Map<String, Pair<Polygon, List<Integer>>> getGeoShapes() {
        return geoShapes;
    }

}
