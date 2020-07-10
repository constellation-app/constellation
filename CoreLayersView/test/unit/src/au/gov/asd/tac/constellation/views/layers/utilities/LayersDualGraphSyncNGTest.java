/*
 * Copyright 2010-2020 Australian Signals Directorate
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

//        graph.setIntValue(layerMaskV, vxId1, 1);
//        graph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
//        graph.setBooleanValue(selectedV, vxId1, false);
//
//        vxId2 = graph.addVertex();
//        graph.setIntValue(layerMaskV, vxId2, 1);
//        graph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
//        graph.setBooleanValue(selectedV, vxId2, false);
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

        queries.clear();
        queries.add("Default");
        queries.add("color == " + vx1Color.toString());

        final WritableGraph writableGraph = graph.getWritableGraph("test", true);
        try {
            LayersConcept.GraphAttribute.LAYER_QUERIES.ensure(writableGraph);
            LayersConcept.GraphAttribute.LAYER_PREFERENCES.ensure(writableGraph);
        } finally {
            writableGraph.commit();
        }

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(queries, 0b10)).executeNow(graph);

        // Check Vertex set correctly
        readableGraph = graph.getReadableGraph();
        try {
            assertEquals((int) readableGraph.getObjectValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId1), 1.0f);
            assertEquals(vx1Color.toString(), (readableGraph.getObjectValue(colorV, vxId1).toString()));

            assertEquals((int) readableGraph.getObjectValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId2), 0.0f);
            assertEquals(vx2Color.toString(), (readableGraph.getObjectValue(colorV, vxId2).toString()));

            assertEquals((int) readableGraph.getObjectValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId3), 0.0f);
            assertEquals(vx3Color.toString(), (readableGraph.getObjectValue(colorV, vxId3).toString()));
        } finally {
            readableGraph.release();
        }

        // change an attribute to trigger switching of graph object within dual graph
        PluginExecution.withPlugin(new SimpleEditPlugin("Test: change attribute value") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                final int maxTransactionsId = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.ensure(graph);
                graph.setIntValue(maxTransactionsId, 0, 9);
            }
        }).executeNow(graph);

        // Check Vertex set correctly on second graph of dualgraph
        readableGraph = graph.getReadableGraph();
        try {
            assertEquals((int) readableGraph.getObjectValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId1), 1.0f);
            assertEquals(vx1Color.toString(), (readableGraph.getObjectValue(colorV, vxId1).toString()));

            assertEquals((int) readableGraph.getObjectValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId2), 0.0f);
            assertEquals(vx2Color.toString(), (readableGraph.getObjectValue(colorV, vxId2).toString()));

            assertEquals((int) readableGraph.getObjectValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getObjectValue(layerVisibilityV, vxId3), 0.0f);
            assertEquals(vx3Color.toString(), (readableGraph.getObjectValue(colorV, vxId3).toString()));
        } finally {
            readableGraph.release();
        }

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(queries, 1)).executeNow(graph);

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getIntValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId2), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        } finally {
            readableGraph.release();
        }

        queries.clear();
        queries.add("Default");
        queries.add("color == " + vx1Color.toString());
        queries.add("color == " + vx2Color.toString());

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(queries, 0b100)).executeNow(graph);

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getIntValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId2), 0b101);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(queries, 1)).executeNow(graph);

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getIntValue(layerMaskV, vxId1), 3);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId2), 0b101);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 1.0f);
        } finally {
            readableGraph.release();
        }

        queries.clear();
        queries.add("Default");
        queries.add("color == " + vx2Color.toString());
        queries.add("color == " + vx2Color.toString());

        PluginExecution.withPlugin(new UpdateLayerSelectionPlugin(queries, 0b10)).executeNow(graph);

        readableGraph = graph.getReadableGraph();
        try {
            assertEquals(readableGraph.getIntValue(layerMaskV, vxId1), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId2), 0b111);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId3), 1);
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
            assertEquals(readableGraph.getIntValue(layerMaskV, vxId1), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId1), 0.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId2), 0b111);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId2), 1.0f);

            assertEquals(readableGraph.getIntValue(layerMaskV, vxId3), 1);
            assertEquals(readableGraph.getFloatValue(layerVisibilityV, vxId3), 0.0f);
        } finally {
            readableGraph.release();
        }
    }
}
