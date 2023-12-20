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
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MapConversions;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * Point marker that represents a geo coordinate on the map
 *
 * @author altair1673
 */
public class PointMarker extends AbstractMarker {

    private static final String MULTIPLE_VALUES_LABEL = "<Multiple Values>";
    private static double MARKER_PATH_SCALING = 14;  // Factor by with the size of the marker (in the path string) needs
                                                     // to be shrunk to be a reasonable size. The line needs to be scaled
                                                     // to counter this.

    // The actual marker path raw string
    private String path = "l-35-90 l-45-80 l-10-30 l0-45 l10-25 l15-20 l50-20 l30 0 l50 20 l15 20 l10 25 l0 45 l-10 30 l-45 80 l-35 90 m0-194 l-22-22 l22-22 l22 22 l-22 22 m0-8 l-14-14 l14-14 l14 14 l-14 14 m0-8 l-6-6 l6-6 l6 6 l-6 6";
    private double latitude;  // The latitude to display marker at
    private double longitude;  // The longitude to display marker at

    private Color attributeFillColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;  // Set a default colour for marker if using colour stored in
                                                                                // nodes colour attribute. This will get overridden by extracted
                                                                                // value
    private final List<ConstellationColor> attributeColours = new ArrayList<>();
    private final List<ConstellationColor> blazeColours = new ArrayList<>();
    private final List<ConstellationColor> overlayColours = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();
    private Color currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
    
    public PointMarker(final MapView parent, final int markerID, final int nodeId, final double latitude, final double longitude, final ConstellationColor attributeColour) {
        super(parent, markerID, nodeId, AbstractMarker.MarkerType.POINT_MARKER);

        this.latitude = latitude;
        this.longitude = longitude;

        this.attributeFillColour = Color.web(attributeColour.getHtmlColor(), MapDetails.MARKER_OPACITY);
        this.attributeColours.add(attributeColour);

        markerPath.setFill(MapDetails.MARKER_DEFAULT_FILL_COLOUR);
        markerPath.setStroke(MapDetails.MARKER_STROKE_COLOUR);
        markerPath.setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * MARKER_PATH_SCALING * this.scalingFactor);
        this.scaleMarker(parent.getCurrentScale());

        // Event handlers for the marker
        markerPath.setOnMouseEntered((final MouseEvent e) -> {
            markerPath.setFill(MapDetails.MARKER_HIGHLIGHTED_FILL_COLOUR);
            e.consume();
        });

        /**
         * On mouse leaving the marker shape reset its colour back to what it was before. This will depend on whether
         * the marker was selected or not. If it was selected, then use the selected colour, if it wasn't selected, use
         * the colour it was before, which is dependent on what mode is being used to set node colours - however the
         * member variable currentColour stores the most recently set colour (when not selected) of the node.
         */
        markerPath.setOnMouseExited((final MouseEvent e) -> {
            if (isSelected) {
                markerPath.setFill(MapDetails.MARKER_SELECTED_FILL_COLOUR);
            } else {
                markerPath.setFill(currentColour);
            }
            e.consume();
        });

