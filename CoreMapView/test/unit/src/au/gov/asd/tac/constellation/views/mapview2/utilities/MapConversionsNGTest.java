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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class MapConversionsNGTest {
    
    private static final DecimalFormat df = new DecimalFormat("0.000");
    
    public MapConversionsNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testInitMapDimensions_validations() {

        // Test checks on map width and height
        final List<Double> invalidDimensions = Arrays.asList(0.0, -1.0, -100.0);
        invalidDimensions.forEach(dimension -> { 
            try {
                MapConversions.initMapDimensions(dimension, 1000.0, 85.0, -85.0, -180.0, 180.0);
                fail ("Width validation failed");
            }
            catch (IllegalArgumentException ex){
                assertEquals(ex.getMessage(), "Supplied map width is invalid");
            }

            try {
                MapConversions.initMapDimensions(1000.0, dimension, 85.0, -85.0, -180.0, 180.0);
                fail ("Height validation failed");
            }
            catch (IllegalArgumentException ex){
                assertEquals(ex.getMessage(), "Supplied map height is invalid");
            }
        });

        // Check latitudes of top left, and bottom right are checked and in the allowable range
        try {
            MapConversions.initMapDimensions(1000.0, 1000.0, 90.1, -85.0, -180.0, 180.0);
            fail ("Top latitude validation failed");
        }
        catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), "Invalid top latitude supplied");
        }          

        // Check that an invalid bottom right latitude is detected
        try {
            MapConversions.initMapDimensions(1000.0, 1000.0, 85.0, -90.1, -180.0, 180.0);
            fail ("Bottom validation failed");
        }
        catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), "Invalid bottom latitude supplied");
        }

        // Check that the top left latitude is > than bottom right
        final List<Double> topLeftLatitudes = Arrays.asList(-80.0, -85.0);
        topLeftLatitudes.forEach(latitude -> {  
            try {
                MapConversions.initMapDimensions(1000.0, 1000.0, latitude, -80.0, -180.0, 180.0);
                fail ("Latitude order validation failed");
            }
            catch (IllegalArgumentException ex){
                assertEquals(ex.getMessage(), "Top left latitude must be greater than bottom right");
            }
        });

        // Check longitudes of top left, and bottom right are checked and in the allowable range
        // Check invalid minimum longitude is detected
        try {
            MapConversions.initMapDimensions(1.0, 1.0, 50.0, -50.0, -180.1, 180.0);
            fail ("Left longitude validation failed");
        }
        catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), "Invalid left longitude supplied");
        }

        // Check invalid maximum longitude is detected
        try {
            MapConversions.initMapDimensions(1.0, 1.0, 50.0, -50.0, -180, 180.1);
            fail ("Right longitude validation failed");
        }
        catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), "Invalid right longitude supplied");
        }
        
        // Confirm no values stored
        assertFalse(MapConversions.getIsInitialized());
        assertEquals(MapConversions.getFullWorldWidth(), 0.0);
        assertEquals(MapConversions.getFullWorldRadius(), 0.0);
        assertEquals(MapConversions.getTopLat(), 0.0);
        assertEquals(MapConversions.getBottomLat(), 0.0);
        assertEquals(MapConversions.getLeftLon(), 0.0);
        assertEquals(MapConversions.getRightLon(), 0.0);
        assertEquals(MapConversions.getMapTopOffsetFromWorldCentre(), 0.0);
        assertEquals(MapConversions.getMapBottomOffsetFromWorldCentre(), 0.0);
        assertEquals(MapConversions.getMapLeftOffsetFromWorldCentre(), 0.0);
        assertEquals(MapConversions.getMapRightOffsetFromWorldCentre(), 0.0);
    }

    @Test
    public void testInitMapDimensions_validDimensions() {
        
        // Try a map which is the basically the full world
        MapConversions.initMapDimensions(1000.0, 998.0, 85.0, -85.0, -180.0, 180.0);
        assertTrue(MapConversions.getIsInitialized());
        assertEquals(df.format(MapConversions.getFullWorldWidth()), "1000.000");
        assertEquals(df.format(MapConversions.getFullWorldRadius()), "159.155");
        assertEquals(df.format(MapConversions.getTopLat()), "85.000");
        assertEquals(df.format(MapConversions.getBottomLat()), "-85.000");
        assertEquals(df.format(MapConversions.getLeftLon()), "-180.000");
        assertEquals(df.format(MapConversions.getRightLon()), "180.000");
        assertEquals(df.format(MapConversions.getMapTopOffsetFromWorldCentre()), "-498.362");
        assertEquals(df.format(MapConversions.getMapBottomOffsetFromWorldCentre()), "498.362");
        assertEquals(df.format(MapConversions.getMapLeftOffsetFromWorldCentre()), "-500.000");
        assertEquals(df.format(MapConversions.getMapRightOffsetFromWorldCentre()), "500.000");
        
        // Try map with only half of full world width
        MapConversions.initMapDimensions(500.0, 998.0, 85.0, -85.0, -90.0, 90.0);
        assertTrue(MapConversions.getIsInitialized());
        assertEquals(df.format(MapConversions.getFullWorldWidth()), "1000.000");
        assertEquals(df.format(MapConversions.getFullWorldRadius()), "159.155");
        assertEquals(df.format(MapConversions.getTopLat()), "85.000");
        assertEquals(df.format(MapConversions.getBottomLat()), "-85.000");
        assertEquals(df.format(MapConversions.getLeftLon()), "-90.000");
        assertEquals(df.format(MapConversions.getRightLon()), "90.000");
        assertEquals(df.format(MapConversions.getMapTopOffsetFromWorldCentre()), "-498.362");
        assertEquals(df.format(MapConversions.getMapBottomOffsetFromWorldCentre()), "498.362");
        assertEquals(df.format(MapConversions.getMapLeftOffsetFromWorldCentre()), "-250.000");
        assertEquals(df.format(MapConversions.getMapRightOffsetFromWorldCentre()), "250.000");
        
               
        // Try map not covering Null Island (0 Lat,0 Long)
        MapConversions.initMapDimensions(125.0, 998.0, 60.0, 10.0, 10.0, 55.0);
        assertTrue(MapConversions.getIsInitialized());
        assertEquals(df.format(MapConversions.getFullWorldWidth()), "1000.000");
        assertEquals(df.format(MapConversions.getFullWorldRadius()), "159.155");
        assertEquals(df.format(MapConversions.getTopLat()), "60.000");
        assertEquals(df.format(MapConversions.getBottomLat()), "10.000");
        assertEquals(df.format(MapConversions.getLeftLon()), "10.000");
        assertEquals(df.format(MapConversions.getRightLon()), "55.000");
        assertEquals(df.format(MapConversions.getMapTopOffsetFromWorldCentre()), "-209.600");
        assertEquals(df.format(MapConversions.getMapBottomOffsetFromWorldCentre()), "-27.920");
        assertEquals(df.format(MapConversions.getMapLeftOffsetFromWorldCentre()), "27.778"); // 10 degrees = 10 * (1000 / 360)
        assertEquals(df.format(MapConversions.getMapRightOffsetFromWorldCentre()), "152.778"); // 55 degrees = 55 * (1000 / 360)
    }
    
    @Test
    public void testLatLongsToXYInMapArea() {
        
        // Reuse map dimensions tested above
        MapConversions.initMapDimensions(500.0, 998.0, 85.0, -85.0, -90.0, 90.0);
        
        // Check longitudes matching left of map have an X offset of 0 and latitudes matching top of map have a Y offset of 0
        double lat = 85.0;
        double lon = -90.0;
        assertEquals(df.format(MapConversions.lonToMapX(lon)), "0.000");
        assertEquals(df.format(MapConversions.latToMapY(lat)), "0.000");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
        
        // Check longitudes matching right of map have an X offset of mapwidth and latitudes matching bottom of map have a Y offset of mapHeight
        lat = -85.0;
        lon = 90.0;
        assertEquals(df.format(MapConversions.lonToMapX(lon)), "500.000");
        assertEquals(df.format(MapConversions.latToMapY(lat)), "996.724");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
        
        // check central lat and long.
        lat = 0.0;
        lon = 0.0;
        assertEquals(df.format(MapConversions.lonToMapX(lon)), "250.000");
        assertEquals(df.format(MapConversions.latToMapY(lat)), "498.362");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
        
        // check other lat and long.
        lat = 20.0;
        lon = 20.0;
        assertEquals(df.format(MapConversions.lonToMapX(20.0)), "305.556");
        assertEquals(df.format(MapConversions.latToMapY(20.0)), "441.643");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
        
        // now use map not covering Null island
        MapConversions.initMapDimensions(125.0, 182.0, 60.0, 10.0, 10.0, 55.0);
        
        // Check longitudes matching left of map have an X offset of 0 and latitudes matching top of map have a Y offset of 0
        lat = 60.0;
        lon = 10.0;
        assertEquals(df.format(MapConversions.lonToMapX(lon)), "0.000");
        assertEquals(df.format(MapConversions.latToMapY(lat)), "0.000");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
        
        // Check longitudes matching right of map have an X offset of mapwidth and latitudes matching bottom of map have a Y offset of mapHeight
        lat = 10.0;
        lon = 55.0;
        assertEquals(df.format(MapConversions.lonToMapX(lon)), "125.000");
        assertEquals(df.format(MapConversions.latToMapY(lat)), "181.680");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
        
        // check another points, correlated to real world spot on other maps (via visual inspection) 
        // note the lat/long values are both positive
        lat = 27.777;
        lon = 34.276;
        assertEquals(df.format(MapConversions.lonToMapX(lon)), "67.433");
        assertEquals(df.format(MapConversions.latToMapY(lat)), "129.229");
        assertTrue(MapConversions.isXInMap(MapConversions.lonToMapX(lon)));
        assertTrue(MapConversions.isYInMap(MapConversions.latToMapY(lat)));
    }
    
    @Test
    public void testMapXToLong() {
        
        // For this test we take a selection of values generated using MapConversions.latToMapX and confirm that calling
        // MapConversions.mapYToLat on the result takes us back to the original latitude. Note that we have already
        // confirmed that MapConversions.latToMapY is working for these values.
        
        // Reuse map dimensions tested above and loop through multiple values in the longitude spectrum and confirm the
        // translations are inverse of each other
        double width = 500.0;
        double height = 998.0;
        double maxLat = 85.0;
        double minLat = -85.0;
        double maxLon = 90.0;
        double minLon = -90.0;
        MapConversions.initMapDimensions(width, height, maxLat, minLat, minLon, maxLon);
        for (double lon=-180.0; lon <= 180.0; lon = lon + 10.0) {
            assertEquals(df.format(MapConversions.mapXToLon(MapConversions.lonToMapX(lon))), df.format(lon));
            assertEquals(MapConversions.isXInMap(MapConversions.lonToMapX(lon)), (lon >= minLon && lon <= maxLon));
        }
        
        // now use map not covering Null island
        width = 125.0;
        height = 182.0;
        maxLat = 60.0;
        minLat = 10.0;
        maxLon = 55.0;
        minLon = 10.0;
        MapConversions.initMapDimensions(width, height, maxLat, minLat, minLon, maxLon);
        for (double lon=-180.0; lon <= 180.0; lon = lon + 10.0) {
            assertEquals(df.format(MapConversions.mapXToLon(MapConversions.lonToMapX(lon))), df.format(lon));
            assertEquals(MapConversions.isXInMap(MapConversions.lonToMapX(lon)), (lon >= minLon && lon <= maxLon));
        }
        
        
        double lon = MapConversions.mapXToLon(-1000000);
        double lat = MapConversions.mapYToLat(-1000000);
        double lon2 = MapConversions.mapXToLon(1000000);
        double lat2 = MapConversions.mapYToLat(1000000);
        int i = 6;
    }
    
    @Test
    public void testMapYToLat() {
        // For this test we take a selection of values generated using MapConversions.latToMapY and confirm that calling
        // MapConversions.mapYToLat on the result takes us back to the original latitude. Note that we have already
        // confirmed that MapConversions.latToMapY is working for these values.
        
        // Reuse map dimensions tested above and loop through multiple values in the latitude spectrum and confirm the
        // translations are inverse of each other
        double width = 500.0;
        double height = 998.0;
        double maxLat = 85.0;
        double minLat = -85.0;
        double maxLon = 90.0;
        double minLon = -90.0;
        MapConversions.initMapDimensions(width, height, maxLat, minLat, minLon, maxLon);
        for (double lat=-90; lat <= 90.0; lat = lat + 1.0) {
            assertEquals(df.format(MapConversions.mapYToLat(MapConversions.latToMapY(lat))), df.format(lat));
            assertEquals(MapConversions.isYInMap(MapConversions.latToMapY(lat)), (lat >= minLat && lat <= maxLat));
        }
        
        // now use map not covering Null island
        width = 125.0;
        height = 182.0;
        maxLat = 60.0;
        minLat = 10.0;
        maxLon = 55.0;
        minLon = 10.0;
        MapConversions.initMapDimensions(width, height, maxLat, minLat, minLon, maxLon);
        assertEquals(df.format(MapConversions.mapYToLat(MapConversions.latToMapY(0.0))), "-0.000");
        for (double lat=-90; lat <= 90.0; lat = lat + 1.0) {
            assertEquals(MapConversions.isYInMap(MapConversions.latToMapY(lat)), (lat >= minLat && lat <= maxLat));
            
            // We skip 0.0 here *(its done above), becuase due to roiunding the reuslt comes out as -0.000
            if (lat != 0.0) {
                assertEquals(df.format(MapConversions.mapYToLat(MapConversions.latToMapY(lat))), df.format(lat));
            }
        }
    }

    @Test
    public void testSetLocationXY() {

        // Reuse map dimensions tested above
        MapConversions.initMapDimensions(500.0, 998.0, 85.0, -85.0, -90.0, 90.0);
        
        Location location = new Location(85.0, -90.0);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "0.000");
        assertEquals(df.format(location.getY()), "0.000");
        
        location = new Location(-85.0, 90.0);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "500.000");
        assertEquals(df.format(location.getY()), "996.724");
        
        location = new Location(0.0, 0.0);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "250.000");
        assertEquals(df.format(location.getY()), "498.362");
        
        location = new Location(20.0, 20.0);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "305.556");
        assertEquals(df.format(location.getY()), "441.643");

        // now use map not covering Null island
        MapConversions.initMapDimensions(125.0, 182.0, 60.0, 10.0, 10.0, 55.0);
        
        location = new Location(60.0, 10.0);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "0.000");
        assertEquals(df.format(location.getY()), "0.000");
        
        location = new Location(10.0, 55.0);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "125.000");
        assertEquals(df.format(location.getY()), "181.680");
        
        location = new Location(27.777, 34.276);
        MapConversions.setLocationXY(location);
        assertEquals(df.format(location.getX()), "67.433");
        assertEquals(df.format(location.getY()), "129.229");
    }

    @Test
    public void testIsLocationInMap() {
        // Reuse map dimensions tested above
        double width = 500.0;
        double height = 998.0;
        double maxLat = 85.0;
        double minLat = -85.0;
        double maxLon = 90.0;
        double minLon = -90.0;
        MapConversions.initMapDimensions(width, height, maxLat, minLat, minLon, maxLon);
        for (double lat=-90; lat <= 90.0; lat = lat + 1.0) {
            for (double lon=-180.0; lon <= 180.0; lon = lon + 10.0) {
                final Location location = new Location(lat, lon);
                MapConversions.setLocationXY(location);
                assertEquals(MapConversions.isLocationInMap(location), (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon)); 
            }
        }
        
        // now use map not covering Null island
        width = 125.0;
        height = 182.0;
        maxLat = 60.0;
        minLat = 10.0;
        maxLon = 55.0;
        minLon = 10.0;
        MapConversions.initMapDimensions(width, height, maxLat, minLat, minLon, maxLon);
        for (double lat=-90; lat <= 90.0; lat = lat + 1.0) {
            for (double lon=-180.0; lon <= 180.0; lon = lon + 10.0) {
                final Location location = new Location(lat, lon);
                MapConversions.setLocationXY(location);
                assertEquals(MapConversions.isLocationInMap(location), (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon)); 
            }
        }
    }
}
