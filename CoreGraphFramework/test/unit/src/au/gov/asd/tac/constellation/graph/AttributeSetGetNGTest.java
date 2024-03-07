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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Attrivute Set Get Test
 *
 * @author algol
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class AttributeSetGetNGTest extends ConstellationTest {

    public AttributeSetGetNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of set method, of class StoreGraph.
     */
    @Test
    public void setGetString() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "string";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final String VALUE1 = "plugh";
        final String VALUE2 = "xyzzy";
        final int id1 = graph.addVertex();
        graph.setStringValue(attr, id1, VALUE1);
        final int id2 = graph.addVertex();
        graph.setStringValue(attr, id2, VALUE2);

        assertEquals("Retrieve string value", VALUE1, graph.getStringValue(attr, id1));
        assertEquals("Retrieve string value", VALUE2, graph.getStringValue(attr, id2));
    }

    @Test
    public void setGetFloat() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "float";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final float VALUE1 = 17;
        final float VALUE2 = 43;
        final int id1 = graph.addVertex();
        graph.setFloatValue(attr, id1, VALUE1);
        final int id2 = graph.addVertex();
        graph.setFloatValue(attr, id2, VALUE2);

        assertTrue("Retrieve float value", VALUE1 == graph.getFloatValue(attr, id1));
        assertTrue("Retrieve float value", VALUE2 == graph.getFloatValue(attr, id2));
    }

    @Test
    public void setGetDefault() {
        final float value = -0.5f;
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "float";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, value, null);

        final int id1 = graph.addVertex();
        final int id2 = graph.addVertex();

        assertTrue("Retrieve float value", value == graph.getFloatValue(attr, id1));
        assertTrue("Retrieve float value", value == graph.getFloatValue(attr, id2));
    }

    @Test
    public void setGetInteger() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "integer";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final int VALUE1 = 17;
        final int VALUE2 = 43;
        final int id1 = graph.addVertex();
        graph.setIntValue(attr, id1, VALUE1);
        final int id2 = graph.addVertex();
        graph.setIntValue(attr, id2, VALUE2);

        assertEquals("Retrieve int value", VALUE1, graph.getIntValue(attr, id1));
        assertEquals("Retrieve int value", VALUE2, graph.getIntValue(attr, id2));
    }

    @Test
    public void setGetBoolean() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "boolean";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final boolean VALUE1 = false;
        final boolean VALUE2 = true;
        final int id1 = graph.addVertex();
        graph.setBooleanValue(attr, id1, VALUE1);
        final int id2 = graph.addVertex();
        graph.setBooleanValue(attr, id2, VALUE2);

        assertEquals("Retrieve boolean value", VALUE1, graph.getBooleanValue(attr, id1));
        assertEquals("Retrieve boolean value", VALUE2, graph.getBooleanValue(attr, id2));
    }

    @Test
    public void setGetObject() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "object";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final Object VALUE1 = new Object();
        final Object VALUE2 = new Object();
        final int id1 = graph.addVertex();
        graph.setObjectValue(attr, id1, VALUE1);
        final int id2 = graph.addVertex();
        graph.setObjectValue(attr, id2, VALUE2);

        assertSame("Retrieve object value", VALUE1, graph.getObjectValue(attr, id1));
        assertSame("Retrieve object value", VALUE2, graph.getObjectValue(attr, id2));
    }

    /**
     * Test of get method, of class StoreGraph.
     */
    @Test
    public void getMultiMany() {
        final GraphWriteMethods graph = new StoreGraph();
        final int intAttr = graph.addAttribute(GraphElementType.VERTEX, IntegerAttributeDescription.ATTRIBUTE_NAME, "intattr", null, null, null);
        final int floatAttr = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "floatattr", null, null, null);
        final int stringAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "stringattr", null, null, null);

        // Add lots of values for this attribute.
        final int N = 10000;
        final int[] v = new int[N];
        for (int i = 0; i < N; i++) {
            v[i] = graph.addVertex();
            graph.setIntValue(intAttr, v[i], i);
            graph.setFloatValue(floatAttr, v[i], 2f * i);
            graph.setStringValue(stringAttr, v[i], "x" + i);
        }

        // Retrieve the values.
        for (int i = 0; i < N; i++) {
            final int ival = graph.getIntValue(intAttr, v[i]);
            assertEquals("Compare int", i, ival);
            final float fval = graph.getFloatValue(floatAttr, v[i]);
            assertTrue("Compare float", 2f * i == fval);
            final String sval = graph.getStringValue(stringAttr, v[i]);
            assertEquals("Compare int", "x" + i, sval);
        }
    }

    @Test
    public void sizeElements() {
        final GraphElementType elementType = GraphElementType.VERTEX;
        final String name = "node1";
        final String attributeType = "integer";
        final GraphWriteMethods graph = new StoreGraph();
        final int attr = graph.addAttribute(elementType, attributeType, name, null, null, null);

        final int N = 10;
        final int[] attrs = new int[N];
        assertEquals("Adding some elements", 0, graph.getVertexCount());
        for (int i = 0; i < N; i++) {
            attrs[i] = graph.addVertex();
            graph.setIntValue(attr, attrs[i], i);
            assertEquals("Adding some elements", i + 1, graph.getVertexCount());
            assertTrue("Adding, capacity", graph.getVertexCapacity() >= graph.getVertexCount());
        }

        for (int i = 0; i < N; i++) {
            graph.removeVertex(attrs[i]);
            assertEquals("Removing some elements", N - i - 1, graph.getVertexCount());
            assertTrue("Removing, capacity", graph.getVertexCapacity() >= graph.getVertexCount());
        }
    }
}
