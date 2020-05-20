/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import java.nio.Buffer;
import org.lwjgl.opengl.GL30;

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
    public TextureBuffer(final GL30 gl, final BufferType buffer) {
        final int nItems = buffer.limit();

        // Generate a buffer object name.
        bufferName = new int[1];
        // TODO_TT:
//        gl.glGenBuffers(1, bufferName, 0);

        // Bind the buffer.
        // Initialise the buffer's data store.
        // TODO_TT:
//        gl.glBindBuffer(GL30.GL_TEXTURE_BUFFER, bufferName[0]);
//        gl.glBufferData(GL30.GL_TEXTURE_BUFFER, sizeOfType() * nItems, buffer, GL30.GL_DYNAMIC_DRAW);

        // Generate a texture name.
        textureName = new int[1];
        // TODO_TT:
//        gl.glGenTextures(1, textureName, 0);

        // Bind the texture to the buffer.
        // TODO_TT:
//        gl.glBindTexture(GL30.GL_TEXTURE_BUFFER, textureName[0]);
//        gl.glTexBuffer(GL30.GL_TEXTURE_BUFFER, internalFormat(), bufferName[0]);

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

    public abstract BufferType connectBuffer(final GL30 gl);

    public void disconnectBuffer(final GL30 gl) {
        // TODO_TT:
//        gl.glBindBuffer(GL30.GL_TEXTURE_BUFFER, bufferName[0]);
//        gl.glUnmapBuffer(GL30.GL_TEXTURE_BUFFER);
    }

    /**
     * Send the data in the buffer to OpenGL.
     *
     * @param gl the current OpenGL context.
     * @param offset The offset (first [type] value) in the buffer to start the
     * update range from.
     * @param size The size (number of [type] values) of the range to update.
     */
    public void update(final GL30 gl, final int offset, final int size) {
        buffer.position(sizeOfType() * offset);
        // TODO_TT:
//        gl.glBindBuffer(GL30.GL_TEXTURE_BUFFER, bufferName[0]);
//        gl.glBufferSubData(GL30.GL_TEXTURE_BUFFER, sizeOfType() * offset, sizeOfType() * size, buffer);
    }

    public void uniform(final GL30 gl, final int uniform, final int textureUnit) {
        // Bind the uniform to the texture unit.
        GL30.glUniform1i(uniform, textureUnit);

        // Activate the texture unit.
        GL30.glActiveTexture(GL30.GL_TEXTURE0 + textureUnit);

        // Bind the texture to the texture unit.
        // TODO_TT:
//        gl.glBindTexture(GL30.GL_TEXTURE_BUFFER, textureName[0]);
    }

    public void dispose(final GL30 gl) {
        // TODO_TT:
//        gl.glDeleteTextures(1, textureName, 0);
//        gl.glDeleteBuffers(1, bufferName, 0);
    }
}
