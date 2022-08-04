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
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Select the induced subgraph connecting the current selection by half hop.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("SelectHalfHopInducedSubgraphPlugin=Add to Selection: Half Hop Induced Subgraph")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectHalfHopInducedSubgraphPlugin extends SimpleEditPlugin {

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

        // identify all links whose adjacent nodes are both selected
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
                }
            }
            if (selectLink) {
                for (int transactionPosition = 0; transactionPosition < linkTransactionCount; transactionPosition++) {
                    final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
                    graph.setBooleanValue(transactionSelectedAttributeId, transactionId, true);
                }
            }
        }
    }
}
