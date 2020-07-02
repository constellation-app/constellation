/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author cygnus_x-1
 */
public class DistanceNGTest {

    @Test
    public void testDegreesToRadians() {
        final double degrees = 38.75;
        final double expectedResult = 0.6763150851478027;
        final double actualResult = Distance.degreesToRadians(degrees);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testRadiansToDegrees() {
        final double radians = 0.6763150851478027;
        final double expectedResult = 38.75;
        final double actualResult = Distance.radiansToDegrees(radians);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testDmsToDd() {
        final String dms = "38:45:0";
        final double expectedResult = 38.75;
        final double actualResult = Distance.dmsToDd(dms);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testDdToDms() {
        final double dd = 38.75;
        final String expectedResult = "38:45:0";
        final String actualResult = Distance.ddToDms(dd);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testHaversineDistanceInDecimalDegrees() {
        final Tuple<Double, Double> TupleOne = Tuple.create(-35.31, 149.12);
        final Tuple<Double, Double> TupleTwo = Tuple.create(-33.85, 151.21);
        final double expectedResult = 2.25657974186211;
        final double actualResult = Distance.Haversine.estimateDistanceInDecimalDegrees(
                TupleOne.getFirst(), TupleOne.getSecond(),
                TupleTwo.getFirst(), TupleTwo.getSecond());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testHaversineDistanceInKilometers() {
        final Tuple<Double, Double> TupleOne = Tuple.create(-35.31, 149.12);
        final Tuple<Double, Double> TupleTwo = Tuple.create(-33.85, 151.21);
        final double expectedResult = 250.92021886395457;
        final double actualResult = Distance.Haversine.estimateDistanceInKilometers(
                TupleOne.getFirst(), TupleOne.getSecond(),
                TupleTwo.getFirst(), TupleTwo.getSecond());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testHaversineDistanceInMiles() {
        final Tuple<Double, Double> TupleOne = Tuple.create(-35.31, 149.12);
        final Tuple<Double, Double> TupleTwo = Tuple.create(-33.85, 151.21);
        final double expectedResult = 155.92421071768894;
        final double actualResult = Distance.Haversine.estimateDistanceInMiles(
                TupleOne.getFirst(), TupleOne.getSecond(),
                TupleTwo.getFirst(), TupleTwo.getSecond());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testHaversineDistanceInNauticalMiles() {
        final Tuple<Double, Double> TupleOne = Tuple.create(-35.31, 149.12);
        final Tuple<Double, Double> TupleTwo = Tuple.create(-33.85, 151.21);
        final double expectedResult = 135.4835273727835;
        final double actualResult = Distance.Haversine.estimateDistanceInNauticalMiles(
                TupleOne.getFirst(), TupleOne.getSecond(),
                TupleTwo.getFirst(), TupleTwo.getSecond());
        assertEquals(expectedResult, actualResult);
    }
}
