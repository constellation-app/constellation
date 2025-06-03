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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterKind;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooserMode;
import java.io.File;
import javafx.scene.input.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class FileInputPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(FileInputPaneNGTest.class.getName());

    private static final FileParameterKind OPEN_TYPE = FileParameterType.FileParameterKind.OPEN;
    private static final FileParameterKind OPEN_MULTIPLE_TYPE = FileParameterType.FileParameterKind.OPEN_MULTIPLE;
    private static final FileParameterKind SAVE_TYPE = FileParameterType.FileParameterKind.SAVE;

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            if (!FxToolkit.isFXApplicationThreadRunning()) {
                FxToolkit.registerPrimaryStage();
            }
        } catch (Exception e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
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

    private static Optional<File> stubLambda(final FileChooserBuilder fileChooserBuilder, final FileChooserMode fileDialogMode) {
        System.out.print("Stubbed Lambda called. FileChooserBuilder: " + fileChooserBuilder + " FileChooserMode: " + fileDialogMode);
        return Optional.empty();
    }

    @Test
    public void testHandleButtonOnAction() {
        System.out.println("testHandleButtonOnAction");

        final FileParameterType.FileParameterKind[] kindArray = {OPEN_TYPE, OPEN_MULTIPLE_TYPE, SAVE_TYPE};
        final String[] titleArray = {"title open", "title open_multiple", "title save"};
        final String[] fileExtensionArray = {null, "", "svg"};

        final CompletableFuture dialogFuture = CompletableFuture.completedFuture(stubLambda(null, null));

        try (MockedStatic<FileChooser> fileChooserStaticMock = Mockito.mockStatic(FileChooser.class, Mockito.CALLS_REAL_METHODS)) {
            // Setup static mock
            fileChooserStaticMock.when(() -> FileChooser.openOpenDialog(any(FileChooserBuilder.class))).thenReturn(dialogFuture);
            fileChooserStaticMock.when(() -> FileChooser.openMultiDialog(any(FileChooserBuilder.class))).thenReturn(dialogFuture);
            fileChooserStaticMock.when(() -> FileChooser.openSaveDialog(any(FileChooserBuilder.class))).thenReturn(dialogFuture);

            for (int i = 0; i < titleArray.length; i++) {

                final FileParameterType.FileParameterKind kind = kindArray[i];
                final String title = titleArray[i];
                final String fileExtension = fileExtensionArray[i];

                final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(kind, fileExtension);
                final FileInputPane instance = new FileInputPane(paramInstance);
                final FileParameterType.FileParameterValue paramaterValue = paramInstance.getParameterValue();

                final FileChooserBuilder fcb = FileChooser.createFileChooserBuilder(title, fileExtension);

                assertEquals(FileChooserBuilder.class, fcb.setSelectionApprover((final File[] selection) -> true).getClass());

                instance.handleButtonOnAction(paramaterValue, paramInstance, fileExtension);
            }
        }
    }

    @Test
    public void handleButtonOnActionInterruptedException() {
        System.out.println("handleButtonOnActionInterruptedException");

        // Mock
        final CompletableFuture dialogFutureMock = mock(CompletableFuture.class);
        // Needs try catch
        try {
            doThrow(InterruptedException.class).when(dialogFutureMock).get();
        } catch (InterruptedException e) {
            System.out.println("Caught InterruptedException setting up mock in testGetFileChooser");
        } catch (ExecutionException e) {
            System.out.println("Caught ExecutionException setting up mock in testGetFileChooser");
        }
        when(dialogFutureMock.thenAccept(any(Consumer.class))).thenReturn(dialogFutureMock);

        // Check mock works
        assertThrows(InterruptedException.class, () -> dialogFutureMock.get());

        try (MockedStatic<FileChooser> fileChooserStaticMock = Mockito.mockStatic(FileChooser.class, Mockito.CALLS_REAL_METHODS)) {
            // Setup static mock
            fileChooserStaticMock.when(() -> FileChooser.openOpenDialog(any(FileChooserBuilder.class))).thenReturn(dialogFutureMock);
            fileChooserStaticMock.when(() -> FileChooser.openMultiDialog(any(FileChooserBuilder.class))).thenReturn(dialogFutureMock);
            fileChooserStaticMock.when(() -> FileChooser.openSaveDialog(any(FileChooserBuilder.class))).thenReturn(dialogFutureMock);

            final FileParameterType.FileParameterKind kind = OPEN_TYPE;
            final String title = "title open";
            final String fileExtension = "";

            final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(kind, fileExtension);
            final FileInputPane instance = new FileInputPane(paramInstance);
            final FileParameterType.FileParameterValue paramaterValue = paramInstance.getParameterValue();
            final FileChooserBuilder fcb = FileChooser.createFileChooserBuilder(title, fileExtension);

            assertEquals(FileChooserBuilder.class, fcb.setSelectionApprover((final File[] selection) -> true).getClass());

            // Should run without any exceptions
            instance.handleButtonOnAction(paramaterValue, paramInstance, fileExtension);
        }
    }

    @Test
    public void handleButtonOnActionExecutionException() {
        System.out.println("handleButtonOnActionExecutionException");

        // Mock
        final CompletableFuture dialogFutureMock = mock(CompletableFuture.class);
        // Needs try catch
        try {
            doThrow(ExecutionException.class).when(dialogFutureMock).get();
        } catch (InterruptedException e) {
            System.out.println("Caught InterruptedException setting up mock in testGetFileChooser");
        } catch (ExecutionException e) {
            System.out.println("Caught ExecutionException setting up mock in testGetFileChooser");
        }
        when(dialogFutureMock.thenAccept(any(Consumer.class))).thenReturn(dialogFutureMock);

        // Check mock works
        assertThrows(ExecutionException.class, () -> dialogFutureMock.get());

        try (MockedStatic<FileChooser> fileChooserStaticMock = Mockito.mockStatic(FileChooser.class, Mockito.CALLS_REAL_METHODS)) {
            // Setup static mock
            fileChooserStaticMock.when(() -> FileChooser.openOpenDialog(any(FileChooserBuilder.class))).thenReturn(dialogFutureMock);
            fileChooserStaticMock.when(() -> FileChooser.openMultiDialog(any(FileChooserBuilder.class))).thenReturn(dialogFutureMock);
            fileChooserStaticMock.when(() -> FileChooser.openSaveDialog(any(FileChooserBuilder.class))).thenReturn(dialogFutureMock);

            final FileParameterType.FileParameterKind kind = OPEN_TYPE;
            final String title = "title open";
            final String fileExtension = "";

            final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(kind, fileExtension);
            final FileInputPane instance = new FileInputPane(paramInstance);
            final FileParameterType.FileParameterValue paramaterValue = paramInstance.getParameterValue();
            final FileChooserBuilder fcb = FileChooser.createFileChooserBuilder(title, fileExtension);

            assertEquals(FileChooserBuilder.class, fcb.setSelectionApprover((final File[] selection) -> true).getClass());

            // Should run without any exceptions
            instance.handleButtonOnAction(paramaterValue, paramInstance, fileExtension);
        }
    }

    @Test
    public void testHandleEventFilter() {
        System.out.println("testHandleEventFilter");

        final List<KeyEvent> eventsSuccess = new ArrayList<>();
        final List<KeyEvent> eventsFail = new ArrayList<>();
        final TextInputControl field = new TextArea();
        final KeyCode[] keyCodes = {KeyCode.RIGHT, KeyCode.LEFT};

        // Setup tests that should succeed
        for (final KeyCode k : keyCodes) {
            eventsSuccess.add(new KeyEvent(null, null, null, "", "", k, true, false, false, false));
            eventsSuccess.add(new KeyEvent(null, null, null, "", "", k, false, true, false, false));
            eventsSuccess.add(new KeyEvent(null, null, null, "", "", k, true, true, false, false));
        }

        eventsSuccess.add(new KeyEvent(null, null, null, "", "", KeyCode.DELETE, false, false, false, false));
        eventsSuccess.add(new KeyEvent(null, null, null, "", "", KeyCode.ESCAPE, false, false, false, false));
        eventsSuccess.add(new KeyEvent(null, null, null, "", "", KeyCode.A, false, true, false, false));

        // All should consume event
        for (final KeyEvent e : eventsSuccess) {
            FileInputPane.handleEventFilter(e, field);
            assertTrue(e.isConsumed());
        }
        // Setup tests that should fail
        for (final KeyCode k : keyCodes) {
            eventsSuccess.add(new KeyEvent(null, null, null, "", "", k, false, false, false, false));
        }

        // None should consume event
        for (final KeyEvent e : eventsFail) {
            FileInputPane.handleEventFilter(e, field);
            assertFalse(e.isConsumed());
        }

        // Test for else do nothing
        final KeyEvent doNothingEvent = new KeyEvent(null, null, null, "", "", KeyCode.B, false, false, false, false);

        FileInputPane.handleEventFilter(doNothingEvent, field);
        assertFalse(doNothingEvent.isConsumed());
    }

    @Test
    public void testHandleEventFilterDeleteSelection() {
        System.out.println("testHandleEventFilterDeleteSelection");

        // Test for delete with selection
        final TextInputControl fieldMock = mock(TextArea.class);
        final IndexRange selectionMock = mock(IndexRange.class);

        when(fieldMock.getSelection()).thenReturn(selectionMock);
        when(selectionMock.getLength()).thenReturn(1);

        final KeyEvent deleteEvent = new KeyEvent(null, null, null, "", "", KeyCode.DELETE, false, false, false, false);
        FileInputPane.handleEventFilter(deleteEvent, fieldMock);
        assertTrue(deleteEvent.isConsumed());

    }

    private PluginParameter<FileParameterType.FileParameterValue> paramInstanceHelper(final FileParameterKind kind, final String extension) {
        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = FileParameterType.build("");
        paramInstance.setName("File Location");
        paramInstance.setDescription("File location and name for export");
        FileParameterType.setKind(paramInstance, kind);
        if (extension != null && !"".equals(extension)) {
            FileParameterType.setFileFilters(paramInstance, new javafx.stage.FileChooser.ExtensionFilter(extension + " file", extension));
        }
        FileParameterType.setWarnOverwrite(paramInstance, true);
        paramInstance.setRequired(true);

        return paramInstance;
    }
}
