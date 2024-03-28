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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.layers.state.LayersViewConcept;

/**
 * Write a given set of queries to the active graph and set the current bit mask
 * to the selected layer.
 *
 * @author aldebaran30701
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public final class UpdateLayerSelectionPlugin extends SimpleEditPlugin {

    private final long bitmask;

    public UpdateLayerSelectionPlugin(final long bitmask) {
        this.bitmask = bitmask;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {
        final int bitmaskAttributeId = LayersViewConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        graph.setLongValue(bitmaskAttributeId, 0, bitmask);
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
        return "Layers View: Update Layer Selection";
    }
}
