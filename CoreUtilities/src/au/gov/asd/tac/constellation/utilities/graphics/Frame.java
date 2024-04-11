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
package au.gov.asd.tac.constellation.utilities.graphics;

import java.io.Serializable;

/**
 * The GLFrame (OrthonormalFrame) class. Possibly the most useful little piece
 * of 3D graphics code for OpenGL environments.
 * <p>
 * Ported to Java from the original OpenGL Bible C++ code.
 *
 * @author algol
 */
public final class Frame implements Serializable {

    private final Vector3f origin;  // Where am I?
    private final Vector3f forward; // Where am I going?
    private final Vector3f up;      // Which way is up?

    private final float absm;

    /**
     * Construct a new Frame.
     */
    public Frame() {
        // At origin.
        origin = new Vector3f(0.0F, 0.0F, 0.0F);

        // Forward is -Z (default OpenGL).
        forward = new Vector3f(0.0F, 0.0F, -1.0F);

        // Up is up (+Y).
        up = new Vector3f(0.0F, 1.0F, 0.0F);

        absm = -1;
    }

    /**
     * Construct a new Frame from an existing Frame using a deep copy.
     *
     * @param frame The original Frame.
     */
    public Frame(final Frame frame) {
        this.origin = frame.getOrigin();
        this.forward = frame.getForwardVector();
        this.up = frame.getUpVector();
        absm = -1;
    }

    public Frame(final Vector3f lookAtEye, final Vector3f lookAtCentre, final Vector3f lookAtUp) {
        final Vector3f direction = Vector3f.subtract(lookAtEye, lookAtCentre);
        absm = direction.normalize();
        origin = new Vector3f(0, 0, 0);
        forward = new Vector3f();
        setForwardVector(direction);
        up = new Vector3f();
        setUpVector(lookAtUp);
    }

    public void getLookAt(final Vector3f lookAtEye, final Vector3f lookAtCentre, final Vector3f lookAtUp) {
        final Vector3f direction = new Vector3f();
        getForwardVector(direction);
        direction.scale(absm);
        lookAtEye.set(direction);
        lookAtEye.add(lookAtCentre);
        getUpVector(lookAtUp);
    }

    public void setOrigin(final Vector3f point) {
        origin.set(point);
    }

    public void setOrigin(final float x, final float y, final float z) {
        origin.set(x, y, z);
    }

    public void getOrigin(final Vector3f point) {
        point.set(origin);
    }

    public Vector3f getOrigin() {
        return new Vector3f(origin);
    }

    public void setForwardVector(final Vector3f point) {
        forward.set(point);
    }

    public void setForwardVector(final float x, final float y, final float z) {
        forward.set(x, y, z);
    }

    public void getForwardVector(final Vector3f point) {
        point.set(forward);
    }

    public Vector3f getForwardVector() {
        return new Vector3f(forward);
    }

    public void setUpVector(final Vector3f point) {
        up.set(point);
    }

    public void setUpVector(final float x, final float y, final float z) {
        up.set(x, y, z);
    }

    public void getUpVector(final Vector3f point) {
        point.set(up);
    }

    public Vector3f getUpVector() {
        return new Vector3f(up);
    }

    public void getXAxis(final Vector3f vector) {
        vector.crossProduct(up, forward);
    }
    
    public Vector3f getXAxis() {
        final Vector3f result = new Vector3f();
        result.crossProduct(up, forward);
        return result;
    }

    public void getYAxis(final Vector3f vector) {
        getUpVector(vector);
    }
    
    public Vector3f getYAxis() {
        final Vector3f result = new Vector3f();
        getUpVector(result);
        return result;
    }

    public void getZAxis(final Vector3f vector) {
        getForwardVector(vector);
    }

    /**
     * Translate along orthonormal axis: world.
     *
     * @param x the x component of the translation vector.
     * @param y the y component of the translation vector.
     * @param z the z component of the translation vector.
     */
    public void translateWorld(final float x, final float y, final float z) {
        origin.a[0] += x;
        origin.a[1] += y;
        origin.a[2] += z;
    }

    /**
     * Translate along orthonormal axis: local.
     *
     * @param x the x component of the translation vector.
     * @param y the y component of the translation vector.
     * @param z the z component of the translation vector.
     */
    public void translateLocal(final float x, final float y, final float z) {
        moveForward(z);
        moveUp(y);
        moveRight(x);
    }

    /**
     * Move forward along Z axis.
     *
     * @param delta the distance to move forward.
     */
    public void moveForward(final float delta) {
        // Move along direction of front direction
        origin.a[0] += forward.a[0] * delta;
        origin.a[1] += forward.a[1] * delta;
        origin.a[2] += forward.a[2] * delta;
    }

