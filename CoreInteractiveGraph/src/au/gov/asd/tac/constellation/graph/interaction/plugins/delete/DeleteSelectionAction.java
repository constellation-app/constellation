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
package au.gov.asd.tac.constellation.graph.interaction.plugins.delete;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * delete currently selected elements
 *
 * @author algol
 */
@ActionID(category = "Edit", id = "au.gov.asd.tac.constellation.functionality.delete.DeleteSelectionAction")
@ActionRegistration(displayName = "#CTL_DeleteSelectionAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/delete/resources/delete.png", surviveFocusChange = false)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 700),
    @ActionReference(path = "Shortcuts", name = "Delete")
})
@Messages("CTL_DeleteSelectionAction=Delete")
public final class DeleteSelectionAction extends SimplePluginAction {

    public DeleteSelectionAction(final GraphNode context) {
        super(context, InteractiveGraphPluginRegistry.DELETE_SELECTION);
    }
}
