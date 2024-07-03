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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.Batch;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.ShaderManager;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.awt.Point;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Draw a selection rectangle.
 *
 * @author algol
 */
public class SelectionBoxRenderable implements GLRenderable {
    
    private static final Logger LOGGER = Logger.getLogger(SelectionBoxRenderable.class.getName());

    // How many vertices do we need to draw a rectangle?
    // What color is the selection rectangle?
    private static final Vector4f SELECTION_COLOR = new Vector4f(0, 0.5F, 1, 0.375F);
    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    private int shader;
    private int shaderMvp;
    private final Batch batch;
    private SelectionBoxModel selectionBoxModel;
    private final BlockingDeque<SelectionBoxModel> modelQueue = new LinkedBlockingDeque<>();

    private static final int NUMBER_OF_VERTICES = 4;
    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int VERTEX_BUFFER_WIDTH = 3;

    private final int colorTarget;
    private final int vertexTarget;

    public SelectionBoxRenderable() {
        batch = new Batch(GL.GL_TRIANGLE_FAN);
        vertexTarget = batch.newFloatBuffer(VERTEX_BUFFER_WIDTH, true);
        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, true);
    }

    @Override
    public int getPriority() {
        return RenderablePriority.ANNOTATIONS_PRIORITY.getValue();
    }

    @Override
    public void init(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        String vs = null;
        String gs = null;
        String fs = null;

        try {
            vs = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThru.vs");
            gs = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThruTriangle.gs");
            fs = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThru.fs");
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        shader = GLTools.loadShaderSourceWithAttributes(gl, "PassThru selection", vs, gs, fs,
                vertexTarget, "vertex",
                colorTarget, "color",
                ShaderManager.FRAG_BASE, "fragColor");
        shaderMvp = gl.glGetUniformLocation(shader, "mvpMatrix");

        batch.initialise(NUMBER_OF_VERTICES);
        for (int i = 0; i < NUMBER_OF_VERTICES; i++) {
            batch.stage(vertexTarget, ZERO_3F);
            batch.stage(colorTarget, SELECTION_COLOR);
        }
        batch.finalise(gl);
    }

    public void setSelectionBoxModel(final SelectionBoxModel selectionBoxModel) {
        this.selectionBoxModel = selectionBoxModel;
    }

    public void queueModel(final SelectionBoxModel model) {
        modelQueue.add(model);
    }

    private int width;
    private int height;

    @Override
    public void reshape(final int x, final int y, final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(final GLAutoDrawable drawable) {
        if (CollectionUtils.isNotEmpty(modelQueue)) {
            selectionBoxModel = modelQueue.getLast();
            modelQueue.clear();
        }
    }

    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f pMatrix) {
        if (selectionBoxModel != null && selectionBoxModel.isClear()) {
            final Point begin = selectionBoxModel.getStartPoint();
            final Point end = selectionBoxModel.getEndPoint();
            final GL3 gl = drawable.getGL().getGL3();

            // Map the vertex buffer.
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, batch.getBufferName(vertexTarget));
            final ByteBuffer bbuf = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
            final FloatBuffer fbuf = bbuf.asFloatBuffer();

            // We should have the same buffer size we started with.
            assert fbuf.limit() == NUMBER_OF_VERTICES * 3;

            // Find the location of the box in projected coordinates
            float left = ((float) begin.x / width) * 2 - 1F;
            float right = ((float) end.x / width) * 2 - 1F;
            float top = ((float) (height - begin.y) / height) * 2 - 1F;
            float bottom = ((float) (height - end.y) / height) * 2 - 1F;

            // Update the four TRIANGLE_FAN coordinates in the vertex buffer.
            float[] v = new float[]{
                right, bottom, 0F,
                left, bottom, 0F,
                left, top, 0F,
                right, top, 0F
            };

            fbuf.put(v);

            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);

            // Disable depth so the rectangle is drawn over everything else.
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            Matrix44f mvpMatrix = new Matrix44f();
            mvpMatrix.makeIdentity();

            // Draw.
            gl.glUseProgram(shader);
            gl.glUniformMatrix4fv(shaderMvp, 1, false, mvpMatrix.a, 0);
            batch.draw(gl);

            // Reenable depth.
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(true);
        }
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        batch.dispose(gl);
    }
}
