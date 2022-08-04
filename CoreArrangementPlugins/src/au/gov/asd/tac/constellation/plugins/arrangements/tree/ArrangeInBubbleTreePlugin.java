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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("ArrangeInBubbleTreePlugin=Arrange in Bubble Trees")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInBubbleTreePlugin extends SimpleEditPlugin {

    public static final String ROOTS_PARAMETER_ID = PluginParameter.buildId(ArrangeInBubbleTreePlugin.class, "roots");
    public static final String IS_MINIMAL_PARAMETER_ID = PluginParameter.buildId(ArrangeInBubbleTreePlugin.class, "is_minimal");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> roots = ObjectParameterType.build(ROOTS_PARAMETER_ID);
        roots.setName("Roots");
        roots.setDescription("A set of root vertex ids");
        parameters.addParameter(roots);

        final PluginParameter<BooleanParameterValue> isMinimal = BooleanParameterType.build(IS_MINIMAL_PARAMETER_ID);
        isMinimal.setName("Is Minimal");
        isMinimal.setDescription("True for a minimal spanning tree, False for a maximal spanning tree");
        parameters.addParameter(isMinimal);

        return parameters;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        // Get the prospective root vertex ids.
        @SuppressWarnings("unchecked") //roots will be a set of integers which extends object type
        final Set<Integer> roots = (Set<Integer>) parameters.getParameters().get(ROOTS_PARAMETER_ID).getObjectValue();
        final boolean isMinimal = parameters.getParameters().get(IS_MINIMAL_PARAMETER_ID).getBooleanValue();

        if (graph.getVertexCount() > 0) {
            final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
            radiusSetter.setRadii();

            final GridChoiceParameters innerGcParams = GridChoiceParameters.getDefaultParameters();
            final Arranger inner = new BubbleTreeArranger(roots, isMinimal);
            final GridChoiceParameters outerGcParams = GridChoiceParameters.getDefaultParameters();
            outerGcParams.setRowOffsets(false);
            final Arranger outer = new GridArranger(outerGcParams);

            final GraphTaxonomyArranger arranger = new GraphComponentArranger(inner, outer, Connections.LINKS);
            arranger.setSingletonArranger(new GridArranger(innerGcParams));
            arranger.setDoubletArranger(new GridArranger(innerGcParams, true));
            arranger.setInteraction(interaction);

            arranger.setMaintainMean(true);

            // We need to include links to discover the components.
            final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, Connections.LINKS);

            arranger.arrange(selectedGraph.getInclusionGraph());
            selectedGraph.retrieveCoords();
        }
    }
}
