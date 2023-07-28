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
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleQueryPlugin;
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
        interaction.setProgress(0, 0, "Selecting all...", true);

        final int selectedVertex = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (selectedVertex == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(ATTRIBUTE_ERROR, "Select Vertex Attribute: 'Selected'"));
        }
        final int vertexCount = graph.getVertexCount();
        for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
            final int vertex = graph.getVertex(vertexPosition);
            graph.setBooleanValue(selectedVertex, vertex, true);
        }

        final int selectedTransaction = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        if (selectedTransaction == Graph.NOT_FOUND) {
            throw new PluginException(PluginNotificationLevel.ERROR, String.format(ATTRIBUTE_ERROR, "Select Transaction Attribute: 'Selected'"));
        }
        final int transactionCount = graph.getTransactionCount();
        for (int transactionPosition = 0; transactionPosition < transactionCount; transactionPosition++) {
            final int transaction = graph.getTransaction(transactionPosition);
            graph.setBooleanValue(selectedTransaction, transaction, true);
        }

        interaction.setProgress(1, 0, "Selected " + vertexCount + " Nodes & " + transactionCount + " Edges.", true);
    }
}
