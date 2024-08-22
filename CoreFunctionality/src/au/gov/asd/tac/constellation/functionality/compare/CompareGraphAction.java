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
package au.gov.asd.tac.constellation.functionality.compare;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.gui.ScreenWindowsHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Tools", id = "au.gov.asd.tac.constellation.functionality.compare.CompareGraphAction")
@ActionRegistration(displayName = "#CTL_CompareGraphAction",
        iconBase = "au/gov/asd/tac/constellation/functionality/compare/compareGraph.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/Tools", position = 600)
@Messages("CTL_CompareGraphAction=Compare Graph")
public final class CompareGraphAction implements ActionListener {

    private final GraphNode context;

    public CompareGraphAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // Check current graphs opened
        if (GraphManager.getDefault().getAllGraphs().size() > 1) {
            PluginExecution.withPlugin(CorePluginRegistry.COMPARE_GRAPH)
                .interactively(true)
                .executeLater(context.getGraph());
        } else {
            Platform.runLater(() -> NotifyDisplayer.displayAlert("Compare Graph", "Warning", 
                    "Two or more graphs need to be open before comparing.", Alert.AlertType.WARNING,
                    ScreenWindowsHelper.getNewCentrePoint()));
        }
    }
}
