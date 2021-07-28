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
package au.gov.asd.tac.constellation.utilities.gui;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author aldebaran30701
 */
public class NotifyDisplayer {

    /**
     * Utility display method to show a dialog to the user.
     * NotifyDescriptor.ERROR_MESSAGE will be used for errors
     * NotifyDescriptor.INFORMATION_MESSAGE will be used for information
     * NotifyDescriptor.WARNING_MESSAGE will be used for warnings
     *
     * @param message the String message to display on the prompt
     * @param descriptorType the int value representative of the message type
     */
    public static void display(final String message, final int descriptorType) {
        final NotifyDescriptor descriptor = new NotifyDescriptor.Message(message, descriptorType);
        DialogDisplayer.getDefault().notify(descriptor);
    }

    /**
     * Utility display method to show an Alert to the user.
     * Alert.AlertType.ERROR will be used for errors Alert.AlertType.INFORMATION
     * will be used for information Alert.AlertType.WARNING will be used for
     * warnings
     *
     * @param title the title of the alert
     * @param header the header message for the alert
     * @param message the message to display within the alert
     * @param alertType the alert icon to add to the alert
     */
    public static void displayAlert(final String title, final String header, final String message, final Alert.AlertType alertType) {
        final Alert dialog = new Alert(alertType, "", ButtonType.OK);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.setResizable(true);
        final Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        dialog.showAndWait();
    }

    /**
     * Utility display method to show an Alert to the user.
     * Alert.AlertType.ERROR will be used for errors Alert.AlertType.INFORMATION
     * will be used for information Alert.AlertType.WARNING will be used for
     * warnings This utility method differs from displayAlert as it uses a
     * TextArea to display a large amount of text.
     *
     * @param title the title of the alert
     * @param header the header message for the alert
     * @param message the message to display within the alert
     * @param alertType the alert icon to add to the alert
     */
    public static void displayLargeAlert(final String title, final String header, final String message, final Alert.AlertType alertType) {
        final Alert dialog = new Alert(alertType, "", ButtonType.OK);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        final TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setText(message);
        dialog.getDialogPane().setExpandableContent(ta);
        dialog.setResizable(true);
        final Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        dialog.showAndWait();
    }

    /**
     * Utility display method to show an Alert and get the confirmation of the
     * user. Alert.AlertType.CONFIRMATION is used. This utility method differs
     * from displayAlert as it uses a TextArea to display a large amount of
     * text.
     *
     * @param title the title of the alert
     * @param header the header message for the alert
     * @param message the message to display within the alert
     *
     * @return the user confirmation type
     */
    public static Optional<ButtonType> displayConfirmationAlert(final String title, final String header, final String message) {
        final Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.NO, ButtonType.YES);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.setResizable(true);
        final Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        return dialog.showAndWait();
    }
}
