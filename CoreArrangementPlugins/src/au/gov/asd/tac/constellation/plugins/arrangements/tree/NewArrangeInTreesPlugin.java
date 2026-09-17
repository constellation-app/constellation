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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
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

    public static final String SPLIT_INTO_TREES_PARAMETER_ID = PluginParameter.buildId(NewArrangeInTreesPlugin.class, "split_into_trees");
    private static final String SPLIT_INTO_TREES_PARAMETER_ID_NAME = "Split into Trees";
    private static final String SPLIT_INTO_TREES_PARAMETER_ID_DESCRIPTION = "Arrange the whole graph, or first split into subgraphs";
    private static final boolean SPLIT_INTO_TREES_DEFAULT = false;

    public static final String DIM_OR_HIDE_PARAMETER_ID = PluginParameter.buildId(NewArrangeInTreesPlugin.class, "dim_or_hide");
    private static final String DIM_OR_HIDE_PARAMETER_ID_NAME = "Dim or Hide Cross-subgraph transactions";
    private static final String DIM_OR_HIDE_PARAMETER_ID_DESCRIPTION = "What to do with transactions that connect subgraphs";
    private static final String NOTHING = "Nothing";
    private static final String DIM = "Dim";
    private static final List<String> DIM_OR_HIDE_PARAM_VALUES = Arrays.asList(NOTHING, DIM, "Hide");
    private static final String DIM_OR_HIDE_PARAMETER_ID_DEFAULT = NOTHING;

    public static final String COLOUR_SUBGRAPHS_PARAMETER_ID = PluginParameter.buildId(NewArrangeInTreesPlugin.class, "colour_subgraphs");
    private static final String COLOUR_SUBGRAPHS_PARAMETER_ID_NAME = "Colour Subgraphs";
    private static final String COLOUR_SUBGRAPHS_PARAMETER_ID_DESCRIPTION = "Give each subgraph a unique colour";
    private static final boolean COLOUR_SUBGRAPHS_DEFAULT = false;

    public static final String SCALE_PARAMETER_ID = PluginParameter.buildId(NewArrangeInTreesPlugin.class, "scale");
    private static final String SCALE_PARAMETER_ID_NAME = "Distance Between Layers";
    private static final String SCALE_PARAMETER_ID_DESCRIPTION = "The distance between each layer of the arranged graph";
    private static final int SCALE_PARAMETER_ID_DEFAULT = 10;

    private PluginParameter<SingleChoiceParameterValue> dimOrHideParam;

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> splitIntoTreesParam = BooleanParameterType.build(SPLIT_INTO_TREES_PARAMETER_ID);
        splitIntoTreesParam.setName(SPLIT_INTO_TREES_PARAMETER_ID_NAME);
        splitIntoTreesParam.setDescription(SPLIT_INTO_TREES_PARAMETER_ID_DESCRIPTION);
        splitIntoTreesParam.setBooleanValue(SPLIT_INTO_TREES_DEFAULT);
        splitIntoTreesParam.addListener((oldValue, newValue) -> dimOrHideParam.setEnabled(splitIntoTreesParam.getBooleanValue()));
        parameters.addParameter(splitIntoTreesParam);

        dimOrHideParam = SingleChoiceParameterType.build(DIM_OR_HIDE_PARAMETER_ID);
        dimOrHideParam.setName(DIM_OR_HIDE_PARAMETER_ID_NAME);
        dimOrHideParam.setDescription(DIM_OR_HIDE_PARAMETER_ID_DESCRIPTION);
        SingleChoiceParameterType.setOptions(dimOrHideParam, DIM_OR_HIDE_PARAM_VALUES);
        SingleChoiceParameterType.setChoice(dimOrHideParam, DIM_OR_HIDE_PARAMETER_ID_DEFAULT);
        dimOrHideParam.setEnabled(SPLIT_INTO_TREES_DEFAULT); // Conditional on splitIntoTreesParam being checked
        parameters.addParameter(dimOrHideParam);

        final PluginParameter<BooleanParameterValue> colourSubgraphsParam = BooleanParameterType.build(COLOUR_SUBGRAPHS_PARAMETER_ID);
        colourSubgraphsParam.setName(COLOUR_SUBGRAPHS_PARAMETER_ID_NAME);
        colourSubgraphsParam.setDescription(COLOUR_SUBGRAPHS_PARAMETER_ID_DESCRIPTION);
        colourSubgraphsParam.setBooleanValue(COLOUR_SUBGRAPHS_DEFAULT);
        parameters.addParameter(colourSubgraphsParam);

        final PluginParameter<IntegerParameterType.IntegerParameterValue> scaleParam = IntegerParameterType.build(SCALE_PARAMETER_ID);
        scaleParam.setName(SCALE_PARAMETER_ID_NAME);
        scaleParam.setDescription(SCALE_PARAMETER_ID_DESCRIPTION);
        scaleParam.setIntegerValue(SCALE_PARAMETER_ID_DEFAULT);
        IntegerParameterType.setMinimum(scaleParam, 1);
        parameters.addParameter(scaleParam);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Arranging...", true);

        if (graph.getVertexCount() < 1) {
            interaction.setProgress(1, 0, "Finished", true);
        }

        // Params
        final boolean splitIntoTrees = parameters.getParameters().get(SPLIT_INTO_TREES_PARAMETER_ID).getBooleanValue();
        final String dimHideChoice = parameters.getParameters().get(DIM_OR_HIDE_PARAMETER_ID).getStringValue();
        final boolean colorNodesByGroup = parameters.getParameters().get(COLOUR_SUBGRAPHS_PARAMETER_ID).getBooleanValue();
        final int scale = parameters.getParameters().get(SCALE_PARAMETER_ID).getIntegerValue();

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

        if (!NOTHING.equals(dimHideChoice)) {
            dimOrHideTransactions(graph, arranger2.getTaxonomy(graph), DIM.equals(dimHideChoice));
        }

        interaction.setProgress(1, 0, "Finished", true);
    }

    private void colourSubGraphs(final GraphWriteMethods graph, final GraphTaxonomy tax) {
        if (tax == null) {
            return;
        }

        final SecureRandom r = new SecureRandom();
        final int bgiconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int colorAttr = VisualConcept.VertexAttribute.COLOR.ensure(graph);

        // Color each subgraph
        tax.getTaxa().forEachValue(subgraph -> {
            final ConstellationColor color = ConstellationColor.getColorValue(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1F);
            subgraph.forEach(vxId -> {
                graph.setStringValue(bgiconAttr, vxId, "Background.Round Circle");
                graph.setObjectValue(colorAttr, vxId, color);
            });
        });
    }

    /**
     * Function that finds all transactions between sub graphs defined in the taxonomy, and then either dims or hides
     * them
     *
     */
    private void dimOrHideTransactions(final GraphWriteMethods graph, final GraphTaxonomy taxonomy, final boolean dimTrans) {
        final MutableIntObjectMap<MutableIntSet> taxa = taxonomy.getTaxa();
        final int transactionDimmedAttribute = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);
        final int transactionVisibilityAttribute = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);

        // For each subgraph, check each node's neighbour
        for (final IntObjectPair<MutableIntSet> keyValue : taxa.keyValuesView()) {
            final MutableIntSet subGraphIds = keyValue.getTwo();
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
                    if (subGraphIds.contains(nxId)) {
                        continue;
                    }

                    // If neighbour is not in subgraph, dim/hide
                    final int txId = graph.getVertexTransaction(vxId, i); // This assumes that transaction position matches neighbour position
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
