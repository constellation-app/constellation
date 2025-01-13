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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Copy Test.
 *
 * @author algol
 */
public class CopyNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    /**
     * Copy a graph from which a node has been removed.
     */
    @Test
    public void copy() {
        final String labela = "labela";
        final String labelb = "labelb";
        final String labelc = "labelc";

        final StoreGraph graph = new StoreGraph();
        final int nodeAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "nattr", "descr string", "", null);
        final int edgeAttr = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "eattr", "descr string", "", null);

        final int nodea = graph.addVertex();
        graph.setStringValue(nodeAttr, nodea, labela);
        final int nodeb = graph.addVertex();
        graph.setStringValue(nodeAttr, nodeb, labelb);
        final int nodec = graph.addVertex();
        graph.setStringValue(nodeAttr, nodec, labelc);

        final int edge0 = graph.addTransaction(nodea, nodeb, true);
        graph.setStringValue(edgeAttr, edge0, labela);
        final int edge1 = graph.addTransaction(nodeb, nodec, true);
        graph.setStringValue(edgeAttr, edge1, labelb);

        graph.removeVertex(nodea);

        final GraphReadMethods copy = graph.copy();

        assertEquals("Nodes copied", 2, copy.getVertexCount());
        assertEquals("Edges copied", 1, copy.getTransactionCount());

        // nodeb and nodec should be in the copy with the original labels.
        final int nodeAttrCopy = copy.getAttribute(GraphElementType.VERTEX, "nattr");
        assertEquals("Copy node b", labelb, copy.getStringValue(nodeAttrCopy, nodeb));
        assertEquals("Copy node c", labelc, copy.getStringValue(nodeAttrCopy, nodec));

        // edge1 should be in the copy with the original graph, going between the copies of nodeb and nodec.
        final int edgeAttrCopy = copy.getAttribute(GraphElementType.TRANSACTION, "eattr");
        assertEquals("Copy edge", labelb, copy.getStringValue(edgeAttrCopy, edge1));
        assertEquals("Copy edge from", nodeb, graph.getTransactionSourceVertex(edge1));
        assertEquals("Copy edge to", nodec, graph.getTransactionDestinationVertex(edge1));
    }

    @Test
    public void testCopyDoesNotModifyOriginal() {
        final StoreGraph graph = new StoreGraph();
        final int nameAttribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "", "", null);

        final int vx0 = graph.addVertex();
        graph.setStringValue(nameAttribute, vx0, "hello");

        final GraphReadMethods copy = graph.copy();

        assertEquals(graph.getStringValue(nameAttribute, vx0), copy.getStringValue(nameAttribute, vx0));

        GraphWriteMethods writableCopy = ((GraphWriteMethods) copy);
        assertEquals(graph.getStringValue(nameAttribute, vx0), writableCopy.getStringValue(nameAttribute, vx0));

        writableCopy.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name2", "", "", null);

        writableCopy.setStringValue(nameAttribute, vx0, "good bye");

        assertNotSame(graph.getStringValue(nameAttribute, vx0), writableCopy.getStringValue(nameAttribute, vx0));
    }
}
