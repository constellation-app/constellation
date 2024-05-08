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
package au.gov.asd.tac.constellation.views.mapview.layers;

import au.gov.asd.tac.constellation.utilities.geospatial.Distance;
import au.gov.asd.tac.constellation.views.mapview.utilities.MarkerUtilities;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.MapPosition;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;


/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapLayer.class, position = 0)
public class DayNightLayer extends MapLayer {

    private static final int STROKE_COLOR = MarkerUtilities.color(255, 0, 0, 0);
    private static final int SUN_COLOR = MarkerUtilities.color(127, 255, 255, 0);
    private static final int TWIGHLIGHT_CIVIL_COLOR = MarkerUtilities.color(31, 0, 0, 0);
    private static final int TWIGHLIGHT_NAUTICAL_COLOR = MarkerUtilities.color(63, 0, 0, 0);
    private static final int TWIGHLIGHT_ASTRONOMICAL_COLOR = MarkerUtilities.color(95, 0, 0, 0);
    private static final int NIGHT_COLOR = MarkerUtilities.color(127, 0, 0, 0);
    private static final int EARTH_RADIUS_M = 6_371_008;


    @Override
    public String getName() {
        return "Day / Night";
    }

    @Override
    public boolean requiresUpdate() {
        return false;
    }

    @Override
    public PImage update() {
        final int width = renderer.width - 5;
        final int height = renderer.height - 5;
        final PGraphics dayNightImage = renderer.createGraphics(width, height, PConstants.JAVA2D);
        dayNightImage.beginDraw();

        final long currentTime = System.currentTimeMillis();
        final Location sunLocation = getSunPosition(currentTime);
        final ScreenPosition sunPosition = map.getScreenPosition(sunLocation);
        final Location leftShadowLocation = getShadowPosition(sunLocation, ShadowOrientation.LEFT);
        final Location rightShadowLocation = getShadowPosition(sunLocation, ShadowOrientation.RIGHT);

        // draw the sun
        dayNightImage.stroke(STROKE_COLOR);
        dayNightImage.fill(SUN_COLOR);
        dayNightImage.ellipse(sunPosition.x, sunPosition.y, 10, 10);

        // calculate shadow radius
        final float twighlightCivilRadiusMeters = getShadowRadiusFromAngle(0.566666);
        final float twighlightNauticalRadiusMeters = getShadowRadiusFromAngle(6.0);
        final float twighlightAstronomicalRadiusMeters = getShadowRadiusFromAngle(12.0);
        final float nightRadiusMeters = getShadowRadiusFromAngle(18.0);

        // left twilight civil
        final Location leftTwighlightCivilRadiusLocation = new Location(
                leftShadowLocation.getLat() - Distance.Haversine.kilometersToDecimalDegrees(twighlightCivilRadiusMeters / 1000),
                leftShadowLocation.getLon() - Distance.Haversine.kilometersToDecimalDegrees(twighlightCivilRadiusMeters / 1000));
        final List<Location> leftTwighlightCivilLocations = MarkerUtilities.generateCircle(leftShadowLocation, leftTwighlightCivilRadiusLocation);
        final List<MapPosition> leftTwighlightCivilPositions = leftTwighlightCivilLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(TWIGHLIGHT_CIVIL_COLOR);
        dayNightImage.beginShape();
        leftTwighlightCivilPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // left twilight nautical
        final Location leftTwighlightNauticalRadiusLocation = new Location(
                leftShadowLocation.getLat() - Distance.Haversine.kilometersToDecimalDegrees(twighlightNauticalRadiusMeters / 1000),
                leftShadowLocation.getLon() - Distance.Haversine.kilometersToDecimalDegrees(twighlightNauticalRadiusMeters / 1000));
        final List<Location> leftTwighlightNauticalLocations = MarkerUtilities.generateCircle(leftShadowLocation, leftTwighlightNauticalRadiusLocation);
        final List<MapPosition> leftTwighlightNauticalPositions = leftTwighlightNauticalLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(TWIGHLIGHT_NAUTICAL_COLOR);
        dayNightImage.beginShape();
        leftTwighlightNauticalPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // left twilight astronomical
        final Location leftTwighlightAstronomicalRadiusLocation = new Location(
                leftShadowLocation.getLat() + Distance.Haversine.kilometersToDecimalDegrees(twighlightAstronomicalRadiusMeters / 1000),
                leftShadowLocation.getLon() + Distance.Haversine.kilometersToDecimalDegrees(twighlightAstronomicalRadiusMeters / 1000));
        final List<Location> leftTwighlightAstronomicalLocations = MarkerUtilities.generateCircle(leftShadowLocation, leftTwighlightAstronomicalRadiusLocation);
        final List<MapPosition> leftTwighlightAstronomicalPositions = leftTwighlightAstronomicalLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(TWIGHLIGHT_ASTRONOMICAL_COLOR);
        dayNightImage.beginShape();
        leftTwighlightAstronomicalPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // left night
        final Location leftNightRadiusLocation = new Location(
                leftShadowLocation.getLat() + Distance.Haversine.kilometersToDecimalDegrees(nightRadiusMeters / 1000),
                leftShadowLocation.getLon() + Distance.Haversine.kilometersToDecimalDegrees(nightRadiusMeters / 1000));
        final List<Location> leftNightLocations = MarkerUtilities.generateCircle(leftShadowLocation, leftNightRadiusLocation);
        final List<MapPosition> leftNightPositions = leftNightLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(NIGHT_COLOR);
        dayNightImage.beginShape();
        leftNightPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // right twilight civil
        final Location rightTwighlightCivilRadiusLocation = new Location(
                rightShadowLocation.getLat() - Distance.Haversine.kilometersToDecimalDegrees(twighlightCivilRadiusMeters / 1000),
                rightShadowLocation.getLon() - Distance.Haversine.kilometersToDecimalDegrees(twighlightCivilRadiusMeters / 1000));
        final List<Location> rightTwighlightCivilLocations = MarkerUtilities.generateCircle(rightShadowLocation, rightTwighlightCivilRadiusLocation);
        final List<MapPosition> rightTwighlightCivilPositions = rightTwighlightCivilLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(TWIGHLIGHT_CIVIL_COLOR);
        dayNightImage.beginShape();
        rightTwighlightCivilPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // right twilight nautical
        final Location rightTwighlightNauticalRadiusLocation = new Location(
                rightShadowLocation.getLat() - Distance.Haversine.kilometersToDecimalDegrees(twighlightNauticalRadiusMeters / 1000),
                rightShadowLocation.getLon() - Distance.Haversine.kilometersToDecimalDegrees(twighlightNauticalRadiusMeters / 1000));
        final List<Location> rightTwighlightNauticalLocations = MarkerUtilities.generateCircle(rightShadowLocation, rightTwighlightNauticalRadiusLocation);
        final List<MapPosition> rightTwighlightNauticalPositions = rightTwighlightNauticalLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(TWIGHLIGHT_NAUTICAL_COLOR);
        dayNightImage.beginShape();
        rightTwighlightNauticalPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // right twilight astronomical
        final Location rightTwighlightAstronomicalRadiusLocation = new Location(
                rightShadowLocation.getLat() + Distance.Haversine.kilometersToDecimalDegrees(twighlightAstronomicalRadiusMeters / 1000),
                rightShadowLocation.getLon() + Distance.Haversine.kilometersToDecimalDegrees(twighlightAstronomicalRadiusMeters / 1000));
        final List<Location> rightTwighlightAstronomicalLocations = MarkerUtilities.generateCircle(rightShadowLocation, rightTwighlightAstronomicalRadiusLocation);
        final List<MapPosition> rightTwighlightAstronomicalPositions = rightTwighlightAstronomicalLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(TWIGHLIGHT_ASTRONOMICAL_COLOR);
        dayNightImage.beginShape();
        rightTwighlightAstronomicalPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);

