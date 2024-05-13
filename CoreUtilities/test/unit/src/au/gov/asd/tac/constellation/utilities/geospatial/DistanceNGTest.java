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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author cygnus_x-1
 * @author antares
 */
public class DistanceNGTest {
    
    public DistanceNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    /**
     * Test of degreesToRadians method, of class Distance.
     */
    @Test
    public void testDegreesToRadians() {
        System.out.println("testDegreesToRadians");
        
        final double result1 = Distance.degreesToRadians(0);
        assertEquals(result1, 0D);
        
        final double result2 = Distance.degreesToRadians(90);
        assertEquals(result2, Math.PI / 2);
        
        final double result3 = Distance.degreesToRadians(38.75);
        assertEquals(result3, 0.6763150851478027);
    }

    /**
     * Test of radiansToDegrees method, of class Distance.
     */
    @Test
    public void testRadiansToDegrees() {
        System.out.println("testRadiansToDegrees");
        
        final double result1 = Distance.radiansToDegrees(0);
        assertEquals(result1, 0D);
        
        final double result2 = Distance.radiansToDegrees(Math.PI / 2);
        assertEquals(result2, 90D);
        
        final double result3 = Distance.radiansToDegrees(0.6763150851478027);
        assertEquals(result3, 38.75);
    }

    /**
     * Test of dmsToDd method, of class Distance. Null input
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testDmsToDdNull() {
        System.out.println("testDmsToDdNull");
        
        Distance.dmsToDd(null);
    }
    
    /**
     * Test of dmsToDd method, of class Distance. Incorrect Format for input
     */
    @Test(expectedExceptions = AssertionError.class)
    public void testDmsToDdIncorrectFormat() {
        System.out.println("testDmsToDdncorrectFormat");

        Distance.dmsToDd("0");
    }
    
    /**
     * Test of dmsToDd method, of class Distance.
     */
    @Test
    public void testDmsToDd() {
        System.out.println("testDmsToDd");

        final double result1 = Distance.dmsToDd("38:45:0");
        assertEquals(result1, 38.75);
        
        final double result2 = Distance.dmsToDd("37:105:0");
        assertEquals(result2, 38.75);
        
        final double result3 = Distance.dmsToDd("38:44:60");
        assertEquals(result3, 38.75);
        
        final double result4 = Distance.dmsToDd("37:45:3600");
        assertEquals(result4, 38.75);
    }

    /**
     * Test of ddToDms method, of class Distance.
     */
    @Test
    public void testDdToDms() {
        System.out.println("testDdToDms");

        final String result1 = Distance.ddToDms(0);
        assertEquals(result1, "0:0:0");
               
        final String result2 = Distance.ddToDms(100);
        assertEquals(result2, "100:0:0");
        
        final String result3 = Distance.ddToDms(38.75);
        assertEquals(result3, "38:45:0");        
    }
    
