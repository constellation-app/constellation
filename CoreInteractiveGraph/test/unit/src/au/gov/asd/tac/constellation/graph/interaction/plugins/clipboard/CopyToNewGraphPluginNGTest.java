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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin.COPY_ALL_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin.COPY_KEYS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin.NEW_GRAPH_OUTPUT_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard.CopyToNewGraphPlugin.NEW_SCHEMA_NAME_PARAMETER_ID;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class CopyToNewGraphPluginNGTest {
    
    private StoreGraph graph;
    
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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        
        final int tId1 = graph.addTransaction(vxId1, vxId2, true);
        final int tId2 = graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId2, vxId3, true);
        
        final int selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
        
        graph.setBooleanValue(selectedTransactionAttribute, tId1, true);
        graph.setBooleanValue(selectedTransactionAttribute, tId2, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createParameters method, of class CopyToNewGraphPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final CopyToNewGraphPlugin instance = new CopyToNewGraphPlugin();
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 4);
        assertTrue(params.getParameters().containsKey(NEW_SCHEMA_NAME_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(COPY_ALL_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(COPY_KEYS_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(NEW_GRAPH_OUTPUT_PARAMETER_ID));
    }

    /**
     * Test of read method, of class CopyToNewGraphPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testRead() throws InterruptedException, PluginException {
        System.out.println("read");
        
        final CopyToNewGraphPlugin instance = new CopyToNewGraphPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setStringValue(NEW_SCHEMA_NAME_PARAMETER_ID, VisualSchemaFactory.VISUAL_SCHEMA_ID);
        
        instance.read(graph, null, parameters);
        
        final Graph newGraph = (Graph) parameters.getObjectValue(NEW_GRAPH_OUTPUT_PARAMETER_ID);
        try (final ReadableGraph newGraphRead = newGraph.getReadableGraph()) {
            assertEquals(newGraphRead.getVertexCount(), 2);
            // despite having 2 transactions selected, only 1 gets copied over due to the other transaction not having both nodes (on either end) selected as well
            assertEquals(newGraphRead.getTransactionCount(), 1);
        }
    }
    
    /**
     * Test of read method, of class CopyToNewGraphPlugin. Copy everything from the graph
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testReadCopyAll() throws InterruptedException, PluginException {
        System.out.println("readCopyAll");
        
        final CopyToNewGraphPlugin instance = new CopyToNewGraphPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setStringValue(NEW_SCHEMA_NAME_PARAMETER_ID, VisualSchemaFactory.VISUAL_SCHEMA_ID);
        parameters.setBooleanValue(COPY_ALL_PARAMETER_ID, true);
        
        instance.read(graph, null, parameters);
        
        final Graph newGraph = (Graph) parameters.getObjectValue(NEW_GRAPH_OUTPUT_PARAMETER_ID);
        try (final ReadableGraph newGraphRead = newGraph.getReadableGraph()) {
            assertEquals(newGraphRead.getVertexCount(), 3);
            assertEquals(newGraphRead.getTransactionCount(), 3);
        }
    }
    
    /**
     * Test of read method, of class CopyToNewGraphPlugin. Don't copy the keys over to the new graph.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testReadNotCopyKeys() throws InterruptedException, PluginException {
        System.out.println("readNotCopyKeys");
        
        int customAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "testAttribute", "", null, "");
        
        graph.setPrimaryKey(GraphElementType.VERTEX, customAttribute);
        
        final CopyToNewGraphPlugin instance = new CopyToNewGraphPlugin();
        final PluginParameters parameters = instance.createParameters();
        parameters.setStringValue(NEW_SCHEMA_NAME_PARAMETER_ID, VisualSchemaFactory.VISUAL_SCHEMA_ID);
        parameters.setBooleanValue(COPY_KEYS_PARAMETER_ID, false);
        
        assertEquals(graph.getPrimaryKey(GraphElementType.VERTEX), new int[]{customAttribute});
        
        instance.read(graph, null, parameters);
        
        final Graph newGraph = (Graph) parameters.getObjectValue(NEW_GRAPH_OUTPUT_PARAMETER_ID);
        try (final ReadableGraph newGraphRead = newGraph.getReadableGraph()) {
            assertEquals(newGraphRead.getVertexCount(), 2);
            assertEquals(newGraphRead.getTransactionCount(), 1);
            
            customAttribute = newGraphRead.getAttribute(GraphElementType.VERTEX, "testAttribute");
            // we haven't copied the key over and so while the custom attribute should still be present in the new graph
            // it will no longer be a key attribute
            assertNotEquals(customAttribute, Graph.NOT_FOUND);
            assertNotEquals(newGraphRead.getPrimaryKey(GraphElementType.VERTEX), new int[]{customAttribute});
        }
    }
}