        // right night
        final Location rightNightRadiusLocation = new Location(
                rightShadowLocation.getLat() + Distance.Haversine.kilometersToDecimalDegrees(nightRadiusMeters / 1000),
                rightShadowLocation.getLon() + Distance.Haversine.kilometersToDecimalDegrees(nightRadiusMeters / 1000));
        final List<Location> rightNightLocations = MarkerUtilities.generateCircle(rightShadowLocation, rightNightRadiusLocation);
        final List<MapPosition> rightNightPositions = rightNightLocations.stream()
                .map(location -> new MapPosition(map.mapDisplay.getObjectFromLocation(location)))
                .toList();
        dayNightImage.noStroke();
        dayNightImage.fill(NIGHT_COLOR);
        dayNightImage.beginShape();
        rightNightPositions.forEach(position -> dayNightImage.vertex(position.x, position.y));
        dayNightImage.endShape(PConstants.CLOSE);
        dayNightImage.endDraw();

        return dayNightImage;
    }

    private enum ShadowOrientation {
        LEFT,
        RIGHT
    }

    private float getShadowRadiusFromAngle(final double angle) {
        final double shadowRadius = EARTH_RADIUS_M * Math.PI * 0.5;
        final double twilightDistance = ((EARTH_RADIUS_M * 2 * Math.PI) / 360) * angle;
        return (float) (shadowRadius - twilightDistance);
    }

    final Location getShadowPosition(final Location sunPosition, final ShadowOrientation orientation) {
        if (sunPosition == null) {
            return null;
        }

        return new Location(-sunPosition.getLat(), orientation == ShadowOrientation.LEFT
                ? sunPosition.getLon() - 180 : sunPosition.getLon() + 180);
    }

    private Location getSunPosition(final long currentTime) {
        final double rad = 0.017453292519943295;
        final double jc = (getJDay(currentTime) - 2_451_545) / 36525;
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

    private double getJDay(final long currentTime) {
        return (currentTime / 86_400_000.0) + 2_440_587.5;
    }
}
