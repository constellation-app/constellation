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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.io.Serializable;

/**
 * Determine the bounding box of two sets of 3D vertices.
 * <p>
 * The graph renderer uses two sets of vertices to display a graph. The bounding
 * box must therefore take both sets of vertices into account.
 * <p>
 * There are two ways of providing vertices. First, when a graph is added and
 * the vertex array buffer is being built, vertexes can be added one by one
 * using addVertex(). Second, when a node is moved, an entire array buffer can
 * be passed.
 * <p>
 * When a node is moved by the user, the bounding box must be recalculated by
 * scanning all of the nodes, because we don't know if the nodes that were moved
 * made the bounding box bigger or smaller, or had no effect.
 * <p>
 * A bounding box can be used to place the camera in a position that can view
 * all of the nodes.
 *
 * @author algol
 */
public final class BoundingBox implements Serializable {

    public static final float MINIMUM_SIZE = 5F;
    public static final float MINIMUM_CAMERA_DISTANCE = 6F;
    public static final float EMPTYBOX_CAMERA_DISTANCE = -1000F;

    // The minimum and maximum (x, y, z) and (x2, y2, z2) values.
    private Vector3f min;
    private Vector3f max;
    private Vector3f min2;
    private Vector3f max2;

    // Is the bounding box empty?
    private boolean isEmpty;

    public BoundingBox copy() {
        return new BoundingBox(this);
    }

    /**
     * Construct a new reset BoundingBox.
     */
    public BoundingBox() {
        resetMinMax();
        isEmpty = true;
    }

    /**
     * Construct a new BoundingBox from an existing BoundingBox using a deep
     * copy.
     *
     * @param bb The original BoundingBox.
     */
    public BoundingBox(final BoundingBox bb) {
        min = new Vector3f(bb.getMin());
        max = new Vector3f(bb.getMax());
        min2 = new Vector3f(bb.getMin2());
        max2 = new Vector3f(bb.getMax2());

        isEmpty = bb.isEmpty;
    }

    /**
     * Add a vertex to the bounded box.
     *
     * @param x X coordinate of vertex.
     * @param y Y coordinate of vertex.
     * @param z Z coordinate of vertex.
     */
    public void addVertex(final float x, final float y, final float z) {
        min.a[0] = Math.min(min.a[0], x);
        max.a[0] = Math.max(max.a[0], x);
        min.a[1] = Math.min(min.a[1], y);
        max.a[1] = Math.max(max.a[1], y);
        min.a[2] = Math.min(min.a[2], z);
        max.a[2] = Math.max(max.a[2], z);
    }

    /**
     * Add a secondary vertex to the bounded box.
     *
     * @param x X2 coordinate of vertex.
     * @param y Y2 coordinate of vertex.
     * @param z Z2 coordinate of vertex.
     */
    public void addVertex2(final float x, final float y, final float z) {
        min2.a[0] = Math.min(min2.a[0], x);
        max2.a[0] = Math.max(max2.a[0], x);
        min2.a[1] = Math.min(min2.a[1], y);
        max2.a[1] = Math.max(max2.a[1], y);
        min2.a[2] = Math.min(min2.a[2], z);
        max2.a[2] = Math.max(max2.a[2], z);
    }

    /**
     * Set the primary bounding box to MINIMUM_SIZE.
     */
    public void zero() {
        min.a[0] = -MINIMUM_SIZE;
        max.a[0] = MINIMUM_SIZE;
        min.a[1] = -MINIMUM_SIZE;
        max.a[1] = MINIMUM_SIZE;
        min.a[2] = -MINIMUM_SIZE;
        max.a[2] = MINIMUM_SIZE;
    }

    /**
     * Set the alternate bounding box to the same as the primary bounding box.
     */
    public void zero2() {
        min2 = min;
        max2 = max;
    }

