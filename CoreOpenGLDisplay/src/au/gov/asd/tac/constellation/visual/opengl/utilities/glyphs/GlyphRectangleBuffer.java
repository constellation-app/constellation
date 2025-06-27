/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class encapsulates a BufferedImage that holds rectangles containing
 * images of glyphs.
 * <p>
 * Each new rectangle is drawn at the current x,y position if there's enough
 * room, otherwise a new rectangle line is started.
 *
 * @author algol
 */
final class GlyphRectangleBuffer {

    // The buffers that glyphs will be drawn to.
    // Each one of these will eventually be copied to a texture buffer.
    //
    private final List<BufferedImage> rectBuffers;

    private final int bufferType;

    // The current rectangle buffer and its graphics (a reference to
    // the end item in rectBuffers).
    //
    private BufferedImage rectBuffer;
    private Graphics2D g2d;

    /**
     * If we draw the rectangles immediately next to each other, pixels from one
     * image can bleed through to a neighbouring image, resulting in visible
     * artifacts. Therefore we add some space between rectangles.
     */
    private static final int PADDING = 2;

    // The next position to draw a rectangle at.
    //
    private int x;
    private int y;

    // The maximum bounding box height in the current rectangle line.
    // We need this so we know how much to add to y to go to the next line of rectangles.
    //
    private int maxHeight;

    // The coordinates (and other infornation) of each rectangle
    // as required by OpenGL.
    //
    private static final int FLOATS_PER_RECT = 4;
    private float[] rectTextureCoordinates;

    /**
     * Remember where we wrote each rectangle.
     * <p>
     * This stops us writing the same image twice.
     * <p>
     * The key is the hash code of the image in the rectangle.
     */
    private final Map<Integer, Integer> memory;

    /**
     * How many rectangles have been created?
     */
    private int rectangleCount;

    // The width and height of each (texture buffer) BufferedImage.
    //
    public final int width;
    public final int height;

    /**
     *
     * @param width
     * @param height
     * @param bufferType Use BufferedImage.TYPE_BYTE_GRAY for CONSTELLATION (we
     * only need grayscale). Use BufferedImage.TYPE_INT_ARGB for standalone to
     * see the pretty colors.
     */
    GlyphRectangleBuffer(final int width, final int height, final int bufferType) {
        this.width = width;
        this.height = height;
        this.bufferType = bufferType;
        rectBuffers = new ArrayList<>();
        memory = new HashMap<>();
        reset();
    }

    public int size() {
        return rectBuffers.size();
    }

    /**
     * Return the i'th buffer.
     *
     * @return
     */
    BufferedImage get(final int i) {
        return rectBuffers.get(i);
    }

    public void readRectangleBuffer(final int page, final ByteBuffer buffer) {
        final BufferedImage rb = rectBuffers.get(page);
        final DataBufferByte dbb = (DataBufferByte) rb.getData().getDataBuffer();
        buffer.put(dbb.getData());
    }

    public int getRectangleCount() {
        return rectangleCount;
    }

    public float[] getRectangleCoordinates() {
        return rectTextureCoordinates.clone();
    }