    /**
     * Move up along Y axis.
     *
     * @param delta the distance to move up.
     */
    public void moveUp(final float delta) {
        // Move along direction of up direction.
        origin.a[0] += up.a[0] * delta;
        origin.a[1] += up.a[1] * delta;
        origin.a[2] += up.a[2] * delta;
    }

    /**
     * Move right along X axis.
     *
     * @param delta the distance to move right.
     */
    public void moveRight(final float delta) {
        // Move along direction of right vector.
        final Vector3f cross = new Vector3f();
        cross.crossProduct(up, forward);

        origin.a[0] += cross.a[0] * delta;
        origin.a[1] += cross.a[1] * delta;
        origin.a[2] += cross.a[2] * delta;
    }

    /**
     * Get the complete model matrix for the frame.
     *
     * @param matrix the matrix that will hold the current model matrix.
     */
    public void getMatrix(final Matrix44f matrix) {
        getMatrix(matrix, false);
    }

    /**
     * Get the model matrix for the frame.
     *
     * @param matrix the matrix will hold the rotation component of the current
     * model matrix.
     * @param rotationOnly If true, only get the rotation matrix, otherwise get
     * the complete matrix.
     */
    public void getMatrix(final Matrix44f matrix, final boolean rotationOnly) {
        // Calculate the right side (x) vector, drop it right into the matrix.
        final Vector3f vXAxis = new Vector3f();
        vXAxis.crossProduct(up, forward);

        // Set matrix column does not fill in the fourth row.
        matrix.setRow(vXAxis, 0);
        matrix.a[3] = 0;

        // Y Column
        matrix.setRow(up, 1);
        matrix.a[7] = 0;

        // Z Column
        matrix.setRow(forward, 2);
        matrix.a[11] = 0;

        // Translation (already done)
        if (rotationOnly) {
            matrix.a[12] = 0.0F;
            matrix.a[13] = 0.0F;
            matrix.a[14] = 0.0F;
        } else {
            matrix.setRow(origin, 3);
        }

        matrix.a[15] = 1;
    }

    public void getCameraMatrix(final Matrix44f m) {
        getCameraMatrix(m, false);
    }

    /**
     * Assemble the camera matrix.
     *
     * @param m the model view matrix of the camera.
     * @param rotationOnly If true, only get the rotation matrix, otherwise get
     * the complete matrix.
     */
    public void getCameraMatrix(final Matrix44f m, final boolean rotationOnly) {
        final Vector3f x = new Vector3f();
        final Vector3f z = new Vector3f();

        // Make rotation matrix
        // Z vector is reversed
        z.a[0] = -forward.a[0];
        z.a[1] = -forward.a[1];
        z.a[2] = -forward.a[2];

        // X vector = Y cross Z
        x.crossProduct(up, z);

        // Matrix has no translation information and is transposed (rows instead of columns).
        m.setTransposed(0, 0, x.a[0]);
        m.setTransposed(0, 1, x.a[1]);
        m.setTransposed(0, 2, x.a[2]);
        m.setTransposed(0, 3, 0);
        m.setTransposed(1, 0, up.a[0]);
        m.setTransposed(1, 1, up.a[1]);
        m.setTransposed(1, 2, up.a[2]);
        m.setTransposed(1, 3, 0);
        m.setTransposed(2, 0, z.a[0]);
        m.setTransposed(2, 1, z.a[1]);
        m.setTransposed(2, 2, z.a[2]);
        m.setTransposed(2, 3, 0);
        m.setTransposed(3, 0, 0);
        m.setTransposed(3, 1, 0);
        m.setTransposed(3, 2, 0);
        m.setTransposed(3, 3, 1);

        if (rotationOnly) {
            return;
        }

        // Apply translation too.
        final Matrix44f trans = new Matrix44f();
        trans.makeTranslationMatrix(-origin.a[0], -origin.a[1], -origin.a[2]);
        final Matrix44f M = new Matrix44f();
        M.multiply(m, trans);

        // Copy result back into m.
        m.set(M);
    }

    /**
     * Rotate around local X.
     *
     * @param angle the angle to rotate by.
     */
    public void rotateLocalX(final float angle) {
        final Matrix33f rotMat = new Matrix33f();
        final Vector3f localX = new Vector3f();
        final Vector3f rotVec = new Vector3f();

        // Get the local X axis.
        localX.crossProduct(up, forward);

        // Make a rotation matrix.
        rotMat.makeRotationMatrix(angle, localX.a[0], localX.a[1], localX.a[2]);

        // Rotate Y and Z.
        rotVec.rotate(up, rotMat);
        up.set(rotVec);

        rotVec.rotate(forward, rotMat);
        forward.set(rotVec);
    }

