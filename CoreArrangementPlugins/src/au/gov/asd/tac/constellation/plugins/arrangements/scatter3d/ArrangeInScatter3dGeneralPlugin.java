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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter3d;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin for scatter3d arrangement
 *
 * @author CrucisGamma
 */
@ServiceProvider(service = Plugin.class)
@Messages({
    "ArrangeInScatter3dGeneralPlugin=Arrange in Scatter3d (General)",
    "SelectedOnly=Arrange only selected nodes"
})
public class ArrangeInScatter3dGeneralPlugin extends SimpleEditPlugin {

    public static final String SCATTER3D_X_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_x_attribute");
    public static final String SCATTER3D_Y_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_y_attribute");
    public static final String SCATTER3D_Z_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_z_attribute");
    public static final String SCATTER3D_X_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_x");
    public static final String SCATTER3D_Y_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_y");
    public static final String SCATTER3D_Z_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_z");

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
        radiusSetter.setRadii();

        final Map<String, PluginParameter<?>> pp = parameters.getParameters();
        final Scatter3dChoiceParameters scatter3dParams = Scatter3dChoiceParameters.getDefaultParameters();
        scatter3dParams.setXDimension(pp.get(SCATTER3D_X_ATTRIBUTE).getStringValue());
        scatter3dParams.setYDimension(pp.get(SCATTER3D_Y_ATTRIBUTE).getStringValue());
        scatter3dParams.setZDimension(pp.get(SCATTER3D_Z_ATTRIBUTE).getStringValue());
        scatter3dParams.setLogarithmicX(pp.get(SCATTER3D_X_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setLogarithmicY(pp.get(SCATTER3D_Y_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setLogarithmicZ(pp.get(SCATTER3D_Z_LOGARITHMIC).getBooleanValue());

        final Scatter3dArranger arranger = new Scatter3dArranger(scatter3dParams);
        arranger.arrange(graph);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> x_attribute = StringParameterType.build(SCATTER3D_X_ATTRIBUTE);
        x_attribute.setName("X Attribute");
        x_attribute.setDescription("The attribute to use for the x dimension");
        x_attribute.setStringValue("");
        parameters.addParameter(x_attribute);

        final PluginParameter<StringParameterValue> y_attribute = StringParameterType.build(SCATTER3D_Y_ATTRIBUTE);
        y_attribute.setName("Y Attribute");
        y_attribute.setDescription("The attribute to use for the y dimension");
        y_attribute.setStringValue("");
        parameters.addParameter(y_attribute);

        final PluginParameter<StringParameterValue> z_attribute = StringParameterType.build(SCATTER3D_Z_ATTRIBUTE);
        z_attribute.setName("Z Attribute");
        z_attribute.setDescription("The attribute to use for the z dimension");
        z_attribute.setStringValue("");
        parameters.addParameter(z_attribute);

        final PluginParameter<BooleanParameterValue> xLogarithmic = BooleanParameterType.build(SCATTER3D_X_LOGARITHMIC);
        xLogarithmic.setName("Use Logarithmic Scaling for X");
        xLogarithmic.setDescription("Scale the X axis in Logarithmic Scale");
        xLogarithmic.setBooleanValue(false);
        parameters.addParameter(xLogarithmic);

        final PluginParameter<BooleanParameterValue> yLogarithmic = BooleanParameterType.build(SCATTER3D_Y_LOGARITHMIC);
        yLogarithmic.setName("Use Logarithmic Scaling for Y");
        yLogarithmic.setDescription("Scale the Y axis in Logarithmic Scale");
        yLogarithmic.setBooleanValue(false);
        parameters.addParameter(yLogarithmic);

        final PluginParameter<BooleanParameterValue> zLogarithmic = BooleanParameterType.build(SCATTER3D_Z_LOGARITHMIC);
        zLogarithmic.setName("Use Logarithmic Scaling for Z");
        zLogarithmic.setDescription("Scale the Z axis in Logarithmic Scale");
        zLogarithmic.setBooleanValue(false);
        parameters.addParameter(zLogarithmic);

        return parameters;
    }
}
