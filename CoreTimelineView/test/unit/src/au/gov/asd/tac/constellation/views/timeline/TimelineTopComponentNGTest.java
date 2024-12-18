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
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.nodes.Node;
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
        final TimelineState mockState = spy(new TimelineState());

        final Node[] activatedNodes = {mockGraphNode};
        final int txCount = 0;
        final String currentDatetimeAttribute = "mockedCurrentDatetimeAttribute";
        final int txTimAttrId = 101;
        final int txSelAttrId = 202;
        final int attrID = 303;

        when(mockRegistry.getActivatedNodes()).thenReturn(activatedNodes);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);
        when(mockReadableGraph.getTransactionCount()).thenReturn(txCount);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute)).thenReturn(txTimAttrId);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName())).thenReturn(txSelAttrId);
        when(mockReadableGraph.getObjectValue(attrID, 0)).thenReturn(mockState);

        // Create and setup instance
        final TimelineTopComponent instance = new TimelineTopComponent();
        instance.setNode(mockGraphNode);
        instance.setCurrentDatetimeAttr(currentDatetimeAttribute);

        instance.setExtents();
        verify(mockState, times(0)).setLowerTimeExtent(-1.0);
        verify(mockState, times(0)).setUpperTimeExtent(1.0);
        verify(mockState, times(0)).getLowerTimeExtent();
        verify(mockState, times(0)).getUpperTimeExtent();
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
        final TimelineState mockState = spy(new TimelineState());

        final Node[] activatedNodes = {mockGraphNode};
        final int txCount = 1;
        final String currentDatetimeAttribute = "mockedCurrentDatetimeAttribute";
        final int txTimAttrId = 101;
        final int txSelAttrId = 202;
        final int attrID = 303;

        final String dateTimeString = null;

        when(mockRegistry.getActivatedNodes()).thenReturn(activatedNodes);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        when(mockReadableGraph.getTransactionCount()).thenReturn(txCount);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute)).thenReturn(txTimAttrId);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName())).thenReturn(txSelAttrId);
        when(mockReadableGraph.getStringValue(txTimAttrId, 0)).thenReturn(dateTimeString);
        when(mockReadableGraph.getObjectValue(attrID, 0)).thenReturn(mockState);

        // Create and setup instance
        final TimelineTopComponent instance = new TimelineTopComponent();
        instance.setNode(mockGraphNode);
        instance.setCurrentDatetimeAttr(currentDatetimeAttribute);

        instance.setExtents();
        verify(mockState, times(0)).setLowerTimeExtent(-1.0);
        verify(mockState, times(0)).setUpperTimeExtent(1.0);
        verify(mockState, times(0)).getLowerTimeExtent();
        verify(mockState, times(0)).getUpperTimeExtent();
    }

    /**
     * Test of setExtents method, of class TimelineTopComponent.
     */
    @Test
    public void testSetExtentsTransactionValidDate() {
        System.out.println("setExtents Transaction with valid date");
        final TimelinePanel mockTimelinePanel = mock(TimelinePanel.class);
        final OverviewPanel mockOverviewPanel = mock(OverviewPanel.class);
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        final TimelineState mockState = spy(new TimelineState());

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

        instance.setExtents();
        verify(mockState, times(1)).setLowerTimeExtent(-1.0);
        verify(mockState, times(1)).setUpperTimeExtent(1.0);
        verify(mockState, times(3)).getLowerTimeExtent();
        verify(mockState, times(3)).getUpperTimeExtent();
    }

    /**
     * Test of setExtents method, of class TimelineTopComponent.
     */
    @Test
    public void testSetExtentsGetGraphNull() {
        System.out.println("setExtents with null graphnode get graph");
        final Registry mockRegistry = mock(Registry.class);
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        final TimelineState mockState = spy(new TimelineState());

        final Node[] activatedNodes = {mockGraphNode};
        final int txCount = 1;
        final String currentDatetimeAttribute = "mockedCurrentDatetimeAttribute";
        final int txTimAttrId = 101;
        final int txSelAttrId = 202;
        final int attrID = 303;

        final String dateTimeString = null;

        when(mockRegistry.getActivatedNodes()).thenReturn(activatedNodes);
        when(mockGraphNode.getGraph()).thenReturn(mockGraph);
        when(mockGraph.getReadableGraph()).thenReturn(mockReadableGraph);

        when(mockReadableGraph.getTransactionCount()).thenReturn(txCount);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, currentDatetimeAttribute)).thenReturn(txTimAttrId);
        when(mockReadableGraph.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName())).thenReturn(txSelAttrId);
        when(mockReadableGraph.getStringValue(txTimAttrId, 0)).thenReturn(dateTimeString);
        when(mockReadableGraph.getObjectValue(attrID, 0)).thenReturn(mockState);

        // Create and setup instance
        final TimelineTopComponent instance = new TimelineTopComponent();
        instance.setNode(mockGraphNode);
        instance.setCurrentDatetimeAttr(currentDatetimeAttribute);

        // Verify certain functions were run
        verify(mockReadableGraph, times(3)).getAttribute(any(), any());

        // Replace graph wih null
        when(mockGraphNode.getGraph()).thenReturn(null);

        instance.setExtents();

        // Verify functions were NOT run any extra times
        verify(mockReadableGraph, times(3)).getAttribute(any(), any());

        // Verify these functions were NOT run
        verify(mockReadableGraph, times(0)).getTransactionCount();
        verify(mockReadableGraph, times(0)).getStringValue(anyInt(), anyInt());
        verify(mockReadableGraph, times(0)).getLongValue(anyInt(), anyInt());
        verify(mockReadableGraph, times(0)).getBooleanValue(anyInt(), anyInt());
    }

    /**
     * Test of setExtents method, of class TimelineTopComponent.
     */
    @Test
    public void testSetExtentsArguementsNullGraph() {
        System.out.println("setExtents with arguments with null graph");
        final TimelinePanel mockTimelinePanel = mock(TimelinePanel.class);
        final OverviewPanel mockOverviewPanel = mock(OverviewPanel.class);
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        final TimelineState mockState = spy(new TimelineState());

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

        // Replace graph wih null
        when(mockGraphNode.getGraph()).thenReturn(null);

        instance.setExtents(-1.0, 1.0);
        verify(mockState, times(1)).setLowerTimeExtent(-1.0);
        verify(mockState, times(1)).setUpperTimeExtent(1.0);

        // Verify these were NOT run
        verify(mockState, times(0)).getLowerTimeExtent();
        verify(mockState, times(0)).getUpperTimeExtent();
        verify(mockState, times(0)).isShowingSelectedOnly();
        verify(mockState, times(0)).getTimeZone();
    }

    @Test
    public void testGetTimelinePanel() {
        final TimelineTopComponent instance = new TimelineTopComponent();
        final TimelinePanel timelinePanel = mock(TimelinePanel.class);
        instance.setTimelinePanel(timelinePanel);
        assertEquals(instance.getTimelinePanel(), timelinePanel);
    }

    @Test
    public void testGetOverviewPanel() {
        final TimelineTopComponent instance = new TimelineTopComponent();
        final OverviewPanel overviewPanel = mock(OverviewPanel.class);
        instance.setOverviewPanel(overviewPanel);
        assertEquals(instance.getOverviewPanel(), overviewPanel);
    }

}
