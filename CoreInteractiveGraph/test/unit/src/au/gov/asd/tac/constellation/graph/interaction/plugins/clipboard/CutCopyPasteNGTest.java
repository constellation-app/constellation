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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Cut Copy Paste Test.
 *
 * @author algol
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class CutCopyPasteNGTest extends ConstellationTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vNameAttr, tNameAttr, vSelAttr, tSelAttr;
    private Graph graph;

    public CutCopyPasteNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema ss = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        graph = new DualGraph(ss);
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

            vNameAttr = wg.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            if (vNameAttr == Graph.NOT_FOUND) {
                fail();
            }

            tNameAttr = wg.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "name", "descr", "", null);
            if (tNameAttr == Graph.NOT_FOUND) {
                fail();
            }

            vSelAttr = wg.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (vSelAttr == Graph.NOT_FOUND) {
                fail();
            }

            tSelAttr = wg.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "selected", false, null);
            if (tSelAttr == Graph.NOT_FOUND) {
                fail();
            }

            vxId1 = wg.addVertex();
            wg.setFloatValue(attrX, vxId1, 1.0f);
            wg.setFloatValue(attrY, vxId1, 1.0f);
            wg.setBooleanValue(vSelAttr, vxId1, false);
            wg.setStringValue(vNameAttr, vxId1, "name1");
            vxId2 = wg.addVertex();
            wg.setFloatValue(attrX, vxId2, 5.0f);
            wg.setFloatValue(attrY, vxId2, 1.0f);
            wg.setBooleanValue(vSelAttr, vxId2, true);
            wg.setStringValue(vNameAttr, vxId2, "name2");
            vxId3 = wg.addVertex();
            wg.setFloatValue(attrX, vxId3, 1.0f);
            wg.setFloatValue(attrY, vxId3, 5.0f);
            wg.setBooleanValue(vSelAttr, vxId3, false);
            wg.setStringValue(vNameAttr, vxId3, "name3");
            vxId4 = wg.addVertex();
            wg.setFloatValue(attrX, vxId4, 5.0f);
            wg.setFloatValue(attrY, vxId4, 5.4f);
            wg.setBooleanValue(vSelAttr, vxId4, true);
            wg.setStringValue(vNameAttr, vxId4, "name4");
            vxId5 = wg.addVertex();
            wg.setFloatValue(attrX, vxId5, 15.0f);
            wg.setFloatValue(attrY, vxId5, 15.5f);
            wg.setBooleanValue(vSelAttr, vxId5, false);
            wg.setStringValue(vNameAttr, vxId5, "name5");
            vxId6 = wg.addVertex();
            wg.setFloatValue(attrX, vxId6, 26.0f);
            wg.setFloatValue(attrY, vxId6, 26.60f);
            wg.setBooleanValue(vSelAttr, vxId6, true);
            wg.setStringValue(vNameAttr, vxId6, "name6");
            vxId7 = wg.addVertex();
            wg.setFloatValue(attrX, vxId7, 37.0f);
            wg.setFloatValue(attrY, vxId7, 37.7f);
            wg.setBooleanValue(vSelAttr, vxId7, false);
            wg.setStringValue(vNameAttr, vxId7, "name7");

            txId1 = wg.addTransaction(vxId1, vxId2, true);
            wg.setBooleanValue(tSelAttr, txId1, false);
            wg.setStringValue(tNameAttr, txId1, "name101");

            txId2 = wg.addTransaction(vxId1, vxId3, true);
            wg.setBooleanValue(tSelAttr, txId2, true);
            wg.setStringValue(tNameAttr, txId2, "name102");

            txId3 = wg.addTransaction(vxId2, vxId4, true);
            wg.setBooleanValue(tSelAttr, txId3, false);
            wg.setStringValue(tNameAttr, txId3, "name103");

            txId4 = wg.addTransaction(vxId4, vxId2, true);
            wg.setBooleanValue(tSelAttr, txId4, true);
            wg.setStringValue(tNameAttr, txId4, "name104");

            txId5 = wg.addTransaction(vxId5, vxId6, true);
            wg.setBooleanValue(tSelAttr, txId5, false);
            wg.setStringValue(tNameAttr, txId5, "name105");
        } finally {
            wg.commit();
        }
    }

    // TODO: this is not working when run through jenkins unit tests, fix it
