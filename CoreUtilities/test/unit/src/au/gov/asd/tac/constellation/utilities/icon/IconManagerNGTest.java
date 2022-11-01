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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.util.ImageUtilities;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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

    private static final Logger LOGGER = Logger.getLogger(IconManagerNGTest.class.getName());

    private final static Map<String, ConstellationIcon> TEST_CACHE = new HashMap<>();

    public IconManagerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws URISyntaxException {
        // Create test icons
        try (MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class)) {
            // Get a test directory location for the getIconDirectory call
            URL exampleIcon = IconManagerNGTest.class.getResource("resources/");
            File testFile = new File(exampleIcon.toURI());
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);
            // Create some test icons for the test cases
            ConstellationIcon icon1 = new ConstellationIcon.Builder("Test1",
                    new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                            DefaultIconProvider.FLAT_SQUARE.buildBufferedImage(16, ConstellationColor.BLUEBERRY.getJavaColor()),
                            AnalyticIconProvider.STAR.buildBufferedImage(16), 0, 0)))
                    .build();
            icon1.setEditable(true);

            ConstellationIcon icon2 = new ConstellationIcon.Builder("Test2",
                    new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                            DefaultIconProvider.ROUND_SQUARE.buildBufferedImage(16, ConstellationColor.CLOUDS.getJavaColor()),
                            AnalyticIconProvider.ANDROID.buildBufferedImage(16), 0, 0)))
                    .build();
            icon2.setEditable(true);

            ConstellationIcon icon3 = new ConstellationIcon.Builder("Test3",
                    new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                            DefaultIconProvider.HIGHLIGHTED.buildBufferedImage(16, ConstellationColor.PEACH.getJavaColor()),
                            AnalyticIconProvider.MR_SQUIGGLE.buildBufferedImage(16), 0, 0)))
                    .build();
            icon3.setEditable(false);

            // required so that the icons can be removed later
            IconManager.addIcon(icon1);
            IconManager.addIcon(icon2);
            IconManager.addIcon(icon3);

            final Map<String, ConstellationIcon> iconNames = new HashMap<>();
            iconNames.put("Test1", icon1);
            iconNames.put("Test2", icon2);
            iconNames.put("Test3", icon3);

            TEST_CACHE.putAll(iconNames);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // clear the test cache
        TEST_CACHE.clear();
        // remove the test icons
        final URL exampleIcon = IconManagerNGTest.class.getResource("resources/");
        final File testFile = new File(exampleIcon.toURI());
        final String iconDirectory = testFile.getAbsolutePath();
        for (int i = 1; i <= 3; i++) {
            final File iconFile = new File(iconDirectory, "Test" + i + ConstellationIcon.DEFAULT_ICON_SEPARATOR + ConstellationIcon.DEFAULT_ICON_FORMAT);
            if (iconFile.exists() && !iconFile.delete()) {
                LOGGER.log(Level.WARNING, "File was not deleted properly");
            }
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testGetIcon_StringPerformancetest() {
        int total = 0;

        final long start = System.currentTimeMillis();
        for (int i = 0; i <= 10000; i++) {
            total += IconManager.getIcon("Server").getExtendedName().length();
        }
        final long end = System.currentTimeMillis();

        System.out.println("Total = " + total);
        System.out.println("Diff is " + (end - start));

        // TODO: check why the performance improvement is not showing as being quicker than with the previous implementaion.
        if ((end - start) > 3000) {
            fail("IconManager.getIcon() took longer than 3 seconds to load 10,000 icons");
        }
    }

    /**
     * Test of getIconObjects method, of class IconManager.
     */
    @Test
    public void testGetIconObjects() {
        Set<ConstellationIcon> firstTime = IconManager.getIcons();
        Set<ConstellationIcon> secondTime = IconManager.getIcons();
        assertEquals(firstTime, secondTime);
    }

    @Test
    public void testGetIconObjectsPerformanceTest() {
        int total = 0;

        final long start = System.currentTimeMillis();
        for (int i = 0; i <= 1000; i++) {
            total += IconManager.getIcons().size();
        }
        final long end = System.currentTimeMillis();

        System.out.println("Total = " + total);
        System.out.println("Diff is " + (end - start));
    }

    /**
     * Test of getIconNames
     */
    @Test
    public void testGetIconNames() {
        try (MockedStatic<IconManager> iconManagerMock = Mockito.mockStatic(IconManager.class, Mockito.CALLS_REAL_METHODS)) {
            iconManagerMock.when(() -> IconManager.getCache()).thenReturn(TEST_CACHE);
            // Test when editable boolean is set to true
            // Since all custom icons are made editable, this should be the whole test cache
            final Set<String> namesTrue = IconManager.getIconNames(true);
            assertEquals(namesTrue.size(), 3);

            // Test when editable boolean is set to false
            // Since all custom icons are made editable, this should be empty
            final Set<String> namesFalse = IconManager.getIconNames(false);
            assertEquals(namesFalse.size(), 0);

            // Test when editable boolean is set to null
            final Set<String> namesNull = IconManager.getIconNames(null);
            assertEquals(namesNull.size(), 3);
        }
    }

    /**
     * Test of iconExists
     */
    @Test
    public void testIconExists() {
        try (MockedStatic<IconManager> iconManagerMock = Mockito.mockStatic(IconManager.class, Mockito.CALLS_REAL_METHODS)) {
            iconManagerMock.when(() -> IconManager.getCache()).thenReturn(TEST_CACHE);
            final boolean result1 = IconManager.iconExists("Test1");
            assertTrue(result1);

            final boolean result2 = IconManager.iconExists("Test2");
            assertTrue(result2);

            final boolean result3 = IconManager.iconExists("TestDoesNotExist");
            assertFalse(result3);
        }
    }

    /**
     * Test of getIcon
     */
    @Test
    public void testGetIcon() {
        try (MockedStatic<IconManager> iconManagerMock = Mockito.mockStatic(IconManager.class, Mockito.CALLS_REAL_METHODS)) {
            iconManagerMock.when(() -> IconManager.getCache()).thenReturn(TEST_CACHE);
            // Get an icon that exists
            final ConstellationIcon testIcon = IconManager.getIcon("Test1");
            assertEquals(testIcon.getName(), "Test1");

            // Try to get an icon that does not exist
            // Will create a new icon using createMissingIcon
            final ConstellationIcon nullIcon = IconManager.getIcon("TestDoesNotExist1");
            assertEquals(nullIcon.getName(), "TestDoesNotExist1");
        }
    }

    /**
     * Test of removeIcon
     */
    @Test
    public void testRemoveIcon() {
        // Remove the icons created in the setUpClass method
        final boolean result1 = IconManager.removeIcon("Test1");
        assertTrue(result1);

        final boolean result2 = IconManager.removeIcon("Test2");
        assertTrue(result2);

        final boolean result3 = IconManager.removeIcon("Test3");
        assertTrue(result3);

        // Test removing an icon that does not exist
        final boolean result4 = IconManager.removeIcon("TestDoesNotExist");
        assertFalse(result4);
    }
}
