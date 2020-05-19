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
 * Maths for floats.
 *
 * @author algol
 */
public final class Mathf {

    // Useful constants.
    public static final double TWO_PI = 6.283185307179586476925286766559;
    public static final double PI_DIV_180 = 0.01745329251994329576923690768489;
    public static final double INV_PI_DIV_180 = 57.295779513082320876798154814105;

    /**
     * Degrees to radians.
     *
     * @param x an angle in degrees.
     * @return the angle in radians.
     */
    public static double degToRad(final double x) {
        return x * PI_DIV_180;
    }

    /**
     * Radians to degrees.
     *
     * @param x an angle in radians.
     * @return the angle in degrees.
     */
    public static double radToDeg(final double x) {
        return x * INV_PI_DIV_180;
    }

//    /**
//     * Hours to degrees.
//     *
//     * @param x
//     * @return
//     */
//    public static double m3dHrToDeg(double x)
//    {
//        return (x*(1.0/15.0));
//    }
//    /**
//     * Hours to radians.
//     *
//     * @param x
//     * @return
//     */
//    public static double m3dHrToRad(double x)
//    {
//        return degToRad(m3dHrToDeg(x));
//    }
//    /**
//     * Degrees to hours.
//     *
//     * @param x
//     * @return
//     */
//    public static double m3dDegToHr(double x)
//    {
//        return x*15.0;
//    }
//    /**
//     * Radians to hours.
//     * @param x
//     * @return
//     */
//    public static double m3dRadToHr(double x)
//    {
//        return m3dDegToHr(radToDeg(x));
//    }
    /**
     * Returns the smallest power of two greater than or equal to the argument.
     * <p>
     * Returns the same number if it is a power of two, or the next highest
     * power of two otherwise.
     *
     * @param value a value to be tested.
     *
     * @return The smallest power of two greater than or equal to the argument.
     */
    public static int isPowerOfTwo(final int value) {
        int pow2 = 1;

        while (value > pow2) {
            pow2 = (pow2 << 1);
        }

        return pow2;
    }

//// Inject Rotation (3x3) into a full 4x4 matrix...
////    void m3dInjectRotationMatrix44(M3DMatrix44f dst, final M3DMatrix33f src)
//    void m3dInjectRotationMatrix44(float[] dst, final float[] src)
//    {
////        memcpy(dst, src, sizeof(float)*4);
////        memcpy(dst+4, src+4, sizeof(float)*4);
////        memcpy(dst+8, src+8, sizeof(float)*4);
//        System.arraycopy(src, 0, dst, 0, 4);
//        System.arraycopy(src, 4, dst, 4, 4);
//        System.arraycopy(src, 8, dst, 8, 4);
//    }
//// Ditto above for doubles
////    void m3dInjectRotationMatrix44(M3DMatrix44d dst, final M3DMatrix33d src)
//    void m3dInjectRotationMatrix44(double[] dst, final double[] src)
//    {
////        memcpy(dst, src, sizeof(double)*4);
////        memcpy(dst+4, src+4, sizeof(double)*4);
////        memcpy(dst+8, src+8, sizeof(double)*4);
//        System.arraycopy(src, 0, dst, 0, 4);
//        System.arraycopy(src, 4, dst, 4, 4);
//        System.arraycopy(src, 8, dst, 8, 4);
//    }
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
    public static float distanceSquared(final Vector3f u, final Vector3f v) {
        final float x = u.a[0] - v.a[0];
        final float y = u.a[1] - v.a[1];
        final float z = u.a[2] - v.a[2];

        return x * x + y * y + z * z;
    }

    /**
     * Get the distance between two points.
     *
     * The distance between two points is just the magnitude of the difference
     * between two vectors.
     *
     * @param u a vector representing a point.
     * @param v a vector representing a point.
     * @return the distance between the 2 given points.
     */
    public static float distance(final Vector3f u, final Vector3f v) {
        return (float) Math.sqrt(distanceSquared(u, v));
    }

    /**
     * Calculates the signed distance of a point to a plane (normalized).
     *
     * @param point a vector representing a point.
     * @param plane a vector representing a plane.
     *
     * @return the distance between the given point and the given plane.
     */
    public static float distanceToPlane(final Vector3f point, final Vector4f plane) {
        return point.a[0] * plane.a[0] + point.a[1] * plane.a[1] + point.a[2] * plane.a[2] + plane.a[3];
    }

