/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides utilities to convert between real world latitude and longitudes and X,Y coordinates on a map.
 * The conversions all operate on a map with specific dimensions and defined top/left and bottom/right
 * latitude/longitude coordinates.
 * 
 * Points (latitudes, longitudes) that should appear on the map will be converted to X,Y coordinates between 0 and the
 * width/height of the map. Points which are not visible on the map will contain X,Y coordinates outside of the
 * width/height of the map.
 * 
 * Functions also exist to convert X,Y map coordinates back to latitudes and longitudes.
 * 
 * @author serpens24
 */
public class MapConversions {
    private static final Logger LOGGER = Logger.getLogger(MapConversions.class.getName());
    
    private static final String MAP_NOT_INITIALIZED_ERROR = "Map dimensions not initialized";
    private static final double MAX_ABSOLUTE_LATITUDE = 90.0;
    private static final double MAX_ABSOLUTE_LONGITUDE = 180.0;
    
    // bounds of map being used
    private static double mapWidth = 0.0; // Width of map being viewed. Refer to (a) on diagram below.
    private static double mapHeight = 0.0; //Height of map being viewed. Refer to (b) on diagram below.
    
    // The latitudes and longitudes bordering the visibleMap 
    private static double mapTopLat = 0.0; // Latitude (in degrees) of the top left point on map being viewed. Refer
                                           // to (c) on diagram below.
    private static double mapLeftLon = 0.0; // Longitude (in degrees) of the top left point on map being viewed. Refer
                                            // to (c) on diagram below.
    private static double mapBottomLat = 0.0; // Latitude (in degrees) of the bottom right point on map being viewed.
                                              // Refer to (d) on diagram below.
    private static double mapRightLon = 0.0; // Longitude (in degrees) of the bottom right point on map being viewed.
                                             // Refer to (d) on diagram below.
    
    // Bounds of full world map if visible map was extended to 360 degrees of longitude
    private static double fullWorldMapWidth = 0.0; // Width of the full world map if supplied map was extended to 360
                                                   // degrees of latitude. Refer to (e) on diagram below.
    private static double fullWorldMapCentre = 0.0; // Horizontal centre of the full world map from left of map. This
                                                    // is the horizontal position of (x) on the diagram below, which is
                                                    // exaclty half the value of (e).
    private static double fullWorldMapRadius = 0.0; // Radius of the full world map. As the width of the full world map
                                                    // represents 360 degrees of longitude, it also equates to the
                                                    // circumference of the earth. The radius us calculated from this
                                                    // circumference value.
    
    private static double mapLeftOffsetFromFullWorldMapCentre = 0.0; // The horizontal offset of the supplied maps top
                                                                     // left corner (c) in the diagram below, to the
                                                                     // centre of the world map (x) in the diagram
                                                                     // below in the units used to describe mapWidth and
                                                                     // mapHeight.
    private static double mapTopOffsetFromFullWorldMapCentre = 0.0; // The vertical offset of the supplied maps top left
                                                                    // corner (c) in the diagram below, to the centre
                                                                    // of the world map (x) in the diagram below in
                                                                    // the units used to describe mapWidth and mapHeight.
    private static double mapRightOffsetFromFullWorldMapCentre = 0.0; // The horizontal offset of the supplied maps
                                                                     // bottom right corner (d) in the diagram below, to
                                                                     // the centre of the world map (x) in the diagram
                                                                     // below in the units used to describe mapWidth and
                                                                     // mapHeight.
    private static double mapBottomOffsetFromFullWorldMapCentre = 0.0; // The vertical offset of the supplied maps
                                                                     // bottom right corner (d) in the diagram below, to
                                                                     // the centre of the world map (x) in the diagram
                                                                     // below in the units used to describe mapWidth and
                                                                     // mapHeight.
    
    
    private static boolean initialized = false; // Confirm that a map has been loaded to operate on.
    
    // The following diagram attempts to identify the various points and concepts stored by calculations in this class.
    // Refer to comments above for referecnes to points on the diagram.
    
