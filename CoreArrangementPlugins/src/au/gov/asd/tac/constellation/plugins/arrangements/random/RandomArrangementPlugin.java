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
package au.gov.asd.tac.constellation.plugins.arrangements.random;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Plugin framework for random arrangement.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("RandomArrangementPlugin=Arrange Randomly")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class RandomArrangementPlugin extends SimpleEditPlugin {

    public static final String DIMENSIONS_PARAMETER_ID = PluginParameter.buildId(RandomArrangementPlugin.class, "dimensions");

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int dimensions = parameters.getParameters().get(DIMENSIONS_PARAMETER_ID).getIntegerValue();
        final Arranger arranger = new RandomArranger(dimensions);
        final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(wg, SelectedInclusionGraph.Connections.NONE);
        arranger.setMaintainMean(!selectedGraph.isArrangingAll());
        arranger.arrange(selectedGraph.getInclusionGraph());
        selectedGraph.retrieveCoords();
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> dimensionsParam = IntegerParameterType.build(DIMENSIONS_PARAMETER_ID);
        dimensionsParam.setName(DIMENSIONS_PARAMETER_ID);
        dimensionsParam.setDescription("The dimension being 2D or 3D. The default is 2 for 2D.");
        dimensionsParam.setIntegerValue(2);
        parameters.addParameter(dimensionsParam);

        return parameters;
    }
}
