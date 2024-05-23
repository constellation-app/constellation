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
package au.gov.asd.tac.constellation.views.mapview.markers;

import de.fhpotsdam.unfolding.geo.Location;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Constellation Point Marker Test.
 *
 * @author cygnus_x-1
 */
public class ConstellationAbstractMarkerNGTest {

    private static final Logger LOGGER = Logger.getLogger(ConstellationAbstractMarkerNGTest.class.getName());

    private Location locationA;
    private Location locationB;
    private Location locationC;
    private ConstellationPointMarker point;
    private ConstellationLineMarker line;

    @BeforeMethod
    public void setUpMethod() {
        this.locationA = new Location(-1.234f, 567.890f);
        this.locationB = new Location(-2.345f, 678.901f);
        this.locationC = new Location(-3.456f, 789.012f);
        this.point = new ConstellationPointMarker(locationA);
        this.line = new ConstellationLineMarker(Arrays.asList(locationA, locationB));
    }

    /**
     * Test of getId method, of class ConstellationAbstractMarker.
     */
    @Test
    public void testGetId() {
        final String expResult = null;
        final String result = point.getId();
        assertEquals(result, expResult);
    }

    /**
     * Test of setId method, of class ConstellationAbstractMarker.
     */
    @Test
    public void testSetId() {
        final String id = "test_id";
        point.setId(id);
        final String expResult = id;
        final String result = point.id;
        assertEquals(result, expResult);
    }

    @Test
    public void testGetLocation() {
        final Location expResult = locationA;
        final Location result = point.getLocation();
        assertEquals(result, expResult);
    }

    /**
     * Test of setLocation method, of class ConstellationAbstractMarker.
     */
    @Test
    public void testSetLocation_float_float() {
        point.setLocation(locationB.x, locationB.y);
        final List<Location> expResult = Arrays.asList(locationB);
        final List<Location> result = point.locations;
        assertEquals(result, expResult);
    }

    /**
     * Test of setLocation method, of class ConstellationAbstractMarker.
     */
    @Test
    public void testSetLocation_Location() {
        point.setLocation(locationB);
        final List<Location> expResult = Arrays.asList(locationB);
        final List<Location> result = point.locations;
        assertEquals(result, expResult);
    }

    /**
     * Test of getLocations method, of class ConstellationAbstractMarker.
     */
    @Test
    public void testGetLocations() {
        point.setLocation(locationB);
        final List<Location> expResult = Arrays.asList(locationB);
        final List<Location> result = point.getLocations();
        assertEquals(result, expResult);
    }

//    /**
//     * Test of setLocations method, of class ConstellationAbstractMarker.
//     */
//    @Test
//    public void testSetLocations() {
//        final List<Location> locations = new ArrayList<>(Arrays.asList(locationB, locationC));
//        line.setLocations(locations);
//        final List<Location> expResult = locations;
//        final List<Location> result = line.locations;
//        assertEquals(result, expResult);
//    }
}
