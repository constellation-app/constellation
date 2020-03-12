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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Directed shortest paths plugin.
 *
 * @author procyon
 */
@ServiceProvider(service = Plugin.class)
@Messages("DirectedShortestPathsPlugin=Directed Shortest Paths")
public class DirectedShortestPathsPlugin extends SimpleEditPlugin {

    public static final String SOURCE_VERTEX_PARAMETER_ID = PluginParameter.buildId(DirectedShortestPathsPlugin.class, "source_vertex");
    public static final Integer SOURCE_VERTEX_NOT_SET = -1;

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> sourceVertex = SingleChoiceParameterType.build(SOURCE_VERTEX_PARAMETER_ID);
        sourceVertex.setName("Source Node");
        sourceVertex.setDescription("The source node is used to dictate the direction");
        SingleChoiceParameterType.setOptions(sourceVertex, Lists.newArrayList(getSelectedNodes().keySet()));
        params.addParameter(sourceVertex);

        return params;
    }

    /**
     * Returns true if the provided <code>Graph</code> has more than one vertex
     * selected.
     *
     * @param graph the read lock that will be used to query the graph.
     * @return true if the provided <code>Graph</code> has more than one vertex
     * selected.
     */
    public static boolean hasMultipleSelections(final GraphReadMethods graph) {
        int count = 0;
        final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int vxCount = graph.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);
            if (graph.getBooleanValue(vxSelectedAttr, vxId)) {
                count++;

                if (count > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final Map<String, PluginParameter<?>> params = parameters.getParameters();

        final List<Integer> verticesToPath = new ArrayList<>();
        // Make sure more than one vertex on the graph is selected.
        if (hasMultipleSelections(graph)) {
            // Clear current transaction selections.
            final int txCount = graph.getTransactionCount();
            for (int position = 0; position < txCount; position++) {
                final int txId = graph.getTransaction(position);
                graph.setBooleanValue(VisualConcept.TransactionAttribute.SELECTED.get(graph), txId, false);
            }

            int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
            if (vxSelectedAttr != Graph.NOT_FOUND) {
                final int vxCount = graph.getVertexCount();
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = graph.getVertex(position);

                    //Check if the vertex is selected.
                    if (graph.getBooleanValue(vxSelectedAttr, vxId)) {
                        //Add vertex to list of vertices to find paths between.
                        verticesToPath.add(vxId);
                    }
                }

                final String sourceVertexLabel = params.get(SOURCE_VERTEX_PARAMETER_ID).getStringValue();
                final int sourceVertex = sourceVertexLabel == null ? SOURCE_VERTEX_NOT_SET : getSelectedNodes().get(sourceVertexLabel);

                // if a source vertex is set then make sure it is at the start of the List
                if (sourceVertex != SOURCE_VERTEX_NOT_SET) {
                    if (verticesToPath.get(0) != sourceVertex) {
                        verticesToPath.remove((Object) sourceVertex);
                        verticesToPath.add(0, sourceVertex);
                    }
                }

                DijkstraServices ds = new DijkstraServices(graph, verticesToPath, true);
                ds.queryPaths(true);
            }
        }
    }

    private Map<String, Integer> getSelectedNodes() {
        final Map<String, Integer> label2VxId = new HashMap<>();
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph != null) {
            final ReadableGraph rg = graph.getReadableGraph();
            try {
                final int vxLabelAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.LABEL.getName());
                final int vxSelectedAttr = rg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.SELECTED.getName());

                if (vxSelectedAttr != Graph.NOT_FOUND && vxLabelAttr != Graph.NOT_FOUND) {
                    final int vxCount = rg.getVertexCount();
                    for (int position = 0; position < vxCount; position++) {
                        final int vxId = rg.getVertex(position);
                        if (rg.getBooleanValue(vxSelectedAttr, vxId)) {
                            final String label = rg.getStringValue(vxLabelAttr, vxId);
                            label2VxId.put(label, vxId);
                        }
                    }
                }
            } finally {
                rg.release();
            }
        }

        return label2VxId;
    }

}
