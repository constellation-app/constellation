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
import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * An IconData stores byte data for use as a {@link ConstellationIcon}, provided
 * via an InputStream.
 * 
 * @author capricornunicorn123
 * @author cygnus_x-1
 */
public abstract class IconData {

    private static final Logger LOGGER = Logger.getLogger(IconData.class.getName());

    private byte[] data = null;
    private SVGData svgData = null;
    
    public SVGData getSVGData() {
        return getSVGData(ConstellationIcon.DEFAULT_ICON_SIZE, null);
    }
    
    public SVGData getSVGData(final int size, final Color color) {
        if (size != ConstellationIcon.DEFAULT_ICON_SIZE || color != null) {
            return createSVGData(size, color);
        }

        if (svgData == null) {
            svgData = createSVGData(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        }
        return svgData;
    }
    
    protected SVGData createSVGData(final int size, final Color color) {
        try {
            final InputStream is = createVectorInputStream();
            final SVGObject svg = SVGObject.loadFromInputStream(is);
            if (svg != null){
                svg.setDimension(size, size);
                if (color != null) {
                    svg.saturateSVG(ConstellationColor.fromJavaColor(color));
                }
                return svg.toSVGData();
            }
        } catch (final IOException | UnsupportedOperationException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            return null;
        }
        return null;
    }

    /**
     * Get an array of bytes representing the data of a
     * {@link ConstellationIcon} of default size, specified by
     * {@link ConstellationIcon#DEFAULT_ICON_SIZE}, and original color. This
     * data will only be created the first time one of the getData methods is
     * called and will be cached at this point, allowing for lazy loading of
     * icons into memory.
     *
     * @return An array of bytes representing the data of a
     * {@link ConstellationIcon}.
     */
    public byte[] getData() {
        return getData(ConstellationIcon.DEFAULT_ICON_SIZE, null);
    }

    /**
     * Get an array of bytes representing the data of aOL
     * {@link ConstellationIcon} of the specified size and color. This data will
     * only be created the first time one of the getData methods is called and
     * will be cached at this point, allowing for lazy loading of icons into
     * memory.
     *
     * @param size An integer value representing both the height and width of
     * the icon.
     * @param color A {@link Color} representing the color of the icon.
     * @return An array of bytes representing the data of a
     * {@link ConstellationIcon}.
     */
    public byte[] getData(final int size, final Color color) {
        if (size != ConstellationIcon.DEFAULT_ICON_SIZE || color != null) {
            return createData(size, color);
        }

        if (data == null) {
            data = createData(ConstellationIcon.DEFAULT_ICON_SIZE, null);
        }

        return data;
    }

    /**
     * Build an array of bytes representing the data of a
     * {@link ConstellationIcon} from the {@link InputStream} specified by
     * {@link #createRasterInputStream()}.
     *
     * @param size An integer value representing both the height and width of
     * the icon.
     * @param color A {@link Color} representing the color of the icon.
     * @return An array of bytes representing the data of a
     * {@link ConstellationIcon}.
     */
    protected byte[] createData(final int size, final Color color) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            final InputStream is = createRasterInputStream();
            if (is != null) {
                BufferedImage image = ImageIO.read(is);
                if (color != null) {
                    image = colorImage(image, color);
                }
                if (size > 0) {
                    image = scaleImage(image, size);
                }
                if (image != null) {
                    ImageIO.write(image, ConstellationIcon.DEFAULT_ICON_FORMAT, os);
                }

                is.close();
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }

        return os.toByteArray();
    }

    protected static BufferedImage colorImage(final BufferedImage image, final Color color) {
        if (image == null || color == null) {
            return image;
        } else {
            final BufferedImage coloredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    final Color pixel = new Color(image.getRGB(x, y), true);
                    final int blendRed = ((pixel.getRed() * (255 - color.getAlpha())) + (color.getRed() * color.getAlpha())) / 255;
                    final int blendGreen = ((pixel.getGreen() * (255 - color.getAlpha())) + (color.getGreen() * color.getAlpha())) / 255;
                    final int blendBlue = ((pixel.getBlue() * (255 - color.getAlpha())) + (color.getBlue() * color.getAlpha())) / 255;
                    final Color blend = new Color(blendRed, blendGreen, blendBlue, pixel.getAlpha());
                    coloredImage.setRGB(x, y, blend.getRGB());
                }
            }
            return coloredImage;
        }
    }

    /**
     * Scale the height and width of the given {@link BufferedImage} to the size
     * provided.
     *
     * @param image The {@link BufferedImage} to scale.
     * @param size An integer value representing the height and width to which
     * to scale the image.
     * @return A {@link BufferedImage} which is identical to the provided image,
     * only resized.
     */
    protected static BufferedImage scaleImage(final BufferedImage image, final int size) {
        if (image == null || image.getType() == BufferedImage.TYPE_4BYTE_ABGR
                && (image.getWidth() == size || image.getHeight() == size)) {
            return image;
        } else {
            final float scale = size / (float) Math.max(image.getWidth(), image.getHeight());

            final BufferedImage scaledImage = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
            final Graphics2D scaledGraphics = scaledImage.createGraphics();
            scaledGraphics.setColor(new Color(0, 0, 0, 0));
            scaledGraphics.fillRect(0, 0, size - 1, size - 1);

            final int width = (int) (image.getWidth() * scale);
            final int height = (int) (image.getHeight() * scale);
            final int x = (size - width) / 2;
            final int y = (size - height) / 2;

            scaledGraphics.drawImage(image, x, y, width, height, null);

            if (image.getWidth() > size || image.getHeight() > size) {
                LOGGER.log(Level.FINE, "{0}", String.format("Scaled icon from %dx%d type %d to %dx%d type %d",
                        image.getWidth(), image.getHeight(), image.getType(), width, height, BufferedImage.TYPE_4BYTE_ABGR));
            }

            return scaledImage;
        }
    }

    @Override
    public int hashCode() {
        return 79 * 7 + Arrays.hashCode(this.data);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        
        return Arrays.equals(this.data, ((IconData) obj).data);
    }

    @Override
    public String toString() {
        return String.format("Image of %d bytes", data == null ? 0 : data.length);
    }

    /**
     * Build an {@link InputStream} which will provide the data representing a
     * {@link ConstellationIcon}. Note that this method could be called multiple
     * times, so a new {@link InputStream} should be returned each time.
     *
     * @return An {@link InputStream} through which the icon data will be
     * provided.
     * @throws IOException If the {@link InputStream} encounters an issue while
     * transmitting the icon data.
     */
    protected abstract InputStream createRasterInputStream() throws IOException;
    
    protected abstract InputStream createVectorInputStream() throws IOException;
}