    //
    //  |<--------------------------------- e ------------------------------->|
    //
    //  
    //  ^                                                                     ^
    //  |   Extrapolated World map - only longitudal range is considered)     |
    //  |                                                                     |
    //  |           |<---------------- a ---------------->|                   |
    //  |         c +-------------------------------------+  ---              |
    //  |           |                                     |   ^               |
    //  |           |  Supplied map dimensions            |   |               |                                                                   |
    //  |           |                                     |   |               |
    //  |           |                       |             |   |               |
    //  |           |                      -x-            |   b               |
    //  |           |                       |             |   |               |
    //  |           |                                     |   |               |
    //  |           |                                     |   |               |
    //  |           |                                     |   v               |
    //  |           +-------------------------------------+  ---              | 
    //  |                                                 d                   |
    //  |                                                                     |
    //  |                                                                     |
    //  |                                                                     |
    //  v                                                                     v
    //                                                                        
    
    ///
    /// Initialization
    ///
    
    /**
     * Private default constructor.
     */
    private MapConversions() {   
    }
    
    /**
     * Initialize the static class with details of the selected map. This performs all the common calculations used to
     * support various transformations between lat, long and X, Y coordinates.
     * As discussed above, a 'virtual' full world map is generated assuming a LONGITUDINAL RANGE OF -180 TO 180, ie the
     * full world.
     * Maps latitude/longitude extents are provided via explicit top left and bottom right values.
     * 
     * @param width The width of the map to calculate values for.
     * @param height The height of the map to calculate values for. This is only required to provide sanity checks.
     * @param topLat The latitude corresponding to the top left corner of the map.
     * @param leftLon The longitude corresponding to the top left corner of the map.
     * @param bottomLat The latitude corresponding to the bottom right corner of the map.
     * @param rightLon  The longitude corresponding to the bottom right corner of the map.
     */
    public static void initMapDimensions(final double width, final double height, final double topLat, final double leftLon, final double bottomLat, final double rightLon) {
        resetMapDimensions();
        
        // Validate map size
        if (width <= 0) {
            throw new IllegalArgumentException("Supplied map width is invalid");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Supplied map height is invalid");
        }
        
        // Validate latitudes are within valid range for earth and correctly oriented
        if (Math.abs(topLat) > MAX_ABSOLUTE_LATITUDE) {
            throw new IllegalArgumentException("Invalid top latitude supplied");
        }
        if (Math.abs(bottomLat) > MAX_ABSOLUTE_LATITUDE) {
            throw new IllegalArgumentException("Invalid bottom latitude supplied");
        }
        if (bottomLat >= topLat) {
            throw new IllegalArgumentException("Top left latitude must be greater than bottom right");
        }

        // Longitudes can wrap so depending on how map is oriented it may be that left Latitude is > right when
        // For isntance a map going from 150 degrees lat to -150 degrees would be 60 degrees of latitude centreed on
        // the prime meridian. So checks only confirm longitides in valid -180 -> 180 range.
        if (Math.abs(leftLon) > MAX_ABSOLUTE_LONGITUDE) {
            throw new IllegalArgumentException("Invalid left longitude supplied");
        }
        if (Math.abs(rightLon) > MAX_ABSOLUTE_LONGITUDE) {
            throw new IllegalArgumentException("Invalid right longitude supplied");
        }
        
        mapWidth = width;
        mapHeight = height;
        mapTopLat = topLat;
        mapLeftLon = leftLon;
        mapBottomLat = bottomLat;
        mapRightLon = rightLon;
        
        // Determine full world map extents, which provides us an idea of what the full world would look like if the map
        // was extended to cover the full 360 degrees of longitude.
        fullWorldMapWidth = mapWidth /((mapRightLon - mapLeftLon)/360);
        fullWorldMapCentre = fullWorldMapWidth/2;
        fullWorldMapRadius = fullWorldMapWidth/(2 * Math.PI);
        
        // Mark the class as setup as subsequent function calls rely on it
        initialized = true;
        
        // Now using the full world map (virtual) dimensions as a basis, determine where our map sits relative to it, in
        // the units used to describe mapWidth and mapHeight.
        mapLeftOffsetFromFullWorldMapCentre = fullWorldMapCentre * mapLeftLon/180;
        mapRightOffsetFromFullWorldMapCentre = fullWorldMapCentre * mapRightLon/180;
        mapTopOffsetFromFullWorldMapCentre = latToOffsetFromFullMapEquator(mapTopLat);
        mapBottomOffsetFromFullWorldMapCentre = latToOffsetFromFullMapEquator(mapBottomLat);

        // The height of the supplied map is a little redundant in our display processing as we assume it is a true
        // mercartor projection but we can check it is the expected height based on top and bottom lat/long
        final double expectedMapHeight = (mapBottomOffsetFromFullWorldMapCentre - mapTopOffsetFromFullWorldMapCentre);
        if (Math.abs(expectedMapHeight - mapHeight) > 1.0) {
            LOGGER.log(Level.INFO, "Supplied map height suggests invalid Mercartor projection. Expected={0}, supplied={1}",
                       new Object[]{expectedMapHeight, mapHeight});
            // At some point we may decide that the map is no good, or we may try to stretch our Y coordinates, however
            // to date most cases where the value doesnt match have been due to map extremes not matching the given
            // latitude paramters.
        }
        
        // Provide a summary of the selected map and extrapolated full world map details
        LOGGER.log(Level.INFO,"Supplied map dimensions: (width * height) = ({0} * {1})", new Object[]{mapWidth, mapHeight});
        LOGGER.log(Level.INFO, "Supplied map corner lat/longs: top left (lat, long)= ({0}, {1}), bottom right (lat, long)= ({2}, {3})",
                   new Object[]{mapTopLat, mapLeftLon, mapBottomLat, mapRightLon});
        LOGGER.log(Level.INFO, "Full world Dimensions: width={0}, centre={1}, radius={2}",
                   new Object[]{fullWorldMapWidth, fullWorldMapCentre, fullWorldMapRadius});
        LOGGER.log(Level.INFO, "Supplied map corners offsets from full world centre: top left (X , Y)= ({0}, {1}), bottom right (X , Y)= ({2}, {3})",
                   new Object[]{mapLeftOffsetFromFullWorldMapCentre, mapTopOffsetFromFullWorldMapCentre,
                                mapRightOffsetFromFullWorldMapCentre, mapBottomOffsetFromFullWorldMapCentre});
    }
    