    /**
     * Rotate around local Y.
     *
     * @param angle the angle to rotate by.
     */
    public void rotateLocalY(final float angle) {
        final Matrix44f rotMat = new Matrix44f();

        // Rotate the forward vector around the up vector.
        rotMat.makeRotationMatrix(angle, up.a[0], up.a[1], up.a[2]);

        final Vector3f newVect = new Vector3f();

        // Rotate forward pointing vector.
        newVect.a[0] = rotMat.a[0] * forward.a[0] + rotMat.a[4] * forward.a[1] + rotMat.a[8] * forward.a[2];
        newVect.a[1] = rotMat.a[1] * forward.a[0] + rotMat.a[5] * forward.a[1] + rotMat.a[9] * forward.a[2];
        newVect.a[2] = rotMat.a[2] * forward.a[0] + rotMat.a[6] * forward.a[1] + rotMat.a[10] * forward.a[2];
        forward.set(newVect);
    }

    /**
     * Rotate around local Z.
     *
     * @param angle the angle to rotate by.
     */
    public void rotateLocalZ(final float angle) {
        final Matrix44f rotMat = new Matrix44f();

        // Rotate the up vector around the forward vector.
        rotMat.makeRotationMatrix(angle, forward.a[0], forward.a[1], forward.a[2]);

        final Vector3f newVect = new Vector3f();
        newVect.a[0] = rotMat.a[0] * up.a[0] + rotMat.a[4] * up.a[1] + rotMat.a[8] * up.a[2];
        newVect.a[1] = rotMat.a[1] * up.a[0] + rotMat.a[5] * up.a[1] + rotMat.a[9] * up.a[2];
        newVect.a[2] = rotMat.a[2] * up.a[0] + rotMat.a[6] * up.a[1] + rotMat.a[10] * up.a[2];
        up.set(newVect);
    }

    /**
     * Reset the axes to make sure they are orthonormal.
     * <p>
     * This should be called on occasion if the matrix is long-lived and
     * frequently transformed.
     */
    public void normalize() {
        final Vector3f cross = new Vector3f();

        // Calculate cross product of up and forward vectors.
        cross.crossProduct(up, forward);

        // Use result to recalculate forward vector.
        forward.crossProduct(cross, up);

        // Also check for unit length.
        up.normalize();
        forward.normalize();
    }

    /**
     * Rotate in world coordinates.
     *
     * @param angle the angle of rotation.
     * @param x the x component of the rotation axis.
     * @param y the y component of the rotation axis.
     * @param z the z component of the rotation axis.
     */
    public void rotateWorld(final float angle, final float x, final float y, final float z) {
        final Matrix44f rotMat = new Matrix44f();

        // Create the rotation matrix.
        rotMat.makeRotationMatrix(angle, x, y, z);

        final Vector3f newVect = new Vector3f();

        // Transform the up axis (inlined 3x3 rotation).
        newVect.a[0] = rotMat.a[0] * up.a[0] + rotMat.a[4] * up.a[1] + rotMat.a[8] * up.a[2];
        newVect.a[1] = rotMat.a[1] * up.a[0] + rotMat.a[5] * up.a[1] + rotMat.a[9] * up.a[2];
        newVect.a[2] = rotMat.a[2] * up.a[0] + rotMat.a[6] * up.a[1] + rotMat.a[10] * up.a[2];
        up.set(newVect);

        // Transform the forward axis.
        newVect.a[0] = rotMat.a[0] * forward.a[0] + rotMat.a[4] * forward.a[1] + rotMat.a[8] * forward.a[2];
        newVect.a[1] = rotMat.a[1] * forward.a[0] + rotMat.a[5] * forward.a[1] + rotMat.a[9] * forward.a[2];
        newVect.a[2] = rotMat.a[2] * forward.a[0] + rotMat.a[6] * forward.a[1] + rotMat.a[10] * forward.a[2];
        forward.set(newVect);
    }

    /**
     * Rotate around a local axis.
     *
     * @param angle the angle to rotate by.
     * @param x the x component of the rotation axis.
     * @param y the y component of the rotation axis.
     * @param z the z component of the rotation axis.
     */
    public void rotateLocal(final float angle, final float x, final float y, final float z) {
        final Vector3f worldVect = new Vector3f();
        final Vector3f localVect = new Vector3f(x, y, z);

        localToWorld(localVect, worldVect, true);
        rotateWorld(angle, worldVect.a[0], worldVect.a[1], worldVect.a[2]);
    }

    /**
     * Convert local coordinates to world coordinates.
     *
     * @param local a vector in local coordinates.
     * @param world the vector that will contain the local vector translated
     * into world coordinates.
     */
    public void localToWorld(final Vector3f local, final Vector3f world) {
        localToWorld(local, world, false);
    }

