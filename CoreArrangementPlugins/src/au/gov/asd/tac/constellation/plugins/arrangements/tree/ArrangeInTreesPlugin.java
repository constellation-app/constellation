/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.arrangements.AbstractInclusionGraph.Connections;
import au.gov.asd.tac.constellation.plugins.arrangements.Arranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphComponentArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.GraphTaxonomyArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.SelectedInclusionGraph;
import au.gov.asd.tac.constellation.plugins.arrangements.SetRadiusForArrangement;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridArranger;
import au.gov.asd.tac.constellation.plugins.arrangements.grid.GridChoiceParameters;
import au.gov.asd.tac.constellation.plugins.arrangements.uncollide.UncollideArrangement;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Arrange the graph in a tree like manner.
 *
 * @author algol
 * @author sol
 */
@ServiceProvider(service = Plugin.class)
@Messages("ArrangeInTreesPlugin=Arrange in Trees")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInTreesPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Arranging...", true);

        if (graph.getVertexCount() > 0) {
            final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
            radiusSetter.setRadii();

            final Arranger inner = new CircTreeArranger(CircTreeChoiceParameters.getDefaultParameters());

            final Arranger middle = new MdsArranger(MDSChoiceParameters.getDefaultParameters());

            final GridChoiceParameters outerGcParams = GridChoiceParameters.getDefaultParameters();
            outerGcParams.setRowOffsets(false);
            final Arranger outer = new GridArranger(outerGcParams);

            final GridChoiceParameters innerGcParams = GridChoiceParameters.getDefaultParameters();
            final GraphTaxonomyArranger arranger2 = new TreeTaxonArranger(inner, middle);
            arranger2.setInteraction(interaction);

            // Push the MDS parts further away from each other.
            final UncollideArrangement unc = new UncollideArrangement(2);
            unc.setMinPadding(4);
            arranger2.setUncollider(unc);

            final GraphTaxonomyArranger arranger1 = new GraphComponentArranger(arranger2, outer, Connections.LINKS);
            arranger1.setSingletonArranger(new GridArranger(innerGcParams));
            arranger1.setDoubletArranger(new GridArranger(innerGcParams, true));
            arranger1.setInteraction(interaction);

            final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, SelectedInclusionGraph.Connections.LINKS);
            final boolean maintainMean = !selectedGraph.isArrangingAll();
            arranger1.setMaintainMean(maintainMean);
            arranger1.arrange(selectedGraph.getInclusionGraph());

            selectedGraph.retrieveCoords();
        }

        interaction.setProgress(1, 0, "Finished", true);
    }
}
