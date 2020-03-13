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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;

/**
 * Provide asymetric frustums for a stereo anaglyphic view of the world.
 * <p>
 * See
 * http://quiescentspark.blogspot.com/2011/05/rendering-3d-anaglyph-in-opengl.html.
 *
 * @author algol
 */
public class StereoCamera {

    private final float convergence;
    private final float eyeSeparation;
    private final float aspectRatio;
    private final float fov;
    private final float fovRadians;
    private final float nearClippingDistance;
    private final float farClippingDistance;

    private Frustum frustum;
    private Matrix44f translation;

    public StereoCamera(
            final float convergence,
            final float eyeSeparation,
            final float aspectRatio,
            final float fov,
            final float nearClippingDistance,
            final float farClippingDistance) {
        this.convergence = convergence;
        this.eyeSeparation = eyeSeparation;
        this.aspectRatio = aspectRatio;
        this.fov = fov;
        this.fovRadians = (float) Mathf.degToRad(fov);
        this.nearClippingDistance = nearClippingDistance;
        this.farClippingDistance = farClippingDistance;
    }

    public Matrix44f applyLeftFrustum(final Matrix44f mv) {
        final float sep = eyeSeparation / 2f;

        final float top = nearClippingDistance * (float) Math.tan(fovRadians / 2f);
        final float bottom = -top;

        final float a = aspectRatio * (float) Math.tan(fovRadians / 2f) * convergence;

        final float b = a - sep;
        final float c = a + sep;

        final float left = -c * nearClippingDistance / convergence;
        final float right = b * nearClippingDistance / convergence;

        // Set the projection.
        frustum = new Frustum(fov, aspectRatio, left, right, bottom, top, nearClippingDistance, farClippingDistance);

        // Displace the world to the left.
        translation = new Matrix44f();
        translation.makeTranslationMatrix(-sep, 0, 0);
        Matrix44f t2 = new Matrix44f();
        t2.multiply(mv, translation);
//        t.multiply(frustum.getProjectionMatrix(), t2);

        return t2;
    }

    public Matrix44f applyRightFrustum(final Matrix44f mv) {
        final float sep = eyeSeparation / 2f;

        final float top = nearClippingDistance * (float) Math.tan(fovRadians / 2f);
        final float bottom = -top;

        final float a = aspectRatio * (float) Math.tan(fovRadians / 2f) * convergence;

        final float b = a - sep;
        final float c = a + sep;

        final float left = -b * nearClippingDistance / convergence;
        final float right = c * nearClippingDistance / convergence;

        // Set the projection.
        frustum = new Frustum(fov, aspectRatio, left, right, bottom, top, nearClippingDistance, farClippingDistance);

        // Displace the world to the right.
        translation = new Matrix44f();
        translation.makeTranslationMatrix(sep, 0, 0);
        Matrix44f t2 = new Matrix44f();
        t2.multiply(translation, mv);
//        t.multiply(frustum.getProjectionMatrix(), t2);

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
        Matrix44f t2 = new Matrix44f();
        t2.multiply(translation, mv);
        Matrix44f mvp = new Matrix44f();
        mvp.multiply(frustum.getProjectionMatrix(), t2);

        return mvp;
    }
}
