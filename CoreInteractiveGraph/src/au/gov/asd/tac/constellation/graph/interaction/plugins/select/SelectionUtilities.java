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
package au.gov.asd.tac.constellation.graph.interaction.plugins.select;

import au.gov.asd.tac.constellation.utilities.graphics.Matrix33f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;

/**
 * A set of Utility functions for interactive selection plugins
 *
 * @author antares
 */
public class SelectionUtilities {
    
    private SelectionUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Does the given line segment intersect the given axis-aligned rectangle?
     * <p>
     * We assume that the ends of the line segment are outside of the rectangle.
     *
     *
     * @param x1 x-coordinate of first end of line segment
     * @param y1 y-coordinate of first end line segment
     * @param x2 x-coordinate of second end of line segment
     * @param y2 y-coordinate of second end of line segment
     * @param minX min x-coordinate of rectangle
     * @param minY min y-coordinate of rectangle
     * @param maxX max x-coordinate of rectangle
     * @param maxY max y-coordinate of rectangle
     * @return
     */
    public static boolean lineSegmentIntersectsRectangle(final float x1, final float y1, final float x2, final float y2,
            final float minX, final float minY, final float maxX, final float maxY) {
        // Completely outside.
        if ((x1 <= minX && x2 <= minX) || (y1 <= minY && y2 <= minY) || (x1 >= maxX && x2 >= maxX) || (y1 >= maxY && y2 >= maxY)) {
            return false;
        }

//        // Either end completely inside.
//        // Not required if it is assumed that endpoints are outside the rectangle.
//        //        if((x1 > minX && x1 < maxX && y1 > minY && y1 < maxY) || (x2 > minX && x2 < maxX && y2 > minY && y2 < maxY))
//        {
//            return true;
//        }
        // Vertical line.
        // We know now that the end-points are outside the rectangle, so we only have to worry
        // that the line is between the sides.
        if (x1 == x2) {
            // At this point minX <= x1 is always true
            return x1 <= maxX;
        }

        // Slope of line segment.
        final float m = (y2 - y1) / (x2 - x1);

        float y = m * (minX - x1) + y1;
        if (y > minY && y < maxY) {
            return true;
        }

        y = m * (maxX - x1) + y1;
        if (y > minY && y < maxY) {
            return true;
        }

        float x = (minY - y1) / m + x1;
        if (x > minX && x < maxX) {
            return true;
        }

        x = (maxY - y1) / m + x1;

        return x > minX && x < maxX;
    }
    
    /**
     * Takes the x,y,z coordinate of a point in the world and translates it into
     * the equivalent coordinate in the current scene.
     * 
     * @param x
     * @param y
     * @param z
     * @param centre
     * @param rotationMatrix
     * @param cameraDistance
     * @return A vector representing the scene location
     */
    public static Vector3f convertWorldToScene(final float x, final float y, final float z, final Vector3f centre, final Matrix33f rotationMatrix, final float cameraDistance) {
        // Convert world coordinates to camera coordinates.
        final Vector3f worldLocation = new Vector3f();
        final Vector3f sceneLocation = new Vector3f();
        worldLocation.set(x, y, z);
        worldLocation.subtract(centre);
        sceneLocation.rotate(worldLocation, rotationMatrix);
        sceneLocation.setZ(sceneLocation.getZ() - cameraDistance);
        return sceneLocation;
    }
}
