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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests ConstelationIcon
 *
 * @author Delphinus8821
 * @author capricornunicorn123
 */
public class ConstellationIconNGTest {

    private static ConstellationIcon testIcon;
    private final static List<String> aliases = new ArrayList<>();
    private final static List<String> categories = new ArrayList<>();
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        aliases.add("newAlias");
        aliases.add("testAlias");
        categories.add("category1");
        categories.add("category2");
        testIcon = new ConstellationIcon.Builder("Test1",
                new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                        DefaultIconProvider.FLAT_SQUARE.buildBufferedImage(16, ConstellationColor.BLUEBERRY.getJavaColor()),
                        AnalyticIconProvider.STAR.buildBufferedImage(16), 0, 0)))
                .addAliases(aliases)
                .addCategories(categories)
                .build();
        testIcon.setEditable(true);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getCategories method, of class ConstellationIcon.
     */
    @Test
    public void testGetCategories() {
        final List<String> expResult = categories;
        final List<String> result = testIcon.getCategories();
        assertEquals(result, expResult);
    }

    /**
     * Test of isEditable method, of class ConstellationIcon.
     */
    @Test
    public void testIsEditable() {
        final boolean result = testIcon.isEditable();
        assertTrue(result);
    }

    /**
     * Test of setEditable method, of class ConstellationIcon.
     */
    @Test
    public void testSetEditable() {
        boolean editable = false;
        testIcon.setEditable(editable);
        assertFalse(testIcon.isEditable());

        editable = true;
        testIcon.setEditable(editable);
        assertTrue(testIcon.isEditable());
    }

    /**
     * Test of buildByteArray method, of class ConstellationIcon.
     */
    @Test
    public void testBuildByteArray() {
        final byte[] expResult = testIcon.getIconData().getData();
        final byte[] result = testIcon.buildByteArray();
        assertEquals(result, expResult);
    }

    /**
     * Test of buildBufferedImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildBufferedImage_0args() {
        final BufferedImage expResult = testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final BufferedImage result = testIcon.buildBufferedImage();
        assertEquals(result.getColorModel(), expResult.getColorModel());
        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
    }

    /**
     * Test of buildBufferedImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildBufferedImage_int() {
        final int size = 50;
        final BufferedImage expResult = testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final BufferedImage resultImage = testIcon.buildBufferedImage(size);
        // Check that the new buffered image doesn't equal the default one
        // Both images should have the same color model but different heights and widths
        final boolean resultHeight = resultImage.getHeight() != expResult.getHeight();
        final boolean resultWidth = resultImage.getWidth() != expResult.getWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(resultImage.getHeight(), size);
        assertEquals(resultImage.getWidth(), size);
        assertEquals(resultImage.getColorModel(), expResult.getColorModel());
    }

    /**
     * Test of buildBufferedImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildBufferedImage_Color() {
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final BufferedImage expResult = testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final BufferedImage result = testIcon.buildBufferedImage(color);
        // Check that the new buffered image doesn't equals the default one 
        // Both images should have different colors but the same size
        final boolean resultEquals = result.getData() != expResult.getData();
        assertTrue(resultEquals);
        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
    }

    /**
     * Test of buildBufferedImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildBufferedImage_int_Color() {
        final int size = 50;
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final BufferedImage expResult = testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final BufferedImage result = testIcon.buildBufferedImage(size, color);
        final boolean resultEquals = result.getData() != expResult.getData();
        assertTrue(resultEquals);
    }

    /**
     * Test of buildIcon method, of class ConstellationIcon.
     */
    @Test
    public void testBuildIcon_0args() {
        final Icon expResult = testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Icon result = testIcon.buildIcon();
        assertEquals(result.getIconHeight(), expResult.getIconHeight());
        assertEquals(result.getIconWidth(), expResult.getIconWidth());
    }

    /**
     * Test of buildIcon method, of class ConstellationIcon.
     */
    @Test
    public void testBuildIcon_int() {
        final int size = 50;
        final Icon expResult = testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Icon resultImage = testIcon.buildIcon(size);
        // Check that the new icon doesn't equal the default one
        // Both icon should have different heights and widths
        final boolean resultHeight = resultImage.getIconHeight() != expResult.getIconHeight();
        final boolean resultWidth = resultImage.getIconWidth() != expResult.getIconWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(resultImage.getIconHeight(), size);
        assertEquals(resultImage.getIconWidth(), size);
    }

    /**
     * Test of buildIcon method, of class ConstellationIcon.
     */
    @Test
    public void testBuildIcon_Color() {
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final Icon expResult = testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Icon result = testIcon.buildIcon(color);
        // Check that the new icon doesn't equals the default one
        // Both icons should be the same size
        assertEquals(result.getIconHeight(), expResult.getIconHeight());
        assertEquals(result.getIconWidth(), expResult.getIconWidth());
    }

    /**
     * Test of buildIcon method, of class ConstellationIcon.
     */
    @Test
    public void testBuildIcon_int_Color() {
        final int size = 50;
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final Icon expResult = testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Icon result = testIcon.buildIcon(size, color);
        final boolean resultHeight = result.getIconHeight() != expResult.getIconHeight();
        final boolean resultWidth = result.getIconWidth() != expResult.getIconWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(result.getIconHeight(), size);
        assertEquals(result.getIconWidth(), size);
    }

    /**
     * Test of buildImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildImage_0args() {
        final Image expResult = testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Image result = testIcon.buildImage();
        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
    }

    /**
     * Test of buildImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildImage_int() {
        final int size = 50;
        final Image expResult = testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Image resultImage = testIcon.buildImage(size);
        // Check that the new image doesn't equal the default one
        // Both images should have different heights and widths
        final boolean resultHeight = resultImage.getHeight() != expResult.getHeight();
        final boolean resultWidth = resultImage.getWidth() != expResult.getWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(Math.round(resultImage.getHeight()), size);
        assertEquals(Math.round(resultImage.getWidth()), size);
    }

    /**
     * Test of buildImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildImage_Color() {
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final Image expResult = testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Image result = testIcon.buildImage(color);
        // Check that the new icon doesn't equals the default one
        // Both images should be the same size
        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
    }

    /**
     * Test of buildImage method, of class ConstellationIcon.
     */
    @Test
    public void testBuildImage_int_Color() {
        final int size = 50;
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final Image expResult = testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        final Image result = testIcon.buildImage(size, color);
        final boolean resultHeight = result.getHeight() != expResult.getHeight();
        final boolean resultWidth = result.getWidth() != expResult.getWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(Math.round(result.getHeight()), size);
        assertEquals(Math.round(result.getWidth()), size);
    }

    /**
     * Test of buildSVG method, of class ConstellationIcon.
     */
    @Test
    public void testBuildSVG_0args() {
        final SVGObject expResult = new SVGObject(testIcon.buildSVG(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        final SVGObject result = new SVGObject(testIcon.buildSVG());
        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
    }

    /**
     * Test of buildSVG method, of class ConstellationIcon.
     */
    @Test
    public void testBuildSVG_int() {
        final int size = 50;
        final SVGObject expResult = new SVGObject(testIcon.buildSVG(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        final SVGObject result = new SVGObject(testIcon.buildSVG(size));
        // Check that the new image doesn't equal the default one
        // Both images should have different heights and widths
        final boolean resultHeight = result.getHeight() != expResult.getHeight();
        final boolean resultWidth = result.getWidth() != expResult.getWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(Math.round(result.getHeight()), size);
        assertEquals(Math.round(result.getWidth()), size);
    }

    /**
     * Test of buildSVG method, of class ConstellationIcon.
     */
    @Test
    public void testBuildSVG_Color() {
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final SVGObject expResult = new SVGObject(testIcon.buildSVG(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        final SVGObject result = new SVGObject(testIcon.buildSVG(color));
        // Check that the new icon doesn't equals the default one
        // Both images should be the same size
        assertEquals(result.getHeight(), expResult.getHeight());
        assertEquals(result.getWidth(), expResult.getWidth());
    }

    /**
     * Test of buildSVG method, of class ConstellationIcon.
     */
    @Test
    public void testBuildSVG_int_Color() {
        final int size = 50;
        final Color color = ConstellationColor.DARK_GREEN.getJavaColor();
        final SVGObject expResult = new SVGObject(DefaultIconProvider.FLAT_SQUARE.buildSVG(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        final SVGObject result = new SVGObject(DefaultIconProvider.FLAT_SQUARE.buildSVG(size, color));
        final boolean resultHeight = result.getHeight() != expResult.getHeight();
        final boolean resultWidth = result.getWidth() != expResult.getWidth();
        assertTrue(resultHeight);
        assertTrue(resultWidth);
        assertEquals(Math.round(result.getHeight()), size);
        assertEquals(Math.round(result.getWidth()), size);
    }

    /**
     * Test of clearCache method, of class ConstellationIcon.
     */
    @Test
    public void testClearCache() {
        ConstellationIcon.clearCache();

        Icon icon = testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        Image image = testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        BufferedImage bufferedImage = testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null);

        assertEquals(icon, testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        assertEquals(image, testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        assertEquals(bufferedImage, testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null));

        ConstellationIcon.clearCache();

        assertNotEquals(icon, testIcon.buildIcon(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        assertNotEquals(image, testIcon.buildImage(ConstellationIcon.DEFAULT_ICON_SIZE, null));
        assertNotEquals(bufferedImage, testIcon.buildBufferedImage(ConstellationIcon.DEFAULT_ICON_SIZE, null));
    }

    /**
     * Test of toString method, of class ConstellationIcon.
     */
    @Test
    public void testToString() {
        String expResult = testIcon.getExtendedName();
        String result = testIcon.toString();
        assertEquals(result, expResult);
    }
}
