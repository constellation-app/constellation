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
package au.gov.asd.tac.constellation.graph.interaction.plugins.draw;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Copy a node.
 *
 * @author procyon
 */
@ServiceProvider(service = Plugin.class)
@Messages("CreateVertexPlugin=Create Vertex")
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
public final class CreateVertexPlugin extends SimpleEditPlugin {

    public static final String X_PARAMETER_ID = PluginParameter.buildId(CreateVertexPlugin.class, VisualConcept.VertexAttribute.X.getName());
    public static final String Y_PARAMETER_ID = PluginParameter.buildId(CreateVertexPlugin.class, VisualConcept.VertexAttribute.Y.getName());
    public static final String Z_PARAMETER_ID = PluginParameter.buildId(CreateVertexPlugin.class, VisualConcept.VertexAttribute.Z.getName());

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FloatParameterValue> xParam = FloatParameterType.build(X_PARAMETER_ID);
        xParam.setName("X");
        xParam.setDescription("The position of the X coordinate");
        xParam.setFloatValue(0F);
        parameters.addParameter(xParam);

        final PluginParameter<FloatParameterValue> yParam = FloatParameterType.build(Y_PARAMETER_ID);
        yParam.setName("Y");
        yParam.setDescription("The position of the Y coordinate");
        yParam.setFloatValue(0F);
        parameters.addParameter(yParam);

        final PluginParameter<FloatParameterValue> zParam = FloatParameterType.build(Z_PARAMETER_ID);
        zParam.setName("Z");
        zParam.setDescription("The position of the Z coordinate");
        zParam.setFloatValue(0F);
        parameters.addParameter(zParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final float x = parameters.getParameters().get(X_PARAMETER_ID).getFloatValue();
        final float y = parameters.getParameters().get(Y_PARAMETER_ID).getFloatValue();
        final float z = parameters.getParameters().get(Z_PARAMETER_ID).getFloatValue();

        final int xAttrId = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttrId = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttrId = VisualConcept.VertexAttribute.Z.get(graph);

        final int vxLayerAttrId = LayersConcept.VertexAttribute.LAYER_MASK.get(graph);
        final int graphLayerAttrId = LayersConcept.GraphAttribute.LAYER_MASK_SELECTED.get(graph);

        final int vxId = graph.addVertex();
        graph.setFloatValue(xAttrId, vxId, x);
        graph.setFloatValue(yAttrId, vxId, y);
        graph.setFloatValue(zAttrId, vxId, z);

        // add layer mask attributes
        if (graphLayerAttrId != Graph.NOT_FOUND && vxLayerAttrId != Graph.NOT_FOUND) {
            int layer = graph.getIntValue(graphLayerAttrId, 0);
            layer = layer == 1 ? 1 : layer | (1 << 0);
            graph.setIntValue(vxLayerAttrId, vxId, layer);
        }

        graph.getSchema().newVertex(graph, vxId);
    }
}
