/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphComponentArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomy;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridChoiceParameters;
import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.UncollideArrangement;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.security.SecureRandom;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Arrange the graph in a tree like manner.
 *
 * @author algol
 * @author sol
 */
@ServiceProvider(service = Plugin.class)
@Messages("NewArrangeInTreesPlugin=Arrange in Trees")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class NewArrangeInTreesPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Arranging...", true);

        if (graph.getVertexCount() < 1) {
            interaction.setProgress(1, 0, "Finished", true);
        }

        // PARAMS
        final boolean splitIntoTrees = true;
        final boolean dimOrHideTrans = true;
        final boolean dimTrans = true;
        final boolean colorNodesByGroup = false;
        final float scale = 10F;

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
        radiusSetter.setRadii();

        final Arranger treeArranger = new NewTreeArranger(scale);
        final Arranger middle = new MdsArranger(MDSChoiceParameters.getDefaultParameters());

        final TreeTaxonArranger arranger2 = new TreeTaxonArranger(treeArranger, middle, splitIntoTrees);
        arranger2.setInteraction(interaction);

        // Push the MDS parts further away from each other.
        final UncollideArrangement unc = new UncollideArrangement(2);
        unc.setMinPadding(4);
        arranger2.setUncollider(unc);

        final GridChoiceParameters gridParams = GridChoiceParameters.getDefaultParameters();
        gridParams.setRowOffsets(false);
        final Arranger gridArranger = new GridArranger(gridParams);

        final GridChoiceParameters innerGcParams = GridChoiceParameters.getDefaultParameters();

        final GraphTaxonomyArranger arranger = new GraphComponentArranger(arranger2, gridArranger, AbstractInclusionGraph.Connections.LINKS);
        arranger.setSingletonArranger(new GridArranger(innerGcParams));
        arranger.setDoubletArranger(new GridArranger(innerGcParams, true));
        arranger.setInteraction(interaction);

        final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, SelectedInclusionGraph.Connections.LINKS);
        final boolean maintainMean = !selectedGraph.isArrangingAll();
        arranger.setMaintainMean(maintainMean);
        arranger.arrange(selectedGraph.getInclusionGraph());

        selectedGraph.retrieveCoords();

        if (colorNodesByGroup) {
            colourSubGraphs(graph, arranger2.getTreeTaxonomy(graph));
        }

        if (dimOrHideTrans) {
            dimOrHideTransactions(graph, arranger2.getTaxonomy(graph), dimTrans);
        }

        interaction.setProgress(1, 0, "Finished", true);
    }

    private void colourSubGraphs(final GraphWriteMethods graph, final GraphTaxonomy tax) {
        if (tax == null) {
            return;
        }

        final SecureRandom r = new SecureRandom();

        // TODO: change all this to 'ensure'
        if (VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, IconAttributeDescription.ATTRIBUTE_NAME, "background_icon", "background_icon", null, null);
        }
        final int bgiconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);

        if (VisualConcept.VertexAttribute.COLOR.get(graph) == Graph.NOT_FOUND) {
            graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, ColorAttributeDescription.ATTRIBUTE_NAME, ColorAttributeDescription.ATTRIBUTE_NAME, null, null);
        }
        final int colorAttr = VisualConcept.VertexAttribute.COLOR.get(graph);

        // Color the taxonomies so we can see what's going on.
        tax.getTaxa().forEachValue(subgraph -> {
            final ConstellationColor color = ConstellationColor.getColorValue(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1F);
            subgraph.forEach(vxId -> {
                graph.setStringValue(bgiconAttr, vxId, "Background.Round Circle");
                graph.setObjectValue(colorAttr, vxId, color);
            });
        });
    }

    private void dimOrHideTransactions(final GraphWriteMethods graph, final GraphTaxonomy taxonomy, final boolean dimTrans) {
        final MutableIntObjectMap<MutableIntSet> taxa = taxonomy.getTaxa();
        final int transactionDimmedAttribute = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);
        final int transactionVisibilityAttribute = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);

        // For each subgraph, check each node's neighbour
        for (final IntObjectPair<MutableIntSet> keyValue : taxa.keyValuesView()) {
            final MutableIntSet subGraphIds = keyValue.getTwo();
            System.out.println("Dim hide trans, keyValue.getTwo(): " + subGraphIds);
            // For each node
            final MutableIntIterator iterator = subGraphIds.intIterator();
            while (iterator.hasNext()) {
                final int vxId = iterator.next();

                final int neighbourCount = graph.getVertexNeighbourCount(vxId);
                if (neighbourCount < 1) {
                    continue;
                }

                // For each neighbour
                for (int i = 0; i < neighbourCount; i++) {
                    final int nxId = graph.getVertexNeighbour(vxId, i);
                    // If neighbour is not in subgraph, dim/hide
                    if (!subGraphIds.contains(nxId)) {
                        // This assumes that transaction position matches neighbour position
                        final int txId = graph.getVertexTransaction(vxId, i);
                        System.out.println("Dimming txId " + txId);

                        //final float visibiltyValue = !dimTrans ? -2.0F : 2.0F;
                        if (dimTrans) {
                            graph.setBooleanValue(transactionDimmedAttribute, txId, dimTrans);
                        } else {
                            graph.setFloatValue(transactionVisibilityAttribute, txId, -2.0F);
                        }
                    }
                }
            }
        }
    }
}
