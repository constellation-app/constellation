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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author betelgeuse
 */
@Messages("TimelineSelectionPlugin=Timeline: Update Selection")
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class TimelineSelectionPlugin extends SimpleEditPlugin {

    private final Set<Integer> vertices;
    private final Set<Integer> transactions;
    private final boolean isClearingSelection;
    private final boolean isDragSelection;

    public TimelineSelectionPlugin(final Set<Integer> vertices, final Set<Integer> transactions, final boolean isClearingSelection, final boolean isDragSelection) {
        this.vertices = vertices;
        this.transactions = transactions;
        this.isClearingSelection = isClearingSelection;
        this.isDragSelection = isDragSelection;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction,
            final PluginParameters parameters) throws InterruptedException {
        final int selectedVertexAttrID = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttrID = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        for (final int vxID : vertices) {
            graph.setBooleanValue(selectedVertexAttrID, vxID, true);
            vertices.remove(vxID);
        }
        for (final int txID : transactions) {
            graph.setBooleanValue(selectedTransactionAttrID, txID, true);
            transactions.remove(txID);
        }

        if (isClearingSelection) {
            for (int pos = 0; pos < graph.getVertexCount(); pos++) {
                final int vxID = graph.getVertex(pos);
                graph.setBooleanValue(selectedVertexAttrID, vxID, vertices.remove(vxID));
            }
            for (int pos = 0; pos < graph.getTransactionCount(); pos++) {
                final int txID = graph.getTransaction(pos);
                graph.setBooleanValue(selectedTransactionAttrID, txID, transactions.remove(txID));
            }
        } else if (isDragSelection) {
            for (int txID : transactions) {
                graph.setBooleanValue(selectedTransactionAttrID, txID, true);
            }
            for (int vxID : vertices) {
                graph.setBooleanValue(selectedVertexAttrID, vxID, true);
            }
        } else {
            final Set<Integer> toSelectVerts = new HashSet<>();
            final Set<Integer> toDeselectVerts = new HashSet<>();
            for (final int txID : transactions) {
                final boolean shouldSelect = !graph.getBooleanValue(selectedTransactionAttrID, txID);
                final Set<Integer> set = shouldSelect ? toSelectVerts : toDeselectVerts;
                graph.setBooleanValue(selectedTransactionAttrID, txID, shouldSelect);
                set.add(graph.getTransactionSourceVertex(txID));
                set.add(graph.getTransactionDestinationVertex(txID));
            }
            for (final int vxID : toSelectVerts) {
                graph.setBooleanValue(selectedVertexAttrID, vxID, true);
                toDeselectVerts.remove(vxID);
            }
            for (final int vxID : toDeselectVerts) {
                boolean shouldDeselect = true;
                for (int i = 0; i < graph.getVertexTransactionCount(vxID); i++) {
                    final int txID = graph.getVertexTransaction(vxID, i);
                    if (graph.getBooleanValue(selectedTransactionAttrID, txID)) {
                        shouldDeselect = false;
                    }
                }
                if (shouldDeselect) {
                    graph.setBooleanValue(selectedVertexAttrID, vxID, false);
                }
            }
        }
    }
}