    /**
     * Get plane equation from three points.
     *
     * @param planeEq a vector that will hold the calculated plane equation.
     * @param p1 the first point.
     * @param p2 the second point.
     * @param p3 the third point.
     */
    public static void planeEquation(final Vector4f planeEq, final Vector3f p1, final Vector3f p2, final Vector3f p3) {
        // Get two vectors; do the cross product.
        final Vector3f v1 = new Vector3f();
        final Vector3f v2 = new Vector3f();

        // V1 = p3 - p1
        v1.a[0] = p3.a[0] - p1.a[0];
        v1.a[1] = p3.a[1] - p1.a[1];
        v1.a[2] = p3.a[2] - p1.a[2];

        // V2 = P2 - p1
        v2.a[0] = p2.a[0] - p1.a[0];
        v2.a[1] = p2.a[1] - p1.a[1];
        v2.a[2] = p2.a[2] - p1.a[2];

        // Unit normal to plane - Not sure which is the best way here.
        final Vector3f tmp = new Vector3f();
        tmp.crossProduct(v1, v2);
        tmp.normalize();

        planeEq.a[0] = tmp.a[0];
        planeEq.a[1] = tmp.a[1];
        planeEq.a[2] = tmp.a[2];
        // Back substitute to get D
        planeEq.a[3] = -(planeEq.a[0] * p3.a[0] + planeEq.a[1] * p3.a[1] + planeEq.a[2] * p3.a[2]);
    }

    public static float raySphereTest(final Vector3f point, final Vector3f ray, final Vector3f sphereCenter, final float sphereRadius) {
        // Make sure ray is unit length.
        ray.normalize();

        final Vector3f rayToCenter = new Vector3f();	// Ray to center of sphere
        rayToCenter.a[0] = sphereCenter.a[0] - point.a[0];
        rayToCenter.a[1] = sphereCenter.a[1] - point.a[1];
        rayToCenter.a[2] = sphereCenter.a[2] - point.a[2];

        // Project rayToCenter on ray to test.
        final float a = Vector3f.dotProduct(rayToCenter, ray);

        // Distance to center of sphere.
        final float distance2 = Vector3f.dotProduct(rayToCenter, rayToCenter);	// Or length

        float dRet = (sphereRadius * sphereRadius) - distance2 + (a * a);

        // Return distance to intersection.
        if (dRet > 0.0) {
            dRet = a - (float) Math.sqrt(dRet);
        }

        return dRet;
    }

//    /**
//     * Faster (and one shortcut) replacements for gluProject. Get Window
//     * coordinates, discard Z.
//     *
//     * @param vPointOut
//     * @param mModelView
//     * @param mProjection
//     * @param iViewPort
//     * @param vPointIn
//     */
//    public static void projectXYZ(Vector2f vPointOut, Matrix44f mModelView, Matrix44f mProjection, int[] iViewPort, Vector3f vPointIn) {
//        Vector4f vBack = new Vector4f();
//        Vector4f vForth = new Vector4f();
////	memcpy(vBack, vPointIn, sizeof(float)*3);
//        System.arraycopy(vPointIn.a, 0, vBack.a, 0, 3);
//        vBack.a[3] = 1.0f;
////        transformVector(vForth, vBack, mModelView);
////        transformVector(vBack, vForth, mProjection);
//        vForth.transform(vBack, mModelView);
//        vBack.transform(vForth, mProjection);
//        if (!closeEnough(vBack.a[3], 0.0f, 0.000001f)) {
//            float div = 1.0f / vBack.a[3];
//            vBack.a[0] *= div;
//            vBack.a[1] *= div;
//            //vBack[2] *= div;
//        }
//        vPointOut.a[0] = (float) (iViewPort[0]) + (1.0f + (float) (vBack.a[0])) * (float) (iViewPort[2]) / 2.0f;
//        vPointOut.a[1] = (float) (iViewPort[1]) + (1.0f + (float) (vBack.a[1])) * (float) (iViewPort[3]) / 2.0f;
//        // This was put in for Grand Tour... I think it's right.
//        // .... please report any bugs
//        if (iViewPort[0] != 0) // Cast to float is expensive... avoid if posssible
//        {
//            vPointOut.a[0] -= (float) iViewPort[0];
//        }
//        if (iViewPort[1] != 0) {
//            vPointOut.a[1] -= (float) iViewPort[1];
//        }
//    }
//
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
    public static void catmullRom(final float[] vOut, final float[] vP0, final float[] vP1, final float[] vP2, final float[] vP3, final float t) {
        final float t2 = t * t;
        final float t3 = t2 * t;

        // X
        vOut[0] = 0.5f * ((2.0f * vP1[0])
                + (-vP0[0] + vP2[0]) * t
                + (2.0f * vP0[0] - 5.0f * vP1[0] + 4.0f * vP2[0] - vP3[0]) * t2
                + (-vP0[0] + 3.0f * vP1[0] - 3.0f * vP2[0] + vP3[0]) * t3);
        // Y
        vOut[1] = 0.5f * ((2.0f * vP1[1])
                + (-vP0[1] + vP2[1]) * t
                + (2.0f * vP0[1] - 5.0f * vP1[1] + 4.0f * vP2[1] - vP3[1]) * t2
                + (-vP0[1] + 3.0f * vP1[1] - 3.0f * vP2[1] + vP3[1]) * t3);

        // Z
        vOut[2] = 0.5f * ((2.0f * vP1[2])
                + (-vP0[2] + vP2[2]) * t
                + (2.0f * vP0[2] - 5.0f * vP1[2] + 4.0f * vP2[2] - vP3[2]) * t2
                + (-vP0[2] + 3.0f * vP1[2] - 3.0f * vP2[2] + vP3[2]) * t3);
    }

