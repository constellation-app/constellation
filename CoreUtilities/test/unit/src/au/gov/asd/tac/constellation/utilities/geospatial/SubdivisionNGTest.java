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
public class SubdivisionNGTest {

    @Test
    public void testLookupSubdivisionByDisplayName() {
        final String name = "australian capital territory";
        final Subdivision expectedResult = Subdivision.AUSTRALIAN_CAPITAL_TERRITORY;
        final Subdivision actualResult = Subdivision.lookupSubdivisionDisplayName(name);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testLookupSubdivisionByCode() {
        final Country country = Country.AUSTRALIA;
        final String code = "NSW";
        final Subdivision expectedResult = Subdivision.NEW_SOUTH_WALES;
        final Subdivision actualResult = Subdivision.lookupSubdivisionCode(country, code);
        assertEquals(expectedResult, actualResult);
    }
}
