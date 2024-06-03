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
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 *
 * @author cygnus_x-1
 * @author twilight_sparkle
 */
public class FpsBatcher implements SceneBatcher {

    // How many icon indices are available for icons?
    // We reserve the highest number for "unspecified".
    private static final int MAX_ICON_INDEX = 65535 - 1;
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    private static final String COLOR_SHADER_NAME = "backgroundIconColor";
    private static final String ICON_SHADER_NAME = "data";
    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int ICON_BUFFER_WIDTH = 2;
    private static final int DIGIT_ICON_OFFSET = 4;

    private final Batch batch;
    private final int colorTarget;
    private final int iconTarget;
    private float pixelDensity;
    private float projectionScale;
    private int shader;

    // Uniform locations in the shader for drawing the batch
    private int shaderMVMatrix;
    private int shaderPMatrix;
    private int shaderVisibilityLow;
    private int shaderVisibilityHigh;
    private int shaderImagesTexture;
    private int shaderPixelDensity;
    private int shaderPScale;

    public FpsBatcher() {

        // Create the batch
        this.batch = new Batch(GL.GL_POINTS);
        this.colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
        this.iconTarget = batch.newIntBuffer(ICON_BUFFER_WIDTH, false);
        this.pixelDensity = 0;
        this.projectionScale = 0;
    }

    public void setPixelDensity(final float pixelDensity) {
        this.pixelDensity = pixelDensity;
    }

    public void setProjectionScale(final float projectionScale) {
        this.projectionScale = projectionScale;
    }

    @Override
    public void createShader(final GL3 gl) throws IOException {

        // Create the shader
        shader = SharedDrawable.getSimpleIconShader(gl, colorTarget, COLOR_SHADER_NAME, iconTarget, ICON_SHADER_NAME);

        // Set up uniform locations in the shader
        shaderMVMatrix = gl.glGetUniformLocation(shader, "mvMatrix");
        shaderPMatrix = gl.glGetUniformLocation(shader, "pMatrix");
        shaderVisibilityLow = gl.glGetUniformLocation(shader, "visibilityLow");
        shaderVisibilityHigh = gl.glGetUniformLocation(shader, "visibilityHigh");
        shaderImagesTexture = gl.glGetUniformLocation(shader, "images");
        shaderPixelDensity = gl.glGetUniformLocation(shader, "pixelDensity");
        shaderPScale = gl.glGetUniformLocation(shader, "pScale");
    }

