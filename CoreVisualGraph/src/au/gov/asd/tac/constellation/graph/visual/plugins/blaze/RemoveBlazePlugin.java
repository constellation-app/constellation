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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_ID_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.BitSet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Remove the blaze from the specified vertex/vertices.
 * <p>
 * If the vxId int parameter is used, a single vertex will have its blaze
 * toggled, otherwise all of the vertices in the vxIds BitSet have their blazes
 * toggled.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("RemoveBlazePlugin=Remove Blazes")
@PluginInfo(pluginType = PluginType.DELETE, tags = {PluginTags.MODIFY})
public class RemoveBlazePlugin extends SimpleEditPlugin {

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> vertexIdParam = IntegerParameterType.build(VERTEX_ID_PARAMETER_ID);
        vertexIdParam.setName("Vertex Id");
        vertexIdParam.setDescription("The vertex id of the node to set a blaze");
        vertexIdParam.setIntegerValue(Graph.NOT_FOUND);
        parameters.addParameter(vertexIdParam);

        final PluginParameter<ObjectParameterValue> vertexIdsParam = ObjectParameterType.build(VERTEX_IDS_PARAMETER_ID);
        vertexIdsParam.setName("Vertex IDs");
        vertexIdsParam.setDescription("The list of vertex id's to set a blaze for in bulk");
        vertexIdsParam.setObjectValue(null);
        parameters.addParameter(vertexIdsParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int blazeAttr = VisualConcept.VertexAttribute.BLAZE.get(wg);
        if (blazeAttr != Graph.NOT_FOUND) {
            final int vxId = parameters.getIntegerValue(VERTEX_ID_PARAMETER_ID);
            if (vxId != Graph.NOT_FOUND) {
                wg.setObjectValue(blazeAttr, vxId, null);

            } else {
                final BitSet vertices = BlazePluginUtilities.verticesParam(parameters);
                if (vertices != null) {
                    for (int ix = vertices.nextSetBit(0); ix >= 0; ix = vertices.nextSetBit(ix + 1)) {
                        wg.setObjectValue(blazeAttr, ix, null);
                    }
                } else {
                    int selectedAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                    if (selectedAttr != Graph.NOT_FOUND) {
                        final int vxCount = wg.getVertexCount();
                        for (int position = 0; position < vxCount; position++) {
                            int v = wg.getVertex(position);
                            if (wg.getBooleanValue(selectedAttr, v)) {
                                wg.setObjectValue(blazeAttr, v, null);
                            }
                        }
                    }
                }
            }
        }
    }
}
