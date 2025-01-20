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
package au.gov.asd.tac.constellation.graph.utilities.attribute;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Schema Attribute Utilities Test.
 *
 * @author arcturus
 */
public class AttributeUtilitiesNGTest {
    
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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getRegisteredAttributeIdsFromGraph method, of class AttributeUtilities. No nodes on graph
     */
    @Test
    public void testGetRegisteredAttributeIdsFromGraphWithZeroNodes() {
        System.out.println("getRegisteredAttributeIdsFromGraphWithZeroNodes");
        
        final Map<String, Integer> result = AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, GraphElementType.VERTEX);
        assertEquals(result, new HashMap<>());
    }

    /**
     * Test of getRegisteredAttributeIdsFromGraph method, of class AttributeUtilities. Only one node on graph
     */
    @Test
    public void testGetRegisteredAttributeIdsFromGraphWithOneNode() {
        System.out.println("getRegisteredAttributeIdsFromGraphWithOneNode");
        
        final int vx0 = graph.addVertex();
        graph.getSchema().completeVertex(graph, vx0);
        
        final Map<String, Integer> expResult = new HashMap<>();
        expResult.put(VisualConcept.VertexAttribute.LABEL.getName(), VisualConcept.VertexAttribute.LABEL.get(graph));
        expResult.put(VisualConcept.VertexAttribute.IDENTIFIER.getName(), VisualConcept.VertexAttribute.IDENTIFIER.get(graph));
        expResult.put(AnalyticConcept.VertexAttribute.RAW.getName(), AnalyticConcept.VertexAttribute.RAW.get(graph));
        expResult.put(AnalyticConcept.VertexAttribute.TYPE.getName(), AnalyticConcept.VertexAttribute.TYPE.get(graph));
        expResult.put(VisualConcept.VertexAttribute.COLOR.getName(), VisualConcept.VertexAttribute.COLOR.get(graph));
        expResult.put(VisualConcept.VertexAttribute.FOREGROUND_ICON.getName(), VisualConcept.VertexAttribute.FOREGROUND_ICON.get(graph));
        expResult.put(VisualConcept.VertexAttribute.BACKGROUND_ICON.getName(), VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph));
        expResult.put(VisualConcept.VertexAttribute.COLORBLIND_LAYER.getName(), VisualConcept.VertexAttribute.COLORBLIND_LAYER.get(graph));

        final Map<String, Integer> result = AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, GraphElementType.VERTEX);
        assertEquals(result, expResult);
    }
    
    /**
     * Test of getVertexAttributes method, of class AttributeUtilities.
     */
    @Test
    public void testGetVertexAttributes() {
        System.out.println("getVertexAttributes");
        
        final int identiferVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int typeVertexAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int sourceVertexAttribute = AnalyticConcept.VertexAttribute.SOURCE.ensure(graph);
        VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        
        final Map<String, Integer> expResult = new HashMap<>();
        expResult.put(VisualConcept.VertexAttribute.IDENTIFIER.getName(), identiferVertexAttribute);
        expResult.put(AnalyticConcept.VertexAttribute.TYPE.getName(), typeVertexAttribute);
        expResult.put(AnalyticConcept.VertexAttribute.SOURCE.getName(), sourceVertexAttribute);
        
        final Map<String, Integer> result = AttributeUtilities.getVertexAttributes(graph);
        assertEquals(result, expResult);
    }
}
