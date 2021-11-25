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

import au.gov.asd.tac.constellation.utilities.camera.AnaglyphCamera;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor.VisualChangeProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.BlazeBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.ConnectionLabelBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.IconBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.LineBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.LoopBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.NodeLabelBatcher;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.SceneBatcher;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.RenderException;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES3;
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

    // When drawing batches:
    // We attempt to use PolygonOffset() to keep the lines behind the icons.
    // One factor,unit for lines, another factor,unit for points.
    // For some reason, when you zoom in, lines get drawn over icons; why?
    // I suspect it's because perspective means that nodes that are further from the eye/centre axis aren't
    // flat relative to the eye, therefore the lines slope across them.
    // (I tried playing with DepthRange(), but all lines were behind all nodes even in 3D, which looks really weird.
    // Maybe a different n,f would work there.
    private static final float FURTHER_F = 0;
    private static final float FURTHER_U = 1;
    private static final float NEARER_F = 0;
    private static final float NEARER_U = -1;

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

    private static final Logger LOGGER = Logger.getLogger(GraphRenderable.class.getName());

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
                    try {
                        addTask(nodeLabelBatcher.createBatch(access));
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
                    addTask(blazeBatcher.disposeBatch());
                    addTask(blazeBatcher.createBatch(access));
                    addTask(gl -> iconTextureArray = iconBatcher.updateIconTexture(gl));
                    final DrawFlags updatedDrawFlags = access.getDrawFlags();
                    addTask(gl -> drawFlags = updatedDrawFlags);
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
                    try {
                        addTask(connectionLabelBatcher.createBatch(access));
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
                };
            case BACKGROUND_COLOR:
                return (change, access) -> {
                    final ConstellationColor backgroundColor = access.getBackgroundColor();
                    addTask(gl
                            -> graphBackgroundColor = new float[]{backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 1});
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
                    addTask(gl -> drawFlags = updatedDrawFlags);
                };
            case BLAZE_SIZE:
                return (change, access) -> addTask(blazeBatcher.updateSizeAndOpacity(access));
            case CONNECTIONS_OPACITY:
                return (change, access) -> addTask(lineBatcher.updateOpacity(access));
            case BOTTOM_LABEL_COLOR:
                return (change, access) -> addTask(nodeLabelBatcher.setBottomLabelColors(access));
            case BOTTOM_LABELS_REBUILD:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
                    addTask(nodeLabelBatcher.setBottomLabelSizes(access));
                    try {
                        // Note that updating bottom labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                        addTask(nodeLabelBatcher.updateBottomLabels(access));
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
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
                    try {
                        // Note that updating connection labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                        addTask(connectionLabelBatcher.updateLabels(access));
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
                };
            case TOP_LABEL_COLOR:
                return (change, access) -> addTask(nodeLabelBatcher.setTopLabelColors(access));
            case TOP_LABELS_REBUILD:
                return (change, access) -> {
                    addTask(nodeLabelBatcher.setTopLabelColors(access));
                    addTask(nodeLabelBatcher.setTopLabelSizes(access));
                    try {
                        // Note that updating top labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                        addTask(nodeLabelBatcher.updateTopLabels(access));
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
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
                return (change, access) -> 
                    // Note that updating blazes always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
                    addTask(blazeBatcher.updateBlazes(access, change));
            case VERTEX_COLOR:
                return (change, access) -> addTaskIfReady(iconBatcher.updateColors(access, change), iconBatcher);
            case VERTEX_FOREGROUND_ICON:
                return (change, access) -> {
                    addTaskIfReady(iconBatcher.updateIcons(access, change), iconBatcher);
                    addTask(gl -> iconTextureArray = iconBatcher.updateIconTexture(gl));
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
            LOGGER.log(Level.SEVERE, msg, ex);
            final InfoTextPanel itp = new InfoTextPanel(msg);
            final NotifyDescriptor.Message nd = new NotifyDescriptor.Message(itp, NotifyDescriptor.ERROR_MESSAGE);
            nd.setTitle("Shader Error");
            DialogDisplayer.getDefault().notify(nd);
        }

        graphDisplayer.init(drawable);

        GLTools.checkFramebufferStatus(gl, "gr-check");
        parent.rebuild();
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
            tasks.forEach(task -> task.run(gl));
        }
    }

    /**
     * Display this batch store to OpenGL.
     *
     * display is called in response to various events such as the move moving
     * or right clicking. It isn't a continuous render call one might expect in
     * an OpenGL application.
     *
     * @param drawable From the reference: A higher-level abstraction than
     * GLDrawable which supplies an event based mechanism (GLEventListener) for
     * performing OpenGL rendering. A GLAutoDrawable automatically creates a
     * primary rendering context which is associated with the GLAutoDrawable for
     * the lifetime of the object.
     * @param pMatrix
     */
    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f pMatrix) {
        final GL3 gl = drawable.getGL().getGL3();
        graphDisplayer.bindDisplayer(gl);

        if (!skipRedraw) {

            // Direction Indicators.
            if (motion == -1) {
                if (DirectionIndicatorsAction.isShowIndicators()) {
                    initialMotion = System.currentTimeMillis();
                    motion = 0;
                }
            } else if (DirectionIndicatorsAction.isShowIndicators()) {
                motion = (System.currentTimeMillis() - initialMotion) / 100F;
            } else {
                motion = -1;
            }

            gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
            gl.glClearColor(graphBackgroundColor[0], graphBackgroundColor[1], graphBackgroundColor[2], graphBackgroundColor[3]);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            // Bind the textures to their texture units.
            // This only needs to be done once.
            gl.glActiveTexture(GL.GL_TEXTURE0 + TextureUnits.VERTICES);
            gl.glBindTexture(GL2ES3.GL_TEXTURE_BUFFER, xyzTexturiser.getTextureName());
            gl.glActiveTexture(GL.GL_TEXTURE0 + TextureUnits.ICONS);
            gl.glBindTexture(GL2ES3.GL_TEXTURE_2D_ARRAY, iconTextureArray);
            gl.glActiveTexture(GL.GL_TEXTURE0 + TextureUnits.VERTEX_FLAGS);
            gl.glBindTexture(GL2ES3.GL_TEXTURE_BUFFER, vertexFlagsTexturiser.getTextureName());

            final Matrix44f mvMatrix = parent.getDisplayModelViewMatrix();

            if (AnaglyphicDisplayAction.isAnaglyphicDisplay()) {
                // Draw (some parts of) the graph in anaglyph format.
                // To do this, we use an AnaglyphicCamera to draw the graph twice,
                // from the viewpoints of the left and right eyes.
                //

                // The convergence is the plane where objects appear to be at the same depth as the screen.
                // Objects closer than this appear to be in front of the screen; objects further than
                // this appear to be inside the screen.
                //
                // Ideally we want this to be some fixed distance from the camera(s), taking the size of
                // the graph into consideration; for example, half the width of the graph. However,
                // this would mean recalculating the physical size of the graph every time we displayed it (because
                // here we don't want to keep track of which graph we're displaying).
                //
                // As a reasonable substitute, we'll use the distance from the eye to the centre.
                // Resetting the view puts the lookAt centre in the middle of the graph anyway,
                // and moving around generally seems to keep the centre at the same distance from the eye.
                // As a convenient side effect, if the centre is changed to a node, then that node will be at the convergence.
                //
                final Vector3f eye = camera.lookAtEye;
                final Vector3f centre = camera.lookAtCentre;
//                final float distanceToCentre = (float)Math.sqrt(Math.pow(centre.getX()-eye.getX(), 2) + Math.pow(centre.getY()-eye.getY(), 2) + Math.pow(centre.getZ()-eye.getZ(), 2));
                final float distanceToCentre = Vector3f.subtract(centre, eye).getLength();

                final float convergence = Camera.PERSPECTIVE_NEAR + distanceToCentre;

                final float eyeSeparation = 0.25F; // This is an arbitrary value, arrived at by experimentation.
                final float aspect = (float)graphDisplayer.getWidth()/(float)graphDisplayer.getHeight();
                final AnaglyphCamera anaglyphCam = new AnaglyphCamera(convergence, eyeSeparation, aspect, Camera.FIELD_OF_VIEW, Camera.PERSPECTIVE_NEAR, Camera.PERSPECTIVE_FAR);

                // The eye colors are pulled from the preferences by AnaglyphicDisplayAction when
                // anaglyphic mode is turned on. A bit ugly, but it gives us quick access to the colors.
                // Note that the eye glass colors go to the opposite camera.
                //
                final AnaglyphicDisplayAction.EyeColorMask leftEyeColor = AnaglyphicDisplayAction.getLeftColorMask();
                final AnaglyphicDisplayAction.EyeColorMask rightEyeColor = AnaglyphicDisplayAction.getRightColorMask();

                // Draw view from left eye.
                //

                Matrix44f mv = anaglyphCam.applyLeftFrustum(mvMatrix);
                Matrix44f p = anaglyphCam.getProjectionMatrix();

//                gl.glColorMask(true, false, true, true);
                gl.glColorMask(rightEyeColor.red, rightEyeColor.green, rightEyeColor.blue, true);

                drawBatches(gl, mv, p, true);

                // Draw view from right eye.
                //

                // Don't overwrite the other eye.
                //
                gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);

                mv = anaglyphCam.applyRightFrustum(mvMatrix);
                p = anaglyphCam.getProjectionMatrix();

//                gl.glColorMask(false, true, false, true);
                gl.glColorMask(leftEyeColor.red, leftEyeColor.green, leftEyeColor.blue, true);

                drawBatches(gl, mv, p, true);

                gl.glColorMask(true, true, true, true);
            } else {
                drawBatches(gl, mvMatrix, pMatrix, false);

                if (hitTestFboName > 0 && drawHitTest) {
                    // Draw the lines and icons again with unique colors on the hitTest framebuffer.
                    // The lines will be thicker for easier hitting.
                    gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, hitTestFboName);

                    // Explicitly clear the color to black: we need the default color to be 0 so elements drawn as non-zero are recognised.
                    gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);

                    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                    gl.glDisable(GL.GL_LINE_SMOOTH);

                    // Is this the default anyway?
                    final int[] fboBuffers = {
                        GL.GL_COLOR_ATTACHMENT0
                    };
                    gl.glDrawBuffers(1, fboBuffers, 0);

                    gl.glPolygonOffset(FURTHER_F, FURTHER_U);

                    if (drawFlags.drawConnections()) {
                        lineBatcher.setNextDrawIsHitTest();
                        lineBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, false);
                        loopBatcher.setNextDrawIsHitTest();
                        loopBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, false);
                    }

                    gl.glPolygonOffset(NEARER_F, NEARER_U);

                    // Draw node icons into hit test buffer
                    if (drawFlags.drawNodes()) {
                        iconBatcher.setNextDrawIsHitTest();
                        iconBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, false);
                    }

                    gl.glPolygonOffset(0, 0);
                    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);

                    gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, 0);
                    gl.glEnable(GL.GL_LINE_SMOOTH);
                }
            }
        }

        // Get the graph displayer to render its contents to the screen
        graphDisplayer.display(drawable, pMatrix);

    }

    private void drawBatches(final GL3 gl, final Matrix44f mvMatrix, final Matrix44f pMatrix, final boolean greyscale) {
        gl.glPolygonOffset(FURTHER_F, FURTHER_U);

        if (drawFlags.drawConnections()) {
            lineBatcher.setMotion(motion);
            lineBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, greyscale);
            loopBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, greyscale);
        }

        gl.glPolygonOffset(NEARER_F, NEARER_U);

        // Draw node icons
        if (drawFlags.drawNodes()) {
            iconBatcher.setPixelDensity(pixelDensity);
            iconBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, greyscale);
        }

        // Draw node labels
        if (drawFlags.drawNodes() && drawFlags.drawNodeLabels()) {
            nodeLabelBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, greyscale);
        }

        gl.glPolygonOffset(FURTHER_F, FURTHER_U);

        // Draw connection labels
        if (drawFlags.drawConnectionLabels() && drawFlags.drawConnections()) {
            connectionLabelBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, greyscale);
        }

        gl.glPolygonOffset(0, 0);

        // Blazes are only drawn if points are being drawn.
        // Blazes are drawn last because we want them to be on top of everything else.
        if (drawFlags.drawNodes() && drawFlags.drawBlazes()) {
            blazeBatcher.drawBatch(gl, camera, mvMatrix, pMatrix, greyscale);
        }
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
    }
}