    /**
     * Reset the bounding box.
     *
     * Minimums are set to Float.MAX_VALUE, maximums are set to
     * -Float.MAX_VALUE. When the minimum or maximum is retrieved, it easy to
     * tell if any vertexes have been added: the minimum is greater than the
     * maximum.
     *
     * @param isEmpty
     */
    public final void setEmpty(final boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    /**
     * Reset the min + max values to their opposite extremes so simple
     * comparisons work.
     */
    public final void resetMinMax() {
        min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        min2 = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        max2 = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
    }

    /**
     * Is the bounding box empty?
     *
     * @return True if the bounding box must be recalculated, false otherwise.
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Return the minimum (x,y,z) values of the bounding box.
     *
     * @return A Vector3f containing the minimum (x,y,z) values, or null if no
     * vertexes were added.
     */
    public Vector3f getBoundingBoxMinimum() {
        return min.a[0] <= max.a[0] ? min : null;
    }

    /**
     * Return the maximum (x,y,z) values of the bounding box.
     *
     * @return A Vector3f containing the maximum (x,y,z) values, or null if no
     * vertexes were added.
     */
    public Vector3f getBoundingBoxMaximum() {
        return max.a[0] >= min.a[0] ? max : null;
    }

    /**
     * Return the radius of the bounding cube.
     *
     * @return The radius of the bounding cube.
     */
    public float getCubeRadius() {
        if (isEmpty) {
            throw new IllegalArgumentException("Bounding box is empty.");
        }

        // Find the centre of the bounding box.
        final float cx = (min.getX() + max.getX()) / 2.0F;
        final float cy = (min.getY() + max.getY()) / 2.0F;
        final float cz = (min.getZ() + max.getZ()) / 2.0F;

        // The radius of the bounding cube is the distance from the centre to the furthest side of the cube.
        final float dx = max.getX() - cx;
        final float dy = max.getY() - cy;
        final float dz = max.getZ() - cz;
        return Math.max(Math.max(dx, dy), dz);
    }

    /**
     * Return the radius of the bounding sphere.
     * <p>
     * If there is only one vertex, or more generally the vertices are
     * co-positioned, the bounding box will be zero. In that case, return a
     * radius of one.
     *
     * @param mix The mix between xyz and x2y2z2.
     *
     * @return The radius of the bounding sphere.
     */
    public float getSphereRadius(final float mix) {
        if (isEmpty) {
            throw new IllegalArgumentException("Bounding box is empty.");
        }

        // Find the centre of the bounding box.
        final float cx = (min.getX() + max.getX()) / 2.0F;
        final float cy = (min.getY() + max.getY()) / 2.0F;
        final float cz = (min.getZ() + max.getZ()) / 2.0F;

        final float cx2 = (min2.getX() + max2.getX()) / 2.0F;
        final float cy2 = (min2.getY() + max2.getY()) / 2.0F;
        final float cz2 = (min2.getZ() + max2.getZ()) / 2.0F;

        // The radius of the bounding sphere is the distance from the centre to a corner of the box.
        // This isn't quite true; the radius of the bounding sphere is the distance to the further vertex.
        // Doing it using a corner gives a bigger sphere with less calculation.
        final float dx = Graphics3DUtilities.mix(max.getX(), max2.getX(), mix) - Graphics3DUtilities.mix(cx, cx2, mix);
        final float dy = Graphics3DUtilities.mix(max.getY(), max2.getY(), mix) - Graphics3DUtilities.mix(cy, cy2, mix);
        final float dz = Graphics3DUtilities.mix(max.getZ(), max2.getZ(), mix) - Graphics3DUtilities.mix(cz, cz2, mix);

        final float radius = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        return radius != 0F ? radius : 1F;
    }

    /**
     * Return the centre of the scene.
     * <p>
     * @param mix The mix ratio from the primary vertices to the secondary
     * vertices.
     *
     * @return The centre of the scene according to the specified mix.
     */
    public Vector3f getCentre(final float mix) {
        if (isEmpty) {
            return new Vector3f(0F, 0F, 0F);
        }

        final float cx = (min.getX() + max.getX()) / 2.0F;
        final float cy = (min.getY() + max.getY()) / 2.0F;
        final float cz = (min.getZ() + max.getZ()) / 2.0F;

        final float cx2 = (min2.getX() + max2.getX()) / 2.0F;
        final float cy2 = (min2.getY() + max2.getY()) / 2.0F;
        final float cz2 = (min2.getZ() + max2.getZ()) / 2.0F;

        return Graphics3DUtilities.mix(new Vector3f(cx, cy, cz), new Vector3f(cx2, cy2, cz2), mix);
    }

    /**
     * Determine how far should the camera be from the centre of the scene so
     * all of the scene is visible.
     *
     * The distance is determined using only the bounding sphere.
     *
     * @param fov The field of view being used.
     * @param mix The mix between xyz and x2y2z2.
     *
     * @return The distance of the camera from the centre of the scene.
     */
    public float getCameraDistance(final float fov, final float mix) {
        if (isEmpty) {
            return EMPTYBOX_CAMERA_DISTANCE;
        }

        // Find out how far the camera should be from the centre of the bounding sphere.
        float cameraDistance = getSphereRadius(mix) * (float) (1.0F / Math.tan(Math.toRadians(fov / 2.0F)));

        // Don't place the camera nearer than the near edge of the frustum.
        cameraDistance = Math.max(cameraDistance, Camera.PERSPECTIVE_NEAR);

        // If we select a single node of radius 1, the camera distance is about 3.1.
        // This zooms the node to fill the screen, which is a bit in-your-face, and we can't see the labels
        // or get a feel for the surrounding area.
        // Instead, we'll pull back a bit. The distance chosen is one that feels right, rather than anything mathematical.
        cameraDistance = Math.max(cameraDistance, MINIMUM_CAMERA_DISTANCE);

        return cameraDistance;
    }

    public Vector3f getMin() {
        return min;
    }

    public void setMin(final Vector3f min) {
        this.min = min;
    }

    public Vector3f getMax() {
        return max;
    }

    public void setMax(final Vector3f max) {
        this.max = max;
    }

    public Vector3f getMin2() {
        return min2;
    }

    public Vector3f getMax2() {
        return max2;
    }

    /**
     * Set the bounding box explicitly.
     *
     * @param min The minimum vector.
     * @param max The maximum vector.
     * @param min2 The alternate minimum vector.
     * @param max2 The alternate maximum vector.
     */
    public void set(final Vector3f min, final Vector3f max, final Vector3f min2, final Vector3f max2) {
        this.min = min;
        this.max = max;
        this.min2 = min2;
        this.max2 = max2;

        isEmpty = false;
    }
    
    /**
     * Method used for testing to check if BoundingBox values are equal
     * 
     * @param bbox the BoundingBox to compare to this instance
     * @return true if the BoundingBox are the same, false otherwise
     */
    public boolean areSame(final BoundingBox bbox) {
        return min.areSame(bbox.getMin()) && min2.areSame(bbox.getMin2()) && max.areSame(bbox.getMax()) && max2.areSame(bbox.getMax2()) && Boolean.compare(isEmpty, bbox.isEmpty()) == 0;
    }

    @Override
    public String toString() {
        return isEmpty
                ? "BoundingBox[isEmpty]"
                : String.format("BoundingBox[min=%s max=%s centre0=%s centre1=%s cradius=%f sradius0=%f sradius1=%f]", min, max, getCentre(0), getCentre(1), getCubeRadius(), getSphereRadius(0), getSphereRadius(1));
    }
}