    /**
     * Initialize the static class with details of the selected map. This performs all the common calculations used to
     * support various transformations between lat, long and X, Y coordinates.
     * As discussed above, a 'virtual' full world map is generated assuming a LONGITUDINAL RANGE OF -180 TO 180, ie the
     * full world.
     * Maps latitude/longitude extents are provided via top left and bottom right Location values which contain both
     * latitudes and longitudes.
     * 
     * @param width The width of the map to calculate values for.
     * @param height The height of the map to calculate values for. This is only required to provide sanity checks.
     * @param topLeft The location corresponding to the top left corner of the map.
     * @param bottomRight The location corresponding to the bottom right corner of the map.
     */
    public static void initMapDimensions(final double width, final double height, final Location topLeft, final Location bottomRight) {
        initMapDimensions(width, height, topLeft.getLat(), topLeft.getLon(), bottomRight.getLat(), bottomRight.getLon());
    }
    
    /**
     * Reset all values and clear stored map dimensions.
     */
    public static void resetMapDimensions() {
        mapWidth = mapHeight = 0.0;
        mapTopLat = mapLeftLon = mapBottomLat = mapRightLon = 0.0;
        fullWorldMapWidth = fullWorldMapCentre = fullWorldMapRadius = 0.0;
        mapLeftOffsetFromFullWorldMapCentre = mapRightOffsetFromFullWorldMapCentre = 0.0;
        mapTopOffsetFromFullWorldMapCentre = mapBottomOffsetFromFullWorldMapCentre = 0.0;
        initialized = false;
    }
    
