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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Copy the current selected elements to a new graph
 */
@ActionID(category = "Edit", id = "au.gov.asd.tac.constellation.functionality.copypaste.CopyToNewGraphAction")
@ActionRegistration(displayName = "#CTL_CopyToNewGraphAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/clipboard/resources/copyGraph.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 600),
    @ActionReference(path = "Toolbars/File", position = 15),
    @ActionReference(path = "Shortcuts", name = "C-U")
})
@Messages("CTL_CopyToNewGraphAction=Copy to New Graph")
public final class CopyToNewGraphAction extends SimplePluginAction {

    public CopyToNewGraphAction(final GraphNode context) {
        super(context, InteractiveGraphPluginRegistry.COPY_TO_NEW_GRAPH);
    }
}
