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
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import au.gov.tac.constellation.views.mapview2.utillities.MarkerUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.logging.Logger;

/**
 *
 * @author altair1673
 */
public class PointMarker extends AbstractMarker {

    // The actual marker path raw string
    private String path = "c-20.89-55.27-83.59-81.74-137-57.59-53.88,24.61-75.7,87.77-47.83,140.71,12.54,23.69,26.47,46.44,39.93,70.12,15.79,27.4,32,55.27,50.16,87.31a101.37,101.37,0,0,1,4.65-9.76c27.86-49.23,56.66-98,84-147.68,14.86-26,16.72-54.8,6-83.12z";
    private double lattitude;
    private double longitude;
    private double x = 0;
    private double y = 0;
    private double scale;

    private String defaultColour = "#FF0000";
    private String multiValCol = "#D3D3D3";
    private String attributeColour = defaultColour;
    private String blazeColour = null;
    private int blazeColourCount = 0;
    private int overlayColourCount = 0;
    private String overlayColour = null;
    private String currentColour = defaultColour;

    private String labelAttr = null;
    private int labelAttrCount = 0;

    private String identifierAttr = null;
    private int identifierCount = 0;

    public PointMarker(MapView parent, int markerID, int nodeId, double lattitude, double longitude, double scale, double xOffset, double yOffset, String attrColour) {
        super(parent, markerID, nodeId, xOffset, yOffset, AbstractMarker.MarkerType.POINT_MARKER);

        this.lattitude = lattitude;
        this.longitude = longitude;
        this.scale = scale;
        this.attributeColour = (attrColour.isEmpty() || attrColour.isBlank()) ? defaultColour : attrColour;

        markerPath.setScaleX(scale);
        markerPath.setScaleY(scale);

        markerPath.setFill(Color.web(currentColour));
        markerPath.setStroke(Color.BLACK);
        markerPath.setOpacity(0.6);
        markerPath.setStrokeWidth(5);

        // Event handlers for the marker
        markerPath.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                if (!isSelected) {
                    markerPath.setFill(Color.ORANGE);
                }
                e.consume();
            }
        });

        markerPath.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {

                if (!isSelected) {
                    markerPath.setFill(Color.web(currentColour));
                }
                e.consume();
            }
        });

        markerPath.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                parent.deselectAllMarkers();
                select();
                parent.addMarkerIdToSelectedList(markerID, idList, true);
                e.consume();
            }
        });

    }

    public void deselect() {
        markerPath.setFill(Color.web(currentColour));
        isSelected = false;
    }

    public void select() {
        isSelected = true;
        markerPath.setFill(Color.BLUE);
    }

    /**
     * Change marker colour based on marker colour type choses by the user
     *
     * @param option
     */
    public void changeMarkerColour(String option) {
        // Depending on the option change the colour of the marker
        if (option.equals(MapViewPane.DEFAULT_COLOURS)) {
            currentColour = defaultColour;
            markerPath.setFill(Color.web(currentColour));
        } else if (option.equals(MapViewPane.USE_COLOUR_ATTR)) {
            if (idList.size() > 1) {
                currentColour = multiValCol;
                markerPath.setFill(Color.web(currentColour));
            } else {

                markerPath.setFill(Color.web(attributeColour));
                currentColour = attributeColour;

            }
        } else if (option.equals(MapViewPane.USE_BLAZE_COL)) {
            if (blazeColour != null) {
                ConstellationColor colour = ConstellationColor.getColorValue(blazeColour);


                if (blazeColourCount == 1) {
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
            if (overlayColour != null) {
                ConstellationColor colour = ConstellationColor.getColorValue(overlayColour);

                if (overlayColourCount == 1) {
                    currentColour = colour.getHtmlColor();
                    markerPath.setFill(Color.web(currentColour));
                } else {
                    markerPath.setFill(Color.web(multiValCol));
                    currentColour = multiValCol;
                }

            } else {
                markerPath.setFill(Color.web(defaultColour));
            }
        }

    }



    public double getLattitude() {
        return lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getScale() {
        return scale;
    }

    public String getPath() {
        return path;
    }

    /**
     * Sets the marker position on the map
     *
     * @param mapWidth
     * @param mapHeight
     */
    @Override
    public void setMarkerPosition(double mapWidth, double mapHeight) {
        x = MarkerUtilities.longToX(longitude, MapView.MIN_LONG, mapWidth, MapView.MAX_LONG - MapView.MIN_LONG);
        y = MarkerUtilities.latToY(lattitude, mapWidth, mapHeight);
        x += xOffset;
        y -= yOffset;

        super.setX(x);
        super.setY(y);


        path = "M " + x + ", " + y + " Z " + path;

        markerPath.setContent(path);
    }

    /**
     * Sets one of the colour types of the marker
     *
     * @param blazeCol
     */
    public void setBlazeColour(String blazeCol) {
        // Get the blaze colure in the correct format
        blazeCol = blazeCol.split(";")[1];


        if (blazeColourCount == 0) {
            blazeColour = blazeCol;
        }

        ++blazeColourCount;

    }

    public void setOverlayColour(String overlayCol) {

        if (overlayColourCount == 0) {
            overlayColour = overlayCol;
        }

        ++overlayColourCount;
    }

    public String getBlazeColour() {
        return blazeColour;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setLabelAttr(String labelAttribute) {

        if (labelAttrCount == 0) {
            labelAttr = labelAttribute;
        }

        ++labelAttrCount;
    }

    public void setIdentAttr(String identAttribute) {

        if (identifierCount == 0) {
            identifierAttr = identAttribute;
        }

        ++identifierCount;
    }

    public String getLabelAttr() {

        if (labelAttrCount > 1) {
            return "<Multiple Values>";
        }

        return labelAttr;
    }

    public String getIdentAttr() {

        if (identifierCount > 1) {
            return "<Multiple Values>";
        }

        return identifierAttr;
    }

    public String getCurrentColour() {
        return currentColour;
    }

    public String getDefaultColour() {
        return defaultColour;
    }

}
