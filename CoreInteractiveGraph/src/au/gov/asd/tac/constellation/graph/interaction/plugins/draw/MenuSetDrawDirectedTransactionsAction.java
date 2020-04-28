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
package au.gov.asd.tac.constellation.graph.interaction.plugins.draw;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.gui.MenuBaseAction;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This class produces a menu action for the 2D action available on the graph
 * toolbar.
 *
 * @author altair
 */
@ActionID(category = "Edit", id = "au.gov.asd.tac.constellation.functionality.display.MenuSetDrawDirectedTransactionsAction")
@ActionRegistration(displayName = "#CTL_MenuSetDrawDirectedTransactionsAction", lazy = false)
@ActionReference(path = "Menu/Edit", position = 900)
@Messages({
    "CTL_MenuSetDrawDirectedTransactionsAction=Draw Directed Transactions"
})
public class MenuSetDrawDirectedTransactionsAction extends MenuBaseAction {

    /**
     * constructor
     */
    public MenuSetDrawDirectedTransactionsAction() {
        super();
        this.initCheckBox(Bundle.CTL_MenuSetDrawDirectedTransactionsAction(), true);
    }

    @Override
    protected void updateValue() {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.TOGGLE_DRAW_DIRECTED)
                .executeLater(getContext().getGraph());
    }

    @Override
    protected void displayValue() {
        menuButton.setSelected(VisualGraphUtilities.getIsDrawingDirectedTransactions(getContext().getGraph()));
    }
}
