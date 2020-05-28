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
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.utilities.LabelUtilities;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lwjgl.BufferUtils;

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

//    private final Batch batch;
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

//    private final int colorTarget;
//    private final int connectionInfoTarget;

    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int CONNECTION_INFO_BUFFER_WIDTH = 4;

    public LineBatcher() {

        // Create the batch
//        batch = new Batch(GL30.GL_LINES);
//        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, false);
//        connectionInfoTarget = batch.newIntBuffer(CONNECTION_INFO_BUFFER_WIDTH, false);
    }

    @Override
    public boolean batchReady() {
        return false;//return batch.isDrawable();
    }

    @Override
    public void createShader(/*GL30 gl*/) throws IOException {

        // Create the shader
//        lineShader = SharedDrawable.getLineShader(gl, colorTarget, COLOR_SHADER_NAME, connectionInfoTarget, CONNECTION_INFO_SHADER_NAME);
//        lineLineShader = SharedDrawable.getLineLineShader(gl, colorTarget, COLOR_SHADER_NAME, connectionInfoTarget, CONNECTION_INFO_SHADER_NAME);
//
//        lineShaderMVMatrix = GL30.glGetUniformLocation(lineShader, "mvMatrix");
//        lineShaderPMatrix = GL30.glGetUniformLocation(lineShader, "pMatrix");
//        lineShaderLocDrawHitTest = GL30.glGetUniformLocation(lineShader, "drawHitTest");
//        lineShaderVisibilityLow = GL30.glGetUniformLocation(lineShader, "visibilityLow");
//        lineShaderVisibilityHigh = GL30.glGetUniformLocation(lineShader, "visibilityHigh");
//        lineShaderMorphMix = GL30.glGetUniformLocation(lineShader, "morphMix");
//        lineShaderXyzTexture = GL30.glGetUniformLocation(lineShader, "xyzTexture");
//        lineShaderAlpha = GL30.glGetUniformLocation(lineShader, "alpha");
//        lineShaderHighlightColor = GL30.glGetUniformLocation(lineShader, "highlightColor");
//        lineShaderDirectionMotion = GL30.glGetUniformLocation(lineShader, "directionMotion");
//
//        lineLineShaderMVMatrix = GL30.glGetUniformLocation(lineLineShader, "mvMatrix");
//        lineLineShaderPMatrix = GL30.glGetUniformLocation(lineLineShader, "pMatrix");
//        lineLineShaderLocDrawHitTest = GL30.glGetUniformLocation(lineLineShader, "drawHitTest");
//        lineLineShaderVisibilityLow = GL30.glGetUniformLocation(lineLineShader, "visibilityLow");
//        lineLineShaderVisibilityHigh = GL30.glGetUniformLocation(lineLineShader, "visibilityHigh");
//        lineLineShaderMorphMix = GL30.glGetUniformLocation(lineLineShader, "morphMix");
//        lineLineShaderXyzTexture = GL30.glGetUniformLocation(lineLineShader, "xyzTexture");
//        lineLineShaderAlpha = GL30.glGetUniformLocation(lineLineShader, "alpha");
//        lineLineShaderHighlightColor = GL30.glGetUniformLocation(lineLineShader, "highlightColor");
//        lineLineShaderDirectionMotion = GL30.glGetUniformLocation(lineLineShader, "directionMotion");
    }

    @Override
    public GLRenderableUpdateTask disposeBatch() {
        connections.clear();
        connectionPosToBufferPos.clear();
//        return gl -> {
//            batch.dispose(gl);
//        };
        return null;
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
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(numLines * 2 * COLOR_BUFFER_WIDTH);
        IntBuffer dataBuffer = BufferUtils.createIntBuffer(numLines * 2 * CONNECTION_INFO_BUFFER_WIDTH);
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

//        return gl -> {
//            if (numLines > 0) {
//                batch.initialise(numLines * 2);
//                batch.buffer(gl, colorTarget, colorBuffer);
//                batch.buffer(gl, connectionInfoTarget, dataBuffer);
//                batch.finalise(gl);
//            }
//        };
        return null;
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
//        return SceneBatcher.updateIntBufferTask(change, access, this::updateConnectionInfo, gl -> {
//            return batch.connectIntBuffer(gl, connectionInfoTarget);
//        }, gl -> {
//            batch.disconnectBuffer(gl, connectionInfoTarget);
//        }, new boolean[]{true, true, true, false, true, true, true, true});
return null;
    }

    public GLRenderableUpdateTask updateColors(final VisualAccess access, final VisualChange change) {
//        return SceneBatcher.updateFloatBufferTask(change, access, this::bufferColorInfo, gl -> {
//            return batch.connectFloatBuffer(gl, colorTarget);
//        }, gl -> {
//            batch.disconnectBuffer(gl, colorTarget);
//        }, COLOR_BUFFER_WIDTH * 2);
return null;
    }

    public GLRenderableUpdateTask updateOpacity(final VisualAccess access) {
//        final float updatedOpacity = access.getConnectionOpacity();
//        return gl -> {
//            opacity = updatedOpacity;
//        };
return null;
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
//        return gl -> {
//            this.highlightColor = new float[]{color.getRed(), color.getGreen(), color.getBlue(), 1};
//        };
return null;
    }

    @Override
    public void drawBatch(/*final GL30 gl, */final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix) {
//
//        if (batch.isDrawable()) {
//            // Uniform variables
//            GL30.glUseProgram(lineShader);
//            if (drawForHitTest) {
//                GL30.glUniform1i(lineShaderLocDrawHitTest, GL30.GL_TRUE);
//                GL30.glUniform1f(lineShaderDirectionMotion, -1);
//            } else {
//                GL30.glUniform1i(lineShaderLocDrawHitTest, GL30.GL_FALSE);
//                GL30.glUniform1f(lineShaderDirectionMotion, motion);
//            }
//            GL30.glUniformMatrix4fv(lineShaderMVMatrix, false, mvMatrix.a);
//            GL30.glUniformMatrix4fv(lineShaderPMatrix, false, pMatrix.a);
//            GL30.glUniform1f(lineShaderVisibilityLow, camera.getVisibilityLow());
//            GL30.glUniform1f(lineShaderVisibilityHigh, camera.getVisibilityHigh());
//            GL30.glUniform1f(lineShaderMorphMix, camera.getMix());
//            GL30.glUniform1i(lineShaderXyzTexture, TextureUnits.VERTICES);
//            GL30.glUniform1f(lineShaderAlpha, opacity);
//            GL30.glUniform4fv(lineShaderHighlightColor, highlightColor);
//            batch.draw(gl);
//
//            GL30.glUseProgram(lineLineShader);
//            if (drawForHitTest) {
//                GL30.glUniform1i(lineLineShaderLocDrawHitTest, GL30.GL_TRUE);
//                GL30.glUniform1f(lineLineShaderDirectionMotion, -1);
//            } else {
//                GL30.glUniform1i(lineLineShaderLocDrawHitTest, GL30.GL_FALSE);
//                GL30.glUniform1f(lineLineShaderDirectionMotion, motion);
//            }
//
//            GL30.glUniformMatrix4fv(lineLineShaderMVMatrix, false, mvMatrix.a);
//            GL30.glUniformMatrix4fv(lineLineShaderPMatrix, false, pMatrix.a);
//            GL30.glUniform1f(lineLineShaderVisibilityLow, camera.getVisibilityLow());
//            GL30.glUniform1f(lineLineShaderVisibilityHigh, camera.getVisibilityHigh());
//            GL30.glUniform1f(lineLineShaderMorphMix, camera.getMix());
//            GL30.glUniform1i(lineLineShaderXyzTexture, TextureUnits.VERTICES);
//            GL30.glUniform1f(lineLineShaderAlpha, opacity);
//            GL30.glUniform4fv(lineLineShaderHighlightColor, highlightColor);
//            batch.draw(gl);
//        }
        drawForHitTest = false;
    }
}
