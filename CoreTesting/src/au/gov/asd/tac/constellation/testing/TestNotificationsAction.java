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
package au.gov.asd.tac.constellation.testing;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.testing.TestNotificationsAction")
@ActionRegistration(displayName = "#CTL_TestNotificationsAction", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Developer", position = 0)
@Messages("CTL_TestNotificationsAction=Test Notifications")

/**
 * A demo of the different notifications the applications supports
 */
public final class TestNotificationsAction implements ActionListener {

    private static final String ERROR = "Error";
    private static final String WARNING = "Warning";
    private static final String INFORMATION = "Information";
    private static final String PLAIN = "Plain";
    private static final String DISPLAY_LARGE_ALERT = "Display Large Alert";
    private static final String DISPLAY_ALERT = "Display Alert";
    private static final String ERROR_MESSAGE = "Error Message";
    private static final String WARNING_MESSAGE = "Warning Message";
    private static final String INFORMATION_MESSAGE = "Information Message";
    private static final String QUESTION_MESSAGE = "Question Message";
    private static final String PLAIN_MESSAGE = "Plain Message";

    private final GraphNode context;

    public TestNotificationsAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        // plugin notifications
        final Plugin plugin = new TestNotificationsPlugin();
        PluginExecution.withPlugin(plugin).executeLater(context.getGraph());
        
        // NetBeans NotifyDisplayer options
        NotifyDisplayer.display(PLAIN_MESSAGE, NotifyDescriptor.PLAIN_MESSAGE);
        NotifyDisplayer.display(QUESTION_MESSAGE, NotifyDescriptor.QUESTION_MESSAGE);
        NotifyDisplayer.display(INFORMATION_MESSAGE, NotifyDescriptor.INFORMATION_MESSAGE);
        NotifyDisplayer.display(WARNING_MESSAGE, NotifyDescriptor.WARNING_MESSAGE);
        NotifyDisplayer.display(ERROR_MESSAGE, NotifyDescriptor.ERROR_MESSAGE);

        // Constellation Utility options
        Platform.runLater(() -> {
            NotifyDisplayer.displayAlert(DISPLAY_ALERT, PLAIN, PLAIN_MESSAGE, Alert.AlertType.NONE);
            NotifyDisplayer.displayAlert(DISPLAY_ALERT, INFORMATION, INFORMATION_MESSAGE, Alert.AlertType.INFORMATION);
            NotifyDisplayer.displayAlert(DISPLAY_ALERT, WARNING, WARNING_MESSAGE, Alert.AlertType.WARNING);
            NotifyDisplayer.displayAlert(DISPLAY_ALERT, ERROR, ERROR_MESSAGE, Alert.AlertType.ERROR);

            NotifyDisplayer.displayLargeAlert(DISPLAY_LARGE_ALERT, PLAIN, PLAIN_MESSAGE, Alert.AlertType.NONE);
            NotifyDisplayer.displayLargeAlert(DISPLAY_LARGE_ALERT, INFORMATION, INFORMATION_MESSAGE, Alert.AlertType.INFORMATION);
            NotifyDisplayer.displayLargeAlert(DISPLAY_LARGE_ALERT, WARNING, WARNING_MESSAGE, Alert.AlertType.WARNING);
            NotifyDisplayer.displayLargeAlert(DISPLAY_LARGE_ALERT, ERROR, ERROR_MESSAGE, Alert.AlertType.ERROR);
        });
    }
}
