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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.testing.CoreTestingPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Small World Graph Builder action.
 *
 * @author canis_majoris
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.construction.SmallWorldGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_SmallWorldGraphBuilderAction")
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Build Graph", position = 1002)
})
@Messages("CTL_SmallWorldGraphBuilderAction=Small World Graph Builder")
public final class SmallWorldGraphBuilderAction implements ActionListener {

    private final GraphNode context;

    public SmallWorldGraphBuilderAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final List<String> atypes = Arrays.asList("Unknown");
        final MultiChoiceParameterValue types = new MultiChoiceParameterType.MultiChoiceParameterValue();
        types.setChoices(atypes);

        PluginExecution.withPlugin(CoreTestingPluginRegistry.SMALL_WORLD_GRAPH_BUILDER)
                .withParameter(SmallWorldGraphBuilderPlugin.N_PARAMETER_ID, 10)
                .withParameter(SmallWorldGraphBuilderPlugin.K_PARAMETER_ID, 4)
                .withParameter(SmallWorldGraphBuilderPlugin.P_PARAMETER_ID, 0.5F)
                .withParameter(SmallWorldGraphBuilderPlugin.T_PARAMETER_ID, 100)
                .withParameter(SmallWorldGraphBuilderPlugin.BUILD_MODE_PARAMETER_ID, "Default")
                .withParameter(SmallWorldGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID, true)
                .withParameter(SmallWorldGraphBuilderPlugin.NODE_TYPES_PARAMETER_ID, types)
                .withParameter(SmallWorldGraphBuilderPlugin.TRANSACTION_TYPES_PARAMETER_ID, types)
                .interactively(true)
                .executeLater(context.getGraph());
    }
}
