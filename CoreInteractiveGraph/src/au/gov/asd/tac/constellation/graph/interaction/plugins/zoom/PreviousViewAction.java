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
 * Zoom the camera to where it was before zoom to selection.
 *
 * @author algol
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.zoom.PreviousViewAction")
@ActionRegistration(displayName = "#CTL_PreviousViewAction", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/zoom_from_selection.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Tools", position = 0)
})
@Messages("CTL_PreviousViewAction=Previous Camera View")
public final class PreviousViewAction implements ActionListener {

    private final GraphNode context;

    /**
     * Construct a new ZoomFromSelectionAction.
     *
     * @param context GraphNode.
     */
    public PreviousViewAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.PREVIOUS_VIEW)
                .executeLater(context.getGraph());
    }
}
