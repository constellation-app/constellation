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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteReader;
import au.gov.asd.tac.constellation.graph.attribute.io.GraphByteWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author algol
 */
public final class Plane {

    private static final Logger LOGGER = Logger.getLogger(Plane.class.getName());

    private final BufferedImage image;
    private final String label;
    private final int imageWidth;
    private final int imageHeight;
    private float x;
    private float y;
    private float z;
    private float width;
    private float height;
    private boolean isVisible;

    /**
     * Define an image to be drawn on a plane.
     * <p>
     * The width and height values are the size of the image in the scene. The
     * imageWidth and imageHeight values are the physical width and height of
     * the image. These may be different: a 100x100 physical image may be drawn
     * as a 200x200 plane, for example. The x,y,z values define the position of
     * the image within the scene.
     *
     * @param label The name of this plane.
     * @param x The x position of the origin of the image when displayed.
     * @param y The y position of the origin of the image when displayed.
     * @param z The z position of the origin of the image when displayed.
     * @param width The drawing width of the image when displayed.
     * @param height The drawing height of the image when displayed.
     * @param image The bytes that define the image (in PNG format).
     * @param imageWidth The width of the image in pixels.
     * @param imageHeight The height of the image in pixels.
     */
    public Plane(final String label, final float x, final float y, final float z, final float width, final float height, final BufferedImage image, final int imageWidth, final int imageHeight) {
        this.image = image;
        this.label = label;
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        isVisible = true;
    }

    /**
     * Returns the image.
     *
     * @return the image.
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returns the plane label.
     *
     * @return the plane label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the x position of the origin of the image when displayed.
     *
     * @return the x position of the origin of the image when displayed.
     */
    public float getX() {
        return x;
    }

    public void setX(final float x) {
        this.x = x;
    }

    /**
     * Returns the y position of the origin of the image when displayed.
     *
     * @return the y position of the origin of the image when displayed.
     */
    public float getY() {
        return y;
    }

    public void setY(final float y) {
        this.y = y;
    }

    /**
     * Returns the z position of the origin of the image when displayed.
     *
     * @return the z position of the origin of the image when displayed.
     */
    public float getZ() {
        return z;
    }

    public void setZ(final float z) {
        this.z = z;
    }

    /**
     * Returns the drawing width of the image when displayed.
     *
     * @return the drawing width of the image when displayed.
     */
    public float getWidth() {
        return width;
    }

    public void setWidth(final float width) {
        this.width = width;
    }

    /**
     * Returns the drawing height of the image when displayed.
     *
     * @return the drawing height of the image when displayed.
     */
    public float getHeight() {
        return height;
    }

    public void setHeight(final float height) {
        this.height = height;
    }

    /**
     * Returns the width of the image in pixels.
     *
     * @return the width of the image in pixels.
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * Returns the height of the image in pixels.
     *
     * @return the height of the image in pixels.
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Returns true if the image is displayed, False if not.
     *
     * @return True if the image is displayed, False if not.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Sets the visibility of a plane.
     *
     * @param isVisible the new visibility of the plane.
     */
    public void setVisible(final boolean isVisible) {
        this.isVisible = isVisible;
    }

    public static Plane readNode(final JsonNode jnode, final GraphByteReader byteReader) throws IOException {
        final String label = jnode.get("label").textValue();
        final float x = (float) jnode.get("x").doubleValue();
        final float y = (float) jnode.get("y").doubleValue();
        final float z = (float) jnode.get("z").doubleValue();
        final float width = (float) jnode.get("width").doubleValue();
        final float height = (float) jnode.get("height").doubleValue();
        final int imageWidth = jnode.get("image_width").intValue();
        final int imageHeight = jnode.get("image_height").intValue();

        final String reference = jnode.get("plane_ref").textValue();
        final byte[] bytes = byteReader.read(reference).getData();
        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

        return new Plane(label, x, y, z, width, height, image, imageWidth, imageHeight);
    }

    public void writeNode(final JsonGenerator jg, final GraphByteWriter byteWriter) throws IOException {
        jg.writeStringField("label", label);
        jg.writeNumberField("x", x);
        jg.writeNumberField("y", y);
        jg.writeNumberField("z", z);
        jg.writeNumberField("width", width);
        jg.writeNumberField("height", height);
        jg.writeNumberField("image_width", imageWidth);
        jg.writeNumberField("image_height", imageHeight);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        final byte[] bytes = os.toByteArray();
        final String reference = byteWriter.write(new ByteArrayInputStream(bytes));
        jg.writeStringField("plane_ref", reference);

        LOGGER.log(Level.INFO, "{0}", String.format("Write plane '%s', byteLabel '%s', size %d", label, reference, bytes.length));
    }

    @Override
    public String toString() {
        return String.format("Plane[%s@(%f,%f,%f) %dx%d]", label, x, y, z, imageWidth, imageHeight);
    }
}
