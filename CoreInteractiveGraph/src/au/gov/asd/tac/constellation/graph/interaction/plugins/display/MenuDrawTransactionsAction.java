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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.gui.MenuBaseAction;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This class produces a menu action for the 'record video' action available on
 * the graph toolbar.
 *
 * @author altair
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.display.MenuDrawTransactionsAction")
@ActionRegistration(displayName = "#CTL_MenuDrawTransactionsAction", lazy = false)
@ActionReference(path = "Menu/Display/Connection Types", position = 100)
@Messages({
    "CTL_MenuDrawTransactionsAction=Transactions"
})
public class MenuDrawTransactionsAction extends MenuBaseAction {

    /**
     * constructor
     */
    public MenuDrawTransactionsAction() {
        super();
        this.initRadioButton(Bundle.CTL_MenuDrawTransactionsAction(), "ConnectionMode", false);
    }

    @Override
    protected void updateValue() {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.SET_CONNECTION_MODE)
                .withParameter(SetConnectionModePlugin.CONNECTION_MODE_PARAMETER_ID, ConnectionMode.TRANSACTION)
                .executeLater(getContext().getGraph());
    }

    @Override
    protected void displayValue() {
        final Graph graph = getContext().getGraph();
        boolean flag = VisualGraphUtilities.getConnectionMode(graph) == ConnectionMode.TRANSACTION;
        menuButton.setSelected(flag);
    }
}
