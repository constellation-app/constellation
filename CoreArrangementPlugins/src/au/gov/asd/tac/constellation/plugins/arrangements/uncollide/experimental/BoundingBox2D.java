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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 * This class is designed to provide a 2D bounding box for a graph.
 *
 * @author algol
 * @author Nova
 */
public class BoundingBox2D extends AbstractBoundingBox {

    /**
     * Generate a 2D bounding box for the graph.
     * <p>
     * This method creates a bounding box for the verticies of a given graph. It
     * does this by finding the extremes of both the X and Y axis. These values
     * are then made available as attributes of the class instance.
     *
     * @param wg the graph
     * @return instance of class BoundingBox2D based on input graph
     */
    protected BoundingBox2D(final GraphReadMethods wg) {
        super(wg);
    }

    private BoundingBox2D(final float minX, final float maxX, final float minY, final float maxY) {
        super(minX, maxX, minY, maxY);
    }

    /**
     * Gets BoundingBox2D of the top left quadrant of this BoundingBox2D
     *
     * @return BoundingBox2D
     */
    protected BoundingBox2D topLeftQuadrant() {
        return new BoundingBox2D(minX, midX, midY, maxY);
    }

    /**
     * Gets BoundingBox2D of the top right quadrant of this BoundingBox2D
     *
     * @return BoundingBox2D
     */
    protected BoundingBox2D topRightQuadrant() {
        return new BoundingBox2D(midX, maxX, midY, maxY);
    }

    /**
     * Gets BoundingBox2D of the bottom left quadrant of this BoundingBox2D
     *
     * @return BoundingBox2D
     */
    protected BoundingBox2D bottomLeftQuadrant() {
        return new BoundingBox2D(minX, midX, minY, midY);
    }

    /**
     * Gets BoundingBox2D of the bottom right quadrant of this BoundingBox2D
     *
     * @return BoundingBox2D
     */
    protected BoundingBox2D bottomRightQuadrant() {
        return new BoundingBox2D(midX, maxX, minY, midY);
    }
}
