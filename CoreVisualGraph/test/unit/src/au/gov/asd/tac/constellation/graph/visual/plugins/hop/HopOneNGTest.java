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
package au.gov.asd.tac.constellation.graph.visual.plugins.hop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Hop One Test.
 *
 * @author altair
 */
public class HopOneNGTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vAttrId, tAttrId;
    private StoreGraph graph;

    public HopOneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        graph = null;
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema ss = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new StoreGraph(ss);
        attrX = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0.0, null);
        if (attrX == Graph.NOT_FOUND) {
            fail();
        }

        attrY = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0.0, null);
        if (attrY == Graph.NOT_FOUND) {
            fail();
        }

        attrZ = graph.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0.0, null);
        if (attrZ == Graph.NOT_FOUND) {
            fail();
        }

        vAttrId = graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
        if (vAttrId == Graph.NOT_FOUND) {
            fail();
        }

        tAttrId = graph.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
        if (tAttrId == Graph.NOT_FOUND) {
            fail();
        }

        vxId1 = graph.addVertex();
        graph.setFloatValue(attrX, vxId1, 1.0f);
        graph.setFloatValue(attrY, vxId1, 1.0f);
        graph.setBooleanValue(vAttrId, vxId1, false);
        vxId2 = graph.addVertex();
        graph.setFloatValue(attrX, vxId2, 5.0f);
        graph.setFloatValue(attrY, vxId2, 1.0f);
        graph.setBooleanValue(vAttrId, vxId2, false);
        vxId3 = graph.addVertex();
        graph.setFloatValue(attrX, vxId3, 1.0f);
        graph.setFloatValue(attrY, vxId3, 5.0f);
        graph.setBooleanValue(vAttrId, vxId3, false);
        vxId4 = graph.addVertex();
        graph.setFloatValue(attrX, vxId4, 5.0f);
        graph.setFloatValue(attrY, vxId4, 5.0f);
        graph.setBooleanValue(vAttrId, vxId4, false);
        vxId5 = graph.addVertex();
        graph.setFloatValue(attrX, vxId5, 10.0f);
        graph.setFloatValue(attrY, vxId5, 10.0f);
        graph.setBooleanValue(vAttrId, vxId5, false);
        vxId6 = graph.addVertex();
        graph.setFloatValue(attrX, vxId6, 15.0f);
        graph.setFloatValue(attrY, vxId6, 15.0f);
        graph.setBooleanValue(vAttrId, vxId6, false);
        vxId7 = graph.addVertex();
        graph.setFloatValue(attrX, vxId7, 100.0f);
        graph.setFloatValue(attrY, vxId7, 100.0f);
        graph.setBooleanValue(vAttrId, vxId7, false);

        txId1 = graph.addTransaction(vxId1, vxId2, false);
        txId2 = graph.addTransaction(vxId1, vxId3, false);
        txId3 = graph.addTransaction(vxId2, vxId4, true);
        txId4 = graph.addTransaction(vxId4, vxId2, true);
        txId5 = graph.addTransaction(vxId5, vxId6, false);
    }

    @Test
    public void oneHopTest() throws InterruptedException, PluginException {
        graph.setBooleanValue(vAttrId, vxId2, true);
        graph.setBooleanValue(vAttrId, vxId5, true);
        graph.setBooleanValue(vAttrId, vxId7, true);

        assertFalse(String.format("Node [%d] should be de-selected", vxId1), graph.getBooleanValue(vAttrId, vxId1));
        assertTrue(String.format("Node [%d] should be selected", vxId2), graph.getBooleanValue(vAttrId, vxId2));
        assertFalse(String.format("Node [%d] should be de-selected", vxId3), graph.getBooleanValue(vAttrId, vxId3));
        assertFalse(String.format("Node [%d] should be de-selected", vxId4), graph.getBooleanValue(vAttrId, vxId4));
        assertTrue(String.format("Node [%d] should be selected", vxId5), graph.getBooleanValue(vAttrId, vxId5));
        assertFalse(String.format("Node [%d] should be de-selected", vxId6), graph.getBooleanValue(vAttrId, vxId6));
        assertTrue(String.format("Node [%d] should be selected", vxId7), graph.getBooleanValue(vAttrId, vxId7));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId1), graph.getBooleanValue(tAttrId, txId1));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId2), graph.getBooleanValue(tAttrId, txId2));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId3), graph.getBooleanValue(tAttrId, txId3));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId4), graph.getBooleanValue(tAttrId, txId4));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId5), graph.getBooleanValue(tAttrId, txId5));

        PluginExecution.withPlugin(VisualGraphPluginRegistry.HOP_OUT)
                .withParameter(HopOutPlugin.HOPS_PARAMETER_ID, HopUtilities.HOP_OUT_ONE)
                .executeNow(graph);

        assertTrue(String.format("Node [%d] should be selected", vxId1), graph.getBooleanValue(vAttrId, vxId1));
        assertTrue(String.format("Node [%d] should be selected", vxId2), graph.getBooleanValue(vAttrId, vxId2));
        assertFalse(String.format("Node [%d] should be de-selected", vxId3), graph.getBooleanValue(vAttrId, vxId3));
        assertTrue(String.format("Node [%d] should be selected", vxId4), graph.getBooleanValue(vAttrId, vxId4));
        assertTrue(String.format("Node [%d] should be selected", vxId5), graph.getBooleanValue(vAttrId, vxId5));
        assertTrue(String.format("Node [%d] should be selected", vxId6), graph.getBooleanValue(vAttrId, vxId6));
        assertTrue(String.format("Node [%d] should be selected", vxId7), graph.getBooleanValue(vAttrId, vxId7));
        assertTrue(String.format("Transaction [%d] should be selected", txId1), graph.getBooleanValue(tAttrId, txId1));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId2), graph.getBooleanValue(tAttrId, txId2));
        assertTrue(String.format("Transaction [%d] should be selected", txId3), graph.getBooleanValue(tAttrId, txId3));
        assertTrue(String.format("Transaction [%d] should be selected", txId4), graph.getBooleanValue(tAttrId, txId4));
        assertTrue(String.format("Transaction [%d] should be selected", txId5), graph.getBooleanValue(tAttrId, txId5));
    }

    @Test
    public void oneHopTest2() throws InterruptedException, PluginException {
        graph.setBooleanValue(vAttrId, vxId1, true);
        graph.setBooleanValue(tAttrId, txId3, true);

        assertTrue(String.format("Node [%d] should be selected", vxId1), graph.getBooleanValue(vAttrId, vxId1));
        assertFalse(String.format("Node [%d] should be de-selected", vxId2), graph.getBooleanValue(vAttrId, vxId2));
        assertFalse(String.format("Node [%d] should be de-selected", vxId3), graph.getBooleanValue(vAttrId, vxId3));
        assertFalse(String.format("Node [%d] should be de-selected", vxId4), graph.getBooleanValue(vAttrId, vxId4));
        assertFalse(String.format("Node [%d] should be de-selected", vxId5), graph.getBooleanValue(vAttrId, vxId5));
        assertFalse(String.format("Node [%d] should be de-selected", vxId6), graph.getBooleanValue(vAttrId, vxId6));
        assertFalse(String.format("Node [%d] should be de-selected", vxId7), graph.getBooleanValue(vAttrId, vxId7));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId1), graph.getBooleanValue(tAttrId, txId1));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId2), graph.getBooleanValue(tAttrId, txId2));
        assertTrue(String.format("Transaction [%d] should be selected", txId3), graph.getBooleanValue(tAttrId, txId3));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId4), graph.getBooleanValue(tAttrId, txId4));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId5), graph.getBooleanValue(tAttrId, txId5));

        PluginExecution.withPlugin(VisualGraphPluginRegistry.HOP_OUT)
                .withParameter(HopOutPlugin.HOPS_PARAMETER_ID, HopUtilities.HOP_OUT_ONE)
                .executeNow(graph);

        assertTrue(String.format("Node [%d] should be selected", vxId1), graph.getBooleanValue(vAttrId, vxId1));
        assertTrue(String.format("Node [%d] should be selected", vxId2), graph.getBooleanValue(vAttrId, vxId2));
        assertTrue(String.format("Node [%d] should be selected", vxId3), graph.getBooleanValue(vAttrId, vxId3));
        assertTrue(String.format("Node [%d] should be selected", vxId4), graph.getBooleanValue(vAttrId, vxId4));
        assertFalse(String.format("Node [%d] should be de-selected", vxId5), graph.getBooleanValue(vAttrId, vxId5));
        assertFalse(String.format("Node [%d] should be de-selected", vxId6), graph.getBooleanValue(vAttrId, vxId6));
        assertFalse(String.format("Node [%d] should be de-selected", vxId7), graph.getBooleanValue(vAttrId, vxId7));
        assertTrue(String.format("Transaction [%d] should be selected", txId1), graph.getBooleanValue(tAttrId, txId1));
        assertTrue(String.format("Transaction [%d] should be selected", txId2), graph.getBooleanValue(tAttrId, txId2));
        assertTrue(String.format("Transaction [%d] should be selected", txId3), graph.getBooleanValue(tAttrId, txId3));
        assertTrue(String.format("Transaction [%d] should be selected", txId4), graph.getBooleanValue(tAttrId, txId4));
        assertFalse(String.format("Transaction [%d] should be de-selected", txId5), graph.getBooleanValue(tAttrId, txId5));
    }
}
