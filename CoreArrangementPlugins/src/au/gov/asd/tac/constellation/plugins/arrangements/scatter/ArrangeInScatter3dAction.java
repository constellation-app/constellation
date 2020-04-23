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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Arranging vertexes in a scatter3d
 *
 * @author CrucisGamma
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dAction")
@ActionRegistration(displayName = "#CTL_ArrangeInScatter3dAction", iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/scatter/resources/scatter3d.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 0),
    @ActionReference(path = "Toolbars/Arrange", position = 0),
    @ActionReference(path = "Shortcuts", name = "C-A-S")
})
@Messages("CTL_ArrangeInScatter3dAction=Scatter3d")

public final class ArrangeInScatter3dAction extends AbstractAction {

    private final GraphNode context;

    public ArrangeInScatter3dAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {

        final Plugin plugin = PluginRegistry.get(ArrangementPluginRegistry.SCATTER_3D);
        final PluginParameters params = plugin.createParameters();
        final Graph graph = context.getGraph();
        plugin.updateParameters(graph, params);

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_ArrangeInScatter3dAction(), params);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            PluginExecution.withPlugin(plugin).withParameters(params).executeLater(graph);
        }
    }
}
