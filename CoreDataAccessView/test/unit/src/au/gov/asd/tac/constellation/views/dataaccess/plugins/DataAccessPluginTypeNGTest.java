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
package au.gov.asd.tac.constellation.views.dataaccess.plugins;

import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DataAccessPluginTypeNGTest {

    /**
     * Test of getTypes method, of class DataAccessPluginType.
     */
    @Test
    public void testGetTypes() {
        System.out.println("getTypes");

        final List<String> types = DataAccessPluginType.getTypes();
        // assuming at this point that Core types will be included in the list so checking for those
        assertTrue(types.indexOf("Favourites") < types.indexOf("Import"));
        assertTrue(types.indexOf("Import") < types.indexOf("Extend"));
        assertTrue(types.indexOf("Extend") < types.indexOf("Enrichment"));
        assertTrue(types.indexOf("Enrichment") < types.indexOf("Clean"));
        assertTrue(types.indexOf("Clean") < types.indexOf("Workflow"));
        assertTrue(types.indexOf("Workflow") < types.indexOf("Utility"));
        assertTrue(types.indexOf("Utility") < types.indexOf("Experimental"));
        assertTrue(types.indexOf("Experimental") < types.indexOf("Developer"));
    }

    /**
     * Test of getTypeWithPosition method, of class DataAccessPluginType.
     */
    @Test
    public void testGetTypeWithPosition() {
        System.out.println("getTypeWithPosition");

        final Map<String, Integer> typesAndPositions = DataAccessPluginType.getTypeWithPosition();
        // assuming at this point that Core types will be included in the list so checking for those
        assertTrue(typesAndPositions.get("Favourites") == Integer.MIN_VALUE);
        assertTrue(typesAndPositions.get("Import") == 1000);
        assertTrue(typesAndPositions.get("Extend") == 2000);
        assertTrue(typesAndPositions.get("Enrichment") == 2300);
        assertTrue(typesAndPositions.get("Clean") == 3000);
        assertTrue(typesAndPositions.get("Workflow") == 4000);
        assertTrue(typesAndPositions.get("Utility") == 5000);
        assertTrue(typesAndPositions.get("Experimental") == Integer.MAX_VALUE - 1);
        assertTrue(typesAndPositions.get("Developer") == Integer.MAX_VALUE);
    }
}
