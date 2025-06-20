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
package au.gov.asd.tac.constellation.graph.schema.visual.plugins;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Completes the selection on the graph with its schema.
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("CompleteSchemaSelectionPlugin=Complete Graph Selection Plugin")
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
public class CompleteSchemaSelectionPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        if (graph.getSchema() != null) {
            // Retrieve graph details
            final int vertexCount = graph.getVertexCount();
            final int transactionCount = graph.getTransactionCount();

            // Retrieve colorblind preferences 
            final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
            final String colorMode = prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT);

            // Local process-tracking variables (Process is indeterminate until node and transaction quantity is needed.)
            int currentProgress = 0;
            int maxProgress = -1;
            interaction.setProgress(currentProgress, maxProgress, "Completing schema...", true, parameters);
            
            final int vxColorblindAttr = VisualConcept.VertexAttribute.COLORBLIND_LAYER.ensure(graph);
            final int txColorblindAttr = VisualConcept.TransactionAttribute.COLORBLIND_LAYER.ensure(graph);
            
            final int vxSelectedAttr = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
            final int txSelectedAttr = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
            
            int selectedVertexCount = 0;
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                if (graph.getBooleanValue(vxSelectedAttr, vertexId)) {
                    selectedVertexCount++;
                }
            }       

            // Process Vertices
            maxProgress = selectedVertexCount;
            interaction.setProgress(currentProgress,
                    maxProgress,
                    String.format("Completing %s.",
                            PluginReportUtilities.getNodeCountString(selectedVertexCount)
                    ),
                    true
            );
            
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = graph.getVertex(vertexPosition);
                if (graph.getBooleanValue(vxSelectedAttr, vertexId)) {
                    graph.getSchema().completeVertex(graph, vertexId);
                }
                
                interaction.setProgress(++currentProgress, maxProgress, true);
            }

            int selectedTransactionCount = 0;
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int transactionId = graph.getTransaction(transactionPosition);
                if (graph.getBooleanValue(txSelectedAttr, transactionId)) {
                    selectedTransactionCount++;
                }
            }
            
            // Process Transactions
            maxProgress = selectedTransactionCount;
            currentProgress = 0;
            interaction.setProgress(currentProgress,
                    maxProgress,
                    String.format("Completing %s.",
                            PluginReportUtilities.getTransactionCountString(selectedTransactionCount)
                    ),
                    true
            );
            
            interaction.setProgress(currentProgress, maxProgress, "Completing transaction(s)...", true);
            
            for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
                final int transactionId = graph.getTransaction(transactionPosition);
                if (graph.getBooleanValue(txSelectedAttr, transactionId)) {
                    graph.getSchema().completeTransaction(graph, transactionId);
                }
                interaction.setProgress(++currentProgress, maxProgress, true);
            }

            // Set process to complete
            maxProgress = 0;
            interaction.setProgress(currentProgress,
                    maxProgress,
                    String.format("Completed %s & %s.",
                            PluginReportUtilities.getNodeCountString(vertexCount),
                            PluginReportUtilities.getTransactionCountString(transactionCount)
                    ),
                    true
            );
             
            if (!"None".equals(colorMode)) {
                final int vxColorRefAttribute = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.ensure(graph);
                final int txColorRefAttribute = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.ensure(graph);
                graph.setStringValue(vxColorRefAttribute, 0, graph.getAttributeName(vxColorblindAttr));
                graph.setStringValue(txColorRefAttribute, 0, graph.getAttributeName(txColorblindAttr));
            } else {
                graph.removeAttribute(vxColorblindAttr);
                graph.removeAttribute(txColorblindAttr);
            }

        }
    }
}
