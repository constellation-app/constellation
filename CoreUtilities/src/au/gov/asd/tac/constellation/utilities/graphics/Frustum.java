/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import java.util.Arrays;

/**
 * A viewing frustum.
 * 
 * @author capricornunicorn123
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
     * A private constructor to facilitate a deep copy of a Frustum.
     * @param projMatrix
     * @param nearUL untransformed corner of this frustum
     * @param nearLL untransformed corner of this frustum
     * @param nearUR untransformed corner of this frustum
     * @param nearLR untransformed corner of this frustum
     * @param farUL untransformed corner of this frustum
     * @param farLL untransformed corner of this frustum
     * @param farUR untransformed corner of this frustum
     * @param farLR untransformed corner of this frustum
     * @param nearULT transformed corner of this frustum
     * @param nearLLT transformed corner of this frustum
     * @param nearURT transformed corner of this frustum
     * @param nearLRT transformed corner of this frustum
     * @param farULT transformed corner of this frustum
     * @param farLLT transformed corner of this frustum
     * @param farURT transformed corner of this frustum
     * @param farLRT transformed corner of this frustum
     * @param nearPlane
     * @param farPlane
     * @param leftPlane
     * @param rightPlane
     * @param topPlane
     * @param bottomPlane
     */
    private Frustum(final Matrix44f projMatrix, 
            final Vector4f nearUL, final Vector4f nearLL, final Vector4f nearUR, final Vector4f nearLR, final Vector4f farUL, final Vector4f farLL, final Vector4f farUR, final Vector4f farLR,
            final Vector4f nearULT, final Vector4f nearLLT, final Vector4f nearURT, final Vector4f nearLRT, final Vector4f farULT, final Vector4f farLLT, final Vector4f farURT, final Vector4f farLRT,
            final Vector4f nearPlane, final Vector4f farPlane, final Vector4f leftPlane, final Vector4f rightPlane, final Vector4f topPlane, final Vector4f bottomPlane) {
        
        this.projMatrix = projMatrix;

        // Untransformed corners of this frustum.
        this.nearUL = nearUL;
        this.nearLL = nearLL;
        this.nearUR = nearUR;
        this.nearLR = nearLR;
        this.farUL = farUL;
        this.farLL = farLL;
        this.farUR = farUR;
        this.farLR = farLR;

        // Transformed corners of this frustum.
        this.nearULT = nearULT;
        this.nearLLT = nearLLT;
        this.nearURT = nearURT;
        this.nearLRT = nearLRT;
        this.farULT = farULT;
        this.farLLT = farLLT;
        this.farURT = farURT;
        this.farLRT = farLRT;

        // Base and transformed plane equations.
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.leftPlane = leftPlane;
        this.rightPlane = rightPlane;
        this.topPlane = topPlane;
        this.bottomPlane = bottomPlane;
    }
    
    /**
     * Creates a deep copy of a Frustum.
     * @return 
     */
    public Frustum getCopy(){
        return new Frustum(this.projMatrix,
                this.nearUL, this.nearLL, this.nearUR, this.nearLR, this.farUL, this.farLL, this.farUR, this.farLR,
                this.nearULT, this.nearLLT, this.nearURT, this.nearLRT, this.farULT, this.farLLT, this.farURT, this.farLRT,
                this.nearPlane, this.farPlane, this.leftPlane, this.rightPlane, this.topPlane, this.bottomPlane);
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
        projMatrix.a[15] = 1.0F;

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
        projMatrix.a[0] = (2.0F * near) / (xmax - xmin);
        projMatrix.a[5] = (2.0F * near) / (ymax - ymin);
        projMatrix.a[8] = (xmax + xmin) / (xmax - xmin);
        projMatrix.a[9] = (ymax + ymin) / (ymax - ymin);
        projMatrix.a[10] = -((far + near) / (far - near));
        projMatrix.a[11] = -1;
        projMatrix.a[14] = -((2.0F * far * near) / (far - near));
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

        // Create the transformation matrix. 
        camera.getForwardVector(vForward);        
        camera.getUpVector(vUp);
        camera.getOrigin(vOrigin);

        // Calculate the right side (x) vector
        vCross.crossProduct(vUp, vForward);

        // The Matrix
        // X Column
        rotMat.setRow(vCross, 0);
        rotMat.a[3] = 0;

        // Y Column
        rotMat.setRow(vUp, 1);
        rotMat.a[7] = 0;

        // Z Column
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
        return inView(point, radius);
    }
    
    /**
     * Evaluate a point against frustum perimeter.
     * A false value means it is entirely outside the frustum.
     * A true value means the pint is either inside of the frustum or on the edge of the frustum.
     * The radius value allows a test for a point (radius==0) or a sphere.
     * Note: Unless the frustum has been transformed with respect to the view point, then
     * the frustum perimeter is defined with the assumption that the view point is at worldPosition (0,0,0)
     *
     * @param point
     * @param radius
     *
     * @return False if it is not in the frustum, true if it intersects the
     * frustum.
     */
    public boolean inView(final Vector3f point, final float radius) {       
        return Mathf.distanceToPlane(point, nearPlane) + radius >= 0.0 // Near Plane - See if it is in front of me
                && Mathf.distanceToPlane(point, farPlane) + radius >= 0.0 // Distance to far plane
                && Mathf.distanceToPlane(point, leftPlane) + radius >= 0.0
                && Mathf.distanceToPlane(point, rightPlane) + radius >= 0.0
                && Mathf.distanceToPlane(point, bottomPlane) + radius >= 0.0
                && Mathf.distanceToPlane(point, topPlane) + radius >= 0.0;
    }
    
    /**
     * Evaluate a line drawn from a point against a frustum perimeter to determine at what point this line first enters the frustum.
     * <p>
     * A null value means it does not pass through the frustum at any point.
     * If the point starts in the frustum, that value is returned.
     *
     * @param initialEndPoint
     * @param finalEndPoint
     *
     * @return The point of first entry into the frustum
     * frustum.
     */
    public Vector3f getEntryPoint(final Vector3f initialEndPoint, final Vector3f finalEndPoint) {       
        // The point is already within the frustum so return the point as the initial entry point. 
        if (inView(initialEndPoint, 0)){
            return initialEndPoint;
        }
        
        // Initialise the entry point with invalid values to indicate no intersection has been found.
        final Vector3f entryPoint = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
        
        // Check each face 
        Arrays.asList(nearPlane, farPlane, leftPlane, rightPlane, topPlane, bottomPlane).forEach(plane -> {
            
            // Get the point that the line intesects the plane
            final Vector3f candidateEntryPoint = this.getFaceIntersection(plane, initialEndPoint, finalEndPoint);
            
            // If a candidate entry point was found, update the entry point with the closest found intersection
            if (candidateEntryPoint != null && (!entryPoint.isValid() || Mathf.distance(candidateEntryPoint, initialEndPoint) < Mathf.distance(entryPoint, initialEndPoint))) {
                entryPoint.set(candidateEntryPoint);
            }
        });
        return entryPoint.isValid() ? entryPoint : null;
    }
    
    /**
     * Evaluates a line drawn between two end points against a plane.
     * Method also evaluates to ensure that the intersection point is within the bounds of the frustum. 
     * @param plane
     * @param initialEndPoint
     * @param finalEndPoint
     */
    private Vector3f getFaceIntersection(final Vector4f plane, final Vector3f initialEndPoint, final Vector3f finalEndPoint) {
        final Vector3f planeIntersectionPoint = Mathf.planeIntersectionPoint(initialEndPoint, finalEndPoint, plane);
        return planeIntersectionPoint.isValid() && inView(planeIntersectionPoint, 0.1F) ? planeIntersectionPoint : null;
    }
}
