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
public class JMultiChoiceComboBoxMenuNGTest {

    private final List<String> items = new ArrayList<>();
    private static MockedStatic<InstalledFileLocator> installedFileLocatorMockedStatic;
    private static InstalledFileLocator installedFileLocatorMocked;

    private static final String COOKIE_ICON_PATH = "au/gov/asd/tac/constellation/utilities/modules/ext/icons/cookie.png";
    private static final String DROP_DOWN_ARROW_ICON_PATH = "au/gov/asd/tac/constellation/utilities/modules/ext/icons/drop_down_arrow.png";
    private static final File ICON_FILE = new File(DROP_DOWN_ARROW_ICON_PATH);
    
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
                .thenReturn(ICON_FILE);

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

        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
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
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu(icon, items);
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
            new JMultiChoiceComboBoxMenu(icon, items);
        }
    }

    /**
     * Test of addButtonActionListener method, of class
     * JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testaddButtonActionListener() {
        System.out.println("addButtonActionListener");
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);

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
     * JMultiChoiceComboBoxMenu when the selectedItem contains.
     */
    @Test
    public void testAddMenuItemActionListener_selectedItem_contains() {
        System.out.println("addMenuItemActionListener_selectedItem_contains");

        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        final ActionEvent actionEventMock = mock(ActionEvent.class);
        final Map<JMenuItem, String> menuItems = instance.getMenuItems();
        final String itemSelected = "Item 3";
        final JMenuItem menuItem = getKey(menuItems, itemSelected).stream().findFirst().get();
        final ListSelectionListener listSelectionListenerMock1 = mock(ListSelectionListener.class);

        doReturn(menuItem).when(actionEventMock).getSource();

        instance.addSelectionListener(listSelectionListenerMock1);
        instance.setSelectedItem(itemSelected);

        assertEquals(instance.getSelectedItems().size(), 1);

        menuItem.getActionListeners()[0].actionPerformed(actionEventMock);

        verify(listSelectionListenerMock1, times(1)).valueChanged(any(ListSelectionEvent.class));
        assertEquals(instance.getSelectedItems().size(), 0);
    }

    /**
     * Test of addMenuItemActionListener method, of class
     * JMultiChoiceComboBoxMenu when the selectedItem does not contain.
     */
    @Test
    public void testAddMenuItemActionListener_selectedItem_does_not_contain() {
        System.out.println("addMenuItemActionListener_selectedItem_does_not_contain");

        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        final ActionEvent actionEventMock = mock(ActionEvent.class);
        final Map<JMenuItem, String> menuItems = instance.getMenuItems();
        final String itemSelected = "Item 1";
        final JMenuItem menuItem = getKey(menuItems, "Item 4").stream().findFirst().get();
        final ListSelectionListener listSelectionListenerMock1 = mock(ListSelectionListener.class);

        doReturn(menuItem).when(actionEventMock).getSource();

        instance.addSelectionListener(listSelectionListenerMock1);
        instance.setSelectedItem(itemSelected);

        assertEquals(instance.getSelectedItems().size(), 1);

        menuItem.getActionListeners()[0].actionPerformed(actionEventMock);

        verify(listSelectionListenerMock1, times(1)).valueChanged(any(ListSelectionEvent.class));
        assertEquals(instance.getSelectedItems().size(), 2);
    }

    private <K, V> Set<K> getKey(Map<K, V> map, V value) {
        Set<K> keys = new HashSet<>();
        for (Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * Test of getText method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        final String text = "Test text";
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu(text, items);
        final String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of setText method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        final String text = "Test text";
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        instance.setText(text);
        final String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of getIcon method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        final Icon result = instance.getIcon();
        assertNotNull(result);
    }

    /**
     * Test of setIcon method, of class JMultiChoiceComboBoxMenu.
     * @throws java.net.MalformedURLException
     */
    @Test
    public void testSetIcon() throws MalformedURLException {
        System.out.println("setIcon");
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);

        final Icon newIcon1 = new ImageIcon(Utilities.toURI(ICON_FILE).toURL());
        final Icon newIcon2 = new ImageIcon(DROP_DOWN_ARROW_ICON_PATH);
        instance.setIcon(newIcon1);
        Icon result = instance.getIcon();
        assertEquals(newIcon1, result);

        instance.setIcon(newIcon2);
        result = instance.getIcon();
        assertEquals(newIcon2, result);
    }

    /**
     * Test of getItems method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testGetItems() {
        System.out.println("getItems");
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        final Set<String> result = instance.getItems();
        assertEqualsNoOrder(result.toArray(), items.toArray());
    }

    /**
     * Test of getSelectedItems method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testGetSelectedItems() {
        System.out.println("getSelectedItems");
        final String item = "Item 3";
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        instance.setSelectedItem(item);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.size(), 1);
        assertEquals(resultSet.stream().findFirst().get(), item);
    }

    /**
     * Test of setSelectedItem method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        final String item = "Item 3";
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        assertEquals(instance.getSelectedItems().size(), 0);
        instance.setSelectedItem(item);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.stream().findFirst().get(), item);
    }

    /**
     * Test of setSelectedItems method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testSetSelectedItems() {
        final String s1 = "Item 1";
        final String s2 = "Item 3";
        final String s3 = "Item 4";
        final String[] selectedItems = {s1, s2, s3};
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        assertEquals(instance.getSelectedItems().size(), 0);
        instance.setSelectedItems(s1, s2, s3);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.size(), 3);
        assertEqualsNoOrder(resultSet.toArray(), selectedItems);
    }

    /**
     * Test of addSelectedItem method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testAddSelectedItem() {
        System.out.println("addSelectedItem");
        final String item = "Item 3";
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        instance.addSelectedItem(item);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.size(), 1);
        assertEquals(resultSet.stream().findFirst().get(), item);
    }

    /**
     * Test of addSelectedItems method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testAddSelectedItems() {
        System.out.println("addSelectedItems");
        final String s1 = "Item 1";
        final String s2 = "Item 3";
        final String s3 = "Item 4";
        final String[] selectedItems = {s1, s2, s3};
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        instance.addSelectedItems(s1, s2, s3);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.size(), 3);
        assertEqualsNoOrder(resultSet.toArray(), selectedItems);
    }

    /**
     * Test of removeSelectedItem method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testRemoveSelectedItem() {
        System.out.println("removeSelectedItem");
        final String s1 = "Item 1";
        final String s2 = "Item 3";
        final String s3 = "Item 4";
        final String[] selectedItems = {s1, s2, s3};
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);

        instance.addSelectedItems(s1, s2, s3);
        final Set<String> itemsBeforeRemove = instance.getSelectedItems();
        assertEquals(itemsBeforeRemove.size(), 3);
        assertEqualsNoOrder(itemsBeforeRemove.toArray(), selectedItems);

        instance.removeSelectedItem(s2);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.size(), 2);
        assertEqualsNoOrder(resultSet.toArray(), new String[]{s1, s3});
    }

    /**
     * Test of removeSelectedItems method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testRemoveSelectedItems() {
        System.out.println("removeSelectedItems");
        final String s1 = "Item 1";
        final String s2 = "Item 3";
        final String s3 = "Item 4";
        final String[] selectedItems = {s1, s2, s3};
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);

        instance.addSelectedItems(s1, s2, s3);
        final Set<String> itemsBeforeRemove = instance.getSelectedItems();
        assertEquals(itemsBeforeRemove.size(), 3);
        assertEqualsNoOrder(itemsBeforeRemove.toArray(), selectedItems);

        instance.removeSelectedItems(s2, s3);
        final Set<String> resultSet = instance.getSelectedItems();
        assertEquals(resultSet.size(), 1);
        assertEquals(resultSet.toArray(), new String[]{s1});
    }

    /**
     * Test of clearSelection method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testClearSelection() {
        System.out.println("clearSelection");
        final String s1 = "Item 1";
        final String s2 = "Item 3";
        final String s3 = "Item 4";
        final String[] selectedItems = {s1, s2, s3};
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);

        instance.addSelectedItems(s1, s2, s3);
        final Set<String> itemsBeforeRemove = instance.getSelectedItems();
        assertEquals(itemsBeforeRemove.size(), 3);
        assertEqualsNoOrder(itemsBeforeRemove.toArray(), selectedItems);

        instance.clearSelection();
        final Set<String> resultSet = instance.getSelectedItems();
        assertTrue(resultSet.isEmpty());
    }

    /**
     * Test of addSelectionListener method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testAddSelectionListener() {
        System.out.println("addSelectionListener");
        final ListSelectionListener listSelectionListenerMock = mock(ListSelectionListener.class);
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        instance.addSelectionListener(listSelectionListenerMock);
        final Set<ListSelectionListener> resultListeners = instance.getListeners();
        assertEquals(resultListeners.size(), 1);
        assertEquals(resultListeners.stream().findFirst().get(), listSelectionListenerMock);
    }

    /**
     * Test of valueChanged method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testValueChanged() {
        System.out.println("valueChanged");
        final ListSelectionListener listSelectionListenerMock1 = mock(ListSelectionListener.class);
        final ListSelectionListener listSelectionListenerMock2 = mock(ListSelectionListener.class);
        final ListSelectionEvent mockEvent = mock(ListSelectionEvent.class);
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);

        instance.addSelectionListener(listSelectionListenerMock1);
        instance.addSelectionListener(listSelectionListenerMock2);
        instance.valueChanged(mockEvent);
        verify(listSelectionListenerMock1, times(1)).valueChanged(mockEvent);
        verify(listSelectionListenerMock2, times(1)).valueChanged(mockEvent);
    }

    /**
     * Test of setToolTipText method, of class JMultiChoiceComboBoxMenu.
     */
    @Test
    public void testSetToolTipText() {
        System.out.println("setToolTipText");
        final String text = "Tooltip text to test";
        final JMultiChoiceComboBoxMenu instance = new JMultiChoiceComboBoxMenu("Text", items);
        instance.setToolTipText(text);
        final String result = instance.getToolTipText();
        assertEquals(result, text);
    }
}
