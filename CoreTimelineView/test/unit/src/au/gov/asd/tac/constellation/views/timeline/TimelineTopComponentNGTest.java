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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.time.ZoneId;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class TimelineTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(TimelineTopComponentNGTest.class.getName());

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

//    /**
//     * Test of setExtents method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testSetExtents_double_double() {
//        System.out.println("setExtents");
//        double lowerTimeExtent = 0.0;
//        double upperTimeExtent = 0.0;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.setExtents(lowerTimeExtent, upperTimeExtent);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of setExtents method, of class TimelineTopComponent.
     */
    @Test
    public void testSetExtentsNoTransactions() {
        System.out.println("setExtents no transactions");
        final Registry mockRegistry = mock(Registry.class);
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);

        final Node[] activatedNodes = {mockGraphNode};
        final int txCount = 0;
        final String currentDatetimeAttribute = "mockedCurrentDatetimeAttribute";
        final int txTimAttrId = 101;
        final int txSelAttrId = 202;

        when(mockRegistry.getActivatedNodes()).thenReturn(activatedNodes);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);
        when(mockReadableGraph.getTransactionCount()).thenReturn(txCount);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute)).thenReturn(txTimAttrId);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName())).thenReturn(txSelAttrId);

        // Create and setup instance
        final TimelineTopComponent instance = new TimelineTopComponent();
        instance.setNode(mockGraphNode);
        instance.setCurrentDatetimeAttr(currentDatetimeAttribute);

        instance.setExtents();
    }

    /**
     * Test of setExtents method, of class TimelineTopComponent.
     */
    @Test
    public void testSetExtentsTransactionNullDate() {
        System.out.println("setExtents Transaction with null date");
        final Registry mockRegistry = mock(Registry.class);
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);

        final Node[] activatedNodes = {mockGraphNode};
        final int txCount = 1;
        final String currentDatetimeAttribute = "mockedCurrentDatetimeAttribute";
        final int txTimAttrId = 101;
        final int txSelAttrId = 202;

        final String dateTimeString = null;

        when(mockRegistry.getActivatedNodes()).thenReturn(activatedNodes);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        when(mockReadableGraph.getTransactionCount()).thenReturn(txCount);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute)).thenReturn(txTimAttrId);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName())).thenReturn(txSelAttrId);
        when(mockReadableGraph.getStringValue(txTimAttrId, 0)).thenReturn(dateTimeString);

        // Create and setup instance
        final TimelineTopComponent instance = new TimelineTopComponent();
        instance.setNode(mockGraphNode);
        instance.setCurrentDatetimeAttr(currentDatetimeAttribute);

        instance.setExtents();
    }

    /**
     * Test of setExtents method, of class TimelineTopComponent.
     */
    @Test
    public void testSetExtentsTransactionValidDate() {
        System.out.println("setExtents Transaction with valid date");
        //final Registry mockRegistry = mock(Registry.class);
        final TimelinePanel mockTimelinePanel = mock(TimelinePanel.class);
        final OverviewPanel mockOverviewPanel = mock(OverviewPanel.class);
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        final TimelineState mockState = mock(TimelineState.class);

        final int txCount = 1;
        final String currentDatetimeAttribute = null;
        final int txTimAttrId = 101;
        final int txSelAttrId = 202;
        final int attrID = 303;

        final String dateTimeString = "not null";
        final long dateTimeLong = 0;

        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        when(mockReadableGraph.getTransactionCount()).thenReturn(txCount);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute)).thenReturn(txTimAttrId);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName())).thenReturn(txSelAttrId);
        when(mockReadableGraph.getStringValue(txTimAttrId, 0)).thenReturn(dateTimeString);
        when(mockReadableGraph.getLongValue(txTimAttrId, 0)).thenReturn(dateTimeLong);
        when(mockReadableGraph.getAttribute(GraphElementType.META, TimelineConcept.MetaAttribute.TIMELINE_STATE.getName())).thenReturn(attrID);
        when(mockReadableGraph.getObjectValue(attrID, 0)).thenReturn(mockState);
        when(mockReadableGraph.getBooleanValue(txSelAttrId, 0)).thenReturn(true);

        assertEquals(mockReadableGraph.getStringValue(txTimAttrId, 0), dateTimeString);

        // Create and setup instance
        final TimelineTopComponent instance = new TimelineTopComponent();

        instance.setNode(mockGraphNode);
        instance.setTimelinePanel(mockTimelinePanel);
        instance.setOverviewPanel(mockOverviewPanel);
        //instance.setState(mockState);

        instance.setExtents();
    }
