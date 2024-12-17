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
public class CompositeNodeStateIOProviderNGTest {
    
    private final String compositeNodeString = "{\"nodeId\":1,\"expandedState\":null,\"contractedState\":{\"constituentNodeStore\":\"[{\\\"source.Identifier\\\":\\\"Vertex #0\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #0>Type<Unknown>\\\"},{\\\"source.Identifier\\\":\\\"Vertex #1\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #1>Type<Unknown>\\\"}]\",\"expandedIds\":[\"copy.Identifier<Vertex #0>Type<Unknown>\",\"copy.Identifier<Vertex #1>Type<Unknown>\"],\"affectedExpandedIds\":[\"copy.Identifier<Vertex #0>Type<Unknown>\",\"copy.Identifier<Vertex #1>Type<Unknown>\"],\"mean\":[0.0,1.0,0.5]}}";
    
    private StoreGraph graph;
    
    private int vxId;
    
    private int compositeStateVertexAttribute;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
        
        vxId = graph.addVertex();
        
        compositeStateVertexAttribute = AnalyticConcept.VertexAttribute.COMPOSITE_STATE.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    /**
     * Test of readObject method, of class CompositeNodeStateIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testReadObject() throws IOException {
        System.out.println("readObject");
        
        final ImmutableObjectCache cache = new ImmutableObjectCache();
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode compositeStateNode = mapper.createObjectNode();
        compositeStateNode.put("composite_state", compositeNodeString);
        
        assertNull(graph.getStringValue(compositeStateVertexAttribute, vxId));
        
        final CompositeNodeStateIOProvider instance = new CompositeNodeStateIOProvider();
        instance.readObject(compositeStateVertexAttribute, vxId, compositeStateNode.get("composite_state"), graph, null, null, null, cache);
        
        assertEquals(graph.getStringValue(compositeStateVertexAttribute, vxId), compositeNodeString);
    }

    /**
     * Test of writeObject method, of class CompositeNodeStateIOProvider.
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObject() throws IOException {
        System.out.println("writeObject");
        
        final GraphAttribute compositeStateAttribute = new GraphAttribute(graph, compositeStateVertexAttribute);
        
        final CompositeNodeStateIOProvider instance = new CompositeNodeStateIOProvider();
        
        graph.setStringValue(compositeStateVertexAttribute, vxId, compositeNodeString);
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(compositeStateAttribute, vxId, jsonGenerator, graph, null, true);
            jsonGenerator.close();
                        
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("composite_state", compositeNodeString);
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
    
    /**
     * Test of writeObject method, of class CompositeNodeStateIOProvider. Composite state attribute value is default. Not verbose
     * @throws java.io.IOException
     */
    @Test
    public void testWriteObjectDefaultValueNoVerbose() throws IOException {
        System.out.println("writeObject");
        
        final GraphAttribute compositeStateAttribute = new GraphAttribute(graph, compositeStateVertexAttribute);
        
        final CompositeNodeStateIOProvider instance = new CompositeNodeStateIOProvider();
        
        try (final ByteArrayOutputStream actual = new ByteArrayOutputStream();
                final ByteArrayOutputStream expected = new ByteArrayOutputStream()) {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(actual, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            instance.writeObject(compositeStateAttribute, vxId, jsonGenerator, graph, null, false);
            jsonGenerator.close();
                        
            jsonGenerator = new JsonFactory().createGenerator(expected, JsonEncoding.UTF8);
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
            jsonGenerator.close();
            
            assertEquals(actual.toString(), expected.toString());
        }
    }
}
