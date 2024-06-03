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
import au.gov.asd.tac.constellation.utilities.graphics.FloatArray;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author twilight_sparkle
 */
public class BlazeBatcher implements SceneBatcher {

    private static final String COLOR_SHADER_NAME = "blazeColor";
    private static final String BLAZE_INFO_SHADER_NAME = "blazeData";

    private final Batch batch;
    private int shader;

    private float blazeSize;
    private float blazeOpacity;

    // Uniform locations in the shader for drawing the batch
    private int shaderMVMatrix;
    private int shaderPMatrix;
    private int shaderVisibilityLow;
    private int shaderVisibilityHigh;
    private int shaderMorphMix;
    private int shaderXyzTexture;
    private int shaderImagesTexture;
    private int shaderScale;
    private int shaderOpacity;
    private int shaderGreyscale; // anaglyphic drawing

    private FloatArray blazeColors;
    private IntArray blazeInfo;

    private final int colorTarget;
    private final int infoTarget;

    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int INFO_BUFFER_WIDTH = 4;

    public BlazeBatcher() {

        // Create the batch
        batch = new Batch(GL.GL_POINTS);
        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
        infoTarget = batch.newIntBuffer(INFO_BUFFER_WIDTH, false);
    }

    @Override
    public boolean batchReady() {
        return batch.isDrawable();
    }

    @Override
    public void createShader(GL3 gl) throws IOException {

        // Create the shader
        shader = SharedDrawable.getBlazeShader(gl, colorTarget, COLOR_SHADER_NAME, infoTarget, BLAZE_INFO_SHADER_NAME);

        // Set up uniform locations in the shader
        shaderMVMatrix = gl.glGetUniformLocation(shader, "mvMatrix");
        shaderPMatrix = gl.glGetUniformLocation(shader, "pMatrix");
        shaderVisibilityLow = gl.glGetUniformLocation(shader, "visibilityLow");
        shaderVisibilityHigh = gl.glGetUniformLocation(shader, "visibilityHigh");
        shaderMorphMix = gl.glGetUniformLocation(shader, "morphMix");
        shaderXyzTexture = gl.glGetUniformLocation(shader, "xyzTexture");
        shaderImagesTexture = gl.glGetUniformLocation(shader, "images");
        shaderScale = gl.glGetUniformLocation(shader, "scale");
        shaderOpacity = gl.glGetUniformLocation(shader, "opacity");
        shaderGreyscale = gl.glGetUniformLocation(shader, "greyscale");
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        return gl -> batch.dispose(gl);
    }

    public GLRenderableUpdateTask updateBlazes(final VisualAccess access, final VisualChange change) {
        // We build the whole batch again - can't update blazes in place at this stage.
        blazeColors.clear();
        blazeInfo.clear();
        fillBatch(access);

        return gl -> {
            batch.dispose(gl);
            batch.initialise(blazeInfo.size() / INFO_BUFFER_WIDTH);
            batch.buffer(gl, colorTarget, FloatBuffer.wrap(blazeColors.rawArray()));
            batch.buffer(gl, infoTarget, IntBuffer.wrap(blazeInfo.rawArray()));
            batch.finalise(gl);
        };
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {
        fillBatch(access);
        return gl -> {
            batch.initialise(blazeInfo.size() / INFO_BUFFER_WIDTH);
            batch.buffer(gl, colorTarget, FloatBuffer.wrap(blazeColors.rawArray()));
            batch.buffer(gl, infoTarget, IntBuffer.wrap(blazeInfo.rawArray()));
            batch.finalise(gl);
        };
    }

    public GLRenderableUpdateTask updateSizeAndOpacity(final VisualAccess access) {
        final float updatedBlazeSize = access.getBlazeSize();
        final float updatedBlazeOpacity = access.getBlazeOpacity();
        return gl -> {
            blazeSize = updatedBlazeSize;
            blazeOpacity = updatedBlazeOpacity;
        };
    }

    private void fillBatch(final VisualAccess access) {
        blazeColors = new FloatArray();
        blazeInfo = new IntArray();
        for (int pos = 0; pos < access.getVertexCount(); pos++) {
            bufferBlaze(pos, blazeColors, blazeInfo, access);
        }
        blazeColors.trimToSize();
        blazeInfo.trimToSize();
    }

    private void bufferBlaze(final int pos, final FloatArray colorBuffer, final IntArray infoBuffer, final VisualAccess access) {
        if (access.isBlazed(pos)) {
            final ConstellationColor blazeColor = access.getBlazeColor(pos);
            final int blazeAngle = access.getBlazeAngle(pos);
            final float visibility = access.getVertexVisibility(pos);

            colorBuffer.add(blazeColor.getRed(), blazeColor.getGreen(), blazeColor.getBlue(), visibility);
            infoBuffer.add(pos, -1, blazeAngle, 0);
        }
    }

    @Override
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale) {

        if (batch.isDrawable()) {
            gl.glUseProgram(shader);
            gl.glUniformMatrix4fv(shaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(shaderMorphMix, camera.getMix());
            gl.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1i(shaderImagesTexture, TextureUnits.ICONS);
            gl.glUniform1f(shaderScale, blazeSize);
            gl.glUniform1f(shaderOpacity, blazeOpacity);
            gl.glUniform1i(shaderGreyscale, greyscale ? 1 : 0);

            gl.glDisable(GL.GL_DEPTH_TEST);
            batch.draw(gl);
            gl.glEnable(GL.GL_DEPTH_TEST);
        }
    }
}
