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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;

/**
 * Provide asymetric frustums for an anaglyphic view of the world.
 * <p>
 * The human visual system needs depth cues from a flat image (photograph or
 * display-screen) in terms of how much an object shifts laterally between the
 * left eye and the right eye. When we say parallax, we mean exactly this kind
 * of displacement in the image. In rendering an anaglyph, all that we're trying
 * to achieve is to get the right kind of parallax for the objects in the scene
 * and the rest is automatically done in the brain, for free!
 * <p>
 * Parallax is not just qualitative, it has a numeric value and can be positive,
 * negative or zero. In the application, parallax is created by defining two
 * cameras corresponding to the left and right eyes separated by some distance
 * (called interocular distance or simply eye-separation) and having a plane at
 * a certain depth along the viewing direction (called convergence distance) at
 * which the parallax is zero. Objects at the convergence depth will appear to
 * be at the same depth as the screen. Objects closer to the camera than the
 * convergence distance will seem to be out-of-screen and objects further in
 * depth than the convergence distance will appear inside the screen.
 * <p>
 * See
 * http://quiescentspark.blogspot.com/2011/05/rendering-3d-anaglyph-in-opengl.html.
 * <p>
 * Because the anaglyphic view works by
 *
 * @author algol
 */
public class AnaglyphCamera {

    private final float convergence;
    private final float eyeSeparation;
    private final float aspectRatio;
    private final float fov;
    private final float fovRadians;
    private final float nearClippingDistance;
    private final float farClippingDistance;

    private Frustum frustum;
    private Matrix44f translation;

    /**
     *
     * @param convergence
     * @param eyeSeparation
     * @param aspectRatio
     * @param fov
     * @param nearClippingDistance
     * @param farClippingDistance
     */
    public AnaglyphCamera(final float convergence, final float eyeSeparation, final float aspectRatio,
            final float fov, final float nearClippingDistance, final float farClippingDistance) {
        this.convergence = convergence;
        this.eyeSeparation = eyeSeparation;
        this.aspectRatio = aspectRatio;
        this.fov = fov;
        this.fovRadians = (float) Mathf.degToRad(fov);
        this.nearClippingDistance = nearClippingDistance;
        this.farClippingDistance = farClippingDistance;
    }

    public Matrix44f applyLeftFrustum(final Matrix44f mv) {
        final float sep = eyeSeparation / 2F;

        final float top = nearClippingDistance * (float) Math.tan(fovRadians / 2F);
        final float bottom = -top;

        final float a = aspectRatio * (float) Math.tan(fovRadians / 2F) * convergence;

        final float b = a - sep;
        final float c = a + sep;

        final float left = -c * nearClippingDistance / convergence;
        final float right = b * nearClippingDistance / convergence;

        // Set the projection.
        frustum = new Frustum(fov, aspectRatio, left, right, bottom, top, nearClippingDistance, farClippingDistance);

        // Displace the world to the left.
        translation = new Matrix44f();
        translation.makeTranslationMatrix(-sep, 0, 0);
        final Matrix44f t2 = new Matrix44f();
        t2.multiply(mv, translation);

        return t2;
    }

    public Matrix44f applyRightFrustum(final Matrix44f mv) {
        final float sep = eyeSeparation / 2F;

        final float top = nearClippingDistance * (float) Math.tan(fovRadians / 2F);
        final float bottom = -top;

        final float a = aspectRatio * (float) Math.tan(fovRadians / 2F) * convergence;

        final float b = a - sep;
        final float c = a + sep;

        final float left = -b * nearClippingDistance / convergence;
        final float right = c * nearClippingDistance / convergence;

        // Set the projection.
        frustum = new Frustum(fov, aspectRatio, left, right, bottom, top, nearClippingDistance, farClippingDistance);

        // Displace the world to the right.
        translation = new Matrix44f();
        translation.makeTranslationMatrix(sep, 0, 0);
        final Matrix44f t2 = new Matrix44f();
        t2.multiply(translation, mv);

        return t2;
    }

    /**
     *
     * @return Projection matrix for current eye.
     */
    public Matrix44f getProjectionMatrix() {
        return frustum.getProjectionMatrix();
    }

    /**
     * Returns the model-view-projection matrix for current eye.
     *
     * @param mv the transformation matrix of the camera.
     * @return the model-view-projection matrix for current eye.
     */
    public Matrix44f getMvpMatrix(final Matrix44f mv) {
        final Matrix44f t2 = new Matrix44f();
        t2.multiply(translation, mv);
        final Matrix44f mvp = new Matrix44f();
        mvp.multiply(frustum.getProjectionMatrix(), t2);

        return mvp;
    }
}
