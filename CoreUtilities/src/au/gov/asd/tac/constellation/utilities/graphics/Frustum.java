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
package au.gov.asd.tac.constellation.utilities.graphics;

/**
 * A viewing frustum.
 *
 * @author algol
 */
public final class Frustum {

    // The projection matrix for this frustum.
    private Matrix44f projMatrix;

    // Untransformed corners of this frustum.
    private Vector4f nearUL;
    private Vector4f nearLL;
    private Vector4f nearUR;
    private Vector4f nearLR;
    private Vector4f farUL;
    private Vector4f farLL;
    private Vector4f farUR;
    private Vector4f farLR;

    // Transformed corners of this frustum.
    private Vector4f nearULT;
    private Vector4f nearLLT;
    private Vector4f nearURT;
    private Vector4f nearLRT;
    private Vector4f farULT;
    private Vector4f farLLT;
    private Vector4f farURT;
    private Vector4f farLRT;

    // Base and transformed plane equations.
    private Vector4f nearPlane;
    private Vector4f farPlane;
    private Vector4f leftPlane;
    private Vector4f rightPlane;
    private Vector4f topPlane;
    private Vector4f bottomPlane;

    /**
     * A symmetric perspective projection.
     *
     * @param fov Field of view.
     * @param aspect Aspect ration (width/height).
     * @param near Distance to near clipping plane.
     * @param far Distance to fart clipping plane.
     */
    public Frustum(final float fov, final float aspect, final float near, final float far) {
        setPerspective(fov, aspect, near, far);
    }

    /**
     * A perspective projection.
     *
     * @param fov Field of view.
     * @param aspect Aspect ration (width/height).
     * @param xmin Left side of near plane of frustum.
     * @param xmax Right side of near plane of frustum.
     * @param ymin Bottom of near plane of frustum.
     * @param ymax Top of near plane of frustum.
     * @param near Distance to near clipping plane.
     * @param far Distance to far clipping plane.
     */
    public Frustum(final float fov, final float aspect, final float xmin, final float xmax, final float ymin, final float ymax, final float near, final float far) {
        setPerspective(fov, aspect, xmin, xmax, ymin, ymax, near, far);
    }

    /**
     * An orthographic projection with reasonable defaults.
     */
    public Frustum() {
        setOrthographic(-1, 1, -1, 1, -1, 1);
    }

    /**
     * An orthographic projection.
     *
     * @param xMin Left side of near plane of frustum.
     * @param xMax Right side of near plane of frustum.
     * @param yMin Bottom of near plane of frustum.
     * @param yMax Top of near plane of frustum.
     * @param zMin Distance to near clipping plane.
     * @param zMax Distance to far clipping plane.
     */
    public Frustum(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin, final float zMax) {
        setOrthographic(xMin, xMax, yMin, yMax, zMin, zMax);
    }

    public Matrix44f getProjectionMatrix() {
        return projMatrix;
    }

    private void init() {
        projMatrix = new Matrix44f();

        // Untransformed corners.
        nearUL = new Vector4f();
        nearLL = new Vector4f();
        nearUR = new Vector4f();
        nearLR = new Vector4f();
        farUL = new Vector4f();
        farLL = new Vector4f();
        farUR = new Vector4f();
        farLR = new Vector4f();

        // Transformed corners.
        nearULT = new Vector4f();
        nearLLT = new Vector4f();
        nearURT = new Vector4f();
        nearLRT = new Vector4f();
        farULT = new Vector4f();
        farLLT = new Vector4f();
        farURT = new Vector4f();
        farLRT = new Vector4f();

        // Base and transformed plane equations.
        nearPlane = new Vector4f();
        farPlane = new Vector4f();
        leftPlane = new Vector4f();
        rightPlane = new Vector4f();
        topPlane = new Vector4f();
        bottomPlane = new Vector4f();
    }

