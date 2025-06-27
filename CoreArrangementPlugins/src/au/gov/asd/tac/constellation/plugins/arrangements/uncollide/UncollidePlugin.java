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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("UncollidePlugin=Uncollide")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class UncollidePlugin extends SimpleEditPlugin {

    public static final String DIMENSION_PARAMETER_ID = PluginParameter.buildId(UncollidePlugin.class, "dimension");
    public static final String SET_XYZ2_PARAMETER_ID = PluginParameter.buildId(UncollidePlugin.class, "set_xyz2");

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int dimensions = parameters.getIntegerValue(DIMENSION_PARAMETER_ID);
        final boolean set2 = parameters.getBooleanValue(SET_XYZ2_PARAMETER_ID);

        final Arranger arranger = new UncollideArrangement(dimensions, set2);
        ((UncollideArrangement) arranger).setInteraction(interaction);

        final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(wg, SelectedInclusionGraph.Connections.NONE);
        arranger.setMaintainMean(!selectedGraph.isArrangingAll());
        arranger.arrange(selectedGraph.getInclusionGraph());
        selectedGraph.retrieveCoords();
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> dimensionsParam = IntegerParameterType.build(DIMENSION_PARAMETER_ID);
        dimensionsParam.setName("Dimensions");
        dimensionsParam.setDescription("The dimention being 2D or 3D. The default is 2 for 2D.");
        dimensionsParam.setIntegerValue(2);
        parameters.addParameter(dimensionsParam);

        final PluginParameter<BooleanParameterValue> set2Param = BooleanParameterType.build(SET_XYZ2_PARAMETER_ID);
        set2Param.setName("Set XYZ 2");
        set2Param.setDescription("If True, set X2, Y2, Z2. The default is False.");
        set2Param.setBooleanValue(false);
        parameters.addParameter(set2Param);

        return parameters;
    }
}
