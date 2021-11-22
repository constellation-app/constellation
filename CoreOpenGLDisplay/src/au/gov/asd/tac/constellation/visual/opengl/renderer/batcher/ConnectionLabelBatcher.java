/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.LabelUtilities;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.ConnectionGlyphStream;
import au.gov.asd.tac.constellation.visual.opengl.utilities.glyphs.ConnectionGlyphStreamContext;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 *
 * @author twilight_sparkle
 */
public class ConnectionLabelBatcher implements SceneBatcher {

    // Shader variable names corresponding to data in the batch
    private static final String LABEL_FLOATS_SHADER_NAME = "glyphLocationData";
    private static final String LABEL_INTS_SHADER_NAME = "graphLocationData";
    public static final int MAX_STAGGERS = 7;

    // Batch and shader
    private final Batch labelBatch;
    private int shader;

    // Information which is constant across the graph
    private final Matrix44f attributeLabelInfo = new Matrix44f();
    private final Matrix44f attributeLabelInfoReference = new Matrix44f();
    private final Matrix44f summaryLabelInfo = new Matrix44f();
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
    private int shaderGreyscale; // anaglyphic drawing

    private final int floatsTarget;
    private final int intsTarget;

    private static final int FLOAT_BUFFER_WIDTH = 4;
    private static final int INT_BUFFER_WIDTH = 4;

    public ConnectionLabelBatcher() {
        // Create the batch
        labelBatch = new Batch(GL.GL_POINTS);
        final ConstellationColor summaryColor = VisualGraphDefaults.DEFAULT_LABEL_COLOR;
        summaryLabelInfo.setRow(summaryColor.getRed(), summaryColor.getGreen(), summaryColor.getBlue(), VisualGraphDefaults.DEFAULT_LABEL_SIZE * LabelUtilities.NRADIUS_TO_LABEL_UNITS, 0);
        floatsTarget = labelBatch.newFloatBuffer(FLOAT_BUFFER_WIDTH, false);
        intsTarget = labelBatch.newIntBuffer(INT_BUFFER_WIDTH, false);
    }

    public void setCurrentConnection(final int lowNodeId, final int highNodeId, final int linkLabelCount, ConnectionGlyphStreamContext context) {
        context.currentStagger = 0;
        context.currentOffset = 0;
        context.nextLeftOffset = 0;
        context.nextRightOffset = 0;
        context.currentLowNodeId = lowNodeId;
        context.currentHighNodeId = highNodeId;
        context.currentLinkLabelCount = linkLabelCount;
    }

