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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import org.openide.util.NbBundle;

/**
 *
 * @author twilight_sparkle
 */
@NbBundle.Messages("PointSelectionPlugin=Select Graph Element")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public final class PointSelectionPlugin extends SimpleEditPlugin {

    final IntArray vertexIds;
    final IntArray transactionIds;
    final boolean isToggle;
    final boolean clearSelection;

    public PointSelectionPlugin(final IntArray vertexIds, final IntArray transactionIds, final boolean isToggle, final boolean clearSelection) {
        this.vertexIds = vertexIds;
        this.transactionIds = transactionIds;
        this.isToggle = isToggle;
        this.clearSelection = clearSelection;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int maxTransactionsAttribute = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.get(graph);
        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        final int connectionModeAttrId = VisualConcept.GraphAttribute.CONNECTION_MODE.get(graph);
        final ConnectionMode connectionMode = connectionModeAttrId != Graph.NOT_FOUND ? graph.getObjectValue(connectionModeAttrId, 0) : ConnectionMode.EDGE;
        final int maxTransactionsDrawn = maxTransactionsAttribute != Graph.NOT_FOUND ? graph.getIntValue(maxTransactionsAttribute, 0) : VisualGraphDefaults.DEFAULT_MAX_TRANSACTION_TO_DRAW;

        // Deselect all vertices and transactions.
        if (clearSelection) {
            if (vertexSelectedAttribute != Graph.NOT_FOUND) {
                for (int position = 0; position < graph.getVertexCount(); position++) {
                    final int vertexId = graph.getVertex(position);
                    if (graph.getBooleanValue(vertexSelectedAttribute, vertexId)) {
                        graph.setBooleanValue(vertexSelectedAttribute, vertexId, false);
                    }
                }
            }
            if (transactionSelectedAttribute != Graph.NOT_FOUND) {
                for (int position = 0; position < graph.getTransactionCount(); position++) {
                    final int transactionId = graph.getTransaction(position);
                    if (graph.getBooleanValue(transactionSelectedAttribute, transactionId)) {
                        graph.setBooleanValue(transactionSelectedAttribute, transactionId, false);
                    }
                }
            }
        }

        if (vertexSelectedAttribute != Graph.NOT_FOUND) {
            vertexIds.forEach(vertexId -> {
                final boolean select = !isToggle || !graph.getBooleanValue(vertexSelectedAttribute, vertexId);
                graph.setBooleanValue(vertexSelectedAttribute, vertexId, select);
            });
        }

        if (transactionSelectedAttribute != Graph.NOT_FOUND) {
            transactionIds.forEach(transactionId -> {
                final boolean select = !isToggle || !graph.getBooleanValue(transactionSelectedAttribute, transactionId);
                // When a line is selected using the mouse, it may represent a single transaction or multiple transactions, depending on what the current ConnectionMode is.
                // The line is given the id of whichever transaction SceneBatchStore happens to see first. We use that id to select the other transactions represented by that id (if any).
                // We also have to take into account that the renderer draws transactions as edges if there are more than maxTxDrawn transactions between two vertices.
                if (connectionMode == ConnectionMode.LINK) {
                    final int linkId = graph.getTransactionLink(transactionId);
                    final int txCount = graph.getLinkTransactionCount(linkId);
                    for (int position = 0; position < txCount; position++) {
                        final int tx = graph.getLinkTransaction(linkId, position);
                        graph.setBooleanValue(transactionSelectedAttribute, tx, select);
                    }
                } else {
                    final int edgeId = graph.getTransactionEdge(transactionId);
                    final int edgeTxCount = graph.getEdgeTransactionCount(edgeId);
                    final int linkId = graph.getTransactionLink(transactionId);
                    final int linkTxCount = graph.getLinkTransactionCount(linkId);
                    if (connectionMode == ConnectionMode.EDGE || linkTxCount > maxTransactionsDrawn) {
                        for (int position = 0; position < edgeTxCount; position++) {
                            final int tx = graph.getEdgeTransaction(edgeId, position);
                            graph.setBooleanValue(transactionSelectedAttribute, tx, select);
                        }
                    } else {
                        graph.setBooleanValue(transactionSelectedAttribute, transactionId, select);
                    }
                }
            });
        }
    }
}
