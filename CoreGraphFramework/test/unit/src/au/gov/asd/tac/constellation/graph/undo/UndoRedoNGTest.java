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
package au.gov.asd.tac.constellation.graph.undo;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import javax.swing.undo.UndoManager;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Undo Redo Test.
 *
 * @author algol
 */
public class UndoRedoNGTest {

    private int attrX, attrY, attrZ;
    private int vxId1, vxId2, vxId3, vxId4, vxId5, vxId6, vxId7;
    private int txId1, txId2, txId3, txId4, txId5;
    private int vNameAttr, tNameAttr, vSelAttr, tSelAttr;
    private Graph graph;
    private UndoManager undoMgr;

    public UndoRedoNGTest() {
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
        undoMgr = new UndoManager();
        graph.setUndoManager(undoMgr);

        WritableGraph wg = graph.getWritableGraph("original load", true);
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

//    @Test
//    public void undoTwoOperationsTest() throws InterruptedException {
//        final boolean[] canUndo = new boolean[4];
//        final boolean[] canRedo = new boolean[4];
//        final boolean[] undoNamesMatch = new boolean[3];
//        final boolean[] redoNamesMatch = new boolean[2];
//
//        checkBaseElements();
//        checkElementCount(7, 5);
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[0] = undoMgr.canUndo();
//                canRedo[0] = undoMgr.canRedo();
//                undoNamesMatch[0] = "Undo original load".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (InterruptedException | InvocationTargetException ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[0]);
//        assertFalse("redo not available", canRedo[0]);
//        assertTrue("last undo", undoNamesMatch[0]);
//
//        addNode();
//
//        checkElementCount(8, 5);
//        checkNewNode();
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[1] = undoMgr.canUndo();
//                canRedo[1] = undoMgr.canRedo();
//                undoNamesMatch[1] = "Undo add vertex".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (InterruptedException | InvocationTargetException ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[1]);
//        assertFalse("redo not available", canRedo[1]);
//        assertTrue("Next undo", undoNamesMatch[0]);
//
//        graph.undo();
//
//        checkElementCount(7, 5);
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[2] = undoMgr.canUndo();
//                canRedo[2] = undoMgr.canRedo();
//                undoNamesMatch[2] = "Undo original load".equals(undoMgr.getUndoPresentationName());
//                redoNamesMatch[0] = "Redo add vertex".equals(undoMgr.getRedoPresentationName());
//            });
//        } catch (InterruptedException | InvocationTargetException ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[2]);
//        assertTrue("redo available", canRedo[2]);
//        assertTrue("Next undo", undoNamesMatch[2]);
//        assertTrue("Next redo", redoNamesMatch[0]);
//
//        graph.undo();
//
//        checkElementCount(0, 0);
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[3] = undoMgr.canUndo();
//                canRedo[3] = undoMgr.canRedo();
//                redoNamesMatch[1] = "Redo original load".equals(undoMgr.getRedoPresentationName());
//            });
//        } catch (InterruptedException | InvocationTargetException ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertFalse("undo not available", canUndo[3]);
//        assertTrue("redo available", canRedo[3]);
//        assertTrue("Next redo", redoNamesMatch[1]);
//    }
//    @Test
//    public void vertexUndoRedoTest() throws InterruptedException {
//        final boolean[] canUndo = new boolean[4];
//        final boolean[] canRedo = new boolean[4];
//        final boolean[] undoNamesMatch = new boolean[4];
//        final boolean[] redoNamesMatch = new boolean[1];
//
//        checkBaseElements();
//        checkElementCount(7, 5);
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[0] = undoMgr.canUndo();
//                canRedo[0] = undoMgr.canRedo();
//                undoNamesMatch[0] = "Undo original load".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (InterruptedException | InvocationTargetException ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[0]);
//        assertFalse("redo not available", canRedo[0]);
//        assertTrue("last undo", undoNamesMatch[0]);
//
//        addNode();
//
//        checkElementCount(8, 5);
//        checkNewNode();
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[1] = undoMgr.canUndo();
//                canRedo[1] = undoMgr.canRedo();
//                undoNamesMatch[1] = "Undo add vertex".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (Exception ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[1]);
//        assertFalse("redo not available", canRedo[1]);
//        assertTrue("last undo", undoNamesMatch[1]);
//
//        graph.undo();
//
//        checkElementCount(7, 5);
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[2] = undoMgr.canUndo();
//                canRedo[2] = undoMgr.canRedo();
//                undoNamesMatch[2] = "Undo original load".equals(undoMgr.getUndoPresentationName());
//                redoNamesMatch[0] = "Redo add vertex".equals(undoMgr.getRedoPresentationName());
//            });
//        } catch (Exception ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[2]);
//        assertTrue("redo available", canRedo[2]);
//        assertTrue("last undo", undoNamesMatch[2]);
//        assertTrue("last redo", redoNamesMatch[0]);
//
//        graph.redo();
//
//        checkElementCount(8, 5);
//        checkNewNode();
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[3] = undoMgr.canUndo();
//                canRedo[3] = undoMgr.canRedo();
//                undoNamesMatch[3] = "Undo add vertex".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (Exception ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[3]);
//        assertFalse("redo not available", canRedo[3]);
//        assertTrue("last undo", undoNamesMatch[3]);
//    }
//    @Test
//    public void undoOneOperationsTest() throws InterruptedException {
//        final boolean[] canUndo = new boolean[3];
//        final boolean[] canRedo = new boolean[3];
//        final boolean[] undoNamesMatch = new boolean[3];
//        final boolean[] redoNamesMatch = new boolean[1];
//
//        checkBaseElements();
//        checkElementCount(7, 5);
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[0] = undoMgr.canUndo();
//                canRedo[0] = undoMgr.canRedo();
//                undoNamesMatch[0] = "Undo original load".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (Exception ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[0]);
//        assertFalse("redo not available", canRedo[0]);
//        assertTrue("last undo", undoNamesMatch[0]);
//
//        addNode();
//
//        checkElementCount(8, 5);
//        checkNewNode();
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[1] = undoMgr.canUndo();
//                canRedo[1] = undoMgr.canRedo();
//                undoNamesMatch[1] = "Undo add vertex".equals(undoMgr.getUndoPresentationName());
//            });
//        } catch (Exception ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[1]);
//        assertFalse("redo not available", canRedo[1]);
//        assertTrue("Next undo", undoNamesMatch[1]);
//
//        graph.undo();
//
//        checkElementCount(7, 5);
//        checkBaseElements();
//        try {
//            SwingUtilities.invokeAndWait(() -> {
//                canUndo[2] = undoMgr.canUndo();
//                canRedo[2] = undoMgr.canRedo();
//                undoNamesMatch[2] = "Undo original load".equals(undoMgr.getUndoPresentationName());
//                redoNamesMatch[0] = "Redo add vertex".equals(undoMgr.getRedoPresentationName());
//            });
//        } catch (Exception ex) {
//            assertFalse(ex.toString(), false);
//        }
//        assertTrue("undo available", canUndo[2]);
//        assertTrue("redo available", canRedo[2]);
//        assertTrue("Next undo", undoNamesMatch[2]);
//        assertTrue("Next redo", redoNamesMatch[0]);
//
//        checkBaseElements();
//    }
    // add new node
    private void addNode() throws InterruptedException {
        WritableGraph wg = graph.getWritableGraph("add vertex", true);
        try {
            int id = wg.addVertex();
            wg.setFloatValue(attrX, id, 126.0f);
            wg.setFloatValue(attrY, id, 126.60f);
            wg.setBooleanValue(vSelAttr, id, true);
            wg.setStringValue(vNameAttr, id, "name10");
        } finally {
            wg.commit();
        }
    }

    // check base information
    private void checkBaseElements() {
        ReadableGraph rg = graph.getReadableGraph();
        try {
            assertTrue("nd 'name1' found", nodeFound(rg, "name1"));
            assertTrue("nd 'name2' found", nodeFound(rg, "name2"));
            assertTrue("nd 'name3' found", nodeFound(rg, "name3"));
            assertTrue("nd 'name4' found", nodeFound(rg, "name4"));
            assertTrue("nd 'name5' found", nodeFound(rg, "name5"));
            assertTrue("nd 'name6' found", nodeFound(rg, "name6"));
            assertTrue("nd 'name7' found", nodeFound(rg, "name7"));
            assertTrue("tx 'name101' found", transactionFound(rg, "name101"));
            assertTrue("tx 'name102' found", transactionFound(rg, "name102"));
            assertTrue("tx 'name103' found", transactionFound(rg, "name103"));
            assertTrue("tx 'name104' found", transactionFound(rg, "name104"));
            assertTrue("tx 'name105' found", transactionFound(rg, "name105"));
        } finally {
            rg.release();
        }
    }

    // check base information
    private void checkElementCount(int ndCount, int txCount) {
        ReadableGraph rg = graph.getReadableGraph();
        try {
            assertEquals("node count", ndCount, rg.getVertexCount());
            assertEquals("transaction count", txCount, rg.getTransactionCount());
        } finally {
            rg.release();
        }
    }

    // check base information
    private void checkNewNode() {
        ReadableGraph rg = graph.getReadableGraph();
        try {
            assertTrue("nd 'name10' found", nodeFound(rg, "name10"));
        } finally {
            rg.release();
        }
    }

    // determine whether the node of the specified name exists in the graph
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
