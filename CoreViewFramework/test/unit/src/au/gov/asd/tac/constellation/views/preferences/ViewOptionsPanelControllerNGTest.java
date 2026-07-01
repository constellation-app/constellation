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
package au.gov.asd.tac.constellation.views.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import org.mockito.MockedConstruction;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * Test class of ViewOptionsPanelController.
 *
 * @author sol695510
 */
public class ViewOptionsPanelControllerNGTest {

    final Map<String, Boolean> prefsAllFalse = Map.ofEntries(
            entry("Analytic View", Boolean.FALSE),
            entry("Attribute Editor", Boolean.FALSE),
            entry("Conversation View", Boolean.FALSE),
            entry("Data Access View", Boolean.FALSE),
            entry("Error Report", Boolean.FALSE));

    final Map<String, Boolean> prefsAllTrue = Map.ofEntries(
            entry("Analytic View", Boolean.TRUE),
            entry("Attribute Editor", Boolean.TRUE),
            entry("Conversation View", Boolean.TRUE),
            entry("Data Access View", Boolean.TRUE),
            entry("Error Report", Boolean.TRUE));

    /**
     * Test of update method, of class ViewOptionsPanelController.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");

        try (MockedConstruction<ViewOptionsPanel> mockVOP = mockConstruction(ViewOptionsPanel.class)) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.update();

            // Assert that a mock of the ViewOptionsPanel was constructed.
            final List<ViewOptionsPanel> constructedVOP = mockVOP.constructed();
            assertEquals(constructedVOP.size(), 1);

            // Verify that these methods were run on the constructed mock.
            verify(constructedVOP.get(0), times(1)).fireTableDataChanged();
            verify(constructedVOP.get(0), times(1)).createTableModel();
        }
    }

    /**
     * Test of applyChanges method, of class ViewOptionsPanelController.
     */
    @Test
    public void testApplyChanges() {
        System.out.println("applyChanges");

        // When isChanged() returns true.
        try (MockedConstruction<ViewOptionsPanel> mockVOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllTrue);
        }); MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.applyChanges();

            // Assert that a mock of the ViewOptionsPanel was constructed.
            final List<ViewOptionsPanel> constructedVOP = mockVOP.constructed();
            assertEquals(constructedVOP.size(), 1);

            // Assert that a mock of the PropertyChangeSupport was constructed.
            final List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
            assertEquals(constructedPCS.size(), 1);

            // Verify that this method was run during isValid().
            verify(constructedPCS.get(0), times(1)).firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            // Verify that these methods were run during isChanged().
            verify(constructedVOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVOP.get(0), times(1)).getOptionsFromUI();

            // Verify that this method was run due to isChanged() returning true.
            verify(constructedPCS.get(0), times(1)).firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }

        // When isChanged() returns false.
        try (MockedConstruction<ViewOptionsPanel> mockVOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllFalse);
        }); MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.applyChanges();

            // Assert that a mock of the ViewOptionsPanel was not constructed.
            final List<ViewOptionsPanel> constructedVOP = mockVOP.constructed();
            assertEquals(constructedVOP.size(), 1);

            // Assert that a mock of the PropertyChangeSupport was constructed.
            final List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
            assertEquals(constructedPCS.size(), 1);

            // Verify that this method was run during isValid().
            verify(constructedPCS.get(0), times(1)).firePropertyChange(OptionsPanelController.PROP_VALID, null, null);

            // Verify that these methods were run during isChanged().
            verify(constructedVOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVOP.get(0), times(1)).getOptionsFromUI();

            // Verify that this method was not run due to isChanged() returning false.
            verify(constructedPCS.get(0), times(0)).firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
    }

    /**
     * Test of isChanged method, of class ViewOptionsPanelController.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");

        // When the options from the NbPreferences and UI differ.
        try (MockedConstruction<ViewOptionsPanel> mockVOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllTrue);
        })) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            final boolean result = instance.isChanged();
            final boolean expResult = true;
            assertEquals(result, expResult);

            // Assert that a mock of the ViewOptionsPanel was constructed.
            final List<ViewOptionsPanel> constructed = mockVOP.constructed();
            assertEquals(constructed.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructed.get(0), times(1)).getOptionsFromPrefs();
            verify(constructed.get(0), times(1)).getOptionsFromUI();
        }

        // When the options from the NbPreferences and UI match.
        try (MockedConstruction<ViewOptionsPanel> mockVOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllFalse);
        })) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            final boolean result = instance.isChanged();
            final boolean expResult = false;
            assertEquals(result, expResult);

            // Assert that a mock of the ViewOptionsPanel was constructed.
            final List<ViewOptionsPanel> constructed = mockVOP.constructed();
            assertEquals(constructed.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructed.get(0), times(1)).getOptionsFromPrefs();
            verify(constructed.get(0), times(1)).getOptionsFromUI();
        }
    }

    /**
     * Test of getComponent method, of class ViewOptionsPanelController.
     */
    @Test
    public void testGetComponent() {
        System.out.println("getComponent");
        final ViewOptionsPanelController instance = new ViewOptionsPanelController();

        final Object result = instance.getComponent(Lookup.EMPTY);
        assertEquals(result.getClass(), ViewOptionsPanel.class);
    }

    /**
     * Test of getHelpCtx method, of class ViewOptionsPanelController.
     */
    @Test
    public void testGetHelpCtx() {
        System.out.println("getHelpCtx");
        final ViewOptionsPanelController instance = new ViewOptionsPanelController();

        final Object result1 = instance.getHelpCtx();
        assertEquals(result1.getClass(), HelpCtx.class);

        final HelpCtx result2 = instance.getHelpCtx();
        assertEquals(result2.getHelpID(), "au.gov.asd.tac.constellation.views.preferences");
    }

    /**
     * Test of addPropertyChangeListener of class ViewOptionsPanelController.
     */
    @Test
    public void testAddPropertyChangeListener() {
        System.out.println("addPropertyChangeListener");
        final PropertyChangeListener pcl = null;

        try (MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.addPropertyChangeListener(pcl);

            // Assert that a mock of the PropertyChangeSupport was constructed.
            final List<PropertyChangeSupport> constructed = mockPCS.constructed();
            assertEquals(constructed.size(), 1);

            verify(constructed.get(0), times(1)).addPropertyChangeListener(pcl);
        }
    }

    /**
     * Test of removePropertyChangeListener methods of class ViewOptionsPanelController.
     */
    @Test
    public void testRemovePropertyChangeListener() {
        System.out.println("removePropertyChangeListener");
        final PropertyChangeListener pcl = null;

        try (MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {

            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.removePropertyChangeListener(pcl);

            // Assert that a mock of the PropertyChangeSupport was constructed.
            final List<PropertyChangeSupport> constructed = mockPCS.constructed();
            assertEquals(constructed.size(), 1);

            verify(constructed.get(0), times(1)).removePropertyChangeListener(pcl);
        }
    }
}
