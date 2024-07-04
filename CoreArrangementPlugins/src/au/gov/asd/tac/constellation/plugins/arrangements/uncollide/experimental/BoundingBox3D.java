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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * This class is designed to provide a 3D bounding box for a graph.
 * <p>
 *
 * @author algol
 * @author Nova
 */
public class BoundingBox3D extends AbstractBoundingBox {

    protected final float minZ;
    protected final float maxZ;
    protected final float midZ;

    /**
     * Generate a 3D bounding box for the graph.
     * <p>
     * This method creates a bounding box for the verticies of a given graph. It
     * does this by finding the extremes of the X,Y and Z axis. These values are
     * then made available as attributes of the class instance.
     *
     * @param wg the graph
     * @return instance of class BoundingBox2D based on input graph
     */
    protected BoundingBox3D(final GraphReadMethods wg) {
        super(wg);

        final int zId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());

        float minZObserved = wg.getFloatValue(zId, wg.getVertex(0));
        float maxZObserved = minZObserved;

        final int vxCount = wg.getVertexCount();
        if (vxCount == 0) {
            throw new IllegalArgumentException("Graph must contain atleast one vertex to find BoundingBox");
        }

        for (int position = 1; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);
            final float z = wg.getFloatValue(zId, vxId);

            if (z < minZObserved) {
                minZObserved = z;
            }
            if (z > maxZObserved) {
                maxZObserved = z;
            }
        }

        this.minZ = minZObserved;
        this.maxZ = maxZObserved;
        this.midZ = minZObserved + (maxZObserved - minZObserved) * (float) 0.5;
    }

    private BoundingBox3D(final float minX, final float maxX, final float minY, final float maxY, final float minZ, final float maxZ) {
        super(minX, maxX, minY, maxY);

        this.minZ = minZ;
        this.maxZ = maxZ;
        this.midZ = minZ + (maxZ - minZ) * (float) 0.5;
    }

    /**
     * Gets BoundingBox3D of the top left front octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D topLeftFrontOctant() {
        return new BoundingBox3D(minX, midX, midY, maxY, midZ, maxZ);
    }

    /**
     * Gets BoundingBox3D of the top right front octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D topRightFrontOctant() {
        return new BoundingBox3D(midX, maxX, midY, maxY, midZ, maxZ);
    }

    /**
     * Gets BoundingBox3D of the bottom left front octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D bottomLeftFrontOctant() {
        return new BoundingBox3D(minX, midX, minY, midY, midZ, maxZ);
    }

    /**
     * Gets BoundingBox3D of the bottom right front octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D bottomRightFrontOctant() {
        return new BoundingBox3D(midX, maxX, minY, midY, midZ, maxZ);
    }

    /**
     * Gets BoundingBox3D of the top left back octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D topLeftBackOctant() {
        return new BoundingBox3D(minX, midX, midY, maxY, minZ, midZ);
    }

    /**
     * Gets BoundingBox3D of the top right back octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D topRightBackOctant() {
        return new BoundingBox3D(midX, maxX, midY, maxY, minZ, midZ);
    }

    /**
     * Gets BoundingBox3D of the bottom left back octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D bottomLeftBackOctant() {
        return new BoundingBox3D(minX, midX, minY, midY, minZ, midZ);
    }

    /**
     * Gets BoundingBox3D of the bottom right back octant of this BoundingBox3D
     *
     * @return BoundingBox3D
     */
    protected BoundingBox3D bottomRightBackOctant() {
        return new BoundingBox3D(midX, maxX, minY, midY, minZ, midZ);
    }
}
