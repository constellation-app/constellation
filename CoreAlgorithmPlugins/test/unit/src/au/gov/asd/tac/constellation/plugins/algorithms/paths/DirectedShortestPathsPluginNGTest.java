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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static au.gov.asd.tac.constellation.plugins.algorithms.paths.DirectedShortestPathsPlugin.SOURCE_NODE_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import static org.testng.Assert.assertEquals;
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
public class DirectedShortestPathsPluginNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    
    private int tId1;
    private int tId2;
    private int tId3;
    private int tId4;
    private int tId5;
    private int tId6;
    
    private int vertexLabelAttribute;
    private int vertexSelectedAttribute;
    
    public DirectedShortestPathsPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        vxId5 = graph.addVertex();
        
        tId1 = graph.addTransaction(vxId1, vxId2, false);
        tId2 = graph.addTransaction(vxId1, vxId3, true);
        tId3 = graph.addTransaction(vxId1, vxId4, true);
        tId4 = graph.addTransaction(vxId4, vxId2, true);
        tId5 = graph.addTransaction(vxId2, vxId5, true);
        tId6 = graph.addTransaction(vxId3, vxId5, true);
        
        vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        
        graph.setStringValue(vertexLabelAttribute, vxId1, "Vertex 1");
        graph.setStringValue(vertexLabelAttribute, vxId2, "Vertex 2");
        graph.setStringValue(vertexLabelAttribute, vxId3, "Vertex 3");
        graph.setStringValue(vertexLabelAttribute, vxId4, "Vertex 4");
        graph.setStringValue(vertexLabelAttribute, vxId5, "Vertex 5");
        
        graph.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId2, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId3, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, true);
        graph.setBooleanValue(vertexSelectedAttribute, vxId5, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of createParameters method, of class DirectedShortestPathsPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final DirectedShortestPathsPlugin instance = new DirectedShortestPathsPlugin();
        
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 1);
        assertTrue(params.getParameters().containsKey(SOURCE_NODE_PARAMETER_ID));
    }

    /**
     * Test of updateParameters method, of class DirectedShortestPathsPlugin. Null Graph
     */
    @Test
    public void testUpdateParametersNullGraph() {
        System.out.println("updateParametersNullGraph");
        
        final DirectedShortestPathsPlugin instance = new DirectedShortestPathsPlugin();
        
        final PluginParameters params = instance.createParameters();
        final PluginParameter<SingleChoiceParameterValue> sourceNode = (PluginParameter<SingleChoiceParameterValue>) params.getParameters().get(SOURCE_NODE_PARAMETER_ID);
        
        assertTrue(SingleChoiceParameterType.getOptions(sourceNode).isEmpty());
        
        instance.updateParameters(null, params);
        assertTrue(SingleChoiceParameterType.getOptions(sourceNode).isEmpty());
    }
    
    /**
     * Test of updateParameters method, of class DirectedShortestPathsPlugin.
     */
    @Test
    public void testUpdateParameters() {
        System.out.println("updateParameters");
        
        final DirectedShortestPathsPlugin instance = new DirectedShortestPathsPlugin();
        
        final PluginParameters params = instance.createParameters();
        final PluginParameter<SingleChoiceParameterValue> sourceNode = (PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue>) params.getParameters().get(SOURCE_NODE_PARAMETER_ID);
        
        assertTrue(SingleChoiceParameterType.getOptions(sourceNode).isEmpty());
        
        instance.updateParameters(new DualGraph(graph.getSchema(), graph), params);
        assertEquals(SingleChoiceParameterType.getOptions(sourceNode).size(), 5);
        
        
        graph.setBooleanValue(vertexSelectedAttribute, vxId4, false);
        instance.updateParameters(new DualGraph(graph.getSchema(), graph), params);
        //confirm the options are only set once (i.e. the graph change won't affect this)
        assertEquals(SingleChoiceParameterType.getOptions(sourceNode).size(), 5);
    }
}