    /**
     * Calculates the corners of the frustum and sets the orthographic
     * projection matrix.
     *
     * @param xMin the minimum coordinate on the x axis.
     * @param xMax the maximum coordinate on the x axis.
     * @param yMin the minimum coordinate on the y axis.
     * @param yMax the maximum coordinate on the y axis.
     * @param zMin the minimum coordinate on the z axis.
     * @param zMax the maximum coordinate on the z axis.
     */
    public void setOrthographic(final float xMin, final float xMax, final float yMin, final float yMax, final float zMin, final float zMax) {
        init();
        projMatrix.makeOrthographicMatrix(xMin, xMax, yMin, yMax, zMin, zMax);
        projMatrix.a[15] = 1.0f;

        // Fill in values for untransformed frustum corners
        // Near upper left.
        nearUL.a[0] = xMin;
        nearUL.a[1] = yMax;
        nearUL.a[2] = zMin;
        nearUL.a[3] = 1;

        // Near lower left.
        nearLL.a[0] = xMin;
        nearLL.a[1] = yMin;
        nearLL.a[2] = zMin;
        nearLL.a[3] = 1;

        // Near upper right.
        nearUR.a[0] = xMax;
        nearUR.a[1] = yMax;
        nearUR.a[2] = zMin;
        nearUR.a[3] = 1;

        // Near lower right.
        nearLR.a[0] = xMax;
        nearLR.a[1] = yMin;
        nearLR.a[2] = zMin;
        nearLR.a[3] = 1;

        // Far upper left.
        farUL.a[0] = xMin;
        farUL.a[1] = yMax;
        farUL.a[2] = zMax;
        farUL.a[3] = 1;

        // Far lower left.
        farLL.a[0] = xMin;
        farLL.a[1] = yMin;
        farLL.a[2] = zMax;
        farLL.a[3] = 1;

        // Far upper right.
        farUR.a[0] = xMax;
        farUR.a[1] = yMax;
        farUR.a[2] = zMax;
        farUR.a[3] = 1;

        // Far lower right.
        farLR.a[0] = xMax;
        farLR.a[1] = yMin;
        farLR.a[2] = zMax;
        farLR.a[3] = 1;
    }

    /**
     * Create a symmetric frustum with a perspective projection matrix.
     *
     * @param fov Field of view (degrees).
     * @param aspect Aspect ratio.
     * @param near Distance to near clipping plane.
     * @param far Distance to far clipping plane.
     */
    public void setPerspective(final float fov, final float aspect, final float near, final float far) {
        // Do the calculations for the near clipping plane.
        final float ymax = near * (float) Math.tan(fov * Math.PI / 360.0);
        final float ymin = -ymax;
        final float xmin = ymin * aspect;
        final float xmax = -xmin;
        setPerspective(fov, aspect, xmin, xmax, ymin, ymax, near, far);
    }

    /**
     * Create a frustum with a perspective projection matrix.
     *
     * Because the corners of the near clipping plane are specified explicitly,
     * an asymmetric frustum can be created.
     *
     * @param fov Field of view.
     * @param aspect Aspect ratio.
     * @param xmin Left side of near plane of frustum.
     * @param xmax Right side of near plane of frustum.
     * @param ymin Bottom of near plane of frustum.
     * @param ymax Top of near plane of frustum.
     * @param near Distance to near clipping plane.
     * @param far Distance to far clipping plane.
     */
    public void setPerspective(final float fov, final float aspect, final float xmin, final float xmax, final float ymin, final float ymax, final float near, final float far) {
        init();

        // Construct the projection matrix.
        projMatrix.makeIdentity();
        projMatrix.a[0] = (2.0f * near) / (xmax - xmin);
        projMatrix.a[5] = (2.0f * near) / (ymax - ymin);
        projMatrix.a[8] = (xmax + xmin) / (xmax - xmin);
        projMatrix.a[9] = (ymax + ymin) / (ymax - ymin);
        projMatrix.a[10] = -((far + near) / (far - near));
        projMatrix.a[11] = -1;
        projMatrix.a[14] = -((2.0f * far * near) / (far - near));
        projMatrix.a[15] = 0;

        // Do the calculations for the far clipping plane.
        final float yFmax = far * (float) Math.tan(fov * Math.PI / 360.0);
        final float yFmin = -yFmax;
        final float xFmin = yFmin * aspect;
        final float xFmax = -xFmin;

        // Fill in values for untransformed frustum corners.
        // Near upper left.
        nearUL.a[0] = xmin;
        nearUL.a[1] = ymax;
        nearUL.a[2] = -near;
        nearUL.a[3] = 1;

        // Near lower left.
        nearLL.a[0] = xmin;
        nearLL.a[1] = ymin;
        nearLL.a[2] = -near;
        nearLL.a[3] = 1;

        // Near upper right.
        nearUR.a[0] = xmax;
        nearUR.a[1] = ymax;
        nearUR.a[2] = -near;
        nearUR.a[3] = 1;

        // Near lower right.
        nearLR.a[0] = xmax;
        nearLR.a[1] = ymin;
        nearLR.a[2] = -near;
        nearLR.a[3] = 1;

        // Far upper left.
        farUL.a[0] = xFmin;
        farUL.a[1] = yFmax;
        farUL.a[2] = -far;
        farUL.a[3] = 1;

        // Far lower left.
        farLL.a[0] = xFmin;
        farLL.a[1] = yFmin;
        farLL.a[2] = -far;
        farLL.a[3] = 1;

        // Far upper right.
        farUR.a[0] = xFmax;
        farUR.a[1] = yFmax;
        farUR.a[2] = -far;
        farUR.a[3] = 1;

        // Far lower right.
        farLR.a[0] = xFmax;
        farLR.a[1] = yFmin;
        farLR.a[2] = -far;
        farLR.a[3] = 1;
    }

