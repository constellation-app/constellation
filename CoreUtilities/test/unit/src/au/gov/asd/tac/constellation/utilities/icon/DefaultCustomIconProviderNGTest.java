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
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
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

    final static String TEST_ICON_NAME = "TestIcon2";

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
    @Test(expectedExceptions = NullPointerException.class)
    public void runTests() throws URISyntaxException {
        try (MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class);
                MockedStatic<IconManager> iconManagerMock = Mockito.mockStatic(IconManager.class)) {
            // Get a test directory location for the getIconDirectory call
            URL exampleIcon = DefaultCustomIconProviderNGTest.class.getResource("resources/");
            File testFile = new File(exampleIcon.toURI());
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);

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
            DefaultCustomIconProvider instance = new DefaultCustomIconProvider();
            // Add a test icon to be removed later
            instance.addIcon(icon);

            // Run testAddIcon
            testAddIcon(iconManagerMock);

            //Run testAddIconFileDoesExist
            testAddIconFileDoesExist(icon);

            // Run testRemoveIcon
            testRemoveIcon(iconManagerMock, icon);

            // Run testRemoveIconDoesNotExist
            testRemoveIconDoesNotExist();

            // Run testLoadIcons
            testLoadIcons(testFile);

            // Verify defaultCustomIconProvider.getIconDirectory was called the correct number of times
            defaultCustomIconProviderMock.verify(() -> DefaultCustomIconProvider.getIconDirectory(), times(6));

        }
    }

    /**
     * Test of addIcon method, of class DefaultCustomIconProvider.
     *
     * @param iconManager
     * @throws URISyntaxException
     */
    public void testAddIcon(final MockedStatic<IconManager> iconManager) throws URISyntaxException {
        // Create a test icon
        final ConstellationColor ICON_COLOR = ConstellationColor.BLUEBERRY;
        final ConstellationIcon ICON_BACKGROUND = DefaultIconProvider.FLAT_SQUARE;
        final ConstellationIcon ICON_SYMBOL = AnalyticIconProvider.STAR;

        ConstellationIcon icon = new ConstellationIcon.Builder("TestIcon",
                new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                        ICON_BACKGROUND.buildBufferedImage(16, ICON_COLOR.getJavaColor()),
                        ICON_SYMBOL.buildBufferedImage(16), 0, 0)))
                .build();

        iconManager.when(() -> IconManager.iconExists(Mockito.any())).thenReturn(false);

        DefaultCustomIconProvider instance = new DefaultCustomIconProvider();

        final boolean result = instance.addIcon(icon);
        assertEquals(result, true);

        // Clean up for after the test
        if (DefaultCustomIconProviderNGTest.class.getResource("resources/TestIcon.png") != null) {
            File removeFile = new File(DefaultCustomIconProviderNGTest.class.getResource("resources/TestIcon.png").toURI());
            removeFile.delete();
        }

    }

    /**
     * Test of addIcon method, of class DefaultCustomIconProvider using an icon
     * file that does exist.
     *
     * @param icon
     */
    public void testAddIconFileDoesExist(final ConstellationIcon icon) {
        // Get an icon that does exist
        DefaultCustomIconProvider instance = new DefaultCustomIconProvider();

        final boolean result = instance.addIcon(icon);
        assertEquals(result, false);
    }

    /**
     * Test of removeIcon method, of class DefaultCustomIconProvider.
     *
     * @param iconManager
     * @param icon
     */
    public void testRemoveIcon(final MockedStatic<IconManager> iconManager, final ConstellationIcon icon) {
        // Test remove icon
        DefaultCustomIconProvider instance = new DefaultCustomIconProvider();
        iconManager.when(() -> IconManager.getIcon(Mockito.any())).thenReturn(icon);
        final boolean result = instance.removeIcon(TEST_ICON_NAME);
        assertEquals(result, true);
    }

    /**
     * Test of removeIcon method, of class DefaultCustomIconProvider, using an
     * icon that does not exist.
     */
    public void testRemoveIconDoesNotExist() {
        // Test remove icon
        DefaultCustomIconProvider instance = new DefaultCustomIconProvider();
        final boolean result = instance.removeIcon("TestIcon3");
        assertEquals(result, false);
    }

    /**
     * Test of loadIcons method, of class DefaultCustomIcomProvider.
     *
     * @param testFile
     */
    public void testLoadIcons(final File testFile) {
        DefaultCustomIconProvider instance = new DefaultCustomIconProvider();
        List<ConstellationIcon> list = instance.getIcons();

        // the number of icons should be the number of files in the resources folder minus "test_bagel_blue.png"
        final int expResult = testFile.listFiles().length - 1;
        assertEquals(list.size(), expResult);
    }
}
