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
package au.gov.asd.tac.constellation.views.layers.shortcut;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Defines the keyboard shortcuts for use with the Layers view
 *
 * @author formalhaut69
 */
@ActionID(category = "Options", id = "au.gov.asd.tac.constellation.views.layersviewshortcuts")
@ActionRegistration(displayName = "#CTL_LayersViewShortcuts", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "CA-D"),
    @ActionReference(path = "Shortcuts", name = "CA-L"),
    @ActionReference(path = "Shortcuts", name = "CA-1"),
    @ActionReference(path = "Shortcuts", name = "CA-2"),
    @ActionReference(path = "Shortcuts", name = "CA-3"),
    @ActionReference(path = "Shortcuts", name = "CA-4"),
    @ActionReference(path = "Shortcuts", name = "CA-5"),
    @ActionReference(path = "Shortcuts", name = "CA-6"),
    @ActionReference(path = "Shortcuts", name = "CA-7"),
    @ActionReference(path = "Shortcuts", name = "CA-8"),
    @ActionReference(path = "Shortcuts", name = "CA-9")
})
@NbBundle.Messages("CTL_LayersViewShortcuts=Layers View: Shortcuts")
public class LayersViewShortcuts extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {

        final String hotkey = e.getActionCommand();
        switch (hotkey) {
            case "CA-L":
                PluginExecution.withPlugin(new NewLayerPlugin()).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-D":
                PluginExecution.withPlugin(new DeselectAllLayersPlugin()).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-1":
                PluginExecution.withPlugin(new EnableLayerPlugin(1)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-2":
                PluginExecution.withPlugin(new EnableLayerPlugin(2)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-3":
                PluginExecution.withPlugin(new EnableLayerPlugin(3)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-4":
                PluginExecution.withPlugin(new EnableLayerPlugin(4)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-5":
                PluginExecution.withPlugin(new EnableLayerPlugin(5)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-6":
                PluginExecution.withPlugin(new EnableLayerPlugin(6)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-7":
                PluginExecution.withPlugin(new EnableLayerPlugin(7)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-8":
                PluginExecution.withPlugin(new EnableLayerPlugin(8)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            case "CA-9":
                PluginExecution.withPlugin(new EnableLayerPlugin(9)).executeLater(GraphManager.getDefault().getActiveGraph());
                break;
            default:
                break;
        }
    }
}
