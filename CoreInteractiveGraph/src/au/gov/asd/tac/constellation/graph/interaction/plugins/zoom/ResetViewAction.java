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
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Reset the display so the graph is unrotated, unpanned, unzoomed, and the eye
 * can see all of the graph.
 * <p>
 * The display will be reset to a different axis depending on whether nothing,
 * shift, or control was held down.
 *
 * @author algol
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.zoom.ResetViewAction")
@ActionRegistration(displayName = "#CTL_ResetViewAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/zoom_reset.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 300),
    @ActionReference(path = "Toolbars/Display", position = 0),
    @ActionReference(path = "Shortcuts", name = "C-Down")
})
@Messages("CTL_ResetViewAction=Reset View")
public final class ResetViewAction extends AbstractAction {

    final GraphNode context;

    public ResetViewAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final boolean isCtrl = (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0;
        final boolean isShift = (e.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0;
        final String axis;
        final boolean negative;
        if (isShift && !isCtrl) {
            axis = "x";
            negative = false;
        } else if (isCtrl && !isShift) {
            axis = "y";
            negative = true;
        } else {
            axis = "z";
            negative = false;
        }

        final Graph graph = context.getGraph();

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW)
                .withParameter(ResetViewPlugin.AXIS_PARAMETER_ID, axis)
                .withParameter(ResetViewPlugin.NEGATIVE_PARAMETER_ID, negative)
                .withParameter(ResetViewPlugin.SIGNIFICANT_PARAMETER_ID, true)
                .executeLater(graph);
    }
}
