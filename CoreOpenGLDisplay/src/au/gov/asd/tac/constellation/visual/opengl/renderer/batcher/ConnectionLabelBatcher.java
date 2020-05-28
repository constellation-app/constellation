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

import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.FloatArray;
import au.gov.asd.tac.constellation.utilities.graphics.IntArray;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.STUB_GL;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.LabelUtilities;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.GlyphManager;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;


/**
 *
 * @author twilight_sparkle
 */
public class ConnectionLabelBatcher implements GlyphManager.GlyphStream, SceneBatcher {

    // Shader variable names corresponding to data in the batch
    private static final String LABEL_FLOATS_SHADER_NAME = "glyphLocationData";
    private static final String LABEL_INTS_SHADER_NAME = "graphLocationData";
    private static final int MAX_STAGGERS = 7;

    // Batch and shader
//    private final Batch attributeLabelBatch;
//    private final Batch summaryLabelBatch;
    private FloatArray attributeLabelFloats;
    private IntArray attributeLabelInts;
    private FloatArray summaryLabelFloats;
    private IntArray summaryLabelInts;
    private FloatArray currentFloats;
    private IntArray currentInts;
    private int shader;

    // State information for populating the batch
    private int currentLowNodeId;
    private int currentHighNodeId;
    private int currentLinkLabelCount;
    private int currentStagger;
    private int currentOffset;
    private int nextLeftOffset;
    private int nextRightOffset;
    private float currentVisiblity;
    private int currentTotalScale;
    private int currentLabelNumber;
    private float currentWidth;

    // Information which is constant across the graph
    private final Matrix44f attributeLabelInfo = new Matrix44f();
    private final Matrix44f attributeLabelInfoReference = new Matrix44f();
    private final Matrix44f summaryLabelInfo = new Matrix44f();
    private Matrix44f currentLabelInfo;
    private float[] backgroundColor;
    private float[] highlightColor;

    // Uniform locations in the shader for drawing the batch
    private int shaderMVMatrix;
    private int shaderPMatrix;
    private int shaderLabelInfo;
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

//    private final int floatsTarget;
//    private final int intsTarget;

    private static final int FLOAT_BUFFER_WIDTH = 4;
    private static final int INT_BUFFER_WIDTH = 4;

    public ConnectionLabelBatcher() {

//        // Create the batch
//        attributeLabelBatch = new Batch(GL30.GL_POINTS);
//        final ConstellationColor summaryColor = VisualGraphDefaults.DEFAULT_LABEL_COLOR;
//        summaryLabelInfo.setRow(summaryColor.getRed(), summaryColor.getGreen(), summaryColor.getBlue(), VisualGraphDefaults.DEFAULT_LABEL_SIZE * LabelUtilities.NRADIUS_TO_LABEL_UNITS, 0);
//        floatsTarget = attributeLabelBatch.newFloatBuffer(FLOAT_BUFFER_WIDTH, false);
//        intsTarget = attributeLabelBatch.newIntBuffer(INT_BUFFER_WIDTH, false);
//        summaryLabelBatch = new Batch(attributeLabelBatch);
    }

    public void setCurrentConnection(final int lowNodeId, final int highNodeId, final int linkLabelCount) {
        currentStagger = 0;
        currentOffset = nextLeftOffset = nextRightOffset = 0;
        currentLowNodeId = lowNodeId;
        currentHighNodeId = highNodeId;
        currentLinkLabelCount = linkLabelCount;
    }