    public void nextParallelConnection(final int width, ConnectionGlyphStreamContext context) {
        context.currentStagger = context.currentStagger == MAX_STAGGERS ? 1 : context.currentStagger + 1;
        if (context.nextLeftOffset == 0) {
            context.nextLeftOffset += ((width / 2) + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
            context.nextRightOffset -= ((width / 2) + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
        } else if (context.nextLeftOffset <= -context.nextRightOffset) {
            context.currentOffset = context.nextLeftOffset + (width / 2);
            context.nextLeftOffset += (width + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
        } else {
            context.currentOffset = context.nextRightOffset - (width / 2);
            context.nextRightOffset -= (width + LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS);
        }
    }

    @Override
    public boolean batchReady() {
        return labelBatch.isDrawable();
    }

    @Override
    public void createShader(GL3 gl) throws IOException {
        // Create the shader
        shader = SharedDrawable.getConnectionLabelShader(gl, floatsTarget, LABEL_FLOATS_SHADER_NAME, intsTarget, LABEL_INTS_SHADER_NAME);

        // Set up uniform locations in the shader
        shaderMVMatrix = gl.glGetUniformLocation(shader, "mvMatrix");
        shaderPMatrix = gl.glGetUniformLocation(shader, "pMatrix");
        shaderLabelInfo = gl.glGetUniformLocation(shader, "labelInfo");
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
        shaderGreyscale = gl.glGetUniformLocation(shader, "greyscale");
    }

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) throws InterruptedException {
        final ConnectionGlyphStream glyphStream = new ConnectionGlyphStream();
        fillLabels(access, glyphStream);

        return gl -> {
            labelBatch.initialise(glyphStream.getCurrentFloats().size() / FLOAT_BUFFER_WIDTH);
            labelBatch.buffer(gl, intsTarget, IntBuffer.wrap(glyphStream.getCurrentInts().rawArray()));
            labelBatch.buffer(gl, floatsTarget, FloatBuffer.wrap(glyphStream.getCurrentFloats().rawArray()));
            labelBatch.finalise(gl);
        };
    }

    public GLRenderableUpdateTask updateLabels(final VisualAccess access) throws InterruptedException {
        // We build the whole batch again - can't update labels in place at this stage.
        final ConnectionGlyphStream glyphStream = new ConnectionGlyphStream();
        fillLabels(access, glyphStream);

        return gl -> {
            labelBatch.dispose(gl);
            labelBatch.initialise(glyphStream.getCurrentFloats().size() / FLOAT_BUFFER_WIDTH);
            labelBatch.buffer(gl, intsTarget, IntBuffer.wrap(glyphStream.getCurrentInts().rawArray()));
            labelBatch.buffer(gl, floatsTarget, FloatBuffer.wrap(glyphStream.getCurrentFloats().rawArray()));
            labelBatch.finalise(gl);
        };
    }

    private void fillLabels(final VisualAccess access, ConnectionGlyphStream glyphStream) throws InterruptedException {
        final ConnectionGlyphStreamContext context = new ConnectionGlyphStreamContext();

        for (int link = 0; link < access.getLinkCount(); link++) {
            final int connectionCount = access.getLinkConnectionCount(link);
            setCurrentConnection(access.getLinkLowVertex(link), access.getLinkHighVertex(link), connectionCount, context);
            for (int pos = 0; pos < connectionCount; pos++) {
                final int connection = access.getLinkConnection(link, pos);
                nextParallelConnection((int) (LabelUtilities.NRADIUS_TO_LINE_WIDTH_UNITS * Math.min(LabelUtilities.MAX_TRANSACTION_WIDTH, access.getConnectionWidth(connection))), context);
                final Matrix44f currentLabelInfo = access.isLabelSummary(connection) ? summaryLabelInfo : attributeLabelInfoReference;
                bufferLabel(connection, access, glyphStream, currentLabelInfo, context);
            }

        }

        glyphStream.trimToSize();
    }

    private void bufferLabel(final int pos, final VisualAccess access, final ConnectionGlyphStream glyphStream, Matrix44f currentLabelInfo, final ConnectionGlyphStreamContext context) {
        int totalScale = 0;
        context.visibility = access.getConnectionVisibility(pos);
        for (int label = 0; label < access.getConnectionLabelCount(pos); label++) {
            context.labelNumber = label;
            final String text = access.getConnectionLabelText(pos, label);
            ArrayList<String> lines = LabelUtilities.splitTextIntoLines(text);
            for (final String line : lines) {
                context.totalScale = totalScale;
                SharedDrawable.getGlyphManager().renderTextAsLigatures(line, glyphStream, context);
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
        return gl -> {
            attributeLabelInfo.set(attributeLabelInfoReference);
        };
    }

    public GLRenderableUpdateTask setLabelSizes(final VisualAccess access) {
        final int numConnectionLabels = Math.min(LabelUtilities.MAX_LABELS_TO_DRAW, access.getConnectionAttributeLabelCount());
        for (int i = 0; i < numConnectionLabels; i++) {
            attributeLabelInfoReference.set(i, 3, (int) (LabelUtilities.NRADIUS_TO_LABEL_UNITS * Math.min(access.getConnectionLabelSize(i), LabelUtilities.MAX_LABEL_SIZE)));
        }
        return gl -> {
            attributeLabelInfo.set(attributeLabelInfoReference);
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
            labelBatch.dispose(gl);
        };
    }

    @Override
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale) {
        if (labelBatch.isDrawable()) {
            gl.glUseProgram(shader);

            // Let the glyph controller bind the glyph info and glyph image textures
            SharedDrawable.updateGlyphTextureController(gl);
            SharedDrawable.getGlyphTextureController().bind(gl, shaderGlyphInfoTexture, TextureUnits.GLYPH_INFO, shaderGlyphImageTexture, TextureUnits.GLYPHS);

            // This is kind of weird - fix this to actually mean something?
            final int further_f = 0;
            final int further_u = 1;
            gl.glPolygonOffset(further_f, further_u);
            gl.glDepthFunc(GL.GL_LEQUAL);

            // Uniform variables
            gl.glUniformMatrix4fv(shaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(shaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(shaderLocWidth, SharedDrawable.getGlyphManager().getWidthScalingFactor());
            gl.glUniform1f(shaderLocHeight, SharedDrawable.getGlyphManager().getHeightScalingFactor());
            gl.glUniform1f(shaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(shaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(shaderMorphMix, camera.getMix());
            gl.glUniform1i(shaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1i(shaderBackgroundGlyphIndex, SharedDrawable.getLabelBackgroundGlyphPosition());
            gl.glUniform4fv(shaderBackgroundColor, 1, backgroundColor, 0);
            gl.glUniform4fv(shaderHighlightColor, 1, highlightColor, 0);
            gl.glUniform1i(shaderGreyscale, greyscale ? 1 : 0);

            if (labelBatch.isDrawable()) {
                gl.glUniformMatrix4fv(shaderLabelInfo, 1, false, attributeLabelInfo.a, 0);
                labelBatch.draw(gl);
            }

        }
    }

}
