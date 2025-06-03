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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.io;

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class TransactionTypeIOProviderNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    
    private int tId1;
    private int tId2;
    
    private int typeTransactionAttribute;
    
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
        
        tId1 = graph.addTransaction(vxId1, vxId2, true);
        tId2 = graph.addTransaction(vxId2, vxId1, true);
        
        typeTransactionAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

        /**
     * Test of readObject method, of class TransactionTypeIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("readObject");
        
        final ImmutableObjectCache cache = new ImmutableObjectCache();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode mainNode = mapper.createObjectNode();
        
        final ObjectNode typeObject = mainNode.putObject("typeObject");
        typeObject.put("Name", "Correlation");
        typeObject.put("Description", "A transaction representing a two entities which are part of the same larger entity, eg. a person is correlated to their online identifier");
        typeObject.put("Style", "SOLID");
        typeObject.put("Directed", false);
        typeObject.put("Incomplete", false);
        final ObjectNode typeColor = typeObject.putObject("Color");
        typeColor.put("name", "Azure");
        typeObject.putObject("Properties");
        
        mainNode.put("typeString", "Correlation");
        
        assertNull(graph.getObjectValue(typeTransactionAttribute, tId1));
        assertNull(graph.getObjectValue(typeTransactionAttribute, tId2));
        
        final TransactionTypeIOProvider instance = new TransactionTypeIOProvider();
        instance.readObject(typeTransactionAttribute, tId1, mainNode.get("typeObject"), graph, null, null, null, cache);
        instance.readObject(typeTransactionAttribute, tId2, mainNode.get("typeString"), graph, null, null, null, cache);
        
        assertEquals(graph.getObjectValue(typeTransactionAttribute, tId1), AnalyticConcept.TransactionType.CORRELATION);
        assertEquals(graph.getObjectValue(typeTransactionAttribute, tId2), AnalyticConcept.TransactionType.CORRELATION);
    }
    
    /**
     * Test of writeObject method, of class TransactionTypeIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("writeObject");
        
        final GraphAttribute typeAttribute = new GraphAttribute(graph, typeTransactionAttribute);
        
        graph.setObjectValue(typeTransactionAttribute, tId1, AnalyticConcept.TransactionType.CORRELATION);
        
        final TransactionTypeIOProvider instance = new TransactionTypeIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(typeAttribute, tId1, jsonGenerator, graph, null, true);
            jsonGenerator.close();
            
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectFieldStart("Type");
            jsonGenerator.writeStringField("Name", "Correlation");
            jsonGenerator.writeStringField("Description", "A transaction representing a two entities which are part of the same larger entity, eg. a person is correlated to their online identifier");
            
            jsonGenerator.writeObjectFieldStart("Color");
            jsonGenerator.writeStringField("name", "Azure");
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeStringField("Style", "SOLID");          
            jsonGenerator.writeBooleanField("Directed", false);
            
            jsonGenerator.writeObjectFieldStart("Properties");
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeBooleanField("Incomplete", false);
            
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
    
    /**
     * Test of writeObject method, of class TransactionTypeIOProvider. Value set to default null, verbose true.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObjectNullValue() throws IOException {
        System.out.println("writeObjectNullValue");
        
        final GraphAttribute typeAttribute = new GraphAttribute(graph, typeTransactionAttribute);
        
        final TransactionTypeIOProvider instance = new TransactionTypeIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(typeAttribute, tId1, jsonGenerator, graph, null, true);
            jsonGenerator.close();
            
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNullField("Type");
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
    
    /**
     * Test of writeObject method, of class TransactionTypeIOProvider. Value set to default null, verbose false.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObjectNullValueNoVerbose() throws IOException {
        System.out.println("writeObjectNullValue");
        
        final GraphAttribute typeAttribute = new GraphAttribute(graph, typeTransactionAttribute);
        
        final TransactionTypeIOProvider instance = new TransactionTypeIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(typeAttribute, tId1, jsonGenerator, graph, null, false);
            jsonGenerator.close();
            
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
}
