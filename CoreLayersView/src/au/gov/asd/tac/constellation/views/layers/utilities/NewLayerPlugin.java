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
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.layer.LayerDescription;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewState;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author formalhaut69
 */
public class NewLayerPlugin extends SimpleEditPlugin {

    public NewLayerPlugin() {

    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {
        if (graph == null) {
            return;
        }

        final int layersViewStateAttributeId = LayersViewConcept.MetaAttribute.LAYERS_VIEW_STATE.ensure(graph);
        if (layersViewStateAttributeId == Graph.NOT_FOUND) {
            return;
        }

        LayersViewState currentState = graph.getObjectValue(layersViewStateAttributeId, 0);
        if (currentState == null) {
            final List<LayerDescription> layers = new ArrayList<>();
            layers.add(new LayerDescription(1, true, LayerDescription.DEFAULT_QUERY_STRING, LayerDescription.DEFAULT_QUERY_DESCRIPTION));
            layers.add(new LayerDescription(2, false, StringUtils.EMPTY, StringUtils.EMPTY));
            currentState = new LayersViewState(layers);
        }

        final LayersViewState newState = new LayersViewState(currentState);
        newState.addLayer();

        graph.setObjectValue(layersViewStateAttributeId, 0, newState);
    }

    @Override
    protected boolean isSignificant() {
        return false;
    }

    @Override
    public String getName() {
        return "Layers View: New Layer Plugin";
    }
}
