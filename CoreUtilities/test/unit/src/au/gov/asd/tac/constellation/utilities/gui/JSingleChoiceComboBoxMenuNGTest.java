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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class JSingleChoiceComboBoxMenuNGTest {

    private final List<String> items = new ArrayList<>();
    private static MockedStatic<InstalledFileLocator> installedFileLocatorMockedStatic;
    private static InstalledFileLocator installedFileLocatorMocked;

    private static final String COOKIE_ICON_PATH = "au/gov/asd/tac/constellation/utilities/modules/ext/icons/cookie.png";
    private static final String DROP_DOWN_ARROW_ICON_PATH = "au/gov/asd/tac/constellation/utilities/modules/ext/icons/drop_down_arrow.png";
    private static final File iconFile = new File(DROP_DOWN_ARROW_ICON_PATH);

    public JSingleChoiceComboBoxMenuNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        installedFileLocatorMocked = mock(InstalledFileLocator.class);
        installedFileLocatorMockedStatic = Mockito.mockStatic(InstalledFileLocator.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        installedFileLocatorMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        installedFileLocatorMockedStatic.when(InstalledFileLocator::getDefault)
                .thenReturn(installedFileLocatorMocked);
        installedFileLocatorMockedStatic.when(() -> InstalledFileLocator.getDefault()
                .locate("modules/ext/icons/drop_down_arrow.png", "au.gov.asd.tac.constellation.utilities", false))
                .thenReturn(iconFile);

        items.clear();
        items.add("Item 1");
        items.add("Item 2");
        items.add("Item 3");
        items.add("Item 4");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        installedFileLocatorMockedStatic.reset();
    }

    /**
     * Test the constructor works with a Text
     */
    @Test
    public void testConstructor_WithText() {
        System.out.println("constructor_WithText");
        final File iconFile = new File("non_existing_icon.png");
        installedFileLocatorMockedStatic.when(() -> InstalledFileLocator.getDefault()
                .locate(anyString(), anyString(), anyBoolean()))
                .thenReturn(iconFile);

        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        final String expResult = "Text";

        final String result = instance.getText();
        assertEquals(result, expResult);
    }

    /**
     * Test the constructor works with an Icon
     */
    @Test
    public void testConstructor_WithIcon() {
        System.out.println("constructor_WithIcon");
        final Icon icon = new ImageIcon(COOKIE_ICON_PATH);
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu(icon, items);
        final CompoundIcon result = (CompoundIcon) instance.getIcon();
        assertEquals(result.getIconCount(), 2);
        final List<Icon> icons = new ArrayList<>();
        icons.add(result.getIcon(0));
        icons.add(result.getIcon(1));
        assertTrue(icons.contains(icon));
    }

    /**
     * Test the constructor Triggers Assertion Error when the arrow icon cannot
     * be generated.
     */
    @Test(expectedExceptions = AssertionError.class)
    public void testConstructor_WithIcon_ThrowsMalformedURLExceptionTriggersAssertionError() throws MalformedURLException {
        System.out.println("constructor_WithIcon_ThrowsMalformedURLExceptionTriggersAssertionError");
        try (final MockedStatic<Utilities> utilitiesMockStatic = Mockito.mockStatic(Utilities.class);) {
            final Icon icon = new ImageIcon(COOKIE_ICON_PATH);
            final URI uriMock = mock(URI.class);
            utilitiesMockStatic.when(() -> Utilities.toURI(any(File.class)))
                    .thenReturn(uriMock);
            when(uriMock.toURL()).thenThrow(MalformedURLException.class);
            new JSingleChoiceComboBoxMenu(icon, items);
        }
    }

    /**
     * Test of addButtonActionListener method, of class
     * JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testaddButtonActionListener() {
        System.out.println("addButtonActionListener");
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);

        final JButton button = instance.getButton();
        final JPopupMenu menu = instance.getMenu();

        final JButton spyJButton = spy(button);
        final JPopupMenu spyMenu = spy(menu);

        final Point p = mock(Point.class);
        doReturn(p).when(spyJButton).getLocationOnScreen();

        instance.addButtonActionListener(spyJButton, spyMenu);

        spyMenu.setVisible(false);
        spyJButton.getActionListeners()[0].actionPerformed(null);
        assertTrue(spyMenu.isVisible());

        spyMenu.setVisible(true);
        spyJButton.getActionListeners()[0].actionPerformed(null);
        assertFalse(spyMenu.isVisible());
    }

    /**
     * Test of addMenuItemActionListener method, of class
     * JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testAddMenuItemActionListener() {
        System.out.println("addMenuItemActionListener");

        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        final ActionEvent actionEventMock = mock(ActionEvent.class);
        final Map<JMenuItem, String> menuItems = instance.getMenuItems();
        final String itemSelected = "Item 3";
        final JMenuItem newMenuItem = getKeys(menuItems, "Item 4").stream().findFirst().get();
        final ListSelectionListener listSelectionListenerMock1 = mock(ListSelectionListener.class);

        doReturn(newMenuItem).when(actionEventMock).getSource();

        instance.addSelectionListener(listSelectionListenerMock1);
        instance.setSelectedItem(itemSelected);

        assertEquals(instance.getSelectedItem().stream().findFirst().get().toString(), itemSelected);

        newMenuItem.getActionListeners()[0].actionPerformed(actionEventMock);

        final Set<String> resultSet = instance.getSelectedItem();
        assertEquals(resultSet.stream().findFirst().get().toString(), "Item 4");
        verify(listSelectionListenerMock1, times(1)).valueChanged(any(ListSelectionEvent.class));
    }

    private <K, V> Set<K> getKeys(Map<K, V> map, V value) {
        Set<K> keys = new HashSet<>();
        for (Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * Test of getText method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        final String text = "Test text";
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu(text, items);
        final String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of setText method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        final String text = "Test text";
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.setText(text);
        final String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of getIcon method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        final Icon result = instance.getIcon();
        assertNotNull(result);
    }

    /**
     * Test of setIcon method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetIcon() throws MalformedURLException {
        System.out.println("setIcon");

        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);

        final Icon newIcon1 = new ImageIcon(Utilities.toURI(iconFile).toURL());
        final Icon newIcon2 = new ImageIcon(DROP_DOWN_ARROW_ICON_PATH);
        instance.setIcon(newIcon1);
        Icon result = instance.getIcon();
        assertEquals(newIcon1, result);

        instance.setIcon(newIcon2);
        result = instance.getIcon();
        assertEquals(newIcon2, result);
    }

    /**
     * Test of getItems method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetItems() {
        System.out.println("getItems");
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        final Set result = instance.getItems();
        assertEqualsNoOrder(result.toArray(), items.toArray());
    }

    /**
     * Test of setSelectedItem method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        final String item = "Item 3";
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        assertTrue(instance.getSelectedItem().isEmpty());
        instance.setSelectedItem(item);
        final Set<String> resultSet = instance.getSelectedItem();
        assertEquals(resultSet.stream().findFirst().get().toString(), item);
    }

    /**
     * Test of getSelectedItem method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetSelectedItem() {
        System.out.println("getSelectedItem");
        final String item = "Item 3";
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.setSelectedItem(item);
        final Set resultSet = instance.getSelectedItem();
        assertEquals(resultSet.size(), 1);
        assertEquals(resultSet.stream().findFirst().get().toString(), item);
    }

    /**
     * Test of setToolTipText method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetToolTipText() {
        System.out.println("setToolTipText");
        final String text = "Tooltip text to test";
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.setToolTipText(text);
        final String result = instance.getToolTipText();
        assertEquals(result, text);
    }

    /**
     * Test of addSelectionListener method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testAddSelectionListener() {
        System.out.println("addSelectionListener");

        final ListSelectionListener listSelectionListenerMock = mock(ListSelectionListener.class);
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.addSelectionListener(listSelectionListenerMock);
        final Set<ListSelectionListener> resultListeners = instance.getListeners();
        assertEquals(resultListeners.size(), 1);
        assertEquals(resultListeners.stream().findFirst().get(), listSelectionListenerMock);
    }

    /**
     * Test of valueChanged method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testValueChanged() {
        System.out.println("valueChanged");

        final ListSelectionListener listSelectionListenerMock1 = mock(ListSelectionListener.class);
        final ListSelectionListener listSelectionListenerMock2 = mock(ListSelectionListener.class);
        final ListSelectionEvent mockEvent = mock(ListSelectionEvent.class);
        final JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);

        instance.addSelectionListener(listSelectionListenerMock1);
        instance.addSelectionListener(listSelectionListenerMock2);
        instance.valueChanged(mockEvent);
        verify(listSelectionListenerMock1, times(1)).valueChanged(mockEvent);
        verify(listSelectionListenerMock2, times(1)).valueChanged(mockEvent);
    }
}
