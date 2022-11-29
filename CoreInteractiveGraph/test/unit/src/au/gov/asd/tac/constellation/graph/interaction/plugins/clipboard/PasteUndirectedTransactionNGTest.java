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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Paste Undirected Transaction Test.
 *
 * @author algol
 */
public class PasteUndirectedTransactionNGTest {

    public PasteUndirectedTransactionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Test
//    public void controlUAndPasteWhichBreaks() {
////        System.setProperty("netbeans.full.hack", "true");
//        final String name0 = "node0";
//        final String name1 = "node1";
//
//        final StoreGraph graph = new StoreGraph();
//        final int nameAttr = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "name", "", "", null);
//        final int selectedVertexAttr = graph.addAttribute(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "", "", null);
//        final int selectedTransactionAttr = graph.addAttribute(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected", "", "", null);
//        int[] keys = new int[1];
//        keys[0] = nameAttr;
//        graph.setPrimaryKey(GraphElementType.VERTEX, keys);
//
//        graph.addVertex(0);
//        graph.setStringValue(nameAttr, 0, name0);
//
//        graph.addVertex(1);
//        graph.setStringValue(nameAttr, 1, name1);
//
//        // Add an undirected transaction between the two vertices
//        int transId = graph.addTransaction(0, 1, false);
//
//        // Select everything and copy to new graph
//        graph.setBooleanValue(selectedVertexAttr, 0, true);
//        graph.setBooleanValue(selectedVertexAttr, 1, true);
//        graph.setBooleanValue(selectedTransactionAttr, transId, true);
//        Graph graph2 = null;
//        try {
//            graph2 = CopySelectedElementsPlugin.makeGraph(graph, null, true, false);
//        } catch (InterruptedException ex) {
//            assert false;
//        }
//
//        ReadableGraph rg = graph2.getReadableGraph();
//        assert rg.getBooleanValue(selectedVertexAttr, 0) == true;
//        assert rg.getBooleanValue(selectedVertexAttr, 1) == true;
//        rg.release();
//
//        // Switch the names of the two vertices in the new graph
//        try {
//            WritableGraph gwm = graph2.getWritableGraph("renaming", false);
//            gwm.setStringValue(nameAttr, 0, name1);
//            gwm.setStringValue(nameAttr, 1, name0);
//            gwm.commit();
//        } catch (InterruptedException ex) {
//            assert false;
//        }
//
//        // In the original graph, deselect vertex with id 1 and delete everything else
//        graph.setBooleanValue(selectedVertexAttr, 0, false);
//        try {
//            PluginExecution.withPlugin(new DeleteSelectionPlugin()).executeNow(graph);
//        } catch (InterruptedException | PluginException ex) {
//            assert false;
//        }
//
//        // used so we can undo
//        DualGraph dualGraph = new DualGraph(null, graph);
//        dualGraph.setUndoManager(new UndoRedo.Manager());
//
//        // Copy and paste everything from graph2 into graph
//        try {
//            PluginExecution.withPlugin(new CopyPlugin()).executeNow(graph2);
//            PluginExecution.withPlugin(new PastePlugin()).executeNow(dualGraph);
//        } catch (InterruptedException | PluginException ex) {
//            ex.printStackTrace(System.out);
//            assert false;
//        }
//
//        // Try to undo the paste - should fail here
//        SwingUtilities.invokeAndWait(() -> {
//            try {
//                dualGraph.undo();
//            } catch (Exception ex) {
//                assert true;
//            }
//        });
//    }
//    @Test
//    public void testUndoUndirected() throws InterruptedException {
//        final DualGraph g = new DualGraph(null);
//        final UndoRedo.Manager urm = new UndoRedo.Manager();
//        g.setUndoManager(urm);
//        WritableGraph wg = g.getWritableGraph("add vertices", true);
//        final int vxId0 = wg.addVertex();
//        final int vxId1 = wg.addVertex();
//        final int vxId2 = wg.addVertex();
//        wg.commit();
//
//        wg = g.getWritableGraph("add transaction", true);
//        final int txId = wg.addTransaction(vxId0, vxId1, false);
//        Assert.assertEquals(vxId0, wg.getTransactionSourceVertex(txId));
//        Assert.assertEquals(vxId1, wg.getTransactionDestinationVertex(txId));
//        wg.commit();
//
//        wg = g.getWritableGraph("move source", true);
//        wg.setTransactionSourceVertex(txId, vxId2);
//        Assert.assertEquals(vxId1, wg.getTransactionSourceVertex(txId));
//        Assert.assertEquals(vxId2, wg.getTransactionDestinationVertex(txId));
//        wg.commit();
//
//        System.out.printf("@@URM 0 %s\n", urm.getUndoPresentationName());
//        SwingUtilities.invokeAndWait(() -> {
//            g.undo();
//        });
////        System.out.printf("@@URM 1 %s\n", urm.getUndoPresentationName());
////        g.undo();
////        System.out.printf("@@URM 2 %s\n", urm.getUndoPresentationName());
////        g.undo();
//
//        ReadableGraph rg = g.getReadableGraph();
//        Assert.assertEquals(vxId0, wg.getTransactionSourceVertex(txId));
//        Assert.assertEquals(vxId1, wg.getTransactionDestinationVertex(txId));
//        rg.release();
//    }
}
