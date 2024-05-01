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
package au.gov.asd.tac.constellation.graph.node.templates;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.awt.event.ActionEvent;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author twilight_sparkle
 */
@ActionID(category = "File", id = "au.gov.asd.tac.constellation.graph.node.templates.SaveTemplateAction")
@ActionRegistration(displayName = "#CTL_SaveTemplateAction",
        iconBase = "au/gov/asd/tac/constellation/graph/node/templates/saveTemplate.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 600, separatorBefore = 599)
})
@Messages("CTL_SaveTemplateAction=Save Template")
public class SaveTemplateAction extends AbstractAction {

    private final GraphNode context;

    public SaveTemplateAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Plugin plugin = PluginRegistry.get(GraphNodePluginRegistry.SAVE_TEMPLATE);
        final PluginParameters params = plugin.createParameters();
        while (true) {
            final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_SaveTemplateAction(), params);
            dialog.showAndWait();
            if (PluginParametersDialog.OK.equals(dialog.getResult())) {
                if (NewSchemaGraphAction.getTemplateNames().containsKey(params.getStringValue(SaveTemplatePlugin.TEMPLATE_NAME_PARAMETER_ID))) {
                    final PluginParameters warningParams = new PluginParameters();
                    final PluginParameter<StringParameterValue> warningMessageParam = StringParameterType.build("");
                    warningMessageParam.setName("");
                    warningMessageParam.setStringValue("Warning template with that name already exists - really overwrite?");
                    StringParameterType.setIsLabel(warningMessageParam, true);
                    warningParams.addParameter(warningMessageParam);
                    final PluginParametersSwingDialog overwrite = new PluginParametersSwingDialog("Overwrite?", warningParams);
                    overwrite.showAndWait();
                    if (!PluginParametersDialog.OK.equals(overwrite.getResult())) {
                        continue;
                    }
                }
                Future<?> f = PluginExecution.withPlugin(plugin).withParameters(params).executeLater(context.getGraph());
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
            break;
        }
    }

}
