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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
public class VisualSchemaV4UpdateProviderNGTest {
    
    StoreGraph mockStoreGraph;
    SchemaFactoryUtilities mockSchemaFactory;
    VisualSchemaV4UpdateProvider instance;
    
    public VisualSchemaV4UpdateProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new VisualSchemaV4UpdateProvider();
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
     * Test of getSchema method, of class VisualSchemaV4UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV4UpdateProviderNGTest.getSchema");
        resetMocking();
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV4UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV4UpdateProviderNGTest.getFromVersionNumber");
        resetMocking();
        assertEquals(instance.getFromVersionNumber(), VisualSchemaV3UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV4UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV4UpdateProviderNGTest.getToVersionNumber");
        resetMocking();
        assertEquals(instance.getToVersionNumber(), VisualSchemaV4UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }
    
    /**
     * Test of schemaUpdate method, of class VisualSchemaV4UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("VisualSchemaV4UpdateProviderNGTest.testSchemaUpdate");

        ArgumentCaptor<GraphElementType> elementTypeCaptor = ArgumentCaptor.forClass(GraphElementType.class);
        ArgumentCaptor<Object> arrayCaptor = ArgumentCaptor.forClass(Object.class);

        when(mockStoreGraph.getAttribute(GraphElementType.VERTEX, "Name")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.TRANSACTION, "UniqueId")).thenReturn(708);
        when(mockStoreGraph.getVertexCount()).thenReturn(2);
        when(mockStoreGraph.getTransactionCount()).thenReturn(2);
        when(mockStoreGraph.getVertex(anyInt())).thenReturn(0,1);
        when(mockStoreGraph.getTransaction(anyInt())).thenReturn(2,3);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("Label0");
        when(mockStoreGraph.getStringValue(23, 1)).thenReturn("Label1");
        when(mockStoreGraph.getStringValue(708, 2)).thenReturn("Label2");
        when(mockStoreGraph.getStringValue(708, 3)).thenReturn("Label3");
        when(mockStoreGraph.getStringValue(0, 0)).thenReturn("LabelValue");
        when(mockStoreGraph.getStringValue(0, 1)).thenReturn(null);
        when(mockStoreGraph.getSchema()).thenReturn(new Schema(new VisualSchemaFactory()));
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).setPrimaryKey(GraphElementType.VERTEX);
        Mockito.verify(mockStoreGraph, times(2)).setPrimaryKey(GraphElementType.TRANSACTION);
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 0, "Label0");
        Mockito.verify(mockStoreGraph, times(2)).setStringValue(0, 1, "Label1");
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 2, "Label2");
        Mockito.verify(mockStoreGraph, times(1)).setStringValue(0, 3, "Label3");
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(23);
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(708);
        Mockito.verify(mockStoreGraph, times(2)).setPrimaryKey(elementTypeCaptor.capture(), (int[])arrayCaptor.capture());
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(anyInt(), anyInt(), any(GraphLabels.class));
        assertTrue("Node".equals(elementTypeCaptor.getAllValues().get(0).toString()));
        assertTrue("Transaction (no merging)".equals(elementTypeCaptor.getAllValues().get(1).toString()));
        assertEquals(arrayCaptor.getAllValues().get(0), 0);
        assertEquals(arrayCaptor.getAllValues().get(1), 0);
    } 
}
