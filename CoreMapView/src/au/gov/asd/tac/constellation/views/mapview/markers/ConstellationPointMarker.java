/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview.markers;

import au.gov.asd.tac.constellation.utilities.file.ConstellationInstalledFileLocator;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapPosition;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.openide.util.Exceptions;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * A marker with point geometry.
 *
 * @author cygnus_x-1
 */
public class ConstellationPointMarker extends ConstellationAbstractMarker {

    // image file path
    private static final String TEMPLATE_IMAGE_PATH = "marker-template-icon.png";

    // image data
    private PImage TEMPLATE_IMAGE;

    // point relative to image
    private float POINT_X_OFFSET;
    private float POINT_Y_OFFSET;

    public ConstellationPointMarker() {
        super();
        createImages();
    }

    public ConstellationPointMarker(final Location location) {
        super(location);
        createImages();
    }

    public ConstellationPointMarker(final List<Location> locations) {
        super(locations);
        assert this.locations.size() == 1;
        createImages();
    }

    private void createImages() {
        try {
            final File templateImageFile = ConstellationInstalledFileLocator.locate(
                    "modules/ext/data/" + TEMPLATE_IMAGE_PATH,
                    "au.gov.asd.tac.constellation.views.mapview",
                    false, ConstellationPointMarker.class.getProtectionDomain());
            final BufferedImage templateImage = ImageIO.read(templateImageFile);
            TEMPLATE_IMAGE = new PImage(templateImage.getWidth(), templateImage.getHeight(), PConstants.ARGB);
            TEMPLATE_IMAGE.loadPixels();
            templateImage.getRGB(0, 0, TEMPLATE_IMAGE.width, TEMPLATE_IMAGE.height, TEMPLATE_IMAGE.pixels, 0, TEMPLATE_IMAGE.width);
            TEMPLATE_IMAGE.updatePixels();

            POINT_X_OFFSET = TEMPLATE_IMAGE.width / 2;
            POINT_Y_OFFSET = TEMPLATE_IMAGE.height;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void addLocation(Location location) {
        throw new UnsupportedOperationException("A point marker can only have one location.");
    }

    @Override
    public void addLocation(float latitude, float longitude) {
        throw new UnsupportedOperationException("A point marker can only have one location.");
    }

    @Override
    public void removeLocation(Location location) {
        throw new UnsupportedOperationException("A point marker must have exactly one location.");
    }

    @Override
    public void removeLocation(float latitude, float longitude) {
        throw new UnsupportedOperationException("A point marker must have exactly one location.");
    }

    @Override
    public void setLocations(final List<Location> locations) {
        assert locations.size() == 1 : "A point marker can only have one location.";
        super.setLocations(locations);
    }

    @Override
    public void addLocations(List<Location> locations) {
        throw new UnsupportedOperationException("A point marker can only have one location.");
    }

    @Override
    public void removeLocations(List<Location> locations) {
        throw new UnsupportedOperationException("A point marker must have exactly one location.");
    }

    @Override
    public boolean draw(final PGraphics graphics, final List<MapPosition> positions, final UnfoldingMap map) {
        if (positions.isEmpty() || isHidden()) {
            return false;
        }

        final float x = positions.get(0).x;
        final float y = positions.get(0).y;

        graphics.pushStyle();

        if (size > MarkerUtilities.DEFAULT_SIZE) {
            graphics.strokeWeight(strokeWeight);
            graphics.stroke(strokeColor);
            graphics.fill(getFillColor());
            graphics.ellipseMode(PConstants.RADIUS);
            graphics.ellipse(x, y, size, size);
        } else {
            TEMPLATE_IMAGE.loadPixels();
            for (int i = 0; i < TEMPLATE_IMAGE.width * TEMPLATE_IMAGE.height; i++) {
                final int[] pixelArgb = MarkerUtilities.argb(TEMPLATE_IMAGE.pixels[i]);
                if (!(pixelArgb[0] == 0 || (pixelArgb[1] == 0 && pixelArgb[2] == 0 && pixelArgb[3] == 0))) {
                    TEMPLATE_IMAGE.pixels[i] = getFillColor();
                }
            }
            TEMPLATE_IMAGE.updatePixels();

            graphics.imageMode(PConstants.CORNER);
            graphics.image(TEMPLATE_IMAGE, x - POINT_X_OFFSET, y - POINT_Y_OFFSET);
        }

        graphics.popStyle();

        return true;
    }

    @Override
    public boolean isInside(final UnfoldingMap map, final float checkX, final float checkY) {
        final float x = map.mapDisplay.getScreenPosition(getLocation()).x;
        final float y = map.mapDisplay.getScreenPosition(getLocation()).y;
        return checkX > x - POINT_X_OFFSET && checkX < x - POINT_X_OFFSET + TEMPLATE_IMAGE.width
                && checkY > y - POINT_Y_OFFSET && checkY < y - POINT_Y_OFFSET + TEMPLATE_IMAGE.height;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ConstellationPointMarker other = (ConstellationPointMarker) obj;
        if (!Objects.equals(this.getId(), other.getId())) {
            return false;
        }
        if (!Objects.equals(this.getLocation(), other.getLocation())) {
            return false;
        }
        if (!Objects.equals(this.getProperties(), other.getProperties())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.getId());
        hash = 41 * hash + Objects.hashCode(this.getLocation());
        hash = 41 * hash + Objects.hashCode(this.getProperties());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("PointMarker @ %s", getLocation());
    }
}
