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

import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Attribute Definition Test.
 * <p>
 * Unit tests for create/editing/deletion of attribute for elements.
 *
 * @author algol
 */
public class AttributeDefinitionNGTest {

    public AttributeDefinitionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of addAttribute method, of class StoreGraph.
     */
    @Test
    public void addAttribute() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        final int newix = graph.addAttribute(elementType, attributeType, name, null, null, null);
        assertTrue("New attribute has index>=0", newix >= 0);
    }

    /**
     * Test of addAttribute method, of class StoreGraph.
     */
    @Test
    public void addAttributeBad() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "_node";
        final GraphWriteMethods graph = new StoreGraph();
        int newix = graph.addAttribute(elementType, StringAttributeDescription.ATTRIBUTE_NAME, name, null, null, null);
        assertTrue("New attribute has index < 0", newix <= 0);
    }

    @Test
    public void renameAttribute() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "anOldName";
        final GraphWriteMethods graph = new StoreGraph();
        int newAttrId = graph.addAttribute(elementType, StringAttributeDescription.ATTRIBUTE_NAME, name, null, null, null);
        assertTrue("New attribute has index >= 0", newAttrId >= 0);

        graph.updateAttributeName(newAttrId, "aNewName");

        int attrId = graph.getAttribute(GraphElementType.VERTEX, "aNewName");
        assertEquals("new attribute name:", newAttrId, attrId);
    }

    @Test
    public void changeAttributeDescription() {
        final String name = "aField";
        final GraphWriteMethods graph = new StoreGraph();

        int newAttrId = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, name, "no description", null, null);
        assertTrue("New attribute has index < 0", newAttrId >= 0);

        Attribute attr = new GraphAttribute(graph, newAttrId);
        String value = attr.getDescription();
        assertEquals("Check description:", value, "no description");
        graph.updateAttributeDescription(newAttrId, "new description");

        int attrId = graph.getAttribute(GraphElementType.VERTEX, name);
        Attribute attr2 = new GraphAttribute(graph, attrId);
        assertEquals("Check description:", attr2.getDescription(), "new description");
    }

    @Test
    public void changeAttributeDefault() {
        final String name = "aField";
        final GraphWriteMethods graph = new StoreGraph();

        int newAttrId = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, name, "no description", "nothing", null);
        assertTrue("New attribute has index < 0", newAttrId >= 0);

        Attribute attr = new GraphAttribute(graph, newAttrId);
        String value = (String) (attr.getDefaultValue());
        assertEquals("Check default value:", value, "nothing");
        graph.updateAttributeDefaultValue(newAttrId, "new default");

        int attrId = graph.getAttribute(GraphElementType.VERTEX, name);
        Attribute attr2 = new GraphAttribute(graph, attrId);
        assertEquals("Check default value:", (String) (attr2.getDefaultValue()), "new default");
    }

    /**
     * Test of addAttribute method, of class StoreGraph.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addAttributeTwice() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        graph.addAttribute(elementType, attributeType, name, null, null, null);
        graph.addAttribute(elementType, IntegerAttributeDescription.ATTRIBUTE_NAME, name, null, null, null);
        fail("Shouldn't be able to add attribute twice");
    }

    /**
     * Test of getAttributeIndex method, of class StoreGraph.
     */
    @Test
    public void getAttributeIndex() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        final int newid = graph.addAttribute(elementType, attributeType, name, null, null, null);
        final int id = graph.getAttribute(elementType, name);
        assertEquals("Retrieved index equals added index", newid, id);
    }

    @Test
    public void getUnknownAttributeIndex() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        graph.addAttribute(elementType, attributeType, name, null, null, null);
        final int id = graph.getAttribute(elementType, "node2");
        assertEquals("Getting unknown attribute", Graph.NOT_FOUND, id);
    }

    /**
     * Test of getAttributeIndex method, of class StoreGraph.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createAttributeTypeUnknown() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "arraylist";
        final GraphWriteMethods graph = new StoreGraph();
        graph.addAttribute(elementType, attributeType, name, null, null, null);
        fail("Shouldn't be able to create an unknown type " + attributeType);
    }

    /**
     * Test of getAttributeIndex method, of class StoreGraph.
     */
    @Test(expectedExceptions = ArrayIndexOutOfBoundsException.class)
    public void getAttributeTypeIndexOutOfBounds() {
        final GraphWriteMethods graph = new StoreGraph();
        graph.getAttribute(GraphElementType.VERTEX, 99);
        fail("Shouldn't be able to get an out-of-bounds attribute");
    }

    @Test
    public void emptyGraphNoAttributes() {
        final GraphWriteMethods graph = new StoreGraph();

        final int gcount = graph.getAttributeCount(GraphElementType.GRAPH);
        assertEquals("Number of graph attributes", 0, gcount);

        final int vcount = graph.getAttributeCount(GraphElementType.VERTEX);
        assertEquals("Number of vertex attributes", 0, vcount);

        final int lcount = graph.getAttributeCount(GraphElementType.LINK);
        assertEquals("Number of link attributes", 0, lcount);

        final int ecount = graph.getAttributeCount(GraphElementType.EDGE);
        assertEquals("Number of edge attributes", 0, ecount);

        final int tcount = graph.getAttributeCount(GraphElementType.TRANSACTION);
        assertEquals("Number of transaction attributes", 0, tcount);
    }

    /**
     * Test of removeAttribute method, of class StoreGraph.
     */
    @Test
    public void removeAttribute() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        final int id = graph.addAttribute(elementType, attributeType, name, null, null, null);
        graph.removeAttribute(id);
        final int vcount = graph.getAttributeCount(GraphElementType.VERTEX);
        assertEquals("Number of vertex attributes", 0, vcount);
    }

    /**
     * Test of removeAttribute method, of class StoreGraph.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void removeAttributeTwice() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        final int id1 = graph.addAttribute(elementType, attributeType, name + "1", null, null, null);
        final int id2 = graph.addAttribute(elementType, attributeType, name + "2", null, null, null);
        graph.removeAttribute(id1);
        graph.removeAttribute(id1);
        fail("Shouldn't be able to remove attribute twice");
    }

    /**
     * Test of getAttributeNames method, of class StoreGraph.
     */
    @Test
    public void getAttributeNames() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        final int NATTRS = 10;
        final int[] ids = new int[NATTRS];
        for (int i = 0; i < NATTRS; i++) {
            ids[i] = graph.addAttribute(elementType, attributeType, name + SeparatorConstants.UNDERSCORE + i, null, null, null);
        }

        for (int i = 0; i < NATTRS; i++) {
            final String namei = name + SeparatorConstants.UNDERSCORE + i;
            final Attribute attr = new GraphAttribute(graph, ids[i]);
            if (!attr.getName().equals(namei)) {
                fail("Can't find name '" + namei + "' in returned names.");
            }
        }
    }
}
