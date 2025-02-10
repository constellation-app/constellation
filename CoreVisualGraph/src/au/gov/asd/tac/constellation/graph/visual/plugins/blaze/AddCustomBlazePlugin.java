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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.COLOR_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_ID_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.BitSet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Add a custom blaze to the specified vertex/vertices.
 * <p>
 * If the vxId int parameter is used, a single vertex will have its blaze
 * toggled, otherwise all of the vertices in the vxIds BitSet have their blazes
 * toggled.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("AddCustomBlazePlugin=Add Custom Blazes")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class AddCustomBlazePlugin extends SimpleEditPlugin {

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> vertexIdParam = IntegerParameterType.build(VERTEX_ID_PARAMETER_ID);
        vertexIdParam.setName("Vertex Id");
        vertexIdParam.setDescription("The vertex id to add a color blaze");
        vertexIdParam.setIntegerValue(Graph.NOT_FOUND);
        parameters.addParameter(vertexIdParam);

        final PluginParameter<ObjectParameterValue> vertexIdsParam = ObjectParameterType.build(VERTEX_IDS_PARAMETER_ID);
        vertexIdsParam.setName("Vertex Ids");
        vertexIdsParam.setDescription("The list of vertex ids to add a color blaze (in bulk)");
        parameters.addParameter(vertexIdsParam);

        final PluginParameter<ColorParameterValue> colorParam = ColorParameterType.build(COLOR_PARAMETER_ID);
        colorParam.setName("Color");
        colorParam.setDescription("The color value");
        colorParam.setColorValue(BlazeUtilities.DEFAULT_BLAZE.getColor());
        parameters.addParameter(colorParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int blazeAttr = VisualConcept.VertexAttribute.BLAZE.ensure(wg);
        final ConstellationColor cv = parameters.getColorValue(COLOR_PARAMETER_ID);
        final int vxId = parameters.getIntegerValue(VERTEX_ID_PARAMETER_ID);
        if (vxId != Graph.NOT_FOUND) {
            setBlazeColor(wg, blazeAttr, vxId, cv);
        } else {
            final BitSet vertices = BlazePluginUtilities.verticesParam(parameters);
            if (vertices != null) {
                for (int ix = vertices.nextSetBit(0); ix >= 0; ix = vertices.nextSetBit(ix + 1)) {
                    setBlazeColor(wg, blazeAttr, ix, cv);
                }
            } else {
                final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(wg);
                if (selectedAttr != Graph.NOT_FOUND) {
                    int vertexCount = wg.getVertexCount();
                    for (int i = 0; i < vertexCount; i++) {
                        int v = wg.getVertex(i);
                        if (wg.getBooleanValue(selectedAttr, v)) {
                            setBlazeColor(wg, blazeAttr, v, cv);
                        }
                    }
                }
            }
        }
    }

    /**
     * If a blaze exists on the specified vertex, set its color.
     *
     * @param wg
     * @param blazeId
     * @param vxId
     * @param cv
     */
    private static void setBlazeColor(final GraphWriteMethods wg, final int blazeId, final int vxId, final ConstellationColor cv) {
        Blaze blaze = (Blaze) wg.getObjectValue(blazeId, vxId);
        if (blaze == null) {
            blaze = BlazeUtilities.DEFAULT_BLAZE;
        }

        final Blaze blazec = new Blaze(blaze.getAngle(), cv);
        wg.setObjectValue(blazeId, vxId, blazec);
    }
}
