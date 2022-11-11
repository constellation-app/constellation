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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

/**
 *
 * @author altair1673
 */
public class DayNightLayer extends AbstractMapLayer {

    private final Group dayNightGroup;
    private static final int EARTH_RADIUS_M = 6_371_008;

    private static final Logger LOGGER = Logger.getLogger("DayNightMapLogger");

    private Location leftShadowLocation = null;
    private Location rightShadowLocation = null;

    private final Color twighlightCivilColour = new Color(0, 0, 0, 0.122);
    private final Color twighlightNauticalColour = new Color(0, 0, 0, 0.247);
    private final Color twighlightAstronomicalColour = new Color(0, 0, 0, 0.373);
    private final Color nightColour = new Color(0, 0, 0, 0.5);

    public DayNightLayer(MapView parent, int id) {
        super(parent, id);

        dayNightGroup = new Group();


    }

    @Override
    public void setUp() {
        Location sunLocation = getSunLocation(System.currentTimeMillis());
        Circle sun = getSun(sunLocation);

        leftShadowLocation = getShadowPosition(sunLocation, true);
        rightShadowLocation = getShadowPosition(sunLocation, false);

        dayNightGroup.getChildren().add(sun);

        // calculate shadow radius
        final float twighlightCivilRadiusMeters = getShadowRadiusFromAngle(0.566666);
        final float twighlightNauticalRadiusMeters = getShadowRadiusFromAngle(6.0);
        final float twighlightAstronomicalRadiusMeters = getShadowRadiusFromAngle(12.0);
        final float nightRadiusMeters = getShadowRadiusFromAngle(18.0);

        createShadow(twighlightCivilRadiusMeters, leftShadowLocation, twighlightCivilColour);
        createShadow(twighlightNauticalRadiusMeters, leftShadowLocation, twighlightNauticalColour);
        createShadow(twighlightAstronomicalRadiusMeters, leftShadowLocation, twighlightAstronomicalColour);
        createShadow(nightRadiusMeters, leftShadowLocation, nightColour);

        createShadow(twighlightCivilRadiusMeters, rightShadowLocation, twighlightCivilColour);
        createShadow(twighlightNauticalRadiusMeters, rightShadowLocation, twighlightNauticalColour);
        createShadow(twighlightAstronomicalRadiusMeters, rightShadowLocation, twighlightAstronomicalColour);
        createShadow(nightRadiusMeters, rightShadowLocation, nightColour);

    }

    private void createShadow(float shadowRadius, Location shadowLocation, Color colour) {
        final Location shadowCoordinates = new Location(
                shadowLocation.lat - Distance.Haversine.kilometersToDecimalDegrees(shadowRadius / 1000),
                shadowLocation.lon - Distance.Haversine.kilometersToDecimalDegrees(shadowRadius / 1000));
        final List<Location> shadowLocations = generateCircle(shadowLocation, shadowCoordinates);
        projectShadowCoordinates(shadowLocations);
        String shadowPath = generatePath(shadowLocations);
        addShadowToGroup(shadowPath, colour);
    }

    private void addShadowToGroup(String shadowPath, Color colour) {
        SVGPath shadowMarker = new SVGPath();
        shadowMarker.setContent(shadowPath);
        shadowMarker.setFill(colour);
        //shadowMarker.setStroke(Color.BLACK);
        //shadowMarker.setOpacity(0.5);
        shadowMarker.setStrokeWidth(0);

        dayNightGroup.getChildren().add(shadowMarker);
    }

    private void projectShadowCoordinates(List<Location> locations) {
        locations.forEach(location -> {

            location.x = super.longToX(location.x, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
            location.y = super.latToY(location.y, 1010.33, 1224) - 149;


        });

    }

    private String generatePath(List<Location> locations) {
        String path = "";
        boolean first = true;
        for (int i = 0; i < locations.size(); ++i) {
            if (Double.isNaN(locations.get(i).y) || Double.isNaN(locations.get(i).x)) {
                continue;
            }

            if (first) {
                path = "M" + locations.get(i).x + "," + locations.get(i).y;

                first = false;
            } else {

                path += "L" + locations.get(i).x + "," + locations.get(i).y;

            }

        }

        return path;
    }

    private Circle getSun(Location sunLocation) {
        Circle sun = new Circle();

        sun.setRadius(5);

        double sunX = super.longToX(sunLocation.lon, MapView.minLong, 1010.33, MapView.maxLong - MapView.minLong);
        double sunY = super.latToY(sunLocation.lat, 1010.33, 1224) - 149;

        sun.setTranslateX(sunX);
        sun.setTranslateY(sunY);

        sun.setFill(Color.YELLOW);
        sun.setOpacity(0.75);


        return sun;
    }

    @Override
    public Group getLayer() {
        return dayNightGroup;
    }

    final Location getShadowPosition(final Location sunPosition, final boolean leftOrientation) {
        if (sunPosition == null) {
            return null;
        }

        return new Location(-sunPosition.lat, leftOrientation
                ? sunPosition.lon - 180 : sunPosition.lon + 180);
    }

    private Location getSunLocation(long currentTime) {
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

    private double getDay(final long currentTime) {
        return (currentTime / 86_400_000.0) + 2_440_587.5;
    }

    private float getShadowRadiusFromAngle(final double angle) {
        final double shadowRadius = EARTH_RADIUS_M * Math.PI * 0.5;
        final double twilightDistance = ((EARTH_RADIUS_M * 2 * Math.PI) / 360) * angle;
        return (float) (shadowRadius - twilightDistance);
    }

    public List<Location> generateCircle(final Location centre, final Location delta) {
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
            final Location vertexLocation = new Location(vertexY, vertexX);
            //vertexLocation.x = vertexX;
            //vertexLocation.y = vertexY;
            circleVertices.add(vertexLocation);
        }

        return circleVertices;
    }


}
