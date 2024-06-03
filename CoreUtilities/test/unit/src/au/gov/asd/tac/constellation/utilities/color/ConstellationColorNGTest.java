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
package au.gov.asd.tac.constellation.utilities.color;

import java.awt.Color;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ConstellationColorNGTest {
    
    public ConstellationColorNGTest() {
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
     * Convenience method for asserting the RGBA values of a ConstellationColor
     * 
     * @param color
     * @param expectedRedValue
     * @param expectedGreenValue
     * @param expectedBlueValue
     * @param expectedAlphaValue 
     */
    private void assertColorProperties(final ConstellationColor color, final float expectedRedValue, final float expectedGreenValue, 
            final float expectedBlueValue, final float expectedAlphaValue) {
        assertEquals(color.getRed(), expectedRedValue);
        assertEquals(color.getGreen(), expectedGreenValue);
        assertEquals(color.getBlue(), expectedBlueValue);
        assertEquals(color.getAlpha(), expectedAlphaValue);
    }

    /**
     * Test of getColorValue method, of class ConstellationColor. 1 parameter
     */
    @Test
    public void testGetColorValueOneParameter() {
        System.out.println("getColorValueOneParameter");
        
        final ConstellationColor nullColor = ConstellationColor.getColorValue(null);
        assertEquals(nullColor, null);
        
        final ConstellationColor namedColor = ConstellationColor.getColorValue("Banana");
        assertEquals(namedColor, ConstellationColor.BANANA);
        
        final ConstellationColor namedGreyColor = ConstellationColor.getColorValue("Gray");
        assertEquals(namedGreyColor, ConstellationColor.GREY);
        
        final ConstellationColor rgbColor = ConstellationColor.getColorValue("rgb255255000");
        assertColorProperties(rgbColor, 1F, 1F, 0F, 1F);
        
        final ConstellationColor rgbCommaColor = ConstellationColor.getColorValue("0.2,1,0.6,0.7");
        assertColorProperties(rgbCommaColor, 0.2F, 1F, 0.6F, 0.7F);
        
        final ConstellationColor htmlColor = ConstellationColor.getColorValue("#6699CC");
        assertColorProperties(htmlColor, 0.4F, 0.6F, 0.8F, 1F);
        
        final ConstellationColor notAColor = ConstellationColor.getColorValue("Not a color");
        assertEquals(notAColor, null);
    }

    /**
     * Test of getColorValue method, of class ConstellationColor. 4 parameters
     */
    @Test
    public void testGetColorValueFourParameters() {
        System.out.println("getColorValueFourParameters");
        
        final ConstellationColor namedColor = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        assertEquals(namedColor, ConstellationColor.BANANA);
        
        final ConstellationColor unnamedColor = ConstellationColor.getColorValue(0.5F, 0.5F, 0.5F, 0.5F);
        assertEquals(unnamedColor.getName(), null);
        assertEquals(unnamedColor.getRGBString(), "0.500000,0.500000,0.500000,0.500000");
    }

    /**
     * Test of getJavaColor method, of class ConstellationColor.
     */
    @Test
    public void testGetJavaColor() {
        System.out.println("getJavaColor");
        
        final ConstellationColor constellationColor = ConstellationColor.getColorValue("Banana");
        final Color javaColor = constellationColor.getJavaColor();
        assertEquals(javaColor.getRed(), 254);
        assertEquals(javaColor.getGreen(), 255);
        assertEquals(javaColor.getBlue(), 106);
        assertEquals(javaColor.getAlpha(), 255);
    }

    /**
     * Test of getJavaFXColor method, of class ConstellationColor.
     */
    @Test
    public void testGetJavaFXColor() {
        System.out.println("getJavaFXColor");
        
        final ConstellationColor constellationColor = ConstellationColor.getColorValue("GoldenRod");
        final javafx.scene.paint.Color javaFxColor = constellationColor.getJavaFXColor();
        assertEquals(javaFxColor.getRed(), 1.0);
        assertEquals(javaFxColor.getGreen(), 0.75);
        assertEquals(javaFxColor.getBlue(), 0.0);
        assertEquals(javaFxColor.getOpacity(), 1.0);
    }

    /**
     * Test of getHtmlColor method, of class ConstellationColor.
     */
    @Test
    public void testGetHtmlColor() {
        System.out.println("getHtmlColor");
        
        final ConstellationColor constellationColor = ConstellationColor.getColorValue("Banana");
        final String htmlColor = constellationColor.getHtmlColor();
        assertEquals(htmlColor, "#feff6a");
    }

    /**
     * Test of fromJavaColor method, of class ConstellationColor.
     */
    @Test
    public void testFromJavaColor() {
        System.out.println("fromJavaColor");
        
        final ConstellationColor nullColor = ConstellationColor.fromJavaColor(null);
        assertEquals(nullColor, null);
        
        final ConstellationColor constellationColor = ConstellationColor.fromJavaColor(Color.CYAN);
        assertColorProperties(constellationColor, 0F, 1F, 1F, 1F);
    }
    
    /**
     * Test of fromFXColor method, of class ConstellationColor.
     */
    @Test
    public void testFromFXColor() {
        System.out.println("fromFXColor");
        
        final ConstellationColor nullColor = ConstellationColor.fromFXColor(null);
        assertEquals(nullColor, null);
        
        final ConstellationColor constellationColor = ConstellationColor.fromFXColor(javafx.scene.paint.Color.CYAN);
        assertColorProperties(constellationColor, 0F, 1F, 1F, 1F);
    }

    /**
     * Test of fromHtmlColor method, of class ConstellationColor.
     */
    @Test
    public void testFromHtmlColor() {
        System.out.println("fromHtmlColor");
        
        final ConstellationColor nullColor = ConstellationColor.fromHtmlColor(null);
        assertEquals(nullColor, null);
        
        final ConstellationColor wrongLengthColor = ConstellationColor.fromHtmlColor("#00000000000");
        assertEquals(wrongLengthColor, null);
        
        final ConstellationColor noHashColor = ConstellationColor.fromHtmlColor("FFEEDDC");
        assertEquals(noHashColor, null);
        
        final ConstellationColor ucConstellationColor = ConstellationColor.fromHtmlColor("#FFEEDD");
        assertColorProperties(ucConstellationColor, 1F, 238/255F, 221/255F, 1F);
        
        final ConstellationColor lcConstellationColor = ConstellationColor.fromHtmlColor("#ffeedd");
        assertColorProperties(lcConstellationColor, 1F, 238/255F, 221/255F, 1F);
    }

    /**
     * Test of getContrastHtmlColor method, of class ConstellationColor.
     */
    @Test
    public void testGetContrastHtmlColor() {
        System.out.println("getContrastHtmlColor");
        
        final ConstellationColor nullColor = ConstellationColor.getContrastHtmlColor(null);
        assertEquals(nullColor, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColor = ConstellationColor.getContrastHtmlColor("#FFEEDD");
        assertEquals(darkContrastColor, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColor = ConstellationColor.getContrastHtmlColor("#221100");
        assertEquals(lightContrastColor, ConstellationColor.WHITE);
        
        final ConstellationColor middleColor = ConstellationColor.getContrastHtmlColor("#808080");
        assertEquals(middleColor, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastRGBColor method, of class ConstellationColor.
     */
    @Test
    public void testGetContrastRGBColor() {
        System.out.println("getContrastRGBColor");

        final ConstellationColor nullColor = ConstellationColor.getContrastRGBColor(null);
        assertEquals(nullColor, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColor = ConstellationColor.getContrastRGBColor("RGB255225200");
        assertEquals(darkContrastColor, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColor = ConstellationColor.getContrastRGBColor("RGB000050100");
        assertEquals(lightContrastColor, ConstellationColor.WHITE);
        
        final ConstellationColor middleColor = ConstellationColor.getContrastRGBColor("RGB128128128");
        assertEquals(middleColor, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastColor method, of class ConstellationColor. ConstellationColor parameter
     */
    @Test
    public void testGetContrastColorConstellationColor() {
        System.out.println("getContrastColorConstellationColor");
        
        final ConstellationColor nullColor = ConstellationColor.getContrastColor((ConstellationColor) null);
        assertEquals(nullColor, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColor = ConstellationColor.getContrastColor(ConstellationColor.BANANA);
        assertEquals(darkContrastColor, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColor = ConstellationColor.getContrastColor(ConstellationColor.LIGHT_BLUE);
        assertEquals(lightContrastColor, ConstellationColor.WHITE);
        
        final ConstellationColor middleColor = ConstellationColor.getContrastColor(ConstellationColor.getColorValue(0.5F, 0.5F, 0.5F, 1));
        assertEquals(middleColor, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastfromRgbWithCommaColor method, of class ConstellationColor.
     */
    @Test
    public void testGetContrastfromRgbWithCommaColor() {
        System.out.println("getContrastfromRgbWithCommaColor");
        
        final ConstellationColor nullColor = ConstellationColor.getContrastfromRgbWithCommaColor(null);
        assertEquals(nullColor, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColor = ConstellationColor.getContrastfromRgbWithCommaColor("1,0.95,0.9");
        assertEquals(darkContrastColor, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColor = ConstellationColor.getContrastfromRgbWithCommaColor("[0,0.5,1]");
        assertEquals(lightContrastColor, ConstellationColor.WHITE);
        
        final ConstellationColor middleColor = ConstellationColor.getContrastfromRgbWithCommaColor("0.5,0.5,0.5");
        assertEquals(middleColor, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastColor method, of class ConstellationColor. Java Color parameter
     */
    @Test
    public void testGetContrastColorJavaColor() {
        System.out.println("getContrastColorJavaColor");
        
        final ConstellationColor nullColor = ConstellationColor.getContrastColor((Color) null);
        assertEquals(nullColor, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColor = ConstellationColor.getContrastColor(Color.LIGHT_GRAY);
        assertEquals(darkContrastColor, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColor = ConstellationColor.getContrastColor(Color.DARK_GRAY);
        assertEquals(lightContrastColor, ConstellationColor.WHITE);
        
        final ConstellationColor middleColor = ConstellationColor.getContrastColor(Color.GRAY);
        assertEquals(middleColor, ConstellationColor.BLACK);
    }
    
    /**
     * Test of fromRgbColor method, of class ConstellationColor.
     */
    @Test
    public void testFromRgbColor() {
        System.out.println("fromRgbColor");
        
        final ConstellationColor nullColor = ConstellationColor.fromRgbColor(null);
        assertEquals(nullColor, null);
        
        final ConstellationColor constellationColor = ConstellationColor.fromRgbColor("RGB000255255");
        assertColorProperties(constellationColor, 0F, 1F, 1F, 1F);
    }

    /**
     * Test of fromRgbWithCommaColor method, of class ConstellationColor.
     */
    @Test
    public void testFromRgbWithCommaColor() {
        System.out.println("fromRgbWithCommaColor");
        
        final ConstellationColor nullColor = ConstellationColor.fromRgbWithCommaColor(null);
        assertEquals(nullColor, null);
        
        final ConstellationColor constellationColorNoBracket = ConstellationColor.fromRgbWithCommaColor("0,1,1");
        assertColorProperties(constellationColorNoBracket, 0F, 1F, 1F, 1F);
        
        final ConstellationColor constellationColorBracket = ConstellationColor.fromRgbWithCommaColor("[0,1,1]");
        assertColorProperties(constellationColorBracket, 0F, 1F, 1F, 1F);
    }

    /**
     * Test of equals method, of class ConstellationColor.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final ConstellationColor color1 = ConstellationColor.getColorValue("Banana");
        
        final ConstellationColor color2 = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        final ConstellationColor color3 = ConstellationColor.fromHtmlColor("#FEFF6A");
        
        final ConstellationColor color4 = ConstellationColor.getColorValue("GoldenRod");
        
        assertTrue(color1.equals(color2));
        assertTrue(color1.equals(color3));
        assertTrue(color1.equals("Banana"));
        assertFalse(color1.equals(color4));
        assertFalse(color1.equals("GoldenRod"));
    }

    /**
     * Test of toString method, of class ConstellationColor.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final ConstellationColor color1 = ConstellationColor.getColorValue("Banana");        
        final ConstellationColor color2 = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        final ConstellationColor color3 = ConstellationColor.getColorValue(253/255F, 1F, 106/255F, 1F);
        
        assertEquals(color1.toString(), "Banana");
        assertEquals(color2.toString(), "Banana");
        assertEquals(color3.toString(), "#fdff6a");
    }

    /**
     * Test of compareTo method, of class ConstellationColor.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        final ConstellationColor color1 = ConstellationColor.getColorValue("Banana");        
        final ConstellationColor color2 = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        final ConstellationColor color3 = ConstellationColor.getColorValue(253/255F, 1F, 106/255F, 1F);
        final ConstellationColor color4 = ConstellationColor.getColorValue(1F, 1F, 106/255F, 1F);
        final ConstellationColor color5 = ConstellationColor.getColorValue(253/255F, 0.9F, 106/255F, 1F);
        final ConstellationColor color6 = ConstellationColor.getColorValue(253/255F, 1F, 120/255F, 1F);
        final ConstellationColor color7 = ConstellationColor.getColorValue(253/255F, 1F, 106/255F, 0.4F);
        final ConstellationColor color8 = ConstellationColor.fromHtmlColor("#FDFF6A");
        
        assertEquals(color1.compareTo(color2), 0);
        assertEquals(color1.compareTo(color3), 1);
        assertEquals(color3.compareTo(color1), -1);
        assertEquals(color3.compareTo(color4), -1);
        assertEquals(color3.compareTo(color5), 1);
        assertEquals(color3.compareTo(color6), -1);
        assertEquals(color3.compareTo(color7), 1);
        assertEquals(color3.compareTo(color8), 0);
    }

    /**
     * Test of createPalette method, of class ConstellationColor. 3 parameters
     */
    @Test
    public void testCreatePaletteThreeParameters() {
        System.out.println("createPaletteThreeParameters");
        
        final ConstellationColor[] zeroColorPalette = ConstellationColor.createPalette(0, 0.6F, 0.4F);
        assertEquals(zeroColorPalette.length, 0);
        
        final ConstellationColor[] oneColorPalette = ConstellationColor.createPalette(1, 0.6F, 0.4F);
        assertEquals(oneColorPalette.length, 1);
        assertColorProperties(oneColorPalette[0], 0.4F, 41/255F, 41/255F, 1F);
        
        final ConstellationColor[] twoColorPalette = ConstellationColor.createPalette(2, 0.6F, 0.4F);
        assertEquals(twoColorPalette.length, 2);
        assertColorProperties(twoColorPalette[1], 41/255F, 0.4F, 0.4F, 1F);
        
        final ConstellationColor[] moreColorPalette = ConstellationColor.createPalette(4, 0.6F, 0.4F);
        assertEquals(moreColorPalette.length, 4);
        assertColorProperties(moreColorPalette[2], 71/255F, 41/255F, 0.4F, 1F);
        assertColorProperties(moreColorPalette[3], 71/255F, 0.4F, 41/255F, 1F);
    }

    /**
     * Test of createPalette method, of class ConstellationColor. 1 parameter
     */
    @Test
    public void testCreatePaletteOneParameter() {
        System.out.println("createPaletteOneParameter");
        
        final ConstellationColor[] zeroColorPalette = ConstellationColor.createPalette(0);
        assertEquals(zeroColorPalette.length, 0);
        
        final ConstellationColor[] oneColorPalette = ConstellationColor.createPalette(1);
        assertEquals(oneColorPalette.length, 1);
        assertColorProperties(oneColorPalette[0], 1F, 0F, 0F, 1F);
        
        final ConstellationColor[] twoColorPalette = ConstellationColor.createPalette(2);
        assertEquals(twoColorPalette.length, 2);
        assertColorProperties(twoColorPalette[1], 0F, 1F, 1F, 1F);
        
        final ConstellationColor[] moreColorPalette = ConstellationColor.createPalette(4);
        assertEquals(moreColorPalette.length, 4);
        assertColorProperties(moreColorPalette[2], 128/255F, 0F, 1F, 1F);
        assertColorProperties(moreColorPalette[3], 128/255F, 1F, 0F, 1F);
    }

    /**
     * Test of createLinearPalette method, of class ConstellationColor. Not enough colors to create palette
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateLinearPaletteNotEnoughColors() {
        System.out.println("createLinearPaletteNotEnoughColors");
        
        ConstellationColor.createLinearPalette(1, ConstellationColor.BANANA, ConstellationColor.GOLDEN_ROD);
    }
    
    /**
     * Test of createLinearPalette method, of class ConstellationColor.
     */
    @Test
    public void testCreateLinearPalette() {
        System.out.println("createLinearPalette");

        final ConstellationColor[] palette = ConstellationColor.createLinearPalette(3, ConstellationColor.CYAN, ConstellationColor.GOLDEN_ROD);
        assertEquals(palette.length, 3);
        assertColorProperties(palette[0], 0F, 1F, 1F, 1F);
        assertColorProperties(palette[1], 0.5F, 0.875F, 0.5F, 1F);
        assertColorProperties(palette[2], 1F, 0.75F, 0F, 1F);
    }

    /**
     * Test of createPalettePhi method, of class ConstellationColor.
     */
    @Test
    public void testCreatePalettePhi() {
        System.out.println("createPalettePhi");
        
        final ConstellationColor[] palette = ConstellationColor.createPalettePhi(3, 0.1F, 0.2F, 0.3F);
        assertEquals(palette.length, 3);
        assertColorProperties(palette[0], 77/255F, 70/255F, 61/255F, 1F);
        assertColorProperties(palette[1], 66/255F, 61/255F, 77/255F, 1F);
        assertColorProperties(palette[2], 61/255F, 77/255F, 61/255F, 1F);
    }   
}
