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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The GlyphManagerOpenGLController manages the OpenGL buffers necessary to
 * render text in Constellation.
 *
 * @author sirius
 */
public class GlyphManagerOpenGLController {

    private static final int EXTERNAL_FORMAT = GL2ES2.GL_RED;
    private static final int INTERNAL_FORMAT = GL.GL_R8;

    private static final int FLOATS_PER_GLYPH = 4;
    private static final int BYTES_PER_FLOAT = Float.BYTES;

    private final GlyphManager glyphManager;

    private final int[] coordinatesBufferName = new int[1];
    private final int[] coordinatesTextureName = new int[1];
    private int coordinatesBufferedGlyphs = 0;
    private int coordinatesBufferSize;

    private final int[] glyphsTextureName = new int[1];
    private int glyphsGlyphsBuffered = 0;
    private int glyphsPageCapacity;

    // The number of pages that have been loaded onto the graphics card
    private int glyphsPagesBuffered = 0;

    private final List<ByteBuffer> glyphsPageBuffers = new ArrayList<>();

    public GlyphManagerOpenGLController(GlyphManager glyphManager) {
        this.glyphManager = glyphManager;
    }

    public GlyphManager getGlyphManager() {
        return glyphManager;
    }

    public void init(GL3 gl) {
        initCoordinates(gl);
        initGlyphs(gl);
    }

    public void update(GL3 gl) {
        updateCoordinates(gl);
        updateGlyphs(gl);
    }

    public void bind(GL3 gl, int coordinatesUniformLocation, int coordinatesTextureUnit, int glyphsUniformLocation, int glyphsTexureUnit) {
        bindCoordinates(gl, coordinatesUniformLocation, coordinatesTextureUnit);
        bindGlyphs(gl, glyphsUniformLocation, glyphsTexureUnit);
    }

    private void initCoordinates(GL3 gl) {

        gl.glGenBuffers(1, coordinatesBufferName, 0);

        coordinatesBufferSize = glyphManager.getGlyphTextureCoordinates().length * BYTES_PER_FLOAT;
        gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, coordinatesBufferName[0]);
        gl.glBufferData(GL2ES3.GL_TEXTURE_BUFFER, coordinatesBufferSize, null, GL.GL_DYNAMIC_DRAW);

        gl.glGenTextures(1, coordinatesTextureName, 0);

        gl.glBindTexture(GL2ES3.GL_TEXTURE_BUFFER, coordinatesTextureName[0]);
        gl.glTexBuffer(GL2ES3.GL_TEXTURE_BUFFER, GL.GL_RGBA32F, coordinatesBufferName[0]);
    }

    private void updateCoordinates(GL3 gl) {

        final int newTextureCoordinatesBufferSize = glyphManager.getGlyphTextureCoordinates().length * BYTES_PER_FLOAT;
        if (newTextureCoordinatesBufferSize > coordinatesBufferSize) {
            coordinatesBufferSize = newTextureCoordinatesBufferSize;
            gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, coordinatesBufferName[0]);
            gl.glBufferData(GL2ES3.GL_TEXTURE_BUFFER, coordinatesBufferSize, null, GL.GL_DYNAMIC_DRAW);
            coordinatesBufferedGlyphs = 0;
        }

        if (coordinatesBufferedGlyphs < glyphManager.getGlyphCount()) {
            final int offset = coordinatesBufferedGlyphs * FLOATS_PER_GLYPH;
            final int size = glyphManager.getGlyphCount() * FLOATS_PER_GLYPH - offset;
            gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, coordinatesBufferName[0]);
            final FloatBuffer glyphsCoordinates = FloatBuffer.wrap(glyphManager.getGlyphTextureCoordinates(), offset, size);
            gl.glBufferSubData(GL2ES3.GL_TEXTURE_BUFFER, (long) offset * BYTES_PER_FLOAT, (long) size * BYTES_PER_FLOAT, glyphsCoordinates);
            coordinatesBufferedGlyphs = glyphManager.getGlyphCount();
        }
    }

    private void bindCoordinates(GL3 gl, int uniformLocation, int textureUnit) {
        gl.glActiveTexture(GL.GL_TEXTURE0 + textureUnit);
        gl.glBindTexture(GL2ES3.GL_TEXTURE_BUFFER, coordinatesTextureName[0]);
        gl.glUniform1i(uniformLocation, textureUnit);
    }

    private void initGlyphs(GL3 gl) {

        gl.glGenTextures(1, glyphsTextureName, 0);

        gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, glyphsTextureName[0]);

        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL2ES3.GL_TEXTURE_2D_ARRAY, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

        final int width = glyphManager.getTextureWidth();
        final int height = glyphManager.getTextureHeight();
        final int pageCount = glyphManager.getGlyphPageCount();
        gl.glTexImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, INTERNAL_FORMAT, width, height, pageCount, 0, EXTERNAL_FORMAT, GL.GL_UNSIGNED_BYTE, null);
        glyphsPageCapacity = pageCount;
    }

    private void updateGlyphs(GL3 gl) {

        final int width = glyphManager.getTextureWidth();
        final int height = glyphManager.getTextureHeight();
        final int pageCount = glyphManager.getGlyphPageCount();
        final int glyphCount = glyphManager.getGlyphCount();

        // If there have been new glyphs then some of then might be on the last page
        // buffered to the graphics card. We need to mark this last page as unbuffered
        // so that it gets buffered again.
        if (glyphCount > glyphsGlyphsBuffered && glyphsPagesBuffered > 0) {
            glyphsPagesBuffered--;
            glyphsPageBuffers.remove(glyphsPageBuffers.size() - 1);
        }

        glyphsGlyphsBuffered = glyphCount;

        if (pageCount > glyphsPageCapacity) {
            gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, glyphsTextureName[0]);
            gl.glTexImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, INTERNAL_FORMAT, width, height, pageCount, 0, EXTERNAL_FORMAT, GL.GL_UNSIGNED_BYTE, null);
            glyphsPageCapacity = pageCount;
            glyphsPagesBuffered = 0;
        }

        while (glyphsPagesBuffered < pageCount) {

            final ByteBuffer pixelBuffer;
            if (glyphsPageBuffers.size() > glyphsPagesBuffered) {
                pixelBuffer = glyphsPageBuffers.get(glyphsPagesBuffered);
                pixelBuffer.rewind();
            } else {
                pixelBuffer = ByteBuffer.allocateDirect(width * height);
                glyphManager.readGlyphTexturePage(glyphsPagesBuffered, pixelBuffer);
                glyphsPageBuffers.add(pixelBuffer);
                pixelBuffer.flip();
            }

            gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, glyphsTextureName[0]);
            gl.glTexSubImage3D(GL2ES3.GL_TEXTURE_2D_ARRAY, 0, 0, 0, glyphsPagesBuffered, width, height, 1, EXTERNAL_FORMAT, GL.GL_UNSIGNED_BYTE, pixelBuffer);

            glyphsPagesBuffered++;
        }
    }

    private void bindGlyphs(GL3 gl, int uniformLocation, int textureUnit) {
        gl.glUniform1i(uniformLocation, textureUnit);
        gl.glActiveTexture(GL.GL_TEXTURE0 + textureUnit);
        gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, glyphsTextureName[0]);
    }
}