    private int updateIconTexture(final GL3 gl) {
        final int[] v = new int[1];
        gl.glGetIntegerv(GL2ES3.GL_MAX_ARRAY_TEXTURE_LAYERS, v, 0);
        final int maxTextureLayers = v[0];
        GLTools.LOADED_ICON_HELPER.setMaximumTextureLayers(maxTextureLayers);
        return GLTools.loadSharedIconTextures(gl, GLTools.MAX_ICON_WIDTH, GLTools.MAX_ICON_HEIGHT);
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {
        final FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(COLOR_BUFFER_WIDTH * 2);
        bufferColorInfo(0, colorBuffer, ConstellationColor.WHITE);
        bufferColorInfo(1, colorBuffer, ConstellationColor.WHITE);
        colorBuffer.flip();
        final IntBuffer iconBuffer = Buffers.newDirectIntBuffer(ICON_BUFFER_WIDTH * 2);
        bufferIconInfo(0, iconBuffer, 0, 0);
        bufferIconInfo(1, iconBuffer, 0, 0);
        iconBuffer.flip();
        return gl -> {
            batch.initialise(2);
            batch.buffer(gl, colorTarget, colorBuffer);
            batch.buffer(gl, iconTarget, iconBuffer);
            // Ensure that the icons for the digits are loaded into the texture
            for (int digit = 0; digit < 10; digit++) {
                GLTools.LOADED_ICON_HELPER.addIcon(Integer.toString(digit));
            }
            updateIconTexture(gl);
            batch.finalise(gl);
        };
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        return gl -> batch.dispose(gl);
    }

    @Override
    public boolean batchReady() {
        return batch.isDrawable();
    }

    @Override
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale) {
        if (batch.isDrawable()) {
            gl.glUseProgram(shader);

            // Uniform variables
            gl.glUniformMatrix4fv(shaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1i(shaderImagesTexture, TextureUnits.ICONS);
            gl.glUniform1f(shaderPixelDensity, pixelDensity);
            gl.glUniform1f(shaderPScale, projectionScale);
            batch.draw(gl);
        }
    }

    @FunctionalInterface
    public static interface IconOperation {

        int buffer(final int pos, final IntBuffer buffer, final int digit, final int offset);
    }

    public GLRenderableUpdateTask updateIcons(final int[] digits) {
        final IconOperation operation = this::bufferIconInfo;
        final IntBufferConnection connector = gl -> batch.connectIntBuffer(gl, iconTarget);
        final BufferDisconnection disconnector = gl -> batch.disconnectBuffer(gl, iconTarget);
        final int width = ICON_BUFFER_WIDTH;

        final boolean[] updateMask = new boolean[width];
        Arrays.fill(updateMask, true);
        final int maskSize = updateMask.length;
        final int numChanges = digits.length;
        final IntBuffer updateBuffer = Buffers.newDirectIntBuffer(maskSize * numChanges);
        final int[] bufferUpdatePositions = new int[numChanges];
        int updatePosition = 0;
        for (int i = 0; i < numChanges; i++) {
            final int updatedPosition = operation.buffer(i, updateBuffer, digits[i], i * DIGIT_ICON_OFFSET);
            if (updatedPosition >= 0) {
                bufferUpdatePositions[updatePosition++] = updatedPosition;
            }
        }
        final int numUpdates = updatePosition;
        updateBuffer.flip();
        return gl -> {
            final IntBuffer buffer = connector.connect(gl);
            for (int i = 0; i < numUpdates; i++) {
                buffer.position(bufferUpdatePositions[i] * maskSize);
                for (boolean update : updateMask) {
                    if (update) {
                        buffer.put(updateBuffer.get());
                    } else {
                        buffer.get();
                    }
                }
            }
            disconnector.disconnect(gl);
        };
    }

    private int bufferIconInfo(final int pos, final IntBuffer buffer, final int digit, final int offset) {
        final int foregroundIconIndex = GLTools.LOADED_ICON_HELPER.addIcon(Integer.toString(digit));
        if (foregroundIconIndex > MAX_ICON_INDEX) {
            final String msg = String.format("Too many foreground icons: %d > %d", foregroundIconIndex, MAX_ICON_INDEX);
            throw new IllegalStateException(msg);
        }

        final int backgroundIconIndex = GLTools.TRANSPARENT_ICON_INDEX;
        if (backgroundIconIndex > MAX_ICON_INDEX) {
            final String msg = String.format("Too many background icons: %d > %d", backgroundIconIndex, MAX_ICON_INDEX);
            throw new IllegalStateException(msg);
        }

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);

        buffer.put(icons);
        buffer.put(offset);
        return pos;
    }

    @FunctionalInterface
    public static interface ColorOperation {

        int buffer(final int pos, final FloatBuffer buffer, final ConstellationColor color);
    }

    public GLRenderableUpdateTask updateColors(final ConstellationColor color) {
        final ColorOperation operation = this::bufferColorInfo;
        final FloatBufferConnection connector = gl -> batch.connectFloatBuffer(gl, colorTarget);
        final BufferDisconnection disconnector = gl -> batch.disconnectBuffer(gl, colorTarget);
        final int width = COLOR_BUFFER_WIDTH;

        final boolean[] updateMask = new boolean[width];
        Arrays.fill(updateMask, true);
        final int maskSize = updateMask.length;
        final int numChanges = 2;
        final FloatBuffer updateBuffer = Buffers.newDirectFloatBuffer(maskSize * numChanges);
        final int[] bufferUpdatePositions = new int[numChanges];
        int updatePos = 0;
        for (int i = 0; i < numChanges; i++) {
            final int updatedPosition = operation.buffer(i, updateBuffer, color);
            if (updatedPosition >= 0) {
                bufferUpdatePositions[updatePos++] = updatedPosition;
            }
        }
        final int numUpdates = updatePos;
        updateBuffer.flip();
        return gl -> {
            final FloatBuffer buffer = connector.connect(gl);
            for (int i = 0; i < numUpdates; i++) {
                buffer.position(bufferUpdatePositions[i] * maskSize);
                for (boolean update : updateMask) {
                    if (update) {
                        buffer.put(updateBuffer.get());
                    } else {
                        buffer.get();
                    }
                }
            }
            disconnector.disconnect(gl);
        };
    }

    private int bufferColorInfo(final int pos, final FloatBuffer colorBuffer, final ConstellationColor color) {
        colorBuffer.put(color.getRed());
        colorBuffer.put(color.getGreen());
        colorBuffer.put(color.getBlue());
        colorBuffer.put(color.getAlpha());
        return pos;
    }
}
