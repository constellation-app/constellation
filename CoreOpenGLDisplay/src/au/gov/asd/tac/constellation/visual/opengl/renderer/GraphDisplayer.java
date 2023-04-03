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
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.Batch;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.ShaderManager;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link GLRenderable} which is used as an intermediate staging texture that
 * a {@link GraphRenderable} can render to, which is then drawn to the screen.
 * <p>
 * There are two advantages to using a GraphDisplayer rather than drawing a
 * {@link GraphRenderable} directly to the screen. Firstly, when we only need
 * renderables unrelated to the graph to be updated, we can redraw the graph
 * simply by redrawing this texture to the screen (rather than having to redraw
 * each component of the graph through vastly more complicated shaders).
 * Secondly, a GraphDisplayer can easily implement post-processing visual
 * filters on their textures (such as color or skew transformations).
 *
 * @author twilight_sparkle
 */
public class GraphDisplayer implements GLRenderable {

    private static final Logger LOGGER = Logger.getLogger(GraphDisplayer.class.getName());

    // Batch constants
    private static final int GRAPH_TEXTURE_NUMBER_OF_VERTICES = 4;
    private static final int TEXTURE_COORDINATES_BUFFER_WIDTH = 2;
    private static final int VERTEX_BUFFER_WIDTH = 2;

    // Batch and batch targets
    private final Batch graphTextureBatch;
    private final int vertexTarget;
    private final int textureCoordinatesTarget;

    // GL Targets
    final int[] graphFboName = new int[1];
    final int[] graphColorTextureName = new int[1];
    final int[] graphDepthTextureName = new int[1];
    final int[] graphDrawBuffers = new int[1];

    // GL canvas dimension tracking
    private int width;
    private int height;
    private boolean needsResize;

    // Shader and shader locations
    protected int graphTextureShader;
    private int graphColorTextureShaderLocation;
    private int graphDepthTextureShaderLocation;

    /**
     * Creates a new GraphDisplayer.
     */
    public GraphDisplayer() {
        graphTextureBatch = new Batch(GL.GL_TRIANGLE_STRIP);
        vertexTarget = graphTextureBatch.newFloatBuffer(VERTEX_BUFFER_WIDTH, true);
        textureCoordinatesTarget = graphTextureBatch.newFloatBuffer(TEXTURE_COORDINATES_BUFFER_WIDTH, true);
    }

    /**
     * Retrieve the code for the vertex shader for this GraphDisplayer. This may
     * be overriden by subclasses.
     *
     * @return The vertex shader code as a String.
     * @throws IOException
     */
    protected String getVertexShader() throws IOException {
        return GLTools.loadFile(GraphDisplayer.class, "shaders/Graph.vs");
    }

    /**
     * Retrieve the code for the fragment shader for this GraphDisplayer. This
     * may be overriden by subclasses.
     *
     * @return The vertex shader code as a String.
     * @throws IOException
     */
    protected String getFragmentShader() throws IOException {
        return GLTools.loadFile(GraphDisplayer.class, "shaders/Graph.fs");
    }

    /**
     * Allows subclasses to create extra shader locations in the
     * <code>init()</code> phase of the GL life-cycle for the purpose of
     * post-processing visual effects.
     *
     * @param gl The GL Context on which to create shader locations.
     */
    protected void createShaderLocations(final GL3 gl) {
    }

