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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin for scatter3d arrangement
 *
 * @author CrucisGamma
 */
@ServiceProvider(service = Plugin.class)
@Messages({
    "ArrangeInScatter3dGeneralPlugin=Arrange in Scatter 3D",
    "SelectedOnly=Arrange only selected nodes"
})
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInScatter3dGeneralPlugin extends SimpleEditPlugin {

    public static final String SCATTER_3D_X_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_x_attribute");
    public static final String SCATTER_3D_Y_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_y_attribute");
    public static final String SCATTER_3D_Z_ATTRIBUTE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_z_attribute");
    public static final String SCATTER_3D_X_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_x");
    public static final String SCATTER_3D_Y_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_y");
    public static final String SCATTER_3D_Z_LOGARITHMIC = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_logarithmic_z");
    public static final String SCATTER_3D_DO_NOT_SCALE = PluginParameter.buildId(ArrangeInScatter3dGeneralPlugin.class, "scatter3d_do_not_scale");

    private static final String X_ATTRIBUTE = "X Attribute";
    private static final String Y_ATTRIBUTE = "Y Attribute";
    private static final String Z_ATTRIBUTE = "Z Attribute";
    private static final String X_LOGARITHMIC = "Use Logarithmic Scaling for X";
    private static final String Y_LOGARITHMIC = "Use Logarithmic Scaling for Y";
    private static final String Z_LOGARITHMIC = "Use Logarithmic Scaling for Z";
    private static final String DO_NOT_USE_SCALE = "Do not use final Scaling algorithm";

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
        radiusSetter.setRadii();

        final Map<String, PluginParameter<?>> pp = parameters.getParameters();
        final Scatter3dChoiceParameters scatter3dParams = Scatter3dChoiceParameters.getDefaultParameters();

        final String xDimensionName = pp.get(SCATTER_3D_X_ATTRIBUTE).getStringValue();
        final String yDimensionName = pp.get(SCATTER_3D_Y_ATTRIBUTE).getStringValue();
        final String zDimensionName = pp.get(SCATTER_3D_Z_ATTRIBUTE).getStringValue();

        if (StringUtils.isAnyBlank(new String[]{xDimensionName, yDimensionName, zDimensionName})) {
            interaction.notify(PluginNotificationLevel.FATAL, "You must supply all 3 attribute names for Scatter 3D");
            return;
        }

        scatter3dParams.setXDimension(xDimensionName);
        scatter3dParams.setYDimension(yDimensionName);
        scatter3dParams.setZDimension(zDimensionName);
        scatter3dParams.setLogarithmicX(pp.get(SCATTER_3D_X_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setLogarithmicY(pp.get(SCATTER_3D_Y_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setLogarithmicZ(pp.get(SCATTER_3D_Z_LOGARITHMIC).getBooleanValue());
        scatter3dParams.setDoNotScale(pp.get(SCATTER_3D_DO_NOT_SCALE).getBooleanValue());

        final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, SelectedInclusionGraph.Connections.NONE);
        selectedGraph.addAttributeToCopy(new GraphAttribute(graph, graph.getAttribute(GraphElementType.VERTEX, xDimensionName)));
        selectedGraph.addAttributeToCopy(new GraphAttribute(graph, graph.getAttribute(GraphElementType.VERTEX, yDimensionName)));
        selectedGraph.addAttributeToCopy(new GraphAttribute(graph, graph.getAttribute(GraphElementType.VERTEX, zDimensionName)));

        final Scatter3dArranger arranger = new Scatter3dArranger(scatter3dParams);
        arranger.arrange(selectedGraph.getInclusionGraph());
        selectedGraph.retrieveCoords();
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> xAttribute = SingleChoiceParameterType.build(SCATTER_3D_X_ATTRIBUTE);
        xAttribute.setName(X_ATTRIBUTE);
        xAttribute.setDescription("The attribute to use for the x dimension");
        xAttribute.setStringValue("");
        parameters.addParameter(xAttribute);

        final PluginParameter<SingleChoiceParameterValue> yAttribute = SingleChoiceParameterType.build(SCATTER_3D_Y_ATTRIBUTE);
        yAttribute.setName(Y_ATTRIBUTE);
        yAttribute.setDescription("The attribute to use for the y dimension");
        yAttribute.setStringValue("");
        parameters.addParameter(yAttribute);

        final PluginParameter<SingleChoiceParameterValue> zAttribute = SingleChoiceParameterType.build(SCATTER_3D_Z_ATTRIBUTE);
        zAttribute.setName(Z_ATTRIBUTE);
        zAttribute.setDescription("The attribute to use for the z dimension");
        zAttribute.setStringValue("");
        parameters.addParameter(zAttribute);

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

        final PluginParameter<BooleanParameterValue> doNotScale = BooleanParameterType.build(SCATTER_3D_DO_NOT_SCALE);
        doNotScale.setName(DO_NOT_USE_SCALE);
        doNotScale.setDescription("Don't scale resultant scattergram");
        doNotScale.setBooleanValue(false);
        parameters.addParameter(doNotScale);

        return parameters;
    }

    @Override
    public void updateParameters(final Graph graph, final PluginParameters parameters) {

        // Get the list of non-default attributes
        final ReadableGraph rg = graph.getReadableGraph();
        Map<String, Integer> vertexAttributes = null;
        try {
            vertexAttributes = AttributeUtilities.getVertexAttributes(rg, 0);
        } finally {
            rg.release();
        }

        if (vertexAttributes != null) {
            final List<String> keys = new ArrayList<>(vertexAttributes.keySet());

            final PluginParameter<SingleChoiceParameterValue> xAttribute = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(SCATTER_3D_X_ATTRIBUTE);
            SingleChoiceParameterType.setOptions(xAttribute, keys);

            final PluginParameter<SingleChoiceParameterValue> yAttribute = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(SCATTER_3D_Y_ATTRIBUTE);
            SingleChoiceParameterType.setOptions(yAttribute, keys);

            final PluginParameter<SingleChoiceParameterValue> zAttribute = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(SCATTER_3D_Z_ATTRIBUTE);
            SingleChoiceParameterType.setOptions(zAttribute, keys);
        }
    }
}
