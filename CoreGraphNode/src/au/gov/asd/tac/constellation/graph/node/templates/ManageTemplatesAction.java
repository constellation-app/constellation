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
package au.gov.asd.tac.constellation.graph.node.templates;

import au.gov.asd.tac.constellation.graph.node.GraphNodePluginRegistry;
import au.gov.asd.tac.constellation.graph.node.create.NewSchemaGraphAction;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Future;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author twilight_sparkle
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.graph.node.templates.ManageTemplatesAction")
@ActionRegistration(displayName = "#CTL_ManageTemplatesAction",
        iconBase = "au/gov/asd/tac/constellation/graph/node/templates/manageTemplates.png")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 610)
})
@Messages("CTL_ManageTemplatesAction=Manage Templates")
public class ManageTemplatesAction implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Plugin plugin = PluginRegistry.get(GraphNodePluginRegistry.MANAGE_TEMPLATES);
        final PluginParameters params = plugin.createParameters();
        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_ManageTemplatesAction(), params);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            Future<?> f = PluginExecution.withPlugin(plugin).withParameters(params).executeLater(null);
            PluginExecution.withPlugin(new SimplePlugin() {
                @Override
                public String getName() {
                    return "Update Template Menu";
                }

                @Override
                protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
                    NewSchemaGraphAction.recreateTemplateMenuItems();
                }
            }).waitingFor(f).executeLater(null);
        }
    }

}
