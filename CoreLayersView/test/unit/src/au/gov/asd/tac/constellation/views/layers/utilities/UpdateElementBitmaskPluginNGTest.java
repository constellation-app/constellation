/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.context.LayerAction;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class UpdateElementBitmaskPluginNGTest {

    private int layerMaskV, layerMaskT, layerVisibilityV, layerVisibilityT, selectedV, selectedT;
    private int vxId1, vxId2, txId1, txId2;
    private StoreGraph graph;

    public UpdateElementBitmaskPluginNGTest() {
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

        // Adding 2 Vertices - not selected, layer 1, visible
        vxId1 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId1, 1);
        graph.setFloatValue(layerVisibilityV, vxId1, 1.0f);
        graph.setBooleanValue(selectedV, vxId1, false);

        vxId2 = graph.addVertex();
        graph.setIntValue(layerMaskV, vxId2, 1);
        graph.setFloatValue(layerVisibilityV, vxId2, 1.0f);
        graph.setBooleanValue(selectedV, vxId2, false);

        // Adding 2 Transactions - not selected, layer 1, visible
        txId1 = graph.addTransaction(vxId1, vxId2, true);
        graph.setIntValue(layerMaskT, txId1, 1);
        graph.setFloatValue(layerVisibilityT, txId1, 1.0f);
        graph.setBooleanValue(selectedT, txId1, false);

        txId2 = graph.addTransaction(vxId1, vxId2, false);
        graph.setIntValue(layerMaskT, txId2, 1);
        graph.setFloatValue(layerVisibilityT, vxId2, 1.0f);
        graph.setBooleanValue(selectedT, vxId2, false);
    }

    @Test
    public void addNoElementsSelectedTest() throws InterruptedException, PluginException {
        setupGraph();
        // Check Vertex set correctly
        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
        assertTrue(false == graph.getBooleanValue(selectedT, txId1));

        assertTrue(1 == graph.getIntValue(layerMaskT, txId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
        assertTrue(false == graph.getBooleanValue(selectedT, txId2));

        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(2, LayerAction.ADD, true)).executeNow(graph);

        // Check Vertex unchanged
        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction unchanged
        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
        assertTrue(false == graph.getBooleanValue(selectedT, txId1));

        assertTrue(1 == graph.getIntValue(layerMaskT, txId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
        assertTrue(false == graph.getBooleanValue(selectedT, txId2));
    }

    @Test
    public void addRemoveTwoElementsSelectedTest() throws InterruptedException, PluginException {
        setupGraph();
        // Check Vertex set correctly
        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
        assertTrue(false == graph.getBooleanValue(selectedT, txId1));

        assertTrue(1 == graph.getIntValue(layerMaskT, txId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
        assertTrue(false == graph.getBooleanValue(selectedT, txId2));

        // Setting Vertex and Transacion 2 to selected.
        graph.setBooleanValue(selectedT, txId2, true);
        graph.setBooleanValue(selectedV, vxId2, true);

        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(1, LayerAction.ADD, true)).executeNow(graph);

        // Check Vertex values
        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));

        assertTrue(0b11 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(true == graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction values
        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
        assertTrue(false == graph.getBooleanValue(selectedT, txId1));

        assertTrue(0b11 == graph.getIntValue(layerMaskT, txId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
        assertTrue(true == graph.getBooleanValue(selectedT, txId2));

        // Remove from layers
        PluginExecution.withPlugin(new UpdateElementBitmaskPlugin(1, LayerAction.REMOVE, true)).executeNow(graph);

        // check vertices set correctly
        assertTrue(1 == graph.getIntValue(layerMaskV, vxId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId1));
        assertTrue(false == graph.getBooleanValue(selectedV, vxId1));

        assertTrue(1 == graph.getIntValue(layerMaskV, vxId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityV, vxId2));
        assertTrue(true == graph.getBooleanValue(selectedV, vxId2));

        // Check Transaction set correctly
        assertTrue(1 == graph.getIntValue(layerMaskT, txId1));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId1));
        assertTrue(false == graph.getBooleanValue(selectedT, txId1));

        assertTrue(1 == graph.getIntValue(layerMaskT, txId2));
        assertTrue(1.0f == graph.getFloatValue(layerVisibilityT, txId2));
        assertTrue(true == graph.getBooleanValue(selectedT, txId2));
    }
}
