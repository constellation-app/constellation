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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class VisualSchemaV3UpdateProviderNGTest extends ConstellationTest {
    
    StoreGraph mockStoreGraph;
    VisualSchemaV3UpdateProvider instance;
    
    public VisualSchemaV3UpdateProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new VisualSchemaV3UpdateProvider();
        mockStoreGraph = mock(StoreGraph.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getSchema method, of class VisualSchemaV3UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV3UpdateProviderNGTest.getSchema");
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV3UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV3UpdateProviderNGTest.getFromVersionNumber");
        assertEquals(instance.getFromVersionNumber(), VisualSchemaV2UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV3UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV3UpdateProviderNGTest.getToVersionNumber");
        assertEquals(instance.getToVersionNumber(), VisualSchemaV3UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV3UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_Valid() {
        System.out.println("VisualSchemaV3UpdateProviderNGTest.testSchemaUpdate_Valid");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "visual_state")).thenReturn(23);
        when(mockStoreGraph.getObjectValue(23, 0)).thenReturn(24);
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(0, 0, 24);
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(23);
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV3UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_NotFound() {
        System.out.println("VisualSchemaV3UpdateProviderNGTest.testSchemaUpdate_NotFound");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "visual_state")).thenReturn(Graph.NOT_FOUND);
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(0)).getObjectValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(0)).removeAttribute(anyInt());
    }
}
