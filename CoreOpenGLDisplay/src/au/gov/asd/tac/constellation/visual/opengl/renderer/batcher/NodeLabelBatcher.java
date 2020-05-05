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
import au.gov.asd.tac.constellation.utilities.graphics.FloatArray;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.LabelUtilities;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManager;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author twilight_sparkle
 */
public class NodeLabelBatcher implements GlyphManager.GlyphStream, SceneBatcher {

    // Shader variable names corresponding to data in the topBatch
    private static final String LABEL_FLOATS_SHADER_NAME = "glyphLocationData";
    private static final String LABEL_INTS_SHADER_NAME = "graphLocationData";

    // Batch and shader
    private final Batch topBatch;
    private final Batch bottomBatch;
    private FloatArray topLabelFloats;
    private IntArray topLabelInts;
    private FloatArray bottomLabelFloats;
    private IntArray bottomLabelInts;
    private FloatArray currentFloats;
    private IntArray currentInts;
    private int shader;

    // State information for populating the topBatch
    private int currentNodeId;
    private float currentVisibility;
    private int currentTotalScale;
    private int currentLabelNumber;

    // Objects providing label information which is constant across the graph
    private final Matrix44f labelBottomInfo = new Matrix44f();
    private final Matrix44f labelTopInfo = new Matrix44f();
    private final Matrix44f labelBottomInfoReference = new Matrix44f();
    private final Matrix44f labelTopInfoReference = new Matrix44f();
    private float[] backgroundColor;
    private float[] highlightColor;

    // Uniform locations in the shader for drawing the topBatch
    private int shaderMVMatrix;
    private int shaderPMatrix;
    private int shaderLabelBottomInfo;
    private int shaderLabelTopInfo;
    private int shaderLocWidth;
    private int shaderLocHeight;
    private int shaderVisibilityLow;
    private int shaderVisibilityHigh;
    private int shaderMorphMix;
    private int shaderBackgroundGlyphIndex;
    private int shaderBackgroundColor;
    private int shaderHighlightColor;
    private int shaderXyzTexture;
    private int shaderGlyphInfoTexture;
    private int shaderGlyphImageTexture;

    private final int labelFloatsTarget;
    private final int labelIntsTarget;

    private static final int FLOAT_BUFFERS_WIDTH = 4;
    private static final int INT_BUFFERS_WIDTH = 4;

    public NodeLabelBatcher() {

        // Create the batches
        topBatch = new Batch(GL3.GL_POINTS);
        labelFloatsTarget = topBatch.newFloatBuffer(FLOAT_BUFFERS_WIDTH, false);
        labelIntsTarget = topBatch.newIntBuffer(INT_BUFFERS_WIDTH, false);
        bottomBatch = new Batch(topBatch);
    }

    private void setCurrentContext(final int nodeId, final int totalScale, final float visibility, final int labelNumber) {
        currentNodeId = nodeId;
        currentTotalScale = totalScale;
        currentVisibility = visibility;
        currentLabelNumber = labelNumber;
    }

    @Override
    public void addGlyph(int glyphPosition, float x, float y) {
        currentFloats.add(glyphPosition, x, y, currentVisibility);
        currentInts.add(currentNodeId, currentTotalScale, currentLabelNumber, 0);
    }

    @Override
    public void newLine(float width) {
        currentFloats.add(SharedDrawable.getLabelBackgroundGlyphPosition(), -width / 2.0f - 0.2f, 0.0f, currentVisibility);
        currentInts.add(currentNodeId, currentTotalScale, currentLabelNumber, 0);
    }

    @Override
    public boolean batchReady() {
        return topBatch.isDrawable() && bottomBatch.isDrawable();
    }

