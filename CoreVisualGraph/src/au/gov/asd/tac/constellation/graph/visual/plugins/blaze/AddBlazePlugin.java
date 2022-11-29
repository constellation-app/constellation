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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.DEFAULT_BLAZE;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_IDS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities.VERTEX_ID_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.BitSet;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Add a default blaze to the specified vertex/vertices.
 * <p>
 * If the vxId int parameter is used, a single vertex will have its blaze
 * toggled, otherwise all of the vertices in the vxIds BitSet have their blazes
 * toggled.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("AddBlazePlugin=Add Blazes")
public class AddBlazePlugin extends SimpleEditPlugin {

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> vertexIdParam = IntegerParameterType.build(VERTEX_ID_PARAMETER_ID);
        vertexIdParam.setName("Vertex Id");
        vertexIdParam.setDescription("The vertex id of the node to set a blaze");
        vertexIdParam.setObjectValue(Graph.NOT_FOUND);
        parameters.addParameter(vertexIdParam);

        final PluginParameter<ObjectParameterValue> vertexIdsParam = ObjectParameterType.build(VERTEX_IDS_PARAMETER_ID);
        vertexIdsParam.setObjectValue(null);
        vertexIdsParam.setName("Vertex Ids");
        vertexIdsParam.setDescription("The list of vertex ids to set a blaze for in bulk");
        parameters.addParameter(vertexIdsParam);

        return parameters;
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        int blazeAttr = VisualConcept.VertexAttribute.BLAZE.ensure(wg);

        final int vxId = parameters.getIntegerValue(VERTEX_ID_PARAMETER_ID);
        if (vxId != Graph.NOT_FOUND) {
            wg.setObjectValue(blazeAttr, vxId, DEFAULT_BLAZE);
        } else {
            final BitSet vertices = BlazePluginUtilities.verticesParam(parameters);
            if (vertices != null) {
                for (int ix = vertices.nextSetBit(0); ix >= 0; ix = vertices.nextSetBit(ix + 1)) {
                    wg.setObjectValue(blazeAttr, ix, DEFAULT_BLAZE);
                }
            } else {
                int selectedAttr = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
                if (selectedAttr != Graph.NOT_FOUND) {
                    int vertexCount = wg.getVertexCount();
                    for (int i = 0; i < vertexCount; i++) {
                        int v = wg.getVertex(i);
                        if (wg.getBooleanValue(selectedAttr, v)) {
                            wg.setObjectValue(blazeAttr, v, DEFAULT_BLAZE);
                        }
                    }
                }
            }
        }
    }
}
