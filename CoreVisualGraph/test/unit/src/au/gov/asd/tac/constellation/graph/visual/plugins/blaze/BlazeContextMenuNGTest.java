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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
public class BlazeContextMenuNGTest {
    
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
     * Test of getItems method, of class BlazeContextMenu. Node Element type
     */
    @Test
    public void testGetItemsNodes() {
        System.out.println("getItemsNodes");
        
        final List<ConstellationColor> colorList = Arrays.asList(ConstellationColor.BANANA, ConstellationColor.getColorValue(0.4F, 0.5F, 0.6F, 1F));
        final List<String> expResult = Arrays.asList("Banana", "#668099", "Add Custom Blazes", "Remove Blazes");
        
        final BlazeContextMenu instance = new BlazeContextMenu();
        try(final MockedStatic<BlazeActions> blazeActionsMockedStatic = Mockito.mockStatic(BlazeActions.class)) {
            blazeActionsMockedStatic.when(() -> BlazeActions.getPresetCustomColors()).thenReturn(colorList);
            final List<String> result = instance.getItems(null, GraphElementType.VERTEX, 0);
            assertEquals(result, expResult);
        }
    }
    
    /**
     * Test of getItems method, of class BlazeContextMenu. Transaction Element type
     */
    @Test
    public void testGetItemsTransactions() {
        System.out.println("getItemsTransactions");
        
        final BlazeContextMenu instance = new BlazeContextMenu();
        final List<String> result = instance.getItems(null, GraphElementType.TRANSACTION, 0);
        assertEquals(result, Collections.emptyList());
    }
}
