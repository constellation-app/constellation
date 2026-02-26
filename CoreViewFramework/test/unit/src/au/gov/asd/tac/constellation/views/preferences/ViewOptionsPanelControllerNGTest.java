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
import org.openide.util.HelpCtx;
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
        final ViewOptionsPanelController instance = new ViewOptionsPanelController();

        try (MockedConstruction<ViewOptionsPanel> mock = mockConstruction(ViewOptionsPanel.class)) {
            instance.update();

            // Assert that a mock of the panel was constructed.
            final List<ViewOptionsPanel> constructed = mock.constructed();
            assertEquals(constructed.size(), 1);

            // Verify that this method was run on the constructed panel.
            verify(constructed.get(0), times(1)).fireTableDataChanged();
        }
    }

    /**
     * Test of applyChanges method, of class ViewOptionsPanelController.
     */
    @Test
    public void testApplyChanges() {
        System.out.println("applyChanges");
        final ViewOptionsPanelController instance = new ViewOptionsPanelController();

        // When isChanged() returns true.
        try (MockedConstruction<ViewOptionsPanel> mockVFOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllTrue);
        })) {

            instance.applyChanges();

            // Assert that a mock of the panel was constructed.
            final List<ViewOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructedVFOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVFOP.get(0), times(1)).getOptionsFromUI();
        }

        // When isChanged() returns false.
        try (MockedConstruction<ViewOptionsPanel> mockVFOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllFalse);
        })) {

            instance.applyChanges();

            // Assert that a mock of the panel was not constructed.
            final List<ViewOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 0);
        }
    }

    /**
     * Test of isChanged method, of class ViewOptionsPanelController.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");
        final ViewOptionsPanelController instance1 = new ViewOptionsPanelController();
        final ViewOptionsPanelController instance2 = new ViewOptionsPanelController();

        // When the options from the Preferences and UI differ.
        try (MockedConstruction<ViewOptionsPanel> mockVFOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllTrue);
        })) {

            final boolean result = instance1.isChanged();
            final boolean expResult = true;
            assertEquals(result, expResult);

            // Assert that a mock of the panel was constructed.
            final List<ViewOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructedVFOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVFOP.get(0), times(1)).getOptionsFromUI();
        }

        // When the options from the Preferences and UI match.
        try (MockedConstruction<ViewOptionsPanel> mockVFOP = mockConstruction(ViewOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(prefsAllFalse);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(prefsAllFalse);
        })) {

            final boolean result = instance2.isChanged();
            final boolean expResult = false;
            assertEquals(result, expResult);

            // Assert that a mock of the panel was constructed.
            final List<ViewOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructedVFOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVFOP.get(0), times(1)).getOptionsFromUI();
        }
    }

    /**
     * Test of getHelpCtx method, of class ViewOptionsPanelController.
     */
    @Test
    public void testGetHelpCtx() {
        System.out.println("getHelpCtx");
        final ViewOptionsPanelController instance = new ViewOptionsPanelController();

        final HelpCtx result = instance.getHelpCtx();
        assertEquals(result.getClass(), HelpCtx.class);
    }

    /**
     * Test of addPropertyChangeListener and removePropertyChangeListener methods, of class ViewOptionsPanelController.
     */
    @Test
    public void testAddRemovePropertyChangeListener() {
        System.out.println("addRemovePropertyChangeListener");
        final PropertyChangeListener pcl = null;

        // Test adding the Property Change Listener.
        try (MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {
            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.addPropertyChangeListener(pcl);

            // Assert that a mock of the PCS was constructed.
            final List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
            assertEquals(constructedPCS.size(), 3);

            verify(constructedPCS.get(0), times(1)).addPropertyChangeListener(pcl);
        }

        // Test removing the Property Change Listener.
        try (MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {
            final ViewOptionsPanelController instance = new ViewOptionsPanelController();
            instance.removePropertyChangeListener(pcl);

            // Assert that a mock of the PCS was constructed.
            final List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
            assertEquals(constructedPCS.size(), 1);

            verify(constructedPCS.get(0), times(1)).removePropertyChangeListener(pcl);
        }
    }

    /**
     * Test of getPanel method, of class ViewOptionsPanelController.
     */
    @Test
    public void testGetPanel() {
        System.out.println("getPanel");
        final ViewOptionsPanelController instance = new ViewOptionsPanelController();

        final ViewOptionsPanel result = instance.getPanel();
        assertEquals(result.getClass(), ViewOptionsPanel.class);
    }
}
