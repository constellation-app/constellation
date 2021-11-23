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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Various utilities for matrix manipulations pertaining to the rendering of 3D
 * graphics.
 *
 * @author algol
 */
public class Graphics3DUtilities {

    private static final Logger LOGGER = Logger.getLogger(Graphics3DUtilities.class.getName());
    
    private Graphics3DUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts the supplied point from screen coordinates to world coordinates,
     * placing it on the plane through the supplied position (in world
     * coordinates) normal to the direction the camera is looking
     *
     * @param screenPosition The point in screen coordinates (the z component is
     * ignored as screen coordinates are 2D)
     * @param throughPoint A point on the plane to place the screen position
     * onto.
     * @param modelViewProjectionMatrix The matrix which defines the mapping
     * from screen to world coordinates.
     * @param viewport
     * @param worldPosition
     */
    public static void screenToWorldCoordinates(final Vector3f screenPosition, final Vector3f throughPoint, final Matrix44f modelViewProjectionMatrix, final int[] viewport, final Vector3f worldPosition) {
        // Need to screenToWorld the window location into 3D space
        // To screenToWorld, we need Z and W. Instead of making them up, generate them by projecting the throughPoint.
        final Vector4f proj = new Vector4f();
        Graphics3DUtilities.project(throughPoint, modelViewProjectionMatrix, viewport, proj);
        final Vector4f screenLocation = new Vector4f(screenPosition.getX(), viewport[3] - screenPosition.getY(), proj.getZ(), proj.getW());
        Graphics3DUtilities.unproject(screenLocation, modelViewProjectionMatrix, viewport, worldPosition);
    }

    public static Matrix44f getModelViewMatrix(final Camera camera) {
        Matrix44f mat = new Matrix44f();
        getModelViewMatrix(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp, mat);
        return mat;
    }

    public static void getModelViewMatrix(final Vector3f eye, final Vector3f center, final Vector3f up, final Matrix44f result) {
        final Vector3f f = Vector3f.subtract(center, eye);
        f.normalize();

        final Vector3f up2 = new Vector3f(up);
        up2.normalize();

        final Vector3f s = new Vector3f();
        s.crossProduct(f, up2);

        final Vector3f u = new Vector3f();
        u.crossProduct(s, f);

        final Matrix44f modelView = new Matrix44f();
        modelView.set(0, 0, s.getX());
        modelView.set(1, 0, s.getY());
        modelView.set(2, 0, s.getZ());
        modelView.set(3, 0, 0);
        modelView.set(0, 1, u.getX());
        modelView.set(1, 1, u.getY());
        modelView.set(2, 1, u.getZ());
        modelView.set(3, 1, 0);
        modelView.set(0, 2, -f.getX());
        modelView.set(1, 2, -f.getY());
        modelView.set(2, 2, -f.getZ());
        modelView.set(3, 2, 0);
        modelView.set(0, 3, 0);
        modelView.set(1, 3, 0);
        modelView.set(2, 3, 0);
        modelView.set(3, 3, 1);

        final Matrix44f scaleMatrix = new Matrix44f();
        scaleMatrix.makeTranslationMatrix(-eye.getX(), -eye.getY(), -eye.getZ());
        result.multiply(modelView, scaleMatrix);
    }

    /**
     * Project a position to the viewing plane.
     * <p>
     * See 2.13 Coordinate Transformations in "OpenGL 3.3 (Core Profile)".
     *
     * @param position the position of the eye.
     * @param modelViewProjectionMatrix the model view projection matrix.
     * @param viewport the viewport.
     * @param projectedPosition the projection position.
     * @return true if the projection was successful.
     */
    public static boolean project(final Vector3f position, final Matrix44f modelViewProjectionMatrix, final int[] viewport, final Vector4f projectedPosition) {
        final Matrix44f mvpmat = new Matrix44f();
        mvpmat.set(modelViewProjectionMatrix);
        final float[] tr = mvpmat.multiply(position.getX(), position.getY(), position.getZ(), 1.0F);
        if (tr[3] == 0) {
            return false;
        }

        final float w = tr[3];
        tr[0] /= w;
        tr[1] /= w;
        tr[2] /= w;

        // At this point, the projected coordinates (tr) are normalised to the unit cube.
        float winx = viewport[0] + (viewport[2] * (tr[0] + 1.0F)) / 2.0F;
        float winy = viewport[1] + (viewport[3] * (tr[1] + 1.0F)) / 2.0F;
        float winz = (tr[2] + 1.0F) / 2.0F;

        // Now we're projected to the window.
        projectedPosition.set(winx, winy, winz, w);

        return true;
    }

