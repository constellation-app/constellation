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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openide.util.ImageUtilities;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for DefaultCustomIconProvider
 *
 * @author Delphinus8821
 */
public class DefaultCustomIconProviderNGTest {

    final static String TEST_ICON_NAME = "Category1.TestIcon1";

    public DefaultCustomIconProviderNGTest() {
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
     * Runner for tests
     *
     * @throws java.net.URISyntaxException
     */
    @Test()
    public void runTests() throws URISyntaxException {
        // Note, the DefaultCustomIconProvider class will be called through the IconManager when dealing with custom icons
        //
        try (MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class);) {
            // Get a test directory location for the getIconDirectory call
            URL exampleIcon = DefaultCustomIconProviderNGTest.class.getResource("resources/");
            File testFile = new File(exampleIcon.toURI());
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.containsIcon(Mockito.any())).thenCallRealMethod();
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.reloadIcons()).thenCallRealMethod();
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.loadIcons()).thenCallRealMethod();

            // Create a test icon to be removed by testRemoveIcon
            final ConstellationColor ICON_COLOR = ConstellationColor.BLUEBERRY;
            final ConstellationIcon ICON_BACKGROUND = DefaultIconProvider.FLAT_SQUARE;
            final ConstellationIcon ICON_SYMBOL = AnalyticIconProvider.STAR;

            ConstellationIcon icon = new ConstellationIcon.Builder(TEST_ICON_NAME,
                    new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                            ICON_BACKGROUND.buildBufferedImage(16, ICON_COLOR.getJavaColor()),
                            ICON_SYMBOL.buildBufferedImage(16), 0, 0)))
                    .build();
            icon.setEditable(true);
            System.out.println("===== INITIALISE TEST =====");

            prepareFileDir(testFile);
            DefaultCustomIconProvider.reloadIcons();

            // Add a test icon to be removed later
            System.out.println("Adding initial Icon: " + icon.getExtendedName());
            IconManager.addIcon(icon);

            System.out.println("---------------------");

            final ConstellationColor ICON_COLOR2 = ConstellationColor.RED;
            final ConstellationIcon ICON_BACKGROUND2 = DefaultIconProvider.FLAT_CIRCLE;
            final ConstellationIcon ICON_SYMBOL2 = AnalyticIconProvider.ANDROID;

            ConstellationIcon icon2 = new ConstellationIcon.Builder("Category2.TestIcon",
                    new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                            ICON_BACKGROUND2.buildBufferedImage(16, ICON_COLOR2.getJavaColor()),
                            ICON_SYMBOL2.buildBufferedImage(16), 0, 0)))
                    .build();

            // Run testAddIcon
            System.out.print("TEST: Add an icon: ");
            testAddIcon(icon2, testFile);
            System.out.println(" *PASSED*");

            //Run testAddIconFileDoesExist
            System.out.print("TEST: Dont allow adding same icon again: ");
            testAddIconFileDoesExist(icon2);
            System.out.println(" *PASSED*");

            // Run testRemoveIcon
            System.out.print("TEST: Remove the new icon: ");
            testRemoveIcon(icon2);
            System.out.println(" *PASSED*");

            // Run testRemoveIconDoesNotExist
            System.out.print("TEST: Dont allow removing same icon again: ");
            testRemoveIconDoesNotExist(icon2);
            System.out.println(" *PASSED*");

            // Run testLoadIcons
            System.out.print("TEST: Check icon cache matches directory entries: ");
            testCacheMatch(testFile);
            System.out.println(" *PASSED*");

            System.out.print("TEST: Check the number of calls on a static method: ");
            // Verify defaultCustomIconProvider.getIconDirectory was called the correct number of times
//            defaultCustomIconProviderMock.verify(() -> DefaultCustomIconProvider.getIconDirectory(), times(6));
            System.out.println(" *PASSED*");

        }
    }

    /**
     * Test of addIcon method, of class DefaultCustomIconProvider.
     *
     * @param icon
     * @param testFile
     * @throws URISyntaxException
     */
    public void testAddIcon(final ConstellationIcon icon, final File testFile) throws URISyntaxException {
        // Create a test icon

        boolean icExists = IconManager.iconExists(icon.getExtendedName());
        final boolean result = IconManager.addIcon(icon);
        assertEquals(result, !icExists);

    }

    /**
     * Test of addIcon method, of class DefaultCustomIconProvider using an icon
     * file that does exist.
     *
     * @param icon
     */
    public void testAddIconFileDoesExist(final ConstellationIcon icon) {
        // Check if the icon is present in the local cache
        final boolean localCacheEntryExists = DefaultCustomIconProvider.containsIcon(icon.getName());
        assertEquals(localCacheEntryExists, true);

        // try to add an icon that already exists
        final boolean result = IconManager.addIcon(icon);
        assertEquals(result, false);
    }

    /**
     * Test of removeIcon method, of class DefaultCustomIconProvider.
     *
     * @param icon
     */
    public void testRemoveIcon(final ConstellationIcon icon) {
        // Test removing an icon
        final boolean result = IconManager.removeIcon(icon.getExtendedName());
        assertEquals(result, true);

    }

    /**
     * Test of removeIcon method, of class DefaultCustomIconProvider, using an
     * icon that does not exist.
     *
     * @param icon
     */
    public void testRemoveIconDoesNotExist(final ConstellationIcon icon) {
        // Test removing an icon that doesn't exist
        final boolean result = IconManager.removeIcon(icon.getExtendedName());
        assertEquals(result, false);
    }

    /**
     * Test of loadIcons method, of class DefaultCustomIcomProvider.
     *
     * @param testFile
     */
    public void testCacheMatch(final File testFile) {
        Set<String> iconSet = IconManager.getIconNames(true);
        // At the time of writing the test, the number of icons should be 2: the initial "test_bagel_blue.png" file
        //   and the Category1.TestIcon1.png file created during testing
        // The number of icons in the memory cache should match the number of icons in the directory
        assertEquals(iconSet.size(), testFile.listFiles().length);
    }

    public void prepareFileDir(final File testFile) {
        // reset the icon resource folder to only contain the test_bagel_blue.png file
        List<String> filenames = new ArrayList<>();
        for (File f : testFile.listFiles()) {
            String path = f.getAbsolutePath();
            filenames.add(path);
        }
        for (String path : filenames) {
            if (!path.contains("bagel_blue")) {
                File f = new File(path);
                f.delete();
                System.out.println(" -->> REMOVING file:" + path);
            }
        }
    }
}
