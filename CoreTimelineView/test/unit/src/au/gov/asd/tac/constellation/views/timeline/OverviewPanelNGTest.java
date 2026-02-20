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
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class OverviewPanelNGTest {

    private static final Logger LOGGER = Logger.getLogger(OverviewPanelNGTest.class.getName());

    private Graph graph;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
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
     * Test of clearHistogram method, of class OverviewPanel.
     */
    @Test
    public void testClearHistogram() {
        System.out.println("clearHistogram");

        // Mocked variables
        final TimelineTopComponent mockTopComponent = mock(TimelineTopComponent.class);

        final OverviewPanel instance = new OverviewPanel(mockTopComponent);

        // Assert not null before clearing
        assertNotNull(instance.getHistogramData());
        instance.clearHistogram();

        // Should be null now
        assertNull(instance.getHistogramData());
    }

    /**
     * Test of clearHistogram method, of class OverviewPanel.
     */
    @Test
    public void testClearHistogramIsPartialClearTrue() {
        System.out.println("clearHistogram with args true");
        final boolean isPartialClear = true;

        // Mocked variables
        final TimelineTopComponent mockTopComponent = mock(TimelineTopComponent.class);
        
        final OverviewPanel instance = new OverviewPanel(mockTopComponent);

        // Assert not null before clearing
        assertNotNull(instance.getHistogramData());

        // Populate the histogram multiple times
        instance.populateHistogram(graph.getReadableGraph(), "DateTime", 0, 0, false, false);
        instance.populateHistogram(graph.getReadableGraph(), "DateTime", 0, 0, false, false);
        instance.populateHistogram(graph.getReadableGraph(), "DateTime", 0, 0, false, false);

        // Run function
        instance.clearHistogram(isPartialClear);

        // Should only have one element in histogram data
        assertEquals(instance.getHistogramData().size(), 1);
    }

    /**
     * Test of clearHistogram method, of class OverviewPanel.
     */
    @Test
    public void testClearHistogramIsPartialClearFalse() {
        System.out.println("clearHistogram with args false");
        final boolean isPartialClear = false;

        // Mocked variables
        final TimelineTopComponent mockTopComponent = mock(TimelineTopComponent.class);

        final OverviewPanel instance = new OverviewPanel(mockTopComponent);

        // Assert not null before clearing
        assertNotNull(instance.getHistogramData());
        instance.clearHistogram(isPartialClear);

        // Should be null now
        assertNull(instance.getHistogramData());
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
