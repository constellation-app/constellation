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
 * A vector of three double values.
 *
 * @author algol
 */
public final class Vector3d implements Serializable {

    /**
     * The length of a vector.
     */
    public static final int LENGTH = 3;

    /**
     * The contents of this vector.
     */
    public final double[] a;

    /**
     * Create a new zero vector.
     */
    public Vector3d() {
        a = new double[LENGTH];
    }

    /**
     * Create a new vector with assigned values.
     *
     * @param x X.
     * @param y Y.
     * @param z Z.
     */
    public Vector3d(final double x, final double y, final double z) {
        a = new double[LENGTH];
        a[0] = x;
        a[1] = y;
        a[2] = z;
    }

    /**
     * Create a new vector using values from an existing vector.
     *
     * @param v An existing vector.
     */
    public Vector3d(final Vector3d v) {
        a = new double[LENGTH];
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
    }

    public void add(final Vector3d v) {
        a[0] += v.a[0];
        a[1] += v.a[1];
        a[2] += v.a[2];
    }

    public static Vector3d add(final Vector3d u, final Vector3d v) {
        return new Vector3d(u.a[0] + v.a[0], u.a[1] + v.a[1], u.a[2] + v.a[2]);
    }

    /**
     * Set the values of this vector from x,y,z.
     *
     * @param x X.
     * @param y Y.
     * @param z Z.
     */
    public void set(final double x, final double y, final double z) {
        a[0] = x;
        a[1] = y;
        a[2] = z;
    }

    public void set(final Vector3d v) {
        a[0] = v.a[0];
        a[1] = v.a[1];
        a[2] = v.a[2];
    }

    /**
     * Scale this vector.
     *
     * @param s Scale factor.
     */
    public void scale(final double s) {
        a[0] *= s;
        a[1] *= s;
        a[2] *= s;
    }

    public static Vector3d multiply(final Vector3d v, final double d) {
        return new Vector3d(v.a[0] * d, v.a[1] * d, v.a[2] * d);
    }

    public double getLengthSquared() {
        return (a[0] * a[0]) + (a[1] * a[1]) + (a[2] * a[2]);
    }

    public double getLength() {
        return Math.sqrt(getLengthSquared());
    }

    /**
     * The cross product is the vector perpendicular to the vectors being
     * multiplied and normal to the plane containing them.
     *
     * @param u A vector.
     * @param v A vector.
     * @return the cross product of the 2 given vectors.
     */
    public static Vector3d crossProduct(final Vector3d u, final Vector3d v) {
        final double d0 = u.a[1] * v.a[2] - v.a[1] * u.a[2];
        final double d1 = -u.a[0] * v.a[2] + v.a[0] * u.a[2];
        final double d2 = u.a[0] * v.a[1] - v.a[0] * u.a[1];

        return new Vector3d(d0, d1, d2);
    }

    /**
     * Normalize this vector.
     *
     * @return The pre-normalization length of the vector.
     */
    public double normalize() {
        final double length = getLength();
        if (length != 0) {
            scale(1.0 / length);
        }

        return length;
    }

    public double getMagnitudeSquared() {
        return a[0] * a[0] + a[1] * a[1] + a[2] * a[2];
    }

    public double getMagnitude() {
        return Math.sqrt(getMagnitudeSquared());
    }

    /**
     * Make this vector the normal from three points.
     *
     * @param point1 the first point.
     * @param point2 the second point.
     * @param point3 the third point.
     */
    public void findNormal(final Vector3d point1, final Vector3d point2, final Vector3d point3) {
        final Vector3d v1 = new Vector3d();
        final Vector3d v2 = new Vector3d();

        // Calculate two vectors from the three points. Assumes counter clockwise winding.
        v1.a[0] = point1.a[0] - point2.a[0];
        v1.a[1] = point1.a[1] - point2.a[1];
        v1.a[2] = point1.a[2] - point2.a[2];

        v2.a[0] = point2.a[0] - point3.a[0];
        v2.a[1] = point2.a[1] - point3.a[1];
        v2.a[2] = point2.a[2] - point3.a[2];

        // Take the cross product of the two vectors to get the normal vector.
        final Vector3d cp = crossProduct(v1, v2);
        a[0] = cp.a[0];
        a[1] = cp.a[1];
        a[2] = cp.a[2];
    }

    /**
     * Calculate u-v, store the result in a new vector.
     *
     * @param u An existing vector.
     * @param v Another existing vector.
     * @return the difference between the 2 given vectors.
     */
    public static Vector3d subtract(final Vector3d u, final Vector3d v) {
        return new Vector3d(u.a[0] - v.a[0], u.a[1] - v.a[1], u.a[2] - v.a[2]);
    }

    /**
     * The dot product is the cosine of the angle between the vectors.
     *
     * @param u A vector.
     * @param v A vector.
     *
     * @return The dot product of the vectors.
     */
    public static double dotProduct(final Vector3d u, final Vector3d v) {
        return u.a[0] * v.a[0] + u.a[1] * v.a[1] + u.a[2] * v.a[2];
    }

    public double getX() {
        return a[0];
    }

    public double getY() {
        return a[1];
    }

    public double getZ() {
        return a[2];
    }

    @Override
    public String toString() {
        return String.format("3d[%f,%f,%f]", a[0], a[1], a[2]);
    }
}
