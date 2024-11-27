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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.assertEquals;
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
public class AnalyticSchemaV2UpdateProviderNGTest {
    
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
     * Test of schemaUpdate method, of class AnalyticSchemaV2UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("schemaUpdate");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int oldRawVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, "string", "Raw", null, null, null);
        final int oldTypeVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, "string", "Type", null, null, null);
        final int oldTypeTransactionAttribute = graph.addAttribute(GraphElementType.TRANSACTION, "string", "Type", null, null, null);
        final int identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int tId = graph.addTransaction(vxId1, vxId2, true);
        
        graph.setStringValue(oldRawVertexAttribute, vxId1, "Some raw data");
        graph.setStringValue(oldRawVertexAttribute, vxId2, "Something else");
        graph.setStringValue(oldTypeVertexAttribute, vxId1, "Person");
        graph.setStringValue(oldTypeVertexAttribute, vxId2, "Event");
        graph.setStringValue(oldTypeTransactionAttribute, tId, "Correlation");
        
        int rawVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Raw");
        int typeVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Type");
        int typeTransactionAttribute = graph.getAttribute(GraphElementType.TRANSACTION, "Type");
        
        assertEquals(graph.getAttributeType(rawVertexAttribute), "string");
        assertEquals(graph.getAttributeType(typeVertexAttribute), "string");
        assertEquals(graph.getAttributeType(typeTransactionAttribute), "string");
        
        assertNull(graph.getStringValue(identifierVertexAttribute, vxId1));
        assertNull(graph.getStringValue(identifierVertexAttribute, vxId2));
        
        final AnalyticSchemaV2UpdateProvider instance = new AnalyticSchemaV2UpdateProvider();
        instance.schemaUpdate(graph);
        
        rawVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Raw");
        typeVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Type");
        typeTransactionAttribute = graph.getAttribute(GraphElementType.TRANSACTION, "Type");
        
        assertEquals(graph.getAttributeType(rawVertexAttribute), "raw");
        assertEquals(graph.getAttributeType(typeVertexAttribute), "vertex_type");
        assertEquals(graph.getAttributeType(typeTransactionAttribute), "transaction_type");
        
        assertEquals(((RawData) graph.getObjectValue(rawVertexAttribute, vxId1)).getRawIdentifier(), "Some raw data");
        assertEquals(((RawData) graph.getObjectValue(rawVertexAttribute, vxId1)).getRawType(), "Person");
        assertEquals((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId1), AnalyticConcept.VertexType.PERSON);
        assertEquals(((RawData) graph.getObjectValue(rawVertexAttribute, vxId2)).getRawIdentifier(), "Something else");
        assertEquals(((RawData) graph.getObjectValue(rawVertexAttribute, vxId2)).getRawType(), "Event");
        assertEquals((SchemaVertexType) graph.getObjectValue(typeVertexAttribute, vxId2), AnalyticConcept.VertexType.EVENT);
        assertEquals((SchemaTransactionType) graph.getObjectValue(typeTransactionAttribute, tId), AnalyticConcept.TransactionType.CORRELATION);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, vxId1), "Some raw data");
        assertEquals(graph.getStringValue(identifierVertexAttribute, vxId2), "Something else");
    }   
}
