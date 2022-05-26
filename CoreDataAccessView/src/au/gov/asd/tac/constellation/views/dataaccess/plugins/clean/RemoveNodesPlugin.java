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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Allows removal of nodes based on certain criteria.
 *
 * @author cygnus_x-1
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@Messages("RemoveNodesPlugin=Remove Nodes")
@PluginInfo(pluginType = PluginType.DELETE, tags = {PluginTags.DELETE})
public class RemoveNodesPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    private static final String REMOVE_TYPE_LENGTH = "Identifier Length";

    public static final String THRESHOLD_PARAMETER_ID = PluginParameter.buildId(RemoveNodesPlugin.class, "threshold");
    public static final String REMOVE_TYPE_PARAMETER_ID = PluginParameter.buildId(RemoveNodesPlugin.class, "remove_type");

    @Override
    public String getType() {
        return DataAccessPluginCoreType.CLEAN;
    }

    @Override
    public int getPosition() {
        return 50;
    }

    @Override
    public String getDescription() {
        return "Remove nodes from your graph";
    }

    @Override
    public PluginParameters createParameters() {

        final PluginParameters params = new PluginParameters();

        final List<String> removeTypes = new ArrayList<>();
        removeTypes.add(REMOVE_TYPE_LENGTH);
        final PluginParameter<SingleChoiceParameterValue> removeType = SingleChoiceParameterType.build(REMOVE_TYPE_PARAMETER_ID);
        removeType.setName("Remove By");
        removeType.setDescription("Nodes will be removed based on this");
        removeType.setRequired(true);
        SingleChoiceParameterType.setOptions(removeType, removeTypes);
        SingleChoiceParameterType.setChoice(removeType, REMOVE_TYPE_LENGTH);
        params.addParameter(removeType);

        final PluginParameter<IntegerParameterValue> threshold = IntegerParameterType.build(THRESHOLD_PARAMETER_ID);
        threshold.setName("Threshold");
        threshold.setDescription("Threshold (length)");
        threshold.setIntegerValue(10);
        params.addParameter(threshold);

        return params;
    }

    @Override
    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        int removedCount = 0;

        interaction.setProgress(0, 0, "Removing nodes...", true);

        final String removeType = parameters.getParameters().get(REMOVE_TYPE_PARAMETER_ID).getStringValue();
        final int threshold = parameters.getParameters().get(THRESHOLD_PARAMETER_ID).getIntegerValue();

        final int selectedId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        if (selectedId != Graph.NOT_FOUND) {
            final List<Integer> verticesToRemove = new ArrayList<>();
            if (removeType.equals(REMOVE_TYPE_LENGTH)) {
                final int identifierAttribute = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.IDENTIFIER.getName());
                if (identifierAttribute != Graph.NOT_FOUND) {
                    final int vertexCount = wg.getVertexCount();
                    for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                        final int vxId = wg.getVertex(vertexPosition);

                        if (wg.getBooleanValue(selectedId, vxId)) {
                            verticesToRemove.add(vxId);
                        }
                    }
                    for (final int vertex : verticesToRemove) {
                        if (removeNodesByLength(wg, vertex, identifierAttribute, threshold)) {
                            removedCount++;
                        }
                    }
                }
            }
        }

        interaction.setProgress(1, 0, "Removed " + removedCount + " nodes.", true);
    }

    private boolean removeNodesByLength(final GraphWriteMethods wg, final int vertex, final int identifierAttribute, final int threshold) {
        final String identifier = wg.getStringValue(identifierAttribute, vertex);
        if (identifier.length() <= threshold) {
            wg.removeVertex(vertex);
            return true;
        }
        return false;
    }
}
