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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

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
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 * @author Nova
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("UncollidePlugin=Uncollide")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class UncollidePlugin extends SimpleEditPlugin {

    public static final String DIMENSION_PARAMETER_ID = PluginParameter.buildId(UncollidePlugin.class, "dimension");
    public static final String MAX_EXPANSIONS_PARAMETR_ID = PluginParameter.buildId(UncollidePlugin.class, "Max Expansions");

    @Override
    public String getDescription() {
        return "Uncollide all nodes whilst attempting to maintaing graph structure.";
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final Map<String, PluginParameter<?>> params = parameters.getParameters();
        final Dimensions dimensions = Dimensions.valueOf(params.get(DIMENSION_PARAMETER_ID).getStringValue());
        final int maxExpansions = params.get(MAX_EXPANSIONS_PARAMETR_ID).getIntegerValue();

        final Arranger arranger = new UncollideArrangement(dimensions, maxExpansions);
        ((UncollideArrangement) arranger).setInteraction(interaction);

        final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(wg, SelectedInclusionGraph.Connections.NONE);
        arranger.setMaintainMean(!selectedGraph.isArrangingAll());
        arranger.arrange(selectedGraph.getInclusionGraph());
        selectedGraph.retrieveCoords();
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> dimensionsParam = SingleChoiceParameterType.build(DIMENSION_PARAMETER_ID);
        dimensionsParam.setName("Dimensions");
        dimensionsParam.setDescription("The dimention being 2D or 3D. The default is 2 for 2D.");
        SingleChoiceParameterType.setOptions(dimensionsParam, Dimensions.getOptions());
        SingleChoiceParameterType.setChoice(dimensionsParam, Dimensions.TWO.toString());
        parameters.addParameter(dimensionsParam);

        final PluginParameter<IntegerParameterValue> maxExpansionsParam = IntegerParameterType.build(MAX_EXPANSIONS_PARAMETR_ID);
        maxExpansionsParam.setName("Maximum Expansions");
        maxExpansionsParam.setDescription("The maximum number of expansions to allow. A higher number will better retain graph structure butmay lead to a very space out graph, whilst a low number will result in a more compacted graph at a potential cost to graph sturcture.");
        maxExpansionsParam.setIntegerValue(20);
        parameters.addParameter(maxExpansionsParam);
        return parameters;
    }
}
