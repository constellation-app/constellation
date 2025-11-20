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
package au.gov.asd.tac.constellation.preferences;

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class of ViewOptionsPanelController.
 *
 * @author sol695510
 */
public class ViewOptionsPanelControllerNGTest {

    final Map<String, Boolean> viewOptionsDefault = Map.ofEntries(
            entry("Analytic View", Boolean.FALSE),
            entry("Attribute Editor", Boolean.FALSE),
            entry("Conversation View", Boolean.FALSE),
            entry("Data Access View", Boolean.FALSE),
            entry("Error Report", Boolean.FALSE),
            entry("Find and Replace", Boolean.TRUE),
            entry("Histogram", Boolean.FALSE),
            entry("Layers View", Boolean.FALSE),
            entry("Map View", Boolean.FALSE),
            entry("Named Selections", Boolean.FALSE),
            entry("Notes View", Boolean.FALSE),
            entry("Plugin Reporter", Boolean.FALSE),
            entry("Quality Control View", Boolean.FALSE),
            entry("Scatter Plot", Boolean.FALSE),
            entry("Schema View", Boolean.FALSE),
            entry("Scripting View", Boolean.FALSE),
            entry("Table View", Boolean.FALSE),
            entry("Timeline", Boolean.FALSE),
            entry("Simple Graph", Boolean.FALSE),
            entry("Perspective Bookmarks", Boolean.FALSE),
            entry("Plane Manager", Boolean.FALSE),
            entry("Memory Manager", Boolean.FALSE),
            entry("Word Cloud View", Boolean.FALSE),
            entry("Hierarchical", Boolean.FALSE),
            entry("K-Truss", Boolean.FALSE));

    final Map<String, Boolean> viewOptionsAllTrue = Map.ofEntries(
            entry("Analytic View", Boolean.TRUE),
            entry("Attribute Editor", Boolean.TRUE),
            entry("Conversation View", Boolean.TRUE),
            entry("Data Access View", Boolean.TRUE),
            entry("Error Report", Boolean.TRUE),
            entry("Find and Replace", Boolean.TRUE),
            entry("Histogram", Boolean.TRUE),
            entry("Layers View", Boolean.TRUE),
            entry("Map View", Boolean.TRUE),
            entry("Named Selections", Boolean.TRUE),
            entry("Notes View", Boolean.TRUE),
            entry("Plugin Reporter", Boolean.TRUE),
            entry("Quality Control View", Boolean.TRUE),
            entry("Scatter Plot", Boolean.TRUE),
            entry("Schema View", Boolean.TRUE),
            entry("Scripting View", Boolean.TRUE),
            entry("Table View", Boolean.TRUE),
            entry("Timeline", Boolean.TRUE),
            entry("Simple Graph", Boolean.TRUE),
            entry("Perspective Bookmarks", Boolean.TRUE),
            entry("Plane Manager", Boolean.TRUE),
            entry("Memory Manager", Boolean.TRUE),
            entry("Word Cloud View", Boolean.TRUE),
            entry("Hierarchical", Boolean.TRUE),
            entry("K-Truss", Boolean.TRUE));

    public ViewOptionsPanelControllerNGTest() {
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
     * Test of update method, of class ViewFloatingOptionsPanelController.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        final ViewFloatingOptionsPanelController instance = new ViewFloatingOptionsPanelController();

        try (MockedConstruction<ViewFloatingOptionsPanel> mock = mockConstruction(ViewFloatingOptionsPanel.class)) {
            instance.update();

            // Assert that a mock of the panel was constructed.
            final List<ViewFloatingOptionsPanel> constructed = mock.constructed();
            assertEquals(constructed.size(), 1);

            // Verify that this method was run on the constructed panel.
            verify(constructed.get(0), times(1)).fireTableDataChanged();
        }
    }

