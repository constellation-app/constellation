/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
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
@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.plugins.algorithms.paths.ShortestPathsFollowDirectionAction")
@ActionRegistration(displayName = "#CTL_ShortestPathsFollowDirectionAction", iconBase = "au/gov/asd/tac/constellation/plugins/algorithms/paths/shortestpathsfd.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1600)
})
@Messages("CTL_ShortestPathsFollowDirectionAction=Directed Shortest Paths")
public final class ShortestPathsFollowDirectionAction implements ActionListener {

    private final GraphNode context;

    public ShortestPathsFollowDirectionAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final Plugin shortestPathsFollowDirectionPlugin = PluginRegistry.get(AlgorithmPluginRegistry.DIRECTED_SHORTEST_PATHS);
        final PluginParameters parameters = shortestPathsFollowDirectionPlugin.createParameters();
        final PluginParametersSwingDialog dlg = new PluginParametersSwingDialog("Set the source node", parameters);
        dlg.showAndWait();

        if (PluginParametersSwingDialog.OK.equals(dlg.getResult())) {
            PluginExecution.withPlugin(AlgorithmPluginRegistry.DIRECTED_SHORTEST_PATHS)
                    .withParameters(parameters)
                    .executeLater(context.getGraph());
        }

    }
}