    /**
     * Convert local coordinates to world coordinates.
     * <p>
     * Basically do the transformation represented by the rotation and position
     * on the point.
     *
     * @param local a vector in local coordinates.
     * @param world the vector that will contain the local vector translated
     * into world coordinates.
     * @param rotationOnly true if only the rotation part of the camera model
     * matrix should be used.
     */
    public void localToWorld(final Vector3f local, final Vector3f world, final boolean rotationOnly) {
        // Create the rotation matrix based on the vectors
        Matrix44f rotMat = new Matrix44f();

        getMatrix(rotMat, true);

        // Do the rotation (inline it, and remove 4th column...)
        world.a[0] = rotMat.a[0] * local.a[0] + rotMat.a[4] * local.a[1] + rotMat.a[8] * local.a[2];
        world.a[1] = rotMat.a[1] * local.a[0] + rotMat.a[5] * local.a[1] + rotMat.a[9] * local.a[2];
        world.a[2] = rotMat.a[2] * local.a[0] + rotMat.a[6] * local.a[1] + rotMat.a[10] * local.a[2];

        // Translate the point
        if (!rotationOnly) {
            world.a[0] += origin.a[0];
            world.a[1] += origin.a[1];
            world.a[2] += origin.a[2];
        }
    }

    /**
     * Convert world coordinates to local coordinates.
     *
     * @param world a vector in world coordinates.
     * @param local a vector that will contain the world vector translated into
     * local coordinates.
     */
    public void worldToLocal(final Vector3f world, final Vector3f local) {
        // Translate the origin.
        final Vector3f vNewWorld = new Vector3f();
        vNewWorld.a[0] = world.a[0] - origin.a[0];
        vNewWorld.a[1] = world.a[1] - origin.a[1];
        vNewWorld.a[2] = world.a[2] - origin.a[2];

        // Create the rotation matrix based on the vectors.
        final Matrix44f rotMat = new Matrix44f();
        final Matrix44f invMat = new Matrix44f();
        getMatrix(rotMat, true);

        // Do the rotation based on inverted matrix.
        invMat.invert(rotMat);

        local.a[0] = invMat.a[0] * vNewWorld.a[0] + invMat.a[4] * vNewWorld.a[1] + invMat.a[8] * vNewWorld.a[2];
        local.a[1] = invMat.a[1] * vNewWorld.a[0] + invMat.a[5] * vNewWorld.a[1] + invMat.a[9] * vNewWorld.a[2];
        local.a[2] = invMat.a[2] * vNewWorld.a[0] + invMat.a[6] * vNewWorld.a[1] + invMat.a[10] * vNewWorld.a[2];
    }

    /**
     * Transform a point by frame matrix.
     *
     * @param pointSrc a vector containing a point to rotate.
     * @param pointDst a vector that will contain the transformed point.
     */
    public void transformPoint(final Vector3f pointSrc, final Vector3f pointDst) {
        final Matrix44f m = new Matrix44f();
        getMatrix(m, false);    // Rotate and translate
        pointDst.a[0] = m.a[0] * pointSrc.a[0] + m.a[4] * pointSrc.a[1] + m.a[8] * pointSrc.a[2] + m.a[12];
        pointDst.a[1] = m.a[1] * pointSrc.a[0] + m.a[5] * pointSrc.a[1] + m.a[9] * pointSrc.a[2] + m.a[13];
        pointDst.a[2] = m.a[2] * pointSrc.a[0] + m.a[6] * pointSrc.a[1] + m.a[10] * pointSrc.a[2] + m.a[14];
    }

    /**
     * Rotate a vector by frame matrix.
     *
     * @param vectorSrc a vector to rotate.
     * @param vectorDst a vector that will contain the transformed vector.
     */
    public void rotateVector(final Vector3f vectorSrc, final Vector3f vectorDst) {
        final Matrix44f m = new Matrix44f();
        getMatrix(m, true);    // Rotate only

        vectorDst.a[0] = m.a[0] * vectorSrc.a[0] + m.a[4] * vectorSrc.a[1] + m.a[8] * vectorSrc.a[2];
        vectorDst.a[1] = m.a[1] * vectorSrc.a[0] + m.a[5] * vectorSrc.a[1] + m.a[9] * vectorSrc.a[2];
        vectorDst.a[2] = m.a[2] * vectorSrc.a[0] + m.a[6] * vectorSrc.a[1] + m.a[10] * vectorSrc.a[2];
    }

    public static Frame[] createArray(final int length) {
        final Frame[] array = new Frame[length];
        for (int i = 0; i < length; i++) {
            array[i] = new Frame();
        }

        return array;
    }
    
    
    public boolean areSame(final Frame frame) {
        return origin.areSame(frame.origin)
                && forward.areSame(frame.forward)
                && up.areSame(frame.up)
                && absm == frame.absm;
    }

    @Override
    public String toString() {
        return String.format("o=(%s) f=(%s) u=(%s)", origin, forward, up);
    }
}