    @Override
    public void createShader(GL3 gl) throws IOException {

        // Create the shader
        shader = SharedDrawable.getNodeLabelShader(gl, labelFloatsTarget, LABEL_FLOATS_SHADER_NAME, labelIntsTarget, LABEL_INTS_SHADER_NAME);

        // Set up uniform locations in the shader
        shaderMVMatrix = gl.glGetUniformLocation(shader, "mvMatrix");
        shaderPMatrix = gl.glGetUniformLocation(shader, "pMatrix");
        shaderLabelBottomInfo = gl.glGetUniformLocation(shader, "labelBottomInfo");
        shaderLabelTopInfo = gl.glGetUniformLocation(shader, "labelTopInfo");
        shaderLocWidth = gl.glGetUniformLocation(shader, "widthScalingFactor");
        shaderLocHeight = gl.glGetUniformLocation(shader, "heightScalingFactor");
        shaderVisibilityLow = gl.glGetUniformLocation(shader, "visibilityLow");
        shaderVisibilityHigh = gl.glGetUniformLocation(shader, "visibilityHigh");
        shaderMorphMix = gl.glGetUniformLocation(shader, "morphMix");
        shaderBackgroundGlyphIndex = gl.glGetUniformLocation(shader, "backgroundGlyphIndex");
        shaderBackgroundColor = gl.glGetUniformLocation(shader, "backgroundColor");
        shaderHighlightColor = gl.glGetUniformLocation(shader, "highlightColor");
        shaderXyzTexture = gl.glGetUniformLocation(shader, "xyzTexture");
        shaderGlyphInfoTexture = gl.glGetUniformLocation(shader, "glyphInfoTexture");
        shaderGlyphImageTexture = gl.glGetUniformLocation(shader, "glyphImageTexture");
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {
        topLabelFloats = new FloatArray();
        topLabelInts = new IntArray();
        bottomLabelFloats = new FloatArray();
        bottomLabelInts = new IntArray();
        fillTopLabels(access);
        fillBottomLabels(access);

        return gl -> {
            topBatch.initialise(topLabelFloats.size() / FLOAT_BUFFERS_WIDTH);
            topBatch.buffer(gl, labelFloatsTarget, FloatBuffer.wrap(topLabelFloats.rawArray()));
            topBatch.buffer(gl, labelIntsTarget, IntBuffer.wrap(topLabelInts.rawArray()));
            topBatch.finalise(gl);
            bottomBatch.initialise(bottomLabelFloats.size() / FLOAT_BUFFERS_WIDTH);
            bottomBatch.buffer(gl, labelFloatsTarget, FloatBuffer.wrap(bottomLabelFloats.rawArray()));
            bottomBatch.buffer(gl, labelIntsTarget, IntBuffer.wrap(bottomLabelInts.rawArray()));
            bottomBatch.finalise(gl);
        };
    }

    public GLRenderableUpdateTask updateTopLabels(final VisualAccess access) {
        // We build the whole batch again - can't update labels in place at this stage.
        topLabelFloats.clear();
        topLabelInts.clear();
        fillTopLabels(access);
        return gl -> {
            topBatch.dispose(gl);
            topBatch.initialise(topLabelFloats.size() / FLOAT_BUFFERS_WIDTH);
            topBatch.buffer(gl, labelFloatsTarget, FloatBuffer.wrap(topLabelFloats.rawArray()));
            topBatch.buffer(gl, labelIntsTarget, IntBuffer.wrap(topLabelInts.rawArray()));
            topBatch.finalise(gl);
        };
    }

    public GLRenderableUpdateTask updateBottomLabels(final VisualAccess access) {
        // We build the whole batch again - can't update labels in place at this stage.
        bottomLabelFloats.clear();
        bottomLabelInts.clear();
        fillBottomLabels(access);
        return gl -> {
            bottomBatch.dispose(gl);
            bottomBatch.initialise(bottomLabelFloats.size() / FLOAT_BUFFERS_WIDTH);
            bottomBatch.buffer(gl, labelFloatsTarget, FloatBuffer.wrap(bottomLabelFloats.rawArray()));
            bottomBatch.buffer(gl, labelIntsTarget, IntBuffer.wrap(bottomLabelInts.rawArray()));
            bottomBatch.finalise(gl);
        };
    }

    private void fillTopLabels(final VisualAccess access) {
        currentFloats = topLabelFloats;
        currentInts = topLabelInts;
        for (int pos = 0; pos < access.getVertexCount(); pos++) {
            bufferTopLabel(pos, access);
        }
        topLabelFloats.trimToSize();
        topLabelInts.trimToSize();
    }

    private void fillBottomLabels(final VisualAccess access) {
        currentFloats = bottomLabelFloats;
        currentInts = bottomLabelInts;
        for (int pos = 0; pos < access.getVertexCount(); pos++) {
            bufferBottomLabel(pos, access);
        }
        bottomLabelFloats.trimToSize();
        bottomLabelInts.trimToSize();
    }

    private void bufferBottomLabel(final int pos, final VisualAccess access) {
        final float visibility = access.getVertexVisibility(pos);
        int totalScale = LabelUtilities.NRADIUS_TO_LABEL_UNITS;
        for (int label = 0; label < access.getBottomLabelCount(); label++) {
            final String labelText = access.getVertexBottomLabelText(pos, label);
            ArrayList<String> lines = LabelUtilities.splitTextIntoLines(labelText);
            for (final String line : lines) {
                setCurrentContext(pos, -totalScale, visibility, label);
                SharedDrawable.getGlyphManager().renderTextAsLigatures(line, this);
                totalScale += labelBottomInfoReference.get(label, 3);
            }
        }
    }

    private void bufferTopLabel(final int pos, final VisualAccess access) {
        final float visibility = access.getVertexVisibility(pos);
        int totalScale = LabelUtilities.NRADIUS_TO_LABEL_UNITS;
        for (int label = 0; label < access.getTopLabelCount(); label++) {
            final String text = access.getVertexTopLabelText(pos, label);
            ArrayList<String> lines = LabelUtilities.splitTextIntoLines(text);
            Collections.reverse(lines);
            for (final String line : lines) {
                setCurrentContext(pos, totalScale, visibility, label);
                SharedDrawable.getGlyphManager().renderTextAsLigatures(line, this);
                totalScale += labelTopInfoReference.get(label, 3);
            }
        }
    }

    public GLRenderableUpdateTask setBottomLabelSizes(final VisualAccess access) {
        final int numBottomLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getBottomLabelCount());
        for (int i = 0; i < numBottomLabels; i++) {
            labelBottomInfoReference.set(i, 3, (int) (LabelUtilities.NRADIUS_TO_LABEL_UNITS * Math.min(access.getBottomLabelSize(i), LabelUtilities.MAX_LABEL_SIZE)));
        }
        return gl -> {
            labelBottomInfo.set(labelBottomInfoReference);
        };
    }

