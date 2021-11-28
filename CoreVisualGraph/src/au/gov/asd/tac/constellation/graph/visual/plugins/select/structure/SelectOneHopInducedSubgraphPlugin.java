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
package au.gov.asd.tac.constellation.graph.visual.plugins.select.structure;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Select for induced subgraph connecting the current selection by one hop.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("SelectOneHopInducedSubgraphPlugin=Add to Selection: One Hop Induced Subgraph")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectOneHopInducedSubgraphPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        // deselect transactions
        PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_TRANSACTIONS).executeNow(graph);

        // identify all selected nodes
        final List<Integer> selectedNodes = new ArrayList<>();
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            if (graph.getBooleanValue(vertexSelectedAttributeId, vertexId)) {
                selectedNodes.add(vertexId);
            }
        }

        // identify all transactions with at least one adjacent node selected and select them if there is another selected vertex within one hop
        final int linkCount = graph.getLinkCount();
        for (int linkPosition = 0; linkPosition < linkCount; linkPosition++) {
            boolean selectLink = false;
            final int linkId = graph.getLink(linkPosition);
            final int linkTransactionCount = graph.getLinkTransactionCount(linkId);
            for (int transactionPosition = 0; transactionPosition < linkTransactionCount; transactionPosition++) {
                final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
                final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
                final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);
                if (selectedNodes.contains(sourceVertexId) && selectedNodes.contains(destinationVertexId)) {
                    selectLink = true;
                } else if (selectedNodes.contains(sourceVertexId)) {
                    if (hasSelectedNeighbour(graph, destinationVertexId, sourceVertexId, vertexSelectedAttributeId)) {
                        selectLink = true;
                    }
                } else if (selectedNodes.contains(destinationVertexId) && hasSelectedNeighbour(graph, sourceVertexId, destinationVertexId, vertexSelectedAttributeId)) {
                    selectLink = true;
                } else {
                    // Do nothing
                }
            }
            if (selectLink) {
                for (int transactionPosition = 0; transactionPosition < linkTransactionCount; transactionPosition++) {
                    final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
                    graph.setBooleanValue(transactionSelectedAttributeId, transactionId, true);
                }
            }
        }

        // identify all vertices with two or more adjacent transactions selected and select them
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            boolean selectVertex = false;
            int adjacentTransactionsSelected = 0;
            final int vertexId = graph.getVertex(vertexPosition);
            final int vertexTransactionCount = graph.getVertexTransactionCount(vertexId);
            for (int vertexTransactionPosition = 0; vertexTransactionPosition < vertexTransactionCount; vertexTransactionPosition++) {
                final int transactionId = graph.getVertexTransaction(vertexId, vertexTransactionPosition);
                final boolean transactionSelected = graph.getBooleanValue(transactionSelectedAttributeId, transactionId);
                if (transactionSelected) {
                    adjacentTransactionsSelected++;
                }
                if (adjacentTransactionsSelected > 1) {
                    selectVertex = true;
                }
            }
            if (selectVertex) {
                graph.setBooleanValue(vertexSelectedAttributeId, vertexId, true);
            }
        }
    }

    private boolean hasSelectedNeighbour(final GraphReadMethods graph, final int vertexId, final int excludeId, final int vertexSelectedAttributeId) {
        final int neightboutCount = graph.getVertexNeighbourCount(vertexId);
        for (int neighbourPosition = 0; neighbourPosition < neightboutCount; neighbourPosition++) {
            final int neighbourId = graph.getVertexNeighbour(vertexId, neighbourPosition);
            if (neighbourId == excludeId) {
                continue;
            }
            final boolean neighbourSelected = graph.getBooleanValue(vertexSelectedAttributeId, neighbourId);
            if (neighbourSelected) {
                return true;
            }
        }
        return false;
    }
}
