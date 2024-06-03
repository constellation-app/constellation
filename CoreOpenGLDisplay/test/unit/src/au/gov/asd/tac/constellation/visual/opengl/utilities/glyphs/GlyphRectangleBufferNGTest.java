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
package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import static au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManagerBI.DEFAULT_BUFFER_TYPE;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Nova
 */
public class GlyphRectangleBufferNGTest {

    private GlyphRectangleBuffer instance;
    private static final BufferedImage img1 = new BufferedImage(40, 5, DEFAULT_BUFFER_TYPE);
    private static final BufferedImage img2 = new BufferedImage(65, 5, DEFAULT_BUFFER_TYPE);
    private static final BufferedImage img3 = new BufferedImage(50, 95, DEFAULT_BUFFER_TYPE);

    public GlyphRectangleBufferNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new GlyphRectangleBuffer(100, 100, DEFAULT_BUFFER_TYPE);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {

    }

    /**
     * Test of readRectangleBuffer method, of class GlyphRectangleBuffer.
     */
    @Test
    public void testReadRectangleBuffer() {
        // First we put instance in a state where it has two different
        // rectangleBuffers so that we can test the correct data is being
        // returned.
        // This can be achieved using method exceedHeight
        exceedHeight();

        // Now we need to create two different BufferedImage, each identicle to
        // their respective pages in the GlyphRectangleBuffer.
        // Create a BufferedImage that should be identicle to the first page.
        // Set up base image.
        BufferedImage page0Buffer = setupBaseImage();
        Graphics2D g2d = page0Buffer.createGraphics();
        // Add img1.
        // x=2 and y=2 (first image so just starting value)
        g2d.drawImage(img1, 2, 2, null);
        // Add img2.
        // x=2 and y=9 (far left but below first image)
        g2d.drawImage(img1, 2, 9, null);

        // Create a BufferedImage identicle to the second page
        // Set up base image
        BufferedImage page1Buffer = setupBaseImage();
        g2d = page1Buffer.createGraphics();
        // Add img3.
        // x=2 and y=2 (starting values)
        g2d.drawImage(img3, 2, 9, null);

        // Now we do the testing. We will test both pages.
        // Lets start with page 0.
        ByteBuffer result = ByteBuffer.allocateDirect(20000);
        ByteBuffer expected = ByteBuffer.allocateDirect(20000);
        instance.readRectangleBuffer(0, result);
        DataBufferByte dbb = (DataBufferByte) page0Buffer.getData().getDataBuffer();
        expected.put(dbb.getData());
        assertEquals(result, expected);

        // Now lets to page 2. Appending it to the end of the original buffers
        instance.readRectangleBuffer(1, result);
        dbb = (DataBufferByte) page1Buffer.getData().getDataBuffer();
        expected.put(dbb.getData());
        assertEquals(result, expected);
    }

    /**
     * Test of reset method, of class GlyphRectangleBuffer.
     */
    @Test
    public void testReset() {
        // Create GlyphRectangleBuffer and add stuff to it
        GlyphRectangleBuffer modifiedInstance = new GlyphRectangleBuffer(100, 100, 2);
        BufferedImage img = new BufferedImage(5, 5, 2);
        modifiedInstance.addRectImage(img, 0);
        // Check that reset instance is equivilant to a new instance
        modifiedInstance.reset();
        assertEquals(modifiedInstance, new GlyphRectangleBuffer(100, 100, 2));
    }

    /**
     * Test that correct behavior occurs when we add a new image that/ does will
     * not exceed the width of the current line or the height of the buffer
     */
    @Test
    public void testAddRectNewImage() {

        int result = addFirstImage();

        assertEquals(result, 0);
        assertEquals(instance.size(), 1);
        assertEquals(instance.getRectangleCount(), 1);
        float[] expectedCoordinates = new float[256 * 4];
        // This is the postion of the min x, min y coordinates and the width, height of the
        // rectangle as a proportion of the width or height of the buffer.
        expectedCoordinates[0] = (float) 0.02; //min x
        expectedCoordinates[1] = (float) 0.02; // min y
        expectedCoordinates[2] = (float) 0.4; // width
        expectedCoordinates[3] = (float) 0.05; // height
        assertEquals(instance.getRectangleCoordinates(), expectedCoordinates);

        // Now check that when we add a second image it starts to the right of
        // the first image but at the same y coord. (If it fits).
        BufferedImage img = new BufferedImage(10, 10, 2); // Ensure its a different image
        result = instance.addRectImage(img, 0);
        assertEquals(result, 1); // increments by 1 each image
        assertEquals(instance.size(), 1); // no new rectangleBuffer so same value
        assertEquals(instance.getRectangleCount(), 2); // increments by 1 each image
        // New coords in next 4 positions
        expectedCoordinates[4] = (float) 0.44; // min x
        expectedCoordinates[5] = (float) 0.02; // min y
        expectedCoordinates[6] = (float) 0.1; // width
        expectedCoordinates[7] = (float) 0.1; // height
        assertEquals(instance.getRectangleCoordinates(), expectedCoordinates);

    }

