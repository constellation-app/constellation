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

import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.GeoShape;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

/**
 *
 * @author altair1673
 */
public class GeoShapePolygonMarker extends AbstractMarker {

    private final Map<String, Pair<GeoShape, List<Integer>>> geoShapes = new HashMap<>();
    private double edgeScalingFactor;

    public GeoShapePolygonMarker(MapView parent, int markerID, int nodeId, double xOffset, double yOffset) {
        super(parent, markerID, nodeId, xOffset, yOffset, MarkerType.POLYGON_MARKER);
    }

    public void addGeoShape(final String coordinateList, final int elementID) {
        this.scalingFactor = 1 / parent.getScalingFactor();
        final GeoShape geoShape = new GeoShape();
        geoShapes.put(coordinateList, new Pair(geoShape, new ArrayList<>()));
        geoShapes.get(coordinateList).getValue().add(elementID);
        final String[] longLatValues = coordinateList.replace("[", "").replace("]", "").split(",");
        double xTotal = 0;
        double yTotal = 0;
        double coordinateCount = 0;
        for (int j = 0; j + 1 < longLatValues.length; j = j + 2) {
            geoShape.getPoints().addAll(MapConversions.lonToMapX(Double.parseDouble(longLatValues[j])) - 0.0625,
                    MapConversions.latToMapY(Double.parseDouble(longLatValues[j + 1])));
            xTotal += geoShape.getPoints().get(geoShape.getPoints().size() - 2);
            yTotal += geoShape.getPoints().get(geoShape.getPoints().size() - 1);
            ++coordinateCount;
        }
        geoShape.setCenterX(xTotal / coordinateCount);
        geoShape.setCenterY(yTotal / coordinateCount);
        geoShape.setFill(MapDetails.MARKER_DEFAULT_FILL_COLOUR);
        geoShape.setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * this.scalingFactor);
        geoShape.setStroke(MapDetails.MARKER_STROKE_COLOUR);

        // Event handlers for the marker
        geoShape.setOnMouseEntered((final MouseEvent e) -> {
            if (!isSelected) {
                geoShape.setFill(MapDetails.MARKER_HIGHLIGHTED_FILL_COLOUR);
            }
            e.consume();
        });

        geoShape.setOnMouseExited((final MouseEvent e) -> {
            if (!isSelected) {
                geoShape.setFill(geoShape.getCurrentColour());
            }
            e.consume();
        });

        geoShape.setOnMouseClicked((final MouseEvent e) -> {
            parent.deselectAllMarkers();
            select();
            parent.addMarkerIdToSelectedList(markerID, geoShapes.get(coordinateList).getValue(), true);
            e.consume();
        });

    }
    
    @Override
    public void scaleMarker(final double scalingFactor) {
        // As the map increases in scale, marker lines need to reduce to ensure they continue to appear the same size.
        this.scalingFactor = 1 / scalingFactor;
        geoShapes.values().forEach(shapePair -> shapePair.getKey().setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * this.scalingFactor));
    }

    @Override
    public void changeMarkerColour(final String option) {
        geoShapes.values().forEach(shapePair -> shapePair.getKey().changeColour(option));
    }


    @Override
    public void deselect() {
        geoShapes.values().forEach(shapePair -> shapePair.getKey().setFill(shapePair.getKey().getCurrentColour()));
        isSelected = false;
    }

    @Override
    public void select() {
        isSelected = true;
        geoShapes.values().forEach(shapePair -> shapePair.getKey().setFill(MapDetails.MARKER_SELECTED_FILL_COLOUR));
    }

    public Map<String, Pair<GeoShape, List<Integer>>> getGeoShapes() {
        return geoShapes;
    }


    public void setBlazeColour(final String blaze, final String coordinateKey) {
        geoShapes.get(coordinateKey).getKey().setBlazeColour(blaze);
    }

    public void setOverlayColour(final String overlayCol, final String coordinateKey) {
        geoShapes.get(coordinateKey).getKey().setOverlayColour(overlayCol);
    }

    public void setAttributeColour(final String attributeColour, final String coordinateKey) {
        geoShapes.get(coordinateKey).getKey().setAttributeColour(attributeColour);
    }

    public String getLabelAttr(final String coordinateKey) {
        return geoShapes.get(coordinateKey).getKey().getLabelAttr();
    }

    public String getIdentAttr(final String coordinateKey) {
        return geoShapes.get(coordinateKey).getKey().getIdentAttr();
    }

    public void setLabelAttr(final String labelAttribute, final String coordinateKey) {
        geoShapes.get(coordinateKey).getKey().setLabelAttr(labelAttribute);
    }

    public void setIdentAttr(final String identAttribute, final String coordinateKey) {
        geoShapes.get(coordinateKey).getKey().setIdentAttr(identAttribute);
    }
}
