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
package au.gov.asd.tac.constellation.graph.interaction.plugins.delete;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin framework supported the delete selected elements
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("DeleteSelectionPlugin=Delete Selection")
@PluginInfo(pluginType = PluginType.DELETE, tags = {PluginTags.DELETE})
public class DeleteSelectionPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        // Delete vertices first. This will implicitly delete their transactions, including any selected ones,
        // which means less work below.
        int vxSelected = VisualConcept.VertexAttribute.SELECTED.get(graph);

        if (vxSelected != Graph.NOT_FOUND) {
            int vertexCount = graph.getVertexCount();
            int[] deletedVertices = new int[vertexCount];
            int deletedVertexCount = 0;

            for (int position = 0; position < vertexCount; position++) {
                int vertex = graph.getVertex(position);
                if (graph.getBooleanValue(vxSelected, vertex)) {
                    deletedVertices[deletedVertexCount++] = vertex;
                }
            }

            ConstellationLoggerHelper.deletePropertyBuilder(
                    this,
                    GraphRecordStoreUtilities.getSelectedVertices(graph).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                    ConstellationLoggerHelper.SUCCESS
            );

            while (deletedVertexCount > 0) {
                graph.removeVertex(deletedVertices[--deletedVertexCount]);
            }
        }

        int txSelected = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        if (txSelected != Graph.NOT_FOUND) {
            int transactionCount = graph.getTransactionCount();
            int[] deletedTransactions = new int[transactionCount];
            int deletedTransactionCount = 0;

            for (int position = 0; position < transactionCount; position++) {
                int transaction = graph.getTransaction(position);
                if (graph.getBooleanValue(txSelected, transaction)) {
                    deletedTransactions[deletedTransactionCount++] = transaction;
                }
            }

            while (deletedTransactionCount > 0) {
                graph.removeTransaction(deletedTransactions[--deletedTransactionCount]);
            }
        }

        if (graph.getSchema() != null) {
            graph.getSchema().completeGraph(graph);
        }
    }
}
