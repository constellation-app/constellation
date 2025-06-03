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
package au.gov.asd.tac.constellation.visual.opengl.renderer.batcher;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
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

/**
 *
 * @author twilight_sparkle
 */
public class IconBatcher implements SceneBatcher {

    // How many icon indices are available for icons?
    // We reserve the highest number for "unspecified".
    private static final int MAX_ICON_INDEX = 65535 - 1;
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    private static final String COLOR_SHADER_NAME = "backgroundIconColor";
    private static final String ICON_SHADER_NAME = "data";

    private final Batch batch;
    private int shader;

    private float pixelDensity;
    private float[] highlightColorMatrix;
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
    private int shaderFlagsTexture;
    private int shaderHighlightColor;
    private int shaderPixelDensity;
    private int shaderGreyscale; // anaglyphic drawing

    private final int colorTarget;
    private final int iconTarget;
    private static final int ICON_BUFFER_WIDTH = 4;
    private static final int COLOR_BUFFER_WIDTH = 4;

    public IconBatcher() {

        // Create the batch
        batch = new Batch(GL.GL_POINTS);
        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
        iconTarget = batch.newIntBuffer(ICON_BUFFER_WIDTH, false);
    }

    @Override
    public boolean batchReady() {
        return batch.isDrawable();
    }

    @Override
    public void createShader(GL3 gl) throws IOException {

        // Create the shader
        shader = SharedDrawable.getVertexIconShader(gl, colorTarget, COLOR_SHADER_NAME, iconTarget, ICON_SHADER_NAME);

        // Set up uniform locations in the shader
        shaderMVMatrix = gl.glGetUniformLocation(shader, "mvMatrix");
        shaderPMatrix = gl.glGetUniformLocation(shader, "pMatrix");
        shaderLocDrawHitTest = gl.glGetUniformLocation(shader, "drawHitTest");
        shaderVisibilityLow = gl.glGetUniformLocation(shader, "visibilityLow");
        shaderVisibilityHigh = gl.glGetUniformLocation(shader, "visibilityHigh");
        shaderMorphMix = gl.glGetUniformLocation(shader, "morphMix");
        shaderXyzTexture = gl.glGetUniformLocation(shader, "xyzTexture");
        shaderImagesTexture = gl.glGetUniformLocation(shader, "images");
        shaderFlagsTexture = gl.glGetUniformLocation(shader, "flags");
        shaderHighlightColor = gl.glGetUniformLocation(shader, "highlightColor");
        shaderPixelDensity = gl.glGetUniformLocation(shader, "pixelDensity");
        shaderGreyscale = gl.glGetUniformLocation(shader, "greyscale");
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        return gl -> batch.dispose(gl);
    }

    public int updateIconTexture(final GL3 gl) {
        final int[] v = new int[1];
        gl.glGetIntegerv(GL2ES3.GL_MAX_ARRAY_TEXTURE_LAYERS, v, 0);
        final int maxTextureLayers = v[0];
        GLTools.LOADED_ICON_HELPER.setMaximumTextureLayers(maxTextureLayers);
        return GLTools.loadSharedIconTextures(gl, GLTools.MAX_ICON_WIDTH, GLTools.MAX_ICON_HEIGHT);
    }

