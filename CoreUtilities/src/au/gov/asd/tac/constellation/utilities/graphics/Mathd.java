/*
 * Copyright 2010-2025 Australian Signals Directorate
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
 * Maths for doubles.
 *
 * @author algol
 */
public final class Mathd {

    public static void add(final Vector3d result, final Vector3d a, final Vector3d b) {
        result.a[0] = a.a[0] + b.a[0];
        result.a[1] = a.a[1] + b.a[1];
        result.a[2] = a.a[2] + b.a[2];
    }

    public static void subtract(final Vector3d result, final Vector3d a, final Vector3d b) {
        result.a[0] = a.a[0] - b.a[0];
        result.a[1] = a.a[1] - b.a[1];
        result.a[2] = a.a[2] - b.a[2];
    }

    public static void crossProduct(final Vector3d result, final Vector3d u, final Vector3d v) {
        result.a[0] = u.a[1] * v.a[2] - v.a[1] * u.a[2];
        result.a[1] = -u.a[0] * v.a[2] + v.a[0] * u.a[2];
        result.a[2] = u.a[0] * v.a[1] - v.a[0] * u.a[1];
    }

    public static double dotProduct(final Vector3d u, final Vector3d v) {
        return u.a[0] * v.a[0] + u.a[1] * v.a[1] + u.a[2] * v.a[2];
    }

    public static double getAngleBetweenVectors(final Vector3d u, final Vector3d v) {
        final double tmp = dotProduct(u, v);
        return Math.acos(tmp);
    }

    /**
     * Return the square of the distance between two points.
     * <p>
     * The distance between two points is just the magnitude of the difference
     * between two vectors.
     *
     * @param u A Vector3f.
     * @param v A Vector3f.
     *
     * @return The square of the distance between the two points.
     */
    public static double distanceSquared(final Vector3d u, final Vector3d v) {
        final double x = u.a[0] - v.a[0];
        final double y = u.a[1] - v.a[1];
        final double z = u.a[2] - v.a[2];

        return x * x + y * y + z * z;
    }

    public static double getDistance(final Vector3d u, final Vector3d v) {
        return Math.sqrt(distanceSquared(u, v));
    }

    private static int index33(final int row, final int col) {
        return (col * 3) + row;
    }

    private static int index44(final int row, final int col) {
        return (col * 4) + row;
    }

    public static void matrixMultiply(final Matrix44d product, final Matrix44d a, final Matrix44d b) {
        final double[] pa = product.getA();
        final double[] aa = a.getA();
        final double[] ba = b.getA();

        for (int i = 0; i < 4; i++) {
            final double ai0 = aa[index44(i, 0)];
            final double ai1 = aa[index44(i, 1)];
            final double ai2 = aa[index44(i, 2)];
            final double ai3 = aa[index44(i, 3)];
            pa[index44(i, 0)] = ai0 * ba[index44(0, 0)] + ai1 * ba[index44(1, 0)] + ai2 * ba[index44(2, 0)] + ai3 * ba[index44(3, 0)];
            pa[index44(i, 1)] = ai0 * ba[index44(0, 1)] + ai1 * ba[index44(1, 1)] + ai2 * ba[index44(2, 1)] + ai3 * ba[index44(3, 1)];
            pa[index44(i, 2)] = ai0 * ba[index44(0, 2)] + ai1 * ba[index44(1, 2)] + ai2 * ba[index44(2, 2)] + ai3 * ba[index44(3, 2)];
            pa[index44(i, 3)] = ai0 * ba[index44(0, 3)] + ai1 * ba[index44(1, 3)] + ai2 * ba[index44(2, 3)] + ai3 * ba[index44(3, 3)];
        }
    }

