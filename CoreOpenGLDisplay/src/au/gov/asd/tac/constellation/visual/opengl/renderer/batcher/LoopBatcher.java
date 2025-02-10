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
package au.gov.asd.tac.constellation.visual.opengl.renderer.batcher;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author twilight_sparkle
 * @author antares
 */
public class LoopBatcher implements SceneBatcher {

    private static final String COLOR_SHADER_NAME = "vColor";
    private static final String LOOP_INFO_SHADER_NAME = "data";

    private final Batch batch;
    private int shader;

    private boolean drawForHitTest = false;

    // Uniform locations in the shader for drawing the batch
    private int shaderMVMatrix;
    private int shaderPMatrix;
    private int shaderLocDrawHitTest;
    private int shaderVisibilityLow;
    private int shaderVisibilityHigh;
    private int shaderMorphMix;
    private int shaderXyzTexture;
    private int shaderImagesTexture;
    private int shaderGreyscale; // anaglyphic drawing

    private final int colorTarget;
    private final int loopInfoTarget;

    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int LOOP_INFO_BUFFER_WIDTH = 4;

    public LoopBatcher() {

        // create the batch
        batch = new Batch(GL.GL_POINTS);
        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
        loopInfoTarget = batch.newIntBuffer(LOOP_INFO_BUFFER_WIDTH, false);
    }

    @Override
    public boolean batchReady() {
        return batch.isDrawable();
    }

    @Override
    public void createShader(GL3 gl) throws IOException {

        // Create the shader
        shader = SharedDrawable.getLoopShader(gl, colorTarget, COLOR_SHADER_NAME, loopInfoTarget, LOOP_INFO_SHADER_NAME);

        // Set up uniform locations in the shader
        shaderMVMatrix = gl.glGetUniformLocation(shader, "mvMatrix");
        shaderPMatrix = gl.glGetUniformLocation(shader, "pMatrix");
        shaderLocDrawHitTest = gl.glGetUniformLocation(shader, "drawHitTest");
        shaderVisibilityLow = gl.glGetUniformLocation(shader, "visibilityLow");
        shaderVisibilityHigh = gl.glGetUniformLocation(shader, "visibilityHigh");
        shaderMorphMix = gl.glGetUniformLocation(shader, "morphMix");
        shaderXyzTexture = gl.glGetUniformLocation(shader, "xyzTexture");
        shaderImagesTexture = gl.glGetUniformLocation(shader, "images");
        shaderGreyscale = gl.glGetUniformLocation(shader, "greyscale");
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        loopPosToBufferPos.clear();
        return gl -> batch.dispose(gl);
    }

    private final SortedMap<Integer, Integer> loopPosToBufferPos = new TreeMap<>();

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {

        int loopCounter = 0;

        for (int i = 0; i < access.getConnectionCount(); i++) {
            if (access.getConnectionLowVertex(i) == access.getConnectionHighVertex(i)) {
                loopPosToBufferPos.put(i, loopCounter++);
            }
        }

        final int numLoops = loopCounter;
        FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(numLoops * COLOR_BUFFER_WIDTH);
        IntBuffer dataBuffer = Buffers.newDirectIntBuffer(numLoops * LOOP_INFO_BUFFER_WIDTH);
        loopPosToBufferPos.keySet().forEach(pos -> {
            bufferColorInfo(pos, colorBuffer, access);
            bufferLoopInfo(pos, dataBuffer, access);
        });
        colorBuffer.flip();
        dataBuffer.flip();

        return gl -> {
            if (numLoops > 0) {
                batch.initialise(numLoops);
                batch.buffer(gl, colorTarget, colorBuffer);
                batch.buffer(gl, loopInfoTarget, dataBuffer);
                batch.finalise(gl);
            }
        };
    }

    private int bufferLoopInfo(final int pos, final IntBuffer dataBuffer, final VisualAccess access) {
        if (loopPosToBufferPos.containsKey(pos)) {
            final int representativeTransactionId = access.getConnectionId(pos);
            final int loopIconIndex = access.isConnectionDirected(pos) ? GLTools.LOOP_DIRECTED_ICON_INDEX : GLTools.LOOP_UNDIRECTED_ICON_INDEX;
            final int flags = (access.isConnectionDimmed(pos) ? 2 : 0) | (access.isConnectionSelected(pos) ? 1 : 0);
            final int xyzTexturePosition = access.getConnectionLowVertex(pos);

            dataBuffer.put(representativeTransactionId);
            dataBuffer.put(xyzTexturePosition);
            dataBuffer.put(flags);
            dataBuffer.put(loopIconIndex);
            return loopPosToBufferPos.get(pos);
        }
        return -1;
    }

    private int bufferColorInfo(final int pos, final FloatBuffer colorBuffer, final VisualAccess access) {
        if (loopPosToBufferPos.containsKey(pos)) {
            final ConstellationColor color = access.getConnectionColor(pos);

            colorBuffer.put(color.getRed());
            colorBuffer.put(color.getGreen());
            colorBuffer.put(color.getBlue());
            colorBuffer.put(access.getConnectionVisibility(pos));
            return loopPosToBufferPos.get(pos);
        }
        return -1;
    }

    public GLRenderableUpdateTask updateInfo(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateIntBufferTask(change, access, this::bufferLoopInfo, gl -> batch.connectIntBuffer(gl, loopInfoTarget),
                 gl -> batch.disconnectBuffer(gl, loopInfoTarget),
                 LOOP_INFO_BUFFER_WIDTH);
    }

    public GLRenderableUpdateTask updateColors(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateFloatBufferTask(change, access, this::bufferColorInfo, gl -> batch.connectFloatBuffer(gl, colorTarget),
                 gl -> batch.disconnectBuffer(gl, colorTarget),
                 COLOR_BUFFER_WIDTH);
    }

    public void setNextDrawIsHitTest() {
        this.drawForHitTest = true;
    }

    @Override
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale) {

        if (batch.isDrawable()) {
            gl.glUseProgram(shader);

            // Uniform variables
            if (drawForHitTest) {
                gl.glUniform1i(shaderLocDrawHitTest, GL.GL_TRUE);
                drawForHitTest = false;
            } else {
                gl.glUniform1i(shaderLocDrawHitTest, GL.GL_FALSE);
            }
            gl.glUniformMatrix4fv(shaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(shaderMorphMix, camera.getMix());
            gl.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1i(shaderImagesTexture, TextureUnits.ICONS);
            gl.glUniform1i(shaderGreyscale, greyscale ? 1 : 0);
            batch.draw(gl);
        }
    }
}
