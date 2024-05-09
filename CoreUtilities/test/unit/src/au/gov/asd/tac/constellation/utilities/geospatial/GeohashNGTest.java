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
package au.gov.asd.tac.constellation.utilities.geospatial;

import au.gov.asd.tac.constellation.utilities.geospatial.Geohash.Base;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Geohash Test.
 *
 * @author cygnus_x-1
 */
public class GeohashNGTest {

    private double latitude;
    private double longitude;
    private int length;
    private final Base base = Base.B16;
    private String geohash;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        latitude = -27.3;
        longitude = 104.8;
        length = 8;
        geohash = Geohash.encode(latitude, longitude, length, base);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of encode method, of class Geohash.
     */
    @Test
    public void testEncode() {
        String expResult = "b4d8c13c";
        String result = this.geohash;
        assertEquals(result, expResult);
    }

    /**
     * Test of decode method, of class Geohash.
     */
    @Test
    public void testDecode() {
        double[] expResult = new double[]{-27.29827880859375, 104.80133056640625, 0.00274658203125, 0.00274658203125};
        double[] result = Geohash.decode(geohash, base);
        assertEquals(result, expResult);
    }

    /**
     * Test of decode method, of class Geohash.
     */
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testDecodeWithInvalidHash() {
        Geohash.decode("123.456", base);
    }

    /**
     * Test of getErrorForLength method, of class Geohash.
     */
    @Test
    public void testGetErrorForLength() {
        double[] expResult = new double[]{0.00274658203125, 0.00274658203125};
        double[] result = Geohash.getErrorForLength(base, length);
        assertEquals(result, expResult);
    }

    /**
     * Test of getDistanceInGrids method, of class Geohash.
     */
    @Test
    public void testGetDistanceInGrids() {
        int expResult = 22533;
        int result = Geohash.getDistanceInGrids(geohash, "a1b2c3d4", base);
        assertEquals(result, expResult);
    }

    /**
     * Test of getDistanceKm method, of class Geohash.
     */
    @Test
    public void testGetDistanceKm() {
        double expResult = 13738.607553558162;
        double result = Geohash.getDistanceKm(geohash, "a1b2c3d4", base);
        assertEquals(result, expResult);
    }

    /**
     * Test of getGeoJSON method, of class Geohash.
     *
     * @throws java.io.IOException
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGetGeoJSON() throws IOException {
        String expResult = "{\"type\":\"FeatureCollection\",\"bbox\":[104.798583984375,-27.301025390625,104.8040771484375,-27.2955322265625],\"features\":[{\"type\":\"Feature\",\"bbox\":[104.798583984375,-27.301025390625,104.8040771484375,-27.2955322265625],\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[104.79858398,-27.30102539],[104.79858398,-27.29553223],[104.80407715,-27.29553223],[104.80407715,-27.30102539],[104.79858398,-27.30102539]]]},\"properties\":{\"name\":\"b4d8c13c\",\"centreLat\":-27.2982788,\"centreLon\":104.80133057,\"radius\":0.00274659},\"id\":\"b4d8c13c\"}]}";
        String result = Geohash.getGeoJson(geohash);
        assertEquals(result, expResult);
    }
}
