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

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.LabelUtilities;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author twilight_sparkle
 * @author antares
 */
public class LineBatcher implements SceneBatcher {

    private static final int LINE_INFO_ARROW = 1;
    private static final int LINE_INFO_BITS_AVOID = 4;
    private static final int FLOAT_MULTIPLIER = 1024;
    private static final int NEW_LINK = -345;
    private static final String COLOR_SHADER_NAME = "vColor";
    private static final String CONNECTION_INFO_SHADER_NAME = "data";

    private final Batch batch;
    private int lineShader;
    private int lineLineShader;

    private boolean drawForHitTest = false;

    // Uniform locations.
    private int lineShaderMVMatrix;
    private int lineShaderPMatrix;
    private int lineShaderLocDrawHitTest;
    private int lineShaderVisibilityLow;
    private int lineShaderVisibilityHigh;
    private int lineShaderMorphMix;
    private int lineShaderXyzTexture;
    private int lineShaderAlpha;
    private int lineShaderHighlightColor;
    private int lineShaderDirectionMotion;
    private int lineShaderGreyscale; // anaglyphic drawing

    private int lineLineShaderMVMatrix;
    private int lineLineShaderPMatrix;
    private int lineLineShaderLocDrawHitTest;
    private int lineLineShaderVisibilityLow;
    private int lineLineShaderVisibilityHigh;
    private int lineLineShaderMorphMix;
    private int lineLineShaderXyzTexture;
    private int lineLineShaderAlpha;
    private int lineLineShaderHighlightColor;
    private int lineLineShaderDirectionMotion;
    private int lineLineShaderGreyscale; // anaglyphic drawing

    private final int colorTarget;
    private final int connectionInfoTarget;

    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int CONNECTION_INFO_BUFFER_WIDTH = 4;

    public LineBatcher() {

        // Create the batch
        batch = new Batch(GL.GL_LINES);
        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
        connectionInfoTarget = batch.newIntBuffer(CONNECTION_INFO_BUFFER_WIDTH, false);
    }

    @Override
    public boolean batchReady() {
        return batch.isDrawable();
    }

    @Override
    public void createShader(GL3 gl) throws IOException {

        // Create the shader
        lineShader = SharedDrawable.getLineShader(gl, colorTarget, COLOR_SHADER_NAME, connectionInfoTarget, CONNECTION_INFO_SHADER_NAME);
        lineLineShader = SharedDrawable.getLineLineShader(gl, colorTarget, COLOR_SHADER_NAME, connectionInfoTarget, CONNECTION_INFO_SHADER_NAME);

        lineShaderMVMatrix = gl.glGetUniformLocation(lineShader, "mvMatrix");
        lineShaderPMatrix = gl.glGetUniformLocation(lineShader, "pMatrix");
        lineShaderLocDrawHitTest = gl.glGetUniformLocation(lineShader, "drawHitTest");
        lineShaderVisibilityLow = gl.glGetUniformLocation(lineShader, "visibilityLow");
        lineShaderVisibilityHigh = gl.glGetUniformLocation(lineShader, "visibilityHigh");
        lineShaderMorphMix = gl.glGetUniformLocation(lineShader, "morphMix");
        lineShaderXyzTexture = gl.glGetUniformLocation(lineShader, "xyzTexture");
        lineShaderAlpha = gl.glGetUniformLocation(lineShader, "alpha");
        lineShaderHighlightColor = gl.glGetUniformLocation(lineShader, "highlightColor");
        lineShaderDirectionMotion = gl.glGetUniformLocation(lineShader, "directionMotion");
        lineShaderGreyscale = gl.glGetUniformLocation(lineShader, "greyscale");

        lineLineShaderMVMatrix = gl.glGetUniformLocation(lineLineShader, "mvMatrix");
        lineLineShaderPMatrix = gl.glGetUniformLocation(lineLineShader, "pMatrix");
        lineLineShaderLocDrawHitTest = gl.glGetUniformLocation(lineLineShader, "drawHitTest");
        lineLineShaderVisibilityLow = gl.glGetUniformLocation(lineLineShader, "visibilityLow");
        lineLineShaderVisibilityHigh = gl.glGetUniformLocation(lineLineShader, "visibilityHigh");
        lineLineShaderMorphMix = gl.glGetUniformLocation(lineLineShader, "morphMix");
        lineLineShaderXyzTexture = gl.glGetUniformLocation(lineLineShader, "xyzTexture");
        lineLineShaderAlpha = gl.glGetUniformLocation(lineLineShader, "alpha");
        lineLineShaderHighlightColor = gl.glGetUniformLocation(lineLineShader, "highlightColor");
        lineLineShaderDirectionMotion = gl.glGetUniformLocation(lineLineShader, "directionMotion");
        lineLineShaderGreyscale = gl.glGetUniformLocation(lineLineShader, "greyscale");
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        connections.clear();
        connectionPosToBufferPos.clear();
        return gl -> batch.dispose(gl);
    }

    private final SortedMap<Integer, Integer> connectionPosToBufferPos = new TreeMap<>();
    private final List<Integer> connections = new ArrayList<>();

