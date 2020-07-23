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
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Objects;

/**
 * This class is designed to provide a bounding box for a graph.
 * <p>
 * This class has subclasses for creating both 2D and 3D bounding boxes.
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
    BoundingBox2D(final GraphWriteMethods wg) {
        final int xId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.X.getName());
        final int yId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.Y.getName());
        final int rId = wg.getAttribute(GraphElementType.VERTEX, VisualConcept.VertexAttribute.NODE_RADIUS.getName());

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

    private BoundingBox2D(float minX, float minY, float maxX, float maxY) {
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
    BoundingBox2D getTopLeftQuadrant() {
        return new BoundingBox2D(minX, minY, midX, midY);
    }
    
    /**
     * Gets BoundingBox2D of the top right quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D getTopRightQuadrant() {
        return new BoundingBox2D(midX, minY, maxX, midY);
    }
    
    /**
     * Gets BoundingBox2D of the bottom left quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D getBottomLeftQuadrant() {
        return new BoundingBox2D(minX, midY, midX, maxY);
    }
    
    /**
     * Gets BoundingBox2D of the bottom right quadrant of this BoundingBox2D
     * 
     * @return  BoundingBox2D
     */
    BoundingBox2D getBottomRightQuadrant() {
        return new BoundingBox2D(midX, midY, maxX, maxY);
    }  

    @Override
    public String toString() {
        return String.format("Box: Min(%f, %f), Max(%f, %f)", minX, minY, maxX, maxY);
    }
}
