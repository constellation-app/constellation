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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class HierarchicalClusteringServicesNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    
    private int weightAttributeId;

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
        
        txId1 = graph.addTransaction(vxId1, vxId2, true);
        txId2 = graph.addTransaction(vxId2, vxId1, true);
        txId3 = graph.addTransaction(vxId1, vxId3, true);
        txId4 = graph.addTransaction(vxId3, vxId2, true);
        
        weightAttributeId = AnalyticConcept.TransactionAttribute.WEIGHT.ensure(graph);
        
        graph.setFloatValue(weightAttributeId, txId1, 1F);
        graph.setFloatValue(weightAttributeId, txId2, 2F);
        graph.setFloatValue(weightAttributeId, txId3, 3F);
        graph.setFloatValue(weightAttributeId, txId4, 4F);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of fastNewmanWithPendantsClusteredFinal method, of class HierarchicalClusteringServices.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testFastNewmanWithPendantsClusteredFinal() throws InterruptedException {
        System.out.println("fastNewmanWithPendantsClusteredFinal");

        int hierarchicalStateAttribute = ClusteringConcept.MetaAttribute.HIERARCHICAL_CLUSTERING_STATE.get(graph);
        assertEquals(hierarchicalStateAttribute, Graph.NOT_FOUND);
        
        HierarchicalClusteringServices.fastNewmanWithPendantsClusteredFinal(graph, new TextPluginInteraction(), false);
        
        hierarchicalStateAttribute = ClusteringConcept.MetaAttribute.HIERARCHICAL_CLUSTERING_STATE.get(graph);
        assertNotEquals(hierarchicalStateAttribute, Graph.NOT_FOUND);
        
        final HierarchicalState state = graph.getObjectValue(hierarchicalStateAttribute, 0);
        
        assertEquals(state.getStrucModificationCount(), 7);
        assertFalse(state.isInteractive());
        
        final FastNewman.Group[] groups = state.getGroups();
        // first check that the groups match the expected positions for each vertex
        assertEquals(groups[vxId1].getVertex(), vxId1);
        assertEquals(groups[vxId2].getVertex(), vxId2);
        assertEquals(groups[vxId3].getVertex(), vxId3);
        
        // vxId3 should be top of the hierarchy with others underneath it
        assertEquals(groups[vxId1].getParent().getVertex(), vxId3);
        assertEquals(groups[vxId2].getParent().getVertex(), vxId3);
        assertNull(groups[vxId3].getParent());
    }
}
