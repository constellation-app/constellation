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
 * A vector of three floating point values.
 *
 * @author algol
 */
public final class Vector3f implements Serializable {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 3;

    /**
     * The contents of this vector.
     */
    public final float[] a;

    /**
     * Create a new zero vector.
     */
    public Vector3f() {
        a = new float[LENGTH];
    }

    /**
     * Create a new vector with assigned values.
     *
     * @param x X.
     * @param y Y.
     * @param z Z.
     */
    public Vector3f(final float x, final float y, final float z) {
        a = new float[LENGTH];
        set(x, y, z);
    }

    /**
     * Create a new vector using values from an existing vector.
     *
     * @param v An existing vector.
     */
    public Vector3f(final Vector3f v) {
        a = new float[LENGTH];
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
    }

    /**
     * Create a new vector using the first three values from an existing
     * Vector4f.
     *
     * @param v4f An existing Vector4f.
     */
    public Vector3f(final Vector4f v4f) {
        a = new float[LENGTH];
        a[0] = v4f.a[0];
        a[1] = v4f.a[1];
        a[2] = v4f.a[2];
    }

    /**
     * Add another vector to this vector.
     *
     * @param v The Vector3f to add to this.
     */
    public void add(final Vector3f v) {
        a[0] += v.a[0];
        a[1] += v.a[1];
        a[2] += v.a[2];
    }

    /**
     * Add two vectors, store the result in this vector.
     *
     * @param u An existing Vector3f.
     * @param v Another existing Vector3f.
     * @return the sum of the 2 given vectors.
     */
    public static Vector3f add(final Vector3f u, final Vector3f v) {
        return new Vector3f(u.a[0] + v.a[0], u.a[1] + v.a[1], u.a[2] + v.a[2]);
    }

    /**
     * Set the values of this vector from x,y,z.
     *
     * @param x X.
     * @param y Y.
     * @param z Z.
     */
    public void set(final float x, final float y, final float z) {
        a[0] = x;
        a[1] = y;
        a[2] = z;
    }

    /**
     * Set the values of this vector from an existing vector.
     *
     * @param v An existing Vector3f.
     */
    public void set(final Vector3f v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
    }

    /**
     * Rotate a given vector, storing the result in this instance.
     *
     * @param p The vector to rotate.
     * @param m A rotation matrix.
     */
    public void rotate(final Vector3f p, final Matrix33f m) {
        a[0] = m.a[0] * p.a[0] + m.a[3] * p.a[1] + m.a[6] * p.a[2];
        a[1] = m.a[1] * p.a[0] + m.a[4] * p.a[1] + m.a[7] * p.a[2];
        a[2] = m.a[2] * p.a[0] + m.a[5] * p.a[1] + m.a[8] * p.a[2];
    }

    /**
     * Rotate this vector by a matrix.
     *
     * @param m A rotation matrix
     */
    public void rotate(final Matrix33f m) {
        float x = m.a[0] * a[0] + m.a[3] * a[1] + m.a[6] * a[2];
        float y = m.a[1] * a[0] + m.a[4] * a[1] + m.a[7] * a[2];
        float z = m.a[2] * a[0] + m.a[5] * a[1] + m.a[8] * a[2];
        a[0] = x;
        a[1] = y;
        a[2] = z;
    }

    /**
     * Scale this vector.
     *
     * @param s Scale factor.
     */
    public void scale(final float s) {
        a[0] *= s;
        a[1] *= s;
        a[2] *= s;
    }

    /**
     * Transform this vector: rotation and translation via a 4x4 matrix.
     *
     * @param v The vector to transform.
     * @param m A transformation matrix.
     */
    public void transform(final Vector3f v, final Matrix44f m) {
        a[0] = m.a[0] * v.a[0] + m.a[4] * v.a[1] + m.a[8] * v.a[2] + m.a[12];
        a[1] = m.a[1] * v.a[0] + m.a[5] * v.a[1] + m.a[9] * v.a[2] + m.a[13];
        a[2] = m.a[2] * v.a[0] + m.a[6] * v.a[1] + m.a[10] * v.a[2] + m.a[14];
    }

    /**
     * Subtract another vector from this vector.
     *
     * @param v An existing vector.
     */
    public void subtract(final Vector3f v) {
        a[0] -= v.a[0];
        a[1] -= v.a[1];
        a[2] -= v.a[2];
    }

    /**
     * Calculate u-v, store the result in a new vector.
     *
     * @param u An existing vector.
     * @param v Another existing vector.
     * @return the difference between the 2 given vectors.
     */
    public static Vector3f subtract(final Vector3f u, final Vector3f v) {
        return new Vector3f(u.a[0] - v.a[0], u.a[1] - v.a[1], u.a[2] - v.a[2]);
    }

    /**
     * Calculate u-v, store the result in result.
     *
     * @param result The result.
     * @param u An existing vector.
     * @param v Another existing vector.
     */
    public static void subtract(final Vector3f result, final Vector3f u, final Vector3f v) {
        result.a[0] = u.a[0] - v.a[0];
        result.a[1] = u.a[1] - v.a[1];
        result.a[2] = u.a[2] - v.a[2];
    }