        markerPath.setOnMouseClicked((final MouseEvent e) -> {
            parent.deselectAllMarkers();
            select();
            parent.addMarkerIdToSelectedList(markerID, idList, true);
            e.consume();
        });
    }

    @Override
    public void deselect() {
        markerPath.setFill(currentColour);
        isSelected = false;
    }

    @Override
    public void select() {
        isSelected = true;
        markerPath.setFill(MapDetails.MARKER_SELECTED_FILL_COLOUR);
    }

    /**
     * Change marker colour based on marker colour type chosen by the user
     *
     * @param option
     */
    @Override
    public void changeMarkerColour(final String option) {
        
        // There are multiple methods for setting marker colour, either using default colours, or extracting colours
        // from associated nodes, depending on selection, set the appropriate colour and update the currentColour value.
        if (option.equals(MapViewPane.DEFAULT_COLOURS)) {
            // Using the default colour palette to colour unselected markers. 
            currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
        } else if (option.equals(MapViewPane.USE_COLOUR_ATTR)) {
            // The colour to use will come from the store eattributeColour for the marker (which comes from the 'color'
            // attribute of the vertex the marker is tied to. If multiple vertexes share the same location (and hence
            // marker) then a seperate color is used to indicate multiple values.
            currentColour = (idList.size() > 1) ? MapDetails.MARKER_MULTI_FILL_COLOUR : attributeFillColour;
        } else if (option.equals(MapViewPane.USE_BLAZE_COL)) {
            currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
            if (!blazeColours.isEmpty()) {
                currentColour = blazeColours.size() == 1 ? Color.web(blazeColours.get(0).getHtmlColor(), MapDetails.MARKER_OPACITY) : MapDetails.MARKER_MULTI_FILL_COLOUR;
            }
        } else if (option.equals(MapViewPane.USE_OVERLAY_COL)) {
            currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
            if (!overlayColours.isEmpty()) {
                currentColour = overlayColours.size() == 1 ? Color.web(overlayColours.get(0).getHtmlColor(), MapDetails.MARKER_OPACITY) : MapDetails.MARKER_MULTI_FILL_COLOUR;
            }
        }
        markerPath.setFill(currentColour);
    }
    
    public Color getCurrentColour() {
        return this.currentColour;
    }

    public double getLattitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
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
    public void setMarkerPosition(final double mapWidth, final double mapHeight) {
        x = MapConversions.lonToMapX(longitude);
        y = MapConversions.latToMapY(latitude);
        path = "M " + x + SeparatorConstants.COMMA + " " + y + " " + path;
        markerPath.setContent(path);
    }

    /**
     * Sets one of the colour types of the marker
     *
     * @param blazeCol
     */
    public void setBlazeColour(final ConstellationColor colour) {
        // If there is more than one colour found for this marker then we will be using MARKER_MULTI_FILL_COLOUR, so
        // all that needs to be done is to store the first two unique colours found. If only one is found, then that
        // colour can be used, otehrwise if two are stored we know that we need to use MARKER_MULTI_FILL_COLOUR
        if (colour != null && !blazeColours.contains(colour) && blazeColours.size() <= 1) {
            blazeColours.add(colour);
        }
    }

    public void setOverlayColour(final ConstellationColor colour) {
        // If there is more than one colour found for this marker then we will be using MARKER_MULTI_FILL_COLOUR, so
        // all that needs to be done is to store the first two unique colours found. If only one is found, then that
        // colour can be used, otehrwise if two are stored we know that we need to use MARKER_MULTI_FILL_COLOUR
        if (colour != null && !overlayColours.contains(colour) && overlayColours.size() <= 1) {
            overlayColours.add(colour); 
        }
    }


    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    /**
     * Add another label to the set allocated to the marker. If more than one distinct label is allocated then the label
     * MULTIPLE_VALUES_LABEL will be used to represent the label.
     * @param label The label to allocate to the marker.
     */
    public void setLabelAttr(final String label) {
        if (label != null && !labels.contains(label) && labels.size() <= 1) {
            labels.add(label);   
        }
    }

    /**
     * Add another identifier to the set allocated to the marker. If more than one distinct label is allocated then the
     * label MULTIPLE_VALUES_LABEL will be used to represent the label.
     * @param identifier The identifier to allocate to the marker.
     */
    public void setIdentAttr(final String identifier) {
        if (identifier != null && !identifiers.contains(identifier) && identifiers.size() <= 1) {
            identifiers.add(identifier);   
        }
    }

    /**
     * Return the currently used label string.
     * @return The currently used label string.
     */
    public String getLabelAttr() {
        if (labels.isEmpty()) {
            return null;
        }
        return (labels.size() > 1 ? MULTIPLE_VALUES_LABEL : labels.get(0));
    }

    /**
     * Return the currently used identifier string.
     * @return The currently used identifier string.
     */
    public String getIdentAttr() {
        if (identifiers.isEmpty()) {
            return null;
        }
        return (identifiers.size() > 1 ? MULTIPLE_VALUES_LABEL : identifiers.get(0));
    }
    
    @Override
    public void scaleMarker(final double scalingFactor) {
        // As the map increases in scale, marker lines need to reduce to ensure they continue to appear the same size.
        this.scalingFactor = 1 / scalingFactor;
        markerPath.setScaleX(this.scalingFactor / MARKER_PATH_SCALING);
        markerPath.setScaleY(this.scalingFactor / MARKER_PATH_SCALING);
        markerPath.setStrokeWidth(MapDetails.MARKER_LINE_WIDTH * MARKER_PATH_SCALING * this.scalingFactor);

        final double heightDifference = (getY()) - (markerPath.getBoundsInParent().getCenterY() + (markerPath.getBoundsInParent().getHeight() / 2));
        markerPath.setTranslateY(markerPath.getTranslateY() + heightDifference); 
    }
}
