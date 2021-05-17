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
package au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * The GlyphManager manages a series of textures that hold all glyphs for the
 * application. It also performs text layout and provides x,y offsets for each
 * glyph in a specified string of text.
 *
 * @author algol
 */
public interface GlyphManager {

    /**
     * Performs an identical function to renderText() except that glyphs that
     * overlap are combined into a single glyph containing the group of
     * glyphs.This reduces the artifacts when that group of glyphs are rendered.
     *
     * @param text the text containing the characters to be rendered.
     * @param glyphStream the glyph stream to output the characters to.
     * @param context related information for the GlyphStream
     */
    public void renderTextAsLigatures(String text, GlyphManager.GlyphStream glyphStream, GlyphStreamContext context);

    /**
     * Returns the number of glyphs.
     *
     * @return the number of glyphs.
     */
    public int getGlyphCount();

    /**
     * Returns the number of glyph pages.
     *
     * @return the number of glyph pages.
     */
    public int getGlyphPageCount();

    /**
     * Returns The width of the texture buffer.
     *
     * @return The width of the texture buffer.
     */
    public int getTextureWidth();

    /**
     * Returns The height of the texture buffer.
     *
     * @return The height of the texture buffer.
     */
    public int getTextureHeight();

    /**
     * Returns the scaling factor that should be applied to glyph texture widths
     * to convert them to display widths.
     *
     * @return the scaling factor that should be applied to glyph texture widths
     * to convert them to display widths.
     */
    public float getWidthScalingFactor();

    /**
     * Returns the scaling factor that should be applied to glyph texture
     * heights to convert them to display heights.
     *
     * @return the scaling factor that should be applied to glyph texture
     * heights to convert them to display heights.
     */
    public float getHeightScalingFactor();

    /**
     * Returns an array holding the texture coordinates of each glyph. The
     * coordinates of each glyph (x, y, width, height) are stored in 4
     * continuous entries in the array starting at glyphPosition * 4. The
     * returned array can be of any size but only the first glyphCount * 4
     * entries are defined. As texture coordinates are always in the range 0..1,
     * the whole number part of the x value is used to store the page that holds
     * the glyph.
     *
     * @return an array holding the texture coordinates of each glyph.
     */
    public float[] getGlyphTextureCoordinates();

    /**
     * Reads the pixel data from a specified glyph texture page into a specified
     * byte buffer.
     *
     * @param page the glyph texture page to read.
     * @param buffer the buffer to store the pixel data in.
     */
    public void readGlyphTexturePage(int page, ByteBuffer buffer);

    /**
     * Create a glyph to be drawn as a background for the other glyphs.
     *
     * @param alpha The intensity of the background in the range 0.0 to 1.0.
     *
     * @return The position of the background glyph
     */
    public int createBackgroundGlyph(float alpha);

    /**
     * Writes a specified glyph texture page as a PNG image to the specified
     * OutputStream.
     *
     * @param page the glyph texture page to be written.
     * @param out the OutputStream to write the PNG image to.
     *
     * @throws IOException if an error occurs while writing the image.
     */
    public void writeGlyphBuffer(int page, OutputStream out) throws IOException;

    /**
     * A GlyphStream is provided to the GlyphManager when a string of text is
     * rendered and receives a call-back for each glyph that needs to rendered
     * in order to display that text.
     *
     * The call-back includes all the information needed to render the glyph
     * when used in combination with data structures provided by the
     * GlyphManager.
     *
     * The glyphPosition refers to the position of the glyph in the
     * glyphTextureCoordinates buffer. This can be used to get the x, y, width
     * and height of the glyph in texture coordinates.
     *
     * The x and y values provide the offset to the top-left corner of the glyph
     * in render coordinates. Render coordinates have their origin at the top of
     * the text, half way along the width of the rendered text. Render
     * coordinates are proportionally scaled so that the line height of rendered
     * font is 1.
     */
    public static interface GlyphStream {

        public void newLine(float width, GlyphStreamContext context);

        /**
         * Called to indicate that a glyph should be drawn at the specified x,y
         * offset.
         *
         * @param glyphPosition the position of the glyph in the glyph registry.
         * @param x the x location of the glyph.
         * @param y the y location of the glyph.
         * @param context related information for the GlyphStream
         */
        public void addGlyph(int glyphPosition, float x, float y, GlyphStreamContext context);
    }
}
