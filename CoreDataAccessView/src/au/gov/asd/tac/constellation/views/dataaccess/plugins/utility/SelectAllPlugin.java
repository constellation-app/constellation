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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Select everything on the graph.
 *
 * @author cygnus_x-1
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@Messages("SelectAllPlugin=Select All")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectAllPlugin extends SimpleQueryPlugin implements DataAccessPlugin {

    private static final String ATTRIBUTE_ERROR = "Select All could not successfully complete because it does not contain the %s.";

    @Override
    public String getType() {
        return DataAccessPluginCoreType.UTILITY;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "Select everything in your graph";
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        
        // Local process-tracking varables (Process is indeteminate until quantity of merged nodes is known)
        int currentProcessStep = 0;
        int totalProcessSteps = -1; 
        interaction.setProgress(currentProcessStep, totalProcessSteps, "Selecting all...", true);
        
        // Retrieve attribute IDs for graph element "Selected" attribute
        final int selectedVertexID = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int selectedTransactionID = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        // Throw errors if elementID's are not found
        if (selectedVertexID == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(ATTRIBUTE_ERROR, "Select Vertex Attribute: 'Selected'"));
        }
        if (selectedTransactionID == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(ATTRIBUTE_ERROR, "Select Transaction Attribute: 'Selected'"));
        } 
        
        // Determine how many elements are to be selected
        final int vertexCount = graph.getVertexCount();
        final int transactionCount = graph.getTransactionCount();
        totalProcessSteps = vertexCount + transactionCount;
        
        // Select all Vertexs
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            graph.setBooleanValue(selectedVertexID, graph.getVertex(vertexPosition), true);
            interaction.setProgress(++currentProcessStep, totalProcessSteps, true);
        }

        // Select all Transactions
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            graph.setBooleanValue(selectedTransactionID, graph.getTransaction(transactionPosition), true);
            currentProcessStep++;
            interaction.setProgress(currentProcessStep, totalProcessSteps, true);
        }
        
        // Set process to complete
        totalProcessSteps = 0;
        interaction.setProgress(
                currentProcessStep, 
                totalProcessSteps, 
                String.format("Selected %s & %s.", 
                        PluginReportUtilities.getNodeCountString(vertexCount),
                        PluginReportUtilities.getTransactionCountString(transactionCount)
                ),
                true);
    }
}
