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
package au.gov.asd.tac.constellation.graph.visual.plugins.select.structure;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.select.structure.SelectOneHopInducedSubgraphAction")
@ActionRegistration(displayName = "#CTL_SelectOneHopInducedSubgraphAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/select/structure/resources/one_hop_induced_subgraph.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1400),
    @ActionReference(path = "Toolbars/Selection", position = 700)
})
@NbBundle.Messages("CTL_SelectOneHopInducedSubgraphAction=Select One Hop Induced Subgraph")
public class SelectOneHopInducedSubgraphAction extends SimplePluginAction {

    public SelectOneHopInducedSubgraphAction(final GraphNode context) {
        super(context, VisualGraphPluginRegistry.SELECT_INDUCED_ONE_HOP);
    }

}
