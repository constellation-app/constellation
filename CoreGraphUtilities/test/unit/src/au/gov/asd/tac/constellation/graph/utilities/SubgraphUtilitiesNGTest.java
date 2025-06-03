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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Subgraph Utilities Test.
 *
 * @author arcturus
 */
public class SubgraphUtilitiesNGTest {
    
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
        graph = new StoreGraph();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of copyGraph method, of class SubgraphUtilities.
     */
    @Test
    public void testCopyGraphWithTransaction() {
        System.out.println("copyGraphWithTransaction");
        
        final String vxAttribute = "Name";
        final String vxValue = "Foo";
        final String txAttribute = "Name";
        final String txValue = "Bar";
        
        final int vx0 = graph.addVertex();
        final int vx1 = graph.addVertex();
        final int tx0 = graph.addTransaction(vx0, vx1, true);
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, vxAttribute, "", "", null);
        final int txAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, txAttribute, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, txAttr);
        graph.setStringValue(vxAttr, vx0, vxValue);
        graph.setStringValue(txAttr, tx0, txValue);

        assertEquals(graph.getVertexCount(), 2);
        assertEquals(graph.getTransactionCount(), 1);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(graph.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), vxValue);
        assertEquals(graph.getStringValue(txAttr, tx0), txValue);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, vxAttribute);
        final int txAttrCopy = copy.getAttribute(GraphElementType.TRANSACTION, txAttribute);

        assertEquals(copy.getVertexCount(), 2);
        assertEquals(copy.getTransactionCount(), 1);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(copy.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), vxValue);
        assertEquals(copy.getStringValue(txAttrCopy, tx0), txValue);
    }

    /**
     * Test of copyGraph method, of class SubgraphUtilities. No transactions
     */
    @Test
    public void testCopyGraphWithoutTransaction() {
        System.out.println("copyGraphWithoutTransaction");
        
        final String vxAttribute = "Name";
        final String vxValue = "Foo";
        
        final int vx0 = graph.addVertex();
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, vxAttribute, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);

        graph.setStringValue(vxAttr, vx0, vxValue);

        assertEquals(graph.getVertexCount(), 1);
        assertEquals(graph.getTransactionCount(), 0);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), vxValue);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, vxAttribute);

        assertEquals(copy.getVertexCount(), 1);
        assertEquals(copy.getTransactionCount(), 0);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), vxValue);
    }

    /**
     * Test of copyGraph method, of class SubgraphUtilities. No transactions but have transaction attribute.
     */
    @Test
    public void testCopyGraphWithoutTransactionButWithTransactionAttribute() {
        System.out.println("copyGraphWithoutTransactionButWithTransactionAttribute");
        
        final String vxAttribute = "Name";
        final String vxValue = "Foo";
        final String txAttribute = "Name";
        
        final int vx0 = graph.addVertex();
        final int vxAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, vxAttribute, "", "", null);
        final int txAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, txAttribute, "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, vxAttr);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, txAttr);

        graph.setStringValue(vxAttr, vx0, vxValue);

        assertEquals(graph.getVertexCount(), 1);
        assertEquals(graph.getTransactionCount(), 0);
        assertTrue(graph.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(graph.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(graph.getStringValue(vxAttr, vx0), vxValue);

        final StoreGraph copy = SubgraphUtilities.copyGraph(graph);
        final int vxAttrCopy = copy.getAttribute(GraphElementType.VERTEX, vxAttribute);

        assertEquals(copy.getVertexCount(), 1);
        assertEquals(copy.getTransactionCount(), 0);
        assertTrue(copy.getAttribute(GraphElementType.VERTEX, vxAttribute) != Graph.NOT_FOUND);
        assertTrue(copy.getAttribute(GraphElementType.TRANSACTION, txAttribute) != Graph.NOT_FOUND);
        assertEquals(copy.getStringValue(vxAttrCopy, vx0), vxValue);
    }
    
    /**
     * Test of getTransactionTypeSubgraph method, of class SubgraphUtilities.
     */
    @Test
    public void testGetTransactionTypeSubgraph() {
        System.out.println("getTransactionTypeSubgraph");
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        final int vxId4 = graph.addVertex();
        
        final int txId1 = graph.addTransaction(vxId1, vxId2, true);
        final int txId2 = graph.addTransaction(vxId1, vxId3, true);
        final int txId3 = graph.addTransaction(vxId3, vxId1, true);
        final int txId4 = graph.addTransaction(vxId2, vxId4, true);
        
        final int typeTransactionAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        
        graph.setObjectValue(typeTransactionAttribute, txId1, AnalyticConcept.TransactionType.BEHAVIOUR);
        graph.setObjectValue(typeTransactionAttribute, txId2, AnalyticConcept.TransactionType.BEHAVIOUR);
        graph.setObjectValue(typeTransactionAttribute, txId3, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setObjectValue(typeTransactionAttribute, txId4, AnalyticConcept.TransactionType.COMMUNICATION);
        
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getTransactionCount(), 4);
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        
        final Set<SchemaTransactionType> subgraphTypes = new HashSet<>();
        subgraphTypes.add(AnalyticConcept.TransactionType.BEHAVIOUR);
        
        final StoreGraph inclusiveSubgraph = SubgraphUtilities.getTransactionTypeSubgraph(graph, schema, subgraphTypes, false);
        
        // this subgraph should only contain behaviour transactions and attached nodes
        assertEquals(inclusiveSubgraph.getVertexCount(), 3);
        assertEquals(inclusiveSubgraph.getTransactionCount(), 2);
        
        final StoreGraph exclusiveSubgraph = SubgraphUtilities.getTransactionTypeSubgraph(graph, schema, subgraphTypes, true);
        
        // this subgraph should only contain non-behaviour transactions and attached nodes
        assertEquals(exclusiveSubgraph.getVertexCount(), 4);
        assertEquals(exclusiveSubgraph.getTransactionCount(), 2);
    }
}
