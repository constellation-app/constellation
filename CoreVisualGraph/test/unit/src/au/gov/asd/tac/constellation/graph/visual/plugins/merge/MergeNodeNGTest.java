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
package au.gov.asd.tac.constellation.graph.visual.plugins.merge;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.util.ArrayList;
import java.util.HashMap;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Merge Node Test.
 *
 * @author altair
 */
public class MergeNodeNGTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vAttrId, tAttrId;
    private Graph graph;

    public MergeNodeNGTest() {
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
        graph = new DualGraph(null);
        WritableGraph wg = graph.getWritableGraph("add", true);
        try {
            attrX = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x", "x", 0.0, null);
            if (attrX == Graph.NOT_FOUND) {
                fail();
            }

            attrY = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y", "y", 0.0, null);
            if (attrY == Graph.NOT_FOUND) {
                fail();
            }

            attrZ = wg.addAttribute(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z", "z", 0.0, null);
            if (attrZ == Graph.NOT_FOUND) {
                fail();
            }

            vAttrId = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (vAttrId == Graph.NOT_FOUND) {
                fail();
            }

            tAttrId = wg.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (tAttrId == Graph.NOT_FOUND) {
                fail();
            }

            vxId1 = wg.addVertex();
            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.0f);
            wg.setBooleanValue(vAttrId, vxId1, false);
            vxId2 = wg.addVertex();
            wg.setFloatValue(attrX, vxId2, 5.0f);
            wg.setFloatValue(attrY, vxId2, 1.0f);
            wg.setBooleanValue(vAttrId, vxId2, false);
            vxId3 = wg.addVertex();
            wg.setFloatValue(attrX, vxId3, 1.0f);
            wg.setFloatValue(attrY, vxId3, 5.0f);
            wg.setBooleanValue(vAttrId, vxId3, false);
            vxId4 = wg.addVertex();
            wg.setFloatValue(attrX, vxId4, 5.0f);
            wg.setFloatValue(attrY, vxId4, 5.0f);
            wg.setBooleanValue(vAttrId, vxId4, false);
            vxId5 = wg.addVertex();
            wg.setFloatValue(attrX, vxId5, 10.0f);
            wg.setFloatValue(attrY, vxId5, 10.0f);
            wg.setBooleanValue(vAttrId, vxId5, false);
            vxId6 = wg.addVertex();
            wg.setFloatValue(attrX, vxId6, 15.0f);
            wg.setFloatValue(attrY, vxId6, 15.0f);
            wg.setBooleanValue(vAttrId, vxId6, false);
            vxId7 = wg.addVertex();
            wg.setFloatValue(attrX, vxId7, 100.0f);
            wg.setFloatValue(attrY, vxId7, 100.0f);
            wg.setBooleanValue(vAttrId, vxId7, false);

            txId1 = wg.addTransaction(vxId1, vxId2, false);
            txId2 = wg.addTransaction(vxId1, vxId3, false);
            txId3 = wg.addTransaction(vxId2, vxId4, true);
            txId4 = wg.addTransaction(vxId4, vxId2, true);
            txId5 = wg.addTransaction(vxId5, vxId6, false);
        } finally {
            wg.commit();
        }
    }

    // TODO: commenting out the unit test for now as it fails when run through a headless execution
