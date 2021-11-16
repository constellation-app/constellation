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
     * @param colour
     * @param expectedRedValue
     * @param expectedGreenValue
     * @param expectedBlueValue
     * @param expectedAlphaValue 
     */
    private void assertColourProperties(final ConstellationColor colour, final float expectedRedValue, final float expectedGreenValue, 
            final float expectedBlueValue, final float expectedAlphaValue) {
        assertEquals(colour.getRed(), expectedRedValue);
        assertEquals(colour.getGreen(), expectedGreenValue);
        assertEquals(colour.getBlue(), expectedBlueValue);
        assertEquals(colour.getAlpha(), expectedAlphaValue);
    }

    /**
     * Test of getColorValue method, of class ConstellationColor. 1 parameter
     */
    @Test
    public void testGetColorValueOneParameter() {
        System.out.println("getColorValueOneParameter");
        
        final ConstellationColor nullColour = ConstellationColor.getColorValue(null);
        assertEquals(nullColour, null);
        
        final ConstellationColor namedColour = ConstellationColor.getColorValue("Banana");
        assertEquals(namedColour, ConstellationColor.BANANA);
        
        final ConstellationColor namedGreyColour = ConstellationColor.getColorValue("Gray");
        assertEquals(namedGreyColour, ConstellationColor.GREY);
        
        final ConstellationColor rgbColour = ConstellationColor.getColorValue("rgb255255000");
        assertColourProperties(rgbColour, 1F, 1F, 0F, 1F);
        
        final ConstellationColor rgbCommaColour = ConstellationColor.getColorValue("0.2,1,0.6,0.7");
        assertColourProperties(rgbCommaColour, 0.2F, 1F, 0.6F, 0.7F);
        
        final ConstellationColor htmlColour = ConstellationColor.getColorValue("#6699CC");
        assertColourProperties(htmlColour, 0.4F, 0.6F, 0.8F, 1F);
        
        final ConstellationColor notAColour = ConstellationColor.getColorValue("Not a colour");
        assertEquals(notAColour, null);
    }

    /**
     * Test of getColorValue method, of class ConstellationColor. 4 parameters
     */
    @Test
    public void testGetColorValueFourParameters() {
        System.out.println("getColorValueFourParameters");
        
        final ConstellationColor namedColour = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        assertEquals(namedColour, ConstellationColor.BANANA);
        
        final ConstellationColor unnamedColour = ConstellationColor.getColorValue(0.5F, 0.5F, 0.5F, 0.5F);
        assertEquals(unnamedColour.getName(), null);
        assertEquals(unnamedColour.getRGBString(), "0.500000,0.500000,0.500000,0.500000");
    }

    /**
     * Test of getJavaColor method, of class ConstellationColor.
     */
    @Test
    public void testGetJavaColor() {
        System.out.println("getJavaColor");
        
        final ConstellationColor constellationColour = ConstellationColor.getColorValue("Banana");
        final Color javaColour = constellationColour.getJavaColor();
        assertEquals(javaColour.getRed(), 254);
        assertEquals(javaColour.getGreen(), 255);
        assertEquals(javaColour.getBlue(), 106);
        assertEquals(javaColour.getAlpha(), 255);
    }

    /**
     * Test of getJavaFXColor method, of class ConstellationColor.
     */
    @Test
    public void testGetJavaFXColor() {
        System.out.println("getJavaFXColor");
        
        final ConstellationColor constellationColour = ConstellationColor.getColorValue("GoldenRod");
        final javafx.scene.paint.Color javaFxColour = constellationColour.getJavaFXColor();
        assertEquals(javaFxColour.getRed(), 1.0);
        assertEquals(javaFxColour.getGreen(), 0.75);
        assertEquals(javaFxColour.getBlue(), 0.0);
        assertEquals(javaFxColour.getOpacity(), 1.0);
    }

    /**
     * Test of getHtmlColor method, of class ConstellationColor.
     */
    @Test
    public void testGetHtmlColor() {
        System.out.println("getHtmlColor");
        
        final ConstellationColor constellationColour = ConstellationColor.getColorValue("Banana");
        final String htmlColour = constellationColour.getHtmlColor();
        assertEquals(htmlColour, "#feff6a");
    }

    /**
     * Test of fromJavaColor method, of class ConstellationColor.
     */
    @Test
    public void testFromJavaColor() {
        System.out.println("fromJavaColor");
        
        final ConstellationColor nullColour = ConstellationColor.fromJavaColor(null);
        assertEquals(nullColour, null);
        
        final ConstellationColor constellationColour = ConstellationColor.fromJavaColor(Color.CYAN);
        assertColourProperties(constellationColour, 0F, 1F, 1F, 1F);
    }
    
    /**
     * Test of fromFXColor method, of class ConstellationColor.
     */
    @Test
    public void testFromFXColor() {
        System.out.println("fromFXColor");
        
        final ConstellationColor nullColour = ConstellationColor.fromFXColor(null);
        assertEquals(nullColour, null);
        
        final ConstellationColor constellationColour = ConstellationColor.fromFXColor(javafx.scene.paint.Color.CYAN);
        assertColourProperties(constellationColour, 0F, 1F, 1F, 1F);
    }

    /**
     * Test of fromHtmlColor method, of class ConstellationColor.
     */
    @Test
    public void testFromHtmlColor() {
        System.out.println("fromHtmlColor");
        
        final ConstellationColor nullColour = ConstellationColor.fromHtmlColor(null);
        assertEquals(nullColour, null);
        
        final ConstellationColor wrongLengthColour = ConstellationColor.fromHtmlColor("#00000000000");
        assertEquals(wrongLengthColour, null);
        
        final ConstellationColor noHashColour = ConstellationColor.fromHtmlColor("FFEEDDC");
        assertEquals(noHashColour, null);
        
        final ConstellationColor ucConstellationColour = ConstellationColor.fromHtmlColor("#FFEEDD");
        assertColourProperties(ucConstellationColour, 1F, 238/255F, 221/255F, 1F);
        
        final ConstellationColor lcConstellationColour = ConstellationColor.fromHtmlColor("#ffeedd");
        assertColourProperties(lcConstellationColour, 1F, 238/255F, 221/255F, 1F);
    }

    /**
     * Test of getContrastHtmlColor method, of class ConstellationColor.
     */
    @Test
    public void testGetContrastHtmlColor() {
        System.out.println("getContrastHtmlColor");
        
        final ConstellationColor nullColour = ConstellationColor.getContrastHtmlColor(null);
        assertEquals(nullColour, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColour = ConstellationColor.getContrastHtmlColor("#FFEEDD");
        assertEquals(darkContrastColour, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColour = ConstellationColor.getContrastHtmlColor("#221100");
        assertEquals(lightContrastColour, ConstellationColor.WHITE);
        
        final ConstellationColor middleColour = ConstellationColor.getContrastHtmlColor("#808080");
        assertEquals(middleColour, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastRGBColor method, of class ConstellationColor.
     */
    @Test
    public void testGetContrastRGBColor() {
        System.out.println("getContrastRGBColor");

        final ConstellationColor nullColour = ConstellationColor.getContrastRGBColor(null);
        assertEquals(nullColour, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColour = ConstellationColor.getContrastRGBColor("RGB255225200");
        assertEquals(darkContrastColour, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColour = ConstellationColor.getContrastRGBColor("RGB000050100");
        assertEquals(lightContrastColour, ConstellationColor.WHITE);
        
        final ConstellationColor middleColour = ConstellationColor.getContrastRGBColor("RGB128128128");
        assertEquals(middleColour, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastColor method, of class ConstellationColor. ConstellationColor parameter
     */
    @Test
    public void testGetContrastColorConstellationColor() {
        System.out.println("getContrastColorConstellationColor");
        
        final ConstellationColor nullColour = ConstellationColor.getContrastColor((ConstellationColor) null);
        assertEquals(nullColour, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColour = ConstellationColor.getContrastColor(ConstellationColor.BANANA);
        assertEquals(darkContrastColour, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColour = ConstellationColor.getContrastColor(ConstellationColor.LIGHT_BLUE);
        assertEquals(lightContrastColour, ConstellationColor.WHITE);
        
        final ConstellationColor middleColour = ConstellationColor.getContrastColor(ConstellationColor.getColorValue(0.5F, 0.5F, 0.5F, 1));
        assertEquals(middleColour, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastfromRgbWithCommaColor method, of class ConstellationColor.
     */
    @Test
    public void testGetContrastfromRgbWithCommaColor() {
        System.out.println("getContrastfromRgbWithCommaColor");
        
        final ConstellationColor nullColour = ConstellationColor.getContrastfromRgbWithCommaColor(null);
        assertEquals(nullColour, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColour = ConstellationColor.getContrastfromRgbWithCommaColor("1,0.95,0.9");
        assertEquals(darkContrastColour, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColour = ConstellationColor.getContrastfromRgbWithCommaColor("[0,0.5,1]");
        assertEquals(lightContrastColour, ConstellationColor.WHITE);
        
        final ConstellationColor middleColour = ConstellationColor.getContrastfromRgbWithCommaColor("0.5,0.5,0.5");
        assertEquals(middleColour, ConstellationColor.BLACK);
    }

    /**
     * Test of getContrastColor method, of class ConstellationColor. Java Color parameter
     */
    @Test
    public void testGetContrastColorJavaColor() {
        System.out.println("getContrastColorJavaColor");
        
        final ConstellationColor nullColour = ConstellationColor.getContrastColor((Color) null);
        assertEquals(nullColour, ConstellationColor.BLACK);
        
        final ConstellationColor darkContrastColour = ConstellationColor.getContrastColor(Color.LIGHT_GRAY);
        assertEquals(darkContrastColour, ConstellationColor.BLACK);
        
        final ConstellationColor lightContrastColour = ConstellationColor.getContrastColor(Color.DARK_GRAY);
        assertEquals(lightContrastColour, ConstellationColor.WHITE);
        
        final ConstellationColor middleColour = ConstellationColor.getContrastColor(Color.GRAY);
        assertEquals(middleColour, ConstellationColor.BLACK);
    }
    
    /**
     * Test of fromRgbColor method, of class ConstellationColor.
     */
    @Test
    public void testFromRgbColor() {
        System.out.println("fromRgbColor");
        
        final ConstellationColor nullColour = ConstellationColor.fromRgbColor(null);
        assertEquals(nullColour, null);
        
        final ConstellationColor constellationColour = ConstellationColor.fromRgbColor("RGB000255255");
        assertColourProperties(constellationColour, 0F, 1F, 1F, 1F);
    }

    /**
     * Test of fromRgbWithCommaColor method, of class ConstellationColor.
     */
    @Test
    public void testFromRgbWithCommaColor() {
        System.out.println("fromRgbWithCommaColor");
        
        final ConstellationColor nullColour = ConstellationColor.fromRgbWithCommaColor(null);
        assertEquals(nullColour, null);
        
        final ConstellationColor constellationColourNoBracket = ConstellationColor.fromRgbWithCommaColor("0,1,1");
        assertColourProperties(constellationColourNoBracket, 0F, 1F, 1F, 1F);
        
        final ConstellationColor constellationColourBracket = ConstellationColor.fromRgbWithCommaColor("[0,1,1]");
        assertColourProperties(constellationColourBracket, 0F, 1F, 1F, 1F);
    }

    /**
     * Test of equals method, of class ConstellationColor.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final ConstellationColor colour1 = ConstellationColor.getColorValue("Banana");
        
        final ConstellationColor colour2 = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        final ConstellationColor colour3 = ConstellationColor.fromHtmlColor("#FEFF6A");
        
        final ConstellationColor colour4 = ConstellationColor.getColorValue("GoldenRod");
        
        assertTrue(colour1.equals(colour2));
        assertTrue(colour1.equals(colour3));
        assertTrue(colour1.equals("Banana"));
        assertFalse(colour1.equals(colour4));
        assertFalse(colour1.equals("GoldenRod"));
    }

    /**
     * Test of toString method, of class ConstellationColor.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final ConstellationColor colour1 = ConstellationColor.getColorValue("Banana");        
        final ConstellationColor colour2 = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        final ConstellationColor colour3 = ConstellationColor.getColorValue(253/255F, 1F, 106/255F, 1F);
        
        assertEquals(colour1.toString(), "Banana");
        assertEquals(colour2.toString(), "Banana");
        assertEquals(colour3.toString(), "#fdff6a");
    }

    /**
     * Test of compareTo method, of class ConstellationColor.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        final ConstellationColor colour1 = ConstellationColor.getColorValue("Banana");        
        final ConstellationColor colour2 = ConstellationColor.getColorValue(254/255F, 1F, 106/255F, 1F);
        final ConstellationColor colour3 = ConstellationColor.getColorValue(253/255F, 1F, 106/255F, 1F);
        final ConstellationColor colour4 = ConstellationColor.getColorValue(1F, 1F, 106/255F, 1F);
        final ConstellationColor colour5 = ConstellationColor.getColorValue(253/255F, 0.9F, 106/255F, 1F);
        final ConstellationColor colour6 = ConstellationColor.getColorValue(253/255F, 1F, 120/255F, 1F);
        final ConstellationColor colour7 = ConstellationColor.getColorValue(253/255F, 1F, 106/255F, 0.4F);
        final ConstellationColor colour8 = ConstellationColor.fromHtmlColor("#FDFF6A");
        
        assertEquals(colour1.compareTo(colour2), 0);
        assertEquals(colour1.compareTo(colour3), 1);
        assertEquals(colour3.compareTo(colour1), -1);
        assertEquals(colour3.compareTo(colour4), -1);
        assertEquals(colour3.compareTo(colour5), 1);
        assertEquals(colour3.compareTo(colour6), -1);
        assertEquals(colour3.compareTo(colour7), 1);
        assertEquals(colour3.compareTo(colour8), 0);
    }

    /**
     * Test of createPalette method, of class ConstellationColor. 3 parameters
     */
    @Test
    public void testCreatePaletteThreeParameters() {
        System.out.println("createPaletteThreeParameters");
        
        final ConstellationColor[] zeroColourPalette = ConstellationColor.createPalette(0, 0.6F, 0.4F);
        assertEquals(zeroColourPalette.length, 0);
        
        final ConstellationColor[] oneColourPalette = ConstellationColor.createPalette(1, 0.6F, 0.4F);
        assertEquals(oneColourPalette.length, 1);
        assertColourProperties(oneColourPalette[0], 0.4F, 41/255F, 41/255F, 1F);
        
        final ConstellationColor[] twoColourPalette = ConstellationColor.createPalette(2, 0.6F, 0.4F);
        assertEquals(twoColourPalette.length, 2);
        assertColourProperties(twoColourPalette[1], 41/255F, 0.4F, 0.4F, 1F);
        
        final ConstellationColor[] moreColourPalette = ConstellationColor.createPalette(4, 0.6F, 0.4F);
        assertEquals(moreColourPalette.length, 4);
        assertColourProperties(moreColourPalette[2], 71/255F, 41/255F, 0.4F, 1F);
        assertColourProperties(moreColourPalette[3], 71/255F, 0.4F, 41/255F, 1F);
    }

    /**
     * Test of createPalette method, of class ConstellationColor. 1 parameter
     */
    @Test
    public void testCreatePaletteOneParameter() {
        System.out.println("createPaletteOneParameter");
        
        final ConstellationColor[] zeroColourPalette = ConstellationColor.createPalette(0);
        assertEquals(zeroColourPalette.length, 0);
        
        final ConstellationColor[] oneColourPalette = ConstellationColor.createPalette(1);
        assertEquals(oneColourPalette.length, 1);
        assertColourProperties(oneColourPalette[0], 1F, 0F, 0F, 1F);
        
        final ConstellationColor[] twoColourPalette = ConstellationColor.createPalette(2);
        assertEquals(twoColourPalette.length, 2);
        assertColourProperties(twoColourPalette[1], 0F, 1F, 1F, 1F);
        
        final ConstellationColor[] moreColourPalette = ConstellationColor.createPalette(4);
        assertEquals(moreColourPalette.length, 4);
        assertColourProperties(moreColourPalette[2], 128/255F, 0F, 1F, 1F);
        assertColourProperties(moreColourPalette[3], 128/255F, 1F, 0F, 1F);
    }

    /**
     * Test of createLinearPalette method, of class ConstellationColor. Not enough colours to create palette
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateLinearPaletteNotEnoughColours() {
        System.out.println("createLinearPaletteNotEnoughColours");
        
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
        assertColourProperties(palette[0], 0F, 1F, 1F, 1F);
        assertColourProperties(palette[1], 0.5F, 0.875F, 0.5F, 1F);
        assertColourProperties(palette[2], 1F, 0.75F, 0F, 1F);
    }

    /**
     * Test of createPalettePhi method, of class ConstellationColor.
     */
    @Test
    public void testCreatePalettePhi() {
        System.out.println("createPalettePhi");
        
        final ConstellationColor[] palette = ConstellationColor.createPalettePhi(3, 0.1F, 0.2F, 0.3F);
        assertEquals(palette.length, 3);
        assertColourProperties(palette[0], 77/255F, 70/255F, 61/255F, 1F);
        assertColourProperties(palette[1], 66/255F, 61/255F, 77/255F, 1F);
        assertColourProperties(palette[2], 61/255F, 77/255F, 61/255F, 1F);
    }   
}
