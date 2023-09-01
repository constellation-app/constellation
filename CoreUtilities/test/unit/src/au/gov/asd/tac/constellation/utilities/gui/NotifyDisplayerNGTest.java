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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class NotifyDisplayerNGTest {

    private static final Logger LOGGER = Logger.getLogger(NotifyDisplayerNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @Test
    public void display() {
        display(true, true, true);
        display(true, false, true);
        display(false, true, true);
        display(false, false, false);
    }

    @Test
    public void displayWithIcon() {
        displayWithIcon(true, true, true);
        displayWithIcon(true, false, true);
        displayWithIcon(false, true, true);
        displayWithIcon(false, false, false);
    }

    @Test
    public void displayAlert() {
        final String title = "TITLE";
        final String header = "HEADER";
        final String message = "MESSAGE";
        final Alert.AlertType alertType = Alert.AlertType.WARNING;

        final DialogPane dialogPane = mock(DialogPane.class);
        final Scene scene = mock(Scene.class);
        final Stage stage = mock(Stage.class);

        try (final MockedConstruction<Alert> alertMockedConstruction = Mockito.mockConstruction(Alert.class,
                (mock, cnxt) -> {
                    assertEquals(cnxt.arguments().get(0), alertType);
                    assertEquals(cnxt.arguments().get(1), "");
                    assertEquals(cnxt.arguments().get(2), new ButtonType[]{ButtonType.OK});

                    when(mock.getDialogPane()).thenReturn(dialogPane);
                    when(dialogPane.getScene()).thenReturn(scene);
                    when(scene.getWindow()).thenReturn(stage);
                })) {
            NotifyDisplayer.displayAlert(title, header, message, alertType);

            assertEquals(alertMockedConstruction.constructed().size(), 1);

            final Alert mockAlert = alertMockedConstruction.constructed().get(0);

            verify(mockAlert).setTitle(title);
            verify(mockAlert).setHeaderText(header);
            verify(mockAlert).setContentText(message);

            verify(mockAlert).setResizable(true);

            verify(stage).setAlwaysOnTop(true);

            verify(mockAlert).showAndWait();
        }
    }

    @Test
    public void displayLargeAlert() {
        final String title = "TITLE";
        final String header = "HEADER";
        final String message = "MESSAGE";
        final Alert.AlertType alertType = Alert.AlertType.WARNING;

        final DialogPane dialogPane = mock(DialogPane.class);
        final Scene scene = mock(Scene.class);
        final Stage stage = mock(Stage.class);

        try (final MockedConstruction<Alert> alertMockedConstruction = Mockito.mockConstruction(Alert.class,
                (mock, cnxt) -> {
                    assertEquals(cnxt.arguments().get(0), alertType);
                    assertEquals(cnxt.arguments().get(1), "");
                    assertEquals(cnxt.arguments().get(2), new ButtonType[]{ButtonType.OK});

                    when(mock.getDialogPane()).thenReturn(dialogPane);
                    when(dialogPane.getScene()).thenReturn(scene);
                    when(scene.getWindow()).thenReturn(stage);
                })) {
            NotifyDisplayer.displayLargeAlert(title, header, message, alertType);

            assertEquals(alertMockedConstruction.constructed().size(), 1);

            final Alert mockAlert = alertMockedConstruction.constructed().get(0);

            verify(mockAlert).setTitle(title);
            verify(mockAlert).setHeaderText(header);

            final ArgumentCaptor<TextArea> captor = ArgumentCaptor.forClass(TextArea.class);
            verify(dialogPane).setExpandableContent(captor.capture());

            assertEquals(captor.getValue().getText(), message);
            assertTrue(captor.getValue().isWrapText());
            assertFalse(captor.getValue().isEditable());

            verify(mockAlert).setResizable(true);

            verify(stage).setAlwaysOnTop(true);

            verify(mockAlert).showAndWait();
        }
    }

    @Test
    public void displayConfirmationAlert() {
        final String title = "TITLE";
        final String header = "HEADER";
        final String message = "MESSAGE";

        final DialogPane dialogPane = mock(DialogPane.class);
        final Scene scene = mock(Scene.class);
        final Stage stage = mock(Stage.class);

        try (final MockedConstruction<Alert> alertMockedConstruction = Mockito.mockConstruction(Alert.class,
                (mock, cnxt) -> {
                    assertEquals(cnxt.arguments().get(0), Alert.AlertType.CONFIRMATION);
                    assertEquals(cnxt.arguments().get(1), "");
                    assertEquals(cnxt.arguments().get(2), new ButtonType[]{ButtonType.NO, ButtonType.YES});

                    when(mock.getDialogPane()).thenReturn(dialogPane);
                    when(dialogPane.getScene()).thenReturn(scene);
                    when(scene.getWindow()).thenReturn(stage);
                })) {
            NotifyDisplayer.displayConfirmationAlert(title, header, message);

            assertEquals(alertMockedConstruction.constructed().size(), 1);

            final Alert mockAlert = alertMockedConstruction.constructed().get(0);

            verify(mockAlert).setTitle(title);
            verify(mockAlert).setHeaderText(header);
            verify(mockAlert).setContentText(message);

            verify(mockAlert).setResizable(true);

            verify(stage).setAlwaysOnTop(true);

            verify(mockAlert).showAndWait();
        }
    }

    /**
     * Configurable test for the display notification with icon method.
     *
     * @param isEventDispatchThread true if the test is meant to be running in
     * the event dispatch thread, false otherwise
     * @param isFxApplicationThread true if the test is meant to be running in
     * the fx application thread, false otherwise
     * @param runThroughThread true if the call is meant to run through a
     * separate thread first, false otherwise
     */
    private void displayWithIcon(final boolean isEventDispatchThread,
            final boolean isFxApplicationThread,
            final boolean runThroughThread) {
        try (
                final MockedStatic<EventQueue> eventQueueMockedStatic = Mockito.mockStatic(EventQueue.class); final MockedStatic<CompletableFuture> completableFutureMockedStatic = Mockito.mockStatic(CompletableFuture.class); final MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class); final MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class); final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class);) {
            setupThreadingMocks(eventQueueMockedStatic, completableFutureMockedStatic, swingUtilitiesMockedStatic, platformMockedStatic);

            final NotificationDisplayer notificationDisplayer = mock(NotificationDisplayer.class);
            notificationDisplayerMockedStatic.when(NotificationDisplayer::getDefault).thenReturn(notificationDisplayer);

            swingUtilitiesMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(isEventDispatchThread);
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(isFxApplicationThread);

            final String title = "TITLE";
            final Icon icon = mock(Icon.class);
            final String message = "MESSAGE";

            NotifyDisplayer.display(title, icon, message);

            verify(notificationDisplayer).notify(title, icon, message, null);

            if (runThroughThread) {
                completableFutureMockedStatic.verify(() -> CompletableFuture.runAsync(any(Runnable.class)));
            } else {
                completableFutureMockedStatic.verifyNoInteractions();
            }
        }
    }

    /**
     * Configurable test for the display notification method.
     *
     * @param isEventDispatchThread true if the test is meant to be running in
     * the event dispatch thread, false otherwise
     * @param isFxApplicationThread true if the test is meant to be running in
     * the fx application thread, false otherwise
     * @param runThroughThread true if the call is meant to run through a
     * separate thread first, false otherwise
     */
    private void display(final boolean isEventDispatchThread,
            final boolean isFxApplicationThread,
            final boolean runThroughThread) {
        try (
                final MockedStatic<EventQueue> eventQueueMockedStatic = Mockito.mockStatic(EventQueue.class); final MockedStatic<CompletableFuture> completableFutureMockedStatic = Mockito.mockStatic(CompletableFuture.class); final MockedStatic<DialogDisplayer> dialogDisplayerMockedStatic = Mockito.mockStatic(DialogDisplayer.class); final MockedStatic<SwingUtilities> swingUtilitiesMockedStatic = Mockito.mockStatic(SwingUtilities.class); final MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class);) {
            setupThreadingMocks(eventQueueMockedStatic, completableFutureMockedStatic, swingUtilitiesMockedStatic, platformMockedStatic);

            final DialogDisplayer dialogDisplayer = mock(DialogDisplayer.class);
            dialogDisplayerMockedStatic.when(DialogDisplayer::getDefault).thenReturn(dialogDisplayer);

            swingUtilitiesMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(isEventDispatchThread);
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(isFxApplicationThread);

            final NotifyDescriptor descriptor = mock(NotifyDescriptor.class);

            NotifyDisplayer.display(descriptor);

            verify(dialogDisplayer).notify(descriptor);

//            if (runThroughThread) {
//                completableFutureMockedStatic.verify(() -> CompletableFuture.runAsync(any(Runnable.class)));
//            } else {
//                completableFutureMockedStatic.verifyNoInteractions();
//            }
        }
    }

    /**
     * Sets up common mocks to prevent a multi-threaded test from happening.
     *
     * @param eventQueueMockedStatic a static mock to {@link EventQueue}
     * @param completableFutureMockedStatic a static mock to
     * {@link CompletableFuture}
     * @param swingUtilitiesMockedStatic a static mock to {@link SwingUtilities}
     * @param platformMockedStatic a static mock to {@link Platform}
     */
    private void setupThreadingMocks(final MockedStatic<EventQueue> eventQueueMockedStatic,
            final MockedStatic<CompletableFuture> completableFutureMockedStatic,
            final MockedStatic<SwingUtilities> swingUtilitiesMockedStatic,
            final MockedStatic<Platform> platformMockedStatic) {
        completableFutureMockedStatic.when(() -> CompletableFuture.runAsync(any(Runnable.class))).thenAnswer(
                iom -> {
                    final Runnable runnable = iom.getArgument(0);

                    // We are running technically in another thread now so set the UI thread check stubs to false
                    swingUtilitiesMockedStatic.when(SwingUtilities::isEventDispatchThread).thenReturn(false);
                    platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

                    runnable.run();

                    return CompletableFuture.completedFuture(null);
                });

        eventQueueMockedStatic.when(() -> EventQueue.invokeLater(any(Runnable.class))).thenAnswer(
                iom -> {
                    final Runnable runnable = iom.getArgument(0);

                    runnable.run();

                    return null;
                });
    }
}
