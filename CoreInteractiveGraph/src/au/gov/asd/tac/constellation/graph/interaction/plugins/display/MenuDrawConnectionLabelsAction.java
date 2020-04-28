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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.gui.MenuBaseAction;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * This class produces a menu action for the 'set draw nodes' action available
 * on the graph toolbar.
 *
 * @author altair
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.display.MenuDrawConnectionLabelsAction")
@ActionRegistration(displayName = "#CTL_MenuDrawConnectionLabelsAction", lazy = false)
@ActionReference(path = "Menu/Display/Element Visibility", position = 400)
@Messages({
    "CTL_MenuDrawConnectionLabelsAction=Connection Labels"
})
public class MenuDrawConnectionLabelsAction extends MenuBaseAction {

    /**
     * constructor
     */
    public MenuDrawConnectionLabelsAction() {
        super();
        this.initCheckBox(Bundle.CTL_MenuDrawConnectionLabelsAction(), true);
    }

    @Override
    protected void updateValue() {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.TOGGLE_DRAW_FLAG)
                .withParameter(ToggleDrawFlagPlugin.FLAG_PARAMETER_ID, DrawFlags.CONNECTION_LABELS)
                .executeLater(this.getContext().getGraph());
    }

    @Override
    protected void displayValue() {
        final Graph graph = getContext().getGraph();
        boolean flag = (VisualGraphUtilities.getDrawFlags(graph) & DrawFlags.CONNECTION_LABELS) != 0;
        menuButton.setSelected(flag);
    }
}
