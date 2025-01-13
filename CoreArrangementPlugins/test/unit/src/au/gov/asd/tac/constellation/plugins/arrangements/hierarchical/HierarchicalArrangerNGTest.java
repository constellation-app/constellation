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
package au.gov.asd.tac.constellation.plugins.arrangements.hierarchical;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities;
import au.gov.asd.tac.constellation.views.namedselection.utilities.SelectNamedSelectionPanel;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author OrionsGuardian
 */
public class HierarchicalArrangerNGTest {

    private List<Integer> vtxList;
    private List<Integer> txnList;
    private Graph graph;
    private boolean delayedSave = false;
    
    // change this to true if you want to see the graph files in local testing
    // but remember to set it back to false when committing.
    // It should always be false in the repo.
    private static final boolean SAVE_GRAPH_FILES = false;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        if (delayedSave) {
            // This sleep should only be called when running locally if you want to view the arranged graph.
            // The 5 second wait covers the 3 second allowance given for arranging a small complex graph.
            Thread.sleep(5000); // NOSONAR
            saveGraphToFile("hierarchy-arrangement");
            delayedSave = false;
        }
    }

    /**
     * Set up a graph with four vertices and three transactions
     */
    private void setupGraph(final int nodeCount) throws InterruptedException {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        vtxList = new ArrayList<>();
        txnList = new ArrayList<>();
        
        final WritableGraph wg = graph.getWritableGraph("", true);
        
        // add attributes
        final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
        VisualConcept.VertexAttribute.X.ensure(wg);
        VisualConcept.VertexAttribute.Y.ensure(wg);
        VisualConcept.VertexAttribute.Z.ensure(wg);
        VisualConcept.VertexAttribute.NODE_RADIUS.ensure(wg);

        // add vertices
        for (int j = 0; j < nodeCount; j++) {
            vtxList.add(wg.addVertex());
        }
        for (final int vtxId : vtxList) {
            wg.setStringValue(vertexIdentifierAttribute, vtxId, "Vtx-" + vtxId);
        }

        int vtxIndex = 0;
        // add transactions
        List<Integer> parentLevel = new ArrayList<>();
        List<Integer> childLevel = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            parentLevel.add(vtxList.get(vtxIndex++));
        }
        while (vtxIndex < vtxList.size()) {
            int levelIndex = 0;
            while (levelIndex < parentLevel.size()) {
                childLevel.add(vtxList.get(vtxIndex++));
                if (vtxIndex >= vtxList.size()) {
                    break;
                }
                childLevel.add(vtxList.get(vtxIndex++));
                if (vtxIndex >= vtxList.size()) {
                    break;
                }
                levelIndex++;
            }
            int childPos = 0;
            for (final Integer parentId : parentLevel) {
                // link parents to children
                txnList.add(wg.addTransaction(parentId, childLevel.get(childPos++), true));
                if (childPos >= childLevel.size()) {
                    childPos = 0;
                }
                txnList.add(wg.addTransaction(parentId, childLevel.get(childPos++), true));
                childPos++;
                if (childPos >= childLevel.size()) {
                    childPos = 0;
                }
            }    
            parentLevel = childLevel;                
            childLevel = new ArrayList<>();
            // remove some entries in the parentLevel ... not all the children will be parents ... you know, diversity and all
            final int parentCount = parentLevel.size();
            for (int c = parentCount - 1; c > -1; c--) {
                if (c%3 == 0) {
                    parentLevel.remove(c);
                }
            }
        }

        wg.commit();
    }
    
    /**
     * Test of arrange method, of class HierarchicalArranger, with 150 nodes.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testArrange150() throws InterruptedException {
        System.out.println("Test hierarchy arrangement 150");
        setupGraph(150);

        final WritableGraph wg = graph.getWritableGraph("", true);
        final Set<Integer> rootVxIds = new HashSet<>();
        rootVxIds.add(vtxList.get(0));
        rootVxIds.add(vtxList.get(1));
        rootVxIds.add(vtxList.get(2));
        rootVxIds.add(vtxList.get(3));

        final HierarchicalArranger instance = new HierarchicalArranger(rootVxIds);
        instance.arrange(wg);
        final String lastMessage = instance.getLastMessage();            
        assertTrue(lastMessage.contains("pass: 5"));

        wg.commit();
        saveGraphToFile("hierarchy150_test");
    }


    /**
     * Test of arrange method, of class HierarchicalArranger, with 1500 nodes.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testArrange1500() throws InterruptedException {
        System.out.println("Test hierarchy arrangement 1500");
        setupGraph(1500);
        
        final WritableGraph wg = graph.getWritableGraph("", true);
        final Set<Integer> rootVxIds = new HashSet<>();
        rootVxIds.add(vtxList.get(0));
        rootVxIds.add(vtxList.get(1));
        rootVxIds.add(vtxList.get(2));
        rootVxIds.add(vtxList.get(3));

        final HierarchicalArranger instance = new HierarchicalArranger(rootVxIds);
        instance.arrange(wg);
        final String lastMessage = instance.getLastMessage();
        assertTrue(lastMessage.contains("pass: 10"));

        wg.commit();
        saveGraphToFile("hierarchy1500_test");
    }


    /**
     * Test of arrange method, of class HierarchicalArranger, with 15000 nodes.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testArrange15000() throws InterruptedException {
        System.out.println("Test hierarchy arrangement 15000");
        setupGraph(15000);

        final WritableGraph wg = graph.getWritableGraph("", true);
        final Set<Integer> rootVxIds = new HashSet<>();
        rootVxIds.add(vtxList.get(0));
        rootVxIds.add(vtxList.get(1));
        rootVxIds.add(vtxList.get(2));
        rootVxIds.add(vtxList.get(3));

        final HierarchicalArranger instance = new HierarchicalArranger(rootVxIds);
        instance.arrange(wg);
        final String lastMessage = instance.getLastMessage();
        assertTrue(lastMessage.contains("no smoothing passes"));

        wg.commit();
        saveGraphToFile("hierarchy15000_test");
    }

    private void saveGraphToFile(final String filename) throws InterruptedException {
        if (SAVE_GRAPH_FILES) {
            try {
                SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, filename, true);
                System.out.println("Saved graph to " + System.getProperty("java.io.tmpdir") + filename);
            } catch (IOException ex) {
                System.out.println(" >> error saving graph");
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Test of arrange method, of class HierarchicalArranger, with 250 nodes, running in background via the action call.
     * This improves code coverage but does not allow checking of results due to the background task running after the test code has already completed.
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testArrange250viaAction() throws InterruptedException {
        System.out.println("Test hierarchy arrangement 250 via Action call");
        setupGraph(250);

        final WritableGraph wg = graph.getWritableGraph("", true);
        final int selectedVetexId = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
        wg.setBooleanValue(selectedVetexId, vtxList.get(0), true);
        wg.setBooleanValue(selectedVetexId, vtxList.get(1), true);
        wg.setBooleanValue(selectedVetexId, vtxList.get(2), true);
        wg.setBooleanValue(selectedVetexId, vtxList.get(3), true);

        final GraphNode contextMock = mock(GraphNode.class);
        doReturn(graph).when(contextMock).getGraph();

        final SelectNamedSelectionPanel ssp = mock(SelectNamedSelectionPanel.class);
        doReturn(-2L).when(ssp).getNamedSelectionId();

        final MockedStatic<DialogDisplayer> ddStatic = mockStatic(DialogDisplayer.class);
        ddStatic.when(DialogDisplayer::getDefault).thenReturn(new MockedDialogDisplayer());

        final ArrangeInHierarchyAction hierAction = new ArrangeInHierarchyAction(contextMock);
        hierAction.actionPerformed(null);

        wg.commit();
        if (SAVE_GRAPH_FILES) {
            delayedSave = true;
        }
        
    }
    
    private class MockedDialogDisplayer extends DialogDisplayer {

        @Override
        public Object notify(NotifyDescriptor nd) {
            return DialogDescriptor.OK_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor dd) {
            throw new UnsupportedOperationException("Not supported yet.");
        }        
    }
}
