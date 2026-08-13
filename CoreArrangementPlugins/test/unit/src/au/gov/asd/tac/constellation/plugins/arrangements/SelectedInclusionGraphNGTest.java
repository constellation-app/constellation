/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
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
public class SelectedInclusionGraphNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int selectedNodeAttribute;

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
        graph = new StoreGraph();
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId2, vxId1, true);
        graph.addTransaction(vxId1, vxId2, true);
        graph.addTransaction(vxId3, vxId4, true);
        
        selectedNodeAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);
        
        graph.setBooleanValue(selectedNodeAttribute, vxId1, true);
        graph.setBooleanValue(selectedNodeAttribute, vxId2, true);
        graph.setBooleanValue(selectedNodeAttribute, vxId3, true);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of getInclusionGraph method, of class AbstractInclusionGraph.
     */
    @Test
    public void testGetInclusionGraph() {
        System.out.println("getInclusionGraph");
        
        final SelectedInclusionGraph instance = new SelectedInclusionGraph(graph, Connections.NONE);
        final GraphWriteMethods inclusionGraph = instance.getInclusionGraph();
        
        assertEquals(inclusionGraph.getVertexCount(), 3);
        assertEquals(inclusionGraph.getTransactionCount(), 0);
    }
    
    /**
     * Test of getInclusionGraph method, of class AbstractInclusionGraph. Transaction connection mode.
     */
    @Test
    public void testGetInclusionGraphTransaction() {
        System.out.println("getInclusionGraphTransaction");
        
        final SelectedInclusionGraph instance = new SelectedInclusionGraph(graph, Connections.TRANSACTIONS);
        final GraphWriteMethods inclusionGraph = instance.getInclusionGraph();
        
        assertEquals(inclusionGraph.getVertexCount(), 3);
        assertEquals(inclusionGraph.getTransactionCount(), 3);
    }
    
    /**
     * Test of getInclusionGraph method, of class AbstractInclusionGraph. Edge connection mode.
     */
    @Test
    public void testGetInclusionGraphEdge() {
        System.out.println("getInclusionGraphEdge");
        
        final SelectedInclusionGraph instance = new SelectedInclusionGraph(graph, Connections.EDGES);
        final GraphWriteMethods inclusionGraph = instance.getInclusionGraph();
        
        assertEquals(inclusionGraph.getVertexCount(), 3);
        assertEquals(inclusionGraph.getTransactionCount(), 2);
    }
    
    /**
     * Test of getInclusionGraph method, of class AbstractInclusionGraph. Link connection mode.
     */
    @Test
    public void testGetInclusionGraphLink() {
        System.out.println("getInclusionGraphLink");
        
        final SelectedInclusionGraph instance = new SelectedInclusionGraph(graph, Connections.LINKS);
        final GraphWriteMethods inclusionGraph = instance.getInclusionGraph();
        
        assertEquals(inclusionGraph.getVertexCount(), 3);
        assertEquals(inclusionGraph.getTransactionCount(), 1);
    }
}
