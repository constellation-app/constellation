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
package au.gov.asd.tac.constellation.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ApplicationOptionsPanelControllerNGTest {

    public ApplicationOptionsPanelControllerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getColorMask method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testGetColorMask() {
        System.out.println("getColorMask");

        final Map<String, boolean[]> expResultMap = Map.of(
                "Blue", new boolean[]{false, false, true},
                "Cyan", new boolean[]{false, true, true},
                "Green", new boolean[]{false, true, false},
                "Magenta", new boolean[]{true, false, true},
                "Red", new boolean[]{true, false, false},
                "Yellow", new boolean[]{true, true, false}
        );

        for (Map.Entry<String, boolean[]> pair : expResultMap.entrySet()) {
            String color = pair.getKey();
            boolean[] expResult = pair.getValue();
            boolean[] result = ApplicationOptionsPanelController.getColorMask(color);
            assertEquals(result, expResult);
        }

    }

    /**
     * Test of update method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        final ApplicationOptionsPanelController instance = spy(new ApplicationOptionsPanelController());

        try (MockedConstruction<ApplicationOptionsPanel> mock = mockConstruction(ApplicationOptionsPanel.class)) {
            instance.update();

            // Get a list of all created mocks
            List<ApplicationOptionsPanel> constructed = mock.constructed();
            assertEquals(1, constructed.size());

            // Assert that created panel had run the following methods
            verify(constructed.get(0), times(1)).setUserDirectory(anyString());
            verify(constructed.get(0), times(1)).setAutosaveEnabled(anyBoolean());
            verify(constructed.get(0), times(1)).setAutosaveFrequency(anyInt());
            verify(constructed.get(0), times(1)).setWelcomeOnStartup(anyBoolean());
            verify(constructed.get(0), times(1)).setWhatsNewOnStartup(anyBoolean());
            verify(constructed.get(0), times(1)).setWebserverPort(anyInt());
            verify(constructed.get(0), times(1)).setNotebookDirectory(anyString());
            verify(constructed.get(0), times(1)).setRestDirectory(anyString());
            verify(constructed.get(0), times(1)).setDownloadPythonClient(anyBoolean());
            verify(constructed.get(0), times(1)).setCurrentFont(anyString());
            verify(constructed.get(0), times(1)).setFontSize(anyString());
            verify(constructed.get(0), times(1)).setEnableSpellChecking(anyBoolean());
            verify(constructed.get(0), times(1)).setColorModeSelection(anyString());
        }
    }

//    /**
//     * Test of applyChanges method, of class ApplicationOptionsPanelController.
//     */
//    @Test
//    public void testApplyChanges() {
//        System.out.println("applyChanges");
//        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
//        //ApplicationOptionsPanelController instance = mock(ApplicationOptionsPanelController.class);
//        when(instance.isValid()).thenReturn(true);
//
//        try (MockedConstruction<ApplicationOptionsPanel> mockAP = mockConstruction(ApplicationOptionsPanel.class); MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {
//            instance.applyChanges();
//
////            // Assert panel was created
////            List<ApplicationOptionsPanel> constructedAP = mockAP.constructed();
////            assertEquals(1, constructedAP.size());
////
////            // Assert PCS was created
////            List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
////            assertEquals(1, constructedPCS.size());
//            verify(instance, times(1)).isValid();
//            verify(instance, times(1)).isChanged();
//
//        }
//    }
//    /**
//     * Test of cancel method, of class ApplicationOptionsPanelController.
//     */
//    @Test
//    public void testCancel() {
//        System.out.println("cancel");
//        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
//        instance.cancel();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of isValid method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid Basic");
        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
        boolean expResult = false;

        try (MockedConstruction<ApplicationOptionsPanel> mockAP = mockConstruction(ApplicationOptionsPanel.class)) {
            boolean result = instance.isValid();
            assertEquals(result, expResult);

            // Get a list of all created mocks
            List<ApplicationOptionsPanel> constructed = mockAP.constructed();
            assertEquals(1, constructed.size());

            // Assert that created panel had run the following method
            verify(constructed.get(0), times(1)).getUserDirectory();
            // Assert that created panel had NOT run the following methods, as the first is false
            verify(constructed.get(0), times(0)).getAutosaveFrequency();
            verify(constructed.get(0), times(0)).getWebserverPort();
            verify(constructed.get(0), times(0)).getNotebookDirectory();
            verify(constructed.get(0), times(0)).getRestDirectory();
            verify(constructed.get(0), times(0)).getCurrentFont();
            verify(constructed.get(0), times(0)).getFontSize();
            verify(constructed.get(0), times(0)).getColorModeSelection();
        }
    }

    /**
     * Test of isValid method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testIsValidAllValid() {
        System.out.println("isValid All Valid");
        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
        boolean expResult = true;

        try (MockedConstruction<ApplicationOptionsPanel> mockAP = mockConstruction(ApplicationOptionsPanel.class,
                (mockInstance, context) -> {
                    //implement method for mock so true is returned from isValid
                    when(mockInstance.getUserDirectory()).thenReturn("");
                    when(mockInstance.getAutosaveFrequency()).thenReturn(1);
                    when(mockInstance.getWebserverPort()).thenReturn(1);
                    when(mockInstance.getNotebookDirectory()).thenReturn("");
                    when(mockInstance.getRestDirectory()).thenReturn("");
                    when(mockInstance.getCurrentFont()).thenReturn("");
                    when(mockInstance.getFontSize()).thenReturn("");
                    when(mockInstance.getColorModeSelection()).thenReturn("");
                })) {

            boolean result = instance.isValid();
            assertEquals(result, expResult);

            // Get a list of all created mocks
            List<ApplicationOptionsPanel> constructed = mockAP.constructed();
            assertEquals(1, constructed.size());

            // Assert that created panel had run the following methods
            verify(constructed.get(0), times(1)).getUserDirectory();
            verify(constructed.get(0), times(1)).getAutosaveFrequency();
            verify(constructed.get(0), times(1)).getWebserverPort();
            verify(constructed.get(0), times(1)).getNotebookDirectory();
            verify(constructed.get(0), times(1)).getRestDirectory();
            verify(constructed.get(0), times(1)).getCurrentFont();
            verify(constructed.get(0), times(1)).getFontSize();
            verify(constructed.get(0), times(1)).getColorModeSelection();
        }
    }

    /**
     * Test of isChanged method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged Basic");
        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
        boolean expResult = true;

        try (MockedConstruction<ApplicationOptionsPanel> mock = mockConstruction(ApplicationOptionsPanel.class,
                (mockInstance, context) -> {
                    //implement method for mock so null isn't returned
                    when(mockInstance.getUserDirectory()).thenReturn("");
                })) {

            boolean result = instance.isChanged();
            assertEquals(result, expResult);

            // Get a list of all created mocks
            List<ApplicationOptionsPanel> constructed = mock.constructed();
            assertEquals(1, constructed.size());

            // Assert that created panel had run the following method
            verify(constructed.get(0), times(1)).getUserDirectory();
            // Assert that created panel had NOT run the following methods, as the first is false
            verify(constructed.get(0), times(0)).isAutosaveEnabled();
            verify(constructed.get(0), times(0)).getAutosaveFrequency();
            verify(constructed.get(0), times(0)).isWelcomeOnStartupSelected();
            verify(constructed.get(0), times(0)).isWhatsNewOnStartupSelected();
            verify(constructed.get(0), times(0)).getWebserverPort();
            verify(constructed.get(0), times(0)).getNotebookDirectory();
            verify(constructed.get(0), times(0)).getRestDirectory();
            verify(constructed.get(0), times(0)).isDownloadPythonClientSelected();
            verify(constructed.get(0), times(0)).getCurrentFont();
            verify(constructed.get(0), times(0)).getFontSize();
            verify(constructed.get(0), times(0)).isEnableSpellCheckingSelected();
            verify(constructed.get(0), times(0)).getColorModeSelection();
        }
    }

    /**
     * Test of isChanged method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testIsChangedAll() {
        System.out.println("isChanged All");
        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
        final Preferences prefs = NbPreferences.forModule(ApplicationPreferenceKeys.class);
        
        boolean expResult = false;
        
        try (MockedConstruction<ApplicationOptionsPanel> mock = mockConstruction(ApplicationOptionsPanel.class,
                (mockInstance, context) -> {
                    //implement methods for mock so false is returned from isChanged
                    when(mockInstance.getUserDirectory()).thenReturn(prefs.get(ApplicationPreferenceKeys.USER_DIR, ApplicationPreferenceKeys.USER_DIR_DEFAULT));
                    when(mockInstance.isAutosaveEnabled()).thenReturn(prefs.getBoolean(ApplicationPreferenceKeys.AUTOSAVE_ENABLED, ApplicationPreferenceKeys.AUTOSAVE_ENABLED_DEFAULT));
                    when(mockInstance.getAutosaveFrequency()).thenReturn(prefs.getInt(ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE, ApplicationPreferenceKeys.AUTOSAVE_SCHEDULE_DEFAULT));
                    when(mockInstance.isWelcomeOnStartupSelected()).thenReturn(prefs.getBoolean(ApplicationPreferenceKeys.WELCOME_ON_STARTUP, ApplicationPreferenceKeys.WELCOME_ON_STARTUP_DEFAULT));
                    when(mockInstance.isWhatsNewOnStartupSelected()).thenReturn(prefs.getBoolean(ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP, ApplicationPreferenceKeys.TUTORIAL_ON_STARTUP_DEFAULT));
                    when(mockInstance.getWebserverPort()).thenReturn(prefs.getInt(ApplicationPreferenceKeys.WEBSERVER_PORT, ApplicationPreferenceKeys.WEBSERVER_PORT_DEFAULT));
                    when(mockInstance.getNotebookDirectory()).thenReturn(prefs.get(ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR, ApplicationPreferenceKeys.JUPYTER_NOTEBOOK_DIR_DEFAULT));
                    when(mockInstance.getRestDirectory()).thenReturn(prefs.get(ApplicationPreferenceKeys.REST_DIR, ApplicationPreferenceKeys.REST_DIR_DEFAULT));
                    when(mockInstance.isDownloadPythonClientSelected()).thenReturn(prefs.getBoolean(ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD, ApplicationPreferenceKeys.PYTHON_REST_CLIENT_DOWNLOAD_DEFAULT));
                    when(mockInstance.getCurrentFont()).thenReturn(prefs.get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT));
                    when(mockInstance.getFontSize()).thenReturn(prefs.get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT));
                    when(mockInstance.isEnableSpellCheckingSelected()).thenReturn(prefs.getBoolean(ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING, ApplicationPreferenceKeys.ENABLE_SPELL_CHECKING_DEFAULT));
                    when(mockInstance.getColorModeSelection()).thenReturn(prefs.get(ApplicationPreferenceKeys.COLORBLIND_MODE, ApplicationPreferenceKeys.COLORBLIND_MODE_DEFAULT));
                })) {

            boolean result = instance.isChanged();
            assertEquals(result, expResult);

            // Get a list of all created mocks
            List<ApplicationOptionsPanel> constructed = mock.constructed();
            assertEquals(1, constructed.size());

            // Assert that created panel had run the following methods
            verify(constructed.get(0), times(1)).getUserDirectory();
            verify(constructed.get(0), times(1)).isAutosaveEnabled();
            verify(constructed.get(0), times(1)).getAutosaveFrequency();
            verify(constructed.get(0), times(1)).isWelcomeOnStartupSelected();
            verify(constructed.get(0), times(1)).isWhatsNewOnStartupSelected();
            verify(constructed.get(0), times(1)).getWebserverPort();
            verify(constructed.get(0), times(1)).getNotebookDirectory();
            verify(constructed.get(0), times(1)).getRestDirectory();
            verify(constructed.get(0), times(1)).isDownloadPythonClientSelected();
            verify(constructed.get(0), times(1)).getCurrentFont();
            verify(constructed.get(0), times(1)).getFontSize();
            verify(constructed.get(0), times(1)).isEnableSpellCheckingSelected();
            verify(constructed.get(0), times(1)).getColorModeSelection();
        }
    }

    /**
     * Test of addPropertyChangeListener method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testAddPropertyChangeListener() {
        System.out.println("addPropertyChangeListener");
        PropertyChangeListener l = null;

        try (MockedConstruction<PropertyChangeSupport> mock = mockConstruction(PropertyChangeSupport.class)) {
            ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
            instance.addPropertyChangeListener(l);

            // Assert that pcs was made 
            List<PropertyChangeSupport> constructed = mock.constructed();
            assertEquals(constructed.size(), 1);

            verify(constructed.get(0), times(1)).addPropertyChangeListener(l);

        }
    }

    /**
     * Test of removePropertyChangeListener method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testRemovePropertyChangeListener() {
        System.out.println("removePropertyChangeListener");
        PropertyChangeListener l = null;

        try (MockedConstruction<PropertyChangeSupport> mock = mockConstruction(PropertyChangeSupport.class)) {
            ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();
            instance.removePropertyChangeListener(l);

            // Assert that pcs was made 
            List<PropertyChangeSupport> constructed = mock.constructed();
            assertEquals(constructed.size(), 1);

            verify(constructed.get(0), times(1)).removePropertyChangeListener(l);
        }
    }

    /**
     * Test of getComponent method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testGetComponent() {
        System.out.println("getComponent");
        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();

        JComponent result = instance.getComponent(null);
        assertEquals(result.getClass(), ApplicationOptionsPanel.class);
    }

    /**
     * Test of getHelpCtx method, of class ApplicationOptionsPanelController.
     */
    @Test
    public void testGetHelpCtx() {
        System.out.println("getHelpCtx");
        ApplicationOptionsPanelController instance = new ApplicationOptionsPanelController();

        HelpCtx result = instance.getHelpCtx();
        assertEquals(result.getClass(), HelpCtx.class);

    }
}