//    @Test
//    public void copyPasteWithPrimaryKeysTest() throws InterruptedException {
//        final WritableGraph wg = graph.getWritableGraph("set primary keys", true);
//        try {
//            wg.setPrimaryKey(GraphElementType.VERTEX, vNameAttr);
//            wg.setPrimaryKey(GraphElementType.TRANSACTION, tNameAttr);
//        } finally {
//            wg.commit();
//        }
//
//        final GraphNode graphNode1 = new GraphNode(graph, null, new TopComponent(), null);
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.COPY).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        final Graph newGraph = new DualGraph(null);
//        final GraphNode graphNode2 = new GraphNode(newGraph, null, new TopComponent(), null);
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.PASTE).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        ReadableGraph rg = graph.getReadableGraph();
//        try {
//            assertEquals("node count", 7, rg.getVertexCount());
//            assertEquals("transaction count", 5, rg.getTransactionCount());
//
//            assertTrue("nd 'name1' found", nodeFound(rg, "name1"));
//            assertTrue("nd 'name2' found", nodeFound(rg, "name2"));
//            assertTrue("nd 'name3' found", nodeFound(rg, "name3"));
//            assertTrue("nd 'name4' found", nodeFound(rg, "name4"));
//            assertTrue("nd 'name5' found", nodeFound(rg, "name5"));
//            assertTrue("nd 'name6' found", nodeFound(rg, "name6"));
//            assertTrue("nd 'name7' found", nodeFound(rg, "name7"));
//            assertTrue("tx 'name101' found", transactionFound(rg, "name101"));
//            assertTrue("tx 'name102' found", transactionFound(rg, "name102"));
//            assertTrue("tx 'name103' found", transactionFound(rg, "name103"));
//            assertTrue("tx 'name104' found", transactionFound(rg, "name104"));
//            assertTrue("tx 'name105' found", transactionFound(rg, "name105"));
//        } finally {
//            rg.release();
//        }
//    }
//
//    @Test
//    public void copyPasteWithoutPrimaryKeysTest() throws InterruptedException {
//        final File saveCreatedGraph = File.createTempFile("saveCreatedGraph", GraphDataObject.FILE_EXTENSION);
//        ReadableGraph rg1 = graph.getReadableGraph();
//        try {
//            final GraphJsonWriter writer = new GraphJsonWriter();
//            writer.writeGraphToZip(rg1, saveCreatedGraph.getPath(), new TextIoProgress(false));
//        } catch (IOException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        } finally {
//            rg1.release();
//        }
//
//        final GraphNode graphNode1 = new GraphNode(graph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.COPY).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        final Graph newGraph = new DualGraph(null);
//        final GraphNode graphNode2 = new GraphNode(newGraph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.PASTE).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        ReadableGraph rg = graph.getReadableGraph();
//        try {
//            assertEquals("node count", 10, rg.getVertexCount());
//            assertEquals("transaction count", 6, rg.getTransactionCount());
//
//            assertTrue("nd 'name1' found", nodeFound(rg, "name1"));
//            assertTrue("nd 'name2' found", nodeFound(rg, "name2"));
//            assertTrue("nd 'name3' found", nodeFound(rg, "name3"));
//            assertTrue("nd 'name4' found", nodeFound(rg, "name4"));
//            assertTrue("nd 'name5' found", nodeFound(rg, "name5"));
//            assertTrue("nd 'name6' found", nodeFound(rg, "name6"));
//            assertTrue("nd 'name7' found", nodeFound(rg, "name7"));
//            assertTrue("tx 'name101' found", transactionFound(rg, "name101"));
//            assertTrue("tx 'name102' found", transactionFound(rg, "name102"));
//            assertTrue("tx 'name103' found", transactionFound(rg, "name103"));
//            assertTrue("tx 'name104' found", transactionFound(rg, "name104"));
//            assertTrue("tx 'name105' found", transactionFound(rg, "name105"));
//        } finally {
//            rg.release();
//        }
//    }
//
//    @Test
//    public void cutTest() throws InterruptedException {
//        final File saveCreatedGraph = File.createTempFile("before", GraphDataObject.FILE_EXTENSION);
//        ReadableGraph rg1 = graph.getReadableGraph();
//        try {
//            final GraphJsonWriter writer = new GraphJsonWriter();
//            writer.writeGraphToZip(rg1, saveCreatedGraph.getPath(), new TextIoProgress(false));
//        } catch (IOException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        } finally {
//            rg1.release();
//        }
//        final GraphNode graphNode1 = new GraphNode(graph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.CUT).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        ReadableGraph rg = graph.getReadableGraph();
//        try {
//            assertEquals("node count", 4, rg.getVertexCount());
//            assertEquals("transaction count", 0, rg.getTransactionCount());
//
//            assertTrue("nd 'name1' found", nodeFound(rg, "name1"));
//            assertTrue("nd 'name3' found", nodeFound(rg, "name3"));
//            assertTrue("nd 'name5' found", nodeFound(rg, "name5"));
//            assertTrue("nd 'name7' found", nodeFound(rg, "name7"));
//
//            assertFalse("nd 'name2' not found", nodeFound(rg, "name2"));
//            assertFalse("nd 'name4' not found", nodeFound(rg, "name4"));
//            assertFalse("nd 'name6' not found", nodeFound(rg, "name6"));
//            assertFalse("tx 'name101' not found", transactionFound(rg, "name101"));
//            assertFalse("tx 'name102' not found", transactionFound(rg, "name102"));
//            assertFalse("tx 'name103' not found", transactionFound(rg, "name103"));
//            assertFalse("tx 'name104' not found", transactionFound(rg, "name104"));
//            assertFalse("tx 'name105' not found", transactionFound(rg, "name105"));
//        } finally {
//            rg.release();
//        }
//        final File saveCreatedGraph2 = File.createTempFile("after", GraphDataObject.FILE_EXTENSION);
//        ReadableGraph rg2 = graph.getReadableGraph();
//        try {
//            final GraphJsonWriter writer = new GraphJsonWriter();
//            writer.writeGraphToZip(rg2, saveCreatedGraph2.getPath(), new TextIoProgress(false));
//        } catch (IOException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        } finally {
//            rg2.release();
//        }
//    }
//
//    @Test
//    public void cutPasteWithPrimaryKeysTest() throws InterruptedException {
//        WritableGraph wg = graph.getWritableGraph("set primary keys", true);
//        try {
//            wg.setPrimaryKey(GraphElementType.VERTEX, vNameAttr);
//            wg.setPrimaryKey(GraphElementType.TRANSACTION, tNameAttr);
//        } finally {
//            wg.commit();
//        }
//
//        final GraphNode graphNode1 = new GraphNode(graph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.CUT).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        final Graph newGraph = new DualGraph(null);
//        final GraphNode graphNode2 = new GraphNode(newGraph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.PASTE).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        ReadableGraph rg = graph.getReadableGraph();
//        try {
//            assertEquals("node count", 7, rg.getVertexCount());
//            assertEquals("transaction count", 2, rg.getTransactionCount());
//
//            assertTrue("nd 'name1' found", nodeFound(rg, "name1"));
//            assertTrue("nd 'name3' found", nodeFound(rg, "name3"));
//            assertTrue("nd 'name5' found", nodeFound(rg, "name5"));
//            assertTrue("nd 'name7' found", nodeFound(rg, "name7"));
//
//            assertTrue("nd 'name2' found", nodeFound(rg, "name2"));
//            assertTrue("nd 'name4' found", nodeFound(rg, "name4"));
//            assertTrue("nd 'name6' found", nodeFound(rg, "name6"));
//            assertFalse("tx 'name101' not found", transactionFound(rg, "name101"));
//            assertTrue("tx 'name102' found", transactionFound(rg, "name102"));
//            assertFalse("tx 'name103' not found", transactionFound(rg, "name103"));
//            assertTrue("tx 'name104' found", transactionFound(rg, "name104"));
//            assertFalse("tx 'name105' not found", transactionFound(rg, "name105"));
//        } finally {
//            rg.release();
//        }
//    }
//
//    @Test
//    public void cutPasteWithoutPrimaryKeysTest() throws InterruptedException {
//        final GraphNode graphNode1 = new GraphNode(graph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.CUT).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        final Graph newGraph = new DualGraph(null);
//        final GraphNode graphNode2 = new GraphNode(newGraph, null, null, new TopComponent());
//        try {
//            PluginExecution.withPlugin(CorePluginRegistry.PASTE).executeNow(graph);
//        } catch (PluginException ex) {
//            Assert.fail(ex.getLocalizedMessage());
//        }
//
//        ReadableGraph rg = graph.getReadableGraph();
//        try {
//            assertEquals("node count", 7, rg.getVertexCount());
//            assertEquals("transaction count", 1, rg.getTransactionCount());
//
//            assertTrue("nd 'name1' found", nodeFound(rg, "name1"));
//            assertTrue("nd 'name3' found", nodeFound(rg, "name3"));
//            assertTrue("nd 'name5' found", nodeFound(rg, "name5"));
//            assertTrue("nd 'name7' found", nodeFound(rg, "name7"));
//
//            assertTrue("nd 'name2' found", nodeFound(rg, "name2"));
//            assertTrue("nd 'name4' found", nodeFound(rg, "name4"));
//            assertTrue("nd 'name6' found", nodeFound(rg, "name6"));
//            assertFalse("tx 'name101' not found", transactionFound(rg, "name101"));
//            assertFalse("tx 'name102' not found", transactionFound(rg, "name102"));
//            assertFalse("tx 'name103' not found", transactionFound(rg, "name103"));
//            assertTrue("tx 'name104' found", transactionFound(rg, "name104"));
//            assertFalse("tx 'name105' not found", transactionFound(rg, "name105"));
//        } finally {
//            rg.release();
//        }
//    }
    private boolean nodeFound(ReadableGraph graph, String base_name) {
        int nameAttrId = graph.getAttribute(GraphElementType.VERTEX, "name");
        boolean found = false;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            int id = graph.getVertex(i);
            String name = graph.getStringValue(nameAttrId, id);
            if (base_name.equals(name)) {
                found = true;
            }
        }
        return found;
    }

    // determine whether the transaction of the specified name exists in the graph
    private boolean transactionFound(ReadableGraph graph, String base_name) {
        int nameAttrId = graph.getAttribute(GraphElementType.TRANSACTION, "name");
        int nameAttrId2 = graph.getAttribute(GraphElementType.VERTEX, "name");
        boolean found = false;
        for (int i = 0; i < graph.getTransactionCount(); i++) {
            int id = graph.getTransaction(i);
            String name = graph.getStringValue(nameAttrId, id);
            if (base_name.equals(name)) {
                found = true;

                int s = graph.getTransactionSourceVertex(id);
                int d = graph.getTransactionDestinationVertex(id);
                String sn = graph.getStringValue(nameAttrId2, s);
                String dn = graph.getStringValue(nameAttrId2, d);
                String tn = graph.getStringValue(nameAttrId, id);
            }
        }
        return found;
    }
}
