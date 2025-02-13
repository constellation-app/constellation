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
 * Create Composites From Dominant Nodes Action
 *
 * @author twilight_sparkle
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.functionality.composite.CreateCompositesFromDominantNodesAction")
@ActionRegistration(displayName = "#CTL_CreateCompositesFromDominantNodesAction",
        iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/composite/compositeCorrelatedNodes.png")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 800)
})
@Messages("CTL_CreateCompositesFromDominantNodesAction=Composite Correlated Nodes")
public class CreateCompositesFromDominantNodesAction extends SimplePluginAction {

    public CreateCompositesFromDominantNodesAction(final GraphNode context) {
        super(context, InteractiveGraphPluginRegistry.CREATE_COMPOSITE_FROM_DOMINANT_NODES);
    }
}
