/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class GridChoiceNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getValue method, of class GridChoice.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        
        assertEquals(GridChoice.getValue("Square"), GridChoice.SQUARE);
        assertEquals(GridChoice.getValue("Horizontal line"), GridChoice.HORIZONTAL_LINE);
        assertEquals(GridChoice.getValue("Vertical line"), GridChoice.VERTICAL_LINE);
        assertEquals(GridChoice.getValue("Two rows"), GridChoice.TWO_ROWS);
        assertEquals(GridChoice.getValue("Three rows"), GridChoice.THREE_ROWS);
        assertEquals(GridChoice.getValue("Four rows"), GridChoice.FOUR_ROWS);
        assertEquals(GridChoice.getValue("Two columns"), GridChoice.TWO_COLUMNS);
        assertEquals(GridChoice.getValue("Three columns"), GridChoice.THREE_COLUMNS);
        assertEquals(GridChoice.getValue("Four columns"), GridChoice.FOUR_COLUMNS);
        // should be case-insensitive
        assertEquals(GridChoice.getValue("fOur CoLumNs"), GridChoice.FOUR_COLUMNS);
        // not an option, should return Square
        assertEquals(GridChoice.getValue("Five columns"), GridChoice.SQUARE);
    }

    /**
     * Test of getChoices method, of class GridChoice.
     */
    @Test
    public void testGetChoices() {
        System.out.println("getChoices");
        
        final List<String> choicesList = GridChoice.getChoices();
        final Set<String> choicesSet = new HashSet<>(choicesList);
        
        // check that the list doesn't contain any accidental duplicates (since uniqueness isn't guaranteed for lists)
        // and that it is same size as of the the total enum values
        // (without checking each individual value, we'll presume we're not adding random entries here at the expense of actual values)
        assertEquals(choicesList.size(), choicesSet.size());
        assertEquals(choicesList.size(), GridChoice.values().length);
    }
}