    ///
    /// Get parameters
    ///

    /**
     * Return whether a map has been initialized.
     * 
     * @return Boolean indicating whether a map has been initialized.
     */
    public static boolean getIsInitialized() {
        return initialized;
    }
    
    /**
     * Get the width of the map if it covered full world.
     * 
     * @return The width of the map if it covered full world.
     */
    public static double getFullWorldWidth() {
        return fullWorldMapWidth;
    }
    
    /**
     * Get the radius of the map if it covered full world.
     * 
     * @return The radius of the map if it covered full world.
     */
    public static double getFullWorldRadius() {
        return fullWorldMapRadius;
    }
    
    /**
     * Get the top latitude of the map.
     * 
     * @return The top latitude of the map.
     */
    public static double getTopLat() {
        return mapTopLat;
    }
    
    /**
     * Get the bottom latitude of the map.
     * 
     * @return The bottom latitude of the map.
     */
    public static double getBottomLat() {
        return mapBottomLat;
    }
    
    /**
     * Get the left longitude of the map.
     * 
     * @return The left longitude of the map.
     */
    public static double getLeftLon() {
        return mapLeftLon;
    }
    
    /**
     * Get the right longitude of the map.
     * 
     * @return The right longitude of the map.
     */
    public static double getRightLon() {
        return mapRightLon;
    }
    
    /**
     * Get the offset from the virtual world map centre of the top of the map (in map units).
     * @return The offset from the virtual world map centre of the top of the map (in map units).
     */
    public static double getMapTopOffsetFromWorldCentre() {
        return mapTopOffsetFromFullWorldMapCentre;
    }
    
    /**
     * Get the offset from the virtual world map centre of the bottom of the map (in map units).
     * @return The offset from the virtual world map centre of the bottom of the map (in map units).
     */
    public static double getMapBottomOffsetFromWorldCentre() {
        return mapBottomOffsetFromFullWorldMapCentre;
    }
    
    /**
     * Get the offset from the virtual world map centre of the left of the map (in map units).
     * @return The offset from the virtual world map centre of the left of the map (in map units).
     */
    public static double getMapLeftOffsetFromWorldCentre() {
        return mapLeftOffsetFromFullWorldMapCentre;
    }
    
    /**
     * Get the offset from the virtual world map centre of the right of the map (in map units).
     * @return The offset from the virtual world map centre of the right of the map (in map units).
     */
    public static double getMapRightOffsetFromWorldCentre() {
        return mapRightOffsetFromFullWorldMapCentre;
    }
    
    ///
    /// Conversion helper functions
    ///
    
    /**
     * Helper function to convert degrees to radians.
     * @param degrees The degrees value to convert to radians.
     * @return Radians equivalent of the supplied degrees value.
     */
    private static double degreesToRadians(final double degrees) {
        return (degrees * (Math.PI / 180));
    }
    
    /**
     * Helper function to convert radians to degrees.
     * @param degrees The radians value to convert to degrees.
     * @return Degrees equivalent of the supplied radians value.
     */
    private static double radianstoDegrees(final double radians) {
        return (radians * (180 / Math.PI));
    }

