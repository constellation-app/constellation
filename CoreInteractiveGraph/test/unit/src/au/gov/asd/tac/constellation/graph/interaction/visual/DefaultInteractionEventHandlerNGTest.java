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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.interaction.framework.HitState.HitType;
import au.gov.asd.tac.constellation.graph.interaction.framework.VisualAnnotator;
import au.gov.asd.tac.constellation.graph.interaction.framework.VisualInteraction;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class DefaultInteractionEventHandlerNGTest {

    private int vertexIdentifierAttribute;
    private int vertexTypeAttribute;
    private int vertexSelectedAttribute;

    private int vxId1;
    private int vxId2;
    private int vxId3;

    private int txId1;
    private int txId2;

    private Graph graph;
    private WritableGraph wg;
    private VisualManager manager;
    private VisualInteraction visualInteraction;
    private VisualAnnotator visualAnnotator;

    //Dependencies (will be mocked)
    private VisualGraphUtilities mockVisualGraphUtilities;

    DefaultInteractionEventHandler instance = new DefaultInteractionEventHandler(graph, manager, visualInteraction, visualAnnotator);

    public DefaultInteractionEventHandlerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // create an analytic graph
//        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
//        graph = new Graph(schema);
        graph = new DualGraph(null);
        wg = graph.getWritableGraph("DefaultInteractionEventHandlerNGTest", true);

        // add attributes
        vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
        vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(wg);
        vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(wg);

        // add vertices
        vxId1 = wg.addVertex();
        vxId2 = wg.addVertex();
        vxId3 = wg.addVertex();
        txId1 = wg.addTransaction(vxId1, vxId2, true);
        txId2 = wg.addTransaction(vxId2, vxId3, false);

        wg.setBooleanValue(vertexSelectedAttribute, vxId1, true);
        wg.setBooleanValue(vertexSelectedAttribute, vxId2, false);
        wg.setBooleanValue(vertexSelectedAttribute, vxId3, false);

        this.manager = manager;
        this.visualInteraction = visualInteraction;
        this.visualAnnotator = visualAnnotator;
        this.wg = wg;

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of startHandlingEvents method, of class
     * DefaultInteractionEventHandler.
     */
//    @Test
//    public void testStartHandlingEvents() {
//        System.out.println("startHandlingEvents");
//        instance.startHandlingEvents();
//
//////        @Mock
////        BlockingQueue<instance.GestureHandler> queue = mock(BlockingQueue.Class);
//    }
//    /**
//     * Test of stopHandlingEvents method, of class
//     * DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testStopHandlingEvents() {
//        System.out.println("stopHandlingEvents");
//        DefaultInteractionEventHandler instance = null;
//        instance.stopHandlingEvents();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of keyPressed method, of class DefaultInteractionEventHandler.
     */
//    @Test
//    public void testKeyPressed() {
//        System.out.println("keyPressed");
//
//        KeyEvent mockEvent = mock(KeyEvent.class);
//        when(mockEvent.getKeyCode()).thenReturn(KeyEvent.VK_PAGE_UP);
//
//        instance.keyPressed(mockEvent);
//        // TODO review the generated test code and remove the default call to fail.
//
//    }
//
//    /**
//     * Test of keyReleased method, of class DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testKeyReleased() {
//        System.out.println("keyReleased");
//        KeyEvent e = null;
//        DefaultInteractionEventHandler instance = null;
//        instance.keyReleased(e);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of keyTyped method, of class DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testKeyTyped() {
//        System.out.println("keyTyped");
//        KeyEvent e = null;
//        DefaultInteractionEventHandler instance = null;
//        instance.keyTyped(e);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mouseDragged method, of class DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testMouseDragged() {
//        System.out.println("mouseDragged");
//        MouseEvent event = null;
//        DefaultInteractionEventHandler instance = null;
//        instance.mouseDragged(event);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of mousePressed method, of class DefaultInteractionEventHandler.
     */
//    @Test
//    public void testMousePressed() {
//        System.out.println("mousePressed");
//        MouseEvent mockMouseEvent = null;
////        mockEventState = mock(EventState.class);
////        //when(mockEventState.getCurrentHitId()).thenReturn(hitId);
////
////        doReturn(hitId).when(mockEventState).getCurrentHitId();
////
////        MouseEvent mockMouseEvent = mock(MouseEvent.class);
////        when(mockMouseEvent.getKeyCode()).thenReturn(KeyEvent.VK_PAGE_UP);
//
//        instance.mousePressed(mockMouseEvent);
//
//    }
//
//    /**
//     * Test of mouseReleased method, of class DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testMouseReleased() {
//        System.out.println("mouseReleased");
//        MouseEvent event = null;
//        DefaultInteractionEventHandler instance = null;
//        instance.mouseReleased(event);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mouseMoved method, of class DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testMouseMoved() {
//        System.out.println("mouseMoved");
//        MouseEvent event = null;
//        DefaultInteractionEventHandler instance = null;
//        instance.mouseMoved(event);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mouseWheelMoved method, of class DefaultInteractionEventHandler.
//     */
//    @Test
//    public void testMouseWheelMoved() {
//        System.out.println("mouseWheelMoved");
//        MouseWheelEvent event = null;
//        DefaultInteractionEventHandler instance = null;
//        instance.mouseWheelMoved(event);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
    /**
     * Test of gatherSelectedNodes method, of class
     * DefaultInteractionEventHandler.
     */
    @Test
    public void testGatherSelectedNodes_whenHitTypeIsVertex() {
        //Arrange
        System.out.println("gatherSelectedNodes");
        GraphReadMethods rg = graph.getReadableGraph();

        final int hitId = 1;
        final List<Integer> expResult = Arrays.asList(0, hitId);

        EventState EventStateSpy = spy(new EventState());
        doReturn(hitId).when(EventStateSpy).getCurrentHitId();
        doReturn(HitType.VERTEX).when(EventStateSpy).getCurrentHitType();
        //Also works these, but no need to worry about dependency implementation:
        //mockEventState.setCurrentHitId(hitId);
        //mockEventState.setCurrentHitType(HitState.HitType.TRANSACTION);

        instance.eventState = EventStateSpy;

//        mockVisualGraphUtilities = mock(VisualGraphUtilities.class);
//        try ( MockedStatic<VisualGraphUtilities> mockVisualGraphUtilities = Mockito.mockStatic(VisualGraphUtilities.class)) {
//            // Arrange
//            // stub the static method that is called by the class under test
//            mockVisualGraphUtilities.when(() -> VisualGraphUtilities.getSelectedElements(rg)).thenReturn(selectedElements);
////            assertEquals("testing", result1);
        // Act
        List<Integer> result = instance.gatherSelectedNodes(rg);

        // Assert
        assertEquals(result.toArray(), expResult.toArray());
//        }
    }

    /**
     * Test of gatherSelectedNodes method, of class
     * DefaultInteractionEventHandler.
     */
    @Test
    public void testGatherSelectedNodes_whenHitTypeIsTransaction() {
        System.out.println("gatherSelectedNodes");
        //Arrange
        GraphReadMethods rg = graph.getReadableGraph();

        final int hitId = 1;
        final List<Integer> expResult = Arrays.asList(0, hitId, 2);

        EventState EventStateSpy = spy(new EventState());
        doReturn(hitId).when(EventStateSpy).getCurrentHitId();
        doReturn(HitType.TRANSACTION).when(EventStateSpy).getCurrentHitType();
        instance.eventState = EventStateSpy;

        // Act
        List<Integer> result = instance.gatherSelectedNodes(rg);

        // Assert
        assertEquals(result.toArray(), expResult.toArray());

    }
}
