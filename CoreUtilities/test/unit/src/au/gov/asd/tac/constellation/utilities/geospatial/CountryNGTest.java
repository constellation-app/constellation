/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author cygnus_x-1
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class CountryNGTest extends ConstellationTest {

    @Test
    public void testLookupCountryByName() {
        final String name = "Japan";
        final Country expectedResult = Country.JAPAN;
        final Country actualResult = Country.lookupCountryDisplayName(name);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testLookupCountryByDigraph() {
        final String digraph = "JP";
        final Country expectedResult = Country.JAPAN;
        final Country actualResult = Country.lookupCountryDigraph(digraph);
        assertEquals(expectedResult, actualResult);
    }
}
