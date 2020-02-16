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
package au.gov.asd.tac.constellation.arrangements.gather;

import au.gov.asd.tac.constellation.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.contextmenu.ContextMenuProvider;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginExecution;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.visual.graphics3d.Vector3f;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Gather Nodes Context Menu
 *
 * @author algol
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 100)
@Messages("GatherNodesContextMenu=Gather Selected Nodes")
public class GatherNodesContextMenu implements ContextMenuProvider {

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        if (elementType == GraphElementType.GRAPH || elementType == GraphElementType.VERTEX) {
            return Arrays.asList("Gather Selected Nodes");
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int element, final Vector3f unprojected) {
        switch (elementType) {

            case GRAPH:
                PluginExecution.withPlugin(new SimpleEditPlugin(Bundle.GatherNodesContextMenu()) {
                    @Override
                    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                        final BitSet gathers = selectedVertexBits(graph);

                        PluginExecution.withPlugin(ArrangementPluginRegistry.GATHER_NODES_IN_GRAPH)
                                .withParameter(GatherNodesInGraphPlugin.XYZ_PARAMETER_ID, unprojected)
                                .withParameter(GatherNodesInGraphPlugin.GATHERS_PARAMETER_ID, gathers)
                                .executeNow(graph);
                    }
                }).executeLater(graph);
                break;

            case VERTEX:
                PluginExecution.withPlugin(new SimpleEditPlugin(Bundle.GatherNodesContextMenu()) {
                    @Override
                    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                        final BitSet gathers = selectedVertexBits(graph);

                        PluginExecution.withPlugin(ArrangementPluginRegistry.GATHER_NODES)
                                .withParameter(GatherNodesPlugin.VXID_PARAMETER_ID, element)
                                .withParameter(GatherNodesPlugin.GATHERS_PARAMETER_ID, gathers)
                                .executeNow(graph);
                    }
                }).executeLater(graph);
                break;
        }
    }

    /**
     * Gather a graph's selected vertex ids into a BitSet.
     *
     * @param graph The graph.
     *
     * @return A BitSet where selected vertex ids in the graph are set.
     */
    private BitSet selectedVertexBits(final GraphReadMethods graph) {
        final int selectedAttributeId = graph.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());
        final int vertexCount = graph.getVertexCount();
        final BitSet vertexBits = new BitSet();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);

            if (graph.getBooleanValue(selectedAttributeId, vertexId)) {
                vertexBits.set(vertexId);
            }
        }

        return vertexBits;
    }
}
