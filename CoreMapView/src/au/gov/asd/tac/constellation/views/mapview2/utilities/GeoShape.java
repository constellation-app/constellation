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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Manage display of GeoShapes on the MapView. This manages the colours and labels used to display the on shape as well
 * as the position and layout of the shape.
 *
 * @author serpens24, altair1673
 */
public class GeoShape extends Polygon {

    private static final String MULTIPLE_VALUES_LABEL = "<Multiple Values>";

    private final List<ConstellationColor> attributeColours = new ArrayList<>();
    private final List<ConstellationColor> blazeColours = new ArrayList<>();
    private final List<ConstellationColor> overlayColours = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();
    private Color currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;

    private double centerX;
    private double centerY;

    public GeoShape() {
    }

    /**
     * Add another colour to the set of attribute colours allocated to this shape. If more than one distinct colour is
     * allocated then a special MARKER_MULTI_FILL_COLOUR will be used to represent the colour.
     *
     * @param colour The attribute colour to allocate to the shape.
     */
    public void setAttributeColour(final ConstellationColor colour) {
        // If there is more than one colour found for this marker then we will be using MARKER_MULTI_FILL_COLOUR, so
        // all that needs to be done is to store the first two unique colours found. If only one is found, then that
        // colour can be used, otehrwise if two are stored we know that we need to use MARKER_MULTI_FILL_COLOUR
        if (colour != null && !attributeColours.contains(colour) && attributeColours.size() <= 1) {
            attributeColours.add(colour);
        }
    }

    /**
     * Add another colour to the set of blaze colours allocated to this shape. If more than one distinct colour is
     * allocated then a special MARKER_MULTI_FILL_COLOUR will be used to represent the colour.
     *
     * @param colour The blaze colour to allocate to the shape.
     */
    public void setBlazeColour(final ConstellationColor colour) {
        // If there is more than one colour found for this marker then we will be using MARKER_MULTI_FILL_COLOUR, so
        // all that needs to be done is to store the first two unique colours found. If only one is found, then that
        // colour can be used, otehrwise if two are stored we know that we need to use MARKER_MULTI_FILL_COLOUR
        if (colour != null && !blazeColours.contains(colour) && blazeColours.size() <= 1) {
            blazeColours.add(colour);
        }
    }

    /**
     * Add another colour to the set of overlay colours allocated to this shape. If more than one distinct colour is
     * allocated then a special MARKER_MULTI_FILL_COLOUR will be used to represent the colour.
     *
     * @param colour The overlay colour to allocate to the shape.
     */
    public void setOverlayColour(final ConstellationColor colour) {
        // If there is more than one colour found for this marker then we will be using MARKER_MULTI_FILL_COLOUR, so
        // all that needs to be done is to store the first two unique colours found. If only one is found, then that
        // colour can be used, otehrwise if two are stored we know that we need to use MARKER_MULTI_FILL_COLOUR
        if (colour != null && !overlayColours.contains(colour) && overlayColours.size() <= 1) {
            overlayColours.add(colour); 
        }
    }

    /**
     * change the source of colour used to display in the shape based on the source type.
     * @param source The source to use to populate the shape colour.
     */
    public void changeColourSource(final String source) {
        // Depending on the source change the colour of the marker
        currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
        if (source.equals(MapViewPane.USE_COLOUR_ATTR) && !attributeColours.isEmpty()) {
            currentColour = attributeColours.size() == 1 ? Color.web(attributeColours.get(0).getHtmlColor(), MapDetails.MARKER_OPACITY) : MapDetails.MARKER_MULTI_FILL_COLOUR;
        } else if (source.equals(MapViewPane.USE_BLAZE_COL) && !blazeColours.isEmpty()) {
            currentColour = blazeColours.size() == 1 ? Color.web(blazeColours.get(0).getHtmlColor(), MapDetails.MARKER_OPACITY) : MapDetails.MARKER_MULTI_FILL_COLOUR;
        } else if (source.equals(MapViewPane.USE_OVERLAY_COL) && !overlayColours.isEmpty()) {
            currentColour = overlayColours.size() == 1 ? Color.web(overlayColours.get(0).getHtmlColor(), MapDetails.MARKER_OPACITY) : MapDetails.MARKER_MULTI_FILL_COLOUR;
        }
        setFill(currentColour);
    }

    /**
     * Return the currently used colour.
     * @return The currently used colour.
     */
    public Color getCurrentColour() {
        return currentColour;
    }

    /**
     * Add another label to the set allocated to the shape. If more than one distinct label is allocated then the label
     * MULTIPLE_VALUES_LABEL will be used to represent the label.
     * @param label The label to allocate to the shape.
     */
    public void setLabelAttr(final String label) {
        if (label != null && !labels.contains(label) && labels.size() <= 1) {
            labels.add(label);   
        }
    }

    /**
     * Add another identifier to the set allocated to the shape. If more than one distinct label is allocated then the
     * label MULTIPLE_VALUES_LABEL will be used to represent the label.
     * @param identifier The identifier to allocate to the shape.
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

    /**
     * Set the centre X position of the shape.
     * @param centerX  The centre X position of the shape.
     */
    public void setCenterX(final double centerX) {
        this.centerX = centerX;
    }

    /**
     * Set the centre Y position of the shape.
     * @param centerY  The centre Y position of the shape.
     */
    public void setCenterY(final double centerY) {
        this.centerY = centerY;
    }
    
    /**
     * Return the centre X position of the shape.
     * @return The centre X position of the shape.
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * Return the centre Y position of the shape.
     * @return The centre Y position of the shape. 
     */
    public double getCenterY() {
        return centerY;
    }
}
