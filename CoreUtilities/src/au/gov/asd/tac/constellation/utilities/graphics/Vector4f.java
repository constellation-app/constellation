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

/**
 * A vector of four floating point values.
 *
 * @author algol
 */
public final class Vector4f {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 4;

    /**
     * The contents of this vector.
     */
    public final float[] a;

    /**
     * Create a new zero vector.
     */
    public Vector4f() {
        a = new float[LENGTH];
    }

    /**
     * Create a new vector with assigned values.
     *
     * @param x the x component of the vector.
     * @param y the y component of the vector.
     * @param z the z component of the vector.
     * @param w the w component of the vector.
     */
    public Vector4f(final float x, final float y, final float z, final float w) {
        a = new float[LENGTH];
        set(x, y, z, w);
    }

    /**
     * Set the value of this vector from x,y,z,w.
     *
     * @param x the x component of the vector.
     * @param y the y component of the vector.
     * @param z the z component of the vector.
     * @param w the w component of the vector.
     */
    public void set(final float x, final float y, final float z, final float w) {
        a[0] = x;
        a[1] = y;
        a[2] = z;
        a[3] = w;
    }

    /**
     * Set the values of this vector from an existing vector.
     *
     * @param v the vector to be copied.
     */
    public void set(final Vector4f v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
        a[3] = v.a[3];
    }

    /**
     * Scale this vector.
     *
     * @param s the scaling factor.
     */
    public void scale(final float s) {
        a[0] *= s;
        a[1] *= s;
        a[2] *= s;
        a[3] *= s;
    }

    /**
     * Transform this vector: rotation and translation via a 4x4 matrix.
     *
     * @param v the vector to be transformed.
     * @param m the matrix to transform the vector by.
     */
    public void transform(final Vector4f v, final Matrix44f m) {
        a[0] = m.a[0] * v.a[0] + m.a[4] * v.a[1] + m.a[8] * v.a[2] + m.a[12] * v.a[3];
        a[1] = m.a[1] * v.a[0] + m.a[5] * v.a[1] + m.a[9] * v.a[2] + m.a[13] * v.a[3];
        a[2] = m.a[2] * v.a[0] + m.a[6] * v.a[1] + m.a[10] * v.a[2] + m.a[14] * v.a[3];
        a[3] = m.a[3] * v.a[0] + m.a[7] * v.a[1] + m.a[11] * v.a[2] + m.a[15] * v.a[3];
    }

    /**
     * Transform this vector: rotation and translation via a 4x4 matrix.
     *
     * @param m the matrix.
     * @return the vector calculated by transforming this vector by the given
     * matrix.
     */
    public Vector4f transform(final Matrix44f m) {
        final Vector4f v2 = new Vector4f();
        v2.a[0] = m.a[0] * a[0] + m.a[4] * a[1] + m.a[8] * a[2] + m.a[12] * a[3];
        v2.a[1] = m.a[1] * a[0] + m.a[5] * a[1] + m.a[9] * a[2] + m.a[13] * a[3];
        v2.a[2] = m.a[2] * a[0] + m.a[6] * a[1] + m.a[10] * a[2] + m.a[14] * a[3];
        v2.a[3] = m.a[3] * a[0] + m.a[7] * a[1] + m.a[11] * a[2] + m.a[15] * a[3];

        return v2;
    }

    public static void add(final Vector4f result, final Vector4f a, final Vector4f b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
        result.a[2] = a.a[2] + b.a[2];
        result.a[3] = a.a[3] + b.a[3];
    }

    public static void subtract(final Vector4f result, final Vector4f a, final Vector4f b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
        result.a[2] = a.a[2] - b.a[2];
        result.a[3] = a.a[3] - b.a[3];
    }

    public void getMatrixColumn(final Vector4f dst, final int column) {
        System.arraycopy(a, 4 * column, dst.a, 0, 4);
    }

    public void setMatrixColumn(final Vector4f dst, final int column) {
        System.arraycopy(a, 0, dst.a, 4 * column, 4);
    }

    public Vector3f toVector3f() {
        return new Vector3f(a[0], a[1], a[2]);
    }

    /**
     * Return the x component.
     *
     * @return The x component.
     */
    public float getX() {
        return a[0];
    }

    /**
     * Return the y component.
     *
     * @return The y component.
     */
    public float getY() {
        return a[1];
    }

    /**
     * Return the z component.
     *
     * @return The z component.
     */
    public float getZ() {
        return a[2];
    }

    /**
     * Return the w component.
     *
     * @return The w component.
     */
    public float getW() {
        return a[3];
    }

    /**
     * Return the red component.
     *
     * @return The red component.
     */
    public float getR() {
        return a[0];
    }

    /**
     * Return the green component.
     *
     * @return The green component.
     */
    public float getG() {
        return a[1];
    }

    /**
     * Return the blue component.
     *
     * @return The blue component.
     */
    public float getB() {
        return a[2];
    }

    /**
     * Return the alpha component.
     *
     * @return The alpha component.
     */
    public float getA() {
        return a[3];
    }

    @Override
    public String toString() {
        return String.format("4f[%f,%f,%f,%f]", a[0], a[1], a[2], a[3]);
    }
}
