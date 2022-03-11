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
import static org.testng.Assert.*;
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
    SchemaFactoryUtilities mockSchemaFactory;
    VisualSchemaV6UpdateProvider instance;
    
    public VisualSchemaV6UpdateProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new VisualSchemaV6UpdateProvider();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockStoreGraph = mock(StoreGraph.class);
        mockSchemaFactory = mock(SchemaFactoryUtilities.class);
    }

    /**
     * Test of getSchema method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.getSchema");
        resetMocking();
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.getFromVersionNumber");
        resetMocking();
        assertEquals(instance.getFromVersionNumber(), VisualSchemaV5UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV6UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV6UpdateProviderNGTest.getToVersionNumber");
        resetMocking();
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
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "transaction_labels")).thenReturn(25);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("strBottom");
        when(mockStoreGraph.getStringValue(24, 0)).thenReturn("strTop");
        when(mockStoreGraph.getStringValue(25, 0)).thenReturn("strTrans");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 0, "strBottom");
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 0, "strTop");
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 0, "strTrans");
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(23);
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(24);
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(25);
    }
}
