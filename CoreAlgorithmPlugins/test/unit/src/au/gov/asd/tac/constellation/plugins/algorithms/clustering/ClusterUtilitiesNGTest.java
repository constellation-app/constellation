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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
public class ClusterUtilitiesNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    
    private int clusterVertexAttributeId;
    private int colorVertexAttributeId;
    private int colorTransactionAttributeId;
    
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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();
        vxId4 = graph.addVertex();
        
        txId1 = graph.addTransaction(vxId1, vxId2, true);
        txId2 = graph.addTransaction(vxId2, vxId4, true);
        txId3 = graph.addTransaction(vxId1, vxId3, true);
        txId4 = graph.addTransaction(vxId3, vxId2, true);
        
        clusterVertexAttributeId = ClusteringConcept.VertexAttribute.HIERARCHICAL_CLUSTER.ensure(graph);
        colorVertexAttributeId = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        colorTransactionAttributeId = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        
        graph.setIntValue(clusterVertexAttributeId, vxId1, 1);
        graph.setIntValue(clusterVertexAttributeId, vxId2, 2);
        graph.setIntValue(clusterVertexAttributeId, vxId3, 1);
        graph.setIntValue(clusterVertexAttributeId, vxId4, 2);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of colorClusters method, of class ClusterUtilities.
     */
    @Test
    public void testColorClusters() {
        System.out.println("colorClusters");
        
        assertNull(graph.getObjectValue(colorVertexAttributeId, vxId1));
        assertNull(graph.getObjectValue(colorVertexAttributeId, vxId2));
        assertNull(graph.getObjectValue(colorVertexAttributeId, vxId3));
        assertNull(graph.getObjectValue(colorVertexAttributeId, vxId4));
        
        assertNull(graph.getObjectValue(colorTransactionAttributeId, txId1));
        assertNull(graph.getObjectValue(colorTransactionAttributeId, txId2));
        assertNull(graph.getObjectValue(colorTransactionAttributeId, txId3));
        assertNull(graph.getObjectValue(colorTransactionAttributeId, txId4));
        
        ClusterUtilities.colorClusters(graph, clusterVertexAttributeId, colorVertexAttributeId, colorTransactionAttributeId);
        
        final ConstellationColor clusterOneColor = ConstellationColor.fromHtmlColor("#f27979");
        final ConstellationColor clusterTwoColor = ConstellationColor.fromHtmlColor("#799cf2");
        final ConstellationColor betweenClusterColor = ConstellationColor.DARK_GREY;
        
        // Nodes in cluster 1 should be one color, nodes in cluster 2 should be another
        assertEquals(graph.getObjectValue(colorVertexAttributeId, vxId1), clusterOneColor);
        assertEquals(graph.getObjectValue(colorVertexAttributeId, vxId2), clusterTwoColor);
        assertEquals(graph.getObjectValue(colorVertexAttributeId, vxId3), clusterOneColor);
        assertEquals(graph.getObjectValue(colorVertexAttributeId, vxId4), clusterTwoColor);
        
        // transactions between nodes of the same cluster should match the cluster color.
        // transactions between nodes of different clusters should be dark grey.
        assertEquals(graph.getObjectValue(colorTransactionAttributeId, txId1), betweenClusterColor);
        assertEquals(graph.getObjectValue(colorTransactionAttributeId, txId2), clusterTwoColor);
        assertEquals(graph.getObjectValue(colorTransactionAttributeId, txId3), clusterOneColor);
        assertEquals(graph.getObjectValue(colorTransactionAttributeId, txId4), betweenClusterColor);
    }

    /**
     * Test of setColorRef method, of class ClusterUtilities.
     */
    @Test
    public void testSetColorRef() {
        System.out.println("setColorRef");
        
        int nodeColorRefAttributeId = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.get(graph);
        int transactionColorRefAttributeId = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.get(graph);
        
        assertEquals(nodeColorRefAttributeId, Graph.NOT_FOUND);
        assertEquals(transactionColorRefAttributeId, Graph.NOT_FOUND);
        
        ClusterUtilities.setColorRef(graph, "color", "color");
        
        nodeColorRefAttributeId = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.get(graph);
        transactionColorRefAttributeId = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.get(graph);
        
        assertNotEquals(nodeColorRefAttributeId, Graph.NOT_FOUND);
        assertNotEquals(transactionColorRefAttributeId, Graph.NOT_FOUND);
        
        assertEquals(graph.getStringValue(nodeColorRefAttributeId, 0), "color");
        assertEquals(graph.getStringValue(transactionColorRefAttributeId, 0), "color");
    }

    /**
     * Test of explodeGraph method, of class ClusterUtilities.
     */
    @Test
    public void testExplodeGraph() {
        System.out.println("explodeGraph");
        
        final int xVertexAttributeId = VisualConcept.VertexAttribute.X.ensure(graph);
        final int yVertexAttributeId = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int zVertexAttributeId = VisualConcept.VertexAttribute.Z.ensure(graph);
        
        graph.setFloatValue(xVertexAttributeId, vxId1, 0F);
        graph.setFloatValue(yVertexAttributeId, vxId1, 0F);
        graph.setFloatValue(zVertexAttributeId, vxId1, 0F);
        
        graph.setFloatValue(xVertexAttributeId, vxId2, 1F);
        graph.setFloatValue(yVertexAttributeId, vxId2, 1F);
        graph.setFloatValue(zVertexAttributeId, vxId2, 1F);
        
        graph.setFloatValue(xVertexAttributeId, vxId3, 2F);
        graph.setFloatValue(yVertexAttributeId, vxId3, 2F);
        graph.setFloatValue(zVertexAttributeId, vxId3, 2F);
        
        graph.setFloatValue(xVertexAttributeId, vxId4, 3F);
        graph.setFloatValue(yVertexAttributeId, vxId4, 3F);
        graph.setFloatValue(zVertexAttributeId, vxId4, 3F);
        
        int x2NodeAttributeId = VisualConcept.VertexAttribute.X2.get(graph);
        int y2NodeAttributeId = VisualConcept.VertexAttribute.Y2.get(graph);
        int z2NodeAttributeId = VisualConcept.VertexAttribute.Z2.get(graph);
        
        assertEquals(x2NodeAttributeId, Graph.NOT_FOUND);
        assertEquals(y2NodeAttributeId, Graph.NOT_FOUND);
        assertEquals(z2NodeAttributeId, Graph.NOT_FOUND);
        
        ClusterUtilities.explodeGraph(graph, clusterVertexAttributeId);
        
        x2NodeAttributeId = VisualConcept.VertexAttribute.X2.get(graph);
        y2NodeAttributeId = VisualConcept.VertexAttribute.Y2.get(graph);
        z2NodeAttributeId = VisualConcept.VertexAttribute.Z2.get(graph);
        
        assertNotEquals(x2NodeAttributeId, Graph.NOT_FOUND);
        assertNotEquals(y2NodeAttributeId, Graph.NOT_FOUND);
        assertNotEquals(z2NodeAttributeId, Graph.NOT_FOUND);
        
        assertEquals(graph.getFloatValue(x2NodeAttributeId, vxId1), 0.5F);
        assertEquals(graph.getFloatValue(y2NodeAttributeId, vxId1), 0.5F);
        assertEquals(graph.getFloatValue(z2NodeAttributeId, vxId1), 0.5F);
        
        assertEquals(graph.getFloatValue(x2NodeAttributeId, vxId2), 3.5F);
        assertEquals(graph.getFloatValue(y2NodeAttributeId, vxId2), 3.5F);
        assertEquals(graph.getFloatValue(z2NodeAttributeId, vxId2), 3.5F);
        
        assertEquals(graph.getFloatValue(x2NodeAttributeId, vxId3), 2.5F);
        assertEquals(graph.getFloatValue(y2NodeAttributeId, vxId3), 2.5F);
        assertEquals(graph.getFloatValue(z2NodeAttributeId, vxId3), 2.5F);
        
        assertEquals(graph.getFloatValue(x2NodeAttributeId, vxId4), 5.5F);
        assertEquals(graph.getFloatValue(y2NodeAttributeId, vxId4), 5.5F);
        assertEquals(graph.getFloatValue(z2NodeAttributeId, vxId4), 5.5F);
    }
}
