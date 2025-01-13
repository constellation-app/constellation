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
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class RawIOProviderNGTest {
    
    private StoreGraph graph;
    
    private int vxId;
    
    private int rawVertexAttribute;
    
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
        
        vxId = graph.addVertex();
        
        rawVertexAttribute = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of readObject method, of class RawIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("readObject");
        
        final ImmutableObjectCache cache = new ImmutableObjectCache();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode mainNode = mapper.createObjectNode();
        
        final ObjectNode identifierNode = mainNode.putObject("identifier");
        identifierNode.put("rawIdentifier", "myIdentifier");
        identifierNode.putNull("rawType");
        
        final ObjectNode typeNode = mainNode.putObject("type");
        typeNode.putNull("rawIdentifier");
        typeNode.put("rawType", "myType");
        
        mainNode.putNull("null");
        
        assertEquals(graph.getObjectValue(rawVertexAttribute, vxId), new RawData(null, null));
        
        final RawIOProvider instance = new RawIOProvider();
               
        instance.readObject(rawVertexAttribute, vxId, mainNode.get("identifier"), graph, null, null, null, cache);
        assertEquals(graph.getObjectValue(rawVertexAttribute, vxId), new RawData("myIdentifier", null));
        
        instance.readObject(rawVertexAttribute, vxId, mainNode.get("type"), graph, null, null, null, cache);
        assertEquals(graph.getObjectValue(rawVertexAttribute, vxId), new RawData(null, "myType"));
        
        instance.readObject(rawVertexAttribute, vxId, mainNode.get("null"), graph, null, null, null, cache);
        assertEquals(graph.getObjectValue(rawVertexAttribute, vxId), new RawData(null, null));
    }

    /**
     * Test of writeObject method, of class RawIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("writeObject");
        
        final GraphAttribute rawAttribute = new GraphAttribute(graph, rawVertexAttribute);
        
        graph.setObjectValue(rawVertexAttribute, vxId, new RawData("myIdentifier", "myType"));
        
        final RawIOProvider instance = new RawIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(rawAttribute, vxId, jsonGenerator, graph, null, true);
            jsonGenerator.close();
                        
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectFieldStart("Raw");
            jsonGenerator.writeStringField("rawIdentifier", "myIdentifier");
            jsonGenerator.writeStringField("rawType", "myType");
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
    
    /**
     * Test of writeObject method, of class RawIOProvider. Raw attribute value is default. Not verbose
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObjectDefaultValueNoVerbose() throws IOException {
        System.out.println("writeObjectDefaultValueNoVerbose");
        
        final GraphAttribute rawAttribute = new GraphAttribute(graph, rawVertexAttribute);
        
        final RawIOProvider instance = new RawIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(rawAttribute, vxId, jsonGenerator, graph, null, false);
            jsonGenerator.close();
                        
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
}
