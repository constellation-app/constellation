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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import static au.gov.asd.tac.constellation.graph.interaction.visual.renderables.NewLineRenderable.NEW_LINE_COLOR;
import static au.gov.asd.tac.constellation.graph.interaction.visual.renderables.NewLineRenderable.NEW_LINE_WIDTH;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encapsulates the JOGL code required to draw a new line over the main scene
 * when the user is creating a transaction by dragging the mouse between
 * vertices.
 * <p>
 * The line representing the new transaction always begins at the vertex the
 * user selected when initiating the drag. If the mouse is hovering over empty
 * space, the line is drawn between the source vertex and the mouse position.
 * However, if a hit is registered on another vertex the finalise of the line
 * should 'snap' to the coordinates of that vertex.
 */
public class NewLineRenderable implements GLRenderable {
    
    private static final Logger LOGGER = Logger.getLogger(NewLineRenderable.class.getName());

    // Width of the new line
    public static final int NEW_LINE_WIDTH = 2;
    // Colour of the new line
    public static final Vector4f NEW_LINE_COLOR = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    // Shader name
    private int shader;
    // Uniform location for the shader's MVP Matrix
    private int shaderMVP;
    // The batch of OpenGL primitives representing the new line
    private final Batch batch;

    private final GLVisualProcessor parent;

    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    private static final int NUMBER_OF_VERTICES = 2;
    private static final int COLOR_BUFFER_WIDTH = 4;
    private static final int VERTEX_BUFFER_WIDTH = 3;

    private final int colorTarget;
    private final int vertexTarget;

    private final BlockingDeque<NewLineModel> modelQueue = new LinkedBlockingDeque<>();

    public NewLineRenderable(final GLVisualProcessor parent) {
        this.parent = parent;
        batch = new Batch(GL.GL_LINES);
        colorTarget = batch.newFloatBuffer(COLOR_BUFFER_WIDTH, true);
        vertexTarget = batch.newFloatBuffer(VERTEX_BUFFER_WIDTH, true);
    }

    @Override
    public int getPriority() {
        return RenderablePriority.ANNOTATIONS_PRIORITY.getValue();
    }

    /**
     * Initialises the batch store.
     * <p>
     * @param drawable The OpenGL rendering target
     */
    @Override
    public void init(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();

        String newLineVp = null;
        String newLineGp = null;
        String newLineFp = null;

        try {
            newLineVp = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThru.vs");
            newLineGp = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThruLine.gs");
            newLineFp = GLTools.loadFile(GLVisualProcessor.class, "shaders/PassThru.fs");
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        shader = GLTools.loadShaderSourceWithAttributes(gl, "PassThru new line", newLineVp, newLineGp, newLineFp,
                vertexTarget, "vertex",
                colorTarget, "color",
                ShaderManager.FRAG_BASE, "fragColor");

        shaderMVP = gl.glGetUniformLocation(shader, "mvpMatrix");

        // The batch we create here has dummy values for vertices.
        // They'll be updated later when the user drags a line.
        batch.initialise(NUMBER_OF_VERTICES);
        batch.stage(colorTarget, NEW_LINE_COLOR);
        batch.stage(vertexTarget, ZERO_3F);
        batch.stage(colorTarget, NEW_LINE_COLOR);
        batch.stage(vertexTarget, ZERO_3F);

        batch.finalise(gl);
    }

    private NewLineModel model;

    public void queueModel(final NewLineModel model) {
        modelQueue.add(model);
    }

    @Override
    public void update(final GLAutoDrawable drawable) {
        final Camera camera = parent.getDisplayCamera();
        NewLineModel updatedModel = modelQueue.peek();
        while (updatedModel != null && updatedModel.getCamera() != camera) {
            modelQueue.remove();
            updatedModel = modelQueue.peek();
        }
        if (updatedModel != null) {
            updatedModel = modelQueue.remove();
            NewLineModel nextModel = modelQueue.peek();
            while (nextModel != null && nextModel.getCamera() == camera) {
                updatedModel = modelQueue.remove();
                nextModel = modelQueue.peek();
            }
            modelQueue.addFirst(updatedModel);
        }
        model = updatedModel;
    }

    /**
     * Draws the new line on the display.
     * <p>
     * @param drawable The OpenGL rendering target.
     * @param pMatrix The model view projection matrix.
     */
    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f pMatrix) {

        final Matrix44f mvpMatrix = parent.getDisplayModelViewProjectionMatrix();

        // If no endpoints are set, don't draw anything
        if (model != null && !model.isClear()) {
            final Vector3f startPosition = model.getStartLocation();
            final Vector3f endPosition = model.getEndLocation();

            final GL3 gl = drawable.getGL().getGL3();

            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, batch.getBufferName(vertexTarget));
            ByteBuffer bbuf = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL.GL_WRITE_ONLY);
            FloatBuffer fbuf = bbuf.asFloatBuffer();

            // Update the line endpoints in the vertex buffer.
            float[] vertices = new float[]{
                startPosition.getX(), startPosition.getY(), startPosition.getZ(),
                endPosition.getX(), endPosition.getY(), endPosition.getZ()};
            fbuf.put(vertices);

            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);

            // Disable depth so the line is drawn over everything else.
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            // Draw the line.
            gl.glLineWidth(NEW_LINE_WIDTH);
            gl.glUseProgram(shader);
            gl.glUniformMatrix4fv(shaderMVP, 1, false, mvpMatrix.a, 0);
            batch.draw(gl);

            gl.glLineWidth(1);

            // Reenable depth.
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(true);

            // Rebind default array buffer
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Disposes of the JOGL data structures.
     * <p>
     * @param drawable The OpenGL rendering target.
     */
    @Override
    public void dispose(final GLAutoDrawable drawable) {
        batch.dispose(drawable.getGL().getGL3());
    }
}