    public static void matrixMultiply(final Matrix33d product, final Matrix33d a, final Matrix33d b) {
        final double[] pa = product.getA();
        final double[] aa = a.getA();
        final double[] ba = b.getA();

        for (int i = 0; i < 3; i++) {
            final double ai0 = aa[index33(i, 0)];
            final double ai1 = aa[index33(i, 1)];
            final double ai2 = aa[index33(i, 2)];
            pa[index33(i, 0)] = ai0 * ba[index33(0, 0)] + ai1 * ba[index33(1, 0)] + ai2 * ba[index33(2, 0)];
            pa[index33(i, 1)] = ai0 * ba[index33(0, 1)] + ai1 * ba[index33(1, 1)] + ai2 * ba[index33(2, 1)];
            pa[index33(i, 2)] = ai0 * ba[index33(0, 2)] + ai1 * ba[index33(1, 2)] + ai2 * ba[index33(2, 2)];
        }
    }

    public static void transformVector(final Vector3d vOut, final Vector3d v, final Matrix44d m) {
        vOut.a[0] = m.getA()[0] * v.a[0] + m.getA()[4] * v.a[1] + m.getA()[8] * v.a[2] + m.getA()[12];
        vOut.a[1] = m.getA()[1] * v.a[0] + m.getA()[5] * v.a[1] + m.getA()[9] * v.a[2] + m.getA()[13];
        vOut.a[2] = m.getA()[2] * v.a[0] + m.getA()[6] * v.a[1] + m.getA()[10] * v.a[2] + m.getA()[14];
    }

    public static void transformVector(final Vector4d vOut, final Vector4d v, final Matrix44d m) {
        vOut.getA()[0] = m.getA()[0] * v.getA()[0] + m.getA()[4] * v.getA()[1] + m.getA()[8] * v.getA()[2] + m.getA()[12] * v.getA()[3];
        vOut.getA()[1] = m.getA()[1] * v.getA()[0] + m.getA()[5] * v.getA()[1] + m.getA()[9] * v.getA()[2] + m.getA()[13] * v.getA()[3];
        vOut.getA()[2] = m.getA()[2] * v.getA()[0] + m.getA()[6] * v.getA()[1] + m.getA()[10] * v.getA()[2] + m.getA()[14] * v.getA()[3];
        vOut.getA()[3] = m.getA()[3] * v.getA()[0] + m.getA()[7] * v.getA()[1] + m.getA()[11] * v.getA()[2] + m.getA()[15] * v.getA()[3];
    }

    public static void rotateVector(final Vector3d vOut, final Vector3d p, final Matrix33d m) {
        vOut.a[0] = m.getA()[0] * p.a[0] + m.getA()[3] * p.a[1] + m.getA()[6] * p.a[2];
        vOut.a[1] = m.getA()[1] * p.a[0] + m.getA()[4] * p.a[1] + m.getA()[7] * p.a[2];
        vOut.a[2] = m.getA()[2] * p.a[0] + m.getA()[5] * p.a[1] + m.getA()[8] * p.a[2];
    }

    public static void makeScalingMatrix(final Matrix33d m, final double xScale, final double yScale, final double zScale) {
        m.identity();
        m.getA()[0] = xScale;
        m.getA()[4] = yScale;
        m.getA()[8] = zScale;
    }

    public static void makeScalingMatrix(final Matrix33d m, final Vector3d vScale) {
        m.identity();
        m.getA()[0] = vScale.a[0];
        m.getA()[4] = vScale.a[1];
        m.getA()[8] = vScale.a[2];
    }

    public static void makeScalingMatrix(final Matrix44d m, final double xScale, final double yScale, final double zScale) {
        m.identity();
        m.getA()[0] = xScale;
        m.getA()[5] = yScale;
        m.getA()[10] = zScale;
    }

    public static void makeScalingMatrix(final Matrix44d m, final Vector3d vScale) {
        m.identity();
        m.getA()[0] = vScale.a[0];
        m.getA()[5] = vScale.a[1];
        m.getA()[10] = vScale.a[2];
    }

