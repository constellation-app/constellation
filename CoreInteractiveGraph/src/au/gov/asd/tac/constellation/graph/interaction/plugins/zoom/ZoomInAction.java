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
import org.openide.util.NbBundle;

/**
 * Zoom the camera inwards
 *
 * @author Quasar985
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.functionality.zoom.ZoomIn")
@ActionRegistration(displayName = "#CTL_ZoomIn", iconBase = "au/gov/asd/tac/constellation/graph/interaction/plugins/zoom/resources/zoom_in.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 201, separatorBefore = 199),
    @ActionReference(path = "Toolbars/Display", position = 102),
    @ActionReference(path = "Shortcuts", name = "D-EQUALS"),
    @ActionReference(path = "Shortcuts", name = "D-ADD")
})
@NbBundle.Messages("CTL_ZoomIn=Zoom In")
public final class ZoomInAction implements ActionListener {

    private final GraphNode context;

    /**
     * Construct a new ZoomInAction.
     *
     * @param context GraphNode.
     */
    public ZoomInAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.ZOOM_IN).executeLater(context.getGraph());
    }
}
