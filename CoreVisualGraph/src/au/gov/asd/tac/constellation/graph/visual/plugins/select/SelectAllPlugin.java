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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.SetBooleanValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLogger;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Properties;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Select all vertices and transactions.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.SELECTION, tags = {"SELECTION"})
@Messages("SelectAllPlugin=Add to Selection: All")
public class SelectAllPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        Properties properties = new Properties();

        int vxSelected = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (vxSelected != Graph.NOT_FOUND) {
            SetBooleanValuesOperation selectVerticesOperation = new SetBooleanValuesOperation(graph, GraphElementType.VERTEX, vxSelected);
            int vertexCount = graph.getVertexCount();
            for (int position = 0; position < vertexCount; position++) {
                int vertex = graph.getVertex(position);
                selectVerticesOperation.setValue(vertex, true);
            }
            graph.executeGraphOperation(selectVerticesOperation);

            properties.setProperty("vsize", String.valueOf(selectVerticesOperation.size()));
        }

        int txSelected = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        if (txSelected != Graph.NOT_FOUND) {
            SetBooleanValuesOperation selectTransactionsOperation = new SetBooleanValuesOperation(graph, GraphElementType.TRANSACTION, txSelected);
            int transactionCount = graph.getTransactionCount();
            for (int position = 0; position < transactionCount; position++) {
                int transaction = graph.getTransaction(position);
                selectTransactionsOperation.setValue(transaction, true);
            }
            graph.executeGraphOperation(selectTransactionsOperation);

            properties.setProperty("tsize", String.valueOf(selectTransactionsOperation.size()));
        }

        ConstellationLogger.getDefault().pluginProperties(this, properties);

    }
}
