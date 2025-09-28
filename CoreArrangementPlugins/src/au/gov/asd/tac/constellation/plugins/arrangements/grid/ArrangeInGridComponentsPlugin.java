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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Arrange components in grids.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@Messages("ArrangeInGridComponentsPlugin=Arrange Components in Grid")
@PluginInfo(minLogInterval = 5000, pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInGridComponentsPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Arranging...", true);

        if (graph.getVertexCount() > 0) {
            final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
            radiusSetter.setRadii();

            final GridChoiceParameters innerGcParams = GridChoiceParameters.getDefaultParameters();
            final Arranger inner = new GridArranger(innerGcParams);
            final GridChoiceParameters outerGcParams = GridChoiceParameters.getDefaultParameters();
            outerGcParams.setRowOffsets(false);
            final Arranger outer = new GridArranger(outerGcParams);

            final GraphTaxonomyArranger arranger = new GraphComponentArranger(inner, outer, Connections.LINKS);
            arranger.setSingletonArranger(new GridArranger(innerGcParams));
            arranger.setDoubletArranger(new GridArranger(innerGcParams, true));
            arranger.setInteraction(interaction);

            arranger.setMaintainMean(true);

            // We need to include links to dicover the components.
            final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, Connections.LINKS);
            arranger.arrange(selectedGraph.getInclusionGraph());
            selectedGraph.retrieveCoords();
        }

        interaction.setProgress(1, 0, "Finished", true);
    }
}
