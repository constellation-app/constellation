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

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Zoom the camera to the currently selected elements.
 *
 * @author algol
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.zoom.ZoomToSelectionAction")
@ActionRegistration(displayName = "#CTL_ZoomToSelectionAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/zoom_to_selection.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 200, separatorBefore = 199),
    @ActionReference(path = "Toolbars/Display", position = 100),
    @ActionReference(path = "Shortcuts", name = "C-Up")
})
@Messages("CTL_ZoomToSelectionAction=Zoom to Selection")
public final class ZoomToSelectionAction implements ActionListener {

    private final GraphNode context;

    /**
     * Construct a new ZoomToSelectionAction.
     *
     * @param context GraphNode.
     */
    public ZoomToSelectionAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.ZOOM_TO_SELECTION).executeLater(context.getGraph());
    }
}
