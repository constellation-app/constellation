/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.time.ZoneId;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class TimelinePanelNGTest {

    private static final Logger LOGGER = Logger.getLogger(TimelinePanelNGTest.class.getName());

    private Graph graph;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }

        // Setup a graph with elements
        setupGraph();
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
     * Test of getCoordinator method, of class TimelinePanel.
     */
    @Test
    public void testGetCoordinator() {
        System.out.println("getCoordinator");
        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instance = new TimelinePanel(coordinator);

        assertEquals(coordinator, instance.getCoordinator());
    }

    /**
     * Test of setExclusionState method, of class TimelinePanel.
     */
    @Test
    public void testSetExclusionState() {
        System.out.println("setExclusionState");

        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instance = new TimelinePanel(coordinator);

        instance.setExclusionState(2);
        instance.setExclusionState(1);
        instance.setExclusionState(0);

        verify(coordinator, times(1)).setExclusionState(2);
        verify(coordinator, times(1)).setExclusionState(1);
        verify(coordinator, times(1)).setExclusionState(0);
    }

    /**
     * Test of updateTimeline wrapper method, of class TimelinePanel.
     */
    @Test
    public void testUpdateTimeline() {
        System.out.println("updateTimeline");
        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instanceSpy = spy(new TimelinePanel(coordinator));

        final GraphReadMethods mockGraph = mock(GraphReadMethods.class);
        final ZoneId mockZoneId = ZoneId.systemDefault();

        // Assert inital state of timeline panel
        assertEquals(coordinator, instanceSpy.getCoordinator());
        assertNull(instanceSpy.getUpdateTimelineThread());
        assertFalse(coordinator.getIsInProgress());

        // Run function
        instanceSpy.updateTimeline(mockGraph, false, mockZoneId);

        // Assert thread was created
        assertNotNull(instanceSpy.getUpdateTimelineThread());
        // Assert that update timeline update has finished
        assertFalse(coordinator.getIsInProgress());

        // Cleanup data, just in case
        instanceSpy.clearTimelineData();
    }

    /**
     * Test of updateTimelineWorker method, of class TimelinePanel.
     */
    @Test
    public void testUpdateTimelineWorker() {
        System.out.println("updateTimelineWorker");

        final TimelineTopComponent coordinator = mock(TimelineTopComponent.class);
        final TimelinePanel instanceSpy = spy(new TimelinePanel(coordinator));
        final ZoneId mockZoneId = ZoneId.systemDefault();

        instanceSpy.getClusteringManager().generateTree(graph.getReadableGraph(), "DateTime", false);
        instanceSpy.getClusteringManager().filterTree(Double.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE);

        // Assert inital state of timeline panel
        assertEquals(coordinator, instanceSpy.getCoordinator());
        assertNull(instanceSpy.getUpdateTimelineThread());
        assertFalse(coordinator.getIsInProgress());
        assertNull(instanceSpy.getTimeline().getData());

        // Run fucntion
        instanceSpy.updateTimelineWorker(graph.getReadableGraph(), false, mockZoneId);

        // Assert that update timeline update has finished
        assertFalse(coordinator.getIsInProgress());
        // Assert timeline's data isnt null
        assertNotNull(instanceSpy.getTimeline().getData());
        // Cleanup data, just in case
        instanceSpy.clearTimelineData();
    }

    /**
     * Set up a graph with two vertices and two transactions.
     */
    private void setupGraph() throws InterruptedException {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        final int vxId1;
        final int vxId2;
        final int txId1;
        final int txId2;
        final int dateTimeAttribute;

        // Setup attributes
        WritableGraph wg = graph.getWritableGraph("", true);
        try {

            wg.addAttribute(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "DateTime",
                    "Datetime when transaction was created", ZonedDateTimeAttributeDescription.DEFAULT_VALUE, null);

            final SchemaAttribute transactionSelected = VisualConcept.TransactionAttribute.SELECTED;
            wg.addAttribute(transactionSelected.getElementType(), transactionSelected.getAttributeType(), transactionSelected.getName(),
                    transactionSelected.getDescription(), transactionSelected.getDefault(), transactionSelected.getAttributeMergerId());

            final SchemaAttribute vertexSelected = VisualConcept.VertexAttribute.SELECTED;
            wg.addAttribute(vertexSelected.getElementType(), vertexSelected.getAttributeType(), vertexSelected.getName(),
                    vertexSelected.getDescription(), vertexSelected.getDefault(), vertexSelected.getAttributeMergerId());
        } finally {
            wg.commit();
        }

        // Get id for DateTime
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            dateTimeAttribute = rg.getAttribute(GraphElementType.TRANSACTION, "DateTime");
        }

        // Setup nodes and transactions
        wg = graph.getWritableGraph("", true);
        try {
            vxId1 = wg.addVertex();
            vxId2 = wg.addVertex();

            txId1 = wg.addTransaction(vxId1, vxId2, true);
            txId2 = wg.addTransaction(vxId1, vxId2, false);

            wg.setLongValue(dateTimeAttribute, txId1, 1L);
            wg.setLongValue(dateTimeAttribute, txId2, 1L);
        } finally {
            wg.commit();
        }
    }

}
