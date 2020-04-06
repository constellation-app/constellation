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
package au.gov.asd.tac.constellation.utilities.color;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Color Value Test.
 *
 * @author arcturus
 */
public class ColorValueNGTest {

    public ColorValueNGTest() {
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
     * Test of getName method, of class ColorValue.
     */
    @Test
    public void testGetName() {
        ConstellationColor instance = ConstellationColor.getColorValue("Amethyst");
        String expResult = "Amethyst";
        String result = instance.getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of getRed method, of class ColorValue.
     */
    @Test
    public void testGetRed() {
        ConstellationColor instance = ConstellationColor.getColorValue("Amethyst");
        float expResult = 155 / 255f;
        float result = instance.getRed();
        assertEquals(result, expResult);
    }

    /**
     * Test of getGreen method, of class ColorValue.
     */
    @Test
    public void testGetGreen() {
        ConstellationColor instance = ConstellationColor.getColorValue("Amethyst");
        float expResult = 89 / 255f;
        float result = instance.getGreen();
        assertEquals(result, expResult);
    }

    /**
     * Test of getBlue method, of class ColorValue.
     */
    @Test
    public void testGetBlue() {
        ConstellationColor instance = ConstellationColor.getColorValue("Amethyst");
        float expResult = 182 / 255f;
        float result = instance.getBlue();
        assertEquals(result, expResult);
    }

    /**
     * Test of getAlpha method, of class ColorValue.
     */
    @Test
    public void testGetAlpha() {
        ConstellationColor instance = ConstellationColor.getColorValue("Amethyst");
        float expResult = 255 / 255f;
        float result = instance.getAlpha();
        assertEquals(result, expResult);
    }

    /**
     * Test of getRGBString method, of class ColorValue.
     */
    @Test
    public void testGetRGBString() {
        ConstellationColor instance = ConstellationColor.getColorValue("Amethyst");
        String expResult = String.format("%f,%f,%f,%f", 155 / 255f, 89 / 255f, 182 / 255f, 255 / 255f);
        String result = instance.getRGBString();
        assertEquals(result, expResult);
    }

    /**
     * Test of getNamedColorValue method, of class ColorValue.
     */
    @Test
    public void testGetNamedColorValueWithLabel() {
        String label = "Amethyst";
        ConstellationColor expResult = ConstellationColor.getColorValue("Amethyst");
        ConstellationColor result = ConstellationColor.getColorValue(label);
        assertEquals(result, expResult);
    }

    @Test
    public void testGetNamedColorValueWithCommaSepLabel() {
        String label = 155 / 255f + "," + 89 / 255f + "," + 182 / 255f + "," + 255 / 255f;
        ConstellationColor expResult = ConstellationColor.getColorValue("Amethyst");
        ConstellationColor result = ConstellationColor.getColorValue(label);
        assertEquals(result, expResult);
    }

    @Test
    public void testGetNamedColorValueWithGrayLabel() {
        String label = "gray";
        String expResult = "Grey";
        String result = ConstellationColor.getColorValue(label).getName();
        assertEquals(result, expResult);
    }

    /**
     * Test of fromHtmlColor method, of class ColorValue.
     */
    @Test
    public void testFromHtmlColor() {
        String html = "#000000";
        ConstellationColor expResult = ConstellationColor.getColorValue(0f, 0f, 0f, 1f);
        ConstellationColor result = ConstellationColor.fromHtmlColor(html);
        assertEquals(result, expResult);
    }
}
