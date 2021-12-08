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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Plane;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.PlaneState;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLRenderable;
import au.gov.asd.tac.constellation.visual.opengl.renderer.GLVisualProcessor;
import au.gov.asd.tac.constellation.visual.opengl.renderer.TextureUnits;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.Batch;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.ShaderManager;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implement layer planes.
 *
 * @author algol
 */
public final class PlanesRenderable implements GLRenderable {
    
    private static final Logger LOGGER = Logger.getLogger(PlanesRenderable.class.getName());

    public static final int BUFFER_UNUSED = -2;
    private int shader;
    private final Batch planeBatch;
    // Uniforms.
    private int shaderMVPMatrix;
    private int shaderImages;
    private List<Plane> planes;

    private static final int DATA_BUFFER_WIDTH = 3;
    private static final int VERTEX_BUFFER_WIDTH = 3;

    private final int planeInfoTarget;
    private final int vertexTarget;

    private int textureName;

    public PlanesRenderable() {
        planeBatch = new Batch(GL.GL_TRIANGLES);
        planeInfoTarget = planeBatch.newFloatBuffer(DATA_BUFFER_WIDTH, true);
        vertexTarget = planeBatch.newFloatBuffer(VERTEX_BUFFER_WIDTH, true);
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
            vs = GLTools.loadFile(GLVisualProcessor.class, "shaders/Plane.vs");
            gs = GLTools.loadFile(GLVisualProcessor.class, "shaders/Plane.gs");
            fs = GLTools.loadFile(GLVisualProcessor.class, "shaders/Plane.fs");
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        shader = GLTools.loadShaderSourceWithAttributes(gl, "Plane", vs, gs, fs,
                ShaderManager.ATTRIBUTE_DATAA, "data",
                ShaderManager.ATTRIBUTE_VERTEX, "vertex",
                ShaderManager.FRAG_BASE, "fragColor");
        shaderMVPMatrix = gl.glGetUniformLocation(shader, "mvpMatrix");
        shaderImages = gl.glGetUniformLocation(shader, "images");

        textureName = BUFFER_UNUSED;
    }

