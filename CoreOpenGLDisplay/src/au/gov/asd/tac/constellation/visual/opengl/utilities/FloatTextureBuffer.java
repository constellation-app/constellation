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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulate a float buffer.
 *
 * @author algol
 */
public class FloatTextureBuffer extends TextureBuffer<FloatBuffer> {

    private static final Logger LOGGER = Logger.getLogger(FloatTextureBuffer.class.getName());

    public FloatTextureBuffer(final GL3 gl, final FloatBuffer buffer) {
        super(gl, buffer);
    }

    @Override
    protected int sizeOfType() {
        return Buffers.SIZEOF_FLOAT;
    }

    @Override
    protected int internalFormat() {
        return GL.GL_RGBA32F;
    }

    @Override
    public FloatBuffer connectBuffer(GL3 gl) {
        gl.glBindBuffer(GL2ES3.GL_TEXTURE_BUFFER, getBufferName());
        
        // The .glMapBuffer(GL2ES3.GL_TEXTURE_BUFFER, GL2ES3.GL_READ_WRITE); method is throwing a GL exception when 
        // multiple animations are runnng on large graphs and a graph view resize is triggered by opening or closing another view.
        // The error does not cause disruption to the behaviours or Constellation but does result in in an error message being thrown
        try{
            final ByteBuffer bytebuffer = gl.glMapBuffer(GL2ES3.GL_TEXTURE_BUFFER, GL2ES3.GL_READ_WRITE); 
            return bytebuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
        } catch (final GLException ex){
            LOGGER.log(Level.SEVERE, String.format("A GLException occured: %s", ex.getLocalizedMessage()));
        }
        return buffer;
    }

}
