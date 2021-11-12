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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
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
    private List<String> items = new ArrayList<String>();
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
        final File iconFile = new File("non_existing_icon.png");
        installedFileLocatorMockedStatic.when(() -> InstalledFileLocator.getDefault()
                .locate("modules/ext/icons/drop_down_arrow.png", "au.gov.asd.tac.constellation.utilities", false))
                .thenReturn(iconFile);

        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        String expResult = "Text";

        String result = instance.getText();
        assertEquals(result, expResult);
    }

    /**
     * Test the constructor works with an Icon
     */
    @Test
    public void testConstructor_WithIcon() {
        final Icon icon = new ImageIcon(COOKIE_ICON_PATH);
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu(icon, items);
        Icon result = instance.getIcon();
        assertNotNull(result);
    }

    /**
     * Test of getText method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        String text = "Test text";
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu(text, items);
        String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of setText method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        String text = "Test text";
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.setText(text);
        String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of getIcon method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        Icon result = instance.getIcon();
        assertNotNull(result);
    }

    /**
     * Test of setIcon method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetIcon() throws MalformedURLException {
        System.out.println("setIcon");

        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);

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
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        Set result = instance.getItems();
        assertEqualsNoOrder(result.toArray(), items.toArray());
    }

    /**
     * Test of setSelectedItem method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        String item = "Item 3";
        JSingleChoiceComboBoxMenu instance = mock(JSingleChoiceComboBoxMenu.class);
        instance.setSelectedItem(item);
        verify(instance, times(1)).setSelectedItem(item);
    }

    /**
     * Test of getSelectedItem method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testGetSelectedItem() {
        System.out.println("getSelectedItem");
        String item = "Item 3";
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.setSelectedItem(item);
        Set resultSet = instance.getSelectedItem();
        assertEquals(resultSet.size(), 1);
        assertEquals(resultSet.stream().findFirst().get().toString(), item);
    }

    /**
     * Test of setToolTipText method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testSetToolTipText() {
        System.out.println("setToolTipText");
        String text = "Tooltip text to test";
        JSingleChoiceComboBoxMenu instance = new JSingleChoiceComboBoxMenu("Text", items);
        instance.setToolTipText(text);
        String result = instance.getToolTipText();
        assertEquals(result, text);
    }

    /**
     * Test of addSelectionListener method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testAddSelectionListener() {
        System.out.println("addSelectionListener");
        ListSelectionListener listSelectionListenerMock = mock(ListSelectionListener.class);
        JSingleChoiceComboBoxMenu instance = mock(JSingleChoiceComboBoxMenu.class);
        instance.addSelectionListener(listSelectionListenerMock);
        verify(instance, times(1)).addSelectionListener(listSelectionListenerMock);
    }

    /**
     * Test of valueChanged method, of class JSingleChoiceComboBoxMenu.
     */
    @Test
    public void testValueChanged() {
        System.out.println("valueChanged");
        JSingleChoiceComboBoxMenu instance = mock(JSingleChoiceComboBoxMenu.class);
        ListSelectionEvent mockEvent = mock(ListSelectionEvent.class);
        instance.valueChanged(mockEvent);
        verify(instance, times(1)).valueChanged(mockEvent);
    }

}
