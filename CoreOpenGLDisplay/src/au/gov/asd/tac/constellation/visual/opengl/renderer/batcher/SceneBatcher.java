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
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable.GLRenderableUpdateTask;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * An interface for classes that coordinate the creation, updating and drawing
 * of the data in one or more {@link Batch} objects, along with the associated
 * shader and uniform data required to do so.
 * <p>
 * This class also contains a number of static utility methods to perform common
 * update tasks for buffers associated with a {@link Batch}.
 *
 * @author twilight_sparkle
 */
public interface SceneBatcher {

    /**
     * Whether or not this SceneBatcher is currently active on a GL context,
     * allowing it to be drawn, or have its buffers updated.
     *
     * @return
     */
    public boolean batchReady();

    /**
     * Draws the current {@link Batch} held by this Batcher onto the given
     * drawable based on the given state information about the graph.
     *
     * NOTE: This method should only draw a batch containing valid GL data. This
     * can be enforced by checking it against null, and always setting the
     * current batch to null inside disposeBatch.
     *
     * @param gl the OpenGL context.
     * @param camera the current camera.
     * @param mvMatrix the model-view matrix.
     * @param pMatrix the projection matrix.
     * @param greyscale If true, the graph will be drawn in greyscale for anaglyphic display.
     */
    public void drawBatch(final GL3 gl, final Camera camera, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale);

    /**
     * Create the shader with which to draw this SceneBatcher's {@link Batch}.
     *
     * @param gl the current OpenGL context.
     * @throws IOException if an IO error occurs.
     */
    public void createShader(final GL3 gl) throws IOException;

    /**
     * Create a task to dispose of any held batches from a given GL context.
     *
     * This should call {@link Batch#dispose Batch.dispose()} and then set to
     * null the current batch. The batch should not be drawn until a subsequent
     * call to {@link #createBatch createBatch()} is made.
     *
     * @return The GLUpdateTask to dispose this batch
     */
    public GLRenderableUpdateTask disposeBatch();

    /**
     * Create a task to create the data relevant to this SceneBatcher based on a
     * given {@link VisualAccess} and set up the created data on a given GL
     * context so that it can then subsequently be drawn.
     *
     * @param access the VisualAccess object describing the data to be
     * visualised.
     * @return The GLUpdateTask to create this batch.
     * @throws java.lang.InterruptedException
     */
    public GLRenderableUpdateTask createBatch(final VisualAccess access) throws InterruptedException;

    @FunctionalInterface
    public static interface IntBufferOperation {

        int buffer(final int pos, final IntBuffer buffer, final VisualAccess access);
    }

    @FunctionalInterface
    public static interface FloatBufferOperation {

        int buffer(final int pos, final FloatBuffer buffer, final VisualAccess access);
    }

    @FunctionalInterface
    public static interface ByteBufferOperation {

        int buffer(final int pos, final ByteBuffer buffer, final VisualAccess access);
    }

    @FunctionalInterface
    public static interface ByteBufferConnection {

        ByteBuffer connect(final GL3 gl);
    }

    @FunctionalInterface
    public static interface IntBufferConnection {

        IntBuffer connect(final GL3 gl);
    }

    @FunctionalInterface
    public static interface FloatBufferConnection {

        FloatBuffer connect(final GL3 gl);
    }

    @FunctionalInterface
    public static interface BufferDisconnection {

        void disconnect(final GL3 gl);
    }

    public static GLRenderableUpdateTask updateIntBufferTask(final VisualChange change, final VisualAccess access, final IntBufferOperation operation, final IntBufferConnection connector, final BufferDisconnection disconnector, final int width) {
        final boolean[] updateMask = new boolean[width];
        Arrays.fill(updateMask, true);
        return updateIntBufferTask(change, access, operation, connector, disconnector, updateMask);
    }

