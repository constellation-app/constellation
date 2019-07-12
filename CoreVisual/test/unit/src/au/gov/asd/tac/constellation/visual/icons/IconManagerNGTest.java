/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.visual.icons;

import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Icon Manager Test.
 *
 * @author arcturus
 */
public class IconManagerNGTest {

    public IconManagerNGTest() {
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

//    /**
//     * Test of getIconProviders method, of class IconManager.
//     */
//    @Test
//    public void testGetIconProviders() {
//        System.out.println("getIconProviders");
//        Collection expResult = null;
//        Collection result = IconManager.getIconProviders();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCustomProvider method, of class IconManager.
//     */
//    @Test
//    public void testGetCustomProvider() {
//        System.out.println("getCustomProvider");
//        CustomIconProvider expResult = null;
//        CustomIconProvider result = IconManager.getCustomProvider();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of iconExists method, of class IconManager.
//     */
//    @Test
//    public void testIconExists_String_Class() {
//        System.out.println("iconExists");
//        String name = "";
//        boolean expResult = false;
//        boolean result = IconManager.iconExists(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of iconExists method, of class IconManager.
//     */
//    @Test
//    public void testIconExists_String() {
//        System.out.println("iconExists");
//        String name = "";
//        boolean expResult = false;
//        boolean result = IconManager.iconExists(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getIcon method, of class IconManager.
//     */
//    @Test
//    public void testGetIcon_String_Class() {
//        System.out.println("getIcon");
//        String name = "";
//        ConstellationIcon expResult = null;
//        ConstellationIcon result = IconManager.getIcon(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getIcon method, of class IconManager.
//     */
//    @Test
//    public void testGetIcon_String() {
//        System.out.println("getIcon");
//        String name = "";
//        ConstellationIcon expResult = null;
//        ConstellationIcon result = IconManager.getIcon(name);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test
    public void testGetIcon_StringPerformancetest() {
        final IconManager im = new IconManager();

        int total = 0;

        final long start = System.currentTimeMillis();
        for (int i = 0; i <= 10000; i++) {
            total += im.getIcon("Server").getExtendedName().length();
        }
        final long end = System.currentTimeMillis();

        System.out.println("Total = " + total);
        System.out.println("Diff is " + (end - start));

        // TODO: check why the performance improvement is not showing as being quicker than with the previous implementaion.
        if ((end - start) > 3000) {
            fail("IconManager.getIcon() took longer than 3 seconds to load 10,000 icons");
        }
    }

//    /**
//     * Test of getIconNames method, of class IconManager.
//     */
//    @Test
//    public void testGetIconNames() {
//        System.out.println("getIconNames");
//        Boolean editable = null;
//        Set expResult = null;
//        Set result = IconManager.getIconNames(editable);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getIconObjects method, of class IconManager.
     */
    @Test
    public void testGetIconObjects() {
        Set firstTime = IconManager.getIcons();
        Set secondTime = IconManager.getIcons();
        assertEquals(firstTime, secondTime);
    }

    @Test
    public void testGetIconObjectsPerformanceTest() {
        final IconManager im = new IconManager();

        int total = 0;

        final long start = System.currentTimeMillis();
        for (int i = 0; i <= 1000; i++) {
            total += im.getIcons().size();
        }
        final long end = System.currentTimeMillis();

        System.out.println("Total = " + total);
        System.out.println("Diff is " + (end - start));
    }

//    /**
//     * Test of addIcon method, of class IconManager.
//     */
//    @Test
//    public void testAddIcon() {
//        System.out.println("addIcon");
//        ConstellationIcon icon = null;
//        boolean expResult = false;
//        boolean result = IconManager.addIcon(icon);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeIcon method, of class IconManager.
//     */
//    @Test
//    public void testRemoveIcon() {
//        System.out.println("removeIcon");
//        String iconName = "";
//        boolean expResult = false;
//        boolean result = IconManager.removeIcon(iconName);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
