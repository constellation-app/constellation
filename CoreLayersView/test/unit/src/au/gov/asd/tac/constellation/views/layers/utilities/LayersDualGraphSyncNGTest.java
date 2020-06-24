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
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class LayersDualGraphSyncNGTest {

    private int layerMaskV, layerMaskT, layerVisibilityV, layerVisibilityT, selectedV, selectedT, colorV, colorT;
    private int vxId1, vxId2, vxId3;
    private ConstellationColor vx1Color, vx2Color, vx3Color;
    private StoreGraph graph;

    private final List<String> queries = new ArrayList();

    public LayersDualGraphSyncNGTest() {
    }

    public void setupGraph() {
        graph = new StoreGraph();

        // Create LayerMask attributes
        layerMaskV = LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        if (layerMaskV == Graph.NOT_FOUND) {
            fail();
        }
        layerMaskT = LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);
        if (layerMaskT == Graph.NOT_FOUND) {
            fail();
        }

        // Create LayerVisilibity Attributes
        layerVisibilityV = LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
        if (layerVisibilityV == Graph.NOT_FOUND) {
            fail();
        }
        layerVisibilityT = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
        if (layerVisibilityT == Graph.NOT_FOUND) {
            fail();
        }

        // Create Selected Attributes
        selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        if (selectedV == Graph.NOT_FOUND) {
            fail();
        }
        selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        if (selectedT == Graph.NOT_FOUND) {
            fail();
        }

        // Create Color Attributes
        colorV = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        if (colorV == Graph.NOT_FOUND) {
            fail();
        }

        colorT = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        if (colorT == Graph.NOT_FOUND) {
            fail();
        }

        // Adding 2 Vertices layer 1, visible
        vxId1 = graph.addVertex();
        vxId2 = graph.addVertex();
        vxId3 = graph.addVertex();

        vx1Color = ConstellationColor.getColorValue("#ed76b1");
        vx2Color = ConstellationColor.getColorValue("#eb78b2");
        vx3Color = ConstellationColor.getColorValue("#ee71b3");

        graph.setObjectValue(colorV, vxId1, vx1Color);
        graph.setObjectValue(colorV, vxId2, vx2Color);
        graph.setObjectValue(colorV, vxId3, vx3Color);
//        graph.setIntValue(layerMaskV, vxId1, 1);
//        graph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
//        graph.setBooleanValue(selectedV, vxId1, false);
//
//        vxId2 = graph.addVertex();
//        graph.setIntValue(layerMaskV, vxId2, 1);
//        graph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
//        graph.setBooleanValue(selectedV, vxId2, false);
    }

    @Test
    public void dynamicLayerChangeTest() throws InterruptedException, PluginException {
        setupGraph();

        // Check Vertex set correctly
        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId3));
        queries.clear();
        queries.add("Default");
        queries.add("color == " + vx1Color.toString());

        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(queries)).executeNow(graph);
        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(0b10)).executeNow(graph);

        // Check Vertex set correctly
        assertTrue(3 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(vx1Color.toString().equals(graph.getObjectValue(colorV, vxId1).toString()));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(vx2Color.toString().equals(graph.getObjectValue(colorV, vxId2).toString()));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId3));
        assertTrue(vx3Color.toString().equals(graph.getObjectValue(colorV, vxId3).toString()));

        // change an attribute to trigger switching of graph object within dual graph
        PluginExecution.withPlugin(new SimpleEditPlugin("Test: change attribute value") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                final int maxTransactionsId = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.ensure(graph);
                graph.setIntValue(maxTransactionsId, 0, 9);
            }
        }).executeNow(GraphManager.getDefault().getActiveGraph());

        // Check Vertex set correctly on second graph of dualgraph
        assertTrue(3 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(vx1Color.toString().equals(graph.getObjectValue(colorV, vxId1).toString()));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(vx2Color.toString().equals(graph.getObjectValue(colorV, vxId2).toString()));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId3));
        assertTrue(vx3Color.toString().equals(graph.getObjectValue(colorV, vxId3).toString()));

        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(queries)).executeNow(graph);
        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(1)).executeNow(graph);

        assertTrue(3 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId3));

        queries.clear();
        queries.add("Default");
        queries.add("color == " + vx1Color.toString());
        queries.add("color == " + vx2Color.toString());

        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(queries)).executeNow(graph);
        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(0b100)).executeNow(graph);

        assertTrue(3 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId1));

        assertTrue(0b101 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId3));

        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(queries)).executeNow(graph);
        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(1)).executeNow(graph);

        assertTrue(3 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));

        assertTrue(0b101 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId3));

        queries.clear();
        queries.add("Default");
        queries.add("color == " + vx2Color.toString());
        queries.add("color == " + vx2Color.toString());

        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(queries)).executeNow(graph);
        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(0b10)).executeNow(graph);

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId1));

        assertTrue(0b111 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId3));

        PluginExecution.withPlugin(new SimpleEditPlugin("Test: change attribute value") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                final int maxTransactionsId = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.ensure(graph);
                graph.setIntValue(maxTransactionsId, 0, 9);
            }
        }).executeNow(GraphManager.getDefault().getActiveGraph());

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId1));

        assertTrue(0b111 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId3));
        assertTrue(0.0f == graph.getFloatValue(layerVisibilityV, vxId3));
    }

    @Test
    public void addRemoveTwoElementsSelectedTest() throws InterruptedException, PluginException {
//        setupGraph();
//        // Check Vertex set correctly
//        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
//        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));
//
//        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
//        assertTrue(false == graph.getBooleanValue(selectedV, vxId2));
//
//        // Check Transaction set correctly
//        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
//        assertTrue(false == graph.getBooleanValue(selectedT, txId1));
//
//        assertTrue(1 == graph.getIntValue(layerMaskT, txId2));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
//        assertTrue(false == graph.getBooleanValue(selectedT, txId2));
//
//        // Setting Vertex and Transacion 2 to selected.
//        graph.setBooleanValue(selectedT, txId2, true);
//        graph.setBooleanValue(selectedV, vxId2, true);
//
//        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(0b10, LayerAction.ADD)).executeNow(graph);
//
//        // Check Vertex values
//        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
//        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));
//
//        assertTrue(0b11 == graph.getIntValue(layerMaskV, vxId2));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
//        assertTrue(true == graph.getBooleanValue(selectedV, vxId2));
//
//        // Check Transaction values
//        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
//        assertTrue(false == graph.getBooleanValue(selectedT, txId1));
//
//        assertTrue(0b11 == graph.getIntValue(layerMaskT, txId2));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
//        assertTrue(true == graph.getBooleanValue(selectedT, txId2));
//
//        // Remove from layers
//        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(0b10, LayerAction.REMOVE)).executeNow(graph);
//
//        // check vertices set correctly
//        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
//        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));
//
//        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
//        assertTrue(true == graph.getBooleanValue(selectedV, vxId2));
//
//        // Check Transaction set correctly
//        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
//        assertTrue(false == graph.getBooleanValue(selectedT, txId1));
//
//        assertTrue(1 == graph.getIntValue(layerMaskT, txId2));
//        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
//        assertTrue(true == graph.getBooleanValue(selectedT, txId2));
    }
}
