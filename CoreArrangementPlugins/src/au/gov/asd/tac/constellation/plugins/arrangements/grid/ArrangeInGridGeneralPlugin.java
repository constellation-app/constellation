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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin for grid arrangement
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages({
    "ArrangeInGridGeneralPlugin=Arrange in Grid",
    "SelectedOnly=Arrange only selected nodes"
})
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInGridGeneralPlugin extends SimpleEditPlugin {

    public static final String MAINTAIN_MEAN_PARAMETER_ID = PluginParameter.buildId(ArrangeInGridGeneralPlugin.class, "maintain_mean");
    public static final String GRID_CHOICE_PARAMETER_ID = PluginParameter.buildId(ArrangeInGridGeneralPlugin.class, "grid_choice");
    public static final String SIZE_GAIN_PARAMETER_ID = PluginParameter.buildId(ArrangeInGridGeneralPlugin.class, "size_gain");
    public static final String HORIZONTAL_GAP_PARAMETER_ID = PluginParameter.buildId(ArrangeInGridGeneralPlugin.class, "horizontal_gap");
    public static final String VERTICAL_GAP_PARAMETER_ID = PluginParameter.buildId(ArrangeInGridGeneralPlugin.class, "vertical_gap");
    public static final String OFFSET_ROWS_PARAMETER_ID = PluginParameter.buildId(ArrangeInGridGeneralPlugin.class, "offset_rows");

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
        radiusSetter.setRadii();

        final Map<String, PluginParameter<?>> pp = parameters.getParameters();
        final GridChoiceParameters gridParams = GridChoiceParameters.getDefaultParameters();
        gridParams.setGridChoice(GridChoice.getValue(pp.get(GRID_CHOICE_PARAMETER_ID).getStringValue()));
        gridParams.setSizeGain(pp.get(SIZE_GAIN_PARAMETER_ID).getFloatValue());
        gridParams.setHorizontalGap(pp.get(HORIZONTAL_GAP_PARAMETER_ID).getIntegerValue());
        gridParams.setVerticalGap(pp.get(VERTICAL_GAP_PARAMETER_ID).getIntegerValue());
        gridParams.setRowOffsets(pp.get(OFFSET_ROWS_PARAMETER_ID).getBooleanValue());

        final GridArranger arranger = new GridArranger(gridParams);
        final Boolean maintainMean = pp.get(MAINTAIN_MEAN_PARAMETER_ID).getBooleanValue();
        arranger.setMaintainMean(maintainMean);

        final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, SelectedInclusionGraph.Connections.NONE);
        arranger.arrange(selectedGraph.getInclusionGraph());
        selectedGraph.retrieveCoords();
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        // Use the GridChoiceParameters defaults to set the plugin defaults.
        final GridChoiceParameters defaults = GridChoiceParameters.getDefaultParameters();

        final PluginParameter<SingleChoiceParameterValue> gridChoiceParam = SingleChoiceParameterType.build(GRID_CHOICE_PARAMETER_ID);
        gridChoiceParam.setName("Grid choice");
        gridChoiceParam.setDescription("The type of grid arrangement");
        SingleChoiceParameterType.setOptions(gridChoiceParam, GridChoice.getChoices());
        SingleChoiceParameterType.setChoice(gridChoiceParam, defaults.getGridChoice().toString());
        parameters.addParameter(gridChoiceParam);

        final PluginParameter<FloatParameterValue> sizeGainParam = FloatParameterType.build(SIZE_GAIN_PARAMETER_ID);
        sizeGainParam.setName("Size gain");
        sizeGainParam.setDescription("The size gain, the default is " + defaults.getSizeGain());
        sizeGainParam.setFloatValue(defaults.getSizeGain());
        parameters.addParameter(sizeGainParam);

        final PluginParameter<IntegerParameterValue> horizontalGapParam = IntegerParameterType.build(HORIZONTAL_GAP_PARAMETER_ID);
        horizontalGapParam.setName("Horizontal gap");
        horizontalGapParam.setDescription("The horizontal gap, the default is " + defaults.getHorizontalGap());
        horizontalGapParam.setIntegerValue(defaults.getHorizontalGap());
        parameters.addParameter(horizontalGapParam);

        final PluginParameter<IntegerParameterValue> verticalGapParam = IntegerParameterType.build(VERTICAL_GAP_PARAMETER_ID);
        verticalGapParam.setName("Vertical gap");
        verticalGapParam.setDescription("The verticle gap, the default is " + defaults.getVerticalGap());
        verticalGapParam.setIntegerValue(defaults.getVerticalGap());
        parameters.addParameter(verticalGapParam);

        final PluginParameter<BooleanParameterValue> offsetRowsParam = BooleanParameterType.build(OFFSET_ROWS_PARAMETER_ID);
        offsetRowsParam.setName("Offset even numbered rows");
        offsetRowsParam.setDescription("Offset even numbered rows, the default is " + defaults.hasRowOffsets());
        offsetRowsParam.setBooleanValue(defaults.hasRowOffsets());
        parameters.addParameter(offsetRowsParam);

        final PluginParameter<BooleanParameterValue> maintainMeanParam = BooleanParameterType.build(MAINTAIN_MEAN_PARAMETER_ID);
        maintainMeanParam.setName("Maintain Mean");
        maintainMeanParam.setDescription("Maintain Mean, default is True");
        maintainMeanParam.setBooleanValue(true);
        parameters.addParameter(maintainMeanParam);

        return parameters;
    }
}