    /**
     * Test of estimateDistanceInDecimalDegrees method, of class Distance.Haversine.
     */
    @Test
    public void testEstimateDistanceInDecimalDegrees() {
        System.out.println("testEstimateDistanceInDecimalDegrees");
        
        final double result = Distance.Haversine.estimateDistanceInDecimalDegrees(0, 0, 0, 0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.estimateDistanceInDecimalDegrees(-35.31, 149.12, -33.85, 151.21);
        assertEquals(result2, 2.25657974186211);
    }
    
    /**
     * Test of decimalDegreesToKilometers method, of class Distance.Haversine.
     */
    @Test
    public void testDecimalDegreesToKilometers() {
        System.out.println("testDecimalDegreesToKilometers");
        
        final double result = Distance.Haversine.decimalDegreesToKilometers(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.decimalDegreesToKilometers(2.25657974186211);
        assertEquals(result2, 250.92021886395457);
    }
    
    /**
     * Test of decimalDegreesToMiles method, of class Distance.Haversine.
     */
    @Test
    public void testDecimalDegreesToMiles() {
        System.out.println("testDecimalDegreesToMiles");
        
        final double result = Distance.Haversine.decimalDegreesToMiles(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.decimalDegreesToMiles(2.25657974186211);
        assertEquals(result2, 155.92421071768894);
    }
    
    /**
     * Test of decimalDegreesToNauticalMiles method, of class Distance.Haversine.
     */
    @Test
    public void testDecimalDegreesToNauticalMiles() {
        System.out.println("testDecimalDegreesToNauticalMiles");
        
        final double result = Distance.Haversine.decimalDegreesToNauticalMiles(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.decimalDegreesToNauticalMiles(2.25657974186211);
        assertEquals(result2, 135.4835273727835);
    }
    
    /**
     * Test of estimateDistanceInKilometers method, of class Distance.Haversine.
     */
    @Test
    public void testEstimateDistanceInKilometers() {
        System.out.println("testEstimateDistanceInKilometers");
        
        final double result = Distance.Haversine.estimateDistanceInKilometers(0, 0, 0, 0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.estimateDistanceInKilometers(-35.31, 149.12, -33.85, 151.21);
        assertEquals(result2, 250.92021886395457);
    }
    
    /**
     * Test of kilometersToDecimalDegrees method, of class Distance.Haversine.
     */
    @Test
    public void testKilometersToDecimalDegrees() {
        System.out.println("testKilometersToDecimalDegrees");
        
        final double result = Distance.Haversine.kilometersToDecimalDegrees(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.kilometersToDecimalDegrees(250.92021886395457);
        assertEquals(result2, 2.25657974186211);
    }
    
    /**
     * Test of kilometersToMiles method, of class Distance.Haversine.
     */
    @Test
    public void testKilometersToMiles() {
        System.out.println("testKilometersToMiles");
        
        final double result = Distance.Haversine.kilometersToMiles(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.kilometersToMiles(250.92021886395457);
        assertEquals(result2, 155.92421071768894);
    }
    
    /**
     * Test of kilometersToNauticalMiles method, of class Distance.Haversine.
     */
    @Test
    public void testKilometersToNauticalMiles() {
        System.out.println("testKilometersToNauticalMiles");
        
        final double result = Distance.Haversine.kilometersToNauticalMiles(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.kilometersToNauticalMiles(250.92021886395457);
        assertEquals(result2, 135.4835273727835);
    }
    
    /**
     * Test of estimateDistanceInMiles method, of class Distance.Haversine.
     */
    @Test
    public void testEstimateDistanceInMiles() {
        System.out.println("testEstimateDistanceInMiles");
        
        final double result = Distance.Haversine.estimateDistanceInMiles(0, 0, 0, 0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.estimateDistanceInMiles(-35.31, 149.12, -33.85, 151.21);
        assertEquals(result2, 155.92421071768894);
    }
    
    /**
     * Test of milesToDecimalDegrees method, of class Distance.Haversine.
     */
    @Test
    public void testMilesToDecimalDegrees() {
        System.out.println("testMilesToDecimalDegrees");
        
        final double result = Distance.Haversine.milesToDecimalDegrees(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.milesToDecimalDegrees(155.92421071768894);
        assertEquals(result2, 2.25657974186211);
    }
    
    /**
     * Test of milesToKilometers method, of class Distance.Haversine.
     */
    @Test
    public void testMilesToKilometers() {
        System.out.println("testMilesToKilometers");
        
        final double result = Distance.Haversine.milesToKilometers(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.milesToKilometers(155.92421071768894);
        assertEquals(result2, 250.92021886395457);
    }
    
    /**
     * Test of milesToNauticalMiles method, of class Distance.Haversine.
     */
    @Test
    public void testMilesToNauticalMiles() {
        System.out.println("testMilesToNauticalMiles");
        
        final double result = Distance.Haversine.milesToNauticalMiles(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.milesToNauticalMiles(155.92421071768894);
        assertEquals(result2, 135.4835273727835);
    }
    
    /**
     * Test of estimateDistanceInNauticalMiles method, of class Distance.Haversine.
     */
    @Test
    public void testEstimateDistanceInNauticalMiles() {
        System.out.println("testEstimateDistanceInNauticalMiles");
        
        final double result = Distance.Haversine.estimateDistanceInNauticalMiles(0, 0, 0, 0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.estimateDistanceInNauticalMiles(-35.31, 149.12, -33.85, 151.21);
        assertEquals(result2, 135.4835273727835);
    }
    
    /**
     * Test of nauticalMilesToDecimalDegrees method, of class Distance.Haversine.
     */
    @Test
    public void testNauticalMilesToDecimalDegrees() {
        System.out.println("testNauticalMilesToDecimalDegrees");
        
        final double result = Distance.Haversine.nauticalMilesToDecimalDegrees(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.nauticalMilesToDecimalDegrees(135.4835273727835);
        assertEquals(result2, 2.25657974186211);
    }
    
    /**
     * Test of nauticalMilesToKilometers method, of class Distance.Haversine.
     */
    @Test
    public void testNauticalMilesToKilometers() {
        System.out.println("testNauticalMilesToKilometers");
        
        final double result = Distance.Haversine.nauticalMilesToKilometers(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.nauticalMilesToKilometers(135.4835273727835);
        assertEquals(result2, 250.92021886395457);
    }
    
    /**
     * Test of nauticalMilesToMiles method, of class Distance.Haversine.
     */
    @Test
    public void testNauticalMilesToMiles() {
        System.out.println("testNauticalMilesToMiles");
        
        final double result = Distance.Haversine.nauticalMilesToMiles(0);
        assertEquals(result, 0D);

        final double result2 = Distance.Haversine.nauticalMilesToMiles(135.4835273727835);
        assertEquals(result2, 155.92421071768894);
    }
}
