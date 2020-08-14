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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.layers.utilities.NewLayerPlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author formalhaut69
 */
@ActionID(category = "Options", id = "au.gov.asd.tac.constellation.views.layersviewshortcuts")
@ActionRegistration(displayName = "#CTL_LayersViewShortcuts", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "C-0")
})
@NbBundle.Messages("CTL_LayersViewShortcuts=Layers View: Shortcuts")
public class LayersViewShortcuts extends AbstractAction{

    @Override
    public void actionPerformed(ActionEvent e) {
        /*
         * Determine whether we have to write a new named selection (ie Control key was held),
         * or to read a previously saved named selection (ie Control was not held).
         */
        String hotkey = e.getActionCommand();
        PluginExecution.withPlugin(new NewLayerPlugin()).executeLater(GraphManager.getDefault().getActiveGraph());
    }
    
}