    /**
     * Build a transformation matrix and transform the corners of the frustum,
     * then derive the plane equations.
     *
     * @param camera the camera that describes the frustum.
     */
    public void transform(final Frame camera) {
        // Workspace
        final Matrix44f rotMat = new Matrix44f();
        final Vector3f vForward = new Vector3f();
        final Vector3f vUp = new Vector3f();
        final Vector3f vCross = new Vector3f();
        final Vector3f vOrigin = new Vector3f();

        // Create the transformation matrix. This was the trickiest part
        // for me. The default view from OpenGL is down the negative Z
        // axis. However, building a transformation axis from these
        // directional vectors points the frustum the wrong direction. So
        // You must reverse them here, or build the initial frustum
        // backwards - which to do is purely a matter of taste. I chose to
        // compensate here to allow better operability with some of my other
        // legacy code and projects. RSW
        camera.getForwardVector(vForward);
        vForward.a[0] = -vForward.a[0];
        vForward.a[1] = -vForward.a[1];
        vForward.a[2] = -vForward.a[2];

        camera.getUpVector(vUp);
        camera.getOrigin(vOrigin);

        // Calculate the right side (x) vector
        vCross.crossProduct(vUp, vForward);

        // The Matrix
        // X Column
//        memcpy(rotMat, vCross, sizeof(float)*3);
        rotMat.setRow(vCross, 0);
        rotMat.a[3] = 0;

        // Y Column
//        memcpy(&rotMat[4], vUp, sizeof(float)*3);
        rotMat.setRow(vUp, 1);
        rotMat.a[7] = 0;

        // Z Column
//        memcpy(&rotMat[8], vForward, sizeof(float)*3);
        rotMat.setRow(vForward, 2);
        rotMat.a[11] = 0;

        // Translation.
        rotMat.a[12] = vOrigin.a[0];
        rotMat.a[13] = vOrigin.a[1];
        rotMat.a[14] = vOrigin.a[2];
        rotMat.a[15] = 1;

        // Transform the frustum corners.
        nearULT.transform(nearUL, rotMat);
        nearLLT.transform(nearLL, rotMat);
        nearURT.transform(nearUR, rotMat);
        nearLRT.transform(nearLR, rotMat);
        farULT.transform(farUL, rotMat);
        farLLT.transform(farLL, rotMat);
        farURT.transform(farUR, rotMat);
        farLRT.transform(farLR, rotMat);

        // Derive the plane equations from points.
        // Points are given in counter clockwise order to make normals point inside the frustum near and far planes.
        // Near and far planes.
        Mathf.planeEquation(nearPlane, nearULT.toVector3f(), nearLLT.toVector3f(), nearLRT.toVector3f());
        Mathf.planeEquation(farPlane, farULT.toVector3f(), farURT.toVector3f(), farLRT.toVector3f());

        // Top and bottom planes.
        Mathf.planeEquation(topPlane, nearULT.toVector3f(), nearURT.toVector3f(), farURT.toVector3f());
        Mathf.planeEquation(bottomPlane, nearLLT.toVector3f(), farLLT.toVector3f(), farLRT.toVector3f());

        // Left and right planes.
        Mathf.planeEquation(leftPlane, nearLLT.toVector3f(), nearULT.toVector3f(), farULT.toVector3f());
        Mathf.planeEquation(rightPlane, nearLRT.toVector3f(), farLRT.toVector3f(), farURT.toVector3f());
    }

    boolean testSphere(final float x, final float y, final float z, final float fRadius) {
        final Vector3f vPoint = new Vector3f(x, y, z);

        return testSphere(vPoint, fRadius);
    }

    /**
     * Test a point against all frustum planes.
     * <p>
     * A negative distance for any single plane means it is outside the frustum.
     * The radius value allows a test for a point (radius==0) or a sphere.
     *
     * @param point
     * @param radius
     *
     * @return False if it is not in the frustum, true if it intersects the
     * frustum.
     */
    boolean testSphere(final Vector3f point, final float radius) {
        float fDist;

        // Near Plane - See if it is behind me
        fDist = Mathf.distanceToPlane(point, nearPlane);
        if (fDist + radius <= 0.0) {
            return false;
        }

        // Distance to far plane
        fDist = Mathf.distanceToPlane(point, farPlane);
        if (fDist + radius <= 0.0) {
            return false;
        }

        fDist = Mathf.distanceToPlane(point, leftPlane);
        if (fDist + radius <= 0.0) {
            return false;
        }

        fDist = Mathf.distanceToPlane(point, rightPlane);
        if (fDist + radius <= 0.0) {
            return false;
        }

        fDist = Mathf.distanceToPlane(point, bottomPlane);
        if (fDist + radius <= 0.0) {
            return false;
        }

        fDist = Mathf.distanceToPlane(point, topPlane);

        return fDist + radius > 0.0;
    }
}
