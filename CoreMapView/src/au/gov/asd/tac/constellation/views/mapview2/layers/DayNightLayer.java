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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.utilities.geospatial.Distance;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.utilities.MarkerUtilities;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

/**
 * Shows which parts of the planet are in night
 *
 * @author altair1673
 */
public class DayNightLayer extends AbstractMapLayer {

    // Layer that holds the graphical shadow elements
    private final Group dayNightGroup;

    private static final int EARTH_RADIUS_M = 6_371_008;

    private static final double SHADOW_LOCATION_Y_OFFSET = 149;
    private static final double SUN_RADIUS = 5;


    // Colours of the different shadows
    private final Color twighlightCivilColour = new Color(0, 0, 0, 0.122);
    private final Color twighlightNauticalColour = new Color(0, 0, 0, 0.247);
    private final Color twighlightAstronomicalColour = new Color(0, 0, 0, 0.373);
    private final Color nightColour = new Color(0, 0, 0, 0.5);

    public DayNightLayer(final MapView parent, final int id) {
        super(parent, id);

        dayNightGroup = new Group();


    }

    /**
     * Calculates the shadows, sun location and colours them in
     */
    @Override
    public void setUp() {
        // Get the sun location and create a circle to represent the sin
        Location sunLocation = getSunLocation(System.currentTimeMillis());
        Circle sun = getSun(sunLocation);


        // Calculate left and right shadow locations
        Location leftShadowLocation = getShadowPosition(sunLocation, true);
        Location rightShadowLocation = getShadowPosition(sunLocation, false);

        // Add sun graphic to group
        dayNightGroup.getChildren().add(sun);

        // calculate shadow radius
        final float twighlightCivilRadiusMeters = getShadowRadiusFromAngle(0.566666);
        final float twighlightNauticalRadiusMeters = getShadowRadiusFromAngle(6.0);
        final float twighlightAstronomicalRadiusMeters = getShadowRadiusFromAngle(12.0);
        final float nightRadiusMeters = getShadowRadiusFromAngle(18.0);

        // Create left shadows
        createShadow(twighlightCivilRadiusMeters, leftShadowLocation, twighlightCivilColour);
        createShadow(twighlightNauticalRadiusMeters, leftShadowLocation, twighlightNauticalColour);
        createShadow(twighlightAstronomicalRadiusMeters, leftShadowLocation, twighlightAstronomicalColour);
        createShadow(nightRadiusMeters, leftShadowLocation, nightColour);

        // Create right shadows
        createShadow(twighlightCivilRadiusMeters, rightShadowLocation, twighlightCivilColour);
        createShadow(twighlightNauticalRadiusMeters, rightShadowLocation, twighlightNauticalColour);
        createShadow(twighlightAstronomicalRadiusMeters, rightShadowLocation, twighlightAstronomicalColour);
        createShadow(nightRadiusMeters, rightShadowLocation, nightColour);

    }

    /**
     * Creates at a specific location
     *
     * @param shadowRadius - radius of shadow
     * @param shadowLocation - geo coordinate of shadow
     * @param colour
     */
    private void createShadow(final float shadowRadius, final Location shadowLocation, final Color colour) {
        // Find coordinate of shadow in Haversine format
        final Location shadowCoordinates = new Location(
                shadowLocation.getLat() - Distance.Haversine.kilometersToDecimalDegrees(shadowRadius / 1000),
                shadowLocation.getLon() - Distance.Haversine.kilometersToDecimalDegrees(shadowRadius / 1000));

        // Get all the locations of the edges of the shadow
        final List<Location> shadowLocations = generateCircle(shadowLocation, shadowCoordinates);

        // Project the circular show onto the world
        projectShadowCoordinates(shadowLocations);

        // Get the actual shadow svg path
        String shadowPath = generatePath(shadowLocations);

        // Add that shadow path to the graphics group
        addShadowToGroup(shadowPath, colour);
    }

    /**
     * Adds shadow graphic to group
     *
     * @param shadowPath - the raw string svg path
     * @param colour
     */
    private void addShadowToGroup(final String shadowPath, final Color colour) {
        // Create the shadow graphic and colour it
        SVGPath shadowMarker = new SVGPath();
        shadowMarker.setContent(shadowPath);
        shadowMarker.setFill(colour);

        // Get rid of borders and allow events to pass through the shadow
        shadowMarker.setStrokeWidth(0);
        shadowMarker.setMouseTransparent(true);

        dayNightGroup.getChildren().add(shadowMarker);
    }

    /**
     * Convert lat and long coordinates of shadow edges to x and y coords
     *
     * @param locations - array of Location objects that only have their
     * lattitude and longitude fields calculated
     */
    private void projectShadowCoordinates(final List<Location> locations) {
        // For every location calculate its x and y coordinate
        locations.forEach(location -> {

            location.setX(MarkerUtilities.longToX(location.getX(), MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG));
            location.setY(MarkerUtilities.latToY(location.getY(), MapView.MAP_WIDTH, MapView.MAP_HEIGHT) - SHADOW_LOCATION_Y_OFFSET);


        });

    }

    /**
     * Generate the actual path from the shadow locations
     *
     * @param locations
     * @return the string that containing the raw svg path
     */
    private String generatePath(final List<Location> locations) {
        String path = "";
        boolean first = true;

        // Loop through all the locations
        for (int i = 0; i < locations.size(); ++i) {
            if (Double.isNaN(locations.get(i).getY()) || Double.isNaN(locations.get(i).getX())) {
                continue;
            }

            // If is the first location then append a move command if not append a line command
            if (first) {
                path = "M" + locations.get(i).getX() + "," + locations.get(i).getY();

                first = false;
            } else {

                path += "L" + locations.get(i).getX() + "," + locations.get(i).getY();

            }

        }

        return path;
    }

