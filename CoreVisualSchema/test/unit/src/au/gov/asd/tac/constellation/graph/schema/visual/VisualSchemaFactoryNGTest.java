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
package au.gov.asd.tac.constellation.graph.schema.visual;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept.ConstellationViewsConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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
public class VisualSchemaFactoryNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getRegisteredConcepts method, of class VisualSchemaFactory.
     */
    @Test
    public void testGetRegisteredConcepts() {
        System.out.println("getRegisteredConcepts");
        
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        final Set<Class<? extends SchemaConcept>> result = instance.getRegisteredConcepts();
        assertEquals(result.size(), 2);
        assertTrue(result.contains(ConstellationViewsConcept.class));
        assertTrue(result.contains(VisualConcept.class));
    }

    /**
     * Test of getKeyAttributes method, of class VisualSchemaFactory.
     */
    @Test
    public void testGetKeyAttributes() {
        System.out.println("getKeyAttributes");
        
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        
        final List<SchemaAttribute> expNodeResult = Arrays.asList(VisualConcept.VertexAttribute.IDENTIFIER);
        final List<SchemaAttribute> nodeResult = instance.getKeyAttributes(GraphElementType.VERTEX);
        assertEquals(nodeResult, expNodeResult);
        
        final List<SchemaAttribute> expTransactionResult = Arrays.asList(VisualConcept.TransactionAttribute.IDENTIFIER);
        final List<SchemaAttribute> transactionResult = instance.getKeyAttributes(GraphElementType.TRANSACTION);
        assertEquals(transactionResult, expTransactionResult);
        
        // no other element types should return anything
        final List<SchemaAttribute> edgeResult = instance.getKeyAttributes(GraphElementType.EDGE);
        final List<SchemaAttribute> linkResult = instance.getKeyAttributes(GraphElementType.LINK);
        final List<SchemaAttribute> graphResult = instance.getKeyAttributes(GraphElementType.GRAPH);
        final List<SchemaAttribute> metaResult = instance.getKeyAttributes(GraphElementType.META);
        assertEquals(edgeResult, Collections.emptyList());
        assertEquals(linkResult, Collections.emptyList());
        assertEquals(graphResult, Collections.emptyList());
        assertEquals(metaResult, Collections.emptyList());
    }

    /**
     * Test of getBottomLabels method, of class VisualSchemaFactory.
     */
    @Test
    public void testGetBottomLabels() {
        System.out.println("getBottomLabels");
        
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        
        // default should just display node label attribute in white
        final GraphLabel expLabel = new GraphLabel(VisualConcept.VertexAttribute.LABEL.getName(), ConstellationColor.WHITE);
        final GraphLabels result = instance.getBottomLabels();
        assertEquals(result.getNumberOfLabels(), 1);
        assertEquals(result.getLabels().get(0), expLabel);
    }

    /**
     * Test of getVertexTopLabels method, of class VisualSchemaFactory.
     */
    @Test
    public void testGetVertexTopLabels() {
        System.out.println("getVertexTopLabels");
        
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        
        // default should be any node attributes listed as being labels
        // for the Visual schema, there are currently no such attributes
        final GraphLabels result = instance.getVertexTopLabels();
        assertEquals(result.getNumberOfLabels(), 0);
    }

    /**
     * Test of getTransactionLabels method, of class VisualSchemaFactory.
     */
    @Test
    public void testGetTransactionLabels() {
        System.out.println("getTransactionLabels");
        
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        
        // default should be any transaction attributes listed as being labels
        // for the Visual schema, there are currently no such attributes
        final GraphLabels result = instance.getTransactionLabels();
        assertEquals(result.getNumberOfLabels(), 0);
    }

    /**
     * Test of getDecorators method, of class VisualSchemaFactory.
     */
    @Test
    public void testGetDecorators() {
        System.out.println("getDecorators");
        
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        
        // default should be any node attributes listed as being decorators
        // for the Visual schema, there is currently only one such attribute
        final VertexDecorators result = instance.getDecorators();
        assertEquals(result.getNorthWestDecoratorAttribute(), "pinned");
        assertNull(result.getNorthEastDecoratorAttribute());
        assertNull(result.getSouthEastDecoratorAttribute());
        assertNull(result.getSouthWestDecoratorAttribute());
    }

    /**
     * Test of newGraph method, of class VisualSchemaFactory.VisualSchema.
     */
    @Test
    public void testNewGraph() {
        System.out.println("newGraph");
               
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        assertEquals(graph.getAttributeCount(GraphElementType.VERTEX), 0);
        assertEquals(graph.getAttributeCount(GraphElementType.TRANSACTION), 0);
        assertEquals(graph.getAttributeCount(GraphElementType.GRAPH), 0);
        
        schema.newGraph(graph);
        
        assertEquals(graph.getAttributeCount(GraphElementType.VERTEX), 19);
        assertEquals(graph.getAttributeCount(GraphElementType.TRANSACTION), 12);
        assertEquals(graph.getAttributeCount(GraphElementType.GRAPH), 22);
    }
    
    /**
     * Test of newVertex method, of class VisualSchemaFactory.VisualSchema.
     */
    @Test
    public void testNewVertex() {
        System.out.println("newVertex");
               
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        
        assertNull(graph.getStringValue(vertexIdentifierAttribute, vxId));
        assertNull(graph.getStringValue(vertexLabelAttribute, vxId));
        assertNull(graph.getStringValue(vertexColorAttribute, vxId));
        
        schema.newVertex(graph, vxId);
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "Vertex #0");
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "Vertex #0");
        // color generated will be random so only checking that it does now have a value
        assertNotNull(graph.getStringValue(vertexColorAttribute, vxId));
    }
    
    /**
     * Test of completeVertex method, of class VisualSchemaFactory.VisualSchema.
     */
    @Test
    public void testCompleteVertex() {
        System.out.println("completeVertex");
               
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        
        graph.setStringValue(vertexIdentifierAttribute, vxId, "my vertex");
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "my vertex");
        assertNull(graph.getStringValue(vertexLabelAttribute, vxId));
        
        schema.completeVertex(graph, vxId);
        
        // label should become the identifier
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "my vertex");
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "my vertex");
    }
    
    /**
     * Test of newTransaction method, of class VisualSchemaFactory.VisualSchema.
     */
    @Test
    public void testNewTransaction() {
        System.out.println("newTransaction");
               
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int tId = graph.addTransaction(vxId1, vxId2, true);
        
        final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
        final int transactionColorAttribute = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        
        assertNull(graph.getStringValue(transactionIdentifierAttribute, tId));
        assertNull(graph.getStringValue(transactionLabelAttribute, tId));
        assertNull(graph.getStringValue(transactionColorAttribute, tId));
        
        schema.newTransaction(graph, tId);
        
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "Transaction #0");
        assertEquals(graph.getStringValue(transactionLabelAttribute, tId), "Transaction #0");
        // color generated will be random so only checking that it does now have a value
        assertNotNull(graph.getStringValue(transactionColorAttribute, tId));
    }
    
    /**
     * Test of completeTransaction method, of class VisualSchemaFactory.VisualSchema.
     */
    @Test
    public void testCompleteTransaction() {
        System.out.println("completeTransaction");
               
        final VisualSchemaFactory instance = new VisualSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        int tId = graph.addTransaction(vxId1, vxId2, true);
        
        final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
        
        graph.setStringValue(transactionIdentifierAttribute, tId, "my transaction");
        
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "my transaction");
        assertNull(graph.getStringValue(transactionLabelAttribute, tId));
        
        schema.completeTransaction(graph, tId);
        
        // note that normally the directed attribute would be set in the newTransaction function 
        // so there wouldn't normally be a case of changing direction unless directed was directly edited (rather than simply being different to default)
        // changing the direction of the transaction will result in a "copy" being created with the new direction
        // so this transaction id shouldn't exist anymore, but there should still be the same number of transactions as before
        assertFalse(graph.transactionExists(tId));
        assertEquals(graph.getTransactionCount(), 1);
        // since there is only one transaction, we can safely assume it is at position 0
        tId = graph.getTransaction(0);
        
        // label should become the identifier
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "my transaction");
        assertEquals(graph.getStringValue(transactionLabelAttribute, tId), "my transaction");
    }
}