    public static GLRenderableUpdateTask updateIntBufferTask(final VisualChange change, final VisualAccess access, final IntBufferOperation operation, final IntBufferConnection connector, final BufferDisconnection disconnector, final boolean[] updateMask) {
        final int width = updateMask.length;
        final int numChanges = change.getSize();
        final IntBuffer updateBuffer = Buffers.newDirectIntBuffer(width * numChanges);
        final int[] bufferUpdatePositions = new int[numChanges];
        int updatePos = 0;
        for (int i = 0; i < numChanges; i++) {
            int pos = change.getElement(i);
            final int updatedPosition = operation.buffer(pos, updateBuffer, access);
            if (updatedPosition >= 0) {
                bufferUpdatePositions[updatePos++] = updatedPosition;
            }
        }
        final int numUpdates = updatePos;
        updateBuffer.flip();
        return gl -> {
            final IntBuffer buffer = connector.connect(gl);
            for (int i = 0; i < numUpdates; i++) {
                buffer.position(bufferUpdatePositions[i] * width);
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

    public static GLRenderableUpdateTask updateFloatBufferTask(final VisualChange change, final VisualAccess access, final FloatBufferOperation operation, final FloatBufferConnection connector, final BufferDisconnection disconnector, final int width) {
        final boolean[] updateMask = new boolean[width];
        Arrays.fill(updateMask, true);
        return updateFloatBufferTask(change, access, operation, connector, disconnector, updateMask);
    }

    public static GLRenderableUpdateTask updateFloatBufferTask(final VisualChange change, final VisualAccess access, final FloatBufferOperation operation, final FloatBufferConnection connector, final BufferDisconnection disconnector, final boolean[] updateMask) {
        final int width = updateMask.length;
        final int numChanges = change.getSize();
        final FloatBuffer updateBuffer = Buffers.newDirectFloatBuffer(width * numChanges);
        final int[] bufferUpdatePositions = new int[numChanges];
        int updatePos = 0;
        for (int i = 0; i < numChanges; i++) {
            int pos = change.getElement(i);
            final int updatedPosition = operation.buffer(pos, updateBuffer, access);
            if (updatedPosition >= 0) {
                bufferUpdatePositions[updatePos++] = updatedPosition;
            }
        }
        final int numUpdates = updatePos;
        updateBuffer.flip();
        return gl -> {
            final FloatBuffer buffer = connector.connect(gl);
            for (int i = 0; i < numUpdates; i++) {
                buffer.position(bufferUpdatePositions[i] * width);
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

    public static GLRenderableUpdateTask updateByteBufferTask(final VisualChange change, final VisualAccess access, final ByteBufferOperation operation, final ByteBufferConnection connector, final BufferDisconnection disconnector, final int width) {
        final boolean[] updateMask = new boolean[width];
        Arrays.fill(updateMask, true);
        return updateByteBufferTask(change, access, operation, connector, disconnector, updateMask);
    }

    public static GLRenderableUpdateTask updateByteBufferTask(final VisualChange change, final VisualAccess access, final ByteBufferOperation operation, final ByteBufferConnection connector, final BufferDisconnection disconnector, final boolean[] updateMask) {
        final int width = updateMask.length;
        final int numChanges = change.getSize();
        final ByteBuffer updateBuffer = Buffers.newDirectByteBuffer(width * numChanges);
        final int[] bufferUpdatePositions = new int[numChanges];
        int updatePos = 0;
        for (int i = 0; i < numChanges; i++) {
            int pos = change.getElement(i);
            final int updatedPosition = operation.buffer(pos, updateBuffer, access);
            if (updatedPosition >= 0) {
                bufferUpdatePositions[updatePos++] = updatedPosition;
            }
        }
        final int numUpdates = updatePos;
        updateBuffer.flip();
        return gl -> {
            final ByteBuffer buffer = connector.connect(gl);
            for (int i = 0; i < numUpdates; i++) {
                buffer.position(bufferUpdatePositions[i] * width);
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
}
