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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Layers Dual Graph Sync Test
 *
 * @author aldebaran30701
 */
public class LayersDualGraphSyncNGTest {

    private int layerMaskV, layerMaskT, layerVisibilityV, layerVisibilityT, selectedV, selectedT, colorV, colorT;
    private int vxId1, vxId2, vxId3;
    private ConstellationColor vx1Color, vx2Color, vx3Color;
    int bitmaskAttributeId;
    private DualGraph graph;
    private List<String> queries;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        queries = new ArrayList();
        graph = new DualGraph(null);

        final WritableGraph writableGraph = graph.getWritableGraph("test", true);
        try {
            bitmaskAttributeId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(writableGraph);

            // Create LayerMask attributes
            layerMaskV = LayersConcept.VertexAttribute.LAYER_MASK.ensure(writableGraph);
            if (layerMaskV == Graph.NOT_FOUND) {
                fail();
            }
            layerMaskT = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(writableGraph);
            if (layerMaskT == Graph.NOT_FOUND) {
                fail();
            }

            // Create LayerVisilibity Attributes
            layerVisibilityV = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(writableGraph);
            if (layerVisibilityV == Graph.NOT_FOUND) {
                fail();
            }
            layerVisibilityT = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(writableGraph);
            if (layerVisibilityT == Graph.NOT_FOUND) {
                fail();
            }

            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(writableGraph);
            if (selectedV == Graph.NOT_FOUND) {
                fail();
            }
            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(writableGraph);
            if (selectedT == Graph.NOT_FOUND) {
                fail();
            }

            // Create Color Attributes
            colorV = VisualConcept.VertexAttribute.COLOR.ensure(writableGraph);
            if (colorV == Graph.NOT_FOUND) {
                fail();
            }

            colorT = VisualConcept.TransactionAttribute.COLOR.ensure(writableGraph);
            if (colorT == Graph.NOT_FOUND) {
                fail();
            }

            vx1Color = ConstellationColor.getColorValue("#ed76b1");
            vx2Color = ConstellationColor.getColorValue("#eb78b2");
            vx3Color = ConstellationColor.getColorValue("#ee71b3");

            // Adding 2 Vertices layer 1, visible
            vxId1 = writableGraph.addVertex();
            vxId2 = writableGraph.addVertex();
            vxId3 = writableGraph.addVertex();
            writableGraph.setObjectValue(colorV, vxId1, vx1Color);
            writableGraph.setObjectValue(colorV, vxId2, vx2Color);
            writableGraph.setObjectValue(colorV, vxId3, vx3Color);
        } finally {
            writableGraph.commit();
        }
        final WritableGraph writableGraph2 = graph.getWritableGraph("test2", true);

        try {
            writableGraph2.setLongValue(layerMaskV, vxId1, 1);
            writableGraph2.setFloatValue(layerVisibilityV, vxId1, 1.0f);
            writableGraph2.setBooleanValue(selectedV, vxId1, false);

            vxId2 = writableGraph2.addVertex();
            writableGraph2.setLongValue(layerMaskV, vxId2, 1);
            writableGraph2.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            writableGraph2.setBooleanValue(selectedV, vxId2, false);
        } finally {
            writableGraph2.commit();
        }

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void dynamicLayerChangeTest() throws InterruptedException, PluginException {
        // Check Vertex set correctly
        ReadableGraph readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getIntValue(layerMaskV, vxId1), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        } finally {
            readableGraph.release();
        }

        WritableGraph writableGraph = graph.getWritableGraph("", true);

        try {
            writableGraph.setLongValue(bitmaskAttributeId, 0, 0b10);
            writableGraph.setLongValue(layerMaskV, vxId1, 3);
            writableGraph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId2, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId2, 0.0f);
            writableGraph.setLongValue(layerMaskV, vxId3, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId3, 0.0f);
        } finally {
            writableGraph.commit();
        }

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getLongValue(bitmaskAttributeId, 0), 0b10);
            assertEquals((long) readableGraph.getObjectValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals((long) readableGraph.getObjectValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId2), 0.0f);

            assertEquals((long) readableGraph.getObjectValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }

        // change an attribute to trigger switching of graph object within dual graph
        PluginExecution.withPlugin(new SimpleEditPlugin("Test: change attribute value") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                final int maxTransactionsId = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.ensure(graph);
                graph.setLongValue(maxTransactionsId, 0, 9);
            }
        }).executeNow(graph);

        // Check Vertex set correctly on second graph of dualgraph
        readableGraph = graph.getReadableGraph();
        try {
            assertEquals((long) readableGraph.getObjectValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals((long) readableGraph.getObjectValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId2), 0.0f);

            assertEquals((long) readableGraph.getObjectValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }

        writableGraph = graph.getWritableGraph("", true);

        try {
            writableGraph.setLongValue(layerMaskV, vxId1, 3);
            writableGraph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId2, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId3, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId3, 1.0f);
        } finally {
            writableGraph.commit();
        }

        //PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(queries, 1)).executeNow(graph);
        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getLongValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        } finally {
            readableGraph.release();
        }

        writableGraph = graph.getWritableGraph("", true);

        try {
            writableGraph.setLongValue(layerMaskV, vxId1, 3);
            writableGraph.setFloatValue(layerVisibilityV, vxId1, 0.0f);
            writableGraph.setLongValue(layerMaskV, vxId2, 0b101);
            writableGraph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId3, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId3, 0.0f);
        } finally {
            writableGraph.commit();
        }

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getLongValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId2), 0b101);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }

        writableGraph = graph.getWritableGraph("", true);
        try {
            writableGraph.setLongValue(layerMaskV, vxId1, 3);
            writableGraph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId2, 0b101);
            writableGraph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId3, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId3, 1.0f);
        } finally {
            writableGraph.commit();
        }

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getLongValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId2), 0b101);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        } finally {
            readableGraph.release();
        }

        writableGraph = graph.getWritableGraph("", true);
        try {
            writableGraph.setLongValue(layerMaskV, vxId1, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId1, 0.0f);
            writableGraph.setLongValue(layerMaskV, vxId2, 0b111);
            writableGraph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
            writableGraph.setLongValue(layerMaskV, vxId3, 1);
            writableGraph.setFloatValue(layerVisibilityV, vxId3, 0.0f);
        } finally {
            writableGraph.commit();
        }

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getLongValue(layerMaskV, vxId1), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId2), 0b111);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }

        PluginExecution.withPlugin(new SimpleEditPlugin("Test: change attribute value") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                final int maxTransactionsId = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.ensure(graph);
                graph.setIntValue(maxTransactionsId, 0, 9);
            }
        }).executeNow(graph);

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getLongValue(layerMaskV, vxId1), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId2), 0b111);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getLongValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }
    }
}
