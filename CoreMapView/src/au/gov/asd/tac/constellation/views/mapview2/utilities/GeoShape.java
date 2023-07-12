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

    private String defaultColour = "#FF0000";
    private String multiValCol = "#D3D3D3";
    private final List<String> attributeColours = new ArrayList<>();
    private final List<String> blazeColours = new ArrayList<>();
    private final List<String> overlayColours = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private final List<String> identifiers = new ArrayList<>();
    private String currentColour = defaultColour;

    private double centerX;
    private double centerY;

    public GeoShape() {

    }

    public String getCurrentColour() {
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

    public void setAttributeColour(final String attributeColour) {
        if (attributeColours.size() > 1) {
            return;
        }

        attributeColours.add(attributeColour);
    }

    public void changeColour(final String option) {
        // Depending on the option change the colour of the marker
        if (option.equals(MapViewPane.DEFAULT_COLOURS)) {
            currentColour = defaultColour;
            setFill(Color.web(currentColour));
        } else if (option.equals(MapViewPane.USE_COLOUR_ATTR)) {
            if (attributeColours.size() > 1) {
                currentColour = multiValCol;
                setFill(Color.web(currentColour));
            } else if (attributeColours.size() == 1) {
                currentColour = attributeColours.get(0);
                setFill(Color.web(currentColour));
            }

        } else if (option.equals(MapViewPane.USE_BLAZE_COL)) {
            if (!blazeColours.isEmpty()) {
                final ConstellationColor colour = ConstellationColor.getColorValue(blazeColours.get(0));
                if (blazeColours.size() == 1) {
                    currentColour = colour.getHtmlColor();
                    setFill(Color.web(currentColour));
                } else {
                    setFill(Color.web(multiValCol));
                    currentColour = multiValCol;
                }
            } else {
                setFill(Color.web(defaultColour));
            }

        } else if (option.equals(MapViewPane.USE_OVERLAY_COL)) {
            if (!overlayColours.isEmpty()) {
                final ConstellationColor colour = ConstellationColor.getColorValue(overlayColours.get(0));

                if (overlayColours.size() == 1) {
                    currentColour = colour.getHtmlColor();
                    setFill(Color.web(currentColour));
                } else {
                    setFill(Color.web(multiValCol));
                    currentColour = multiValCol;
                }

            } else {
                setFill(Color.web(defaultColour));
            }
        }

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
