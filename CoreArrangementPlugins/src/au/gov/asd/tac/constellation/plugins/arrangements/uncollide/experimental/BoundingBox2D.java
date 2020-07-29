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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;

/**
 * This class is designed to provide a 2D bounding box for a graph.
 * <p>
 * 
 * @author algol
 * @author Nova
 */
class BoundingBox2D { 
    final float minX;
    final float minY;
    final float maxX;
    final float maxY;
    final float midX;
    final float midY;
    
    /**
     * Generate a 2D bounding box for the graph.
     * <p>
     * This method creates a bounding box for the verticies of a given graph.
     * It does this by finding the extremes of both the X and Y axis.
     * These values are then made available as attributes of the class instance.
     * 
     * @param wg  the graph
     * @return  instance of class BoundingBox2D based on input graph
     */
    BoundingBox2D(final GraphReadMethods wg) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());

        float minX = wg.getFloatValue(xId, wg.getVertex(0));
        float minY = wg.getFloatValue(yId, wg.getVertex(0));
        float maxX = minX;
        float maxY = minY;


        final int vxCount = wg.getVertexCount();
        if(vxCount == 0) {
            throw new IllegalArgumentException("Graph must contain atleast one vertex to find BoundingBox");
        }
              
        for (int position = 1; position < vxCount; position++) {
            final int vxId = wg.getVertex(position);

            final float x = wg.getFloatValue(xId, vxId);
            final float y = wg.getFloatValue(yId, vxId);
            
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }

        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.midX = minX + (maxX - minX)*(float)0.5;
        this.midY = minY + (maxY - minY)*(float)0.5;
    }

    private BoundingBox2D(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.midX = minX + (maxX - minX)*(float)0.5;
        this.midY = minY + (maxY - minY)*(float)0.5;
    }

    /**
     * Gets BoundingBox2D of the top left quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D topLeftQuadrant() {
        return new BoundingBox2D(minX, midX, midY, maxY);
    }
    
    /**
     * Gets BoundingBox2D of the top right quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D topRightQuadrant() {
        return new BoundingBox2D(midX, maxX, midY, maxY);
    }
    
    /**
     * Gets BoundingBox2D of the bottom left quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D bottomLeftQuadrant() {
        return new BoundingBox2D(minX, midX, minY, midY);
    }
    
    /**
     * Gets BoundingBox2D of the bottom right quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D bottomRightQuadrant() {
        return new BoundingBox2D(midX, maxX, minY, midY);
    }  

    @Override
    public String toString() {
        return String.format("Box: Min(%f, %f), Max(%f, %f)", minX, minY, maxX, maxY);
    }
}
