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
package au.gov.asd.tac.constellation.plugins.arrangements.hierarchical;

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
@NbBundle.Messages("ArrangeInHierarchyPlugin=Arrange in Hierarchy")
@PluginInfo(pluginType = PluginType.DISPLAY, tags = {PluginTags.MODIFY})
public class ArrangeInHierarchyPlugin extends SimpleEditPlugin {

    public static final String ROOTS_PARAMETER_ID = PluginParameter.buildId(ArrangeInHierarchyPlugin.class, "roots");

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        @SuppressWarnings("unchecked") //roots will be a set of integers, which extends object type
        final Set<Integer> roots = (Set<Integer>) parameters.getParameters().get(ROOTS_PARAMETER_ID).getObjectValue();

        if (graph.getVertexCount() > 0) {
            final SetRadiusForArrangement radiusSetter = new SetRadiusForArrangement(graph);
            radiusSetter.setRadii();

            final GridChoiceParameters innerGcParams = GridChoiceParameters.getDefaultParameters();
            final HierarchicalArranger inner = new HierarchicalArranger(roots);
            HierarchicalArranger.setInteraction(interaction);
            final GridChoiceParameters outerGcParams = GridChoiceParameters.getDefaultParameters();
            outerGcParams.setRowOffsets(false);
            final Arranger outer = new GridArranger(outerGcParams);

            final GraphTaxonomyArranger arranger = new GraphComponentArranger(inner, outer, Connections.LINKS);
            arranger.setSingletonArranger(new GridArranger(innerGcParams));
            arranger.setDoubletArranger(new GridArranger(innerGcParams, true));
            arranger.setInteraction(interaction);

            arranger.setMaintainMean(true);

            final SelectedInclusionGraph selectedGraph = new SelectedInclusionGraph(graph, Connections.LINKS);

            arranger.arrange(selectedGraph.getInclusionGraph());
            selectedGraph.retrieveCoords();
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> roots = ObjectParameterType.build(ROOTS_PARAMETER_ID);
        roots.setName("The root nodes");
        roots.setDescription("A list of the root vertex ids");
        parameters.addParameter(roots);

        return parameters;
    }
}
