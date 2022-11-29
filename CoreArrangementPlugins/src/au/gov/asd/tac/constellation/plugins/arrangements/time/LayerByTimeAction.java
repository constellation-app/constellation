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
package au.gov.asd.tac.constellation.plugins.arrangements.time;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Modifies the existing graph by using a datetime attribute on transactions to
 * split the nodes and transactions into layers on the z-axis
 *
 * @author procyon
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.time.LayerByTimeAction")
@ActionRegistration(displayName = "#CTL_LayerByTimeAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/time/resources/layerByTime.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/Arrange", position = 2500)
@Messages({
    "CTL_LayerByTimeAction=Layer by Time",
    "MSG_NewGraph=This will create a new graph with additional nodes and transactions."
})
public final class LayerByTimeAction extends AbstractAction {

    private final GraphNode context;
    private final Dimension size = new Dimension(550, 750);

    public LayerByTimeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {

        final Plugin plugin = PluginRegistry.get(ArrangementPluginRegistry.TIME);
        final PluginParameters params = plugin.createParameters();
        final Graph graph = context.getGraph();
        plugin.updateParameters(graph, params);

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_LayerByTimeAction(), params);
        dialog.setSize(size);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            PluginExecution.withPlugin(plugin).withParameters(params).executeLater(graph);
        }

    }
}