    /**
     * Test that when we add the same image twice it doesnt add a new image to
     * the buffer and return the index of the original
     */
    @Test
    public void testAddRectSameImage() {

        // Instantiated and add the same image twice
        addFirstImage();
        int result = addFirstImage(); // Wont exceed height or width of buffer
        System.out.println(result);
        System.out.println(instance.size());
        System.out.println(instance.getRectangleCount());
        // Check results are the same as just adding an image once
        assertEquals(result, 0);
        assertEquals(instance.size(), 1);
        assertEquals(instance.getRectangleCount(), 1);
        float[] expectedCoordinates = new float[256 * 4];
        // This is the postion of the min x, min y coordinates and the width, height of the
        // rectangle as a proportion of the width or height of the buffer.
        expectedCoordinates[0] = (float) 0.02; //min x
        expectedCoordinates[1] = (float) 0.02; // min y
        expectedCoordinates[2] = (float) 0.4; // width
        expectedCoordinates[3] = (float) 0.05; // height
        assertEquals(instance.getRectangleCoordinates(), expectedCoordinates);
    }

    /**
     * Now we will check that a new line is created when required Triggers if (x
     * + w + PADDING) >= width that is if there is not enough space on the x
     * axis to fit the new image and buffer.
     *
     * It should add like carriage return on a typewriter, resetting the x,y
     * coordinates to the left of the buffer and below the current line Add new
     * image will will exceed the width as described above.
     */
    @Test
    public void testAddRectExceedWidth() {

        int result = exceedWidth();
        // Assert that all accessible values are correct
        assertEquals(result, 1);
        assertEquals(instance.size(), 1);
        assertEquals(instance.getRectangleCount(), 2);
        // It tesselates the rectangles on the buffer. In this test we reached the
        // end of the line and started on a new line. Hence we place the image
        // all the way too the left and below/above the lowest point so far.
        // Think of a carriage return on a typwriter.
        float[] expectedCoordinates = new float[256 * 4];
        //First 4 values the same as those in testAddRectNewImage
        expectedCoordinates[0] = (float) 0.02; //min x
        expectedCoordinates[1] = (float) 0.02; // min y
        expectedCoordinates[2] = (float) 0.4; // width
        expectedCoordinates[3] = (float) 0.05; // height
        // Same min x, min y below bottom of first image
        expectedCoordinates[4] = (float) 0.02;
        expectedCoordinates[5] = (float) 0.09;
        expectedCoordinates[6] = (float) 0.65;
        expectedCoordinates[7] = (float) 0.05;
        assertEquals(instance.getRectangleCoordinates(), expectedCoordinates);
    }

    /**
     * Now we will check that a new rectangleBuffer is created when required.
     * Triggers if (y + h + PADDING) >= height that is if there is not enough
     * space on the y axis to fit the new image and buffer.
     *
     * It should act like changing the page on a typewriter
     */
    @Test
    public void testAddRectExceedHeight() {

        int result = exceedHeight();
        // Assert that all accessible values are correct
        assertEquals(result, 2);
        assertEquals(instance.size(), 2); // Should be two buffers now
        assertEquals(instance.getRectangleCount(), 3);
        // It tesselates the rectangles on the buffer. In this test we reached the
        // bottom of the buffer. There is no room on this buffer for a new line
        // so we move to a new buffer.

        float[] expectedCoordinates = new float[256 * 4];
        //First 4 values the same as those in testAddRectNewImage
        expectedCoordinates[0] = (float) 0.02; //min x
        expectedCoordinates[1] = (float) 0.02; // min y
        expectedCoordinates[2] = (float) 0.4; // width
        expectedCoordinates[3] = (float) 0.05; // height
        //The next 4 are the values of the image used to trigger a new line
        //Same as those in testAddRectExceedWidth
        expectedCoordinates[4] = (float) 0.02;
        expectedCoordinates[5] = (float) 0.09;
        expectedCoordinates[6] = (float) 0.65;
        expectedCoordinates[7] = (float) 0.05;
        // The next 4 belong the image on the new buffer. The "page" is encoded
        // on the min x value as the integer. Otherwise min x and y should be
        // the same as for the first image
        expectedCoordinates[8] = (float) 1.02; //min x
        expectedCoordinates[9] = (float) 0.02; // min y
        expectedCoordinates[10] = (float) 0.5; // width
        expectedCoordinates[11] = (float) 0.95; // height
        assertEquals(instance.getRectangleCoordinates(), expectedCoordinates);
    }

