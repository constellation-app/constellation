/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * plugin framework for backbone service
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("SelectBackbonePlugin=Add to Selection: Backbone")
public class SelectBackbonePlugin extends SimpleEditPlugin {

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
        final ArrayList<Integer> selected_nodes = new ArrayList<>();

        final int selectedNodeAttrId = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int selectedTransactionAttrId = VisualConcept.TransactionAttribute.SELECTED.get(graph);

        for (int position = 0; position < graph.getVertexCount(); position++) {
            final int vxId = graph.getVertex(position);

            // identify all nodes with connections > 1
            final int degree = graph.getVertexLinkCount(vxId);
            if (degree > 1) {
                graph.setBooleanValue(selectedNodeAttrId, vxId, true);
            }
            if (degree > 1) {
                selected_nodes.add(vxId);
            }
        }

        // identify all transactions whose both nodes are selected
        for (int position = 0; position < graph.getTransactionCount(); position++) {
            final int txId = graph.getTransaction(position);
            if (selected_nodes.contains(graph.getTransactionDestinationVertex(txId)) && selected_nodes.contains(graph.getTransactionSourceVertex(txId))) {
                graph.setBooleanValue(selectedTransactionAttrId, txId, true);
            }

        }
    }
}
