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
import org.openide.util.Exceptions;
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
     * Test of addIcon method, of class DefaultCustomIconProvider.
     */
    @Test
    public void testAddIcon() {
        try ( MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class)) {
            try ( MockedStatic<IconManager> iconManagerMock = Mockito.mockStatic(IconManager.class)) {

                // Get a test directory location for the getIconDirectory call
                URL exampleIcon = DefaultCustomIconProviderNGTest.class.getResource("resources/");
                File testFile = new File(exampleIcon.toURI());

                // Create a test icon
                final ConstellationColor ICON_COLOR = ConstellationColor.BLUEBERRY;
                final ConstellationIcon ICON_BACKGROUND = DefaultIconProvider.FLAT_SQUARE;
                final ConstellationIcon ICON_SYMBOL = AnalyticIconProvider.STAR;

                ConstellationIcon icon = new ConstellationIcon.Builder("TestIcon",
                        new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                                ICON_BACKGROUND.buildBufferedImage(16, ICON_COLOR.getJavaColor()),
                                ICON_SYMBOL.buildBufferedImage(16), 0, 0)))
                        .build();
                icon.buildBufferedImage();

                defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);
                iconManagerMock.when(() -> IconManager.iconExists(Mockito.any())).thenReturn(false);

                DefaultCustomIconProvider instance = new DefaultCustomIconProvider();

                boolean result = instance.addIcon(icon);

                defaultCustomIconProviderMock.verify(() -> DefaultCustomIconProvider.getIconDirectory(), times(2));
                assertEquals(result, true);

            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Test of addIcon method, of class DefaultCustomIconProvider using an icon file that does exist.
     */
    @Test
    public void testAddIconFileDoesExist() {
        try ( MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class)) {

            // Get a test directory location for the getIconDirectory call
            URL exampleIcon = DefaultCustomIconProviderNGTest.class.getResource("resources/");
            File testFile = new File(exampleIcon.toURI());

            // Get an icon that does exist
            ConstellationIcon icon = IconManager.getIcon("resources/test_bagel_blue.png");

            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);
            DefaultCustomIconProvider instance = new DefaultCustomIconProvider();

            boolean result = instance.addIcon(icon);
            assertEquals(result, false);

        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of removeIcon method, of class DefaultCustomIconProvider.
     */
    @Test
    public void testRemoveIcon() {
        try ( MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class)) {
            // Get a test directory location for the getIconDirectory call
            URL exampleIcon = DefaultCustomIconProviderNGTest.class.getResource("resources/");
            File testFile = new File(exampleIcon.toURI());
            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);

            String iconName = "TestIcon";
            DefaultCustomIconProvider instance = new DefaultCustomIconProvider();

            boolean result = instance.removeIcon(iconName);
            assertEquals(result, true);

        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of loadIcons method, of class DefaultCustomIcomProvider.
     */
    @Test
    public void testLoadIcons() {
        try ( MockedStatic<DefaultCustomIconProvider> defaultCustomIconProviderMock = Mockito.mockStatic(DefaultCustomIconProvider.class)) {
            // Get a test directory location for the getIconDirectory call
            URL testIcon = DefaultCustomIconProviderNGTest.class.getResource("resources/");
            File testFile = new File(testIcon.toURI());

            defaultCustomIconProviderMock.when(() -> DefaultCustomIconProvider.getIconDirectory()).thenReturn(testFile);
            DefaultCustomIconProvider instance = new DefaultCustomIconProvider();
            List list = instance.getIcons();
            assertEquals(list.size(), 1);

        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
