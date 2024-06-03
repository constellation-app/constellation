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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.testing.CoreTestingPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Preferential Attachment Graph Builder action.
 *
 * @author canis_majoris
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.construction.PreferentialAttachmentGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_PreferentialAttachmentGraphBuilderAction")
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Build Graph", position = 1001)
})
@Messages("CTL_PreferentialAttachmentGraphBuilderAction=Preferential Attachment Graph Builder")
public final class PreferentialAttachmentGraphBuilderAction implements ActionListener {

    private final GraphNode context;

    public PreferentialAttachmentGraphBuilderAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final List<String> atypes = Arrays.asList("Unknown");
        final MultiChoiceParameterValue types = new MultiChoiceParameterType.MultiChoiceParameterValue();
        types.setChoices(atypes);
        PluginExecution.withPlugin(CoreTestingPluginRegistry.PREFERENTIAL_ATTACHMENT_GRAPH_BUILDER)
                .withParameter(PreferentialAttachmentGraphBuilderPlugin.N_PARAMETER_ID, 10)
                .withParameter(PreferentialAttachmentGraphBuilderPlugin.M_PARAMETER_ID, 1)
                .withParameter(PreferentialAttachmentGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID, true)
                .withParameter(PreferentialAttachmentGraphBuilderPlugin.NODE_TYPES_PARAMETER_ID, types)
                .withParameter(PreferentialAttachmentGraphBuilderPlugin.TRANSACTION_TYPES_PARAMETER_ID, types)
                .interactively(true)
                .executeLater(context.getGraph());
    }
}
