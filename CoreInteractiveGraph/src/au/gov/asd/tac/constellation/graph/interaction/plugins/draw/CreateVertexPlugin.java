/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * copy a node
 *
 * @author procyon
 */
@ServiceProvider(service = Plugin.class)
@Messages("CreateVertexPlugin=Create Vertex")
public final class CreateVertexPlugin extends SimpleEditPlugin {

    public static final String X_PARAMETER_ID = PluginParameter.buildId(CreateVertexPlugin.class, VisualConcept.VertexAttribute.X.getName());
    public static final String Y_PARAMETER_ID = PluginParameter.buildId(CreateVertexPlugin.class, VisualConcept.VertexAttribute.Y.getName());
    public static final String Z_PARAMETER_ID = PluginParameter.buildId(CreateVertexPlugin.class, VisualConcept.VertexAttribute.Z.getName());

    private float x;
    private float y;
    private float z;

    public CreateVertexPlugin() {
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<FloatParameterValue> xParam = FloatParameterType.build(X_PARAMETER_ID);
        xParam.setName("X");
        xParam.setDescription("The position of the X coordinate");
        xParam.setFloatValue(0f);
        parameters.addParameter(xParam);

        final PluginParameter<FloatParameterValue> yParam = FloatParameterType.build(Y_PARAMETER_ID);
        yParam.setName("Y");
        yParam.setDescription("The position of the Y coordinate");
        yParam.setFloatValue(0f);
        parameters.addParameter(yParam);

        final PluginParameter<FloatParameterValue> zParam = FloatParameterType.build(Z_PARAMETER_ID);
        zParam.setName("Z");
        zParam.setDescription("The position of the Z coordinate");
        zParam.setFloatValue(0f);
        parameters.addParameter(zParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

        this.x = parameters.getParameters().get(X_PARAMETER_ID).getFloatValue();
        this.y = parameters.getParameters().get(Y_PARAMETER_ID).getFloatValue();
        this.z = parameters.getParameters().get(Z_PARAMETER_ID).getFloatValue();

        final int xAttrId = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttrId = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttrId = VisualConcept.VertexAttribute.Z.get(graph);

        final int vxId = graph.addVertex();
        graph.setFloatValue(xAttrId, vxId, x);
        graph.setFloatValue(yAttrId, vxId, y);
        graph.setFloatValue(zAttrId, vxId, z);

        graph.getSchema().newVertex(graph, vxId);
    }
}
