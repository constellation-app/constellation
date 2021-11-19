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

import au.gov.asd.tac.constellation.preferences.DeveloperPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.gui.InfoTextPanel;
import au.gov.asd.tac.constellation.visual.opengl.renderer.batcher.FpsBatcher;
import au.gov.asd.tac.constellation.visual.opengl.utilities.RenderException;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;

/**
 * Encapsulate the JOGL code required to render a frames-per-second count for
 * the {@link GraphRenderable}.
 *
 * @author cygnus_x-1
 * @author twilight_sparkle
 */
public class FPSRenderable implements GLRenderable {
    
    private static final Logger LOGGER = Logger.getLogger(FPSRenderable.class.getName());

    private static final int FPS_OFFSET = 50;
    private static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
    private static final Matrix44f IDENTITY_44F = Matrix44f.identity();
    private static final Camera CAMERA = new Camera();

    private final GLVisualProcessor parent;
    private final FpsBatcher fpsBatcher = new FpsBatcher();
    private final Vector3f bottomRightCorner = new Vector3f();
    private float pixelDensity = 0;
    private float pyScale = 0;
    private float pxScale = 0;

    private final boolean enabled;
    private long start = 0;
    private long fps = 0;
    private long countFps = 0;

    public FPSRenderable(final GLVisualProcessor parent) {
        this.parent = parent;

        final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
        this.enabled = prefs.getBoolean(DeveloperPreferenceKeys.DISPLAY_FRAME_RATE,
                DeveloperPreferenceKeys.DISPLAY_FRAME_RATE_DEFAULT);
    }

    @Override
    public int getPriority() {
        return RenderablePriority.ANNOTATIONS_PRIORITY.getValue();
    }

    @Override
    public void init(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();

        try {
            fpsBatcher.createShader(gl);
            fpsBatcher.createBatch(null).run(gl);
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
            final NotifyDescriptor.Message nd = new NotifyDescriptor.Message(itp,
                    NotifyDescriptor.ERROR_MESSAGE);
            nd.setTitle("Shader Error");
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private float calculateXProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        final Vector3f unitPosition = new Vector3f(1, 0, 0);
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float xScale = proj2.getX() - proj1.getX();
        return (256.0F / 64) / xScale;
    }

    private float calculateYProjectionScale(final int[] viewport) {
        // calculate the number of pixels a scene object of y-length 1 projects to.
        final Vector4f proj1 = new Vector4f();
        Graphics3DUtilities.project(ZERO_3F, IDENTITY_44F, viewport, proj1);
        final Vector4f proj2 = new Vector4f();
        final Vector3f unitPosition = new Vector3f(0, 1, 0);
        Graphics3DUtilities.project(unitPosition, IDENTITY_44F, viewport, proj2);
        final float yScale = proj2.getY() - proj1.getY();
        return (256.0F / 64) / yScale;
    }

    @Override
    public void reshape(final int x, final int y, final int width, final int height) {

        // whenever the drawable shape changes, recalculate the place where the fps is drawn
        final int[] viewport = new int[]{x, y, width, height};
        final int dx = width / 2 - FPS_OFFSET;
        final int dy = height / 2 - FPS_OFFSET;
        pxScale = calculateXProjectionScale(viewport);
        pyScale = calculateYProjectionScale(viewport);
        Graphics3DUtilities.moveByProjection(ZERO_3F, IDENTITY_44F, viewport, dx, dy, bottomRightCorner);

        // set the number of pixels per world unit at distance 1
        pixelDensity = (float) (height * 0.5 / Math.tan(Math.toRadians(GLRenderer.FIELD_OF_VIEW)));
    }

    @Override
    public void display(final GLAutoDrawable drawable, final Matrix44f pMatrix) {
        if (start == 0) {
            start = System.currentTimeMillis();
        }

        if ((System.currentTimeMillis() - start) >= 500) {
            fps = countFps << 1;
            start = 0;
            countFps = 0;
        }

        countFps++;

        if (enabled) {
            final GL3 gl = drawable.getGL().getGL3();

            // extract and scale the rotation matrix from the mvp matrix
//            final Matrix44f rotationMatrix = new Matrix44f();
//            parent.getDisplayModelViewProjectionMatrix().getRotationMatrix(rotationMatrix);
            final Matrix44f scalingMatrix = new Matrix44f();
            scalingMatrix.makeScalingMatrix(pxScale, pyScale, 0);
            final Matrix44f srMatrix = new Matrix44f();
            srMatrix.multiply(scalingMatrix, IDENTITY_44F);
//            srMatrix.multiply(scalingMatrix, rotationMatrix);

            // build the fps matrix by translating the sr matrix
            final Matrix44f translationMatrix = new Matrix44f();
            translationMatrix.makeTranslationMatrix(bottomRightCorner.getX(),
                    bottomRightCorner.getY(), bottomRightCorner.getZ());
            final Matrix44f fpsMatrix = new Matrix44f();
            fpsMatrix.multiply(translationMatrix, srMatrix);

            // disable depth so the fps counter is drawn on top
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            // draw the fps counter
            int[] fpsDigits = Long.toString(fps).chars().map(c -> c -= '0').toArray();
            if (fpsDigits.length < 2) {
                fpsDigits = new int[]{0, fpsDigits[0]};
            }
            fpsBatcher.setPixelDensity(pixelDensity);
            fpsBatcher.setProjectionScale(pyScale);
            fpsBatcher.updateColors(ConstellationColor.YELLOW).run(gl);
            fpsBatcher.updateIcons(fpsDigits).run(gl);
            fpsBatcher.drawBatch(gl, CAMERA, fpsMatrix, pMatrix, false);

            // re-enable depth
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(true);
        }
    }

    @Override
    public void dispose(final GLAutoDrawable drawable) {
        final GL3 gl = drawable.getGL().getGL3();
        fpsBatcher.disposeBatch().run(gl);
    }
}
