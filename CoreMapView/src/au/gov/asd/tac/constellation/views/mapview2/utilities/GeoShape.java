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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.mapview2.MapDetails;
import au.gov.asd.tac.constellation.views.mapview2.MapViewPane;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 *
 * @author altair1673
 */
public class GeoShape extends Polygon {

    private final List<Color> attributeFillColours = new ArrayList<>();
    private final List<String> blazeColours = new ArrayList<>();
    private final List<String> overlayColours = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();
    private Color currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;

    private double centerX;
    private double centerY;

    public GeoShape() {

    }

    public Color getCurrentColour() {
        return currentColour;
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

    public void setAttributeColour(final ConstellationColor attributeColour) {
        if (attributeFillColours.size() > 1) {
            return;
        }
        
        attributeFillColours.add(Color.web(attributeColour.getHtmlColor(), MapDetails.MARKER_OPACITY));
    }

    public void changeColour(final String option) {
        // Depending on the option change the colour of the marker
        if (option.equals(MapViewPane.DEFAULT_COLOURS)) {
            currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
        } else if (option.equals(MapViewPane.USE_COLOUR_ATTR)) {
            currentColour = (attributeFillColours.size() > 1) ? MapDetails.MARKER_MULTI_FILL_COLOUR : attributeFillColours.get(0);
        } else if (option.equals(MapViewPane.USE_BLAZE_COL)) {
            if (!blazeColours.isEmpty()) {
                final ConstellationColor colour = ConstellationColor.getColorValue(blazeColours.get(0));
                if (blazeColours.size() == 1) {
                    currentColour = Color.web(colour.getHtmlColor(), MapDetails.MARKER_OPACITY);
                } else {
                    currentColour = MapDetails.MARKER_MULTI_FILL_COLOUR;
                }
            } else {
                currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
            }
        } else if (option.equals(MapViewPane.USE_OVERLAY_COL)) {
            if (!overlayColours.isEmpty()) {
                final ConstellationColor colour = ConstellationColor.getColorValue(overlayColours.get(0));
                if (overlayColours.size() == 1) {
                    currentColour = Color.web(colour.getHtmlColor(), MapDetails.MARKER_OPACITY);
                } else {
                    currentColour = MapDetails.MARKER_MULTI_FILL_COLOUR;
                }
            } else {
                currentColour = MapDetails.MARKER_DEFAULT_FILL_COLOUR;
            }
        }
        setFill(currentColour);
    }

    public void setLabelAttr(final String labelAttribute) {

        if (labels.size() > 1) {
            return;
        }

        labels.add(labelAttribute);
    }

    public void setIdentAttr(final String identAttribute) {

        if (identifiers.size() > 1) {
            return;
        }

        identifiers.add(identAttribute);
    }

    public String getLabelAttr() {

        if (labels.size() > 1) {
            return "<Multiple Values>";
        }

        return labels.get(0);
    }

    public String getIdentAttr() {

        if (identifiers.size() > 1) {
            return "<Multiple Values>";
        }

        return identifiers.get(0);
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(final double centerX) {
        this.centerX = centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(final double centerY) {
        this.centerY = centerY;
    }

}
