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
public class VertexTypeIOProviderNGTest {
    
    private StoreGraph graph;
    
    private int vxId1;
    private int vxId2;
    
    private int typeVertexAttribute;
    
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
        
        typeVertexAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of readObject method, of class VertexTypeIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("readObject");
        
        final ImmutableObjectCache cache = new ImmutableObjectCache();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode mainNode = mapper.createObjectNode();
        
        final ObjectNode typeObject = mainNode.putObject("typeObject");
        typeObject.put("Name", "MD5 Hash");
        typeObject.put("Description", "A node representing an MD5 hash");
        typeObject.put("Foreground Icon", "Security.MD5");
        typeObject.put("Background Icon", "Background.Flat Square");
        typeObject.put("Detection Regex", "[0-9a-fA-F]{32}");
        typeObject.put("Validation Regex", "^[0-9a-f]{32}$");
        typeObject.put("Incomplete", false);
        final ObjectNode typeColor = typeObject.putObject("Color");
        typeColor.put("name", "Cyan");
        typeObject.putObject("Properties");
        
        final ObjectNode typeSuperType = typeObject.putObject("Super Type");
        typeSuperType.put("Name", "Hash");
        typeSuperType.put("Description", "A node representing a hash");
        typeSuperType.put("Foreground Icon", "Character.Hash");
        typeSuperType.put("Background Icon", "Background.Flat Square");
        typeSuperType.put("Incomplete", false);
        final ObjectNode superTypeColor = typeSuperType.putObject("Color");
        superTypeColor.put("name", "Cyan");
        typeSuperType.putObject("Properties");
        
        mainNode.put("typeString", "MD5 Hash");
        
        assertNull(graph.getObjectValue(typeVertexAttribute, vxId1));
        assertNull(graph.getObjectValue(typeVertexAttribute, vxId2));
        
        final VertexTypeIOProvider instance = new VertexTypeIOProvider();
        instance.readObject(typeVertexAttribute, vxId1, mainNode.get("typeObject"), graph, null, null, null, cache);
        instance.readObject(typeVertexAttribute, vxId2, mainNode.get("typeString"), graph, null, null, null, cache);
        
        assertEquals(graph.getObjectValue(typeVertexAttribute, vxId1), AnalyticConcept.VertexType.MD5);
        assertEquals(graph.getObjectValue(typeVertexAttribute, vxId2), AnalyticConcept.VertexType.MD5);
    }
    
    /**
     * Test of writeObject method, of class VertexTypeIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("writeObject");
        
        final GraphAttribute typeAttribute = new GraphAttribute(graph, typeVertexAttribute);
        
        graph.setObjectValue(typeVertexAttribute, vxId1, AnalyticConcept.VertexType.MD5);
        
        final VertexTypeIOProvider instance = new VertexTypeIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(typeAttribute, vxId1, jsonGenerator, graph, null, true);
            jsonGenerator.close();
            
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectFieldStart("Type");
            jsonGenerator.writeStringField("Name", "MD5 Hash");
            jsonGenerator.writeStringField("Description", "A node representing an MD5 hash");
            
            jsonGenerator.writeObjectFieldStart("Color");
            jsonGenerator.writeStringField("name", "Cyan");
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeStringField("Foreground Icon", "Security.MD5");          
            jsonGenerator.writeStringField("Background Icon", "Background.Flat Square");
            jsonGenerator.writeStringField("Detection Regex", "[0-9a-fA-F]{32}");
            jsonGenerator.writeStringField("Validation Regex", "^[0-9a-f]{32}$");
            
            jsonGenerator.writeObjectFieldStart("Super Type");
            jsonGenerator.writeStringField("Name", "Hash");
            jsonGenerator.writeStringField("Description", "A node representing a hash");
            
            jsonGenerator.writeObjectFieldStart("Color");
            jsonGenerator.writeStringField("name", "Cyan");
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeStringField("Foreground Icon", "Character.Hash");          
            jsonGenerator.writeStringField("Background Icon", "Background.Flat Square");
            
            jsonGenerator.writeObjectFieldStart("Properties");
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeBooleanField("Incomplete", false);
            
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeObjectFieldStart("Properties");
            jsonGenerator.writeEndObject();
            
            jsonGenerator.writeBooleanField("Incomplete", false);
            
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
    
    /**
     * Test of writeObject method, of class VertexTypeIOProvider. Value set to default null, verbose true.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObjectNullValue() throws IOException {
        System.out.println("writeObjectNullValue");
        
        final GraphAttribute typeAttribute = new GraphAttribute(graph, typeVertexAttribute);
        
        final VertexTypeIOProvider instance = new VertexTypeIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(typeAttribute, vxId1, jsonGenerator, graph, null, true);
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
     * Test of writeObject method, of class VertexTypeIOProvider. Value set to default null, verbose false.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObjectNullValueNoVerbose() throws IOException {
        System.out.println("writeObjectNullValue");
        
        final GraphAttribute typeAttribute = new GraphAttribute(graph, typeVertexAttribute);
        
        final VertexTypeIOProvider instance = new VertexTypeIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(typeAttribute, vxId1, jsonGenerator, graph, null, false);
            jsonGenerator.close();
            
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
}
