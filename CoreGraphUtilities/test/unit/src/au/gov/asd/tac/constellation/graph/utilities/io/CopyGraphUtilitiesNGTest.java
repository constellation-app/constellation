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
package au.gov.asd.tac.constellation.graph.utilities.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Copy Graph Utilities Test
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class CopyGraphUtilitiesNGTest extends ConstellationTest {

    private static final String META_ATTRIBUTE_1_LABEL = "meta1 label";
    private static final String META_ATTRIBUTE_2_LABEL = "meta2 label";
    private static final String GRAPH_ATTRIBUTE_1_LABEL = "graph1 label";
    private static final String GRAPH_ATTRIBUTE_2_LABEL = "graph2 label";
    private static final String VERTEX_ATTRIBUTE_1_LABEL = "vertex1 label";
    private static final String VERTEX_ATTRIBUTE_2_LABEL = "vertex2 label";
    private static final String TRANSACTION_ATTRIBUTE_1_LABEL = "transaction1 label";
    private static final String TRANSACTION_ATTRIBUTE_2_LABEL = "transaction2 label";
    private static final String LINK_ATTRIBUTE_1_LABEL = "link1 label";
    private static final String LINK_ATTRIBUTE_2_LABEL = "link2 label";
    private static final String EDGE_ATTRIBUTE_1_LABEL = "edge1 label";
    private static final String EDGE_ATTRIBUTE_2_LABEL = "edge2 label";

    StoreGraph source;
    int metaAttribute1, graphAttribute1, vertexAttribute1, transactionAttribute1, linkAttribute1, edgeAttribute1;
    int metaAttribute2, graphAttribute2, vertexAttribute2, transactionAttribute2, linkAttribute2, edgeAttribute2;
    int vx0, vx1, tx0, tx1, ed0, ed1, lk0;

    public CopyGraphUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        source = new StoreGraph();

        // make the attributes
        metaAttribute1 = source.addAttribute(GraphElementType.META, StringAttributeDescription.ATTRIBUTE_NAME, META_ATTRIBUTE_1_LABEL, "meta1 description", "meta1 default", null);
        metaAttribute2 = source.addAttribute(GraphElementType.META, StringAttributeDescription.ATTRIBUTE_NAME, META_ATTRIBUTE_2_LABEL, "meta2 description", "meta2 default", null);
        graphAttribute1 = source.addAttribute(GraphElementType.GRAPH, StringAttributeDescription.ATTRIBUTE_NAME, GRAPH_ATTRIBUTE_1_LABEL, "graph1 description", "graph1 default", null);
        graphAttribute2 = source.addAttribute(GraphElementType.GRAPH, StringAttributeDescription.ATTRIBUTE_NAME, GRAPH_ATTRIBUTE_2_LABEL, "graph2 description", "graph2 default", null);
        vertexAttribute1 = source.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, VERTEX_ATTRIBUTE_1_LABEL, "vertex1 description", "vertex1 default", null);
        vertexAttribute2 = source.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, VERTEX_ATTRIBUTE_2_LABEL, "vertex2 description", "vertex2 default", null);
        transactionAttribute1 = source.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, TRANSACTION_ATTRIBUTE_1_LABEL, "transaction1 description", "transaction1 default", null);
        transactionAttribute2 = source.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, TRANSACTION_ATTRIBUTE_2_LABEL, "transaction2 description", "transaction2 default", null);
        linkAttribute1 = source.addAttribute(GraphElementType.LINK, StringAttributeDescription.ATTRIBUTE_NAME, LINK_ATTRIBUTE_1_LABEL, "link1 description", "link1 default", null);
        linkAttribute2 = source.addAttribute(GraphElementType.LINK, StringAttributeDescription.ATTRIBUTE_NAME, LINK_ATTRIBUTE_2_LABEL, "link2 description", "link2 default", null);
        edgeAttribute1 = source.addAttribute(GraphElementType.EDGE, StringAttributeDescription.ATTRIBUTE_NAME, EDGE_ATTRIBUTE_1_LABEL, "edge1 description", "edge1 default", null);
        edgeAttribute2 = source.addAttribute(GraphElementType.EDGE, StringAttributeDescription.ATTRIBUTE_NAME, EDGE_ATTRIBUTE_2_LABEL, "edge2 description", "edge2 default", null);

        // add 2 nodes and transactions
        vx0 = source.addVertex();
        vx1 = source.addVertex();
        tx0 = source.addTransaction(vx0, vx1, true);
        tx1 = source.addTransaction(vx1, vx0, true);
        ed0 = source.getEdge(0);
        ed1 = source.getEdge(1);
        lk0 = source.getLink(0);

        // set a value for the 2nd attribute for each type
        source.setStringValue(metaAttribute2, 0, "meta2 custom");
        source.setStringValue(graphAttribute2, 0, "graph2 custom");
        source.setStringValue(vertexAttribute2, vx0, "vertex2 custom vx0");
        source.setStringValue(vertexAttribute2, vx1, "vertex2 custom vx1");
        source.setStringValue(transactionAttribute2, tx0, "transaction2 custom tx0");
        source.setStringValue(transactionAttribute2, tx1, "transaction2 custom tx1");
        source.setStringValue(edgeAttribute2, ed0, "edge2 custom ed0");
        source.setStringValue(edgeAttribute2, ed1, "edge2 custom ed1");
        source.setStringValue(linkAttribute2, lk0, "link2 custom lk0");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of copyPrimaryKeys method, of class CopyGraphUtilities.
     */
    @Test
    public void testCopyPrimaryKeys() {
        source.setPrimaryKey(GraphElementType.VERTEX, vertexAttribute2);

        final StoreGraph graph = new StoreGraph();
        final GraphElementType elementType = GraphElementType.VERTEX;
        CopyGraphUtilities.copyPrimaryKeys(source, graph, elementType);

        System.out.println(Arrays.toString(graph.getPrimaryKey(elementType)));
        System.out.println(Arrays.toString(source.getPrimaryKey(elementType)));

        final String originalKeyName = source.getAttributeName(source.getPrimaryKey(elementType)[0]);
        final String newKeyName = graph.getAttributeName(graph.getPrimaryKey(elementType)[0]);

        Assert.assertEquals(newKeyName, originalKeyName);
    }

    /**
     * Test of copyGraphTypeElements method, of class CopyGraphUtilities.
     */
    @Test
    public void testCopyGraphTypeElements() {
        final GraphWriteMethods graph = new StoreGraph();
        final GraphElementType type = GraphElementType.GRAPH;
        CopyGraphUtilities.copyGraphTypeElements(source, graph);

        // copied and the default value copied correctly
        final int graphAttribute1Id = graph.getAttribute(type, GRAPH_ATTRIBUTE_1_LABEL);
        final String graphAttribute1Value = graph.getStringValue(graphAttribute1Id, 0);
        Assert.assertEquals(graphAttribute1Value, "graph1 default");

        // copied and the custom value copied correctly
        final int graphAttribute2Id = graph.getAttribute(type, GRAPH_ATTRIBUTE_2_LABEL);
        final String graphAttribute2Value = graph.getStringValue(graphAttribute2Id, 0);
        Assert.assertEquals(graphAttribute2Value, "graph2 custom");
    }

    /**
     * Test of copyGraphToGraph method, of class CopyGraphUtilities.
     */
    @Test
    public void testCopyGraphToGraph() {
        final GraphWriteMethods graph = new StoreGraph();
        final boolean copyAll = true;
        CopyGraphUtilities.copyGraphToGraph(source, graph, copyAll);

        final int metaAttribute1Id = graph.getAttribute(GraphElementType.META, META_ATTRIBUTE_1_LABEL);
        final int metaAttribute2Id = graph.getAttribute(GraphElementType.META, META_ATTRIBUTE_2_LABEL);
        final int graphAttribute1Id = graph.getAttribute(GraphElementType.GRAPH, GRAPH_ATTRIBUTE_1_LABEL);
        final int graphAttribute2Id = graph.getAttribute(GraphElementType.GRAPH, GRAPH_ATTRIBUTE_2_LABEL);
        final int vertexAttribute1Id = graph.getAttribute(GraphElementType.VERTEX, VERTEX_ATTRIBUTE_1_LABEL);
        final int vertexAttribute2Id = graph.getAttribute(GraphElementType.VERTEX, VERTEX_ATTRIBUTE_2_LABEL);
        final int transactionAttribute1Id = graph.getAttribute(GraphElementType.TRANSACTION, TRANSACTION_ATTRIBUTE_1_LABEL);
        final int transactionAttribute2Id = graph.getAttribute(GraphElementType.TRANSACTION, TRANSACTION_ATTRIBUTE_2_LABEL);
        final int edgeAttribute1Id = graph.getAttribute(GraphElementType.EDGE, EDGE_ATTRIBUTE_1_LABEL);
        final int edgeAttribute2Id = graph.getAttribute(GraphElementType.EDGE, EDGE_ATTRIBUTE_2_LABEL);
        final int linkAttribute1Id = graph.getAttribute(GraphElementType.LINK, LINK_ATTRIBUTE_1_LABEL);
        final int linkAttribute2Id = graph.getAttribute(GraphElementType.LINK, LINK_ATTRIBUTE_2_LABEL);

        // not going to copy meta attributes
        Assert.assertEquals(graph.getStringValue(metaAttribute1Id, 0), "meta1 default");
        Assert.assertEquals(graph.getStringValue(metaAttribute2Id, 0), "meta2 default");

        // everything else will be copied
        Assert.assertEquals(graph.getStringValue(graphAttribute1Id, 0), "graph1 default");
        Assert.assertEquals(graph.getStringValue(graphAttribute2Id, 0), "graph2 custom");
        Assert.assertEquals(graph.getStringValue(vertexAttribute1Id, vx0), "vertex1 default");
        Assert.assertEquals(graph.getStringValue(vertexAttribute2Id, vx0), "vertex2 custom vx0");
        Assert.assertEquals(graph.getStringValue(vertexAttribute1Id, vx1), "vertex1 default");
        Assert.assertEquals(graph.getStringValue(vertexAttribute2Id, vx1), "vertex2 custom vx1");
        Assert.assertEquals(graph.getStringValue(transactionAttribute1Id, tx0), "transaction1 default");
        Assert.assertEquals(graph.getStringValue(transactionAttribute2Id, tx0), "transaction2 custom tx0");
        Assert.assertEquals(graph.getStringValue(transactionAttribute1Id, tx1), "transaction1 default");
        Assert.assertEquals(graph.getStringValue(transactionAttribute2Id, tx1), "transaction2 custom tx1");
        Assert.assertEquals(graph.getStringValue(edgeAttribute1Id, ed0), "edge1 default");
        Assert.assertEquals(graph.getStringValue(edgeAttribute2Id, ed0), "edge2 custom ed0");
        Assert.assertEquals(graph.getStringValue(edgeAttribute1Id, ed1), "edge1 default");
        Assert.assertEquals(graph.getStringValue(edgeAttribute2Id, ed1), "edge2 custom ed1");
        Assert.assertEquals(graph.getStringValue(linkAttribute1Id, lk0), "link1 default");
        Assert.assertEquals(graph.getStringValue(linkAttribute2Id, lk0), "link2 custom lk0");
    }
}
