/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.TestNotificationsAction")
@ActionRegistration(displayName = "#CTL_TestNotificationsAction", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Developer", position = 0)
@Messages("CTL_TestNotificationsAction=Test Notifications")
public final class TestNotificationsAction implements ActionListener {

    private final GraphNode context;

    public TestNotificationsAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Plugin plugin = new TestNotificationsPlugin();
        final PluginParameters pp = plugin.createParameters();

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_TestNotificationsAction(), pp);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            PluginExecution.withPlugin(plugin).withParameters(pp).executeLater(context.getGraph());
        }
    }
}
