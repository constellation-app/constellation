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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Nova
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DimensionsNGTest extends ConstellationTest {

    /**
     * Test of getOptions method, of class Dimensions.
     */
    @Test
    public void testGetOptions() {
        System.out.println("getOptions");
        List<String> expResult = new ArrayList<>();
        expResult.add(Dimensions.TWO.toString());
        expResult.add(Dimensions.THREE.toString());
        List result = Dimensions.getOptions();
        assertEquals(result, expResult);
    }

}
