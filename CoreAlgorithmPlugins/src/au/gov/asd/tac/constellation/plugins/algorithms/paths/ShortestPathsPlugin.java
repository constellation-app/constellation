/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Shortest Paths.
 *
 * @author procyon
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(tags = {PluginTags.ANALYTIC})
@Messages("ShortestPathsPlugin=Shortest Paths")
public class ShortestPathsPlugin extends SimpleEditPlugin {

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
        final List<Integer> verticesToPath = new ArrayList<>();
        // Make sure more than one vertex on the graph is selected.
        if (hasMultipleSelections(graph)) {
            // Clear current transaction selections.
            final int txCount = graph.getTransactionCount();
            for (int position = 0; position < txCount; position++) {
                final int txId = graph.getTransaction(position);
                graph.setBooleanValue(VisualConcept.TransactionAttribute.SELECTED.get(graph), txId, false);
            }

            final int vxCount = graph.getVertexCount();
            final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
            if (vxSelectedAttr != Graph.NOT_FOUND) {
                for (int position = 0; position < vxCount; position++) {
                    final int vxId = graph.getVertex(position);

                    //Check if the vertex is selected.
                    if (graph.getBooleanValue(vxSelectedAttr, vxId)) {
                        //Add vertex to list of vertices to find paths between.
                        verticesToPath.add(vxId);
                    }
                }

                final DijkstraServices ds = new DijkstraServices(graph, verticesToPath, false);
                ds.queryPaths(true);
            }
        }
    }
}