    private int addFirstImage() {
        // Convenience method use to add a consistent first image to the buffer
        // each time.
        // Correct rectangle coordinates can be created using the code below:
        //
        // float[] expectedCoordinates = new float[256 * 4];
        // This is the postion of the min x, min y coordinates and the width, height of the
        // rectangle as a proportion of the width or height of the buffer.
        // expectedCoordinates[0] = (float) 0.02; //min x
        // expectedCoordinates[1] = (float) 0.02; // min y
        // expectedCoordinates[2] = (float) 0.4; // width
        // expectedCoordinates[3] = (float) 0.05; // height
        //
        // These values are tested in testAddRectNewImage
        return instance.addRectImage(img1, 0);
    }

    private int exceedWidth() {
        // Convenience method used to add a two images to the buffer such that
        // the second image has exceeded the width of the buffer
        // Correct rectangle coordinates can be creates using the code below:
        //
        // float[] expectedCoordinates = new float[256 * 4];
        // First 4 values the same as those in testAddRectNewImage
        // expectedCoordinates[0] = (float) 0.02; //min x
        // expectedCoordinates[1] = (float) 0.02; // min y
        // expectedCoordinates[2] = (float) 0.4; // width
        // expectedCoordinates[3] = (float) 0.05; // height
        // Same min x, min y below bottom of first image
        // expectedCoordinates[4] = (float) 0.02;
        // expectedCoordinates[5] = (float) 0.09;
        // expectedCoordinates[6] = (float) 0.65;
        // expectedCoordinates[7] = (float) 0.05;
        //
        // These values are tested in testAddRectExceedWidth

        addFirstImage();
        // Now create an image that will cause the current line to exceed the
        // width of the buffer if we add this to it
        // Img1 and Img2 gives 40+65 = 105 > 100 so should trigger a new line.
        // 5+5 = 10 < 100 so it wont trigger a new buffer.
        return instance.addRectImage(img2, 0);
    }

    private int exceedHeight() {
        // Convenience method used to add a three images to the buffer such that
        // the second image has exceeded the width of the buffer and the third
        // image exceed the height of the buffer
        // Correct rectangle coordinates can be creates using the code below:
        //
        // float[] expectedCoordinates = new float[256 * 4];
        // First 4 values the same as those in testAddRectNewImage
        // expectedCoordinates[0] = (float) 0.02; //min x
        // expectedCoordinates[1] = (float) 0.02; // min y
        // expectedCoordinates[2] = (float) 0.4; // width
        // expectedCoordinates[3] = (float) 0.05; // height
        // Same min x, min y below bottom of first image
        // expectedCoordinates[4] = (float) 0.02;
        // expectedCoordinates[5] = (float) 0.09;
        // expectedCoordinates[6] = (float) 0.65;
        // expectedCoordinates[7] = (float) 0.05;
        // The next 4 belong the image on the new buffer. The "page" is encoded
        // on the min x value as the integer. Otherwise min x and y should be
        // the same as for the first image
        // expectedCoordinates[8] = (float) 1.02; //min x
        // expectedCoordinates[9] = (float) 0.02; // min y
        // expectedCoordinates[10] = (float) 0.5; // width
        // expectedCoordinates[11] = (float) 0.95; // height
        // These values are tested in testAddRectExceedHeight

        exceedWidth();
        // Now we are on a new line add an image that will exceed the height
        // of the buffer and trigger a new buffer.
        // Add img3. Fits width wise but not height wise as 5+5+95 = 105 > 100.
        return instance.addRectImage(img3, 0);
    }

    private BufferedImage setupBaseImage() {
        BufferedImage bi = new BufferedImage(100, 100, DEFAULT_BUFFER_TYPE);
        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, 100, 100);
        g2d.setColor(Color.WHITE);
        return bi;
    }

}
