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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * Provides an abstract representation of an N dimensional box that represents
 * the boundary of a graph.
 * <p>
 * N must be 2 or greater.
 *
 * @author Nova
 */
public class AbstractBoundingBox {

    protected final float minX;
    protected final float minY;
    protected final float maxX;
    protected final float maxY;
    protected final float midX;
    protected final float midY;

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
    protected AbstractBoundingBox(final GraphReadMethods wg) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());

        float minXObserved = wg.getFloatValue(xId, wg.getVertex(0));
        float minYObserved = wg.getFloatValue(yId, wg.getVertex(0));
        float maxXObserved = minXObserved;
        float maxYObserved = minYObserved;

        final int vxCount = wg.getVertexCount();
        if (vxCount == 0) {
            throw new IllegalArgumentException("Graph must contain atleast one vertex to find BoundingBox");
        }

        for (int position = 1; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);

            if (x < minXObserved) {
                minXObserved = x;
            }
            if (x > maxXObserved) {
                maxXObserved = x;
            }
            if (y < minYObserved) {
                minYObserved = y;
            }
            if (y > maxYObserved) {
                maxYObserved = y;
            }
        }

        this.minX = minXObserved;
        this.minY = minYObserved;
        this.maxX = maxXObserved;
        this.maxY = maxYObserved;
        this.midX = minXObserved + (maxXObserved - minXObserved) * (float) 0.5;
        this.midY = minYObserved + (maxYObserved - minYObserved) * (float) 0.5;
    }

    protected AbstractBoundingBox(final float minX, final float maxX, final float minY, final float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.midX = minX + (maxX - minX) * (float) 0.5;
        this.midY = minY + (maxY - minY) * (float) 0.5;
    }

}
