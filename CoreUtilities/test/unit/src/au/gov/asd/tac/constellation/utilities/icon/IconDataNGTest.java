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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for IconData.
 *
 * @author sol695510
 */
public class IconDataNGTest {

    private static MockedStatic<IconData> iconDataStaticMock;
    private static MockedStatic<ImageIO> imageIOStaticMock;
    private static InputStream inputStreamMock;
    private static BufferedImage bufferedImageMock;
    private static IOException IOExceptionMock;

    public IconDataNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        iconDataStaticMock = Mockito.mockStatic(IconData.class);
        imageIOStaticMock = Mockito.mockStatic(ImageIO.class);
        inputStreamMock = Mockito.mock(InputStream.class);
        bufferedImageMock = Mockito.mock(BufferedImage.class);
        IOExceptionMock = Mockito.mock(IOException.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        iconDataStaticMock.close();
        imageIOStaticMock.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        iconDataStaticMock.reset();
        imageIOStaticMock.reset();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getData method, of class IconData.
     */
    @Test
    public void testGetData() {
        System.out.println("testGetData");

        // When niether size is the default (256) or color is null.
        final IconData instance1 = spy(new IconDataImpl());

        final int size1 = 256;
        final Color color1 = new Color(1, 1, 1);

        final byte[] expResult1 = new byte[0];
        final byte[] result1 = instance1.getData(size1, color1);

        verify(instance1, times(1)).createData(Mockito.eq(size1), Mockito.eq(color1));
        assertEquals(result1, expResult1);

        // When color is null.
        final IconData instance2 = spy(new IconDataImpl());

        final int size2 = 0;
        final Color color2 = null;

        final byte[] expResult2 = new byte[0];
        final byte[] result2 = instance2.getData(size2, color2);

        verify(instance2, times(1)).createData(Mockito.eq(size2), Mockito.eq(color2));
        assertEquals(result2, expResult2);

        // When size is the default (256).
        final int size3 = 256;
        final Color color3 = null;

        // When data is null.
        final IconData instance3 = spy(new IconDataImpl());
        instance3.setData(null);

        final byte[] expResult3 = new byte[0];
        final byte[] result3 = instance3.getData(size3, color3);

        verify(instance3, times(1)).createData(Mockito.eq(size3), Mockito.eq(color3));
        assertEquals(result3, expResult3);

        // When data is not null.
        final IconData instance4 = spy(new IconDataImpl());
        instance4.setData(new byte[1]);

        final byte[] expResult4 = new byte[1];
        final byte[] result4 = instance4.getData(size3, color3);

        verify(instance4, times(0)).createData(Mockito.eq(size3), Mockito.eq(color3));
        assertEquals(result4, expResult4);
    }

    /**
     * Test of createData method, of class IconData.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateData() throws IOException {
        System.out.println("testCreateData");

        final IconData instance = spy(new IconDataImpl());

        final int size = 1;
        final Color color = new Color(1, 1, 1);

        imageIOStaticMock.when(() -> ImageIO.read(Mockito.eq(inputStreamMock))).thenReturn(bufferedImageMock);
        iconDataStaticMock.when(() -> IconData.colorImage(Mockito.eq(bufferedImageMock), Mockito.eq(color))).thenReturn(bufferedImageMock);
        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(bufferedImageMock), Mockito.eq(size))).thenReturn(bufferedImageMock);

        final byte[] expResult = new byte[0];
        final byte[] result = instance.createData(size, color);

        imageIOStaticMock.verify(() -> ImageIO.read(Mockito.eq(inputStreamMock)), times(1));
        iconDataStaticMock.verify(() -> IconData.colorImage(Mockito.eq(bufferedImageMock), Mockito.eq(color)), times(1));
        iconDataStaticMock.verify(() -> IconData.scaleImage(Mockito.eq(bufferedImageMock), Mockito.eq(size)), times(1));
        imageIOStaticMock.verify(() -> ImageIO.write(Mockito.eq(bufferedImageMock), Mockito.eq(ConstellationIcon.DEFAULT_ICON_FORMAT), Mockito.any(ByteArrayOutputStream.class)), times(1));

        assertEquals(result, expResult);
    }

    /**
     * Test of createData method, of class IconData, when size is 0, color is
     * null, and image is null since the output returned from ImageIO.read() is
     * null.
     */
    @Test
    public void testCreateData_0AndNull() {
        System.out.println("testCreateData_0AndNull");

        final IconData instance = new IconDataImpl();

        final int size = 0;
        final Color color = null;

        imageIOStaticMock.when(() -> ImageIO.read(Mockito.eq(inputStreamMock))).thenReturn(null);
        iconDataStaticMock.when(() -> IconData.colorImage(Mockito.eq(bufferedImageMock), Mockito.eq(color))).thenReturn(null);
        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(bufferedImageMock), Mockito.eq(size))).thenReturn(null);

