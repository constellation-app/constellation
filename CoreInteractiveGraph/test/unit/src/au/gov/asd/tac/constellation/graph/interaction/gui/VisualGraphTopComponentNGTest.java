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
package au.gov.asd.tac.constellation.graph.interaction.gui;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.interaction.framework.GraphVisualManagerFactory;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.Lookup;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class VisualGraphTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(VisualGraphTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            if (!FxToolkit.isFXApplicationThreadRunning()) {
                FxToolkit.registerPrimaryStage();
            }
        } catch (Exception e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        } catch (Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }
    }

//    /**
//     * Test of getUndoRedo method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testGetUndoRedo() {
//        System.out.println("getUndoRedo");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        UndoRedo expResult = null;
//        UndoRedo result = instance.getUndoRedo();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of requestActive method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testRequestActive() {
//        System.out.println("requestActive");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        instance.requestActive();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDisplayName method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testGetDisplayName() {
//        System.out.println("getDisplayName");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        String expResult = "";
//        String result = instance.getDisplayName();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGraphNode method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testGetGraphNode() {
//        System.out.println("getGraphNode");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        GraphNode expResult = null;
//        GraphNode result = instance.getGraphNode();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of componentOpened method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testComponentOpened() {
//        System.out.println("componentOpened");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        instance.componentOpened();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of componentClosed method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testComponentClosed() {
//        System.out.println("componentClosed");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        instance.componentClosed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of graphChanged method, of class VisualGraphTopComponent.
     */
//    @Test
//    public void testGraphChanged() {
//        System.out.println("graphChanged");
//
//        System.setProperty("java.awt.headless", "true");
//        GraphChangeEvent evt = null;
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        instance.graphChanged(evt);
//
//        System.clearProperty("java.awt.headless");
//
//    }
//    /**
//     * Test of finalize method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testFinalize() throws Exception {
//        System.out.println("finalize");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        instance.finalize();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of canClose method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testCanClose() {
//        System.out.println("canClose");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        boolean expResult = false;
//        boolean result = instance.canClose();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getActions method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testGetActions() {
//        System.out.println("getActions");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        Action[] expResult = null;
//        Action[] result = instance.getActions();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of forceClose method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testForceClose() {
//        System.out.println("forceClose");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        boolean expResult = false;
//        boolean result = instance.forceClose();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     */
    @Test
    public void testSaveGraphNotInMemory() throws Exception {
        System.out.println("saveGraph not in memeory");

        //System.setProperty("java.awt.headless", "true");
        // Mock
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        when(mockGDO.isInMemory()).thenReturn(true);

        final VisualManager mockVisualManager = mock(VisualManager.class);

        final GraphVisualManagerFactory mockGraphVisualManagerFactory = mock(GraphVisualManagerFactory.class);
        when(mockGraphVisualManagerFactory.constructVisualManager(any())).thenReturn(mockVisualManager);

        final Lookup mockLookup = mock(Lookup.class);
        when(mockLookup.lookup(any(Class.class))).thenReturn(mockGraphVisualManagerFactory);


        // Mock contruct save as action, GraphNode
        try 
//            (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class); 
//                MockedStatic<Lookup> mockStaticLookup = Mockito.mockStatic(Lookup.class); 
//                MockedConstruction<DualGraph> mockGraph = Mockito.mockConstruction(DualGraph.class); 
//  
//                ) 
        {
            
            
//            mockStaticLookup.when(Lookup::getDefault).thenReturn(mockLookup);
//            // Checking mocks work correctly
//            assertEquals(Lookup.getDefault(), mockLookup);
//            assertEquals(mockLookup.lookup(Graph.class), mockGraphVisualManagerFactory);
//            assertEquals(mockGraphVisualManagerFactory.constructVisualManager(new DualGraph(null)), mockVisualManager);
//            
//            assertEquals(mockGraphVisualManagerFactory, Lookup.getDefault().lookup(GraphVisualManagerFactory.class));
//            
//            assertEquals(mockGraph.constructed().size(), 1);
                   

            VisualGraphTopComponent instance = new VisualGraphTopComponent();
//            instance.getGraphNode().setDataObject(mockGDO);
//            instance.saveGraph();
//
//            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
//            verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
//            verify(mockSaveAsAction.constructed().get(0)).isSaved();
        }catch(Exception e){}

        //System.clearProperty("java.awt.headless");

    }

//    /**
//     * Test of saveGraph method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testSaveGraphInvalid() throws Exception {
//        System.out.println("saveGraph invalid");
//
//        System.setProperty("java.awt.headless", "true");
//        final GraphDataObject mockGDO = mock(GraphDataObject.class);
//        when(mockGDO.isValid()).thenReturn(false);
//        // Mock contruct save as action, GraphNode
//        try (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class); //                MockedConstruction<GraphNode> mockGraphNode = Mockito.mockConstruction(GraphNode.class, (mock, context) -> {
//                //            when(mock.getDataObject()).thenReturn(mockGDO);
//                //        })
//                ) {
//
//            VisualGraphTopComponent instance = new VisualGraphTopComponent();
//            instance.getGraphNode().setDataObject(mockGDO);
//            instance.saveGraph();
//
//            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
//            assertEquals(mockSaveAsAction.constructed().size(), 1);
//            verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
//            verify(mockSaveAsAction.constructed().get(0)).isSaved();
//
////            assertEquals(1, mockGraphNode.constructed().size());
////            verify(mockGraphNode.constructed().get(0)).getDataObject();
//        }
//        System.clearProperty("java.awt.headless");
//    }
//    /**
//     * Test of getHelpCtx method, of class VisualGraphTopComponent.
//     */
//    @Test
//    public void testGetHelpCtx() {
//        System.out.println("getHelpCtx");
//        VisualGraphTopComponent instance = new VisualGraphTopComponent();
//        HelpCtx expResult = null;
//        HelpCtx result = instance.getHelpCtx();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
