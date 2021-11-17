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

import au.gov.asd.tac.constellation.utilities.VersionUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.visual.opengl.utilities.GLTools;
import au.gov.asd.tac.constellation.visual.opengl.utilities.RenderException;
import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * OpenGL renderer for a {@link GLVisualProcessor}.
 * <p>
 * This class implements a JOGL GLEventListener to coordinate the visualisation
 * of a graph through several {@link GLRenderable} objects.
 * <p>
 * The life cycle of a GLEventListener is:
 * <ul>
 * <li><code>init()</code>
 * <li><code>reshape()</code>
 * <li><code>display() | reshape()</code>
 * <li><code>dispose()</code>
 * </ul>
 * <p>
 * In each stage, this renderer will call the corresponding methods for each
 * {@link GLRenderable} it is coordinating (in an order based on
 * {@link GLRenderable#getPriority}, with ties decided by the order that the
 * renderables are added). The <code>display()</code> phase will first call
 * <code>update()</code> for each renderable, then tell the visual processor
 * that updating is complete, and finally call <code>display()</code> for each
 * renderable.
 * <p>
 * Note that any stage of this life-cycle may be kicked off manually by the
 * corresponding {@link GLVisualProcessor}, or as the underlying window is
 * resized, destroyed or recreated.
 *
 * @author algol, twilight_sparkle
 */
public final class GLRenderer implements GLEventListener {

    static final float FIELD_OF_VIEW = 35;
    private static final float PERSPECTIVE_NEAR = 1;
    private static final float PERSPECTIVE_FAR = 500000;

    private final List<GLRenderable> renderables;

    private boolean initialised = false;

    // Matrices for 3D manipulation.
    private final Frustum viewFrustum;
    // Remember the viewport for dragging.
    // x, y, width, height
    final int[] viewport = new int[4];
    private final Matrix44f projectionMatrix;

    // Flags for various forms of debugging. Do not change for a give graph instance.
    private final boolean debugGl;
    private final boolean printGlCapabilities;
    private final GLVisualProcessor parent;

    /**
     * Create a new GLRenderer.
     *
     * @param parent The visual processor this renderer belongs to
     * @param renderables The (initial) list of renderables this renderer will
     * coordinate visualisation for.
     * @param debugGl Whether or not to utilise a GLContext that includes
     * debugging.
     * @param printGlCapabilities Whether or not to print out a list of GL
     * capabilities upon initialisation.
     */
    public GLRenderer(final GLVisualProcessor parent, final List<GLRenderable> renderables, final boolean debugGl, final boolean printGlCapabilities) {
        this.parent = parent;
        this.renderables = new ArrayList<>(renderables);
        this.debugGl = debugGl;
        this.printGlCapabilities = printGlCapabilities;
        viewFrustum = new Frustum();
        projectionMatrix = new Matrix44f();
    }

    /**
     * Adds the specified {@link GLRenderable} to this renderer. Can only be
     * done before initialisation.
     *
     * @param renderable The {@link GLRenderable} to add.
     */
    final void addRenderable(final GLRenderable renderable) {
        if (initialised) {
            throw new RenderException("Cant add a renderable after renderer is initialised");
        }
        renderables.add(renderable);
    }

    @Override
    public void init(final GLAutoDrawable drawable) {

        initialised = true;
        renderables.sort(Comparator.naturalOrder());

        if (debugGl) {
            drawable.setGL(new DebugGL3(drawable.getGL().getGL3()));
        }
        final GL3 gl = drawable.getGL().getGL3();

        //Look for Graphics cards that work with GL3, but not 3.3.
        //Graphics cards that don't work with GL3 will raise an exception.
        final String thisversion = gl.glGetString(GL.GL_VERSION);

        if (!VersionUtilities.doesVersionMeetMinimum(thisversion, GLInfo.MINIMUM_OPEN_GL_VERSION)) {
            GLInfo.respondToIncompatibleHardwareOrGL(drawable);
        }

        if (printGlCapabilities) {
            GLInfo.printGLCapabilities(gl);
        }

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);

        renderables.forEach(renderable -> {
            renderable.init(drawable);
        });

        // Reset to the default framebuffer.
        gl.glBindFramebuffer(GL.GL_DRAW_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        renderables.forEach(renderable -> {
            renderable.dispose(drawable);
        });
    }

    @Override
    public void display(final GLAutoDrawable drawable) {
        renderables.forEach(renderable -> {
            renderable.update(drawable);
        });
        parent.signalUpdateComplete();
        renderables.forEach(renderable -> {
            renderable.display(drawable, projectionMatrix);
        });
        parent.signalProcessorIdle();
    }

    @Override
    public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
        final GL3 gl = drawable.getGL().getGL3();

        //  Windows-DPI-Scaling
        //
        // If JOGL is ever fixed or another solution is found, either change
        // needsManualDPIScaling to return false (so there is effectively no
        // DPI scaling here) or remove the scaled height and width below.         
        float dpiScaleX = 1.0F;
        float dpiScaleY = 1.0F;
        if (GLTools.needsManualDPIScaling()) {
            dpiScaleX = (float) ((Graphics2D) (parent.canvas).getGraphics()).getTransform().getScaleX();
            dpiScaleY = (float) ((Graphics2D) (parent.canvas).getGraphics()).getTransform().getScaleY();
        }

        // These need to be final as they are used in the lambda function below
        final int dpiScaledWidth = (int) (width * dpiScaleX);
        final int dpiScaledHeight = (int) (height * dpiScaleY);

        gl.glViewport(0, 0, dpiScaledWidth, dpiScaledHeight);

        // Create the projection matrix, and load it on the projection matrix stack.
        viewFrustum.setPerspective(FIELD_OF_VIEW, (float) dpiScaledWidth / (float) dpiScaledHeight, PERSPECTIVE_NEAR, PERSPECTIVE_FAR);

        projectionMatrix.set(viewFrustum.getProjectionMatrix());

        // A GLCanvas sets its minimum size to the preferred size when its redrawn. This means it will get bigger,
        // but never get smaller. Explicitly set the minimum size to get around this.
        ((Component) drawable).setMinimumSize(new Dimension(0, 0));

        renderables.forEach(renderable -> {
            renderable.reshape(x, y, dpiScaledWidth, dpiScaledHeight);
        });

        viewport[0] = x;
        viewport[1] = y;
        viewport[2] = dpiScaledWidth;
        viewport[3] = dpiScaledHeight;
    }

    /**
     * Get the projection matrix that this renderer is currently using.
     *
     * @return The current projection matrix.
     */
    Matrix44f getProjectionMatrix() {
        return projectionMatrix;
    }
}