//    /**
//     * Use this when running unit tests that requires a DialogDisplay and X11 is
//     * not configured
//     */
//    @ServiceProviders({
//        @ServiceProvider(service = DialogDisplayer.class, position = 1)
//    })
//    public static class DialogDisplayerForUnitTests extends DialogDisplayer {
//
//        @Override
//        public Object notify(NotifyDescriptor descriptor) {
//            System.out.println(descriptor.getMessage());
//            return null;
//        }
//
//        @Override
//        public Dialog createDialog(DialogDescriptor descriptor) {
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        }
//
//    }
//
//    @Test
//    public void mergeNothingTest() throws InterruptedException, PluginException {
//        final GraphNode graphNode1 = new GraphNode(graph, null, new TopComponent(), null);
//        WritableGraph wg = graph.getWritableGraph("merge", false);
//        try {
//            assertEquals("Transaction count", 5, wg.getTransactionCount());
//            assertEquals("Node count", 7, wg.getVertexCount());
//
//            PluginExecution.withPlugin(CorePluginRegistry.PERMANENT_NODE_MERGE)
//                    .withParameter(PermanentMergePlugin.SELECTED_NODES_PARAMETER_ID, new ArrayList<>())
//                    .withParameter(PermanentMergePlugin.ATTTRIBUTES_PARAMETER_ID, null)
//                    .executeNow(wg);
//
//            assertEquals("Transaction count", 5, wg.getTransactionCount());
//            assertEquals("Node count", 7, wg.getVertexCount());
//        } finally {
//            wg.commit();
//        }
//    }
//
//    @Test
//    public void mergeOneTest() throws InterruptedException, PluginException {
//        final GraphNode graphNode1 = new GraphNode(graph, null, new TopComponent(), null);
//        WritableGraph wg = graph.getWritableGraph("merge", false);
//        try {
//            assertEquals("Transaction count", 5, wg.getTransactionCount());
//            assertEquals("Node count", 7, wg.getVertexCount());
//
//            ArrayList<Integer> list = new ArrayList<>();
//            list.add(vxId7);
//
//            PluginExecution.withPlugin(CorePluginRegistry.PERMANENT_NODE_MERGE)
//                    .withParameter(PermanentMergePlugin.SELECTED_NODES_PARAMETER_ID, list)
//                    .withParameter(PermanentMergePlugin.ATTTRIBUTES_PARAMETER_ID, null)
//                    .executeNow(wg);
//
//            assertEquals("Transaction count", 5, wg.getTransactionCount());
//            assertEquals("Node count", 7, wg.getVertexCount());
//        } finally {
//            wg.commit();
//        }
//    }
    @Test
    public void mergeTwoTest() throws InterruptedException, PluginException {
        WritableGraph wg = graph.getWritableGraph("merge", false);
        try {
            assertEquals("Transaction count", 5, wg.getTransactionCount());
            assertEquals("Node count", 7, wg.getVertexCount());

            ArrayList<Integer> list = new ArrayList<>();
            list.add(vxId5);
            list.add(vxId6);

            HashMap<Integer, String> values = new HashMap<>();
            values.put(attrX, "123.0");
            values.put(attrY, "456.0");
            values.put(attrZ, "789.0");

            PluginExecution.withPlugin(VisualGraphPluginRegistry.PERMANENT_MERGE)
                    .withParameter(PermanentMergePlugin.SELECTED_NODES_PARAMETER_ID, list)
                    .withParameter(PermanentMergePlugin.ATTTRIBUTES_PARAMETER_ID, values)
                    .executeNow(wg);

            assertEquals("Transaction count", 5, wg.getTransactionCount());
            assertEquals("Node count", 6, wg.getVertexCount());

            assertFalse("node 5 has been eliminated", this.nodeExists(wg, 10.0f, 10.0f, 0.0f));
            assertFalse("node 6 has been eliminated", this.nodeExists(wg, 15.0f, 15.0f, 0.0f));
            assertTrue("new node has been created", this.nodeExists(wg, 123.0f, 456.0f, 789.0f));
        } finally {
            wg.commit();
        }
    }

    // determine whether a node exists based on its coordinates
    private boolean nodeExists(WritableGraph graph, float x, float y, float z) {
        boolean found = false;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            int id = graph.getVertex(i);
            float x2 = graph.getFloatValue(attrX, id);
            float y2 = graph.getFloatValue(attrY, id);
            float z2 = graph.getFloatValue(attrZ, id);
            if (x == x2 && y == y2 && z == z2) {
                found = true;
            }
        }
        return found;
    }

    @Test
    public void mergeManyTest() throws InterruptedException, PluginException {
        WritableGraph wg = graph.getWritableGraph("merge", false);
        try {
            assertEquals("Transaction count", 5, wg.getTransactionCount());
            assertEquals("Node count", 7, wg.getVertexCount());

            ArrayList<Integer> list = new ArrayList<>();
            list.add(vxId1);
            list.add(vxId2);
            list.add(vxId3);
            list.add(vxId4);

            HashMap<Integer, String> values = new HashMap<>();
            values.put(attrX, "123.0");
            values.put(attrY, "456.0");
            values.put(attrZ, "789.0");

            PluginExecution.withPlugin(VisualGraphPluginRegistry.PERMANENT_MERGE)
                    .withParameter(PermanentMergePlugin.SELECTED_NODES_PARAMETER_ID, list)
                    .withParameter(PermanentMergePlugin.ATTTRIBUTES_PARAMETER_ID, values)
                    .executeNow(wg);

            assertEquals("Transaction count", 5, wg.getTransactionCount());
            assertEquals("Node count", 4, wg.getVertexCount());

            assertFalse("node 1 has been eliminated", this.nodeExists(wg, 1.0f, 1.0f, 0.0f));
            assertFalse("node 2 has been eliminated", this.nodeExists(wg, 5.0f, 1.0f, 0.0f));
            assertFalse("node 3 has been eliminated", this.nodeExists(wg, 5.0f, 5.0f, 0.0f));
            assertFalse("node 4 has been eliminated", this.nodeExists(wg, 5.0f, 5.0f, 0.0f));
            assertTrue("new node has been created", this.nodeExists(wg, 123.0f, 456.0f, 789.0f));
        } finally {
            wg.commit();
        }
    }
}