    public static void makeRotationMatrix(final Matrix33d m, final double angle, double x, double y, double z) {
        final double mag = Math.sqrt(x * x + y * y + z * z);
        // Identity matrix
        if (mag == 0.0F) {
            m.identity();
            return;
        }

        // Rotation matrix is normalized
        x /= mag;
        y /= mag;
        z /= mag;
        
        final double s = Math.sin(angle);
        final double c = Math.cos(angle);
        
        final double xx = x * x;
        final double yy = y * y;
        final double zz = z * z;
        final double xy = x * y;
        final double yz = y * z;
        final double zx = z * x;
        final double xs = x * s;
        final double ys = y * s;
        final double zs = z * s;
        final double oneC = 1.0F - c;

        m.getA()[index33(0, 0)] = (oneC * xx) + c;
        m.getA()[index33(0, 1)] = (oneC * xy) - zs;
        m.getA()[index33(0, 2)] = (oneC * zx) + ys;

        m.getA()[index33(1, 0)] = (oneC * xy) + zs;
        m.getA()[index33(1, 1)] = (oneC * yy) + c;
        m.getA()[index33(1, 2)] = (oneC * yz) - xs;

        m.getA()[index33(2, 0)] = (oneC * zx) - ys;
        m.getA()[index33(2, 1)] = (oneC * yz) + xs;
        m.getA()[index33(2, 2)] = (oneC * zz) + c;
    }

    /**
     * Creates a 4x4 rotation matrix, takes radians NOT degrees.
     *
     * @param m
     * @param angle
     * @param x
     * @param y
     * @param z
     */
    public static void makeRotationMatrix(final Matrix44d m, final double angle, double x, double y, double z) {
        final double mag = Math.sqrt(x * x + y * y + z * z);       
        // Identity matrix
        if (mag == 0.0) {
            m.identity();
            return;
        }

        // Rotation matrix is normalized
        x /= mag;
        y /= mag;
        z /= mag;

        final double s = Math.sin(angle);
        final double c = Math.cos(angle);
        
        final double xx = x * x;
        final double yy = y * y;
        final double zz = z * z;
        final double xy = x * y;
        final double yz = y * z;
        final double zx = z * x;
        final double xs = x * s;
        final double ys = y * s;
        final double zs = z * s;
        final double oneC = 1.0F - c;

        m.getA()[index44(0, 0)] = (oneC * xx) + c;
        m.getA()[index44(0, 1)] = (oneC * xy) - zs;
        m.getA()[index44(0, 2)] = (oneC * zx) + ys;
        m.getA()[index44(0, 3)] = 0.0F;

        m.getA()[index44(1, 0)] = (oneC * xy) + zs;
        m.getA()[index44(1, 1)] = (oneC * yy) + c;
        m.getA()[index44(1, 2)] = (oneC * yz) - xs;
        m.getA()[index44(1, 3)] = 0.0F;

        m.getA()[index44(2, 0)] = (oneC * zx) - ys;
        m.getA()[index44(2, 1)] = (oneC * yz) + xs;
        m.getA()[index44(2, 2)] = (oneC * zz) + c;
        m.getA()[index44(2, 3)] = 0.0F;

        m.getA()[index44(3, 0)] = 0.0F;
        m.getA()[index44(3, 1)] = 0.0F;
        m.getA()[index44(3, 2)] = 0.0F;
        m.getA()[index44(3, 3)] = 1.0F;
    }

    public static void makeTranslationMatrix(final Matrix44d m, final double x, final double y, final double z) {
        m.identity();
        m.getA()[12] = x;
        m.getA()[13] = y;
        m.getA()[14] = z;
    }

    private static double detIJ(final Matrix44d m, final int i, final int j) {
        final double[][] mat = new double[3][3];

        int x = 0;
        for (int ii = 0; ii < 4; ii++) {
            if (ii == i) {
                continue;
            }
            int y = 0;
            for (int jj = 0; jj < 4; jj++) {
                if (jj == j) {
                    continue;
                }
                mat[x][y] = m.getA()[(ii * 4) + jj];
                y++;
            }
            x++;
        }

        double ret = mat[0][0] * (mat[1][1] * mat[2][2] - mat[2][1] * mat[1][2]);
        ret -= mat[0][1] * (mat[1][0] * mat[2][2] - mat[2][0] * mat[1][2]);
        ret += mat[0][2] * (mat[1][0] * mat[2][1] - mat[2][0] * mat[1][1]);

        return ret;
    }

