/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * A marker with point geometry.
 *
 * @author cygnus_x-1
 */
public class ConstellationPointMarker extends ConstellationAbstractMarker {

    private static final Logger LOGGER = Logger.getLogger(ConstellationPointMarker.class.getName());

    // image file path
    private static final String TEMPLATE_IMAGE_PATH = "marker-template-icon.png";

    // image data
    private PImage templateImage;

    // point relative to image
    private float pointXOffset;
    private float pointYOffset;

    private static final String POINT_MARKER_ONE_LOCATION_ONLY = "A point marker can only have one location.";
    private static final String POINT_MARKER_ONE_LOCATION_MUST = "A point marker must have exactly one location.";

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
                    ConstellationPointMarker.class.getProtectionDomain());
            final BufferedImage templateBufferedImage = ImageIO.read(templateImageFile);
            this.templateImage = new PImage(templateBufferedImage.getWidth(), templateBufferedImage.getHeight(), PConstants.ARGB);
            this.templateImage.loadPixels();
            templateBufferedImage.getRGB(0, 0, this.templateImage.width, this.templateImage.height, this.templateImage.pixels, 0, this.templateImage.width);
            this.templateImage.updatePixels();

            pointXOffset = this.templateImage.width / 2F;
            pointYOffset = this.templateImage.height;
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void addLocation(Location location) {
        throw new UnsupportedOperationException(POINT_MARKER_ONE_LOCATION_ONLY);
    }

    @Override
    public void addLocation(float latitude, float longitude) {
        throw new UnsupportedOperationException(POINT_MARKER_ONE_LOCATION_ONLY);
    }

    @Override
    public void removeLocation(Location location) {
        throw new UnsupportedOperationException(POINT_MARKER_ONE_LOCATION_MUST);
    }

    @Override
    public void removeLocation(float latitude, float longitude) {
        throw new UnsupportedOperationException(POINT_MARKER_ONE_LOCATION_MUST);
    }

    @Override
    public void setLocations(final List<Location> locations) {
        if (locations.size() == 1) {
            super.setLocations(locations);
        } else {
            throw new IllegalArgumentException(POINT_MARKER_ONE_LOCATION_ONLY);
        }

    }

    @Override
    public void addLocations(List<Location> locations) {
        throw new UnsupportedOperationException(POINT_MARKER_ONE_LOCATION_ONLY);
    }

    @Override
    public void removeLocations(List<Location> locations) {
        throw new UnsupportedOperationException(POINT_MARKER_ONE_LOCATION_MUST);
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
            templateImage.loadPixels();
            for (int i = 0; i < templateImage.width * templateImage.height; i++) {
                final int[] pixelArgb = MarkerUtilities.argb(templateImage.pixels[i]);
                if (!(pixelArgb[0] == 0 || (pixelArgb[1] == 0 && pixelArgb[2] == 0 && pixelArgb[3] == 0))) {
                    templateImage.pixels[i] = getFillColor();
                }
            }
            templateImage.updatePixels();

            graphics.imageMode(PConstants.CORNER);
            graphics.image(templateImage, x - pointXOffset, y - pointYOffset);
        }

        graphics.popStyle();

        return true;
    }

    @Override
    public boolean isInside(final UnfoldingMap map, final float checkX, final float checkY) {
        final float x = map.mapDisplay.getScreenPosition(getLocation()).x;
        final float y = map.mapDisplay.getScreenPosition(getLocation()).y;
        return checkX > x - pointXOffset && checkX < x - pointXOffset + templateImage.width
                && checkY > y - pointYOffset && checkY < y - pointYOffset + templateImage.height;
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
        return Objects.equals(this.getProperties(), other.getProperties());
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
