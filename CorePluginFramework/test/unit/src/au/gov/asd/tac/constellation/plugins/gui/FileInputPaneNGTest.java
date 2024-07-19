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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.filesystems.FileChooserBuilder;

import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
        } catch (Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }
    }

    @Test
    public void testConstructor() {
        System.out.println("testConstructor");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(SAVE_TYPE, FileExtensionConstants.SVG);
        final FileInputPane instance = new FileInputPane(paramInstance);

        assertEquals(instance.getClass(), FileInputPane.class);
    }

    @Test
    public void testConstructorTwoParams() {
        System.out.println("testConstructorTwoParams");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(SAVE_TYPE, FileExtensionConstants.SVG);
        final FileInputPane instance = new FileInputPane(paramInstance, FileInputPane.DEFAULT_WIDTH);

        assertEquals(instance.getClass(), FileInputPane.class);
    }

    @Test
    public void testConstructorThreeParams() {
        System.out.println("testConstructorThreeParams");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(SAVE_TYPE, FileExtensionConstants.SVG);
        final FileInputPane instance = new FileInputPane(paramInstance, FileInputPane.DEFAULT_WIDTH, 1);

        assertEquals(instance.getClass(), FileInputPane.class);
    }

//    @Test
//    public void testGetFileChooser() {
//        System.out.println("testGetFileChooser");
//
//        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(SAVE_TYPE, FileExtensionConstants.SVG);
//
//        final FileInputPane instance = new FileInputPane(paramInstance);
//
//        final Button button = instance.getFileAddButton();
//        System.out.println(button.getText());
//
//        System.out.println(button.getOnAction());
//        assertEquals(instance.getClass(), FileInputPane.class);
//
//    }
    private static Optional<File> stubLambda(final FileChooserBuilder fileChooserBuilder, final FileChooserMode fileDialogMode) {
        System.out.print("Stubbed Lambda called. FileChooserBuilder: " + fileChooserBuilder.toString() + "FileChooserMode: " + fileDialogMode);
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
    public void testGetFileChooser() {
        System.out.println("testGetFileChooser");

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
    public void testHandleEventFilter() {
        System.out.println("testHandleEventFilter");

        final ArrayList<KeyEvent> events = new ArrayList<>();
        final KeyCode[] keyCodes = {KeyCode.RIGHT, KeyCode.LEFT};

        for (final KeyCode k : keyCodes) {
            events.add(new KeyEvent(null, null, null, "", "", k, true, false, false, false));
            events.add(new KeyEvent(null, null, null, "", "", k, false, true, false, false));
            events.add(new KeyEvent(null, null, null, "", "", k, true, true, false, false));
        }

        events.add(new KeyEvent(null, null, null, "", "", KeyCode.DELETE, false, false, false, false));
        events.add(new KeyEvent(null, null, null, "", "", KeyCode.ESCAPE, false, false, false, false));
        events.add(new KeyEvent(null, null, null, "", "", KeyCode.A, false, true, false, false));

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper(SAVE_TYPE, FileExtensionConstants.SVG);
        final FileInputPane instance = new FileInputPane(paramInstance);

        final TextInputControl field = new TextArea();

        for (final KeyEvent e : events) {
            instance.handleEventFilter(e, field);
            assertTrue(e.isConsumed());
        }

        // Test for else do nothing
        final KeyEvent doNothingEvent = new KeyEvent(null, null, null, "", "", KeyCode.B, false, false, false, false);

        instance.handleEventFilter(doNothingEvent, field);
        assertFalse(doNothingEvent.isConsumed());

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
