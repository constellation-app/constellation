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
 * Complete Graph Builder action.
 *
 * @author canis_majoris
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_CompleteGraphBuilderAction")
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Build Graph", position = 1000)
})
@Messages("CTL_CompleteGraphBuilderAction=Complete Graph Builder")
public final class CompleteGraphBuilderAction implements ActionListener {

    private final GraphNode context;

    public CompleteGraphBuilderAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final List<String> atypes = Arrays.asList("Unknown");
        final MultiChoiceParameterValue types = new MultiChoiceParameterType.MultiChoiceParameterValue();
        types.setChoices(atypes);
        PluginExecution.withPlugin(CoreTestingPluginRegistry.COMPLETE_GRAPH_BUILDER)
                .withParameter(CompleteGraphBuilderPlugin.N_PARAMETER_ID, 5)
                .withParameter(CompleteGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID, true)
                .withParameter(CompleteGraphBuilderPlugin.NODE_TYPES_PARAMETER_ID, types)
                .withParameter(CompleteGraphBuilderPlugin.TRANSACTION_TYPES_PARAMETER_ID, types)
                .interactively(true)
                .executeLater(context.getGraph());
    }
}