    public static boolean unproject(final Vector4f position, final Matrix44f modelViewProjectionMatrix, final int[] viewport, final Vector3f unprojectedPosition) {
        final float winx = position.a[0];
        final float winy = position.a[1];
        final float winz = position.a[2];
        final float w = position.a[3];

        // Start unprojecting the window coordinates.
        final float[] tr = new float[4];
        tr[0] = (winx - viewport[0]) * 2.0F / viewport[2] - 1.0F;
        tr[1] = (winy - viewport[1]) * 2.0F / viewport[3] - 1.0F;
        tr[2] = winz * 2.0F - 1.0F;
        tr[3] = w;

        tr[0] *= w;
        tr[1] *= w;
        tr[2] *= w;

        Matrix44f invmat = new Matrix44f();
        invmat.invert(modelViewProjectionMatrix);
        final float[] untr = invmat.multiply(tr[0], tr[1], tr[2], tr[3]);

        // And we're done.
        unprojectedPosition.set(untr[0], untr[1], untr[2]);

        return true;
    }

    /**
     * Move a position in window coordinates by projecting the movement vector
     * from window coordinates to world coordinates.
     *
     * @param position
     * @param modelViewProjectionMatrix
     * @param viewport
     * @param movement
     * @param newposition
     */
    public static void moveByProjection(final Vector3f position, final Matrix44f modelViewProjectionMatrix, final int[] viewport, final Vector3f movement, final Vector3f newposition) {
        final float[] cameraMovement = new float[3];

        final float w = modelViewProjectionMatrix.multiply(position.getX(), position.getY(), position.getZ(), 1)[3];
        cameraMovement[0] = movement.getX() * 2.0F * w / viewport[2];
        cameraMovement[1] = -movement.getY() * 2.0F * w / viewport[3];
        cameraMovement[2] = movement.getZ() * 2.0F * w;

        final Matrix44f invmat = new Matrix44f();
        invmat.invert(modelViewProjectionMatrix);
        final float[] worldMovement = invmat.multiply(cameraMovement[0], cameraMovement[1], cameraMovement[2], 0);
        newposition.set(worldMovement[0], worldMovement[1], worldMovement[2]);

        LOGGER.log(Level.FINE, "movement: {0}", movement);
        LOGGER.log(Level.FINE, "viewport: {0},{1},{2},{3}", new Object[]{viewport[0], viewport[1], viewport[2], viewport[3]});
        LOGGER.log(Level.FINE, "mvp: {0}", modelViewProjectionMatrix);
        LOGGER.log(Level.FINE, "w: {0}", w);
        LOGGER.log(Level.FINE, "cameraMovement: {0},{1},{2}", new Object[]{cameraMovement[0], cameraMovement[1], cameraMovement[2]});
        LOGGER.log(Level.FINE, "invmat: {0}", invmat);
        LOGGER.log(Level.FINE, "worldMovement: {0},{1},{2}", new Object[]{worldMovement[0], worldMovement[1], worldMovement[2]});
    }

