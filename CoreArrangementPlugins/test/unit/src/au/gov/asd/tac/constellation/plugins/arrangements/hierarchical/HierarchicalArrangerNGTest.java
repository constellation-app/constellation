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
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.io.SaveGraphUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private List<Integer> vtxList, txnList;
    private Graph graph;
    private final boolean SAVE_GRAPH_FILES = false; // change this to true if you want to see the graph files in local testing
                                                    // but remember to set it back to false when committing.
    
    public HierarchicalArrangerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

        /**
     * Set up a graph with four vertices and three transactions
     */
    private void setupGraph(final int nodeCount) {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        vtxList = new ArrayList<>();
        txnList = new ArrayList<>();
        
        WritableGraph wg;
        try {
            wg = graph.getWritableGraph("", true);
        
            // add attributes
            int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
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
                //wg.setBooleanValue(selectedV, vtxId, false);
            }
            
            int vtxIndex = 0;
            // add transactions
            List<Integer> parentLevel = new ArrayList<>();
            List<Integer> childLevel = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                parentLevel.add(vtxList.get(vtxIndex++));
            }
            while (vtxIndex < vtxList.size()) {
                for (Integer parentId : parentLevel) {
                    childLevel.add(vtxList.get(vtxIndex++));
                    if (vtxIndex >= vtxList.size()) {
                        break;
                    }
                    childLevel.add(vtxList.get(vtxIndex++));
                    if (vtxIndex >= vtxList.size()) {
                        break;
                    }
                }
                int childPos = 0;
                for (Integer parentId : parentLevel) {
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
                int parentCount = parentLevel.size();
                for (int c = parentCount - 1; c > -1; c--) {
                    if (c%3 == 0) {
                        parentLevel.remove(c);
                    }
                }
            }
            
            wg.commit();
        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test of arrange method, of class HierarchicalArranger.
     */
    @Test
    public void testArrange150() throws Exception {
        System.out.println("arrange");
        setupGraph(150);
        WritableGraph wg;
        try {
            wg = graph.getWritableGraph("", true);
            final Set<Integer> rootVxIds = new HashSet<>();
            rootVxIds.add(vtxList.get(0));
            rootVxIds.add(vtxList.get(1));
            rootVxIds.add(vtxList.get(2));
            rootVxIds.add(vtxList.get(3));
            HierarchicalArranger instance = new HierarchicalArranger(rootVxIds);
            instance.arrange(wg);
            String lastMessage = instance.getLastMessage();
            System.out.println(lastMessage);
            assertTrue(lastMessage.contains("pass: 5"));
            
            saveGraphToFile("hierarchy150_test");
        } catch (final InterruptedException ex) {
            
        }
            }


    /**
     * Test of arrange method, of class HierarchicalArranger.
     */
    @Test
    public void testArrange1500() throws Exception {
        System.out.println("arrange");
        setupGraph(1500);
        WritableGraph wg;
        try {
            wg = graph.getWritableGraph("", true);
            final Set<Integer> rootVxIds = new HashSet<>();
            rootVxIds.add(vtxList.get(0));
            rootVxIds.add(vtxList.get(1));
            rootVxIds.add(vtxList.get(2));
            rootVxIds.add(vtxList.get(3));
            HierarchicalArranger instance = new HierarchicalArranger(rootVxIds);
            instance.arrange(wg);
            String lastMessage = instance.getLastMessage();
            System.out.println(lastMessage);
            assertTrue(lastMessage.contains("pass: 10"));
            
            saveGraphToFile("hierarchy1500_test");
        } catch (final InterruptedException ex) {
            
        }        
    }


    /**
     * Test of arrange method, of class HierarchicalArranger.
     */
    @Test
    public void testArrange15000() throws Exception {
        System.out.println("arrange");
        setupGraph(15000);
        WritableGraph wg;
        try {
            wg = graph.getWritableGraph("", true);
            final Set<Integer> rootVxIds = new HashSet<>();
            rootVxIds.add(vtxList.get(0));
            rootVxIds.add(vtxList.get(1));
            rootVxIds.add(vtxList.get(2));
            rootVxIds.add(vtxList.get(3));
            HierarchicalArranger instance = new HierarchicalArranger(rootVxIds);
            instance.arrange(wg);
            String lastMessage = instance.getLastMessage();
            System.out.println(lastMessage);
            assertTrue(lastMessage.contains("passes: 0"));
            
            saveGraphToFile("hierarchy15000_test");
        } catch (final InterruptedException ex) {
            
        }        
    }

    private void saveGraphToFile(final String filename) throws InterruptedException, IOException {
        if (SAVE_GRAPH_FILES) {
            SaveGraphUtilities.saveGraphToTemporaryDirectory(graph, filename, true);
            System.out.println("Saved graph to " + System.getProperty("java.io.tmpdir") + filename);
        }
    }

    
}