    public GLRenderableUpdateTask setBottomLabelColors(final VisualAccess access) {
        final int numBottomLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getBottomLabelCount());
        for (int i = 0; i < numBottomLabels; i++) {
            final ConstellationColor labelColor = access.getBottomLabelColor(i);
            labelBottomInfoReference.setRow(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue(), labelBottomInfoReference.get(i, 3), i);
        }
        return gl -> {
            labelBottomInfo.set(labelBottomInfoReference);
        };
    }

    public GLRenderableUpdateTask setTopLabelSizes(final VisualAccess access) {
        final int numTopLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getTopLabelCount());
        for (int i = 0; i < numTopLabels; i++) {
            labelTopInfoReference.set(i, 3, (int) (LabelUtilities.NRADIUS_TO_LABEL_UNITS * Math.min(access.getTopLabelSize(i), LabelUtilities.MAX_LABEL_SIZE)));
        }
        return gl -> {
            labelTopInfo.set(labelTopInfoReference);
        };
    }

    public GLRenderableUpdateTask setTopLabelColors(final VisualAccess access) {
        final int numTopLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getTopLabelCount());
        for (int i = 0; i < numTopLabels; i++) {
            final ConstellationColor labelColor = access.getTopLabelColor(i);
            labelTopInfoReference.setRow(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue(), labelTopInfoReference.get(i, 3), i);
        }
        return gl -> {
            labelTopInfo.set(labelTopInfoReference);
        };
    }

    public GLRenderableUpdateTask setHighlightColor(final VisualAccess access) {
        final ConstellationColor color = access.getHighlightColor();
        return gl -> {
            highlightColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), 1};
        };
    }

    public GLRenderableUpdateTask setBackgroundColor(final VisualAccess access) {
        final ConstellationColor color = access.getBackgroundColor();
        return gl -> {
            backgroundColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), 0.25f};
        };
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        return gl -> {
            topBatch.dispose(gl);
            bottomBatch.dispose(gl);
        };
    }

    @Override
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix) {

        if (topBatch.isDrawable() || bottomBatch.isDrawable()) {
            gl.glUseProgram(shader);

            // Let the glyph controller bind the glyph info and glyph image textures
            SharedDrawable.updateGlyphTextureController(gl);
            SharedDrawable.getGlyphTextureController().bind(gl, shaderGlyphInfoTexture, TextureUnits.GLYPH_INFO, shaderGlyphImageTexture, TextureUnits.GLYPHS);

            // Uniform variables
            gl.glUniformMatrix4fv(shaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderLabelBottomInfo, 1, false, labelBottomInfo.a, 0);
            gl.glUniformMatrix4fv(shaderLabelTopInfo, 1, false, labelTopInfo.a, 0);
            gl.glUniform1f(shaderLocWidth, SharedDrawable.getGlyphManager().getWidthScalingFactor());
            gl.glUniform1f(shaderLocHeight, SharedDrawable.getGlyphManager().getHeightScalingFactor());
            gl.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(shaderMorphMix, camera.getMix());
            gl.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1i(shaderBackgroundGlyphIndex, SharedDrawable.getLabelBackgroundGlyphPosition());
            gl.glUniform4fv(shaderBackgroundColor, 1, backgroundColor, 0);
            gl.glUniform4fv(shaderHighlightColor, 1, highlightColor, 0);

            if (topBatch.isDrawable()) {
                topBatch.draw(gl);
            }
            if (bottomBatch.isDrawable()) {
                bottomBatch.draw(gl);
            }
        }
    }
}
