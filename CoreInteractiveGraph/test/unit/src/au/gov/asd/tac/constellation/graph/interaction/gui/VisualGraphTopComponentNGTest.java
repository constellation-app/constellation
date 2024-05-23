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
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.SaveAsAction;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class VisualGraphTopComponentNGTest {

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
    @Test
    public void testGraphChanged() {
        System.out.println("graphChanged");
        GraphChangeEvent evt = null;
        VisualGraphTopComponent instance = new VisualGraphTopComponent();
        instance.graphChanged(evt);
    }

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
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        when(mockGDO.isInMemory()).thenReturn(true);
        // Mock contruct save as action, GraphNode
        try (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class); //MockedConstruction<VisualGraphTopComponent.BackgroundWriter> mockBackgroundWriter = Mockito.mockConstruction(VisualGraphTopComponent.BackgroundWriter.class)  
                ) {

            VisualGraphTopComponent instance = new VisualGraphTopComponent();
            instance.getGraphNode().setDataObject(mockGDO);
            instance.saveGraph();

            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
            verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
            verify(mockSaveAsAction.constructed().get(0)).isSaved();
        }
    }

    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     */
    @Test
    public void testSaveGraphInvalid() throws Exception {
        System.out.println("saveGraph invalid");
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        when(mockGDO.isValid()).thenReturn(false);
        // Mock contruct save as action, GraphNode
        try (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class); //                MockedConstruction<GraphNode> mockGraphNode = Mockito.mockConstruction(GraphNode.class, (mock, context) -> {
                //            when(mock.getDataObject()).thenReturn(mockGDO);
                //        })
                ) {

            VisualGraphTopComponent instance = new VisualGraphTopComponent();
            instance.getGraphNode().setDataObject(mockGDO);
            instance.saveGraph();

            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
            assertEquals(mockSaveAsAction.constructed().size(), 1);
            verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
            verify(mockSaveAsAction.constructed().get(0)).isSaved();

//            assertEquals(1, mockGraphNode.constructed().size());
//            verify(mockGraphNode.constructed().get(0)).getDataObject();
        }
    }

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
