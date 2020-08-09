/*
 * Copyright 2010-2020 Australian Signals Directorate
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
 * A vector of four integer point values.
 */
public final class Vector4i {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 4;

    /**
     * The contents of this vector.
     */
    public final int[] a;

    /**
     * Create a new zero vector.
     */
    public Vector4i() {
        a = new int[LENGTH];
    }

    /**
     * Create a new vector with assigned values.
     *
     * @param x the x component of the vector.
     * @param y the y component of the vector.
     * @param z the z component of the vector.
     * @param w the w component of the vector.
     */
    public Vector4i(final int x, final int y, final int z, int w) {
        a = new int[LENGTH];
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
    public void set(final int x, final int y, final int z, final int w) {
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
    public void set(final Vector4i v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
        a[3] = v.a[3];
    }

    public static void add(final Vector4i result, final Vector4i a, final Vector4i b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
        result.a[2] = a.a[2] + b.a[2];
        result.a[3] = a.a[3] + b.a[3];
    }

    public static void subtract(final Vector4i result, final Vector4i a, final Vector4i b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
        result.a[2] = a.a[2] - b.a[2];
        result.a[3] = a.a[3] - b.a[3];
    }

    public Vector3i toVector3i() {
        return new Vector3i(a[0], a[1], a[2]);
    }

    /**
     * Return the x component.
     *
     * @return The x component.
     */
    public int getX() {
        return a[0];
    }

    /**
     * Return the y component.
     *
     * @return The y component.
     */
    public int getY() {
        return a[1];
    }

    /**
     * Return the z component.
     *
     * @return The z component.
     */
    public int getZ() {
        return a[2];
    }

    /**
     * Return the w component.
     *
     * @return The w component.
     */
    public int getW() {
        return a[3];
    }

    /**
     * Return the red component.
     *
     * @return The red component.
     */
    public int getR() {
        return a[0];
    }

    /**
     * Return the green component.
     *
     * @return The green component.
     */
    public int getG() {
        return a[1];
    }

    /**
     * Return the blue component.
     *
     * @return The blue component.
     */
    public int getB() {
        return a[2];
    }

    /**
     * Return the alpha component.
     *
     * @return The alpha component.
     */
    public int getA() {
        return a[3];
    }

    @Override
    public String toString() {
        return String.format("4d[%d,%d,%d,%d]", a[0], a[1], a[2], a[3]);
    }
}
