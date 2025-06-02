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
package au.gov.asd.tac.constellation.visual.opengl.utilities;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import java.nio.Buffer;

/**
 * Encapsulates texture buffers of various types.
 *
 * @author sirius
 * @param <BufferType>
 */
public abstract class TextureBuffer<BufferType extends Buffer> {

    private final int[] bufferName;
    private final int[] textureName;
    protected final BufferType buffer;

    /**
     * Allocate a texture buffer.
     *
     * @param gl the current OpenGL context.
     * @param buffer A Buffer to be wrapped in a texture.
     */
    protected TextureBuffer(final GL3 gl, final BufferType buffer) {
        final int nItems = buffer.limit();

        // Generate a buffer object name.
        bufferName = new int[1];
        gl.glGenBuffers(1, bufferName, 0);

        // Bind the buffer.
        // Initialise the buffer's data store.
        gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, bufferName[0]);
        gl.glBufferData(GL2ES3.GL_TEXTURE_BUFFER, (long) sizeOfType() * nItems, buffer, GL.GL_DYNAMIC_DRAW);

        // Generate a texture name.
        textureName = new int[1];
        gl.glGenTextures(1, textureName, 0);

        // Bind the texture to the buffer.
        gl.glBindTexture(GL2ES3.GL_TEXTURE_BUFFER, textureName[0]);
        gl.glTexBuffer(GL2ES3.GL_TEXTURE_BUFFER, internalFormat(), bufferName[0]);

        this.buffer = buffer;
    }

    /**
     * The size of the type in the texture buffer.
     * <p>
     * One of Buffers.SIZEOF_*.
     *
     * @return The size of the type in the texture buffer.
     */
    protected abstract int sizeOfType();

    /**
     * The internal format of the texture buffer.
     * <p>
     * Typically GL.GL_RGBA32F for four floats, GL.GL_R8I for one byte, etc.
     *
     * @return The internal format of the texture buffer.
     */
    protected abstract int internalFormat();

    public BufferType getBuffer() {
        return buffer;
    }

    public int getBufferName() {
        return bufferName[0];
    }

    public int getTextureName() {
        return textureName[0];
    }

    public abstract BufferType connectBuffer(final GL3 gl);

    public void disconnectBuffer(final GL gl) {
        gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, bufferName[0]);
        gl.glUnmapBuffer(GL2ES3.GL_TEXTURE_BUFFER);
    }

    /**
     * Send the data in the buffer to OpenGL.
     *
     * @param gl the current OpenGL context.
     * @param offset The offset (first [type] value) in the buffer to start the
     * update range from.
     * @param size The size (number of [type] values) of the range to update.
     */
    public void update(final GL gl, final int offset, final int size) {
        buffer.position(sizeOfType() * offset);
        gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, bufferName[0]);
        gl.glBufferSubData(GL2ES3.GL_TEXTURE_BUFFER, (long) sizeOfType() * offset, (long) sizeOfType() * size, buffer);
    }

    public void uniform(final GL2ES2 gl, final int uniform, final int textureUnit) {
        // Bind the uniform to the texture unit.
        gl.glUniform1i(uniform, textureUnit);

        // Activate the texture unit.
        gl.glActiveTexture(GL.GL_TEXTURE0 + textureUnit);

        // Bind the texture to the texture unit.
        gl.glBindTexture(GL2ES3.GL_TEXTURE_BUFFER, textureName[0]);
    }

    public void dispose(final GL gl) {
        gl.glDeleteTextures(1, textureName, 0);
        gl.glDeleteBuffers(1, bufferName, 0);
    }
}