    /**
     * Take the given latitude value (in degrees) and determine the offset (in map dimension units) of its position on
     * the supplied map from the equator.This is used to help position the selected map extents relative to the centre
     * of the virtual full world map. This is useful as all conversions calculations initially focus on the virtual
     * world map and are then translated to the supplied map extents.
     * 
     * @param latitudeDegrees The latitude (in degrees) to determine the offset for.
     * @return The number of map units that the provided latitude is above or below the equator if drawn on the virtual
     * world map.
     */
    private static double latToOffsetFromFullMapEquator(final double latDegrees) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return 0.0;
        }

        // Need to inverse the sign as +value latitudes are above equator  (and hence negatively offset)
        return (-fullWorldMapRadius * Math.log(Math.tan((Math.PI/4) + (degreesToRadians(latDegrees)/2))));
    }
    
    private static double offsetFromFullMapEquatortoLat(final double offset) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return 0.0;
        }
        
        return radianstoDegrees((Math.atan(Math.exp(-offset/fullWorldMapRadius)) - Math.PI/4) * 2);
    }
    
    
    
    /**
     * Return the X offset from position 0,0 on the map of the supplied longitude. A value less than 0 indicates that
     * the longitude is to the left of the map extents. A value > mapWidth indicates that the longitude is to the right
     * of the map extents. The function isXInMap(double) will perform these extent checks and return a boolean
     * indication that the longitude falls within map bounds.
     * @param longDegrees The longitude to convert.
     * @return X offset from left of map.
     */
    public static double lonToMapX(final double lonDegrees) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return 0.0;
        }
        
        return (lonDegrees - mapLeftLon) * (mapWidth / (mapRightLon - mapLeftLon));
    }
    
    /**
     * Return the Y offset from position 0,0 on the map of the supplied latitude. A value less than 0 indicates that
     * the latitude is above the map extents. A value > mapHeight indicates that the latitude is below the map extents.
     * The function isYInMap(double) will perform these extent checks and return a boolean indication that the latitude
     * falls within map bounds.
     * @param latDegrees The latitude to convert.
     * @return Y offset from top of map.
     */
    public static double latToMapY(final double latDegrees) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return 0.0;
        }
        
        final double yOffsetFromEquator = latToOffsetFromFullMapEquator(latDegrees);
        
        final double degrees = offsetFromFullMapEquatortoLat(yOffsetFromEquator);
        
        return (yOffsetFromEquator - mapTopOffsetFromFullWorldMapCentre);
    }
    
    public static double mapXToLon(final double x) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return 0.0;
        }
        
        return (x /(mapWidth / (mapRightLon - mapLeftLon))) + mapLeftLon;
    } 
    
    public static double mapYToLat(final double y) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return 0.0;
        }
        
        // first convert the y value to an offset from centre of full world map
        final double offset = mapTopOffsetFromFullWorldMapCentre + y;
        return offsetFromFullMapEquatortoLat(offset);
    } 
    
    /**
     * Checks if the supplied X coordinate is within the bounds of the selected map.
     * 
     * @param x The x point to check against the map limits.
     * @return true if the supplied X coordinate is within the range of 0 -> mapHeight,meaning it is able to be
     * displayed on the currently selected map.
     */
    public static boolean isXInMap(final double x) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return false;
        }
        return ((x >= 0) && (x <= mapWidth));
    }
    
    /**
     * Checks if the supplied Y coordinate is within the bounds of the selected map.
     * 
     * @param y The y point to check against the map limits.
     * @return true if the supplied Y coordinate is within the range of 0 -> mapHeight, meaning it is able to be
     * displayed on the currently selected map.
     */
    public static boolean isYInMap(final double y) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return false;
        }
        return ((y >= 0) && (y <= mapHeight));
    }
    
    /**
     * Set the X, Y values within the supplied Location object to correspond to the Latitude and Longitude of the object
     * when presented on the current map.
     * @param location The Location object containing coordinates to transform.
     */
    public static void setLocationXY(final Location location) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
        }
        location.setX(lonToMapX(location.getLon()));
        location.setY(latToMapY(location.getLat())); 
    }
    
    /**
     * Checks if the X,Y coordinates in the supplied Location object fall within the bounds of the selected map.
     * 
     * @param location Location object containing X,Y coordinates (corresponding to its stored longitude, latitude
     * values for the map) to check against the map limits.
     * @return true if both the X,Y coordinates fall within the maps corresponding 0 -> mapWidth and 0 -> mapHeight
     * ranges, meaning it is able to be displayed on the currently selected map.
     */
    public static boolean isLocationInMap(final Location location) {
        if (!initialized) {
            LOGGER.log(Level.WARNING, MAP_NOT_INITIALIZED_ERROR);
            return false;
        }
        return (isXInMap(location.getX()) && isYInMap(location.getY()));
    }
}
