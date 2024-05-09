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
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.zoom.ResetToZAxisAction")
@ActionRegistration(displayName = "#CTL_ResetToZAxisAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/axis_z.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display/Reset View by Axis", position = 500)
})
@NbBundle.Messages("CTL_ResetToZAxisAction=Z Axis")
public class ResetToZAxisAction extends AbstractAction {

    private final GraphNode context;
    private static final Icon AXIS_Z_ICON = UserInterfaceIconProvider.AXIS_Z.buildIcon(16);

    public ResetToZAxisAction(final GraphNode context) {
        putValue(Action.SMALL_ICON, AXIS_Z_ICON);
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Graph graph = context.getGraph();

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, "z")
                .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                .executeLater(graph);
    }
}
