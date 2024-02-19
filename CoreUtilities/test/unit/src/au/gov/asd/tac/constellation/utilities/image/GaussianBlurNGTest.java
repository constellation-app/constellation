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
package au.gov.asd.tac.constellation.utilities.image;

import au.gov.asd.tac.constellation.utilities.image.GaussianBlur.BoxBlurType;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class GaussianBlurNGTest {

    public GaussianBlurNGTest() {
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
     * Test of gaussianBlurReal method, of class GaussianBlur. When the dimensions of the source channel is not equal to height x width
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Source channel does not have the dimensions provided.")
    public void testGaussianBlurRealBadSourceChannelDimensions() {
        System.out.println("gaussianBlurRealBadSourceChannelDimensions");
        final float[] sourceChannel = new float[1];
        final int width = 0;
        final int height = 0;
        GaussianBlur.gaussianBlurReal(sourceChannel, new float[1], width, height, 0);
    }

    /**
     * Test of gaussianBlurReal method, of class GaussianBlur. When target channel is smaller than the source channel
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Target channel is smaller than source channel.")
    public void testGaussianBlurRealBadTargetChannelSize() {
        System.out.println("gaussianBlurRealBadTargetChannelSize");
        final float[] sourceChannel = new float[1];
        final float[] targetChannel = new float[0];
        GaussianBlur.gaussianBlurReal(sourceChannel, targetChannel, 1, 1, 0);
    }

    /**
     * Test of gaussianBlurReal method, of class GaussianBlur.
     */
    @Test
    public void testGaussianBlurReal() {
        System.out.println("gaussianBlurReal");
        final float[] sourceChannel = {1F, 2F, 3F, 4F, 5F, 6F};
        final float[] targetChannel = new float[8];
        final int width = 3;
        final int height = 2;
        final int radius = 2;

        GaussianBlur.gaussianBlurReal(sourceChannel, targetChannel, width, height, radius);

        final float[] expResult = {2.8245978F, 3.2004867F, 3.5763752F, 3.4236248F, 3.7995133F, 4.175402F, 0F, 0F};
        assertEquals(targetChannel, expResult);
    }

    /**
     * Test of gaussianBlurBox method, of class GaussianBlur. When the dimensions of the source channel is not equal to height x width
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Source channel does not have the dimensions provided.")
    public void testGaussianBlurBoxBadSourceChannelDimensions() {
        System.out.println("gaussianBlurBoxBadSourceChannelDimensions");
        final float[] sourceChannel = new float[1];
        final int width = 0;
        final int height = 0;
        GaussianBlur.gaussianBlurBox(sourceChannel, new float[1], width, height, 0, 0, null);
    }

    /**
     * Test of gaussianBlurBox method, of class GaussianBlur. When target channel is smaller than the source channel
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Target channel is smaller than source channel.")
    public void testGaussianBlurBoxBadTargetChannelSize() {
        System.out.println("gaussianBlurBoxBadTargetChannelSize");
        final float[] sourceChannel = new float[1];
        final float[] targetChannel = new float[0];
        GaussianBlur.gaussianBlurBox(sourceChannel, targetChannel, 1, 1, 0, 0, null);
    }

    /**
     * Test of gaussianBlurBox method, of class GaussianBlur. Null BoxBlurType
     */
    @Test(expectedExceptions = {NullPointerException.class})
    public void testGaussianBlurBoxNull() {
        System.out.println("gaussianBlurBoxNull");
        final float[] sourceChannel = new float[6];
        final float[] targetChannel = new float[8];
        final int width = 3;
        final int height = 2;
        final int passes = 1;

        GaussianBlur.gaussianBlurBox(sourceChannel, targetChannel, width, height, 0, passes, null);
    }

    /**
     * Test of gaussianBlurBox method, of class GaussianBlur. Standard BoxBlurType
     */
    @Test
    public void testGaussianBlurBoxStandard() {
        System.out.println("gaussianBlurBoxStandard");
        final float[] sourceChannel = {1F, 2F, 3F, 4F, 5F, 6F};
        final float[] targetChannel = new float[8];
        final int width = 3;
        final int height = 2;
        final int radius = 2;
        final int passes = 1;

        GaussianBlur.gaussianBlurBox(sourceChannel, targetChannel, width, height, radius, passes, BoxBlurType.STANDARD);

        final float[] expResult = {3F, 3.2857144F, 3.5714285F, 3.4285715F, 3.7142856F, 4F, 0F, 0F};
        assertEquals(targetChannel, expResult);
    }

    /**
     * Test of gaussianBlurBox method, of class GaussianBlur. Fast BoxBlurType
     */
    @Test
    public void testGaussianBlurBoxFast() {
        System.out.println("gaussianBlurBoxFast");
        final float[] sourceChannel = {1F, 2F, 3F, 4F, 5F, 6F};
        final float[] targetChannel = new float[8];
        final int width = 3;
        final int height = 2;
        final int radius = 2;
        final int passes = 1;

        GaussianBlur.gaussianBlurBox(sourceChannel, targetChannel, width, height, radius, passes, BoxBlurType.FAST);

        final float[] expResult = {0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F};
        assertEquals(targetChannel, expResult);
    }
    
    /**
     * Test of gaussianBlurBox method, of class GaussianBlur. Fast BoxBlurType
     */
    @Test
    public void testGaussianBlurBoxFastest() {
        System.out.println("gaussianBlurBoxFastest");
        final float[] sourceChannel = {1F, 2F, 3F, 4F, 5F, 6F};
        //final float[] targetChannel = new float[8];
        final float[] targetChannel = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        final int width = 2;
        final int height = 3;
        final int radius = 2;
        final int passes = 1;

        GaussianBlur.gaussianBlurBox(sourceChannel, targetChannel, width, height, radius, passes, BoxBlurType.FASTEST);

        final float[] expResult = {0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F};
        assertEquals(targetChannel, expResult);
    }

    /**
     * Test of normalise method, of class GaussianBlur.
     */
    @Test
    public void testNormalise() {
        System.out.println("normalise");
        final float[] sourceChannel = {3F, 2F, 1F, 4F, 5F, 6F};

        GaussianBlur.normalise(sourceChannel, 10);

        final float[] expResult = {4F, 2F, 0F, 6F, 8F, 10F};
        assertEquals(sourceChannel, expResult);
    }

    /**
     * Test of colorise method, of class GaussianBlur. Threshold too small
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Threshold must be a value between 0 and 255")
    public void testColoriseThresholdTooSmall() {
        System.out.println("coloriseThresholdTooSmall");
        final int threshold = -1;
        GaussianBlur.colorise(null, null, threshold, 0F);
    }

    /**
     * Test of colorise method, of class GaussianBlur. Threshold too big
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Threshold must be a value between 0 and 255")
    public void testColoriseThresholdTooBig() {
        System.out.println("coloriseThresholdTooBig");
        final int threshold = 256;
        GaussianBlur.colorise(null, null, threshold, 0F);
    }

    /**
     * Test of colorise method, of class GaussianBlur. Target channel smaller than source channel
     */
    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = "Target channel is smaller than source channel.")
    public void testColoriseBadTargetChannelSize() {
        System.out.println("coloriseBadTargetChannelSize");
        final float[] sourceChannel = new float[2];
        final int[] targetChannel = new int[1];
        GaussianBlur.colorise(sourceChannel, targetChannel, 100, 0F);
    }

    /**
     * Test of colorise method, of class GaussianBlur.
     */
    @Test
    public void testColorise() {
        System.out.println("colorise");
        final float[] sourceChannel = {1F, 2F, 3F, 4F, 5F};
        final int[] targetChannel = new int[5];
        final int threshold = 100;
        final float severity = 2F;

        GaussianBlur.colorise(sourceChannel, targetChannel, threshold, severity);

        // since the result numbers don't really say much 
        // (due to using the using the signed bit as part of the ARGB value), 
        // we'll compare the expected alpha and color values contained within

        // alpha
        assertEquals(targetChannel[0] >>> 24, 0);
        assertEquals(targetChannel[1] >>> 24, 0);
        assertEquals(targetChannel[2] >>> 24, 154);
        assertEquals(targetChannel[3] >>> 24, 192);
        assertEquals(targetChannel[4] >>> 24, 192);

        //color
        assertEquals(targetChannel[0] & 0xFFFFFF, 0x0034F8);
        assertEquals(targetChannel[1] & 0xFFFFFF, 0x3E9350);
        assertEquals(targetChannel[2] & 0xFFFFFF, 0xB2C01A);
        assertEquals(targetChannel[3] & 0xFFFFFF, 0xFFA413);
        assertEquals(targetChannel[4] & 0xFFFFFF, 0xFF2A00);
    }   
}