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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
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

    public static final String SCATTER_3D_X_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_x_attribute");
    public static final String SCATTER_3D_Y_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_y_attribute");
    public static final String SCATTER_3D_Z_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_z_attribute");
    public static final String SCATTER_3D_X_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_x");
    public static final String SCATTER_3D_Y_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_y");
    public static final String SCATTER_3D_Z_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_z");

    private final String X_ATTRIBUTE = "X Attribute";
    private final String Y_ATTRIBUTE = "Y Attribute";
    private final String Z_ATTRIBUTE = "Z Attribute";
    private final String X_LOGARITHMIC = "Use Logarithmic Scaling for X";
    private final String Y_LOGARITHMIC = "Use Logarithmic Scaling for Y";
    private final String Z_LOGARITHMIC = "Use Logarithmic Scaling for Z";

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
        radiusSetter.setRadii();

        final Map<String, PluginParameter<?>> pp = parameters.getParameters();
        final Scatter3dChoiceParameters scatter3dParams = Scatter3dChoiceParameters.getDefaultParameters();
        scatter3dParams.setXDimension(pp.get(SCATTER_3D_X_ATTRIBUTE).getStringValue());
        scatter3dParams.setYDimension(pp.get(SCATTER_3D_Y_ATTRIBUTE).getStringValue());
        scatter3dParams.setZDimension(pp.get(SCATTER_3D_Z_ATTRIBUTE).getStringValue());
        scatter3dParams.setLogarithmicX(pp.get(SCATTER_3D_X_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setLogarithmicY(pp.get(SCATTER_3D_Y_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setLogarithmicZ(pp.get(SCATTER_3D_Z_LOGARITHMIC).getBooleanValue());

        final Scatter3dArranger arranger = new Scatter3dArranger(scatter3dParams);
        arranger.arrange(graph);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> x_attribute = SingleChoiceParameterType.build(SCATTER_3D_X_ATTRIBUTE);
        x_attribute.setName(X_ATTRIBUTE);
        x_attribute.setDescription("The attribute to use for the x dimension");
        x_attribute.setStringValue("");
        parameters.addParameter(x_attribute);

        final PluginParameter<SingleChoiceParameterValue> y_attribute = SingleChoiceParameterType.build(SCATTER_3D_Y_ATTRIBUTE);
        y_attribute.setName(Y_ATTRIBUTE);
        y_attribute.setDescription("The attribute to use for the y dimension");
        y_attribute.setStringValue("");
        parameters.addParameter(y_attribute);

        final PluginParameter<SingleChoiceParameterValue> z_attribute = SingleChoiceParameterType.build(SCATTER_3D_Z_ATTRIBUTE);
        z_attribute.setName(Z_ATTRIBUTE);
        z_attribute.setDescription("The attribute to use for the z dimension");
        z_attribute.setStringValue("");
        parameters.addParameter(z_attribute);

        final PluginParameter<BooleanParameterValue> xLogarithmic = BooleanParameterType.build(SCATTER_3D_X_LOGARITHMIC);
        xLogarithmic.setName(X_LOGARITHMIC);
        xLogarithmic.setDescription("Scale the X axis in Logarithmic Scale");
        xLogarithmic.setBooleanValue(false);
        parameters.addParameter(xLogarithmic);

        final PluginParameter<BooleanParameterValue> yLogarithmic = BooleanParameterType.build(SCATTER_3D_Y_LOGARITHMIC);
        yLogarithmic.setName(Y_LOGARITHMIC);
        yLogarithmic.setDescription("Scale the Y axis in Logarithmic Scale");
        yLogarithmic.setBooleanValue(false);
        parameters.addParameter(yLogarithmic);

        final PluginParameter<BooleanParameterValue> zLogarithmic = BooleanParameterType.build(SCATTER_3D_Z_LOGARITHMIC);
        zLogarithmic.setName(Z_LOGARITHMIC);
        zLogarithmic.setDescription("Scale the Z axis in Logarithmic Scale");
        zLogarithmic.setBooleanValue(false);
        parameters.addParameter(zLogarithmic);

        return parameters;
    }

    @Override
    public void updateParameters(Graph graph, PluginParameters parameters) {

        // Get the list of non-default attributes
        final ReadableGraph rg = graph.getReadableGraph();
        Map<String, Integer> vertexAttributes = null;
        try {
            vertexAttributes = AttributeUtilities.getVertexAttributes(rg, 0);
        } finally {
            rg.release();
        }

        final List<String> keys = new ArrayList<>(vertexAttributes.keySet());

        final PluginParameter<SingleChoiceParameterValue> xdimension = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(SCATTER_3D_X_ATTRIBUTE);
        SingleChoiceParameterType.setOptions(xdimension, keys);

        final PluginParameter<SingleChoiceParameterValue> ydimension = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(SCATTER_3D_Y_ATTRIBUTE);
        SingleChoiceParameterType.setOptions(ydimension, keys);

        final PluginParameter<SingleChoiceParameterValue> zdimension = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(SCATTER_3D_Z_ATTRIBUTE);
        SingleChoiceParameterType.setOptions(zdimension, keys);
    }
}