    public GLRenderableUpdateTask updateIcons(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateIntBufferTask(change, access, this::bufferIconInfo, gl -> batch.connectIntBuffer(gl, iconTarget),
                gl -> batch.disconnectBuffer(gl, iconTarget),
                ICON_BUFFER_WIDTH);
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {
        final int numVertices = access.getVertexCount();
        final FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(COLOR_BUFFER_WIDTH * numVertices);
        final IntBuffer iconBuffer = Buffers.newDirectIntBuffer(ICON_BUFFER_WIDTH * numVertices);
        for (int pos = 0; pos < numVertices; pos++) {
            bufferColorInfo(pos, colorBuffer, access);
            bufferIconInfo(pos, iconBuffer, access);
        }
        colorBuffer.flip();
        iconBuffer.flip();
        return gl -> {
            if (numVertices > 0) {
                batch.initialise(numVertices);
                batch.buffer(gl, colorTarget, colorBuffer);
                batch.buffer(gl, iconTarget, iconBuffer);
                batch.finalise(gl);
            }
        };
    }

    private int bufferIconInfo(final int pos, final IntBuffer iconBuffer, final VisualAccess access) {
        final String foregroundIconName = access.getForegroundIcon(pos);
        final String backgroundIconName = access.getBackgroundIcon(pos);
        final int foregroundIconIndex = GLTools.LOADED_ICON_HELPER.addIcon(foregroundIconName);
        final int backgroundIconIndex = GLTools.LOADED_ICON_HELPER.addIcon(backgroundIconName);

        final String nWDecoratorName = access.getNWDecorator(pos);
        final String sWDecoratorName = access.getSWDecorator(pos);
        final String sEDecoratorName = access.getSEDecorator(pos);
        final String nEDecoratorName = access.getNEDecorator(pos);

        // Set the icons for any corners for which a decorator name is set. The name will  be checked in the loaded icon set
        // and loaded if found. If not found the decorator will be blank.
        final int nWDecoratorIndex = (nWDecoratorName != null && IconManager.iconExists(nWDecoratorName))
                ? GLTools.LOADED_ICON_HELPER.addIcon(nWDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;
        final int sWDecoratorIndex = (sWDecoratorName != null && IconManager.iconExists(sWDecoratorName))
                ? GLTools.LOADED_ICON_HELPER.addIcon(sWDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;
        final int sEDecoratorIndex = (sEDecoratorName != null && IconManager.iconExists(sEDecoratorName))
                ? GLTools.LOADED_ICON_HELPER.addIcon(sEDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;
        final int nEDecoratorIndex = (nEDecoratorName != null && IconManager.iconExists(nEDecoratorName))
                ? GLTools.LOADED_ICON_HELPER.addIcon(nEDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;

        if (nWDecoratorIndex > MAX_ICON_INDEX || sWDecoratorIndex > MAX_ICON_INDEX || sEDecoratorIndex > MAX_ICON_INDEX || nEDecoratorIndex > MAX_ICON_INDEX) {
            final String msg = "Decorator icon index is too large";
            throw new IllegalStateException(msg);
        }
        if (foregroundIconIndex > MAX_ICON_INDEX) {
            final String msg = String.format("Too many foreground icons: %d > %d", foregroundIconIndex, MAX_ICON_INDEX);
            throw new IllegalStateException(msg);
        }
        if (backgroundIconIndex > MAX_ICON_INDEX) {
            final String msg = String.format("Too many background icons: %d > %d", backgroundIconIndex, MAX_ICON_INDEX);
            throw new IllegalStateException(msg);
        }

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);

        iconBuffer.put(icons);
        iconBuffer.put(decoratorsWest);
        iconBuffer.put(decoratorsEast);
        iconBuffer.put(access.getVertexId(pos));
        return pos;
    }

    public GLRenderableUpdateTask updateColors(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateFloatBufferTask(change, access, this::bufferColorInfo, gl -> batch.connectFloatBuffer(gl, colorTarget),
                gl -> batch.disconnectBuffer(gl, colorTarget),
                COLOR_BUFFER_WIDTH);
    }

    private int bufferColorInfo(final int pos, final FloatBuffer colorBuffer, final VisualAccess access) {
        ConstellationColor color = access.getVertexColor(pos);
        colorBuffer.put(color.getRed());
        colorBuffer.put(color.getGreen());
        colorBuffer.put(color.getBlue());
        colorBuffer.put(access.getVertexVisibility(pos));
        return pos;
    }

    public GLRenderableUpdateTask setHighlightColor(final VisualAccess access) {
        final ConstellationColor highlightColor = access.getHighlightColor();
        return gl -> highlightColorMatrix = new float[]{highlightColor.getRed(), 0, 0, 0, 0,
            highlightColor.getGreen(), 0, 0, 0, 0,
            highlightColor.getBlue(), 0, 0, 0, 0, 1
        };
    }

    public void setPixelDensity(final float pixelDensity) {
        this.pixelDensity = pixelDensity;
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
            } else {
                gl.glUniform1i(shaderLocDrawHitTest, GL.GL_FALSE);
            }
            gl.glUniformMatrix4fv(shaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(shaderPixelDensity, pixelDensity);
            gl.glUniform1f(shaderMorphMix, camera.getMix());
            gl.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1i(shaderImagesTexture, TextureUnits.ICONS);
            gl.glUniform1i(shaderFlagsTexture, TextureUnits.VERTEX_FLAGS);
            gl.glUniformMatrix4fv(shaderHighlightColor, 1, false, highlightColorMatrix, 0);
            gl.glUniform1i(shaderGreyscale, greyscale ? 1 : 0);
            batch.draw(gl);
        }
        drawForHitTest = false;
    }
}
