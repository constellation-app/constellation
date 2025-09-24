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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Map;
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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();       
        graph = new StoreGraph(schema);
        
        VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        VisualConcept.VertexAttribute.LABEL.ensure(graph);
        VisualConcept.VertexAttribute.COLOR.ensure(graph);
        graph.addAttribute(GraphElementType.VERTEX, "string", "MyCustom", "", "", null);
        VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        VisualConcept.TransactionAttribute.COLOR.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getRegisteredAttributeIdsFromGraph method, of class AttributeUtilities.
     */
    @Test
    public void testGetRegisteredAttributeIdsFromGraph() {
        System.out.println("getRegisteredAttributeIdsFromGraph");
        
        final Map<String, Integer> result1 = AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, GraphElementType.VERTEX);
        assertEquals(result1.size(), 3);
        assertEquals(result1.get("Identifier"), Integer.valueOf(0));
        assertEquals(result1.get("Label"), Integer.valueOf(1));
        assertEquals(result1.get("color"), Integer.valueOf(2));
        // MyCustom attribute isn't a registered attribute of the Visual Schema (it's a custom attribute)
        // therefore this shouldn't be included
        assertNull(result1.get("MyCustom"));
        
        final Map<String, Integer> result2 = AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, GraphElementType.TRANSACTION);
        assertEquals(result2.size(), 2);
        // These values should be different to their node counterparts since they are different attributes
        assertEquals(result2.get("Identifier"), Integer.valueOf(4));
        assertEquals(result2.get("color"), Integer.valueOf(5));
        assertNull(result2.get("Label"));
    }

    /**
     * Test of getVertexAttributes method, of class AttributeUtilities.
     */
    @Test
    public void testGetVertexAttributes() {
        System.out.println("getVertexAttributes");
        
        final Map<String, Integer> result = AttributeUtilities.getVertexAttributes(graph);
        assertEquals(result.size(), 4);
        assertEquals(result.get("Identifier"), Integer.valueOf(0));
        assertEquals(result.get("Label"), Integer.valueOf(1));
        assertEquals(result.get("color"), Integer.valueOf(2));
        // unlike getRegisteredAttributeIdsFromGraph, this function will return custom attributes
        assertEquals(result.get("MyCustom"), Integer.valueOf(3));
    }  
}
