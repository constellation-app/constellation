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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
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
public class KTrussNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    private int vxId3;
    private int vxId4;
    private int vxId5;
    private int vxId6;
    private int vxId7;
    
    private int txId1;
    private int txId2;
    private int txId3;
    private int txId4;
    private int txId5;
    private int txId6;
    private int txId7;
    private int txId8;
    private int txId9;
    private int txId10;
    
    private int kTrussVertexAttribute;
    private int kTrussTransactionAttribute;

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
        vxId5 = graph.addVertex();
        vxId6 = graph.addVertex();
        vxId7 = graph.addVertex();
        
        txId1 = graph.addTransaction(vxId1, vxId2, true);
        txId2 = graph.addTransaction(vxId1, vxId3, true);
        txId3 = graph.addTransaction(vxId2, vxId3, true);
        txId4 = graph.addTransaction(vxId3, vxId4, true);
        txId5 = graph.addTransaction(vxId5, vxId4, true);
        txId6 = graph.addTransaction(vxId5, vxId6, true);
        txId7 = graph.addTransaction(vxId6, vxId7, true);
        txId8 = graph.addTransaction(vxId4, vxId7, true);
        txId8 = graph.addTransaction(vxId4, vxId6, true);
        txId8 = graph.addTransaction(vxId5, vxId7, true);
        
        kTrussVertexAttribute = ClusteringConcept.VertexAttribute.K_TRUSS_CLUSTER.ensure(graph);
        kTrussTransactionAttribute = ClusteringConcept.TransactionAttribute.K_TRUSS_CLUSTER.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of recordVertexCluster method, of class KTruss.KTrussPluginResultHandler.
     */
    @Test
    public void testRecordVertexCluster() {
        System.out.println("recordVertexCluster");
        
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId1), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId2), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId3), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId4), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId5), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId6), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId7), 0);
        
        final KTruss.KTrussResultHandler instance = new KTruss.KTrussPluginResultHandler(graph, false);
        instance.recordVertexCluster(vxId4, 2);
        
        // only vxId4 should change
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId1), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId2), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId3), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId4), 2);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId5), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId6), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId7), 0);
    }
    
    /**
     * Test of recordTransactionCluster method, of class KTruss.KTrussPluginResultHandler.
     */
    @Test
    public void testRecordTransactionCluster() {
        System.out.println("recordTransactionCluster");
        
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId1), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId2), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId3), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId4), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId5), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId6), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId7), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId8), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId9), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId10), 0);
        
        final KTruss.KTrussResultHandler instance = new KTruss.KTrussPluginResultHandler(graph, false);
        instance.recordTransactionCluster(txId7, 1);
        
        // only txId5 should change
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId1), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId2), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId3), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId4), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId5), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId6), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId7), 1);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId8), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId9), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId10), 0);
    }
    
    /**
     * Test of run method, of class KTruss.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId1), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId2), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId3), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId4), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId5), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId6), 0);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId7), 0);
        
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId1), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId2), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId3), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId4), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId5), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId6), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId7), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId8), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId9), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId10), 0);
        
        final KTruss.KTrussResultHandler resultHandler = new KTruss.KTrussPluginResultHandler(graph, false);
        KTruss.run(graph, resultHandler);
        
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId1), 3);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId2), 3);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId3), 3);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId4), 4);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId5), 4);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId6), 4);
        assertEquals(graph.getIntValue(kTrussVertexAttribute, vxId7), 4);
        
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId1), 3);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId2), 3);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId3), 3);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId4), 0);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId5), 4);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId6), 4);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId7), 4);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId8), 4);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId9), 3);
        assertEquals(graph.getIntValue(kTrussTransactionAttribute, txId10), 3);
    }
}