    public void createScene(final GLAutoDrawable drawable, final Graph graph) {
        final GL3 gl = drawable.getGL().getGL3();

        dispose(drawable);

        final int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        textureName = textures[0];

        planes = null;

        ReadableGraph rg = graph.getReadableGraph();
        try {
            final int planesAttr = rg.getAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME);
            if (planesAttr != Graph.NOT_FOUND) {
                final PlaneState state = (PlaneState) rg.getObjectValue(planesAttr, 0);
                if (state != null) {
                    planes = state.getPlanes();

                    if (planes.size() > 0) {
                        final ArrayList<BufferedImage> planeBytes = new ArrayList<>();
                        int maxw = 0;
                        int maxh = 0;
                        for (final Plane plane : planes) {
                            planeBytes.add(plane.getImage());
                            maxw = Math.max(maxw, plane.getImageWidth());
                            maxh = Math.max(maxh, plane.getImageHeight());
                        }

                        GLTools.loadTextures(gl, textureName, planeBytes, maxw, maxh, GL.GL_LINEAR, GL.GL_LINEAR, GL.GL_CLAMP_TO_EDGE);

                        // We want to draw a sequence of disjoint rectangles, so we have to use GL_TRIANGLES, because
                        // the other triangle options require the triangles to be contiguous.
                        // This means that it takes six vertices to draw a rectangle rather than four, but since there
                        // will only be a few (roughly 10?), this doesn't seem to be a big deal (yet).
                        if (planeBatch.isDrawable()) {
                            planeBatch.dispose(gl);
                        }
                        planeBatch.initialise(planes.size() * 6);

                        // The color buffer is used to hold texture data.
                        // [0], [1]: the texture coordinates used by the fragment shader to get pixels from a texture.
                        // [2]: boolean: should this layer be visible?
                        // [3]: the texture to be used for this triangle.
                        for (int ix = 0; ix < planeBytes.size(); ix++) {
                            final Plane plane = planes.get(ix);
                            final float x = plane.getX();
                            final float y = plane.getY();
                            final float z = plane.getZ();
                            final float w = plane.getWidth();
                            final float h = plane.getHeight();

                            // The texture array area is as big as the largest width and height.
                            // We have to tell the shaders what propertion of the texture area is used by each image.
                            final float wfrac = maxw != 0 ? plane.getImageWidth() / (float) maxw : plane.getImageWidth();
                            final float hfrac = maxh != 0 ? plane.getImageHeight() / (float) maxh : plane.getImageHeight();

                            // A plane is visible if it has a visibility>0.
                            // Maybe this should match with the visibility of the graph?
                            // We include the visibility with each triangle.
                            final float isVisible = plane.isVisible() ? 1 : 0;

                            planeBatch.stage(planeInfoTarget, 0, 1, isVisible, ix);
                            planeBatch.stage(vertexTarget, x, y, z);
                            planeBatch.stage(planeInfoTarget, 0, 0, wfrac, Graph.NOT_FOUND);
                            planeBatch.stage(vertexTarget, x, y + h, z);
                            planeBatch.stage(planeInfoTarget, 1, 1, hfrac, Graph.NOT_FOUND);
                            planeBatch.stage(vertexTarget, x + w, y, z);

                            planeBatch.stage(planeInfoTarget, 1, 1, isVisible, ix);
                            planeBatch.stage(vertexTarget, x + w, y, z);
                            planeBatch.stage(planeInfoTarget, 0, 0, wfrac, Graph.NOT_FOUND);
                            planeBatch.stage(vertexTarget, x, y + h, z);
                            planeBatch.stage(planeInfoTarget, 1, 0, hfrac, Graph.NOT_FOUND);
                            planeBatch.stage(vertexTarget, x + w, y + h, z);
                        }

                        planeBatch.finalise(gl);
                    }
                }
            }
        } finally {
            rg.release();
        }
    }

    List<Plane> getPlanes() {
        return planes;
    }

    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f mvpMatrix) {
        if (planeBatch.isDrawable()) {
            final GL3 gl = drawable.getGL().getGL3();

            gl.glActiveTexture(GL.GL_TEXTURE0 + TextureUnits.PLANES);
            gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, textureName);

            gl.glUseProgram(shader);
            gl.glUniformMatrix4fv(shaderMVPMatrix, 1, false, mvpMatrix.a, 0);
            gl.glUniform1i(shaderImages, TextureUnits.PLANES);
            planeBatch.draw(gl);
        }
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();

        if (textureName != BUFFER_UNUSED) {
            gl.glDeleteTextures(1, new int[]{textureName}, 0);
            textureName = BUFFER_UNUSED;
        }

        planeBatch.dispose(gl);
    }

    public void setVisiblePlanes(final GLAutoDrawable drawable, final BitSet visibleLayers) {

        if (planeBatch.isDrawable()) {
            final GL3 gl = drawable.getGL().getGL3();
            // Map the buffer range.
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, planeBatch.getBufferName(planeInfoTarget));
            final ByteBuffer bbuf = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL2ES3.GL_READ_WRITE);
            final FloatBuffer fbuf = bbuf.order(ByteOrder.nativeOrder()).asFloatBuffer();

            // Update the visibility flag for the layers.
            // See createScene().
            // Each plane has six data entries, each data entry is four floats
            // visibility is in the first entry.
            final int verticesPerPlane = 24;
            final int nVertices = fbuf.limit() / verticesPerPlane;
            for (int i = 0; i < nVertices; i++) {
                final int base = i * verticesPerPlane;

                fbuf.put(base + 2, visibleLayers.get(i) ? 1F : 0F);
                fbuf.put(base + verticesPerPlane / 2 + 2, visibleLayers.get(i) ? 1F : 0F);
            }

            // Unmap the buffer range.
            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }
    }

    public BitSet getVisiblePlanes(final GLAutoDrawable drawable) {

        final BitSet visiblePlanes = new BitSet();

        if (planeBatch.isDrawable()) {
            final GL3 gl = drawable.getGL().getGL3();
            // Map the buffer range.
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, planeBatch.getBufferName(planeInfoTarget));
            final ByteBuffer bbuf = gl.glMapBuffer(GL.GL_ARRAY_BUFFER, GL2ES3.GL_READ_ONLY);
            final FloatBuffer fbuf = bbuf.order(ByteOrder.nativeOrder()).asFloatBuffer();

            // Get the visibility flag for the planes.
            // Each plane has six dataA entries, each dataA entry is four floats, making 24 vertices per plane.
            // We'll just look at the visibility for the first vertex in each plane (offset 2), since each vertex has the same visibility.
            final int verticesPerPlane = 24;
            final int nPlanes = fbuf.limit() / verticesPerPlane;
            for (int i = 0; i < nPlanes; i++) {
                final int base = i * verticesPerPlane;
                final float vis = fbuf.get(base + 2);
                if (vis > 0) {
                    visiblePlanes.set(i);
                }
            }

            // Unmap the buffer range.
            gl.glUnmapBuffer(GL.GL_ARRAY_BUFFER);
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        }

        return visiblePlanes;
    }
}