    public static void invertMatrix(final Matrix44d mInverse, final Matrix44d m) {
        // calculate 4x4 determinant
        double det = 0.0;
        for (int i = 0; i < 4; i++) {
            det += (i & 0x1) == 1 ? (-m.getA()[i] * detIJ(m, 0, i)) : (m.getA()[i] * detIJ(m, 0, i));
        }
        det = (det != 0 ? (1.0 / det) : Double.POSITIVE_INFINITY);

        // calculate inverse
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final double detij = detIJ(m, j, i);
                mInverse.getA()[(i * 4) + j] = ((i + j) & 0x1) == 1 ? (-detij * det) : (detij * det);
            }
        }
    }

    public static double getDistanceToPlane(final Vector3d point, final Vector4d plane) {
        return point.a[0] * plane.getA()[0] + point.a[1] * plane.getA()[1] + point.a[2] * plane.getA()[2] + plane.getA()[3];
    }

    public static void getPlaneEquation(final Vector4d planeEq, final Vector3d p1, final Vector3d p2, final Vector3d p3) {
        // Get two vectors; do the cross product.
        final Vector3d v1 = new Vector3d();
        final Vector3d v2 = new Vector3d();

        // V1 = p3 - p1
        v1.a[0] = p3.a[0] - p1.a[0];
        v1.a[1] = p3.a[1] - p1.a[1];
        v1.a[2] = p3.a[2] - p1.a[2];

        // V2 = P2 - p1
        v2.a[0] = p2.a[0] - p1.a[0];
        v2.a[1] = p2.a[1] - p1.a[1];
        v2.a[2] = p2.a[2] - p1.a[2];

        // Unit normal to plane - Not sure which is the best way here.
        final Vector3d tmp = new Vector3d();
        crossProduct(tmp, v1, v2);
        tmp.normalize();

        planeEq.getA()[0] = tmp.a[0];
        planeEq.getA()[1] = tmp.a[1];
        planeEq.getA()[2] = tmp.a[2];
        // Back substitute to get D
        planeEq.getA()[3] = -(planeEq.getA()[0] * p3.a[0] + planeEq.getA()[1] * p3.a[1] + planeEq.getA()[2] * p3.a[2]);
    }

