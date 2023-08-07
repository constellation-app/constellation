/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class RemoveUnusedAttributesPluginNGTest {

    GraphWriteMethods graph = new StoreGraph();
    int vertex1, vertex2, vertex3;
    int vertexAttribute1, vertexAttribute2, vertexAttribute3;
    int transaction1, transaction2;
    int transactionAttribute1, transactionAttribute2, transactionAttribute3;

    @BeforeMethod
    public void setUpMethod() {
        // adding nodes to graph
        vertex1 = graph.addVertex();
        vertex2 = graph.addVertex();
        vertex3 = graph.addVertex();
        vertexAttribute1 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "test1", "test1 desc.", null, null);
        vertexAttribute2 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "test2", "test1 desc.", null, null);
        vertexAttribute3 = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "test3", "test1 desc.", null, null);
        // adding transactions to the graph
        transaction1 = graph.addTransaction(vertex1, vertex2, true);
        transaction2 = graph.addTransaction(vertex1, vertex3, true);
        transactionAttribute1 = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "test4", "test1 desc.", null, null);
        transactionAttribute2 = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "test5", "test1 desc.", null, null);
        transactionAttribute3 = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "test6", "test1 desc.", null, null);
    }

    /**
     * Test of edit method, of class RemoveUnusedAttributesPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testAllNullAttributes() throws Exception {

        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);
        
        RemoveUnusedAttributesPlugin instance = new RemoveUnusedAttributesPlugin();
        instance.edit(graph, interaction, parameters);
        assertEquals(graph.getAttribute(GraphElementType.VERTEX, "test1"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.VERTEX, "test2"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.VERTEX, "test3"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test4"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test5"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test6"), GraphConstants.NOT_FOUND);
    }

    @Test
    public void testSomeNullAttributes() throws Exception {
        // adding values to some attributes
        graph.setStringValue(vertexAttribute2, vertex2, "some words");
        graph.setStringValue(vertexAttribute3, vertex1, "some words");
        graph.setStringValue(transactionAttribute1, transaction1, "some words");

        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);
        
        RemoveUnusedAttributesPlugin instance = new RemoveUnusedAttributesPlugin();
        instance.edit(graph, interaction, parameters);
        assertEquals(graph.getAttribute(GraphElementType.VERTEX, "test1"), GraphConstants.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.VERTEX, "test2"), GraphConstants.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.VERTEX, "test3"), GraphConstants.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test4"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test5"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test6"), GraphConstants.NOT_FOUND);
    }

    @Test
    public void testSomeNullKeyAttributes() throws Exception {
        // adding values to some attributes
        graph.setPrimaryKey(GraphElementType.VERTEX, vertexAttribute1);
        graph.setStringValue(vertexAttribute2, vertex2, "some words");
        graph.setStringValue(vertexAttribute3, vertex1, "some words");
        graph.setStringValue(transactionAttribute1, transaction1, "some words");

        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);
        
        RemoveUnusedAttributesPlugin instance = new RemoveUnusedAttributesPlugin();
        instance.edit(graph, interaction, parameters);
        assertNotEquals(graph.getAttribute(GraphElementType.VERTEX, "test1"), GraphConstants.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.VERTEX, "test2"), GraphConstants.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.VERTEX, "test3"), GraphConstants.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test4"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test5"), GraphConstants.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.TRANSACTION, "test6"), GraphConstants.NOT_FOUND);
    }

}
