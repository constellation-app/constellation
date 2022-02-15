/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.interaction;

import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.Arrays;
import java.util.Collections;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class TransactionGraphLabelsAttributeInteractionNGTest {
    
    public TransactionGraphLabelsAttributeInteractionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    /**
     * Test of getDisplayText method, of class TransactionGraphLabelsAttributeInteraction.
     */
    @Test
    public void testGetDisplayText() {
        System.out.println("getDisplayText");
        
        final TransactionGraphLabelsAttributeInteraction instance = new TransactionGraphLabelsAttributeInteraction();
        
        final String nullResult = instance.getDisplayText(null);
        assertNull(nullResult);
        
        final GraphLabels emptyLabels = new GraphLabels(Collections.emptyList());
        final String emptyGraphLabelsResult = instance.getDisplayText(emptyLabels);
        assertEquals(emptyGraphLabelsResult, "");
        
        final GraphLabel testLabel1 = new GraphLabel("test1", ConstellationColor.BANANA, 5F);
        final GraphLabel testLabel2 = new GraphLabel("test2", ConstellationColor.getColorValue(0.1F, 0.2F, 0.3F, 1F), 7F);
        final GraphLabels labels = new GraphLabels(Arrays.asList(testLabel1, testLabel2));
        final String graphLabelsResult = instance.getDisplayText(labels);
        assertEquals(graphLabelsResult, "test1: (Banana), test2: (#19334c)");
    }
}
