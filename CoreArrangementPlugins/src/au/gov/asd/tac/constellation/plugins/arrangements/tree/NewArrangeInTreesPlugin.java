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
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphComponentArranger;
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
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Arrange the graph in a tree like manner.
 *
 * @author algol
 * @author sol
 * @author Quasar985
 */
@ServiceProvider(service = Plugin.class)
@Messages("NewArrangeInTreesPlugin=Arrange in Trees")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class NewArrangeInTreesPlugin extends SimpleEditPlugin {

    public static final String SPLIT_INTO_TREES_PARAMETER_ID = PluginParameter.buildId(NewArrangeInTreesPlugin.class, "split_into_trees");
    private static final String SPLIT_INTO_TREES_PARAMETER_ID_NAME = "Split into Trees";
    private static final String SPLIT_INTO_TREES_PARAMETER_ID_DESCRIPTION = "Arrange the whole graph, or first split into subgraphs";
    private static final boolean SPLIT_INTO_TREES_DEFAULT = true;

    public static final String SCALE_PARAMETER_ID = PluginParameter.buildId(NewArrangeInTreesPlugin.class, "scale");
    private static final String SCALE_PARAMETER_ID_NAME = "Distance Between Layers";
    private static final String SCALE_PARAMETER_ID_DESCRIPTION = "The distance between each layer of the arranged graph";
    private static final int SCALE_PARAMETER_ID_DEFAULT = 10;

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<BooleanParameterValue> splitIntoTreesParam = BooleanParameterType.build(SPLIT_INTO_TREES_PARAMETER_ID);
        splitIntoTreesParam.setName(SPLIT_INTO_TREES_PARAMETER_ID_NAME);
        splitIntoTreesParam.setDescription(SPLIT_INTO_TREES_PARAMETER_ID_DESCRIPTION);
        splitIntoTreesParam.setBooleanValue(SPLIT_INTO_TREES_DEFAULT);
        parameters.addParameter(splitIntoTreesParam);

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
            return;
        }

        // Params
        final boolean splitIntoTrees = parameters.getParameters().get(SPLIT_INTO_TREES_PARAMETER_ID).getBooleanValue();
        final int scale = parameters.getParameters().get(SCALE_PARAMETER_ID).getIntegerValue();

        final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
        radiusSetter.setRadii();

        final Arranger treeArranger = new NewTreeArranger(scale);

        final Arranger middle = new MdsArranger(new MDSChoiceParameters(LinkWeight.USE_EXTENTS, 1, 20, 8, 1, true, 10));

        final GraphTaxonomyArranger arranger2 = splitIntoTrees ? new TreeTaxonArranger(treeArranger, middle) : new GraphComponentArranger(treeArranger, middle, Connections.LINKS);
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

        interaction.setProgress(1, 0, "Finished", true);
    }

}
