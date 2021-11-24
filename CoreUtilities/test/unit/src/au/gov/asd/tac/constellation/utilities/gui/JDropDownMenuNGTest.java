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
package au.gov.asd.tac.constellation.utilities.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionListener;
import static org.mockito.ArgumentMatchers.any;
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
public class JDropDownMenuNGTest {

    private final List<String> items = new ArrayList<>();
    private static MockedStatic<InstalledFileLocator> installedFileLocatorMockedStatic;
    private static InstalledFileLocator installedFileLocatorMocked;

    private static final String COOKIE_ICON_PATH = "au/gov/asd/tac/constellation/utilities/modules/ext/icons/cookie.png";
    private static final String DROP_DOWN_ARROW_ICON_PATH = "au/gov/asd/tac/constellation/utilities/modules/ext/icons/drop_down_arrow.png";
    private static final File iconFile = new File(DROP_DOWN_ARROW_ICON_PATH);

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
        installedFileLocatorMockedStatic.when(() -> InstalledFileLocator.getDefault().locate("modules/ext/icons/drop_down_arrow.png", "au.gov.asd.tac.constellation.utilities", false)).thenReturn(iconFile);

        JDropDownMenu instance = new JDropDownMenu("Text", items);
        String expResult = "Text";

        String result = instance.getText();
        assertEquals(result, expResult);
    }

    /**
     * Test the constructor works with an Icon
     */
    @Test
    public void testConstructor_WithIcon() {
        System.out.println("constructor_WithIcon");
        final Icon icon = new ImageIcon(COOKIE_ICON_PATH);
        final JDropDownMenu instance = new JDropDownMenu(icon, items);
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
            new JDropDownMenu(icon, items);
        }
    }

    /**
     * Test of addButtonActionListener method, of class
     * JDropDownMenu.
     */
    @Test
    public void testaddButtonActionListener() {
        System.out.println("addButtonActionListener");
        final JDropDownMenu instance = new JDropDownMenu("Text", items);

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
     * JDropDownMenu.
     */
    @Test
    public void testAddMenuItemActionListener() {
        System.out.println("addMenuItemActionListener");

        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        final JDropDownMenu instanceSpy = spy(instance);

        final ActionEvent actionEventMock = mock(ActionEvent.class);
        final Map<JMenuItem, String> menuItems = instanceSpy.getMenuItems();
        final JMenuItem newMenuItem = getKeys(menuItems, "Item 3").stream().findFirst().get();

        doReturn(newMenuItem).when(actionEventMock).getSource();
        instanceSpy.addMenuItemActionListener(newMenuItem);
        newMenuItem.getActionListeners()[0].actionPerformed(actionEventMock);

        verify(instanceSpy, times(1)).actionPerformed(any(ActionEvent.class));
    }

    private <K, V> Set<K> getKeys(Map<K, V> map, V value) {
        Set<K> keys = new HashSet<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * Test of getText method, of class JDropDownMenu.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        final String text = "Test text";
        final JDropDownMenu instance = new JDropDownMenu(text, items);
        final String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of setText method, of class JDropDownMenu.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        final String text = "Test text";
        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        instance.setText(text);
        final String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of getIcon method, of class JDropDownMenu.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        final Icon result = instance.getIcon();
        assertNotNull(result);
    }

    /**
     * Test of setIcon method, of class JDropDownMenu.
     */
    @Test
    public void testSetIcon() throws MalformedURLException {
        System.out.println("setIcon");

        final JDropDownMenu instance = new JDropDownMenu("Text", items);

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
     * Test of getItems method, of class JDropDownMenu.
     */
    @Test
    public void testGetItems() {
        System.out.println("getItems");
        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        final Set result = instance.getItems();
        assertEqualsNoOrder(result.toArray(), items.toArray());
    }

    /**
     * Test of setSelectedItem method, of class JDropDownMenu.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        final String item = "Item 2";
        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        final JDropDownMenu instanceSpy = spy(instance);
        instanceSpy.setSelectedItem(item);
        verify(instanceSpy, times(1)).actionPerformed(any(ActionEvent.class));
    }

    /**
     * Test of addActionListener method, of class JDropDownMenu.
     */
    @Test
    public void testAddActionListener() {
        System.out.println("addActionListener");
        final ActionListener actionListenerMock = mock(ActionListener.class);
        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        instance.addActionListener(actionListenerMock);
        final Set<ListSelectionListener> resultListeners = instance.getListeners();
        assertEquals(resultListeners.size(), 1);
        assertEquals(resultListeners.stream().findFirst().get(), actionListenerMock);
    }

    /**
     * Test of actionPerformed method, of class JDropDownMenu.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");

        final ActionListener listSelectionListenerMock1 = mock(ActionListener.class);
        final ActionListener listSelectionListenerMock2 = mock(ActionListener.class);
        final ActionEvent mockEvent = mock(ActionEvent.class);
        final JDropDownMenu instance = new JDropDownMenu("Text", items);

        instance.addActionListener(listSelectionListenerMock1);
        instance.addActionListener(listSelectionListenerMock2);
        instance.actionPerformed(mockEvent);
        verify(listSelectionListenerMock1, times(1)).actionPerformed(mockEvent);
        verify(listSelectionListenerMock2, times(1)).actionPerformed(mockEvent);
    }

    /**
     * Test of setToolTipText method, of class JDropDownMenu.
     */
    @Test
    public void testSetToolTipText() {
        System.out.println("setToolTipText");
        final String text = "Tooltip text to test";
        final JDropDownMenu instance = new JDropDownMenu("Text", items);
        instance.setToolTipText(text);
        final String result = instance.getToolTipText();
        assertEquals(result, text);
    }
}