    /**
     * The cross product is the vector perpendicular to the vectors being
     * multiplied and normal to the plane containing them.
     *
     * @param u A vector.
     * @param v A vector.
     */
    public void crossProduct(final Vector3f u, final Vector3f v) {
        a[0] = u.a[1] * v.a[2] - v.a[1] * u.a[2];
        a[1] = -u.a[0] * v.a[2] + v.a[0] * u.a[2];
        a[2] = u.a[0] * v.a[1] - v.a[0] * u.a[1];
    }

    /**
     * The dot product is the cosine of the angle between the vectors.
     *
     * @param u A vector.
     * @param v A vector.
     *
     * @return The dot product of the vectors.
     */
    public static float dotProduct(final Vector3f u, final Vector3f v) {
        return u.a[0] * v.a[0] + u.a[1] * v.a[1] + u.a[2] * v.a[2];
    }

    public void convexCombineWith(final Vector3f v, final float mix) {
        final float inverseMix = 1 - mix;
        a[0] = (inverseMix * a[0]) + mix * (v.a[0]);
        a[1] = (inverseMix * a[1]) + mix * (v.a[1]);
        a[2] = (inverseMix * a[2]) + mix * (v.a[2]);
    }

    /**
     * Return the angle between the given vectors.
     * <p>
     * This is the acos of the dot product, so will be in the range 0.0 to pi.
     *
     * @param u First vector.
     * @param v Second vector.
     *
     * @return The angle (in radians) between the vectors.
     */
    public static float angleBetweenVectors(final Vector3f u, final Vector3f v) {
        final float tmp = dotProduct(u, v);

        return (float) Math.acos(tmp);
    }

    /**
     * Return the length squared of the vector.
     *
     * @return The length squared of the vector.
     */
    public float getLengthSquared() {
        return (a[0] * a[0]) + (a[1] * a[1]) + (a[2] * a[2]);
    }

    /**
     * Return the length of the vector.
     *
     * @return The length of the vector.
     */
    public float getLength() {
        return (float) Math.sqrt(getLengthSquared());
    }

    /**
     * Normalize this vector.
     *
     * @return The pre-normalization length of the vector.
     */
    public float normalize() {
        final float length = getLength();
        if (length != 0) {
            scale(1.0f / length);
        }

        return length;
    }

    /**
     * Make this vector the normal from three points.
     *
     * @param point1 First point.
     * @param point2 Second point.
     * @param point3 Third point.
     */
    public void findNormal(final Vector3f point1, final Vector3f point2, final Vector3f point3) {
        final Vector3f v1 = new Vector3f();
        final Vector3f v2 = new Vector3f();

        // Calculate two vectors from the three points. Assumes counter clockwise winding.
        v1.a[0] = point1.a[0] - point2.a[0];
        v1.a[1] = point1.a[1] - point2.a[1];
        v1.a[2] = point1.a[2] - point2.a[2];

        v2.a[0] = point2.a[0] - point3.a[0];
        v2.a[1] = point2.a[1] - point3.a[1];
        v2.a[2] = point2.a[2] - point3.a[2];

        // Take the cross product of the two vectors to get the normal vector.
        crossProduct(v1, v2);
    }

    /**
     * Create an array of vectors.
     *
     * @param length The length of the array.
     *
     * @return An array of Vector3f instances with the specified length.
     */
    public static Vector3f[] createArray(final int length) {
        Vector3f[] array = new Vector3f[length];
        for (int i = 0; i < length; i++) {
            array[i] = new Vector3f();
        }

        return array;
    }

    /**
     * Test for the elements of this Vector3f as all zero.
     *
     * @return True if all elements of this Vector3f are zero, false otherwise.
     */
    public boolean isZero() {
        return a[0] == 0 && a[1] == 0 && a[2] == 0;
    }

    public float getX() {
        return a[0];
    }

    public void setX(final float x) {
        a[0] = x;
    }

    public float getY() {
        return a[1];
    }

    public void setY(final float y) {
        a[1] = y;
    }

    public float getZ() {
        return a[2];
    }

    public void setZ(final float z) {
        a[2] = z;
    }

    public float getR() {
        return a[0];
    }

    public float getG() {
        return a[1];
    }

    public float getB() {
        return a[2];
    }

    /**
     * Does this vector contain valid numbers.
     * <p>
     * "Valid" means each element of the vector returns true for
     * Float.isFinite().
     *
     * @return True if each element of the vector is not Nan or infinite.
     */
    public boolean isValid() {
        return Float.isFinite(a[0]) && Float.isFinite(a[1]) && Float.isFinite(a[2]);
    }
    
    /**
     * Method used for testing to check if Vector3f values are equal
     * 
     * @param vec the Vector3f to compare to this instance
     * @return true if the Vector3f are the same, false otherwise
     */
    public boolean areSame(final Vector3f vec) {
        return a[0] == vec.a[0] && a[1] == vec.a[1] && a[2] == vec.a[2];
    }

    @Override
    public String toString() {
        return String.format("3f[%f,%f,%f]", a[0], a[1], a[2]);
    }
}
