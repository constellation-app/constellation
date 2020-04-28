/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import java.nio.ByteBuffer;

/**
 * Encapsulate an integer buffer.
 *
 * @author algol
 */
public class ByteTextureBuffer extends TextureBuffer<ByteBuffer> {

    public ByteTextureBuffer(final GL3 gl, final ByteBuffer buffer) {
        super(gl, buffer);
    }

    @Override
    protected int sizeOfType() {
        return Buffers.SIZEOF_BYTE;
    }

    @Override
    protected int internalFormat() {
        return GL3.GL_R8I;
    }

    @Override
    public ByteBuffer connectBuffer(GL3 gl) {
        gl.glBindBuffer(GL3.GL_TEXTURE_BUFFER, getBufferName());
        return gl.glMapBuffer(GL3.GL_TEXTURE_BUFFER, GL3.GL_READ_WRITE);
    }

}
