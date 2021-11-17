/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.scatterplot.state;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;

/**
 * Write the given ScatterPlotState to the active graph.
 *
 * @author antares
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public class ScatterPlotStateWriter extends SimpleEditPlugin {

    private final ScatterPlotState scatterPlotState;

    public ScatterPlotStateWriter(final ScatterPlotState scatterPlotState) {
        this.scatterPlotState = scatterPlotState;
    }

    @Override
    public String getName() {
        return "Scatter Plot: Update State";
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int scatterPlotStateAttribute = ScatterPlotConcept.MetaAttribute.SCATTER_PLOT_STATE.ensure(graph);
        final ScatterPlotState state = new ScatterPlotState(scatterPlotState);
        graph.setObjectValue(scatterPlotStateAttribute, 0, state);
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }
}
