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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin framework supporting loops
 *
 * @author aquila
 */
@ServiceProvider(service = Plugin.class)
@Messages("SelectLoopsPlugin=Add to Selection: Loops")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectLoopsPlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        int txSelected = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        if (txSelected != Graph.NOT_FOUND) {
            int transactionCount = graph.getTransactionCount();
            /*
             * All transactions are listed in a table, loop through all of them
             * on the graph and select those that have the same source and
             * destination
             */
            for (int position = 0; position < transactionCount; position++) {
                int transaction = graph.getTransaction(position);
                //Find out what the source and destination of each transaction is
                int sourceVertex = graph.getTransactionSourceVertex(transaction);
                int destinationVertex = graph.getTransactionDestinationVertex(transaction);
                //Set "Selected" as true if source == destination
                if (sourceVertex == destinationVertex) {
                    graph.setBooleanValue(txSelected, transaction, true);
                }
            }
        }
    }
}
