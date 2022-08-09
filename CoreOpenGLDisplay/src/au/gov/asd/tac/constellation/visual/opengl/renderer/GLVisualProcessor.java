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

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualOperation;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor.VisualChangeProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import au.gov.asd.tac.constellation.visual.opengl.utilities.SharedDrawable;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * A {@link VisualProcessor} that creates a 3D visualisation using OpenGL. This
 * is the primary visual processor for CONSTELLATION.
 * <p>
 * This visual processor creates a {@link GLCanvas}, and an instance of a
 * {@link GLRenderer} to update the canvas throughout the OpenGL life-cycle.
 * This renderer coordinates the work of actually sending data to the GL Context
 * which is done by a number of {@link GLRenderable} objects. The
 * {@link GraphRenderable} is the main such object which does the bulk of the
 * work. Subclasses of this processor may add other renderables to the renderer
 * using {@link #addRenderable}, but only before the processor is initialised
 * (usually adding them in the constructor is the best option). The only
 * additional renderable we add here is an {@link AxesRenderable} to display
 * coordinate axes in the top right corner of the canvas and a
 * {@link FPSRenderable} to display a frames-per-second counter in the bottom
 * right corner of the canvas (although this will be hidden by default).
 *
 * @author twilight_sparkle
 * @author antares
 */
public class GLVisualProcessor extends VisualProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(GLVisualProcessor.class.getName());

    public static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    public static final Cursor CROSSHAIR_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    // The OpenGL canvas
    protected final GLCanvas canvas;
    // The GLEventListener for the OpenGL canvas. Contains the processing logic to update the canvas throughout the OpenGL life-cycle.
    private final GLRenderer renderer;
    // The primary GLRenderable that performs the bulk of the visualisation. This renderable contains most of the actual logic to send data to the GL Context.
    private final GraphRenderable graphRenderable;
    private final Matrix44f modelViewMatrix = new Matrix44f();

    private Camera camera;
    private boolean updating = false;

    @Override
    protected final void destroyCanvas() {
        canvas.destroy();
    }

    /**
     * Tell this processor that it should draw the "Hit Test" visualisation as
     * well as the regular visualisation as part of each frame of the GL
     * life-cycle.
     * <p>
     * The hit-test visualisation is a hidden visualisation that gets drawn to
     * an off-screen openGL buffer. It fills the background with solid black and
     * then draws each element using a color whose red value corresponds to that
     * element's ID.
     * <p>
     * This method is intended to be called by subclass processors that utilise
     * this hit-test visualisation for some form of graph interaction. See the
     * <code>HitTester</code> class in the Interactive Graph package for a
     * canonical example.
     *
     * @param drawHitTest
     */
    protected final void setDrawHitTest(final boolean drawHitTest) {
        graphRenderable.setDrawHitTest(drawHitTest);
    }

    @Override
    public VisualOperation exportToImage(final File imageFile) {
        return new GLExportToImageOperation(imageFile);
    }

    @Override
    public VisualOperation exportToBufferedImage(final BufferedImage[] img1, final Semaphore waiter) {
        return new GLExportToBufferedImageOperation(img1, waiter);
    }

    /**
     * Retrieve the model view projection matrix currently being used for
     * visualisation. The projection matrix component is retrieved from the
     * {@link GLRenderer}.
     *
     * @return The MVP matrix this visual processor is using in its current
     * display cycle.
     */
    public final Matrix44f getDisplayModelViewProjectionMatrix() {
        final Matrix44f mvpMatrix = new Matrix44f();
        mvpMatrix.multiply(renderer.getProjectionMatrix(), modelViewMatrix);
        return mvpMatrix;
    }

    /**
     * Get the camera currently being used for visualisation.
     *
     * @return The Camera this visual processor is using in its current display
     * cycle.
     */
    public final Camera getDisplayCamera() {
        return camera;
    }

    /**
     * Sets the camera currently being used by this {@link VisualProcessor}.
     * <p>
     * This is only used by the {@link GraphRenderable} to ensure that the
     * Camera sent to the OpenGL context is in sync with the camera that can be
     * retrieved here using {@link #getDisplayCamera getDisplayCamera()}.
     *
     * @param camera
     */
    final void setDisplayCamera(final Camera camera) {
        this.camera = camera;
    }

    /**
     * Get the model view matrix currently being used for visualisation.
     *
     * @return The MV matrix this visual processor is using in its current
     * display cycle.
     */
    public final Matrix44f getDisplayModelViewMatrix() {
        return modelViewMatrix;
    }

    /**
     * Get the model view projection matrix corresponding to the supplied
     * camera. The projection matrix component is retrieved from the
     * {@link GLRenderer}.
     *
     * @param camera The {@link Camera} from which to calculate the model view
     * component of the matrix.
     * @return The MVP matrix corresponding to the supplied camera and the
     * current projection of the {@link GLRenderer}.
     */
    protected Matrix44f getCameraModelViewProjectionMatrix(final Camera camera) {
        final Matrix44f mvMatrix = Graphics3DUtilities.getModelViewMatrix(camera);
        final Matrix44f mvpMatrix = new Matrix44f();
        mvpMatrix.multiply(renderer.getProjectionMatrix(), mvMatrix);
        return mvpMatrix;
    }

    /**
     * Get the viewport (height, width, x, y) currently in use by the
     * {@link GLRenderer}.
     *
     * @return The viewport from the {@link GLRenderer}.
     */
    protected final int[] getViewport() {
        return renderer.viewport;
    }

    @Override
    public final void performVisualUpdate() {
        updating = true;
        canvas.display();
    }

    @Override
    protected void initialise() {
        if (graphRenderable.getGraphDisplayer() == null) {
            graphRenderable.setGraphDisplayer(new GraphDisplayer());
        }
        canvas.addGLEventListener(renderer);
        canvas.setSharedAutoDrawable(SharedDrawable.getSharedAutoDrawable());
    }

    /**
     * Tells the {@link GraphRenderable} to use the specified
     * {@link GraphDisplayer}.
     * <p>
     * A {@link GraphDisplayer} is used as an intermediate texture that the
     * {@link GraphRenderable} draws to which is then drawn to the screen.
     *
     * @param graphDisplayer The {@link GraphDisplayer} to use in the
     * {@link GraphRenderable}.
     */
    protected void setGraphDisplayer(final GraphDisplayer graphDisplayer) {
        if (!isInitialised) {
            graphRenderable.setGraphDisplayer(graphDisplayer);
        }
    }

    @Override
    protected void cleanup() {
        canvas.removeGLEventListener(renderer);
    }

    private final class GLExportToImageOperation implements VisualOperation {

        private final File file;

        public GLExportToImageOperation(final File file) {
            this.file = file;
        }

        @Override
        public void apply() {
            graphRenderable.addTask(drawable -> {
                final GL3 gl = drawable.getGL().getGL3();
                gl.glBindFramebuffer(GL3.GL_READ_FRAMEBUFFER, 0);
                final AWTGLReadBufferUtil util = new AWTGLReadBufferUtil(drawable.getGLProfile(), false);
                BufferedImage img = util.readPixelsToBufferedImage(gl, true);
                // Write the image out as a PNG.
                try {
                    ImageIO.write(img, "png", file);
                } catch (final IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
        }

        @Override
        public List<VisualChange> getVisualChanges() {
            return Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE).build());
        }
    }

    private final class GLExportToBufferedImageOperation implements VisualOperation {

        private final BufferedImage[] img1;
        private final Semaphore waiter;

        /**
         * Export the current GL display to a BufferedImage.
         * <p>
         * The VisualProcessor paradigm doesn't lend itself to returning data,
         * due to the asynchronous operation queue. Therefore, a single element
         * array is passed in and a reference to the newly created BufferedImage
         * is assigned at index 0. The caller maintains a reference to this
         * array to access the BufferedImage. (An array is used to avoid
         * creating yet another class with only one property.)
         * <p>
         * Because the operation is asynchronous, the caller needs to know when
         * the BufferedImage is ready. A Semaphore with zero permits is passed
         * in. The operation releases a permit after the BufferedImage is
         * assigned. The caller waits on the Semaphore and proceeds when a
         * permit is acquired.
         *
         * @param img1 A single element array; the new BufferedImage is assigned
         * to index 0.
         * @param waiter A Semaphore with no permits available; a permit is
         * released when the BufferedImage has been assigned.
         */
        public GLExportToBufferedImageOperation(final BufferedImage[] img1, final Semaphore waiter) {
            this.img1 = img1;
            this.waiter = waiter;
        }

        @Override
        public void apply() {
            graphRenderable.addTask(drawable -> {
                final GL3 gl = drawable.getGL().getGL3();
                gl.glBindFramebuffer(GL3.GL_READ_FRAMEBUFFER, 0);
                final AWTGLReadBufferUtil util = new AWTGLReadBufferUtil(drawable.getGLProfile(), false);
                img1[0] = util.readPixelsToBufferedImage(gl, true);

                waiter.release();
            });
        }

        @Override
        public List<VisualChange> getVisualChanges() {
            return Arrays.asList(new VisualChangeBuilder(VisualProperty.EXTERNAL_CHANGE).build());
        }
    }

    @Override
    protected void signalProcessorIdle() {
        super.signalProcessorIdle();
    }

    @Override
    protected void rebuild() {
        super.rebuild();
    }

    /**
     * Signal that the update phase of this processor has been completed (by the
     * {@link GLRenderer}).
     */
    final void signalUpdateComplete() {
        updating = false;
    }

    /**
     * Query whether or not this processor is currently in its update phase.
     * <p>
     * This method allows the {@link GLRenderer} to determine whether it has
     * entered the display phase of its GL life-cycle in response to an update
     * coordinated by this processor, or as a result of something internal, such
     * as a resizing of the GL canvas.
     *
     * @return Whether or not this processor is currently in its updating phase.
     */
    final boolean isUpdating() {
        return updating;
    }

    /**
     * Add the specified {@link GLRenderable} to this processor's renderer. If
     * this processor has already been initialised, the renderable is not added.
     * <p>
     * Typically this is used in the constructors of subclasses of this
     * processor.
     *
     * @param renderable The {@link GLRenderable} to add.
     */
    protected final void addRenderable(final GLRenderable renderable) {
        if (!isInitialised) {
            renderer.addRenderable(renderable);
        }
    }

    /**
     * Set the name of the buffer to draw the hit test to.
     * <p>
     * Note this shouldn't really go through the visual processor (nor be a
     * public method!). In the future hit tester implementations should probably
     * be their own renderables and they talk to the graph renderable directly
     * as needed.
     *
     * @param hitTestBufferName The GL name of the hit test buffer.
     */
    public void setHitTestFboName(final int hitTestBufferName) {
        graphRenderable.setHitTestFboName(hitTestBufferName);
    }

    /**
     * Construct a new GLVisualProcessor with a {@link GraphRenderable}, an
     * {@link AxesRenderable} and a {@link FPSRenderable}.
     * <p>
     * This processor will not use debugging or print GL capabilities.
     */
    public GLVisualProcessor() {
        this(false, false);
    }

    /**
     * Construct a new GLVisualProcessor with a {@link GraphRenderable} and an
     * {@link AxesRenderable} and a {@link FPSRenderable}.
     *
     * @param debugGl Whether or not to utilise a GLContext that includes
     * debugging.
     * @param printGlCapabilities Whether or not to print out a list of GL
     * capabilities upon initialisation.
     */
    public GLVisualProcessor(final boolean debugGl, final boolean printGlCapabilities) {
        graphRenderable = new GraphRenderable(this);
        final AxesRenderable axesRenderable = new AxesRenderable(this);
        final FPSRenderable fpsRenderable = new FPSRenderable(this);
        renderer = new GLRenderer(this, Arrays.asList(graphRenderable, axesRenderable, fpsRenderable), debugGl, printGlCapabilities);
        try {
            canvas = new GLCanvas(SharedDrawable.getGLCapabilities());
        } catch (final GLException ex) {
            GLInfo.respondToIncompatibleHardwareOrGL(null);
            throw ex;
        }
    }

    @Override
    protected Component getCanvas() {
        return canvas;
    }

    @Override
    public List<VisualChange> getFullRefreshSet(final VisualAccess access) {
        return Arrays.asList(
                new VisualChangeBuilder(VisualProperty.VERTICES_REBUILD).build(),
                new VisualChangeBuilder(VisualProperty.CONNECTIONS_REBUILD).build(),
                new VisualChangeBuilder(VisualProperty.BACKGROUND_COLOR).forItems(1).build(),
                new VisualChangeBuilder(VisualProperty.HIGHLIGHT_COLOR).forItems(1).build(),
                new VisualChangeBuilder(VisualProperty.CONNECTIONS_OPACITY).forItems(1).build(),
                new VisualChangeBuilder(VisualProperty.BLAZE_SIZE).forItems(1).build(),
                //                new VisualChangeBuilder(VisualProperty.DRAW_FLAGS).forItems(1).build(),
                new VisualChangeBuilder(VisualProperty.CAMERA).forItems(1).build()
        );
    }

    @Override
    protected final Set<VisualProperty> getTrumpedProperties(final VisualProperty property) {
        switch (property) {
            case VERTICES_REBUILD:
                return new HashSet<>(Arrays.asList(
                        VisualProperty.VERTEX_SELECTED, VisualProperty.VERTEX_X,
                        VisualProperty.VERTEX_COLOR, VisualProperty.VERTEX_FOREGROUND_ICON,
                        VisualProperty.VERTEX_BLAZED, VisualProperty.BOTTOM_LABELS_REBUILD,
                        VisualProperty.TOP_LABELS_REBUILD, VisualProperty.DRAW_FLAGS
                ));
            case CONNECTIONS_REBUILD:
                return new HashSet<>(Arrays.asList(
                        VisualProperty.CONNECTION_SELECTED, VisualProperty.CONNECTION_COLOR,
                        VisualProperty.CONNECTION_LABELS_REBUILD
                ));
            case CONNECTION_LABELS_REBUILD:
                return new HashSet<>(Arrays.asList(
                        VisualProperty.CONNECTION_LABEL_COLOR
                ));
            case TOP_LABELS_REBUILD:
                return new HashSet<>(Arrays.asList(
                        VisualProperty.TOP_LABEL_COLOR
                ));
            case BOTTOM_LABELS_REBUILD:
                return new HashSet<>(Arrays.asList(
                        VisualProperty.BOTTOM_LABEL_COLOR
                ));
            default:
                return super.getTrumpedProperties(property);
        }
    }

    @Override
    protected final VisualProperty getMasterProperty(final VisualProperty property) {

        switch (property) {
            case BLAZE_SIZE:
            case BLAZE_OPACITY:
                return VisualProperty.BLAZE_SIZE;
            case VISIBLE_ABOVE_THRESHOLD:
            case VISIBILITY_THRESHOLD:
                return VisualProperty.DRAW_FLAGS;
            case VERTEX_SELECTED:
            case VERTEX_DIM:
                return VisualProperty.VERTEX_SELECTED;
            case VERTEX_RADIUS:
            case VERTEX_X:
            case VERTEX_Y:
            case VERTEX_Z:
            case VERTEX_X2:
            case VERTEX_Y2:
            case VERTEX_Z2:
                return VisualProperty.VERTEX_X;
            case VERTEX_COLOR:
                return VisualProperty.VERTEX_COLOR;
            case VERTEX_FOREGROUND_ICON:
            case VERTEX_BACKGROUND_ICON:
            case VERTEX_NW_DECORATOR:
            case VERTEX_NE_DECORATOR:
            case VERTEX_SW_DECORATOR:
            case VERTEX_SE_DECORATOR:
                return VisualProperty.VERTEX_FOREGROUND_ICON;
            case VERTEX_BLAZED:
            case VERTEX_BLAZE_ANGLE:
            case VERTEX_BLAZE_COLOR:
                return VisualProperty.VERTEX_BLAZED;
            case VERTEX_VISIBILITY:
            case VERTICES_ADDED:
            case VERTICES_REMOVED:
            case VERTICES_REBUILD:
                return VisualProperty.VERTICES_REBUILD;
            case CONNECTION_COLOR:
                return VisualProperty.CONNECTION_COLOR;
            case CONNECTION_SELECTED:
            case CONNECTION_DIRECTED:
            case CONNECTION_DIM:
            case CONNECTION_LINESTYLE:
                return VisualProperty.CONNECTION_SELECTED;
            case CONNECTION_VISIBILITY:
            case CONNECTION_WIDTH:
            case CONNECTIONS_ADDED:
            case CONNECTIONS_REMOVED:
            case CONNECTIONS_REBUILD:
                return VisualProperty.CONNECTIONS_REBUILD;
            case TOP_LABEL_SIZE:
            case TOP_LABELS_REBUILD:
            case TOP_LABEL_TEXT:
                return VisualProperty.TOP_LABELS_REBUILD;
            case BOTTOM_LABEL_SIZE:
            case BOTTOM_LABELS_REBUILD:
            case BOTTOM_LABEL_TEXT:
                return VisualProperty.BOTTOM_LABELS_REBUILD;
            case CONNECTION_LABEL_SIZE:
            case CONNECTION_LABELS_REBUILD:
            case CONNECTION_LABEL_TEXT:
                return VisualProperty.CONNECTION_LABELS_REBUILD;
            default:
                return super.getMasterProperty(property);
        }

    }

    @Override
    protected final VisualChangeProcessor getChangeProcessor(final VisualProperty property) {
        // If certain changes requried other renderables to be updated, eg. an attribute that set the size of the axes to draw, we could delgeate that here rather than this being a trivial operation.
        return graphRenderable.getChangeProcessor(property);
    }

    /**
     * Windows-DPI-Scaling
     *
     * This function is only needed by the fix for Windows DPI scaling to get
     * access to the GLCanvas which is a protected member. If JOGL is ever
     * updated to fix Windows DPI scaling this function should be removed.
     */
    public float getDPIScaleY() {
        return (float) ((Graphics2D) (canvas).getGraphics()).getTransform().getScaleY();
    }

}
