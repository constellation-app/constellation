/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author cygnus_x-1
 */
public class MgrsNGTest {

    @Test
    public void testEncodeMgrs() {
        final Tuple<Double, Double> coordinate = Tuple.create(-35.31, 149.12);
        final String expectedResult = "55HFA 92736 90517";
        final String actualResult = Mgrs.encode(coordinate.getFirst(), coordinate.getSecond());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testDecodeMgrs() {
        final String mgrs = "55HFA 92736 90517";
        final double[] expectedResult = new double[]{-35.30999726878406, 149.1200021825661};
        final double[] actualResult = Mgrs.decode(mgrs);
        assertEquals(expectedResult.length, actualResult.length);
        assertEquals(expectedResult[0], actualResult[0]);
        assertEquals(expectedResult[1], actualResult[1]);
    }
}