// Determine if a ray intersects a sphere
// Return value is < 0 if the ray does not intersect
// Return value is 0.0 if ray is tangent
// Positive value is distance to the intersection point
    public static double raySphereTest(final Vector3d point, final Vector3d ray, final Vector3d sphereCenter, final double sphereRadius) {
        // Make sure ray is unit length.
        ray.normalize();

        final Vector3d rayToCenter = new Vector3d();	// Ray to center of sphere
        rayToCenter.a[0] = sphereCenter.a[0] - point.a[0];
        rayToCenter.a[1] = sphereCenter.a[1] - point.a[1];
        rayToCenter.a[2] = sphereCenter.a[2] - point.a[2];

        // Project rayToCenter on ray to test.
        final double a = dotProduct(rayToCenter, ray);

        // Distance to center of sphere.
        final double distance2 = dotProduct(rayToCenter, rayToCenter);	// Or length

        double dRet = (sphereRadius * sphereRadius) - distance2 + (a * a);

        // Return distance to intersection.
        if (dRet > 0.0) {
            dRet = a - Math.sqrt(dRet);
        }

        return dRet;
    }

    /**
     * This function does a three dimensional Catmull-Rom "spline" interpolation
     * between p1 and p2.
     * <p>
     * Pass four points, and a floating point number between 0.0 and 1.0. The
     * curve is interpolated between the middle two points.
     *
     * @param vOut the interpolated point will be stored in this vector.
     * @param vP0 the first point.
     * @param vP1 the second point.
     * @param vP2 the third point.
     * @param vP3 the fourth point.
     * @param t the interpolation parameter.
     */
    public static void catmullRom(final double[] vOut, final double[] vP0, final double[] vP1, final double[] vP2, final double[] vP3, double t) {
        final double t2 = t * t;
        final double t3 = t2 * t;

        // X
        vOut[0] = 0.5 * ((2.0 * vP1[0])
                + (-vP0[0] + vP2[0]) * t
                + (2.0 * vP0[0] - 5.0 * vP1[0] + 4.0 * vP2[0] - vP3[0]) * t2
                + (-vP0[0] + 3.0 * vP1[0] - 3.0 * vP2[0] + vP3[0]) * t3);
        // Y
        vOut[1] = 0.5 * ((2.0 * vP1[1])
                + (-vP0[1] + vP2[1]) * t
                + (2.0 * vP0[1] - 5.0 * vP1[1] + 4.0 * vP2[1] - vP3[1]) * t2
                + (-vP0[1] + 3 * vP1[1] - 3.0 * vP2[1] + vP3[1]) * t3);

        // Z
        vOut[2] = 0.5 * ((2.0 * vP1[2])
                + (-vP0[2] + vP2[2]) * t
                + (2.0 * vP0[2] - 5.0 * vP1[2] + 4.0 * vP2[2] - vP3[2]) * t2
                + (-vP0[2] + 3.0 * vP1[2] - 3.0 * vP2[2] + vP3[2]) * t3);
    }

    /**
     * Compare doubles.
     *
     * @param candidate the candidate double that would be compared.
     * @param compare the double value against which to compare the candidate.
     * @param epsilon the error factor.
     * @return true if the candidate is within the margin of error to the
     * compare value.
     */
    public static boolean closeEnough(final double candidate, final double compare, final double epsilon) {
        return (Math.abs(candidate - compare) < epsilon);
    }

    /**
     * Smoothly step between 0 and 1 between edge1 and edge 2
     *
     * @param edge1
     * @param edge2
     * @param x
     * @return
     */
    public static double m3dSmoothStep(final double edge1, final double edge2, final double x) {
        double t = (x - edge1) / (edge2 - edge1);
        if (t > 1.0) {
            t = 1.0;
        } else if (t < 0.0) {
            t = 0.0F;
        }
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * Planar shadow Matrix - Create a projection to "squish" an object into the
     * plane.
     *
     * @param proj
     * @param planeEq
     * @param vLightPos
     */
    public static void m3dMakePlanarShadowMatrix(final double[] proj, final double[] planeEq, final double[] vLightPos) {
        // These just make the code below easier to read. They will be
        // removed by the optimizer.
        final double a = planeEq[0];
        final double b = planeEq[1];
        final double c = planeEq[2];
        final double d = planeEq[3];

        final double dx = -vLightPos[0];
        final double dy = -vLightPos[1];
        final double dz = -vLightPos[2];

        // Now build the projection matrix
        proj[0] = b * dy + c * dz;
        proj[1] = -a * dy;
        proj[2] = -a * dz;
        proj[3] = 0.0;

        proj[4] = -b * dx;
        proj[5] = a * dx + c * dz;
        proj[6] = -b * dz;
        proj[7] = 0.0;

        proj[8] = -c * dx;
        proj[9] = -c * dy;
        proj[10] = a * dx + b * dy;
        proj[11] = 0.0;

        proj[12] = -d * dx;
        proj[13] = -d * dy;
        proj[14] = -d * dz;
        proj[15] = a * dx + b * dy + c * dz;
        // Shadow matrix ready
    }

    /**
     * Determine the point on a ray closest to another given point in space.
     * Return the distance squared of the two points.
     * <p>
     * @param vPointOnRay The point on the ray closest to vPointInSpace.
     * @param vRayOrigin The origin of the ray.
     * @param vUnitRayDir The unit vector of the ray.
     * @param vPointInSpace The point in space.
     *
     * @return The distance squared of the two points.
     */
    public static double closestPointOnRay(final Vector3d vPointOnRay, final Vector3d vRayOrigin, final Vector3d vUnitRayDir, final Vector3d vPointInSpace) {
        final Vector3d v = new Vector3d();
        subtract(v, vPointInSpace, vRayOrigin);

        final double t = dotProduct(vUnitRayDir, v);

        // This is the point on the ray
        vPointOnRay.a[0] = vRayOrigin.a[0] + (t * vUnitRayDir.a[0]);
        vPointOnRay.a[1] = vRayOrigin.a[1] + (t * vUnitRayDir.a[1]);
        vPointOnRay.a[2] = vRayOrigin.a[2] + (t * vUnitRayDir.a[2]);

        return distanceSquared(vPointOnRay, vPointInSpace);
    }
}