    /**
     * Create a sun at the specified location
     *
     * @param sunLocation
     * @return a circle vector graphic at the current position
     */
    private Circle getSun(final Location sunLocation) {
        Circle sun = new Circle();

        sun.setRadius(SUN_RADIUS);

        // Calculate x and y from lat and lon
        double sunX = MarkerUtilities.longToX(sunLocation.getLon(), MapView.MIN_LONG, MapView.MAP_WIDTH, MapView.MAX_LONG - MapView.MIN_LONG);
        double sunY = MarkerUtilities.latToY(sunLocation.getLat(), MapView.MAP_WIDTH, MapView.MAP_HEIGHT) - SHADOW_LOCATION_Y_OFFSET;

        sun.setTranslateX(sunX);
        sun.setTranslateY(sunY);

        // Colour in the sun
        sun.setFill(Color.YELLOW);
        sun.setOpacity(0.75);
        sun.setMouseTransparent(true);

        return sun;
    }

    @Override
    public Group getLayer() {
        return dayNightGroup;
    }

    /**
     * Get the shadow position based on the sun position
     *
     * @param sunPosition
     * @param leftOrientation
     * @return returns Location object containing shadow position
     */
    private Location getShadowPosition(final Location sunPosition, final boolean leftOrientation) {
        if (sunPosition == null) {
            return null;
        }

        return new Location(-sunPosition.getLat(), leftOrientation
                ? sunPosition.getLon() - 180 : sunPosition.getLon() + 180);
    }

    /**
     * Calculate location of sun based on the current time
     *
     * @param currentTime
     * @return location of the sun
     */
    private Location getSunLocation(final long currentTime) {
        final double rad = 0.017453292519943295;
        final double jc = (getDay(currentTime) - 2_451_545) / 36525;
        final double meanLongSun = (280.46646 + jc * (36000.76983 + jc * 0.0003032)) % 360;
        final double meanAnomSun = 357.52911 + jc * (35999.05029 - 0.0001537 * jc);
        final double sunEq = Math.sin(rad * meanAnomSun) * (1.914602 - jc * (0.004817 + 0.000014 * jc))
                + Math.sin(rad * 2 * meanAnomSun) * (0.019993 - 0.000101 * jc)
                + Math.sin(rad * 3 * meanAnomSun) * 0.000289;
        final double sunTrueLong = meanLongSun + sunEq;
        final double sunAppLong = sunTrueLong - 0.00569 - 0.00478 * Math.sin(rad * 125.04 - 1934.136 * jc);
        final double meanObliqueEcliptic = 23 + (26 + (21.448 - jc * (46.815 + jc * (0.00059 - jc * 0.001813))) / 60) / 60;
        final double obliqueCorr = meanObliqueEcliptic + 0.00256 * Math.cos(rad * 125.04 - 1934.136 * jc);

        final double lat = Math.asin(Math.sin(rad * obliqueCorr) * Math.sin(rad * sunAppLong)) / rad;

        final double eccent = 0.016708634 - jc * (0.000042037 + 0.0000001267 * jc);
        final double y = Math.tan(rad * (obliqueCorr / 2)) * Math.tan(rad * (obliqueCorr / 2));
        final double rqOfTime = 4 * ((y * Math.sin(2 * rad * meanLongSun)
                - 2 * eccent * Math.sin(rad * meanAnomSun)
                + 4 * eccent * y * Math.sin(rad * meanAnomSun) * Math.cos(2 * rad * meanLongSun)
                - 0.5 * y * y * Math.sin(4 * rad * meanLongSun)
                - 1.25 * eccent * eccent * Math.sin(2 * rad * meanAnomSun)) / rad);
        final double trueSolarTimeInDeg = ((currentTime + rqOfTime * 60000) % 86_400_000) / 240_000;
        final double lon = -((trueSolarTimeInDeg < 0) ? trueSolarTimeInDeg + 180 : trueSolarTimeInDeg - 180);
        return new Location(lat, lon);
    }

    /**
     * Get the current day based on the time
     *
     * @param currentTime
     * @return A double representing the day
     */
    private double getDay(final long currentTime) {
        return (currentTime / 86_400_000.0) + 2_440_587.5;
    }

    /**
     * Gets the shadow radius from angle
     *
     * @param angle
     * @return radius of shadow
     */
    private float getShadowRadiusFromAngle(final double angle) {
        final double shadowRadius = EARTH_RADIUS_M * Math.PI * 0.5;
        final double twilightDistance = ((EARTH_RADIUS_M * 2 * Math.PI) / 360) * angle;
        return (float) (shadowRadius - twilightDistance);
    }

    /**
     * Generates an array of locations representing the shadow
     *
     * @param centre - center of circle
     * @param delta - location on the edge of circle
     * @return return an array of locations
     */
    private List<Location> generateCircle(final Location centre, final Location delta) {
        final List<Location> circleVertices = new ArrayList<>();

        // Find the radius of the shadow based on center and a point on the edge
        final float radius = (float) Math.sqrt(
                Math.pow((delta.getX() - centre.getX()), 2)
                + Math.pow((delta.getY() - centre.getY()), 2));

        // Calculate the edges of the shadow
        // Ammount of points to calculate
        final int points = 60;

        // Spacing between each point
        final double spacing = (2 * Math.PI) / points;
        for (int i = 0; i < points + 1; i++) {
            final double angle = spacing * i;

            // Calculate coordinate at specific angle from the center
            final double vertexX = centre.getX() + radius * Math.cos(angle);
            final double vertexY = centre.getY() + radius * Math.sin(angle);
            final Location vertexLocation = new Location(vertexY, vertexX);
            circleVertices.add(vertexLocation);
        }

        return circleVertices;
    }


}
