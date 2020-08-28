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
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import java.util.List;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaut69
 */
public class DeselectAllLayersPluginNGTest {

    private int layerMaskV, layerMaskT, layerVisibilityV, layerVisibilityT;
    private StoreGraph graph;

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
    }

    @Test
    public void testDeselectAllLayersPlugin() throws InterruptedException, PluginException {
        setupGraph();

        PluginExecution.withPlugin(new NewLayerPlugin()).executeNow(graph);

        final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);

        assertTrue(layersViewStateAttributeId != Graph.NOT_FOUND);
        final LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
        final List<LayerDescription> layers = currentState.getLayers();

        layers.get(layers.size() - 1).setCurrentLayerVisibility(true);

        PluginExecution.withPlugin(new DeselectAllLayersPlugin()).executeNow(graph);

        layers.forEach((layer) -> {
            assertTrue(layer.getCurrentLayerVisibility() == false);
        });
    }
}
