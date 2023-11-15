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

import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
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

    private final IconData instanceWithData;

    public IconDataNGTest() {
        instanceWithData = new IconDataImpl();
        instanceWithData.getData();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        iconDataStaticMock = Mockito.mockStatic(IconData.class);
        imageIOStaticMock = Mockito.mockStatic(ImageIO.class);
        inputStreamMock = Mockito.mock(InputStream.class);
        bufferedImageMock = Mockito.mock(BufferedImage.class);
        IOExceptionMock = Mockito.mock(IOException.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        iconDataStaticMock.close();
        imageIOStaticMock.close();
    }

    /**
     * Test of getData method, of class IconData, when size not default and
     * color not null.
     */
    @Test
    public void testGetData() {
        System.out.println("testGetData");

        final IconData instance = spy(new IconDataImpl());

        final int size = 1;
        final Color color = new Color(1, 1, 1);

        final byte[] expResult = new byte[0];
        final byte[] result = instance.getData(size, color);

        verify(instance, times(1)).createData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }

    /**
     * Test of getData method, of class IconData, when size is default and color
     * not null.
     */
    @Test
    public void testGetData_sizeIsDefault() {
        System.out.println("testGetData_sizeIsDefault");

        final IconData instance = spy(new IconDataImpl());

        final int size = ConstellationIcon.DEFAULT_ICON_SIZE;
        final Color color = new Color(1, 1, 1);

        final byte[] expResult = new byte[0];
        final byte[] result = instance.getData(size, color);

        verify(instance, times(1)).createData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }

    /**
     * Test of getData method, of class IconData, when size not default and
     * color is null.
     */
    @Test
    public void testGetData_colorIsNull() {
        System.out.println("testGetData_colorIsNull");

        final IconData instance = spy(new IconDataImpl());

        final int size = 1;
        final Color color = null;

        final byte[] expResult = new byte[0];
        final byte[] result = instance.getData(size, color);

        verify(instance, times(1)).createData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }

    /**
     * Test of getData method, of class IconData, when data is null.
     */
    @Test
    public void testGetData_dataIsNull() {
        System.out.println("testGetData_dataIsNull");

        final IconData instance = spy(new IconDataImpl());

        final int size = ConstellationIcon.DEFAULT_ICON_SIZE;
        final Color color = null;

        final byte[] expResult = new byte[0];
        final byte[] result = instance.getData(size, color);

        verify(instance, times(1)).createData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }
    
    /**
     * Test of getData method, of class IconData, when data not null.
     */
    @Test
    public void testGetData_dataNotNull() {
        System.out.println("testGetData_dataNotNull");

        final IconData instance = spy(instanceWithData);

        final int size = ConstellationIcon.DEFAULT_ICON_SIZE;
        final Color color = null;

        final byte[] expResult = new byte[0];
        final byte[] result = instance.getData(size, color);

        verify(instance, times(0)).createData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
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
    public void testCreateData_sizeIs0AndColorIsNull() {
        System.out.println("testCreateData_sizeIs0AndColorIsNull");

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
 from createRasterInputStream() is null.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateData_inputStreamIsNull() throws IOException {
        System.out.println("testCreateData_inputStreamIsNull");

        final IconData instance = spy(new IconDataImpl());
        when(instance.createRasterInputStream()).thenReturn(null);

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
 from createRasterInputStream() throws IOException.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateData_inputStreamThrowsException() throws IOException {
        System.out.println("testCreateData_inputStreamThrowsException");

        final IconData instance = spy(new IconDataImpl());
        when(instance.createRasterInputStream()).thenThrow(IOExceptionMock);

        final int size = 0;
        final Color color = null;

        final byte[] expResult = new byte[0];
        final byte[] result = instance.createData(size, color);

        imageIOStaticMock.verifyNoInteractions();
        iconDataStaticMock.verifyNoInteractions();

        assertEquals(result, expResult);
    }
    
    /**
     * Test of getSVGData method, of class IconData, when size not default and
     * color not null.
     */
    @Test
    public void testGetSVGData() {
        System.out.println("testGetSVGData");

        final IconData instance = spy(new IconDataImpl());

        final int size = 1;
        final Color color = new Color(1, 1, 1);

        final SVGData expResult = null;
        final SVGData result = instance.getSVGData(size, color);

        verify(instance, times(1)).createSVGData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }
    
    /**
     * Test of getSVGData method, of class IconData, when size is default and color
     * not null.
     */
    @Test
    public void testGetSVGData_sizeIsDefault() {
        System.out.println("testGetSVGData_sizeIsDefault");

        final IconData instance = spy(new IconDataImpl());

        final int size = ConstellationIcon.DEFAULT_ICON_SIZE;
        final Color color = new Color(1, 1, 1);

        final SVGData expResult = null;
        final SVGData result = instance.getSVGData(size, color);

        verify(instance, times(1)).createSVGData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }
    
    /**
     * Test of getSVGData method, of class IconData, when size not default and
     * color is null.
     */
    @Test
    public void testGetSVGData_colorIsNull() {
        System.out.println("testGetSVGData_colorIsNull");

        final IconData instance = spy(new IconDataImpl());

        final int size = 1;
        final Color color = null;

        final SVGData expResult = null;
        final SVGData result = instance.getSVGData(size, color);

        verify(instance, times(1)).createSVGData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }
    
    /**
     * Test of getSVGData method, of class IconData, when data is null.
     */
    @Test
    public void testGetSVGData_dataIsNull() {
        System.out.println("testGetSVGData_dataIsNull");

        final IconData instance = spy(new IconDataImpl());

        final int size = ConstellationIcon.DEFAULT_ICON_SIZE;
        final Color color = null;

        final SVGData expResult = null;
        final SVGData result = instance.getSVGData(size, color);

        verify(instance, times(1)).createSVGData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }

    /**
     * Test of getSVGData method, of class IconData, when data not null.
     */
    @Test
    public void testGetSVGData_dataNotNull() {
        System.out.println("testGetSVGData_dataNotNull");

        final IconData instance = spy(instanceWithData);

        final int size = ConstellationIcon.DEFAULT_ICON_SIZE;
        final Color color = null;

        final SVGData expResult = null;
        final SVGData result = null;

        verify(instance, times(0)).createSVGData(Mockito.eq(size), Mockito.eq(color));

        assertEquals(result, expResult);
    }

    /**
     * Test of createData method, of class IconData, when the output returned
 from createRasterInputStream() is null.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateSVGData_inputStreamIsNull() throws IOException {
        System.out.println("testCreateSVGData_inputStreamIsNull");

        final IconData instance = spy(new IconDataImpl());
        when(instance.createVectorInputStream()).thenReturn(null);

        final int size = 0;
        final Color color = null;

        final SVGData expResult = null;
        final SVGData result = instance.createSVGData(size, color);

        imageIOStaticMock.verifyNoInteractions();
        iconDataStaticMock.verifyNoInteractions();

        assertEquals(result, expResult);
    }

    /**
     * Test of createData method, of class IconData, when the output returned
 from createRasterInputStream() throws IOException.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testCreateSVGData_inputStreamThrowsException() throws IOException {
        System.out.println("testCreateSVGData_inputStreamThrowsException");

        final IconData instance = spy(new IconDataImpl());
        when(instance.createVectorInputStream()).thenThrow(IOExceptionMock);

        final int size = 0;
        final Color color = null;

        final SVGData expResult = null;
        final SVGData result = instance.createSVGData(size, color);

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

        // Scale up.
        final BufferedImage image1 = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final int size1 = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image1), Mockito.eq(size1))).thenCallRealMethod();
        final BufferedImage scaledImage1 = IconData.scaleImage(image1, size1);

        final int expResult1 = size1 * size1;
        final int result1 = scaledImage1.getWidth() * scaledImage1.getHeight();

        assertEquals(result1, expResult1);

        // Scale down.
        final BufferedImage image2 = new BufferedImage(2, 2, BufferedImage.TYPE_4BYTE_ABGR);
        final int size2 = 1;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image2), Mockito.eq(size2))).thenCallRealMethod();
        final BufferedImage scaledImage2 = IconData.scaleImage(image2, size2);

        final int expResult2 = size2 * size2;
        final int result2 = scaledImage2.getWidth() * scaledImage2.getHeight();

        assertEquals(result2, expResult2);

        // When only image's current height equals size.
        final BufferedImage image3 = new BufferedImage(2, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final int size3 = 1;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image3), Mockito.eq(size3))).thenCallRealMethod();
        final BufferedImage scaledImage3 = IconData.scaleImage(image3, size3);

        final int expResult3 = 2;
        final int result3 = scaledImage3.getWidth();

        assertEquals(result3, expResult3);

        // When only image's current width equals size.
        final BufferedImage image4 = new BufferedImage(1, 2, BufferedImage.TYPE_4BYTE_ABGR);
        final int size4 = 1;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image4), Mockito.eq(size4))).thenCallRealMethod();
        final BufferedImage scaledImage4 = IconData.scaleImage(image4, size4);

        final int expResult4 = 2;
        final int result4 = scaledImage4.getHeight();

        assertEquals(result4, expResult4);

        // When image's current width and height don't equal size.
        final BufferedImage image5 = new BufferedImage(3, 1, BufferedImage.TYPE_4BYTE_ABGR);
        final int size5 = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image5), Mockito.eq(size5))).thenCallRealMethod();
        final BufferedImage scaledImage5 = IconData.scaleImage(image5, size5);

        final int expResult5 = size5 * size5;
        final int result5 = scaledImage5.getWidth() * scaledImage5.getHeight();

        assertEquals(result5, expResult5);

        // When image's current height and width don't equal size.
        final BufferedImage image6 = new BufferedImage(1, 3, BufferedImage.TYPE_4BYTE_ABGR);
        final int size6 = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image6), Mockito.eq(size6))).thenCallRealMethod();
        final BufferedImage scaledImage6 = IconData.scaleImage(image6, size6);

        final int expResult6 = size6 * size6;
        final int result6 = scaledImage6.getWidth() * scaledImage6.getHeight();

        assertEquals(result6, expResult6);
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
     * Test of scaleImage method, of class IconData, when image type not
     * TYPE_4BYTE_ABGR.
     */
    @Test
    public void testScaleImage_imageNotTYPE_4BYTE_ABGR() {
        System.out.println("testScaleImage_imageNotTYPE_4BYTE_ABGR");

        final BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        final int size = 2;

        iconDataStaticMock.when(() -> IconData.scaleImage(Mockito.eq(image), Mockito.eq(size))).thenCallRealMethod();
        final BufferedImage scaledImage = IconData.scaleImage(image, size);

        final int expResult = BufferedImage.TYPE_4BYTE_ABGR;
        final int result = scaledImage.getType();

        assertEquals(result, expResult);
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

        // When the class of obj is the same class as IconData and has the same value for data.
        obj = new IconDataImpl();
        assertTrue(instance.equals(obj));

        // When the class of obj is the same class as IconData but has a different value for data.
        obj = instanceWithData;
        assertFalse(instance.equals(obj));
    }

    /**
     * Test of toString method, of class IconData.
     */
    @Test
    public void testToString() {
        System.out.println("testToString");

        // When data is null.
        final IconData instance = spy(new IconDataImpl());

        final String expResult1 = String.format("Image of %d bytes", 0);
        final String result1 = instance.toString();

        assertEquals(result1, expResult1);

        // When data is anything but null.
        final Random random = new Random();
        final int bytes = random.nextInt(50) + 1;

        doReturn(new byte[bytes]).when(instance).createData(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        instance.getData();

        final String expResult2 = String.format("Image of %d bytes", bytes);
        final String result2 = instance.toString();

        assertEquals(result2, expResult2);
    }

    // Class implementation for tests because IconData is abstract.
    private class IconDataImpl extends IconData {

        @Override
        protected InputStream createRasterInputStream() throws IOException {
            return inputStreamMock;
        }

        @Override
        protected InputStream createVectorInputStream() throws IOException {
            return inputStreamMock;
        }
    }
}
