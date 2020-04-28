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
package au.gov.asd.tac.constellation.graph.schema.visual.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Completes the graph with its schema.
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("CompleteSchemaPlugin=Complete Graph Plugin")
public class CompleteSchemaPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        if (graph.getSchema() != null) {
            int currentProgress = 0;
            int maxProgress = graph.getVertexCount() + graph.getTransactionCount();

            // vertices
            for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                graph.getSchema().completeVertex(graph, vertexId);
                currentProgress++;
                interaction.setProgress(currentProgress, maxProgress, "Completing schema...", true);
            }

            // transactions
            for (int transactionPosition = 0; transactionPosition < graph.getTransactionCount(); transactionPosition++) {
                final int transactionId = graph.getTransaction(transactionPosition);

                graph.getSchema().completeTransaction(graph, transactionId);
                currentProgress++;
                interaction.setProgress(currentProgress, maxProgress, "Completing schema...", true);
            }

            // graph
            graph.getSchema().completeGraph(graph);
        }
    }
}
