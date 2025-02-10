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
package au.gov.asd.tac.constellation.graph.interaction.plugins.composite;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.CompositeUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.utilities.VertexDominanceCalculator;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = Plugin.class)
@Messages("CreateCompositesFromDominantNodesPlugin=Composite Correlated Nodes")
@PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
public class CreateCompositesFromDominantNodesPlugin extends SimpleEditPlugin {

    private void findCorrelations(final GraphReadMethods graph, final int vxTypeAttr, final int txTypeAttr, final int vxId, final Set<Integer> allCorrelatedVerts, final Set<Integer> correlationGroup) {
        for (int i = 0; i < graph.getVertexNeighbourCount(vxId); i++) {
            final int nxId = graph.getVertexNeighbour(vxId, i);

            if (!allCorrelatedVerts.contains(nxId)) {
                final int lxId = graph.getLink(vxId, nxId);
                boolean neighbourCorrelated = false;
                for (int j = 0; j < graph.getLinkTransactionCount(lxId); j++) {
                    final int txId = graph.getLinkTransaction(lxId, j);
                    final SchemaTransactionType txType = (SchemaTransactionType) graph.getObjectValue(txTypeAttr, txId);
                    if (txType != null && txType.isSubTypeOf(AnalyticConcept.TransactionType.CORRELATION)) {
                        correlationGroup.add(nxId);
                        allCorrelatedVerts.add(nxId);
                        neighbourCorrelated = true;
                        break;
                    }
                }

                if (neighbourCorrelated) {
                    findCorrelations(graph, vxTypeAttr, txTypeAttr, nxId, allCorrelatedVerts, correlationGroup);
                }
            }
        }

    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        @SuppressWarnings("unchecked") //the default VertexDominanceCalculator extends VertexDominanceCalculator<SchemaVertexType>
        final Comparator<SchemaVertexType> comparator = VertexDominanceCalculator.getDefault().getComparator();
        final int vxTypeAttr = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        final int txTypeAttr = AnalyticConcept.TransactionAttribute.TYPE.get(graph);

        if (vxTypeAttr != Graph.NOT_FOUND && txTypeAttr != Graph.NOT_FOUND) {
            final Set<Integer> allCorrelatedVerts = new HashSet<>();
            boolean correlationsFound = true;
            while (correlationsFound) {
                correlationsFound = false;
                for (int i = 0; i < graph.getVertexCount(); i++) {
                    final int vxId = graph.getVertex(i);
                    if (!allCorrelatedVerts.contains(vxId)) {
                        allCorrelatedVerts.add(vxId);
                        final Set<Integer> correlationGroup = new HashSet<>();
                        correlationGroup.add(vxId);
                        findCorrelations(graph, vxTypeAttr, txTypeAttr, vxId, allCorrelatedVerts, correlationGroup);

                        if (correlationGroup.size() > 1) {
                            final Optional<SchemaVertexType> leadType = correlationGroup.stream().map(v -> (SchemaVertexType) graph.getObjectValue(vxTypeAttr, v)).sorted(comparator).findFirst();
                            if (leadType.isPresent()) {
                                Optional<Integer> leadVertex = correlationGroup.stream().filter(v -> leadType.get().equals((SchemaVertexType) graph.getObjectValue(vxTypeAttr, v))).findFirst();
                                if (leadVertex.isPresent()) {
                                    final int leadVertexId = leadVertex.get();
                                    Set<Integer> comprisingIds = correlationGroup.stream().collect(Collectors.toSet());
                                    CompositeUtilities.makeComposite(graph, comprisingIds, leadVertexId);
                                    correlationsFound = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            PluginExecution.withPlugin(VisualSchemaPluginRegistry.COMPLETE_SCHEMA).executeNow(graph);
        }
    }
}