    /**
     * Compare floats.
     *
     * @param candidate the candidate float that would be compared.
     * @param compare the float value against which to compare the candidate.
     * @param epsilon the error factor.
     * @return true if the candidate is within the margin of error to the
     * compare value.
     */
    public static boolean closeEnough(final float candidate, final float compare, final float epsilon) {
        return Math.abs(candidate - compare) < epsilon;
    }

    /**
     * Finds the tangent bases for a triangle. (Used for normal mapping.)
     * <p>
     * Only a floating point implementation is provided. This has no practical
     * use as doubles. Calculate the tangent basis for a triangle on the surface
     * of a model. This vector is needed for most normal mapping shaders .
     *
     * @param vTangent a vector that will hold the calculated tangent.
     * @param vTriangle vectors that describe a triangle.
     * @param vTexCoords the texture coordinates.
     * @param N the normal vector.
     */
    public static void calculateTangentBasis(final Vector3f vTangent, final Vector3f[] vTriangle, final Vector2f[] vTexCoords, final Vector3f N) {
        final Vector3f dv2v1 = Vector3f.subtract(vTriangle[1], vTriangle[0]);
        final Vector3f dv3v1 = Vector3f.subtract(vTriangle[2], vTriangle[0]);

        final float dc2c1t = vTexCoords[1].a[0] - vTexCoords[0].a[0];
        final float dc2c1b = vTexCoords[1].a[1] - vTexCoords[0].a[1];
        final float dc3c1t = vTexCoords[2].a[0] - vTexCoords[0].a[0];
        final float dc3c1b = vTexCoords[2].a[1] - vTexCoords[0].a[1];

        float M = (dc2c1t * dc3c1b) - (dc3c1t * dc2c1b);
        M = 1.0f / M;

        dv2v1.scale(dc3c1b);
        dv3v1.scale(dc2c1b);

        Vector3f.subtract(vTangent, dv2v1, dv3v1);
//        vTangent.subtract(dv2v1, dv3v1);
        vTangent.scale(M);  // This potentially changes the direction of the vector
        vTangent.normalize();

        Vector3f B = new Vector3f();
//        crossProduct(B, N, vTangent);
//        crossProduct(vTangent, B, N);
        B.crossProduct(N, vTangent);
        vTangent.crossProduct(B, N);
        vTangent.normalize();
    }

    public static float smoothStep(final float edge1, final float edge2, final float x) {
        float t;
        t = (x - edge1) / (edge2 - edge1);
        if (t > 1.0f) {
            t = 1.0f;
        }

        if (t < 0.0) {
            t = 0.0f;
        }

        return t * t * (3.0f - 2.0f * t);
    }

    /**
     * Planar shadow Matrix.
     * <p>
     * Create a projection to "squish" an object into the plane; use
     * m3dGetPlaneEquationd(planeEq, point1, point2, point3).
     *
     * @param proj the resulting projection.
     * @param planeEq the points that define the plane.
     * @param vLightPos the light position.
     */
    public static void makePlanarShadowMatrix(final float[] proj, final float[] planeEq, final float[] vLightPos) {
        // These just make the code below easier to read.
        final float a = planeEq[0];
        final float b = planeEq[1];
        final float c = planeEq[2];
        final float d = planeEq[3];

        final float dx = -vLightPos[0];
        final float dy = -vLightPos[1];
        final float dz = -vLightPos[2];

        // Now build the projection matrix.
        proj[0] = b * dy + c * dz;
        proj[1] = -a * dy;
        proj[2] = -a * dz;
        proj[3] = 0.0f;

        proj[4] = -b * dx;
        proj[5] = a * dx + c * dz;
        proj[6] = -b * dz;
        proj[7] = 0.0f;

        proj[8] = -c * dx;
        proj[9] = -c * dy;
        proj[10] = a * dx + b * dy;
        proj[11] = 0.0f;

        proj[12] = -d * dx;
        proj[13] = -d * dy;
        proj[14] = -d * dz;
        proj[15] = a * dx + b * dy + c * dz;
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
    public static float closestPointOnRay(final Vector3f vPointOnRay, final Vector3f vRayOrigin, final Vector3f vUnitRayDir, final Vector3f vPointInSpace) {
        final Vector3f v = Vector3f.subtract(vPointInSpace, vRayOrigin);

        final float t = Vector3f.dotProduct(vUnitRayDir, v);

        // This is the point on the ray.
        vPointOnRay.a[0] = vRayOrigin.a[0] + (t * vUnitRayDir.a[0]);
        vPointOnRay.a[1] = vRayOrigin.a[1] + (t * vUnitRayDir.a[1]);
        vPointOnRay.a[2] = vRayOrigin.a[2] + (t * vUnitRayDir.a[2]);

        return distanceSquared(vPointOnRay, vPointInSpace);
    }
}
