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
package au.gov.asd.tac.constellation.plugins.algorithms.paths;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Hidden until we figure out how to specify source/destination vertices.
 *
 * @author algol
 */
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.plugins.algorithms.paths.DirectedShortestPathsAction")
@ActionRegistration(displayName = "#CTL_DirectedShortestPathsAction", iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/paths/shortestpathsfd.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1600)
})
@Messages("CTL_DirectedShortestPathsAction=Directed Shortest Paths")
public final class DirectedShortestPathsAction implements ActionListener {

    private final GraphNode context;

    public DirectedShortestPathsAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        PluginExecution.withPlugin(AlgorithmPluginRegistry.DIRECTED_SHORTEST_PATHS)
                .interactively(true)
                .executeLater(context.getGraph());
    }
}