    @Override
    public GLRenderableUpdateTask createBatch(final VisualAccess access) {

        int lineCounter = 0;

        for (int link = 0; link < access.getLinkCount(); link++) {
            if (access.getLinkSource(link) != access.getLinkDestination(link)) {
                connections.add(NEW_LINK);
                for (int pos = 0; pos < access.getLinkConnectionCount(link); pos++) {
                    final int connection = access.getLinkConnection(link, pos);
                    connectionPosToBufferPos.put(connection, lineCounter++);
                    connections.add(connection);
                }
            }
        }

        final int numLines = lineCounter;
        FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(numLines * 2 * COLOR_BUFFER_WIDTH);
        IntBuffer dataBuffer = Buffers.newDirectIntBuffer(numLines * 2 * CONNECTION_INFO_BUFFER_WIDTH);
        connections.forEach(pos -> {
            if (pos == NEW_LINK) {
                leftOffset = 0;
            } else {
                bufferColorInfo(pos, colorBuffer, access);
                bufferConnectionInfo(pos, dataBuffer, access);
            }
        });
        colorBuffer.flip();
        dataBuffer.flip();

        return gl -> {
            if (numLines > 0) {
                batch.initialise(numLines * 2);
                batch.buffer(gl, colorTarget, colorBuffer);
                batch.buffer(gl, connectionInfoTarget, dataBuffer);
                batch.finalise(gl);
            }
        };
    }

    private float leftOffset;
    private float rightOffset;

    private int bufferConnectionInfo(final int pos, final IntBuffer dataBuffer, final VisualAccess access) {
        if (connectionPosToBufferPos.containsKey(pos)) {
            final float width = Math.min(LabelUtilities.MAX_TRANSACTION_WIDTH, access.getConnectionWidth(pos));
            final float offset;
            if (leftOffset == 0) {
                offset = 0;
                leftOffset += width / 2;
                rightOffset = leftOffset;
            } else if (leftOffset < rightOffset) {
                offset = -(leftOffset + width / 2 + 1);
                leftOffset += width + 1;
            } else {
                offset = rightOffset + width / 2 + 1;
                rightOffset += width + 1;
            }

            final int representativeTransactionId = access.getConnectionId(pos);
            final int lowVertex = access.getConnectionLowVertex(pos);
            final int highVertex = access.getConnectionHighVertex(pos);
            final int flags = (access.getConnectionDimmed(pos) ? 2 : 0) | (access.getConnectionSelected(pos) ? 1 : 0);
            final int lineStyle = access.getConnectionLineStyle(pos).ordinal();
            final ConnectionDirection connectionDirection = access.getConnectionDirection(pos);

            dataBuffer.put(representativeTransactionId);
            dataBuffer.put(lowVertex * LINE_INFO_BITS_AVOID + ((connectionDirection == ConnectionDirection.LOW_TO_HIGH || connectionDirection == ConnectionDirection.BIDIRECTED) ? LINE_INFO_ARROW : 0));
            dataBuffer.put(flags);
            dataBuffer.put((int) (offset * FLOAT_MULTIPLIER));
            dataBuffer.put(representativeTransactionId);
            dataBuffer.put(highVertex * LINE_INFO_BITS_AVOID + ((connectionDirection == ConnectionDirection.HIGH_TO_LOW || connectionDirection == ConnectionDirection.BIDIRECTED) ? LINE_INFO_ARROW : 0));
            dataBuffer.put(flags);
            dataBuffer.put(((int) (width * FLOAT_MULTIPLIER)) << 2 | lineStyle);
            return connectionPosToBufferPos.get(pos);
        }
        return -1;
    }

    private int updateConnectionInfo(final int pos, final IntBuffer dataBuffer, final VisualAccess access) {
        if (connectionPosToBufferPos.containsKey(pos)) {
            final float width = Math.min(LabelUtilities.MAX_TRANSACTION_WIDTH, access.getConnectionWidth(pos));
            final int representativeTransactionId = access.getConnectionId(pos);
            final int lowVertex = access.getConnectionLowVertex(pos);
            final int highVertex = access.getConnectionHighVertex(pos);
            final int flags = (access.getConnectionDimmed(pos) ? 2 : 0) | (access.getConnectionSelected(pos) ? 1 : 0);
            final int lineStyle = access.getConnectionLineStyle(pos).ordinal();
            final ConnectionDirection connectionDirection;
            if (access.getConnectionDirected(pos)) {
                if (access.getConnectionDirection(pos) != ConnectionDirection.UNDIRECTED) { // covers the case where the transaction was initially undirected
                    connectionDirection = access.getConnectionDirection(pos);
                } else {
                    connectionDirection = ConnectionDirection.LOW_TO_HIGH;
                }
            } else {
                connectionDirection = ConnectionDirection.UNDIRECTED; // undirected transactions shouldn't have an arrow
            }

            dataBuffer.put(representativeTransactionId);
            dataBuffer.put(lowVertex * LINE_INFO_BITS_AVOID + ((connectionDirection == ConnectionDirection.LOW_TO_HIGH || connectionDirection == ConnectionDirection.BIDIRECTED) ? LINE_INFO_ARROW : 0));
            dataBuffer.put(flags);
            dataBuffer.put(representativeTransactionId);
            dataBuffer.put(highVertex * LINE_INFO_BITS_AVOID + ((connectionDirection == ConnectionDirection.HIGH_TO_LOW || connectionDirection == ConnectionDirection.BIDIRECTED) ? LINE_INFO_ARROW : 0));
            dataBuffer.put(flags);
            dataBuffer.put(((int) (width * FLOAT_MULTIPLIER)) << 2 | lineStyle);
            return connectionPosToBufferPos.get(pos);
        }
        return -1;
    }

