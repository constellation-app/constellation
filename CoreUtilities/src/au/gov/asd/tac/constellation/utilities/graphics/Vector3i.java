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

import java.io.Serializable;

/**
 * A vector of three integer point values.
 */
public final class Vector3i implements Serializable {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 3;

    /**
     * The contents of this vector.
     */
    public final int[] a;

    /**
     * Create a new zero vector.
     */
    public Vector3i() {
        a = new int[LENGTH];
    }

    /**
     * Create a new vector with assigned values.
     *
     * @param x X.
     * @param y Y.
     * @param z Z.
     */
    public Vector3i(final int x, final int y, final int z) {
        a = new int[LENGTH];
        set(x, y, z);
    }

    /**
     * Create a new vector using values from an existing vector.
     *
     * @param v An existing vector.
     */
    public Vector3i(final Vector3i v) {
        a = new int[LENGTH];
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
    }

    /**
     * Add another vector to this vector.
     *
     * @param v The Vector3i to add to this.
     */
    public void add(final Vector3i v) {
        a[0] += v.a[0];
        a[1] += v.a[1];
        a[2] += v.a[2];
    }

    /**
     * Add two vectors, store the result in this vector.
     *
     * @param u An existing Vector3i.
     * @param v Another existing Vector3i.
     * @return the sum of the 2 given vectors.
     */
    public static Vector3i add(final Vector3i u, final Vector3i v) {
        return new Vector3i(u.a[0] + v.a[0], u.a[1] + v.a[1], u.a[2] + v.a[2]);
    }

    /**
     * Set the values of this vector from x,y,z.
     *
     * @param x X.
     * @param y Y.
     * @param z Z.
     */
    public void set(final int x, final int y, final int z) {
        a[0] = x;
        a[1] = y;
        a[2] = z;
    }

    /**
     * Set the values of this vector from an existing vector.
     *
     * @param v An existing Vector3f.
     */
    public void set(final Vector3i v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
    }

    /**
     * Subtract another vector from this vector.
     *
     * @param v An existing vector.
     */
    public void subtract(final Vector3i v) {
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
    public static Vector3i subtract(final Vector3i u, final Vector3i v) {
        return new Vector3i(u.a[0] - v.a[0], u.a[1] - v.a[1], u.a[2] - v.a[2]);
    }

    /**
     * Calculate u-v, store the result in result.
     *
     * @param result The result.
     * @param u An existing vector.
     * @param v Another existing vector.
     */
    public static void subtract(final Vector3i result, final Vector3i u, final Vector3i v) {
        result.a[0] = u.a[0] - v.a[0];
        result.a[1] = u.a[1] - v.a[1];
        result.a[2] = u.a[2] - v.a[2];
    }

    /**
     * Test for the elements of this Vector3i as all zero.
     *
     * @return True if all elements of this Vector3i are zero, false otherwise.
     */
    public boolean isZero() {
        return a[0] == 0 && a[1] == 0 && a[2] == 0;
    }

    public int getX() {
        return a[0];
    }

    public void setX(final int x) {
        a[0] = x;
    }

    public int getY() {
        return a[1];
    }

    public void setY(final int y) {
        a[1] = y;
    }

    public int getZ() {
        return a[2];
    }

    public void setZ(final int z) {
        a[2] = z;
    }

    public int getR() {
        return a[0];
    }

    public int getG() {
        return a[1];
    }

    public int getB() {
        return a[2];
    }
    
    public int getU() {
        return a[0];
    }

    public int getV() {
        return a[1];
    }

    public int getW() {
        return a[2];
    }    

    @Override
    public String toString() {
        return String.format("3d[%d,%d,%d]", a[0], a[1], a[2]);
    }
}
