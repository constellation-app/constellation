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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author algol
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.zoom.ResetToNZAxisAction")
@ActionRegistration(displayName = "#CTL_ResetToNZAxisAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_z_negative.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display/Reset View by Axis", position = 600)
})
@NbBundle.Messages("CTL_ResetToNZAxisAction=-Z Axis")
public class ResetToNZAxisAction extends AbstractAction {

    private final GraphNode context;
    private static final Icon AXIS_NZ_ICON = UserInterfaceIconProvider.AXIS_Z_NEGATIVE.buildIcon(16);

    public ResetToNZAxisAction(final GraphNode context) {
        putValue(Action.SMALL_ICON, AXIS_NZ_ICON);
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Graph graph = context.getGraph();
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "z")
                .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, true)
                .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                .executeLater(graph);
    }
}
