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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Invert the current selection.
 * <p>
 * Selected vertices/transactions become unselected and selected
 * transactions/vertices become selected.
 *
 * @author procyon
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.paths.ShortestPathsAction")
@ActionRegistration(
        displayName = "#CTL_ShortestPathsAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/paths/shortestpaths.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1500),
    @ActionReference(path = "Toolbars/Selection", position = 800)
})
@Messages("CTL_ShortestPathsAction=Shortest Paths")
public final class ShortestPathsAction extends SimplePluginAction {

    public ShortestPathsAction(final GraphNode context) {
        super(context, AlgorithmPluginRegistry.SHORTEST_PATHS);
    }
}
