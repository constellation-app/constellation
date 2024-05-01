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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin framework supporting the de-select all transactions in the graph
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("DeselectTransactionsPlugin=Remove from Selection: Transactions")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class DeselectTransactionsPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final int txSelectedAttrId = wg.getAttribute(GraphElementType.TRANSACTION, VisualConcept.TransactionAttribute.SELECTED.getName());
        if (txSelectedAttrId != Graph.NOT_FOUND) {
            final int txCount = wg.getTransactionCount();
            for (int position = 0; position < txCount; position++) {
                final int txId = wg.getTransaction(position);

                if (wg.getBooleanValue(txSelectedAttrId, txId)) {
                    wg.setBooleanValue(txSelectedAttrId, txId, false);
                }
            }
        }
    }
}