    public void nextParallelConnection(final int width) {
        currentStagger = currentStagger == MAX_STAGGERS ? 1 : currentStagger + 1;
        if (nextLeftOffset == 0) {
            nextLeftOffset += ((width / 2) + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
            nextRightOffset -= ((width / 2) + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
        } else if (nextLeftOffset <= -nextRightOffset) {
            currentOffset = nextLeftOffset + (width / 2);
            nextLeftOffset += (width + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
        } else {
            currentOffset = nextRightOffset - (width / 2);
            nextRightOffset -= (width + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
        }
    }

    public void setCurrentContext(final int totalScale, final float visibility, final int labelNumber) {
        currentVisiblity = visibility;
        currentTotalScale = totalScale;
        currentLabelNumber = labelNumber;
    }

    @Override
    public void addGlyph(int glyphPosition, float x, float y) {
        currentFloats.add(currentWidth, x, y, currentVisiblity);
        currentInts.add(currentLowNodeId, currentHighNodeId, (currentOffset << 16) + (currentTotalScale << 2) + currentLabelNumber, (glyphPosition << 8) + currentStagger * 256 / (Math.min(currentLinkLabelCount, MAX_STAGGERS) + 1));
    }

    @Override
    public void newLine(float width) {
        currentWidth = -width / 2.0f - 0.2f;
        currentFloats.add(currentWidth, currentWidth, 0.0f, currentVisiblity);
        currentInts.add(currentLowNodeId, currentHighNodeId, (currentOffset << 16) + (currentTotalScale << 2) + currentLabelNumber, (SharedDrawable.getLabelBackgroundGlyphPosition() << 8) + currentStagger * 256 / (Math.min(currentLinkLabelCount, MAX_STAGGERS) + 1));
    }

    @Override
    public boolean batchReady() {
        return false;//return attributeLabelBatch.isDrawable() || summaryLabelBatch.isDrawable();
    }

    @Override
    public void createShader(/*GL30 gl*/) throws IOException {
//
//        // Create the shader
//        shader = SharedDrawable.getConnectionLabelShader(gl, floatsTarget, LABEL_FLOATS_SHADER_NAME, intsTarget, LABEL_INTS_SHADER_NAME);
//
//        // Set up uniform locations in the shader
//        shaderMVMatrix = GL30.glGetUniformLocation(shader, "mvMatrix");
//        shaderPMatrix = GL30.glGetUniformLocation(shader, "pMatrix");
//        shaderLabelInfo = GL30.glGetUniformLocation(shader, "labelInfo");
//        shaderLocWidth = GL30.glGetUniformLocation(shader, "widthScalingFactor");
//        shaderLocHeight = GL30.glGetUniformLocation(shader, "heightScalingFactor");
//        shaderVisibilityLow = GL30.glGetUniformLocation(shader, "visibilityLow");
//        shaderVisibilityHigh = GL30.glGetUniformLocation(shader, "visibilityHigh");
//        shaderMorphMix = GL30.glGetUniformLocation(shader, "morphMix");
//        shaderBackgroundGlyphIndex = GL30.glGetUniformLocation(shader, "backgroundGlyphIndex");
//        shaderBackgroundColor = GL30.glGetUniformLocation(shader, "backgroundColor");
//        shaderHighlightColor = GL30.glGetUniformLocation(shader, "highlightColor");
//        shaderXyzTexture = GL30.glGetUniformLocation(shader, "xyzTexture");
//        shaderGlyphInfoTexture = GL30.glGetUniformLocation(shader, "glyphInfoTexture");
//        shaderGlyphImageTexture = GL30.glGetUniformLocation(shader, "glyphImageTexture");
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {

        attributeLabelFloats = new FloatArray();
        attributeLabelInts = new IntArray();
        summaryLabelFloats = new FloatArray();
        summaryLabelInts = new IntArray();
        fillLabels(access);

//        return gl -> {
//            attributeLabelBatch.initialise(attributeLabelFloats.size() / FLOAT_BUFFER_WIDTH);
//            attributeLabelBatch.buffer(gl, intsTarget, IntBuffer.wrap(attributeLabelInts.rawArray()));
//            attributeLabelBatch.buffer(gl, floatsTarget, FloatBuffer.wrap(attributeLabelFloats.rawArray()));
//            attributeLabelBatch.finalise(gl);
//            summaryLabelBatch.initialise(summaryLabelFloats.size() / FLOAT_BUFFER_WIDTH);
//            summaryLabelBatch.buffer(gl, intsTarget, IntBuffer.wrap(summaryLabelInts.rawArray()));
//            summaryLabelBatch.buffer(gl, floatsTarget, FloatBuffer.wrap(summaryLabelFloats.rawArray()));
//            summaryLabelBatch.finalise(gl);
//        };
return null;
    }

    public GLRenderableUpdateTask updateLabels(final VisualAccess access) {
        // We build the whole batch again - can't update labels in place at this stage.
        attributeLabelFloats.clear();
        attributeLabelInts.clear();
        fillLabels(access);
//        return gl -> {
//            attributeLabelBatch.dispose(gl);
//            attributeLabelBatch.initialise(attributeLabelFloats.size() / FLOAT_BUFFER_WIDTH);
//            attributeLabelBatch.buffer(gl, intsTarget, IntBuffer.wrap(attributeLabelInts.rawArray()));
//            attributeLabelBatch.buffer(gl, floatsTarget, FloatBuffer.wrap(attributeLabelFloats.rawArray()));
//            attributeLabelBatch.finalise(gl);
//            summaryLabelBatch.dispose(gl);
//            summaryLabelBatch.initialise(summaryLabelFloats.size() / FLOAT_BUFFER_WIDTH);
//            summaryLabelBatch.buffer(gl, intsTarget, IntBuffer.wrap(summaryLabelInts.rawArray()));
//            summaryLabelBatch.buffer(gl, floatsTarget, FloatBuffer.wrap(summaryLabelFloats.rawArray()));
//            summaryLabelBatch.finalise(gl);
//        };
return null;
    }

    private void fillLabels(final VisualAccess access) {
        for (int link = 0; link < access.getLinkCount(); link++) {
            final int connectionCount = access.getLinkConnectionCount(link);
            setCurrentConnection(access.getLinkLowVertex(link), access.getLinkHighVertex(link), connectionCount);
            for (int pos = 0; pos < connectionCount; pos++) {
                final int connection = access.getLinkConnection(link, pos);
                nextParallelConnection((int) (LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS * Math.min(LabelUtilities.MAX_TRANSACTION_WIDTH, access.getConnectionWidth(connection))));
                currentFloats = access.getIsLabelSummary(connection) ? summaryLabelFloats : attributeLabelFloats;
                currentInts = access.getIsLabelSummary(connection) ? summaryLabelInts : attributeLabelInts;
                currentLabelInfo = access.getIsLabelSummary(connection) ? summaryLabelInfo : attributeLabelInfoReference;
                bufferLabel(connection, access);
            }
        }
        attributeLabelFloats.trimToSize();
        attributeLabelInts.trimToSize();
        summaryLabelFloats.trimToSize();
        summaryLabelInts.trimToSize();
    }

    private void bufferLabel(final int pos, final VisualAccess access) {
        int totalScale = 0;
        final float visibility = access.getConnectionVisibility(pos);
        for (int label = 0; label < access.getConnectionLabelCount(pos); label++) {
            final String text = access.getConnectionLabelText(pos, label);
            ArrayList<String> lines = LabelUtilities.splitTextIntoLines(text);
            for (final String line : lines) {
                setCurrentContext(totalScale, visibility, label);
                SharedDrawable.getGlyphManager().renderTextAsLigatures(line, this);
                totalScale += currentLabelInfo.get(label, 3);
            }
        }
    }

    public GLRenderableUpdateTask setLabelColors(final VisualAccess access) {
        final int numConnectionLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getConnectionAttributeLabelCount());
        for (int i = 0; i < numConnectionLabels; i++) {
            final ConstellationColor labelColor = access.getConnectionLabelColor(i);
            attributeLabelInfoReference.setRow(labelColor.getRed(), labelColor.getGreen(), labelColor.getBlue(), attributeLabelInfoReference.get(i, 3), i);
        }
//        return gl -> {
//            attributeLabelInfo.set(attributeLabelInfoReference);
//        };
return null;
    }

    public GLRenderableUpdateTask setLabelSizes(final VisualAccess access) {
        final int numConnectionLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getConnectionAttributeLabelCount());
        for (int i = 0; i < numConnectionLabels; i++) {
            attributeLabelInfoReference.set(i, 3, (int) (LabelUtilities.NRADIUS_TO_LABEL_UNITS * Math.min(access.getConnectionLabelSize(i), LabelUtilities.MAX_LABEL_SIZE)));
        }
//        return gl -> {
//            attributeLabelInfo.set(attributeLabelInfoReference);
//        };
return null;
    }

    public GLRenderableUpdateTask setHighlightColor(final VisualAccess access) {
        final ConstellationColor color = access.getHighlightColor();
//        return gl -> {
//            highlightColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), 1};
//        };
return null;
    }

    public GLRenderableUpdateTask setBackgroundColor(final VisualAccess access) {
        final ConstellationColor color = access.getBackgroundColor();
//        return gl -> {
//            backgroundColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), 0.25f};
//        };
return null;
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
//        return gl -> {
//            attributeLabelBatch.dispose(gl);
//            summaryLabelBatch.dispose(gl);
//        };
        return null;
    }

    @Override
    public void drawBatch(/*final GL30 gl, */final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix) {
//
//        if (attributeLabelBatch.isDrawable() || summaryLabelBatch.isDrawable()) {
//            GL30.glUseProgram(shader);
//
//            // Let the glyph controller bind the glyph info and glyph image textures
//            SharedDrawable.updateGlyphTextureController(gl);
//            SharedDrawable.getGlyphTextureController().bind(gl, shaderGlyphInfoTexture, TextureUnits.GLYPH_INFO, shaderGlyphImageTexture, TextureUnits.GLYPHS);
//
//            // This is kind of weird - fix this to actually mean something?
//            final int further_f = 0;
//            final int further_u = 1;
//            GL30.glPolygonOffset(further_f, further_u);
//            GL30.glDepthFunc(STUB_GL.GL_LEQUAL);
//
//            // Uniform variables
//            GL30.glUniformMatrix4fv(shaderMVMatrix, false, mvMatrix.a);
//            GL30.glUniformMatrix4fv(shaderPMatrix, false, pMatrix.a);
//            GL30.glUniform1f(shaderLocWidth, SharedDrawable.getGlyphManager().getWidthScalingFactor());
//            GL30.glUniform1f(shaderLocHeight, SharedDrawable.getGlyphManager().getHeightScalingFactor());
//            GL30.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
//            GL30.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
//            GL30.glUniform1f(shaderMorphMix, camera.getMix());
//            GL30.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
//            GL30.glUniform1i(shaderBackgroundGlyphIndex, SharedDrawable.getLabelBackgroundGlyphPosition());
//            GL30.glUniform4fv(shaderBackgroundColor, backgroundColor);
//            GL30.glUniform4fv(shaderHighlightColor, highlightColor);
//
//            if (attributeLabelBatch.isDrawable()) {
//                GL30.glUniformMatrix4fv(shaderLabelInfo, false, attributeLabelInfo.a);
//                attributeLabelBatch.draw(gl);
//            }
//
//            if (summaryLabelBatch.isDrawable()) {
//                GL30.glUniformMatrix4fv(shaderLabelInfo, false, summaryLabelInfo.a);
//                summaryLabelBatch.draw(gl);
//            }
//
//        }
    }

}
