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
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.shortcut.NewLayerPlugin;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaut69
 */
public class NewLayerPluginNGTest {
    
    private StoreGraph graph;

    private void setupGraph() {
        graph = new StoreGraph();

        // Create LayerMask attributes
        LayersConcept.VertexAttribute.LAYER_MASK.ensure(graph);
        LayersConcept.TransactionAttribute.LAYER_MASK.ensure(graph);

        // Create LayerVisilibity Attributes
        LayersConcept.VertexAttribute.LAYER_VISIBILITY.ensure(graph);
        LayersConcept.TransactionAttribute.LAYER_VISIBILITY.ensure(graph);
    }

    @Test
    public void testNewLayerPlugin() throws InterruptedException, PluginException {
        setupGraph();

        PluginExecution.withPlugin(new NewLayerPlugin()).executeNow(graph);

        final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.get(graph);
        assertTrue(layersViewStateAttributeId != Graph.NOT_FOUND);

        final LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
        assertTrue(currentState.getLayerCount() == 2);
    }
}
