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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import java.util.List;
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
public class VertexGraphLabelsAttributeDescriptionNGTest {
    
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
     * Test of convertFromString method, of class VertexGraphLabelsAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final VertexGraphLabelsAttributeDescription instance = new VertexGraphLabelsAttributeDescription();

        final GraphLabels nullResult = instance.convertFromString(null);
        // should be the default here
        assertEquals(nullResult, GraphLabels.NO_LABELS);

        final GraphLabels blankResult = instance.convertFromString("   ");
        // should be the default here as well
        assertEquals(blankResult, GraphLabels.NO_LABELS);

        final GraphLabels validResult = instance.convertFromString("Label1;Blue;1|Label2;Red;2|Label3;Yellow;3");
        assertEquals(validResult.getNumberOfLabels(), 3);
        final List<GraphLabel> validResultLabels = validResult.getLabels();
        assertEquals(validResultLabels.get(0).toString(), "Label1;Blue;1.0");
        assertEquals(validResultLabels.get(1).toString(), "Label2;Red;2.0");
        assertEquals(validResultLabels.get(2).toString(), "Label3;Yellow;3.0");
    }

    /**
     * Test of getVersion method, of class VertexGraphLabelsAttributeDescription.
     */
    @Test
    public void testGetVersion() {
        System.out.println("getVersion");

        final VertexGraphLabelsAttributeDescription instance = new VertexGraphLabelsAttributeDescription();
        assertEquals(instance.getVersion(), 1);
    }
}