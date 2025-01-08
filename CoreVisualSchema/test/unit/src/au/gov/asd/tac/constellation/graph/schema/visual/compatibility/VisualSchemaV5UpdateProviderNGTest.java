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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import org.mockito.ArgumentCaptor;
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
public class VisualSchemaV5UpdateProviderNGTest {
    
    StoreGraph mockStoreGraph;
    VisualSchemaV5UpdateProvider instance;
    
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
        instance = new VisualSchemaV5UpdateProvider();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Perform reset of all mocks and argument captors to ensure clean test steps.
     */
    public void resetMocking() {
        mockStoreGraph = mock(StoreGraph.class);
    }

    /**
     * Test of getSchema method, of class VisualSchemaV5UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV5UpdateProviderNGTest.getSchema");
        resetMocking();
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV5UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV5UpdateProviderNGTest.getFromVersionNumber");
        resetMocking();
        assertEquals(instance.getFromVersionNumber(), VisualSchemaV4UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV5UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV5UpdateProviderNGTest.getToVersionNumber");
        resetMocking();
        assertEquals(instance.getToVersionNumber(), VisualSchemaV5UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV5UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("VisualSchemaV5UpdateProviderNGTest.testSchemaUpdate");

        ArgumentCaptor<Integer> attributeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<VertexDecorators> decoratorsCaptor = ArgumentCaptor.forClass(VertexDecorators.class);
        
        when(mockStoreGraph.getObjectValue(0, 0)).thenReturn(new VertexDecorators("oldNW", "oldNE", "oldSE", "oldSW"));
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), decoratorsCaptor.capture());
        assertEquals((int)attributeCaptor.getValue(), 0);
        assertEquals((int)idCaptor.getValue(), 0);
        assertEquals(decoratorsCaptor.getValue().toString(), "\"oldNW\";;\"oldSE\";\"oldSW\";");
    }   
}
