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
package au.gov.asd.tac.constellation.visual.opengl.renderer;

import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.BlazeBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.ConnectionLabelBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.IconBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.LineBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.LoopBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.NodeLabelBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.SceneBatcher;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.RenderException;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor.VisualChangeProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * A {@link GLRenderable} responsible for the primary component of visualising
 * of a CONSTELLATION graph.
 * <p>
 * This renderable handles the batching, updating and sending to the GL context
 * of graph nodes, connections, blazes, loops and labels.
 *
 * @author algol, twilight_sparkle
 */
public final class GraphRenderable implements GLRenderable {

    private final XyzTexturiser xyzTexturiser = new XyzTexturiser();
    private final VertexFlagsTexturiser vertexFlagsTexturiser = new VertexFlagsTexturiser();
    // Texture for icons.
    private int iconTextureArray;

    private final LineBatcher lineBatcher = new LineBatcher();
    private final LoopBatcher loopBatcher = new LoopBatcher();
    private final NodeLabelBatcher nodeLabelBatcher = new NodeLabelBatcher();
    private final ConnectionLabelBatcher connectionLabelBatcher = new ConnectionLabelBatcher();
    private final IconBatcher iconBatcher = new IconBatcher();
    private final BlazeBatcher blazeBatcher = new BlazeBatcher();

    // We need default values for the background and the draw flags, as the earlier we always display (even with no data) and we check the latter to tell us what to display (again, even with no data).
    private float[] graphBackgroundColor = new float[]{ConstellationColor.BLACK.getRed(), ConstellationColor.BLACK.getGreen(), ConstellationColor.BLACK.getBlue(), 1};
    private DrawFlags drawFlags = DrawFlags.NONE;
    private Camera camera;
    private float motion = -1;
    private long initialMotion;
    // How many pixels per world unit exist at distance 1
    // = height / 2 / tan(FOV/2)
    private float pixelDensity;
    private int hitTestFboName = -1;
    private boolean drawHitTest = true;
    private boolean skipRedraw;

    private final BlockingQueue<GLRenderableUpdateTask> taskQueue = new LinkedBlockingQueue<>();

    private GraphDisplayer graphDisplayer;
    private final GLVisualProcessor parent;

    /**
     * Create a {@link GraphRenderable} for the specified
     * {@link GLVisualProcessor}.
     *
     * @param parent The {@link GLVisualProcessor} that will utilise this
     * renderable for its visualisation.
     */
    public GraphRenderable(final GLVisualProcessor parent) {
        this.parent = parent;
    }

    void setGraphDisplayer(final GraphDisplayer graphDisplayer) {
        this.graphDisplayer = graphDisplayer;
    }

    GraphDisplayer getGraphDisplayer() {
        return graphDisplayer;
    }

    Camera getCamera() {
        return camera;
    }

    private void addTaskIfReady(final GLRenderableUpdateTask task, final SceneBatcher batcher) {
        addTask(gl -> {
            if (batcher.batchReady()) {
                task.run(gl);
            }
        });
    }

    void addTask(final GLRenderableUpdateTask task) {
        taskQueue.add(task);
    }

    void setHitTestFboName(final int hitTestFboName) {
        this.hitTestFboName = hitTestFboName;
    }

    void setDrawHitTest(final boolean drawHitTest) {
        this.drawHitTest = drawHitTest;
    }

