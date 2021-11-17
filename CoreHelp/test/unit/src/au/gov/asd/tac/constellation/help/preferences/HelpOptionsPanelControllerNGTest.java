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
package au.gov.asd.tac.constellation.help.preferences;

import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class HelpOptionsPanelControllerNGTest {

    Preferences prefs;

    public HelpOptionsPanelControllerNGTest() {
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
     * Test of update method, of class HelpOptionsPanelController.
     */
    @Test
    public void testUpdateFalse() {
        System.out.println("updateFalse");

        final boolean returnValue = false;
        final String key = HelpPreferenceKeys.HELP_KEY;
        HelpOptionsPanelController instance = mock(HelpOptionsPanelController.class, Mockito.CALLS_REAL_METHODS);
        HelpOptionsPanel panel = mock(HelpOptionsPanel.class, Mockito.CALLS_REAL_METHODS);
        doNothing().when(panel).setOnlineHelpOption(returnValue);
        when(instance.getPanel()).thenReturn(panel);

        prefs = mock(Preferences.class);
        when(prefs.getBoolean(Mockito.eq(key), Mockito.anyBoolean())).thenReturn(returnValue);

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            instance.update();

            // verify that the panel was set with the proper value
            verify(panel, times(1)).setOnlineHelpOption(Mockito.eq(returnValue));

        }

    }

    /**
     * Test of update method, of class HelpOptionsPanelController.
     */
    @Test
    public void testUpdateTrue() {
        System.out.println("updateTrue");

        final boolean returnValue = true;
        final String key = HelpPreferenceKeys.HELP_KEY;
        HelpOptionsPanelController instance = mock(HelpOptionsPanelController.class, Mockito.CALLS_REAL_METHODS);
        HelpOptionsPanel panel = mock(HelpOptionsPanel.class, Mockito.CALLS_REAL_METHODS);
        doNothing().when(panel).setOnlineHelpOption(returnValue);
        when(instance.getPanel()).thenReturn(panel);

        prefs = mock(Preferences.class);
        when(prefs.getBoolean(Mockito.eq(key), Mockito.anyBoolean())).thenReturn(returnValue);

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            instance.update();

            // verify that the panel was set with the proper value
            verify(panel, times(1)).setOnlineHelpOption(Mockito.eq(returnValue));

        }

    }

    /**
     * Test of applyChanges method, of class HelpOptionsPanelController.
     */
    @Test
    public void testApplyChanges() {
        System.out.println("applyChanges");

        final boolean returnValue = true;
        final boolean changedValue = true;
        final boolean validValue = true;
        final String key = HelpPreferenceKeys.HELP_KEY;
        HelpOptionsPanelController instance = mock(HelpOptionsPanelController.class);
        HelpOptionsPanel panel = mock(HelpOptionsPanel.class);

        // do nothing when pcs is called
        PropertyChangeSupport pcs = mock(PropertyChangeSupport.class);
        doNothing().when(pcs).firePropertyChange(Mockito.any(), Mockito.any(), Mockito.any());

        when(panel.isOnlineHelpSelected()).thenReturn(true);
        when(instance.getPanel()).thenReturn(panel);
        when(instance.isChanged()).thenReturn(changedValue);
        when(instance.isValid()).thenReturn(validValue);
        when(instance.getPropertyChangeSupport()).thenReturn(pcs);

        doCallRealMethod().when(instance).applyChanges();

        prefs = mock(Preferences.class);
        doNothing().when(prefs).putBoolean(Mockito.eq(key), Mockito.eq(returnValue));

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            instance.applyChanges();

            // verify checks to changed and valid value
            verify(instance, times(1)).isValid();
            verify(instance, times(1)).isChanged();
            // verify getpanel was called
            verify(instance, times(1)).getPanel();
            // verify that the preference was put with the correct value
            verify(prefs, times(1)).putBoolean(Mockito.eq(key), Mockito.eq(returnValue));
            // verify that the panel method get help option was called
            verify(panel, times(1)).isOnlineHelpSelected();

            // valid value but unchanged
            when(instance.isChanged()).thenReturn(!changedValue);
            when(instance.isValid()).thenReturn(validValue);

            instance.applyChanges();

            // verify checks to changed and valid are called once more
            verify(instance, times(2)).isValid();
            verify(instance, times(2)).isChanged();
            // verify getpanel was not called anymore
            verify(instance, times(1)).getPanel();
            // verify that the preference was not called again
            verify(prefs, times(1)).putBoolean(Mockito.eq(key), Mockito.eq(returnValue));
            // verify that the panel method was not called again
            verify(panel, times(1)).isOnlineHelpSelected();

            // Test invalid value - should not change or interact
            when(instance.isValid()).thenReturn(!validValue);

            instance.applyChanges();

            // verify checks to valid is called once more
            verify(instance, times(3)).isValid();
            // verify changed is not called again
            verify(instance, times(2)).isChanged();
            // verify getpanel was not called anymore
            verify(instance, times(1)).getPanel();
            // verify that the preference was not called again
            verify(prefs, times(1)).putBoolean(Mockito.eq(key), Mockito.eq(returnValue));
            // verify that the panel method was not called again
            verify(panel, times(1)).isOnlineHelpSelected();

            when(panel.isOnlineHelpSelected()).thenReturn(false);
            when(instance.getPanel()).thenReturn(panel);
            // valid value and changed
            when(instance.isChanged()).thenReturn(changedValue);
            when(instance.isValid()).thenReturn(validValue);

            // update mocks and returned values
            doNothing().when(prefs).putBoolean(Mockito.eq(key), Mockito.eq(!returnValue));
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            instance.applyChanges();

            // verify checks to valid is called once more
            verify(instance, times(4)).isValid();
            // verify changed is not called again
            verify(instance, times(3)).isChanged();
            // verify getpanel was not called anymore
            verify(instance, times(2)).getPanel();
            // verify that the preference was called with a different operand
            verify(prefs, times(1)).putBoolean(Mockito.eq(key), Mockito.eq(!returnValue));
            verify(prefs, times(1)).putBoolean(Mockito.eq(key), Mockito.eq(returnValue));
            // verify that the panel method was not called again
            verify(panel, times(2)).isOnlineHelpSelected();

        }
    }

    /**
     * Test of isChanged method, of class HelpOptionsPanelController.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");

        final boolean panelReturnValue = false;
        final boolean returnValue = false;

        final String key = HelpPreferenceKeys.HELP_KEY;
        HelpOptionsPanelController instance = mock(HelpOptionsPanelController.class);
        HelpOptionsPanel panel = mock(HelpOptionsPanel.class);
        when(panel.isOnlineHelpSelected()).thenReturn(panelReturnValue);
        when(instance.getPanel()).thenReturn(panel);
        doCallRealMethod().when(instance).isChanged();

        prefs = mock(Preferences.class);
        when(prefs.getBoolean(Mockito.eq(key), Mockito.anyBoolean())).thenReturn(returnValue);

        // Create static mock of NbPreferences to return the preferences mock
        try (MockedStatic<NbPreferences> mockedStatic = Mockito.mockStatic(NbPreferences.class)) {
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            // TEST VALUES UNCHANGED
            assertEquals(panelReturnValue, panel.isOnlineHelpSelected());
            assertEquals(panelReturnValue != returnValue, instance.isChanged());

            //check value unmodified
            assertEquals(panelReturnValue, panel.isOnlineHelpSelected());
            verify(panel, times(3)).isOnlineHelpSelected();
            verify(instance, times(1)).getPanel();

            // TEST VALUES UNCHANGED TRUE
            // flip operands to give alternate boolean values
            when(panel.isOnlineHelpSelected()).thenReturn(!panelReturnValue);
            when(instance.getPanel()).thenReturn(panel);

            when(prefs.getBoolean(Mockito.eq(key), Mockito.anyBoolean())).thenReturn(!returnValue);
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            assertEquals(!panelReturnValue, panel.isOnlineHelpSelected());
            assertEquals(!panelReturnValue != !returnValue, instance.isChanged());

            //check value unmodified
            assertEquals(!panelReturnValue, panel.isOnlineHelpSelected());
            verify(panel, times(6)).isOnlineHelpSelected();
            verify(instance, times(2)).getPanel();

            // TEST VALUES CHANGED
            // flip operands of online help value
            when(panel.isOnlineHelpSelected()).thenReturn(panelReturnValue);
            when(instance.getPanel()).thenReturn(panel);

            when(prefs.getBoolean(Mockito.eq(key), Mockito.anyBoolean())).thenReturn(!returnValue);
            mockedStatic.when(() -> NbPreferences.forModule(Mockito.eq(HelpPreferenceKeys.class))).thenReturn(prefs);

            assertEquals(panelReturnValue, panel.isOnlineHelpSelected());
            assertEquals(panelReturnValue != !returnValue, instance.isChanged());

            //check value unmodified
            assertEquals(panelReturnValue, panel.isOnlineHelpSelected());
            verify(panel, times(9)).isOnlineHelpSelected();
            verify(instance, times(3)).getPanel();
        }
    }

    /**
     * Test of getHelpCtx method, of class HelpOptionsPanelController.
     */
    @Test
    public void testGetHelpCtx() {
        System.out.println("getHelpCtx");
        HelpOptionsPanelController instance = new HelpOptionsPanelController();
        String expResult = HelpOptionsPanelController.class.getName();
        HelpCtx result = instance.getHelpCtx();
        assertNotNull(result);
        assertEquals(result.getHelpID(), expResult);
    }
}
