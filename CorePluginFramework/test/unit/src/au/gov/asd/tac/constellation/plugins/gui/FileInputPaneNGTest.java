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
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import javafx.scene.input.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class FileInputPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(FileInputPaneNGTest.class.getName());

    public FileInputPaneNGTest() {
    }

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

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testConstructor() {
        System.out.println("testConstructor");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();
        final FileInputPane instance = new FileInputPane(paramInstance);

        assertEquals(instance.getClass(), FileInputPane.class);
    }

    @Test
    public void testConstructorTwoParams() {
        System.out.println("testConstructorTwoParams");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();
        final FileInputPane instance = new FileInputPane(paramInstance, FileInputPane.DEFAULT_WIDTH);

        assertEquals(instance.getClass(), FileInputPane.class);
    }

    @Test
    public void testConstructorThreeParams() {
        System.out.println("testConstructorThreeParams");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();
        final FileInputPane instance = new FileInputPane(paramInstance, FileInputPane.DEFAULT_WIDTH, 1);

        assertEquals(instance.getClass(), FileInputPane.class);
    }

    @Test
    public void testGetFileChooser() {
        System.out.println("testGetFileChooser");

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();

        final FileInputPane instance = new FileInputPane(paramInstance);

        final Button button = instance.getFileAddButton();
        System.out.println(button.getText());

        System.out.println(button.getOnAction());
        assertEquals(instance.getClass(), FileInputPane.class);

    }

//    @Test
//    public void testGetFileChooser() {
//        System.out.println("testGetFileChooser");
//
//        //final Button dummyButton = new Button(FileParameterType.FileParameterKind.SAVE.toString());
//        final Button dummyButton = new Button("hah");
//        System.out.println(dummyButton.getText());
//                 
//        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();
//        try (MockedConstruction<Button> mockButtonConstructor = Mockito.mockConstruction(Button.class, (mock, context) -> {
//            //doCallRealMethod().when(mock).setOnAction(ArgumentMatchers.<EventHandler<ActionEvent>>any());
//            mock = dummyButton;
//        })) {
//        //try (MockedConstruction<Button> mockButtonConstructor = Mockito.mockConstruction(Button.class)) {
//            // Setup mock constructor
//            //when(mockButtonConstructor.Button()).thenReturn(dummyButton);
//            
//            final FileInputPane instance = new FileInputPane(paramInstance);
//            
//            assertEquals(1, mockButtonConstructor.constructed().size());
//            final Button mockButton = mockButtonConstructor.constructed().get(0);
//            System.out.println(mockButton.getText());
//            
//            System.out.println(mockButton.getOnAction());
//            assertEquals(instance.getClass(), FileInputPane.class);
//        }
//
//    }
//    @Test
//    public void testHandleButtonOnAction() {
//        System.out.println("testHandleButtonOnAction");
//
//        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();
//        final FileInputPane instance = new FileInputPane(paramInstance);
//
//        final FileParameterType.FileParameterValue paramaterValue = paramInstance.getParameterValue();
//        final String fileExtension = null;
//
////        try (MockedStatic<au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser> fileChooserStaticMock = Mockito.mockStatic(au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser.class)) {
////            instance.handleButtonOnAction(paramaterValue, paramInstance, fileExtension);
////        }
//
//        instance.handleButtonOnAction(paramaterValue, paramInstance, fileExtension);
//    }

    @Test
    public void testHandleEventFilter() {
        System.out.println("testHandleEventFilter");

        final ArrayList<KeyEvent> events = new ArrayList<>();
        final KeyCode[] keyCodes = {KeyCode.RIGHT, KeyCode.LEFT};

        for (final KeyCode k : keyCodes) {
            events.add(new KeyEvent(null, null, null, "", "", KeyCode.RIGHT, true, false, false, false));
            events.add(new KeyEvent(null, null, null, "", "", KeyCode.RIGHT, false, true, false, false));
            events.add(new KeyEvent(null, null, null, "", "", KeyCode.RIGHT, true, true, false, false));
        }

        events.add(new KeyEvent(null, null, null, "", "", KeyCode.DELETE, false, false, false, false));
        events.add(new KeyEvent(null, null, null, "", "", KeyCode.ESCAPE, false, false, false, false));
        events.add(new KeyEvent(null, null, null, "", "", KeyCode.A, false, true, false, false));

        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = paramInstanceHelper();
        final FileInputPane instance = new FileInputPane(paramInstance);

        for (final KeyEvent e : events) {
            instance.handleEventFilter(e);
            assertTrue(e.isConsumed());
        }

        // Test for else do nothing
        final KeyEvent doNothingEvent = new KeyEvent(null, null, null, "", "", KeyCode.B, false, false, false, false);

        instance.handleEventFilter(doNothingEvent);
        assertFalse(doNothingEvent.isConsumed());

    }

    private PluginParameter<FileParameterType.FileParameterValue> paramInstanceHelper() {
        final PluginParameter<FileParameterType.FileParameterValue> paramInstance = FileParameterType.build("");
        paramInstance.setName("File Location");
        paramInstance.setDescription("File location and name for export");
        FileParameterType.setKind(paramInstance, FileParameterType.FileParameterKind.SAVE);
        FileParameterType.setFileFilters(paramInstance, new javafx.stage.FileChooser.ExtensionFilter("SVG file", FileExtensionConstants.SVG));
        FileParameterType.setWarnOverwrite(paramInstance, true);
        paramInstance.setRequired(true);

        return paramInstance;
    }
}
