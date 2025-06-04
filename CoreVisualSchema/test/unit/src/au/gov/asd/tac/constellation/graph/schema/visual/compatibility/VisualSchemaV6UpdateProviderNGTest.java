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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
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
public class VisualSchemaV6UpdateProviderNGTest {
    
    StoreGraph mockStoreGraph;
    VisualSchemaV6UpdateProvider instance;
    
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
        instance = new VisualSchemaV6UpdateProvider();
        mockStoreGraph = mock(StoreGraph.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getSchema method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.getSchema");
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.getFromVersionNumber");
        assertEquals(instance.getFromVersionNumber(), VisualSchemaV5UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.getToVersionNumber");
        assertEquals(instance.getToVersionNumber(), VisualSchemaV6UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.testSchemaUpdate");
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "node_labels_bottom")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "node_labels_top")).thenReturn(24);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("strBottom");
        when(mockStoreGraph.getStringValue(24, 0)).thenReturn("strTop");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 0, "strBottom");
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 0, "strTop");
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(23);
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(24);
    }
}