    /**
     * Move a position in window coordinates by projecting, moving, then
     * unprojecting.
     *
     * @param position The position to move.
     * @param modelViewProjectionMatrix The model-view-projection matrix for
     * projecting/unprojecting.
     * @param viewport The current window viewport.
     * @param deltaX The window Δx.
     * @param deltaY The window Δy.
     * @param newposition The returned new position.
     *
     * @return True if the calculation could be made, false otherwise (eg w==0).
     */
    public static boolean moveByProjection(final Vector3f position, final Matrix44f modelViewProjectionMatrix, final int[] viewport, final int deltaX, final int deltaY, final Vector3f newposition) {
        // To avoid playing with large numbers, we move the fixed position (0,0,0),
        // then add the original position at the end.
        //        final Matrix44f mvpmat = new Matrix44f();
//        mvpmat.set(modelViewProjectionMatrix);
//        final float[] tr = mvpmat.multiply(0f, 0f, 0f, 1.0f);
        final float[] tr = modelViewProjectionMatrix.multiply(0F, 0F, 0F, 1.0F);
        if (tr[3] == 0) {
            return false;
        }

        final float w = tr[3];
        tr[0] /= w;
        tr[1] /= w;
        tr[2] /= w;

        // At this point, the projected coordinates (tr) are normalised to the unit cube.
        float winx = viewport[0] + (viewport[2] * (tr[0] + 1.0F)) / 2.0F;
        float winy = viewport[1] + (viewport[3] * (tr[1] + 1.0F)) / 2.0F;
        float winz = (tr[2] + 1.0F) / 2.0F;

        // Now we're projected to the window.
        // Add the delta from the before and after mouse movement.
        // Note that the ydelta is substracted: OpenGL and Windows have opposite y axes.
        winx += deltaX;
        winy -= deltaY;

        // Start unprojecting the window coordinates.
        tr[0] = (winx - viewport[0]) * 2.0F / viewport[2] - 1.0F;
        tr[1] = (winy - viewport[1]) * 2.0F / viewport[3] - 1.0F;
        tr[2] = winz * 2.0F - 1.0F;

        tr[0] *= w;
        tr[1] *= w;
        tr[2] *= w;

        final Matrix44f invmat = new Matrix44f();
        invmat.invert(modelViewProjectionMatrix);
        final float[] untr = invmat.multiply(tr[0], tr[1], tr[2], tr[3]);
        untr[0] += position.getX();
        untr[1] += position.getY();
        untr[2] += position.getZ();

        // And we're done.
        newposition.set(untr[0], untr[1], untr[2]);

        return true;
    }

    /**
     * An implementation of the GLSL <tt>mix()</tt> function.
     *
     * @param v1 A Vector3f.
     * @param v2 Another Vector3f.
     * @param a The mix parameter (generally a number from 0 to 1).
     *
     * @return A Vector3f containing the mix of the two input Vector3f values.
     */
    public static Vector3f mix(final Vector3f v1, final Vector3f v2, final float a) {
        final float a1 = 1 - a;

        return new Vector3f(
                v1.getX() * a1 + v2.getX() * a,
                v1.getY() * a1 + v2.getY() * a,
                v1.getZ() * a1 + v2.getZ() * a
        );
    }

    public static float mix(final float f1, final float f2, final float a) {
        final float a1 = 1 - a;

        return f1 * a1 + f2 * a;
    }

    /**
     * An implementation of the GLSL <tt>clamp()</tt> function.
     *
     * @param x the value to clamp.
     * @param minVal the minimum value.
     * @param maxVal the maximum value.
     *
     * @return min (max (x, minVal), maxVal).
     */
    public static int clamp(final int x, final int minVal, final int maxVal) {
        return Math.min(Math.max(x, minVal), maxVal);
    }

    /**
     * An implementation of the GLSL <tt>clamp()</tt> function.
     *
     * @param x the value to clamp.
     * @param minVal the minimum value.
     * @param maxVal the maximum value.
     *
     * @return min (max (x, minVal), maxVal).
     */
    public static float clamp(final float x, final float minVal, final float maxVal) {
        return Math.min(Math.max(x, minVal), maxVal);
    }

    /**
     * An implementation of the GLSL <tt>distance</tt> function.
     *
     * @param p0 A Vector3f.
     * @param p1 A Vector3f.
     *
     * @return The distance between p0 and p1.
     */
    public static float distance(final Vector3f p0, final Vector3f p1) {
        final Vector3f delta = Vector3f.subtract(p0, p1);

        return delta.getLength();
    }
}
