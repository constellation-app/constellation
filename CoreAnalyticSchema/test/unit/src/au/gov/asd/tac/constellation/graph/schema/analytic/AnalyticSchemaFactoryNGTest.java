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
package au.gov.asd.tac.constellation.graph.schema.analytic;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory.AnalyticSchema;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
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
public class AnalyticSchemaFactoryNGTest {
    
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
     * Test of getRegisteredConcepts method, of class AnalyticSchemaFactory.
     */
    @Test
    public void testGetRegisteredConcepts() {
        System.out.println("getRegisteredConcepts");
        
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Set<Class<? extends SchemaConcept>> result = instance.getRegisteredConcepts();
        assertEquals(result.size(), 3);
        assertTrue(result.contains(SchemaConcept.ConstellationViewsConcept.class));
        assertTrue(result.contains(VisualConcept.class));
        assertTrue(result.contains(AnalyticConcept.class));
    }

    /**
     * Test of getKeyAttributes method, of class AnalyticSchemaFactory.
     */
    @Test
    public void testGetKeyAttributes() {
        System.out.println("getKeyAttributes");
        
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        
        final List<SchemaAttribute> expNodeResult = Arrays.asList(VisualConcept.VertexAttribute.IDENTIFIER,
                AnalyticConcept.VertexAttribute.TYPE);
        final List<SchemaAttribute> nodeResult = instance.getKeyAttributes(GraphElementType.VERTEX);
        assertEquals(nodeResult, expNodeResult);
        
        final List<SchemaAttribute> expTransactionResult = Arrays.asList(VisualConcept.TransactionAttribute.IDENTIFIER,
                AnalyticConcept.TransactionAttribute.TYPE, TemporalConcept.TransactionAttribute.DATETIME);
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
     * Test of newGraph method, of class AnalyticSchemaFactory.AnalyticSchema.
     */
    @Test
    public void testNewGraph() {
        System.out.println("newGraph");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        assertEquals(graph.getAttributeCount(GraphElementType.VERTEX), 0);
        assertEquals(graph.getAttributeCount(GraphElementType.TRANSACTION), 0);
        assertEquals(graph.getAttributeCount(GraphElementType.GRAPH), 0);
        
        schema.newGraph(graph);
        
        assertEquals(graph.getAttributeCount(GraphElementType.VERTEX), 22);
        assertEquals(graph.getAttributeCount(GraphElementType.TRANSACTION), 16);
        assertEquals(graph.getAttributeCount(GraphElementType.GRAPH), 22);
    }
    
    /**
     * Test of newVertex method, of class AnalyticSchemaFactory.AnalyticSchema.
     */
    @Test
    public void testNewVertex() {
        System.out.println("newVertex");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vertexSourceAttribute = AnalyticConcept.VertexAttribute.SOURCE.ensure(graph);
        
        assertNull(graph.getStringValue(vertexIdentifierAttribute, vxId));
        assertNull(graph.getStringValue(vertexLabelAttribute, vxId));
        assertNull(graph.getStringValue(vertexColorAttribute, vxId));
        assertNull(graph.getStringValue(vertexTypeAttribute, vxId));
        assertNull(graph.getStringValue(vertexSourceAttribute, vxId));
        
        schema.newVertex(graph, vxId);
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "Vertex #0");
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "Vertex #0<Unknown>");
        // color generated will be random so only checking that it does now have a value
        assertNotNull(graph.getStringValue(vertexColorAttribute, vxId));
        assertEquals(graph.getObjectValue(vertexTypeAttribute, vxId), SchemaVertexType.unknownType());
        assertEquals(graph.getStringValue(vertexSourceAttribute, vxId), "Manually Created");
    }
    
    /**
     * Test of completeVertex method, of class AnalyticSchemaFactory.AnalyticSchema. Setting Identifier
     */
    @Test
    public void testCompleteVertexIdentifier() {
        System.out.println("completeVertexIdentifier");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vertexRawAttribute = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
        final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vertexBackgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int vertexForegroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);
        final int vertexCountryAttribute = SpatialConcept.VertexAttribute.COUNTRY.ensure(graph);
        
        graph.setStringValue(vertexIdentifierAttribute, vxId, "my vertex");
        graph.setStringValue(vertexCountryAttribute, vxId, "Au");
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "my vertex");
        assertNull(graph.getStringValue(vertexLabelAttribute, vxId));
        assertNull(graph.getObjectValue(vertexTypeAttribute, vxId));
        assertEquals(graph.getObjectValue(vertexRawAttribute, vxId), new RawData(null, null));
        assertNull(graph.getObjectValue(vertexColorAttribute, vxId));
        assertEquals(graph.getStringValue(vertexBackgroundIconAttribute, vxId), "Background.Flat Square");
        assertEquals(graph.getStringValue(vertexForegroundIconAttribute, vxId), "");
        assertEquals(graph.getStringValue(vertexCountryAttribute, vxId), "Au");
        
        schema.completeVertex(graph, vxId);
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "my vertex");
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "my vertex<Unknown>");
        assertNull(graph.getObjectValue(vertexTypeAttribute, vxId));
        assertEquals(graph.getObjectValue(vertexRawAttribute, vxId), new RawData("my vertex", "Unknown"));
        assertEquals(graph.getObjectValue(vertexColorAttribute, vxId), ConstellationColor.GREY);
        assertEquals(graph.getStringValue(vertexBackgroundIconAttribute, vxId), "Background.Flat Square");
        assertEquals(graph.getStringValue(vertexForegroundIconAttribute, vxId), "Unknown");
        // Country digraph should autocomplete
        assertEquals(graph.getStringValue(vertexCountryAttribute, vxId), "Australia");
    }
    
    /**
     * Test of completeVertex method, of class AnalyticSchemaFactory.AnalyticSchema. Setting Raw
     */
    @Test
    public void testCompleteVertexRaw() {
        System.out.println("completeVertexRaw");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vertexRawAttribute = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
        final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vertexBackgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int vertexForegroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);
        
        graph.setObjectValue(vertexRawAttribute, vxId, new RawData("my vertex", "Person"));
        
        assertNull(graph.getStringValue(vertexIdentifierAttribute, vxId));
        assertNull(graph.getStringValue(vertexLabelAttribute, vxId));
        assertNull(graph.getObjectValue(vertexTypeAttribute, vxId));
        assertEquals(graph.getObjectValue(vertexRawAttribute, vxId), new RawData("my vertex", "Person"));
        assertNull(graph.getObjectValue(vertexColorAttribute, vxId));
        assertEquals(graph.getStringValue(vertexBackgroundIconAttribute, vxId), "Background.Flat Square");
        assertEquals(graph.getStringValue(vertexForegroundIconAttribute, vxId), "");
        
        schema.completeVertex(graph, vxId);
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "my vertex");
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "my vertex<Person>");
        assertEquals(graph.getObjectValue(vertexTypeAttribute, vxId), AnalyticConcept.VertexType.PERSON);
        assertEquals(graph.getObjectValue(vertexRawAttribute, vxId), new RawData("my vertex", "Person"));
        assertEquals(graph.getObjectValue(vertexColorAttribute, vxId), ConstellationColor.AMETHYST);
        assertEquals(graph.getStringValue(vertexBackgroundIconAttribute, vxId), "Background.Flat Square");
        assertEquals(graph.getStringValue(vertexForegroundIconAttribute, vxId), "Person.Person");
    }
    
    /**
     * Test of completeVertex method, of class AnalyticSchemaFactory.AnalyticSchema. Setting Label
     */
    @Test
    public void testCompleteVertexLabel() {
        System.out.println("completeVertexLabel");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId = graph.addVertex();
        
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int vertexRawAttribute = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
        final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vertexBackgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int vertexForegroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);
        
        graph.setStringValue(vertexLabelAttribute, vxId, "my vertex<Event>");
        
        assertNull(graph.getStringValue(vertexIdentifierAttribute, vxId));
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "my vertex<Event>");
        assertNull(graph.getObjectValue(vertexTypeAttribute, vxId));
        assertEquals(graph.getObjectValue(vertexRawAttribute, vxId), new RawData(null, null));
        assertNull(graph.getObjectValue(vertexColorAttribute, vxId));
        assertEquals(graph.getStringValue(vertexBackgroundIconAttribute, vxId), "Background.Flat Square");
        assertEquals(graph.getStringValue(vertexForegroundIconAttribute, vxId), "");
        
        schema.completeVertex(graph, vxId);
        
        assertEquals(graph.getStringValue(vertexIdentifierAttribute, vxId), "my vertex");
        assertEquals(graph.getStringValue(vertexLabelAttribute, vxId), "my vertex<Event>");
        assertEquals(graph.getObjectValue(vertexTypeAttribute, vxId), AnalyticConcept.VertexType.EVENT);
        assertEquals(graph.getObjectValue(vertexRawAttribute, vxId), new RawData("my vertex", "Event"));
        assertEquals(graph.getObjectValue(vertexColorAttribute, vxId), ConstellationColor.PEACH);
        assertEquals(graph.getStringValue(vertexBackgroundIconAttribute, vxId), "Background.Flat Square");
        assertEquals(graph.getStringValue(vertexForegroundIconAttribute, vxId), "Miscellaneous.Signal");
    }
    
    /**
     * Test of resolveVertexType method, of class AnalyticSchemaFactory.AnalyticSchema.
     */
    @Test
    public void testResolveVertexType() {
        System.out.println("resolveVertexType");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final AnalyticSchema schema = (AnalyticSchema) instance.createSchema();
        
        final SchemaVertexType caseSensitiveResult = SchemaVertexType.unknownType().copy().rename("person");
        final SchemaVertexType transactionTypeResult = SchemaVertexType.unknownType().copy().rename("Correlation");
        final SchemaVertexType notATypeResult = SchemaVertexType.unknownType().copy().rename("Fake");
        
        assertEquals(schema.resolveVertexType("Person"), AnalyticConcept.VertexType.PERSON);
        // type should be case sensitive
        assertEquals(schema.resolveVertexType("person"), caseSensitiveResult);
        // shouldn't resolve to transaction types
        assertEquals(schema.resolveVertexType("Correlation"), transactionTypeResult);
        // shouldn't resolve otherwise made up types
        assertEquals(schema.resolveVertexType("Fake"), notATypeResult);
    }
    
    /**
     * Test of newTransaction method, of class AnalyticSchemaFactory.AnalyticSchema.
     */
    @Test
    public void testNewTransaction() {
        System.out.println("newTransaction");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int tId = graph.addTransaction(vxId1, vxId2, true);
        
        final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
        final int transactionColorAttribute = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionSourceAttribute = AnalyticConcept.TransactionAttribute.SOURCE.ensure(graph);
        final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);
        
        assertNull(graph.getStringValue(transactionIdentifierAttribute, tId));
        assertNull(graph.getStringValue(transactionLabelAttribute, tId));
        assertNull(graph.getStringValue(transactionColorAttribute, tId));
        assertNull(graph.getObjectValue(transactionTypeAttribute, tId));
        assertNull(graph.getStringValue(transactionSourceAttribute, tId));
        assertFalse(graph.getBooleanValue(transactionDirectedAttribute, tId));
        
        schema.newTransaction(graph, tId);
        
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "Transaction #0");
        assertEquals(graph.getStringValue(transactionLabelAttribute, tId), "Unknown");
        // color generated will be random so only checking that it does now have a value
        assertNotNull(graph.getStringValue(transactionColorAttribute, tId));
        assertEquals(graph.getObjectValue(transactionTypeAttribute, tId), SchemaTransactionType.unknownType());
        assertEquals(graph.getStringValue(transactionSourceAttribute, tId), "Manually Created");
        assertTrue(graph.getBooleanValue(transactionDirectedAttribute, tId));
    }
    
    /**
     * Test of completeTransaction method, of class AnalyticSchemaFactory.AnalyticSchema. Setting identifier
     */
    @Test
    public void testCompleteTransactionIdentifier() {
        System.out.println("completeTransactionIdentifier");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        int tId = graph.addTransaction(vxId1, vxId2, true);
        
        final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
        final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionColorAttribute = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        final int transactionStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(graph);
        final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);
        
        graph.setStringValue(transactionIdentifierAttribute, tId, "my transaction");
        
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "my transaction");
        assertNull(graph.getStringValue(transactionLabelAttribute, tId));
        assertNull(graph.getObjectValue(transactionTypeAttribute, tId));
        assertNull(graph.getObjectValue(transactionColorAttribute, tId));
        assertEquals(graph.getObjectValue(transactionStyleAttribute, tId), LineStyle.SOLID);
        assertFalse(graph.getBooleanValue(transactionDirectedAttribute, tId));
        
        schema.completeTransaction(graph, tId);
        
        // note that normally the directed attribute would be set in the newTransaction function 
        // so there wouldn't normally be a case of changing direction unless directed was directly edited (rather than simply being different to default)
        // changing the direction of the transaction will result in a "copy" being created with the new direction
        // so this transaction id shouldn't exist anymore, but there should still be the same number of transactions as before
        assertFalse(graph.transactionExists(tId));
        assertEquals(graph.getTransactionCount(), 1);
        // since there is only one transaction, we can safely assume it is at position 0
        tId = graph.getTransaction(0);
        
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "my transaction");
        assertEquals(graph.getStringValue(transactionLabelAttribute, tId), "Unknown");
        assertNull(graph.getObjectValue(transactionTypeAttribute, tId));
        assertEquals(graph.getObjectValue(transactionColorAttribute, tId), ConstellationColor.CLOUDS);
        assertEquals(graph.getObjectValue(transactionStyleAttribute, tId), LineStyle.SOLID);
        assertFalse(graph.getBooleanValue(transactionDirectedAttribute, tId));
    }
    
    /**
     * Test of completeTransaction method, of class AnalyticSchemaFactory.AnalyticSchema. Setting label
     */
    @Test
    public void testCompleteTransactionLabel() {
        System.out.println("completeTransactionLabel");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final Schema schema = instance.createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int tId = graph.addTransaction(vxId1, vxId2, false);
        
        final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
        final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionColorAttribute = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        final int transactionStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(graph);
        final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);
        
        graph.setStringValue(transactionLabelAttribute, tId, "my transaction<Correlation>");
        
        assertNull(graph.getStringValue(transactionIdentifierAttribute, tId));
        assertEquals(graph.getStringValue(transactionLabelAttribute, tId), "my transaction<Correlation>");
        assertNull(graph.getObjectValue(transactionTypeAttribute, tId));
        assertNull(graph.getObjectValue(transactionColorAttribute, tId));
        assertEquals(graph.getObjectValue(transactionStyleAttribute, tId), LineStyle.SOLID);
        assertFalse(graph.getBooleanValue(transactionDirectedAttribute, tId));
        
        schema.completeTransaction(graph, tId);
        
        assertEquals(graph.getStringValue(transactionIdentifierAttribute, tId), "my transaction");
        assertEquals(graph.getStringValue(transactionLabelAttribute, tId), "Correlation");
        assertEquals(graph.getObjectValue(transactionTypeAttribute, tId), AnalyticConcept.TransactionType.CORRELATION);
        assertEquals(graph.getObjectValue(transactionColorAttribute, tId), ConstellationColor.AZURE);
        assertEquals(graph.getObjectValue(transactionStyleAttribute, tId), LineStyle.SOLID);
        assertFalse(graph.getBooleanValue(transactionDirectedAttribute, tId));
    }
    
    /**
     * Test of resolveTransactionType method, of class AnalyticSchemaFactory.AnalyticSchema.
     */
    @Test
    public void testResolveTransactionType() {
        System.out.println("resolveTransactionType");
               
        final AnalyticSchemaFactory instance = new AnalyticSchemaFactory();
        final AnalyticSchema schema = (AnalyticSchema) instance.createSchema();
        
        final SchemaTransactionType caseSensitiveResult = SchemaTransactionType.unknownType().copy().rename("correlation");
        final SchemaTransactionType nodeTypeResult = SchemaTransactionType.unknownType().copy().rename("Person");
        final SchemaTransactionType notATypeResult = SchemaTransactionType.unknownType().copy().rename("Fake");
        
        assertEquals(schema.resolveTransactionType("Correlation"), AnalyticConcept.TransactionType.CORRELATION);
        // type should be case sensitive
        assertEquals(schema.resolveTransactionType("correlation"), caseSensitiveResult);
        // shouldn't resolve to vertex types
        assertEquals(schema.resolveTransactionType("Person"), nodeTypeResult);
        // shouldn't resolve otherwise made up types
        assertEquals(schema.resolveTransactionType("Fake"), notATypeResult);
    }
}
