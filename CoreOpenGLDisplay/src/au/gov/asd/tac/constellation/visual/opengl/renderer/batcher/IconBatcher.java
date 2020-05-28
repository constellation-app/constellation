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
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

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

//    private final Batch batch;
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

//    private final int colorTarget;
//    private final int iconTarget;
    private static final int ICON_BUFFER_WIDTH = 4;
    private static final int COLOR_BUFFER_WIDTH = 4;

    public IconBatcher() {

        // Create the batch
//        batch = new Batch(GL30.GL_POINTS);
//        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
//        iconTarget = batch.newIntBuffer(ICON_BUFFER_WIDTH, false);
    }

    @Override
    public boolean batchReady() {
        return false;//return batch.isDrawable();
    }

    @Override
    public void createShader(/*GL30 gl*/) throws IOException {
//
//        // Create the shader
//        shader = SharedDrawable.getVertexIconShader(gl, colorTarget, COLOR_SHADER_NAME, iconTarget, ICON_SHADER_NAME);
//
//        // Set up uniform locations in the shader
//        shaderMVMatrix = GL30.glGetUniformLocation(shader, "mvMatrix");
//        shaderPMatrix = GL30.glGetUniformLocation(shader, "pMatrix");
//        shaderLocDrawHitTest = GL30.glGetUniformLocation(shader, "drawHitTest");
//        shaderVisibilityLow = GL30.glGetUniformLocation(shader, "visibilityLow");
//        shaderVisibilityHigh = GL30.glGetUniformLocation(shader, "visibilityHigh");
//        shaderMorphMix = GL30.glGetUniformLocation(shader, "morphMix");
//        shaderXyzTexture = GL30.glGetUniformLocation(shader, "xyzTexture");
//        shaderImagesTexture = GL30.glGetUniformLocation(shader, "images");
//        shaderFlagsTexture = GL30.glGetUniformLocation(shader, "flags");
//        shaderHighlightColor = GL30.glGetUniformLocation(shader, "highlightColor");
//        shaderPixelDensity = GL30.glGetUniformLocation(shader, "pixelDensity");
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
//        return gl -> {
//            batch.dispose(gl);
//        };
return null;
    }

    public int updateIconTexture(/*final GL30 gl*/) {
//        final int[] v = new int[1];
//        GL30.glGetIntegerv(GL30.GL_MAX_ARRAY_TEXTURE_LAYERS, v);
//        final int maxTextureLayers = v[0];
//        GLTools.LOADED_ICON_HELPER.setMaximumTextureLayers(maxTextureLayers);
//        return GLTools.loadSharedIconTextures(gl, GLTools.MAX_ICON_WIDTH, GLTools.MAX_ICON_HEIGHT);
return 0;
    }

    public GLRenderableUpdateTask updateIcons(final VisualAccess access, final VisualChange change) {
//        return SceneBatcher.updateIntBufferTask(change, access, this::bufferIconInfo, gl -> {
//            return batch.connectIntBuffer(gl, iconTarget);
//        }, gl -> {
//            batch.disconnectBuffer(gl, iconTarget);
//        }, ICON_BUFFER_WIDTH);
return null;
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {
        final int numVertices = access.getVertexCount();
        final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(COLOR_BUFFER_WIDTH * numVertices);
        final IntBuffer iconBuffer = BufferUtils.createIntBuffer(ICON_BUFFER_WIDTH * numVertices);
        for (int pos = 0; pos < numVertices; pos++) {
            bufferColorInfo(pos, colorBuffer, access);
            bufferIconInfo(pos, iconBuffer, access);
        }
        colorBuffer.flip();
        iconBuffer.flip();
//        return gl -> {
//            if (numVertices > 0) {
//                batch.initialise(numVertices);
//                batch.buffer(gl, colorTarget, colorBuffer);
//                batch.buffer(gl, iconTarget, iconBuffer);
//                batch.finalise(gl);
//            }
//        };
return null;
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
        final int nWDecoratorIndex = nWDecoratorName != null ? GLTools.LOADED_ICON_HELPER.addIcon(nWDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;
        final int sWDecoratorIndex = sWDecoratorName != null ? GLTools.LOADED_ICON_HELPER.addIcon(sWDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;
        final int sEDecoratorIndex = sEDecoratorName != null ? GLTools.LOADED_ICON_HELPER.addIcon(sEDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;
        final int nEDecoratorIndex = nEDecoratorName != null ? GLTools.LOADED_ICON_HELPER.addIcon(nEDecoratorName) : GLTools.TRANSPARENT_ICON_INDEX;

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
//        return SceneBatcher.updateFloatBufferTask(change, access, this::bufferColorInfo, gl -> {
//            return batch.connectFloatBuffer(gl, colorTarget);
//        }, gl -> {
//            batch.disconnectBuffer(gl, colorTarget);
//        }, COLOR_BUFFER_WIDTH);
return null;
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
//        return gl -> {
//            highlightColorMatrix = new float[]{highlightColor.getRed(), 0, 0, 0,
//                0, highlightColor.getGreen(), 0, 0,
//                0, 0, highlightColor.getBlue(), 0,
//                0, 0, 0, 1
//            };
//        };
return null;
    }

    public void setPixelDensity(final float pixelDensity) {
        this.pixelDensity = pixelDensity;
    }

    public void setNextDrawIsHitTest() {
        this.drawForHitTest = true;
    }

    @Override
    public void drawBatch(/*final GL30 gl, */final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix) {
//        if (batch.isDrawable()) {
//            GL30.glUseProgram(shader);
//
//            // Uniform variables
//            if (drawForHitTest) {
//                GL30.glUniform1i(shaderLocDrawHitTest, GL30.GL_TRUE);
//            } else {
//                GL30.glUniform1i(shaderLocDrawHitTest, GL30.GL_FALSE);
//            }
//            GL30.glUniformMatrix4fv(shaderMVMatrix, false, mvMatrix.a);
//            GL30.glUniformMatrix4fv(shaderPMatrix, false, pMatrix.a);
//            GL30.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
//            GL30.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
//            GL30.glUniform1f(shaderPixelDensity, pixelDensity);
//            GL30.glUniform1f(shaderMorphMix, camera.getMix());
//            GL30.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
//            GL30.glUniform1i(shaderImagesTexture, TextureUnits.ICONS);
//            GL30.glUniform1i(shaderFlagsTexture, TextureUnits.VERTEX_FLAGS);
//            GL30.glUniformMatrix4fv(shaderHighlightColor, false, highlightColorMatrix);
//            batch.draw(gl);
//        }
        drawForHitTest = false;
    }
}
