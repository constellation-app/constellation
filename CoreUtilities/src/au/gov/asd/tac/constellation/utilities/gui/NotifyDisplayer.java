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

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;

/**
 *
 * @author aldebaran30701
 */
public class NotifyDisplayer {

    private static final Logger LOGGER = Logger.getLogger(NotifyDisplayer.class.getName());

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
        display(descriptor);
    }

    /**
     * Utility to notify the user of some fact. The passed icon can be used to
     * classify the severity or type of notification.
     *
     * @param title the title of the notification
     * @param icon the icon to be added to the dialog
     * @param message the notification message
     */
    public static void display(final String title, final Icon icon, final String message) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // If this was called from one of the UI threads we don't want to
            // display the dialog and block beacasue some OS's (macos) will go into deadlock
            // I think what happens is, instead of running the display dialog in this
            // "task" it creates a new one to display the dialog, puts it on the event
            // queue then blocks in this task waiting for input from the user closing the dialog.
            // Now because this thread (the UI thread) is blocked waiting for user input
            // the dialog is never rendered and a deadlock happens.
            CompletableFuture.runAsync(() -> display(title, icon, message));
        } else {
            EventQueue.invokeLater(() -> NotificationDisplayer.getDefault().notify(title, icon, message, null));
        }
    }

    /**
     * Display the passed notify descriptor and do not worry about the user
     * response.
     *
     * @param descriptor the descriptor to display in a dialog
     */
    public static void display(final NotifyDescriptor descriptor) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // If this was called from one of the UI threads we don't want to
            // display the dialog and block beacasue some OS's (macos) will go into deadlock
            // I think what happens is, instead of running the display dialog in this
            // "task" it creates a new one to display the dialog, puts it on the event
            // queue then blocks in this task waiting for input from the user closing the dialog.
            // Now because this thread (the UI thread) is blocked waiting for user input
            // the dialog is never rendered and a deadlock happens.
            LOGGER.log(Level.INFO, "TITLE: {0} | MESSAGE: {1}", new Object[]{descriptor.getTitle(), descriptor.getMessage()});
            CompletableFuture.runAsync(() -> display(descriptor));
            LOGGER.log(Level.INFO, "TITLE: {0} | MESSAGE: {1}", new Object[]{descriptor.getTitle(), descriptor.getMessage()});
        } else {
//            EventQueue.invokeLater(() -> {
//                DialogDisplayer.getDefault().notify(descriptor);
//                LOGGER.log(Level.INFO, "TITLE: {0} | MESSAGE: {1}", new Object[]{descriptor.getTitle(), descriptor.getMessage()});
//            });
        }
    }

    /**
     * Display the passed notify descriptor and wait for a response. This method
     * will not block but return a future that can be used for dealing with
     * carry on processing.
     *
     * @param descriptor the descriptor to display in a dialog
     * @return a future containing the user selection from the dialog
     */
    public static CompletableFuture<Object> displayAndWait(final NotifyDescriptor descriptor) {
        if (SwingUtilities.isEventDispatchThread() || Platform.isFxApplicationThread()) {
            // Call the method again and wrap it in a completable future so its not
            // on the UI thread.
            return CompletableFuture.supplyAsync(() -> displayAndWait(descriptor));
        } else {
            // Execute the dialog open and present on the event queue and return
            // in a completable future so that callers can act on the response
            // once it completes
            final ShowDialogRunner showDialogRunner = new ShowDialogRunner(descriptor);
            try {
                EventQueue.invokeAndWait(showDialogRunner);

                return CompletableFuture.completedFuture(showDialogRunner.getSelection());
            } catch (InterruptedException ex) {
                LOGGER.log(Level.WARNING, "Thread displaying the notify dialog was interrupted.", ex);
                Thread.currentThread().interrupt();

                return CompletableFuture.completedFuture(null);
            } catch (InvocationTargetException ex) {
                // An error happened when showing the dialog. Send it up the stack.
                throw new RuntimeException("Error occured during user dialog notification" + ex.getCause());
            }
        }
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
    public static void displayAlert(final String title,
            final String header,
            final String message,
            final Alert.AlertType alertType) {
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
    public static void displayLargeAlert(final String title,
            final String header,
            final String message,
            final Alert.AlertType alertType) {
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
    public static Optional<ButtonType> displayConfirmationAlert(final String title,
            final String header,
            final String message) {
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
