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
package au.gov.asd.tac.constellation.graph.interaction.plugins.composite;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action for creating a composite node from the currently selected nodes. It is
 * accessed from the tools menu.
 *
 * @see MakeCompositeFromSelectionPlugin
 * @author twilight_sparkle
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.functionality.composite.CreateCompositeFromSelectionAction")
@ActionRegistration(displayName = "#CTL_CreateCompositeFromSelectionAction",
        iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/composite/compositeSelectedNodes.png")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 700)
})
@Messages("CTL_CreateCompositeFromSelectionAction=Composite Selected Nodes")
public class CreateCompositeFromSelectionAction extends SimplePluginAction {

    public CreateCompositeFromSelectionAction(final GraphNode context) {
        super(context, InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_SELECTION);
    }
}