    /**
     * Test of applyChanges method, of class ViewFloatingOptionsPanelController.
     */
    @Test
    public void testApplyChanges() {
        System.out.println("applyChanges");
        final ViewFloatingOptionsPanelController instance = new ViewFloatingOptionsPanelController();

        // When isChanged() returns true.
        try (MockedConstruction<ViewFloatingOptionsPanel> mockVFOP = mockConstruction(ViewFloatingOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(viewOptionsDefault);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(viewOptionsAllTrue);
        })) {

            instance.applyChanges();

            // Assert that a mock of the panel was constructed.
            final List<ViewFloatingOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructedVFOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVFOP.get(0), times(1 + viewOptionsDefault.size())).getOptionsFromUI();
        }

        // When isChanged() returns false.
        try (MockedConstruction<ViewFloatingOptionsPanel> mockVFOP = mockConstruction(ViewFloatingOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(viewOptionsDefault);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(viewOptionsDefault);
        })) {

            instance.applyChanges();

            // Assert that a mock of the panel was not constructed.
            final List<ViewFloatingOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 0);
        }
    }

    /**
     * Test of isChanged method, of class ViewFloatingOptionsPanelController.
     */
    @Test
    public void testIsChanged() {
        System.out.println("isChanged");
        final ViewFloatingOptionsPanelController instance1 = new ViewFloatingOptionsPanelController();
        final ViewFloatingOptionsPanelController instance2 = new ViewFloatingOptionsPanelController();

        // When the options from the Preferences and UI differ.
        try (MockedConstruction<ViewFloatingOptionsPanel> mockVFOP = mockConstruction(ViewFloatingOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(viewOptionsDefault);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(viewOptionsAllTrue);
        })) {

            final boolean result = instance1.isChanged();
            final boolean expResult = true;
            assertEquals(result, expResult);

            // Assert that a mock of the panel was constructed.
            final List<ViewFloatingOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructedVFOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVFOP.get(0), times(1)).getOptionsFromUI();
        }

        // When the options from the Preferences and UI match.
        try (MockedConstruction<ViewFloatingOptionsPanel> mockVFOP = mockConstruction(ViewFloatingOptionsPanel.class, (mockInstance, context) -> {
            when(mockInstance.getOptionsFromUI()).thenReturn(viewOptionsDefault);
            when(mockInstance.getOptionsFromPrefs()).thenReturn(viewOptionsDefault);
        })) {

            final boolean result = instance2.isChanged();
            final boolean expResult = false;
            assertEquals(result, expResult);

            // Assert that a mock of the panel was constructed.
            final List<ViewFloatingOptionsPanel> constructedVFOP = mockVFOP.constructed();
            assertEquals(constructedVFOP.size(), 1);

            // Verify that these methods were run during isChanged().
            verify(constructedVFOP.get(0), times(1)).getOptionsFromPrefs();
            verify(constructedVFOP.get(0), times(1)).getOptionsFromUI();
        }
    }

    /**
     * Test of getHelpCtx method, of class ViewFloatingOptionsPanelController.
     */
    @Test
    public void testGetHelpCtx() {
        System.out.println("getHelpCtx");
        final ViewFloatingOptionsPanelController instance = new ViewFloatingOptionsPanelController();

        final HelpCtx result = instance.getHelpCtx();
        assertEquals(result.getClass(), HelpCtx.class);
    }

    /**
     * Test of addPropertyChangeListener and removePropertyChangeListener methods, of class
     * ViewFloatingOptionsPanelController.
     */
    @Test
    public void testAddRemovePropertyChangeListener() {
        System.out.println("addRemovePropertyChangeListener");
        final PropertyChangeListener pcl = null;

        // Test adding the Property Change Listener.
        try (MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {
            final ViewFloatingOptionsPanelController instance = new ViewFloatingOptionsPanelController();
            instance.addPropertyChangeListener(pcl);

            // Assert that a mock of the PCS was constructed.
            final List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
            assertEquals(constructedPCS.size(), 1);

            verify(constructedPCS.get(0), times(1)).addPropertyChangeListener(pcl);
        }

        // Test removing the Property Change Listener.
        try (MockedConstruction<PropertyChangeSupport> mockPCS = mockConstruction(PropertyChangeSupport.class)) {
            final ViewFloatingOptionsPanelController instance = new ViewFloatingOptionsPanelController();
            instance.removePropertyChangeListener(pcl);

            // Assert that a mock of the PCS was constructed.
            final List<PropertyChangeSupport> constructedPCS = mockPCS.constructed();
            assertEquals(constructedPCS.size(), 1);

            verify(constructedPCS.get(0), times(1)).removePropertyChangeListener(pcl);
        }
    }

    /**
     * Test of getPanel method, of class ViewFloatingOptionsPanelController.
     */
    @Test
    public void testGetPanel() {
        System.out.println("getPanel");
        final ViewFloatingOptionsPanelController instance = new ViewFloatingOptionsPanelController();

        final ViewFloatingOptionsPanel result = instance.getPanel();
        assertEquals(result.getClass(), ViewFloatingOptionsPanel.class);
    }
}