    /**
     * Gets the relevant {@link VisualChangeProcessor} for the specified
     * {@link VisualProperty}.
     * <p>
     * This change processor will perform the necessary buffer updating and
     * resending for this renderable, taking care of the fact that retrieval
     * from the supplied {@link VisualAccess} needs to be synchronous, whilst
     * the sending of buffered data needs to occur asynchronously as part of the
     * next display phase of the GL life-cycle.
     *
     * @param property
     * @return
     */
    VisualChangeProcessor getChangeProcessor(VisualProperty property) {
        switch (property) {
            case VERTICES_REBUILD:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setTopLabelColors(access));
                    addTask(nodeLabelBatcher.setTopLabelSizes(access));
                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
                    addTask(nodeLabelBatcher.setBottomLabelSizes(access));
                    addTask(xyzTexturiser.dispose());
                    addTask(xyzTexturiser.createTexture(access));
                    addTask(vertexFlagsTexturiser.dispose());
                    addTask(vertexFlagsTexturiser.createTexture(access));
                    addTask(iconBatcher.disposeBatch());
                    addTask(iconBatcher.createBatch(access));
                    addTask(nodeLabelBatcher.disposeBatch());
                    addTask(nodeLabelBatcher.createBatch(access));
                    addTask(blazeBatcher.disposeBatch());
                    addTask(blazeBatcher.createBatch(access));
                    addTask(gl -> {
                        iconTextureArray = iconBatcher.updateIconTexture(gl);
                    });
                    final DrawFlags updatedDrawFlags = access.getDrawFlags();
                    addTask(gl -> {
                        drawFlags = updatedDrawFlags;
                    });
                };
            case CONNECTIONS_REBUILD:
                return (change, access) -> {
                    addTask(connectionLabelBatcher.setLabelColors(access));
                    addTask(connectionLabelBatcher.setLabelSizes(access));
                    addTask(lineBatcher.disposeBatch());
                    addTask(lineBatcher.createBatch(access));
                    addTask(loopBatcher.disposeBatch());
                    addTask(loopBatcher.createBatch(access));
                    addTask(connectionLabelBatcher.disposeBatch());
                    addTask(connectionLabelBatcher.createBatch(access));
                };
            case BACKGROUND_COLOR:
                return (change, access) -> {
                    final ConstellationColor backgroundColor = access.getBackgroundColor();
                    addTask(gl -> {
                        graphBackgroundColor = new float[]{backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 1};
                    });
                    addTask(connectionLabelBatcher.setBackgroundColor(access));
                    addTask(nodeLabelBatcher.setBackgroundColor(access));
                };
            case HIGHLIGHT_COLOUR:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setHighlightColor(access));
                    addTask(connectionLabelBatcher.setHighlightColor(access));
                    addTask(lineBatcher.setHighlightColor(access));
                    addTask(iconBatcher.setHighlightColor(access));
                };
            case DRAW_FLAGS:
                return (change, access) -> {
                    final DrawFlags updatedDrawFlags = access.getDrawFlags();
                    addTask(gl -> {
                        drawFlags = updatedDrawFlags;
                    });
                };
            case BLAZE_SIZE:
                return (change, access) -> {
                    addTask(blazeBatcher.updateSizeAndOpacity(access));
                };
            case CONNECTIONS_OPACITY:
                return (change, access) -> {
                    addTask(lineBatcher.updateOpacity(access));
                };
            case BOTTOM_LABEL_COLOR:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
                };
            case BOTTOM_LABELS_REBUILD:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
                    addTask(nodeLabelBatcher.setBottomLabelSizes(access));
                    // Note that updating bottom labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                    addTask(nodeLabelBatcher.updateBottomLabels(access));
                };
            case CAMERA:
                return (change, access) -> {
                    final Camera updatedCamera = access.getCamera();
                    addTask(gl -> {
                        camera = updatedCamera;
                        parent.setDisplayCamera(camera);
                        Graphics3DUtilities.getModelViewMatrix(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp, parent.getDisplayModelViewMatrix());
                    });
                };
            case CONNECTION_LABEL_COLOR:
                return (change, access) -> {
                    addTask(connectionLabelBatcher.setLabelColors(access));
                };
            case CONNECTION_LABELS_REBUILD:
                return (change, access) -> {
                    addTask(connectionLabelBatcher.setLabelColors(access));
                    addTask(connectionLabelBatcher.setLabelSizes(access));
                    // Note that updating connection labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                    addTask(connectionLabelBatcher.updateLabels(access));
                };
            case TOP_LABEL_COLOR:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setTopLabelColors(access));
                };
            case TOP_LABELS_REBUILD:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setTopLabelColors(access));
                    addTask(nodeLabelBatcher.setTopLabelSizes(access));
                    // Note that updating top labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                    addTask(nodeLabelBatcher.updateTopLabels(access));
                };
            case CONNECTION_COLOR:
                return (change, access) -> {
                    addTaskIfReady(loopBatcher.updateColors(access, change), loopBatcher);
                    addTaskIfReady(lineBatcher.updateColors(access, change), lineBatcher);
                };
            case CONNECTION_SELECTED:
                return (change, access) -> {
                    addTaskIfReady(loopBatcher.updateInfo(access, change), loopBatcher);
                    addTaskIfReady(lineBatcher.updateInfo(access, change), lineBatcher);
                };
            case VERTEX_BLAZED:
                return (change, access) -> {
                    // Note that updating blazes always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                    addTask(blazeBatcher.updateBlazes(access, change));
                };
            case VERTEX_COLOR:
                return (change, access) -> {
                    addTaskIfReady(iconBatcher.updateColors(access, change), iconBatcher);
                };
            case VERTEX_FOREGROUND_ICON:
                return (change, access) -> {
                    addTaskIfReady(iconBatcher.updateIcons(access, change), iconBatcher);
                    addTask(gl -> {
                        iconTextureArray = iconBatcher.updateIconTexture(gl);
                    });
                };
            case VERTEX_SELECTED:
                return (change, access) -> {
                    if (vertexFlagsTexturiser.isReady()) {
                        addTask(vertexFlagsTexturiser.updateFlags(access, change));
                    } else {
                        addTask(vertexFlagsTexturiser.dispose());
                        addTask(vertexFlagsTexturiser.createTexture(access));
                    }
                };
            case VERTEX_X:
                return (change, access) -> {
                    if (vertexFlagsTexturiser.isReady()) {
                        addTask(xyzTexturiser.updateXyzs(access, change));
                    } else {
                        addTask(xyzTexturiser.dispose());
                        addTask(xyzTexturiser.createTexture(access));
                    }
                };
            case EXTERNAL_CHANGE:
            default:
                return (change, access) -> {
                };
        }
    }

    /**
     * Initialise the batch store.
     * <p>
     * Only JOGL initialisation is done here to match the JOGL context.
     * Everything here is independent of any particular graph.
     *
     * @param drawable GL drawable.
     */
    @Override
    public void init(final GLAutoDrawable drawable) {

        final GL3 gl = drawable.getGL().getGL3();

        // The icon shader draws the node icons.
        // The blaze shader draws visual attachments to the nodes.
        // There are two line shaders. Lines close to the camera are drawn as triangles to
        // provide perspective. Lines further away don't look good as triangles (too many artifacts),
        // so distant lines are drawn as lines.
        // Loops for each node are drawn individually.
        // Each character is drawn individually.
        try {
            blazeBatcher.createShader(gl);
            lineBatcher.createShader(gl);
            loopBatcher.createShader(gl);
            nodeLabelBatcher.createShader(gl);
            connectionLabelBatcher.createShader(gl);
            iconBatcher.createShader(gl);
        } catch (final IOException | RenderException ex) {
            // If we get here, a shader didn't compile. This obviously shouldn't happen in production; 
            // our shaders are static and read from built-in resource files (it happens a lot in 
            // development when we edit a shader, but that's OK). Since at least one shader is null, 
            // there will be subsequent NullPointerExceptions, but there's nothing we can do about that. 
            // Without shaders, we're dead in the water anyway.
            final String msg
                    = "This error may have occurred because your video card and/or driver is\n"
                    + "incompatible with CONSTELLATION.\n\n"
                    + "Please inform CONSTELLATION support, including the text of this message.\n\n"
                    + ex.getMessage();
            Logger.getLogger(GraphRenderable.class
                    .getName()).log(Level.SEVERE, msg, ex);
            final InfoTextPanel itp = new InfoTextPanel(msg);
            final NotifyDescriptor.Message nd = new NotifyDescriptor.Message(itp, NotifyDescriptor.ERROR_MESSAGE);
            nd.setTitle("Shader Error");
            DialogDisplayer.getDefault().notify(nd);
        }

        graphDisplayer.init(drawable);

        GLTools.checkFramebufferStatus(gl, "gr-check");

    }

    @Override
    public void reshape(final int x, final int y, final int width, final int height) {
        // Sets the number of pixels per world unit at distance 1
        this.pixelDensity = (float) (height * 0.5 / Math.tan(Math.toRadians(GLRenderer.FIELD_OF_VIEW)));
        graphDisplayer.reshape(x, y, width, height);
    }

    @Override
    public void update(GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        skipRedraw = false;
        if (parent.isUpdating()) {
            final List<GLRenderableUpdateTask> tasks = new ArrayList<>();
            if (taskQueue.isEmpty()) {
                skipRedraw = true;
            }
            taskQueue.drainTo(tasks);
            tasks.forEach(task -> {
                task.run(gl);
            });
        }
    }

    /**
     * Display this batch store to OpenGL.
     *
     * @param drawable
     * @param pMatrix
     */
    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f pMatrix) {

        final GL3 gl = drawable.getGL().getGL3();

        // Bind to the graph displayer, and if a redraw is required, render the graph to the displayer.
        graphDisplayer.bindDisplayer(gl);
        if (!skipRedraw) {

            // Direction Indicators.
            if (motion == -1) {
                if (DirectionIndicatorsAction.showIndicators) {
                    initialMotion = System.currentTimeMillis();
                    motion = 0;
                }
            } else if (DirectionIndicatorsAction.showIndicators) {
                motion = (System.currentTimeMillis() - initialMotion) / 100f;
            } else {
                motion = -1;
            }

            gl.glEnable(GL3.GL_LINE_SMOOTH);
            gl.glEnable(GL3.GL_POLYGON_OFFSET_FILL);
            gl.glClearColor(graphBackgroundColor[0], graphBackgroundColor[1], graphBackgroundColor[2], graphBackgroundColor[3]);
            gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

            // Bind the textures to their texture units.
            // This only needs to be done once.
            gl.glActiveTexture(GL3.GL_TEXTURE0 + TextureUnits.VERTICES);
            gl.glBindTexture(GL3.GL_TEXTURE_BUFFER, xyzTexturiser.getTextureName());
            gl.glActiveTexture(GL3.GL_TEXTURE0 + TextureUnits.ICONS);
            gl.glBindTexture(GL3.GL_TEXTURE_2D_ARRAY, iconTextureArray);
            gl.glActiveTexture(GL3.GL_TEXTURE0 + TextureUnits.VERTEX_FLAGS);
            gl.glBindTexture(GL3.GL_TEXTURE_BUFFER, vertexFlagsTexturiser.getTextureName());

            // We attempt to use PolygonOffset() to keep the lines behind the icons.
            // One factor,unit for lines, another factor,unit for points.
            // For some reason, when you zoom in, lines get drawn over icons; why?
            // I suspect it's because perspective means that nodes that are further from the eye/centre axis aren't
            // flat relative to the eye, therefore the lines slope across them.
            // (I tried playing with DepthRange(), but all lines were behind all nodes even in 3D, which looks really weird.
            // Maybe a different n,f would work there.
            final float further_f = 0;
            final float further_u = 1;
            final float nearer_f = 0;
            final float nearer_u = -1;

            gl.glPolygonOffset(further_f, further_u);

            final Matrix44f mvMatrix = parent.getDisplayModelViewMatrix();

            if (drawFlags.drawConnections()) {
                lineBatcher.setMotion(motion);
                lineBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
                loopBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
            }

            gl.glPolygonOffset(nearer_f, nearer_u);

            // Draw node icons
            if (drawFlags.drawNodes()) {
                iconBatcher.setPixelDensity(pixelDensity);
                iconBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
            }

            // Draw node labels
            if (drawFlags.drawNodes() && drawFlags.drawNodeLabels()) {
                nodeLabelBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
            }

            gl.glPolygonOffset(further_f, further_u);

            // Draw connection labels
            if (drawFlags.drawConnectionLabels() && drawFlags.drawConnections()) {
                connectionLabelBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
            }

            gl.glPolygonOffset(0, 0);

            // Blazes are only drawn if points are being drawn.
            // Blazes are drawn last because we want them to be on top of everything else.
            if (drawFlags.drawNodes() && drawFlags.drawBlazes()) {
                blazeBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
            }

            if (hitTestFboName > 0 && drawHitTest) {
                // Draw the lines and icons again with unique colors on the hitTest framebuffer.
                // The lines will be thicker for easier hitting.
                gl.glBindFramebuffer(GL3.GL_DRAW_FRAMEBUFFER, hitTestFboName);

                // Explicitly clear the color to black: we need the default color to be 0 so elements drawn as non-zero are recognised.
                gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

                gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
                gl.glDisable(GL3.GL_LINE_SMOOTH);

                // Is this the default anyway?
                final int[] fboBuffers = {
                    GL3.GL_COLOR_ATTACHMENT0
                };
                gl.glDrawBuffers(1, fboBuffers, 0);

                gl.glPolygonOffset(further_f, further_u);

                if (drawFlags.drawConnections()) {
                    lineBatcher.setNextDrawIsHitTest();
                    lineBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
                    loopBatcher.setNextDrawIsHitTest();
                    loopBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
                }

                gl.glPolygonOffset(nearer_f, nearer_u);

                // Draw node icons into hit test buffer
                if (drawFlags.drawNodes()) {
                    iconBatcher.setNextDrawIsHitTest();
                    iconBatcher.drawBatch(gl, camera, mvMatrix, pMatrix);
                }

                gl.glPolygonOffset(0, 0);
                gl.glDisable(GL3.GL_POLYGON_OFFSET_FILL);

                gl.glBindFramebuffer(GL3.GL_DRAW_FRAMEBUFFER, 0);
                gl.glEnable(GL3.GL_LINE_SMOOTH);
            }
        }

        // Get the graph displayer to render its contents to the screen
        graphDisplayer.display(drawable, pMatrix);
    }

    /**
     * Dispose of JOGL data structures.
     * <p>
     * Note: do not destroy the shared things (currently icon texture array,
     * font texture, shaders).
     *
     * @param drawable GL drawable.
     */
    @Override
    public void dispose(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        lineBatcher.disposeBatch().run(gl);
        loopBatcher.disposeBatch().run(gl);
        blazeBatcher.disposeBatch().run(gl);
        iconBatcher.disposeBatch().run(gl);
        nodeLabelBatcher.disposeBatch().run(gl);
        connectionLabelBatcher.disposeBatch().run(gl);
        xyzTexturiser.dispose().run(gl);
        vertexFlagsTexturiser.dispose().run(gl);
        graphDisplayer.dispose(drawable);
        parent.rebuild();
    }
}
