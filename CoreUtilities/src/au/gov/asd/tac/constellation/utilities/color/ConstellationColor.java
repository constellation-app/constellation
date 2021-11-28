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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A ConstellationColor manages data related to a color in CONSTELLATION,
 * including its red, gree, blue and alpha components, and optionally a name.
 *
 * @author sirius
 * @author cygnus_x-1
 */
public final class ConstellationColor implements Comparable<ConstellationColor>, Serializable {

    // colors
    public static final ConstellationColor AMETHYST = new ConstellationColor("Amethyst", 155, 89, 182, 255);
    public static final ConstellationColor AZURE = new ConstellationColor("Azure", 46, 105, 197, 255);
    public static final ConstellationColor BANANA = new ConstellationColor("Banana", 254, 255, 106, 255);
    public static final ConstellationColor BLACK = new ConstellationColor("Black", Color.BLACK);
    public static final ConstellationColor BLUE = new ConstellationColor("Blue", Color.BLUE);
    public static final ConstellationColor BLUEBERRY = new ConstellationColor("Blueberry", 153, 179, 255, 255);
    public static final ConstellationColor BROWN = new ConstellationColor("Brown", 0.5F, 0.25F, 0.25F, 1.0F);
    public static final ConstellationColor CARROT = new ConstellationColor("Carrot", 230, 126, 34, 255);
    public static final ConstellationColor CHERRY = new ConstellationColor("Cherry", 222, 36, 70, 255);
    public static final ConstellationColor CHOCOLATE = new ConstellationColor("Chocolate", 119, 95, 77, 255);
    public static final ConstellationColor CLOUDS = new ConstellationColor("Clouds", 236, 240, 241, 255);
    public static final ConstellationColor CYAN = new ConstellationColor("Cyan", Color.CYAN);
    public static final ConstellationColor DARK_GREEN = new ConstellationColor("DarkGreen", 0.0F, 0.5F, 0.0F, 1.0F);
    public static final ConstellationColor DARK_GREY = new ConstellationColor("DarkGrey", Color.DARK_GRAY);
    public static final ConstellationColor DARK_ORANGE = new ConstellationColor("DarkOrange", 1.0F, 0.5F, 0.25F, 1.0F);
    public static final ConstellationColor EMERALD = new ConstellationColor("Emerald", 46, 204, 79, 255);
    public static final ConstellationColor GOLDEN_ROD = new ConstellationColor("GoldenRod", 1.0F, 0.75F, 0.0F, 1.0F);
    public static final ConstellationColor GREEN = new ConstellationColor("Green", Color.GREEN);
    public static final ConstellationColor GREY = new ConstellationColor("Grey", Color.GRAY);
    public static final ConstellationColor LIGHT_BLUE = new ConstellationColor("LightBlue", 0.0F, 0.5F, 1.0F, 1.0F);
    public static final ConstellationColor LIGHT_GREEN = new ConstellationColor("LightGreen", 0.5F, 1.0F, 0.0F, 1.0F);
    public static final ConstellationColor MAGENTA = new ConstellationColor("Magenta", Color.MAGENTA);
    public static final ConstellationColor MANILLA = new ConstellationColor("Manilla", 255, 230, 153, 255);
    public static final ConstellationColor MELON = new ConstellationColor("Melon", 179, 230, 179, 255);
    public static final ConstellationColor MUSK = new ConstellationColor("Musk", 255, 116, 147, 255);
    public static final ConstellationColor NAVY = new ConstellationColor("Navy", 0.0F, 0.0F, 0.5F, 1.0F);
    public static final ConstellationColor NIGHT_SKY = new ConstellationColor("Night Sky", 27, 30, 36, 255);
    public static final ConstellationColor OLIVE = new ConstellationColor("Olive", 0.5F, 0.5F, 0.0F, 1.0F);
    public static final ConstellationColor ORANGE = new ConstellationColor("Orange", Color.ORANGE);
    public static final ConstellationColor PEACH = new ConstellationColor("Peach", 1.0F, 0.7F, 0.6F, 1.0F);
    public static final ConstellationColor PINK = new ConstellationColor("Pink", Color.PINK);
    public static final ConstellationColor PURPLE = new ConstellationColor("Purple", 0.63F, 0.28F, 0.63F, 1.0F);
    public static final ConstellationColor RED = new ConstellationColor("Red", Color.RED);
    public static final ConstellationColor TEAL = new ConstellationColor("Teal", 0.0F, 0.5F, 0.5F, 1.0F);
    public static final ConstellationColor TURQUOISE = new ConstellationColor("Turquoise", 0, 202, 213, 255);
    public static final ConstellationColor VIOLET = new ConstellationColor("Violet", 0.75F, 0.0F, 1.0F, 1.0F);
    public static final ConstellationColor WHITE = new ConstellationColor("White", Color.WHITE);
    public static final ConstellationColor YELLOW = new ConstellationColor("Yellow", Color.YELLOW);

