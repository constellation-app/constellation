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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MarkerUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.MouseEvent;
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

    private String defaultColour = "#FF0000";
    private String multiValCol = "#D3D3D3";
    private String attributeColour = defaultColour;
    private List<String> blazeColours = new ArrayList<>();
    private List<String> overlayColours = new ArrayList<>();
    //private String overlayColour = null;
    private String currentColour = defaultColour;

    private final Map<String, Pair<Polygon, List<Integer>>> geoShapes = new HashMap<>();

    public GeoShapePolygonMarker(MapView parent, int markerID, int nodeId, double xOffset, double yOffset) {
        super(parent, markerID, nodeId, xOffset, yOffset, MarkerType.POLYGON_MARKER);
    }

    public void addGeoShape(final String coordinateList, final int elementID) {
        final Polygon geoShape = new Polygon();
        geoShapes.put(coordinateList, new Pair(geoShape, new ArrayList<>()));
        geoShapes.get(coordinateList).getValue().add(elementID);
        final String[] longLatValues = coordinateList.replace("[", "").replace("]", "").split(",");

        for (int j = 0; j + 1 < longLatValues.length; j = j + 2) {
            //LOGGER.log(Level.SEVERE, "Coordinates: " + longLatValues[j] + ", " + longLatValues[j + 1]);
            geoShape.getPoints().addAll(MarkerUtilities.longToX(Double.parseDouble(longLatValues[j]), MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG) - 0.0625,
                    MarkerUtilities.latToY(Double.parseDouble(longLatValues[j + 1]), MapView.MAP_WIDTH, MapView.MAP_HEIGHT) + geoShapeYOffset);
        }

        geoShape.setFill(Color.RED);
        geoShape.setStroke(Color.BLACK);
        geoShape.setOpacity(0.5);

        // Event handlers for the marker
        geoShape.setOnMouseEntered((final MouseEvent e) -> {
            if (!isSelected) {
                geoShape.setFill(Color.ORANGE);
            }
            e.consume();
        });

        geoShape.setOnMouseExited((final MouseEvent e) -> {
            if (!isSelected) {
                geoShape.setFill(Color.web(currentColour));
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
    public void changeMarkerColour(final String option) {
        // Depending on the option change the colour of the marker
        if (option.equals(MapViewPane.DEFAULT_COLOURS)) {
            currentColour = defaultColour;
            changeColourOfAllShapes(currentColour);
        } else if (option.equals(MapViewPane.USE_COLOUR_ATTR)) {
            geoShapes.values().forEach(shapePair -> {
                if(shapePair.getValue().size() >= 1)
                {
                    currentColour = multiValCol;
                    shapePair.getKey().setFill(Color.web(currentColour));
                }
                else if(shapePair.getValue().size() == 1)
                {
                    currentColour = attributeColour;
                    shapePair.getKey().setFill(Color.web(currentColour));
                }
            });

        } else if (option.equals(MapViewPane.USE_BLAZE_COL)) {
            if (blazeColours.size() != 0) {
                final ConstellationColor colour = ConstellationColor.getColorValue(blazeColours.get(0));
                if (blazeColours.size() == 1) {
                    currentColour = colour.getHtmlColor();
                    markerPath.setFill(Color.web(currentColour));
                } else {
                    markerPath.setFill(Color.web(multiValCol));
                    currentColour = multiValCol;
                }

            } else {
                markerPath.setFill(Color.web(defaultColour));
            }
        } else if (option.equals(MapViewPane.USE_OVERLAY_COL)) {
            /*if (overlayColour != null) {
                final ConstellationColor colour = ConstellationColor.getColorValue(overlayColour);

                if (overlayColourCount == 1) {
                    currentColour = colour.getHtmlColor();
                    markerPath.setFill(Color.web(currentColour));
                } else {
                    markerPath.setFill(Color.web(multiValCol));
                    currentColour = multiValCol;
                }

            } else {
                markerPath.setFill(Color.web(defaultColour));
            }*/
        }
    }

    private void changeColourOfAllShapes(final String col) {
        geoShapes.values().forEach(shapePair -> shapePair.getKey().setFill(Color.web(col)));
    }

    @Override
    public void deselect() {
        geoShapes.values().forEach(shapePair -> shapePair.getKey().setFill(Color.web(currentColour)));
        isSelected = false;
    }

    @Override
    public void select() {
        isSelected = true;
        geoShapes.values().forEach(shapePair -> shapePair.getKey().setFill(Color.BLUE));
    }

    public Map<String, Pair<Polygon, List<Integer>>> getGeoShapes() {
        return geoShapes;
    }

    public void setBlazeColour(final String blaze) {
        if (blazeColours.size() > 1) {
            return;
        }

        final String blazeCol = blaze.split(SeparatorConstants.SEMICOLON)[1];

        blazeColours.add(blazeCol);
    }

    public void setOverlayColour(final String overlayCol) {
        if (overlayColours.size() > 1) {
            return;
        }

        overlayColours.add(overlayCol);
    }

    public void setAttributeColour(String attributeColour) {
        this.attributeColour = attributeColour;
    }


}