    private int bufferColorInfo(final int pos, final FloatBuffer colorBuffer, final VisualAccess access) {
        if (connectionPosToBufferPos.containsKey(pos)) {
            final ConstellationColor color = access.getConnectionColor(pos);
            colorBuffer.put(color.getRed());
            colorBuffer.put(color.getGreen());
            colorBuffer.put(color.getBlue());
            colorBuffer.put(access.getConnectionVisibility(pos));
            colorBuffer.put(color.getRed());
            colorBuffer.put(color.getGreen());
            colorBuffer.put(color.getBlue());
            colorBuffer.put(access.getConnectionVisibility(pos));
            return connectionPosToBufferPos.get(pos);
        }
        return -1;
    }

    public GLRenderableUpdateTask updateInfo(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateIntBufferTask(change, access, this::updateConnectionInfo, gl -> batch.connectIntBuffer(gl, connectionInfoTarget),
                 gl -> batch.disconnectBuffer(gl, connectionInfoTarget),
                 new boolean[]{true, true, true, false, true, true, true, true});
    }

    public GLRenderableUpdateTask updateColors(final VisualAccess access, final VisualChange change) {
        return SceneBatcher.updateFloatBufferTask(change, access, this::bufferColorInfo, gl -> batch.connectFloatBuffer(gl, colorTarget),
                 gl -> batch.disconnectBuffer(gl, colorTarget),
                 COLOR_BUFFER_WIDTH * 2);
    }

    public GLRenderableUpdateTask updateOpacity(final VisualAccess access) {
        final float updatedOpacity = access.getConnectionOpacity();
        return gl -> opacity = updatedOpacity;
    }

    public void setNextDrawIsHitTest() {
        this.drawForHitTest = true;
    }

    private float opacity;
    private float motion;
    private float[] highlightColor;

    public void setMotion(final float motion) {
        this.motion = motion;
    }

    public GLRenderableUpdateTask setHighlightColor(final VisualAccess access) {
        final ConstellationColor color = access.getHighlightColor();
        return gl -> this.highlightColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), 1};
    }

    @Override
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale) {

        if (batch.isDrawable()) {
            // Uniform variables
            gl.glUseProgram(lineShader);
            if (drawForHitTest) {
                gl.glUniform1i(lineShaderLocDrawHitTest, GL.GL_TRUE);
                gl.glUniform1f(lineShaderDirectionMotion, -1);
            } else {
                gl.glUniform1i(lineShaderLocDrawHitTest, GL.GL_FALSE);
                gl.glUniform1f(lineShaderDirectionMotion, motion);
            }
            gl.glUniformMatrix4fv(lineShaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(lineShaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(lineShaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(lineShaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(lineShaderMorphMix, camera.getMix());
            gl.glUniform1i(lineShaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1f(lineShaderAlpha, opacity);
            gl.glUniform4fv(lineShaderHighlightColor, 1, highlightColor, 0);
            gl.glUniform1i(lineShaderGreyscale, greyscale ? 1 : 0);
            batch.draw(gl);

            gl.glUseProgram(lineLineShader);
            if (drawForHitTest) {
                gl.glUniform1i(lineLineShaderLocDrawHitTest, GL.GL_TRUE);
                gl.glUniform1f(lineLineShaderDirectionMotion, -1);
            } else {
                gl.glUniform1i(lineLineShaderLocDrawHitTest, GL.GL_FALSE);
                gl.glUniform1f(lineLineShaderDirectionMotion, motion);
            }
            gl.glUniformMatrix4fv(lineLineShaderMVMatrix, 1, false, mvMatrix.a, 0);
            gl.glUniformMatrix4fv(lineLineShaderPMatrix, 1, false, pMatrix.a, 0);
            gl.glUniform1f(lineLineShaderVisibilityLow, camera.getVisibilityLow());
            gl.glUniform1f(lineLineShaderVisibilityHigh, camera.getVisibilityHigh());
            gl.glUniform1f(lineLineShaderMorphMix, camera.getMix());
            gl.glUniform1i(lineLineShaderXyzTexture, TextureUnits.VERTICES);
            gl.glUniform1f(lineLineShaderAlpha, opacity);
            gl.glUniform4fv(lineLineShaderHighlightColor, 1, highlightColor, 0);
            gl.glUniform1i(lineLineShaderGreyscale, greyscale ? 1 : 0);
            batch.draw(gl);
        }
        drawForHitTest = false;
    }
}
