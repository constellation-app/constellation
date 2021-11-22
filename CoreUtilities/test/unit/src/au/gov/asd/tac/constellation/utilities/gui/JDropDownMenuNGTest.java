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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
        JDropDownMenu instance = new JDropDownMenu(icon, items);
        CompoundIcon result = (CompoundIcon) instance.getIcon();
        assertEquals(result.getIconCount(), 2);

        final List<Icon> icons = new ArrayList<>();
        icons.add(result.getIcon(0));
        icons.add(result.getIcon(1));
        assertTrue(icons.contains(icon));
    }

    /**
     * Test of getText method, of class JDropDownMenu.
     */
    @Test
    public void testGetText() {
        System.out.println("getText");
        String text = "Test text";
        JDropDownMenu instance = new JDropDownMenu(text, items);
        String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of setText method, of class JDropDownMenu.
     */
    @Test
    public void testSetText() {
        System.out.println("setText");
        String text = "Test text";
        JDropDownMenu instance = new JDropDownMenu("Text", items);
        instance.setText(text);
        String result = instance.getText();
        assertEquals(result, text);
    }

    /**
     * Test of getIcon method, of class JDropDownMenu.
     */
    @Test
    public void testGetIcon() {
        System.out.println("getIcon");
        JDropDownMenu instance = new JDropDownMenu("Text", items);
        Icon result = instance.getIcon();
        assertNotNull(result);
    }

    /**
     * Test of setIcon method, of class JDropDownMenu.
     */
    @Test
    public void testSetIcon() {
        System.out.println("setIcon");

        JDropDownMenu instance = new JDropDownMenu("Text", items);

        try {
            final Icon newIcon1 = new ImageIcon(Utilities.toURI(iconFile).toURL());
            final Icon newIcon2 = new ImageIcon(DROP_DOWN_ARROW_ICON_PATH);
            instance.setIcon(newIcon1);
            Icon result = instance.getIcon();
            assertEquals(newIcon1, result);

            instance.setIcon(newIcon2);
            result = instance.getIcon();
            assertEquals(newIcon2, result);
        } catch (MalformedURLException ex) {
            System.out.println("Could not create drop down arrow image");
        }
    }

    /**
     * Test of getItems method, of class JDropDownMenu.
     */
    @Test
    public void testGetItems() {
        System.out.println("getItems");
        JDropDownMenu instance = new JDropDownMenu("Text", items);
        Set result = instance.getItems();
        assertEqualsNoOrder(result.toArray(), items.toArray());
    }

    /**
     * Test of setSelectedItem method, of class JDropDownMenu.
     */
    @Test
    public void testSetSelectedItem() {
        System.out.println("setSelectedItem");
        String item = "Item 3";
        JDropDownMenu instance = mock(JDropDownMenu.class);

        instance.setSelectedItem(item);
        verify(instance, times(1)).setSelectedItem(item);
    }

    /**
     * Test of addActionListener method, of class JDropDownMenu.
     */
    @Test
    public void testAddActionListener() {
        System.out.println("addActionListener");
        ActionListener actionListenerMock = mock(ActionListener.class);
        JDropDownMenu instance = mock(JDropDownMenu.class);
        instance.addActionListener(actionListenerMock);
        verify(instance, times(1)).addActionListener(actionListenerMock);
    }

    /**
     * Test of actionPerformed method, of class JDropDownMenu.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");

        JDropDownMenu instance = mock(JDropDownMenu.class);
        // validate
        ActionEvent mockEvent = mock(ActionEvent.class);
        instance.actionPerformed(mockEvent);
        verify(instance, times(1)).actionPerformed(mockEvent);
    }

    /**
     * Test of setToolTipText method, of class JDropDownMenu.
     */
    @Test
    public void testSetToolTipText() {
        System.out.println("setToolTipText");
        String text = "Tooltip text to test";
        JDropDownMenu instance = new JDropDownMenu("Text", items);
        instance.setToolTipText(text);
        String result = instance.getToolTipText();
        assertEquals(result, text);
    }
}