    public void reset() {

        // Start with room for an arbitrary number of rectangles
        // so we don't have to grow the array too quickly.
        //
        rectTextureCoordinates = new float[256 * FLOATS_PER_RECT];

        rectBuffers.clear();
        memory.clear();
        if (g2d != null) {
            g2d.dispose();
            g2d = null;
        }

        rectangleCount = 0;

        newRectBuffer();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GlyphRectangleBuffer other = (GlyphRectangleBuffer) obj;
        if (this.bufferType != other.bufferType) {
            return false;
        }
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.maxHeight != other.maxHeight) {
            return false;
        }
        if (this.rectangleCount != other.rectangleCount) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        if (!bufferedImagesEqual(this.rectBuffer, other.rectBuffer)) {
            return false;
        }
        if (!this.memory.equals(other.memory)) {
            return false;
        }
        if (this.rectBuffers.size() != other.rectBuffers.size()) {
            return false;
        }
        if (!Arrays.equals(this.rectTextureCoordinates, other.rectTextureCoordinates)) {
            return false;
        }
        for (int i = 0; this.rectBuffers.size() > i; i++) {
            if (!bufferedImagesEqual(this.rectBuffers.get(i), other.rectBuffers.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.rectBuffers);
        hash = 47 * hash + this.bufferType;
        hash = 47 * hash + Objects.hashCode(this.rectBuffer);
        hash = 47 * hash + this.x;
        hash = 47 * hash + this.y;
        hash = 47 * hash + this.maxHeight;
        hash = 47 * hash + Arrays.hashCode(this.rectTextureCoordinates);
        hash = 47 * hash + Objects.hashCode(this.memory);
        hash = 47 * hash + this.rectangleCount;
        hash = 47 * hash + this.width;
        hash = 47 * hash + this.height;
        return hash;
    }

    /**
     * Add a rectangle containing an image to the buffers.
     * <p>
     * If the image has already been added (using the hash code of the int[] of
     * the image's pixels as the key), the index of the existing rectangle is
     * returned. Otherwise, the image is added and the index of the new
     * rectangle is returned.
     *
     * @param img A BufferedImage containing an image.
     * @param extra The number of extra pixels drawn around the edges of this
     * image to avoid interpolation problems later. Store the actual image but
     * only record the size-extra.
     * @return rectIndex The index of the rectangle in this.memory.
     */
    int addRectImage(final BufferedImage img, final int extra) {
        final int w = img.getWidth();
        final int h = img.getHeight();

        // Get the hash code of the image.
        // BufferedImage doesn't have a hashCode() method, so we use the underlying pixels.
        //
        final int hashCode = Arrays.hashCode(img.getRGB(0, 0, w, h, null, 0, w));

        Integer rectIndex = memory.get(hashCode);
        if (rectIndex == null) {
            rectIndex = addHashcode(hashCode, img, extra, w, h);
        }

        return rectIndex;
    }

    private synchronized int addImageToBuffer(final BufferedImage img, final int rectIndex, final int extra, final int w, final int h) {
        if ((x + w + PADDING) >= width) {
            newRectLine();
        }
        if ((y + h + PADDING) >= height) {
            newRectBuffer();
        }

        // Copy the image to the current buffer using the identity (unchanged) op.
        // (The obvious drawImage() variation is technically asynchronous, so don't use it.)
        //
        g2d.drawImage(img, x, y, null);

        putImageInRectTextureCoordinates(rectIndex, extra, w, h);

        x += w + PADDING;
        maxHeight = Math.max(h, maxHeight);

        rectangleCount++;

        return rectIndex;
    }

    private int putImageInRectTextureCoordinates(int rectIndex, final int extra, final int w, final int h) {

        final int ptr = rectIndex * FLOATS_PER_RECT;

        // Ensure that the coordinates array has enough room to add more coordinates.
        //
        if (ptr == rectTextureCoordinates.length) {
            rectTextureCoordinates = Arrays.copyOf(rectTextureCoordinates, rectTextureCoordinates.length * 2);
        }

        // Texture coordinates are in units of texture buffer size,
        // each coordinate ranges from 0 to 1. The x coordinate also encodes
        // the texture page.
        //
        int pageNumber = size() - 1;
        rectTextureCoordinates[ptr + 0] = pageNumber + ((x + extra) / (float) width);
        rectTextureCoordinates[ptr + 1] = (y + extra) / (float) height;
        rectTextureCoordinates[ptr + 2] = (w - extra * 2) / (float) width;
        rectTextureCoordinates[ptr + 3] = (h - extra * 2) / (float) height;

        return rectIndex;
    }

    private void newRectBuffer() {
        rectBuffer = new BufferedImage(width, height, bufferType);

        if (g2d != null) {
            g2d.dispose();
        }
        g2d = rectBuffer.createGraphics();

        // We don't want to antialias images that are already antialiased.
        //
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);

        rectBuffers.add(rectBuffer);

        x = PADDING;
        y = PADDING;
        maxHeight = 0;
    }

    private void newRectLine() {
        x = PADDING;
        y += maxHeight + PADDING;
        maxHeight = 0;
    }

    private boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        // Code copied from https://stackoverflow.com/questions/15305037/java-compare-one-bufferedimage-to-another
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int xCoord = 0; xCoord < img1.getWidth(); xCoord++) {
                for (int yCoord = 0; yCoord < img1.getHeight(); yCoord++) {
                    if (img1.getRGB(xCoord, yCoord) != img2.getRGB(xCoord, yCoord)) {
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private int addHashcode(final int hashCode, final BufferedImage img, final int extra, final int w, final int h) {
        int value = memory.size();
        Integer rectIndex = memory.putIfAbsent(hashCode, value);
        if (rectIndex == null) {
            rectIndex = value;
            addImageToBuffer(img, rectIndex, extra, w, h);
        }
        return rectIndex;
    }
}
