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
package au.gov.asd.tac.constellation.views.mapview.utilities;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview.markers.ConstellationAbstractMarker;
import de.fhpotsdam.unfolding.geo.Location;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for markers in the Map View.
 *
 * @author cygnus_x-1
 */
public class MarkerUtilities {

    public static final int DEFAULT_SIZE = 0;
    public static final int DEFAULT_COLOR = color(127, 63, 127, 255);
    public static final int DEFAULT_CLUSTER_COLOR = color(127, 63, 0, 255);
    public static final int DEFAULT_CUSTOM_COLOR = color(127, 255, 127, 0);
    public static final int DEFAULT_HIGHLIGHT_COLOR = color(127, 255, 255, 0);
    public static final int DEFAULT_SELECT_COLOR = color(127, 255, 0, 0);
    public static final int DEFAULT_BOX_COLOR = color(127, 255, 255, 255);
    public static final int DEFAULT_STROKE_COLOR = color(127, 0, 0, 0);
    public static final int DEFAULT_STROKE_WEIGHT = 1;

    private static final int B_MASK = 255;
    private static final int G_MASK = 255 << 8;
    private static final int R_MASK = 255 << 16;
    private static final int A_MASK = 255 << 24;

    private MarkerUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Generate an integer representing a color for use in Processing from ARGB
     * values.
     *
     * @param alpha an integer between 0 and 255 representing the alpha
     * component of the color.
     * @param red an integer between 0 and 255 representing the red component of
     * the color.
     * @param green an integer between 0 and 255 representing the green
     * component of the color.
     * @param blue an integer between 0 and 255 representing the blue component
     * of the color.
     * @return an integer representing a color for use in Processing.
     */
    public static int color(final int alpha, final int red, final int green, final int blue) {
        return (Math.min(alpha, 127) << 24) + (red << 16) + (green << 8) + (blue);
    }

    /**
     * Generate an integer representing a color for use in Processing from a
     * (@link ColorValue} object.
     *
     * @param colorValue a {@link ConstellationColor} object
     * @return an integer representing a color
     */
    public static final int color(final ConstellationColor colorValue) {
        final int a = (int) (colorValue.getAlpha() * 255F);
        final int r = (int) (colorValue.getRed() * 255F);
        final int g = (int) (colorValue.getGreen() * 255F);
        final int b = (int) (colorValue.getBlue() * 255F);
        return color(a, r, g, b);
    }

    /**
     * Generate an integer representing a color for use in Processing from a
     * HTML string. The provided string should include the leading '#' and
     * optionally an alpha value.
     *
     * @param html a HTML color string
     * @return an integer representing a color
     */
    public static int color(final String html) {
        if ((html.startsWith("#") && html.length() == 7)
                || (html.startsWith("#") && html.length() == 9)) {
            final int a = html.length() == 7 ? 127
                    : Integer.parseInt(html.substring(1, 3), 16);
            final String rgbHtml = html.length() == 7 ? html.substring(1)
                    : html.substring(3);
            final int r = Integer.parseInt(rgbHtml.substring(0, 2), 16);
            final int g = Integer.parseInt(rgbHtml.substring(2, 4), 16);
            final int b = Integer.parseInt(rgbHtml.substring(4, 6), 16);
            return color(a, r, g, b);
        } else {
            throw new IllegalArgumentException("The string provided is not a valid HTML color (Expected #RRGGBB or #aaRRGGBB)");
        }

    }

    /**
     * Generate ARGB values from a Processing color.
     *
     * @param color an integer representing a color
     * @return an array of integers storing values for the alpha, red, green and
     * blue components of the color
     */
    public static int[] argb(final int color) {
        int[] argb = new int[4];
        argb[0] = (color & A_MASK) >> 24;
        argb[1] = (color & R_MASK) >> 16;
        argb[2] = (color & G_MASK) >> 8;
        argb[3] = (color & B_MASK);
        return argb;
    }

    /**
     * Generate a {@link ConstellationColor} object from a Processing color.
     *
     * @param color an integer representing a color
     * @return a {@link ConstellationColor} object
     */
    public static final ConstellationColor value(final int color) {
        final int[] argb = argb(color);
        return ConstellationColor.getColorValue(argb[1] / 255F,
                argb[2] / 255F, argb[3] / 255F, argb[0] / 255F);
    }

    /**
     * Generate a HTML string from a Processing color.
     *
     * @param color an integer representing a color
     * @return a HTML color string
     */
    public static String html(final int color) {
        final int[] argb = argb(color);
        return String.format("#%02x%02x%02x%02x",
                argb[0], argb[1], argb[2], argb[3]);
    }

    /**
     * Convert a color to greyscale.
     *
     * @param color an integer representing a color
     * @return an integer representing a greyscale color
     */
    public static final int greyscale(final int color) {
        final int[] argb = argb(color);
        final int intensity = (argb[1] + argb[2] + argb[3]) / 3;
        return color(argb[0], intensity, intensity, intensity);
    }

    /**
     * Generate a list of vertices approximating a circle around the given
     * centre location out to the given delta location.
     *
     * @param centre a {@link Location} object representing the centre of the
     * circle
     * @param delta a {@link Location} object representing an point on the edge
     * of the circle
     * @return a list of {@link Location} objects approximating the circle.
     */
    public static List<Location> generateCircle(final Location centre, final Location delta) {
        final List<Location> circleVertices = new ArrayList<>();

        final float radius = (float) Math.sqrt(
                Math.pow((delta.x - centre.x), 2)
                + Math.pow((delta.y - centre.y), 2));

        final int points = 60;
        final double spacing = (2 * Math.PI) / points;
        for (int i = 0; i < points + 1; i++) {
            final double angle = spacing * i;
            final double vertexX = centre.x + radius * Math.cos(angle);
            final double vertexY = centre.y + radius * Math.sin(angle);
            final Location vertexLocation = new Location((float) vertexX, (float) vertexY);
            circleVertices.add(vertexLocation);
        }

        return circleVertices;
    }

    public static List<Location> generateBoundingBox(final ConstellationAbstractMarker marker) {
        final List<Location> boundingBoxVertices = new ArrayList<>();

        final List<Location> locations = marker.getLocations();
        if (locations.size() > 1) {
            float minLatitude = Float.POSITIVE_INFINITY;
            float minLongitude = Float.POSITIVE_INFINITY;
            float maxLatitude = Float.NEGATIVE_INFINITY;
            float maxLongitude = Float.NEGATIVE_INFINITY;

            // calculate min and max latitude and longitude values
            for (final Location location : locations) {
                final float latitude = location.getLat();
                final float longitude = location.getLon();

                if (latitude < minLatitude) {
                    minLatitude = latitude;
                } else if (latitude > maxLatitude) {
                    maxLatitude = latitude;
                } else {
                    // Do nothing
                }

                if (longitude < minLongitude) {
                    minLongitude = longitude;
                } else if (longitude > maxLongitude) {
                    maxLongitude = longitude;
                } else {
                    // Do nothing
                }
            }

            // if the polygon crosses the prime meridian, reverse
            // the longitude values to minimise the polygon
            if (maxLongitude - maxLatitude > 180) {
                final float tempLongitude = minLongitude;
                minLongitude = maxLongitude;
                maxLongitude = tempLongitude;
            }

            boundingBoxVertices.add(new Location(minLatitude, minLongitude));
            boundingBoxVertices.add(new Location(minLatitude, maxLongitude));
            boundingBoxVertices.add(new Location(maxLatitude, maxLongitude));
            boundingBoxVertices.add(new Location(maxLatitude, minLongitude));
        }

        return boundingBoxVertices;
    }
}