    public static final List<ConstellationColor> NAMED_COLOR_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    AMETHYST,
                    AZURE,
                    BANANA,
                    BLACK,
                    BLUE,
                    BLUEBERRY,
                    BROWN,
                    CARROT,
                    CHERRY,
                    CHOCOLATE,
                    CLOUDS,
                    CYAN,
                    DARK_GREEN,
                    DARK_GREY,
                    DARK_ORANGE,
                    EMERALD,
                    GOLDEN_ROD,
                    GREEN,
                    GREY,
                    LIGHT_BLUE,
                    LIGHT_GREEN,
                    MAGENTA,
                    MANILLA,
                    MELON,
                    MUSK,
                    NAVY,
                    NIGHT_SKY,
                    OLIVE,
                    ORANGE,
                    PEACH,
                    PINK,
                    PURPLE,
                    RED,
                    TEAL,
                    TURQUOISE,
                    VIOLET,
                    WHITE,
                    YELLOW
            ));

    // alpha
    public static final float ZERO_ALPHA = 0F;

    private static final Map<String, ConstellationColor> NAMED_COLOR_MAP = new HashMap<>();

    static {
        for (final ConstellationColor colorValue : NAMED_COLOR_LIST) {
            NAMED_COLOR_MAP.put(colorValue.name.toUpperCase(), colorValue);
        }
    }

    /**
     * Return a ColorValue corresponding to the given name.
     * <p>
     * If the name does not exist, an attempt will be made to parse the label
     * string as either the 'RGBrrrgggbbb' or 'r,g,b,a' format.
     *
     * @param name a color name
     * @return A ColorValue instance if one can be derived, else null.
     */
    public static ConstellationColor getColorValue(final String name) {
        if (name == null) {
            return null;
        }

        String ucName = name.toUpperCase();
        if ("GRAY".equals(ucName)) {
            ucName = "GREY";
        }
        if (NAMED_COLOR_MAP.containsKey(ucName)) {
            return NAMED_COLOR_MAP.get(ucName);
        } else if (ucName.startsWith("RGB")) {
            return fromRgbColor(ucName);
        } else if (name.contains(",")) {
            return fromRgbWithCommaColor(ucName);
        } else if (ucName.startsWith("#")) {
            return fromHtmlColor(name);
        } else {
            // Do nothing
        }

        return null;
    }

    /**
     * Return a ColorValue corresponding to the given rgba values.
     *
     * @param red the red component of the color
     * @param green the green component of the color
     * @param blue the blue component of the color
     * @param alpha the alpha component of the color
     * @return a ColorValue instance
     */
    public static ConstellationColor getColorValue(final float red,
            final float green, final float blue, final float alpha) {
        for (final ConstellationColor colorValue : NAMED_COLOR_LIST) {
            if (colorValue.getRed() == red
                    && colorValue.getGreen() == green
                    && colorValue.getBlue() == blue
                    && colorValue.getAlpha() == alpha) {
                return colorValue;
            }
        }

        return new ConstellationColor(null, red, green, blue, alpha);
    }

    private final String name;
    private final float redColorValue;
    private final float greenColorValue;
    private final float blueColorValue;
    private final float alpha;

    /**
     * Create a ColorValue with red, green, blue, and alpha in the range [0, 1].
     *
     * @param name the name of the color.
     * @param red the red component of the color.
     * @param green the green component of the color.
     * @param blue the blue component of the color.
     * @param alpha the alpha component of the color.
     */
    private ConstellationColor(final String name, final float red, final float green, final float blue, final float alpha) {
        assert red >= 0F && red <= 1F
                && green >= 0F && green <= 1F
                && blue >= 0F && blue <= 1F
                && alpha >= 0F && alpha <= 1F;

        this.name = name;
        this.redColorValue = red;
        this.greenColorValue = green;
        this.blueColorValue = blue;
        this.alpha = alpha;
    }

    /**
     * Create a ColorValue with red, green, blue and alpha in the range [0-255].
     *
     * @param name the name of the color.
     * @param red the red component of the color.
     * @param green the green component of the color.
     * @param blue the blue component of the color.
     * @param alpha the alpha component of the color.
     */
    private ConstellationColor(final String name, final int red, final int green, final int blue, final int alpha) {
        this(name, red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    /**
     * Create a ColorValue from an existing Java Color.
     *
     * @param name The name of the color.
     * @param color An existing java color.
     */
    private ConstellationColor(final String name, final Color color) {
        this(name, color.getRGBComponents(null)[0],
                color.getRGBComponents(null)[1],
                color.getRGBComponents(null)[2],
                color.getRGBComponents(null)[3]);
    }

    public String getName() {
        return name;
    }

    public float getRed() {
        return redColorValue;
    }

    public float getGreen() {
        return greenColorValue;
    }

    public float getBlue() {
        return blueColorValue;
    }

    public float getAlpha() {
        return alpha;
    }

    public String getRGBString() {
        return String.format("%f,%f,%f,%f", redColorValue, greenColorValue, blueColorValue, alpha);
    }

    /**
     * Convert a ColorValue to a Java Color.
     *
     * @return A Java color corresponding to this ColorValue.
     */
    public Color getJavaColor() {
        return new Color(redColorValue, greenColorValue, blueColorValue, alpha);
    }

    /**
     * Convert a ColorValue to a JavaFX Color.
     *
     * @return A JavaFX color corresponding to this ColorValue.
     */
    public javafx.scene.paint.Color getJavaFXColor() {
        return new javafx.scene.paint.Color(redColorValue, greenColorValue, blueColorValue, alpha);
    }

    /**
     * Convert a ColorValue to an HTML hex color string (#RRGGBB). Note that the
     * alpha value is ignored.
     *
     * @return An HTML hex color string (#RRGGBB) corresponding to this
     * ColorValue.
     */
    public String getHtmlColor() {
        final int r = (int) (redColorValue * 255);
        final int g = (int) (greenColorValue * 255);
        final int b = (int) (blueColorValue * 255);

        return String.format("#%02x%02x%02x", r, g, b);
    }

    /**
     * Return a ColorValue corresponding to the given Java Color.
     *
     * @param color a Java color
     * @return a ColorValue instance
     */
    public static ConstellationColor fromJavaColor(final Color color) {
        return color == null ? null : ConstellationColor.getColorValue(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    /**
     * Convert a JavaFX Color to a ColorValue.
     *
     * @param color A JavaFX Color.
     * @return A new ColorValue.
     */
    public static ConstellationColor fromFXColor(final javafx.scene.paint.Color color) {
        return color == null ? null : ConstellationColor.getColorValue((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), 1F);
    }

    /**
     * Convert an HTML hex color string (#RRGGBB) to a ColorValue.
     *
     * @param color the HTML hex color string.
     * @return A new ColorValue.
     */
    public static ConstellationColor fromHtmlColor(final String color) {
        if (color == null || color.length() != 7 || color.charAt(0) != '#') {
            return null;
        }

        final int red = Integer.parseInt(color.substring(1, 3), 16);
        final int green = Integer.parseInt(color.substring(3, 5), 16);
        final int blue = Integer.parseInt(color.substring(5, 7), 16);

        return ConstellationColor.getColorValue(red / 255F, green / 255F, blue / 255F, 1F);
    }

    /**
     * Get a contrasting color based on YIQ values. Will return BLACK if the
     * passed in color is null.
     *
     * @param color HTML color to contrast with
     * @return the ConstellationColor which contrasts with color.
     */
    public static ConstellationColor getContrastHtmlColor(final String color) {
        return getContrastColor(color == null ? null : fromHtmlColor(color));
    }

    /**
     * Get a contrasting color based on YIQ values. Will return BLACK if the
     * passed in color is null.
     *
     * @param color RGB color to contrast with
     * @return the ConstellationColor which contrasts with color.
     */
    public static ConstellationColor getContrastRGBColor(final String color) {
        return getContrastColor(color == null ? null : fromRgbColor(color));
    }

    /**
     * Get a contrasting color based on YIQ values. Will return BLACK if the
     * passed in color is null.
     *
     * @param color ConstellationColor color to contrast with
     * @return the ConstellationColor which contrasts with color.
     */
    public static ConstellationColor getContrastColor(final ConstellationColor color) {
        return getContrastColor(color == null ? null : color.getJavaColor());
    }

    /**
     * Get a contrasting color based on YIQ values. Will return BLACK if the
     * passed in color is null.
     *
     * @param color RGB color String separated by commas to contrast with
     * @return the ConstellationColor which contrasts with color.
     */
    public static ConstellationColor getContrastfromRgbWithCommaColor(final String color) {
        return getContrastColor(color == null ? null : fromRgbWithCommaColor(color));
    }

    /**
     * Get a contrasting color based on YIQ values. Will return BLACK if the
     * passed in color is null.
     *
     * @param color JavaFX color to contrast with
     * @return the ConstellationColor which contrasts with color.
     */
    public static ConstellationColor getContrastColor(final Color color) {
        if (color == null) {
            return ConstellationColor.BLACK;
        }
        final double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000F;
        return y >= 128 ? ConstellationColor.BLACK : ConstellationColor.WHITE;
    }

    /**
     * Convert a RGB color string (RGBrrrgggbbb) to a ColorValue.
     *
     * @param color The RGB color string
     * @return A new ColorValue.
     */
    public static ConstellationColor fromRgbColor(final String color) {
        if (color == null) {
            return null;
        }
        
        final float red = Integer.parseInt(color.substring(3, 6), 10);
        final float green = Integer.parseInt(color.substring(6, 9), 10);
        final float blue = Integer.parseInt(color.substring(9, 12), 10);
        return new ConstellationColor(null, red / 255F, green / 255F, blue / 255F, 1F);
    }

    /**
     * Convert an RGBA color string "r,g,b,a" or "[r,g,b,a]" to a ColorValue.
     * <p>
     * Since colors end up as actual Python lists in pandas dataframes, they get
     * back to here as strings surrounded by brackets, hence we conveniently
     * look for the brackets and remove them.
     *
     * @param color The RGB color string
     * @return A new ColorValue.
     */
    public static ConstellationColor fromRgbWithCommaColor(final String color) {
        if (color == null) {
            return null;
        }
        
        // If the color string has surrounding "[]", remove them.
        final String fixedColor = color.startsWith("[") && color.endsWith("]") ? color.substring(1, color.length() - 1) : color;
        final String[] fields = split(fixedColor, 4, ',');
        final float red = Float.parseFloat(fields[0]);
        final float green = Float.parseFloat(fields[1]);
        final float blue = Float.parseFloat(fields[2]);
        final float alpha = fields[3] != null ? Float.parseFloat(fields[3]) : 1F;
        return new ConstellationColor(null, red, green, blue, alpha);
    }
    
    /**
     * A fast split method (slightly faster than String.split()).
     *
     * The maximum size of the resulting String[] must be known beforehand. The
     * actual size may be less.
     *
     * @param s The string to split.
     * @param size The expected size of the resulting array.
     * @param separator The separator character to split on.
     *
     * @return A String[] containing the fields that have been split out of the
     * string.
     */
    private static String[] split(final String s, final int size, final char separator) {
        final String[] a = new String[size];
        final int length = s.length();
        int pos = -1;
        int i = 0;
        while (pos < length) {
            int ixSep = s.indexOf(separator, pos + 1);
            if (ixSep == -1) {
                // If there are no more separators, there is still one more field to split.
                ixSep = s.length();
            }
            a[i++] = s.substring(pos + 1, ixSep);
            pos = ixSep;
        }

        return a;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ConstellationColor) {
            final ConstellationColor c = (ConstellationColor) other;
            return redColorValue == c.redColorValue && greenColorValue == c.greenColorValue && blueColorValue == c.blueColorValue && alpha == c.alpha;
        }

        if (other instanceof String) {
            return name != null && name.equals(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Float.floatToIntBits(this.redColorValue);
        hash = 41 * hash + Float.floatToIntBits(this.greenColorValue);
        hash = 41 * hash + Float.floatToIntBits(this.blueColorValue);
        hash = 41 * hash + Float.floatToIntBits(this.alpha);
        return hash;
    }

    @Override
    public String toString() {
        return name != null ? name : getHtmlColor();
    }

    @Override
    public int compareTo(final ConstellationColor o) {
        if (name != null && o.name != null) {
            return name.compareTo(o.name);
        } else if (name != null) {
            return 1;
        } else if (o.name != null) {
            return -1;
        } else {
            if (redColorValue != o.redColorValue) {
                return compareColourComponents(redColorValue, o.redColorValue);
            } else if (greenColorValue != o.greenColorValue) {
                return compareColourComponents(greenColorValue, o.greenColorValue);
            }
            return blueColorValue != o.blueColorValue ? compareColourComponents(blueColorValue, o.blueColorValue)
                    : compareColourComponents(alpha, o.alpha);
        }
    }
    
    /**
     * Compare two colour components and return the result. 
     * 1 if colour1Compoment has higher value.
     * -1 if colour2Compoment has higher value.
     * 0 if values are the same.
     * 
     * @param colour1Component the first colour component value to compare
     * @param colour2Component the second colour component value to compare
     * @return integer representing the result of the comparison
     */
    private int compareColourComponents(final float colour1Component, final float colour2Component) {
        return colour1Component == colour2Component ? 0 : (int) ((colour1Component - colour2Component) / Math.abs(colour1Component - colour2Component)); 
    }

    /**
     * Return an array of ColorValues ranging across a palette.
     *
     * @param colorCount The number of colors to return.
     * @param saturation Saturation.
     * @param brightness Brightness.
     *
     * @return An array of ColorValues.
     */
    public static ConstellationColor[] createPalette(final int colorCount, final float saturation, final float brightness) {
        final ConstellationColor[] palette = new ConstellationColor[colorCount];

        if (colorCount == 0) {
            return palette;
        }

        palette[0] = new ConstellationColor(null, Color.getHSBColor(0F, saturation, brightness));
        if (colorCount == 1) {
            return palette;
        }

        palette[1] = new ConstellationColor(null, Color.getHSBColor(0.5F, saturation, brightness));
        if (colorCount == 2) {
            return palette;
        }

        int colorsInRound = 2;
        float offset = -0.25F;
        float separation = 0.5F;

        int currentColor = 2;

        while (true) {
            float h = offset;
            for (int i = 0; i < colorsInRound; i++) {
                palette[currentColor++] = new ConstellationColor(null, Color.getHSBColor(h, saturation, brightness));
                if (currentColor == colorCount) {
                    return palette;
                }

                h += separation;
            }

            colorsInRound *= 2;
            offset /= 2;
            separation /= 2;
        }
    }

    public static ConstellationColor[] createPalette(final int colorCount) {
        return createPalette(colorCount, 1F, 1F);
    }

    /**
     * Return an array of colors representing a linear palette from startColor
     * to endColor.
     *
     * @param colorCount The number of colors in the palette.
     * @param startColor The color at the start of the palette.
     * @param endColor The color at the end of the palette.
     *
     * @return An ArrayList of colors.
     */
    public static ConstellationColor[] createLinearPalette(final int colorCount, final ConstellationColor startColor, final ConstellationColor endColor) {
        if (colorCount < 2) {
            throw new IllegalArgumentException("There must be at least two colors in the palette.");
        }

        final float dr = (endColor.redColorValue - startColor.redColorValue) / (colorCount - 1);
        final float dg = (endColor.greenColorValue - startColor.greenColorValue) / (colorCount - 1);
        final float db = (endColor.blueColorValue - startColor.blueColorValue) / (colorCount - 1);

        final ConstellationColor[] palette = new ConstellationColor[colorCount];
        palette[0] = startColor;
        for (int i = 1; i < colorCount - 1; i++) {
            palette[i] = new ConstellationColor(null, startColor.redColorValue + i * dr,
                    startColor.greenColorValue + i * dg, startColor.blueColorValue + i * db, 1F);
        }
        palette[colorCount - 1] = endColor;

        return palette;
    }

    /**
     * http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
     *
     * @param colorCount The number of colors required.
     * @param startValue The starting point of the color generator.
     * @param saturation The saturation of the color (0.5).
     * @param brightness The brightness of the color.
     *
     * @return A ColorValue array.
     */
    public static ConstellationColor[] createPalettePhi(final int colorCount,
            final float startValue, final float saturation, final float brightness) {
        final PalettePhiIterator ppi = new PalettePhiIterator(startValue, saturation, brightness);
        final ConstellationColor[] palette = new ConstellationColor[colorCount];
        for (int i = 0; i < colorCount; i++) {
            palette[i] = ppi.next();
        }

        return palette;
    }

    /**
     * Return an infinitely long sequence of colors.
     */
    public static class PalettePhiIterator implements Iterator<ConstellationColor> {

        private static final double INVPHI = 2 / (1 + Math.sqrt(5));
        private double h;
        private final float s;
        private final float b;

        public PalettePhiIterator(final float startValue, final float saturation, final float brightness) {
            this.h = startValue;
            this.s = saturation;
            this.b = brightness;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public ConstellationColor next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final ConstellationColor cv = new ConstellationColor(null, Color.getHSBColor((float) h, s, b));
            h = (h + INVPHI) % 1;

            return cv;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