    /**
     * Allows subclasses to bind to extra shader locations in the
     * <code>display()</code> phase of the GL life-cycle for the purpose of
     * post-processing visual effects.
     *
     * @param gl The GL Context on which to bind shader locations.
     */
    protected void bindShaderLocations(final GL3 gl) {
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();

        String graphVp = null;
        String graphFp = null;
        try {
            graphVp = getVertexShader();
            graphFp = getFragmentShader();
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Can''t read graph texture shaders", ex);
        }
        graphTextureShader = GLTools.loadShaderSourceWithAttributes(gl, "graphTex", graphVp, null, graphFp,
                vertexTarget, "position",
                textureCoordinatesTarget, "inputTextureCoordinates",
                ShaderManager.FRAG_BASE, "fragColor");

        graphTextureBatch.initialise(GRAPH_TEXTURE_NUMBER_OF_VERTICES);
        graphTextureBatch.stage(vertexTarget, -1F, -1F);
        graphTextureBatch.stage(vertexTarget, 1F, -1F);
        graphTextureBatch.stage(vertexTarget, -1F, 1F);
        graphTextureBatch.stage(vertexTarget, 1F, 1F);
        graphTextureBatch.stage(textureCoordinatesTarget, 0F, 0F);
        graphTextureBatch.stage(textureCoordinatesTarget, 1F, 0F);
        graphTextureBatch.stage(textureCoordinatesTarget, 0F, 1F);
        graphTextureBatch.stage(textureCoordinatesTarget, 1F, 1F);
        graphTextureBatch.finalise(gl);

        // Create the FBO to draw the graph onto.
        gl.glGenFramebuffers(1, graphFboName, 0);
        gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, graphFboName[0]);

        // Create a texture for color information.
        gl.glGenTextures(1, graphColorTextureName, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, graphColorTextureName[0]);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, 10, 10, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
        gl.glFramebufferTexture(GL.GL_DRAW_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, graphColorTextureName[0], 0);
        graphDrawBuffers[0] = GL.GL_COLOR_ATTACHMENT0;

        // Create a texture for depth information and attach it.
        gl.glGenTextures(1, graphDepthTextureName, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, graphDepthTextureName[0]);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT16, 10, 10, 0, GL2ES2.GL_DEPTH_COMPONENT, GL.GL_FLOAT, null);
        gl.glFramebufferTexture(GL.GL_DRAW_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, graphDepthTextureName[0], 0);

        graphColorTextureShaderLocation = gl.glGetUniformLocation(graphTextureShader, "graphTexture");
        graphDepthTextureShaderLocation = gl.glGetUniformLocation(graphTextureShader, "depthTexture");
        createShaderLocations(gl);
    }

    @Override
    public void reshape(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        needsResize = true;
    }

    @Override
    public void display(GLAutoDrawable drawable, Matrix44f pMatrix) {

        // Draw the graph texture to the screen
        final GL3 gl = drawable.getGL().getGL3();
        gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, graphColorTextureName[0]);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 1);
        gl.glBindTexture(GL.GL_TEXTURE_2D, graphDepthTextureName[0]);
        gl.glUseProgram(graphTextureShader);
        gl.glUniform1i(graphColorTextureShaderLocation, 0);
        gl.glUniform1i(graphDepthTextureShaderLocation, 1);
        bindShaderLocations(gl);
        gl.glDisable(GL.GL_DEPTH_TEST);
        graphTextureBatch.draw(gl);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        graphTextureBatch.dispose(gl);
    }

    /**
     * Called by the {@link GraphRenderable} using this graph display to bind to
     * this displayer's buffers so that the graph is drawn onto these buffers
     * rather than directly onto the screen buffers.
     *
     * @param gl The GL Context on which to bind to this displayer's buffers.
     */
    final void bindDisplayer(final GL3 gl) {
        gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, graphFboName[0]);
        gl.glDrawBuffers(1, graphDrawBuffers, 0);
        if (needsResize) {
            gl.glActiveTexture(GL.GL_TEXTURE0);
            gl.glBindTexture(GL.GL_TEXTURE_2D, graphColorTextureName[0]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB8, width, height, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, null);
            gl.glActiveTexture(GL.GL_TEXTURE0 + 1);
            gl.glBindTexture(GL.GL_TEXTURE_2D, graphDepthTextureName[0]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT16, width, height, 0, GL2ES2.GL_DEPTH_COMPONENT, GL.GL_FLOAT, null);
            needsResize = false;
        }
    }

    /**
     * The width of the display window.
     *
     * @return The width of the display window.
     */
    public int getWidth() {
        return width;
    }

    /**
     * The height of the display window.
     *
     * @return The height of the display window.
     */
    public int getHeight() {
        return height;
    }
}