        final byte[] expResult = new byte[0];
        final byte[] result = instance.createData(size, color);

        imageIOStaticMock.verify(() -> ImageIO.read(Mockito.eq(inputStreamMock)), times(1));
        iconDataStaticMock.verify(() -> IconData.colorImage(Mockito.eq(null), Mockito.eq(color)), times(0));
        iconDataStaticMock.verify(() -> IconData.scaleImage(Mockito.eq(null), Mockito.eq(size)), times(0));
        imageIOStaticMock.verify(() -> ImageIO.write(Mockito.eq(null), Mockito.eq(ConstellationIcon.DEFAULT_ICON_FORMAT), Mockito.any(ByteArrayOutputStream.class)), times(0));

        assertEquals(result, expResult);
    }

    /**
     * Test of createData method, of class IconData, when the output returned
     * from createInputStream() is null.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateData_inputStreamIsNull() throws IOException {
        System.out.println("testCreateData_inputStreamIsNull");

        final IconData instance = spy(new IconDataImpl());
        when(instance.createInputStream()).thenReturn(null);

        final int size = 0;
        final Color color = null;

        final byte[] expResult = new byte[0];
        final byte[] result = instance.createData(size, color);

        imageIOStaticMock.verifyNoInteractions();
        iconDataStaticMock.verifyNoInteractions();

        assertEquals(result, expResult);
    }

    /**
     * Test of createData method, of class IconData, when the output returned
     * from createInputStream() throws IOException.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateData_inputStreamThrowsException() throws IOException {
        System.out.println("testCreateData_inputStreamThrowsException");

        final IconData instance = spy(new IconDataImpl());
        when(instance.createInputStream()).thenThrow(IOExceptionMock);

        final int size = 0;
        final Color color = null;

        final byte[] expResult = new byte[0];
        final byte[] result = instance.createData(size, color);

        imageIOStaticMock.verifyNoInteractions();
        iconDataStaticMock.verifyNoInteractions();

        assertEquals(result, expResult);
    }

    /**
     * Test of colorImage method, of class IconData.
     */
    @Test
    public void testColorImage() {
        System.out.println("testColorImage");

        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final Color color = new Color(1, 1, 1);

        final Color pixel = new Color(image.getRGB(0, 0), true);
        final int blendRed = ((pixel.getRed() * (255 - color.getAlpha())) + (color.getRed() * color.getAlpha())) / 255;
        final int blendGreen = ((pixel.getGreen() * (255 - color.getAlpha())) + (color.getGreen() * color.getAlpha())) / 255;
        final int blendBlue = ((pixel.getBlue() * (255 - color.getAlpha())) + (color.getBlue() * color.getAlpha())) / 255;
        final Color blend = new Color(blendRed, blendGreen, blendBlue, pixel.getAlpha());

        iconDataStaticMock.when(() -> IconData.colorImage(Mockito.eq(image), Mockito.eq(color))).thenCallRealMethod();
        final BufferedImage coloredImage = IconData.colorImage(image, color);

        final int expResult = blend.getRGB();
        final int result = coloredImage.getRGB(0, 0);

        assertEquals(result, expResult);
    }

    /**
     * Test of colorImage method, of class IconData, when image is null.
     */
    @Test
    public void testColorImage_imageIsNull() {
        System.out.println("testColorImage_imageIsNull");

        final BufferedImage image = null;
        final Color color = new Color(1, 1, 1);

        iconDataStaticMock.when(() -> IconData.colorImage(Mockito.eq(image), Mockito.eq(color))).thenCallRealMethod();
        final BufferedImage coloredImage = IconData.colorImage(image, color);

        final BufferedImage expResult = image;
        final BufferedImage result = coloredImage;

        assertEquals(result, expResult);
    }

    /**
     * Test of colorImage method, of class IconData, when color is null.
     */
    @Test
    public void testColorImage_colorIsNull() {
        System.out.println("testColorImage_colorIsNull");

        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final Color color = null;

        iconDataStaticMock.when(() -> IconData.colorImage(Mockito.eq(image), Mockito.eq(color))).thenCallRealMethod();
        final BufferedImage coloredImage = IconData.colorImage(image, color);

        final BufferedImage expResult = image;
        final BufferedImage result = coloredImage;

        assertEquals(result, expResult);
    }

    /**
     * Test of scaleImage method, of class IconData.
     */
    @Test
    public void testScaleImage() {
        System.out.println("testScaleImage");

        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final int size = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image), Mockito.eq(size))).thenCallRealMethod();
        final BufferedImage scaledImage = IconData.scaleImage(image, size);

        final int expResult = size * size;
        final int result = scaledImage.getHeight() * scaledImage.getWidth();

        assertEquals(result, expResult);
    }

    /**
     * Test of scaleImage method, of class IconData, when image is null.
     */
    @Test
    public void testScaleImage_imageIsNull() {
        System.out.println("testScaleImage_imageIsNull");

        final BufferedImage image = null;
        final int size = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image), Mockito.eq(size))).thenCallRealMethod();
        final BufferedImage scaledImage = IconData.scaleImage(image, size);

        final BufferedImage expResult = null;
        final BufferedImage result = scaledImage;

        assertEquals(result, expResult);
    }

    /**
     * Test of scaleImage method, of class IconData, when image type is not
     * TYPE_4BYTE_ABGR.
     */
    @Test
    public void testScaleImage_imageIsNotTYPE_4BYTE_ABGR() {
        System.out.println("testScaleImage_imageIsNotTYPE_4BYTE_ABGR");

        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        final int size = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image), Mockito.eq(size))).thenCallRealMethod();
        final BufferedImage scaledImage = IconData.scaleImage(image, size);

        final int expResult = BufferedImage.TYPE_4BYTE_ABGR;
        final int result = scaledImage.getType();

        assertEquals(result, expResult);
    }

    /**
     * Test of scaleImage method, of class IconData, when size equals the
     * image's current sizes for width or height.
     */
    @Test
    public void testScaleImage_sizeEqualsCurrentWidthOrHeight() {
        System.out.println("testScaleImage_sizeEqualsCurrentWidthOrHeight");

        // When image's current width is 2 and current height equals new size.
        final BufferedImage image1 = new BufferedImage(2, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final int size1 = 1;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image1), Mockito.eq(size1))).thenCallRealMethod();
        final BufferedImage scaledImage1 = IconData.scaleImage(image1, size1);

        final int expResult1 = 2;
        final int result1 = scaledImage1.getWidth();

        assertEquals(result1, expResult1);

        // When image's current height is 2 and current width equals new size.
        final BufferedImage image2 = new BufferedImage(1, 2, BufferedImage.TYPE_4BYTE_ABGR);
        final int size2 = 1;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image2), Mockito.eq(size2))).thenCallRealMethod();
        final BufferedImage scaledImage2 = IconData.scaleImage(image2, size2);

        final int expResult2 = 2;
        final int result2 = scaledImage2.getHeight();

        assertEquals(result2, expResult2);
    }

    /**
     * Test of equals method, of class IconData.
     */
    @Test
    public void testEquals() {
        System.out.println("testEquals");

        final IconData instance = new IconDataImpl();

        // When obj is null.
        Object obj = null;
        assertFalse(instance.equals(obj));

        // When the class of obj is not the same class as this.
        obj = new Object();
        assertFalse(instance.equals(obj));

        // When the class of obj is the same class as this but has a different value for data.
        obj = new IconDataImpl();

        instance.setData(new byte[1]);
        ((IconData) obj).setData(new byte[2]);

        assertFalse(instance.equals(obj));

        // When the class of obj is the same class as this and has the same value for data.
        obj = new IconDataImpl();

        instance.setData(new byte[1]);
        ((IconData) obj).setData(new byte[1]);

        assertTrue(instance.equals(obj));
    }

    /**
     * Test of toString method, of class IconData.
     */
    @Test
    public void testToString() {
        System.out.println("testToString");

        final IconData instance = new IconDataImpl();

        // When data is null.
        instance.setData(null);

        final String expResult1 = String.format("Image of %d bytes", 0);
        final String result1 = instance.toString();

        assertEquals(result1, expResult1);

        // When data is not null.
        instance.setData(new byte[2]);

        final String expResult2 = String.format("Image of %d bytes", 2);
        final String result2 = instance.toString();

        assertEquals(result2, expResult2);
    }

    private class IconDataImpl extends IconData {

        @Override
        protected InputStream createInputStream() throws IOException {
            return inputStreamMock;
        }
    }
}