//    /**
//     * Test of getTimelineLowerTimeExtent method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testGetTimelineLowerTimeExtent() {
//        System.out.println("getTimelineLowerTimeExtent");
//        TimelineTopComponent instance = new TimelineTopComponent();
//        double expResult = 0.0;
//        double result = instance.getTimelineLowerTimeExtent();
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTimelineUpperTimeExtent method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testGetTimelineUpperTimeExtent() {
//        System.out.println("getTimelineUpperTimeExtent");
//        TimelineTopComponent instance = new TimelineTopComponent();
//        double expResult = 0.0;
//        double result = instance.getTimelineUpperTimeExtent();
//        assertEquals(result, expResult, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of componentOpened method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testComponentOpened() {
//        System.out.println("componentOpened");
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.componentOpened();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of zoomFromOverview method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testZoomFromOverview() {
//        System.out.println("zoomFromOverview");
//        ScrollEvent se = null;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.zoomFromOverview(se);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of componentClosed method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testComponentClosed() {
//        System.out.println("componentClosed");
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.componentClosed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeProperties method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testWriteProperties() {
//        System.out.println("writeProperties");
//        Properties p = null;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.writeProperties(p);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readProperties method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testReadProperties() {
//        System.out.println("readProperties");
//        Properties p = null;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.readProperties(p);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCurrentDatetimeAttr method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testSetCurrentDatetimeAttr() {
//        System.out.println("setCurrentDatetimeAttr");
//        String currentDatetimeAttr = "";
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.setCurrentDatetimeAttr(currentDatetimeAttr);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateTimeZone method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testUpdateTimeZone() {
//        System.out.println("updateTimeZone");
//        ZoneId timeZone = null;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.updateTimeZone(timeZone);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setExclusionState method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testSetExclusionState() {
//        System.out.println("setExclusionState");
//        int exclusionState = 0;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.setExclusionState(exclusionState);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setIsShowingSelectedOnly method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testSetIsShowingSelectedOnly() {
//        System.out.println("setIsShowingSelectedOnly");
//        boolean isShowingSelectedOnly = false;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.setIsShowingSelectedOnly(isShowingSelectedOnly);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setIsShowingNodeLabels method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testSetIsShowingNodeLabels() {
//        System.out.println("setIsShowingNodeLabels");
//        boolean isShowingNodeLabels = false;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.setIsShowingNodeLabels(isShowingNodeLabels);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setNodeLabelsAttr method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testSetNodeLabelsAttr() {
//        System.out.println("setNodeLabelsAttr");
//        String nodeLabelsAttr = "";
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.setNodeLabelsAttr(nodeLabelsAttr);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNodeLabelsAttr method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testGetNodeLabelsAttr() {
//        System.out.println("getNodeLabelsAttr");
//        TimelineTopComponent instance = new TimelineTopComponent();
//        String expResult = "";
//        String result = instance.getNodeLabelsAttr();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getUndoRedo method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testGetUndoRedo() {
//        System.out.println("getUndoRedo");
//        TimelineTopComponent instance = new TimelineTopComponent();
//        UndoRedo expResult = null;
//        UndoRedo result = instance.getUndoRedo();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resultChanged method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testResultChanged() {
//        System.out.println("resultChanged");
//        LookupEvent ev = null;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.resultChanged(ev);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of graphChanged method, of class TimelineTopComponent.
//     */
//    @Test
//    public void testGraphChanged() {
//        System.out.println("graphChanged");
//        GraphChangeEvent evt = null;
//        TimelineTopComponent instance = new TimelineTopComponent();
//        instance.graphChanged(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
