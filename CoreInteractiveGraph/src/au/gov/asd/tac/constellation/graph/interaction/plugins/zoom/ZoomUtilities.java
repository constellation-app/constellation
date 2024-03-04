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
package au.gov.asd.tac.constellation.graph.interaction.plugins.zoom;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quasar985
 */
public class ZoomUtilities {

    private static final Logger LOGGER = Logger.getLogger(ZoomUtilities.class.getName());

    static public void zoom(final GraphWriteMethods graph, final int zoomAmount, final Vector3f zoomDirection) throws InterruptedException, PluginException {
        final Camera oldCamera = VisualGraphUtilities.getCamera(graph);
        final Camera camera = new Camera(oldCamera);

        final float dist = closestNodeDistance(graph, camera.lookAtCentre);
        final float distanceToClosestNode = (dist != Float.MAX_VALUE) ? dist : CameraUtilities.getFocusVector(camera).getLength();

        // Distance is devidied by 5 to closer match scrolling zoom
        CameraUtilities.zoom(camera, zoomAmount, zoomDirection, distanceToClosestNode / 5);

        // Skip the animation, just set the new camera position
        VisualGraphUtilities.setCamera(graph, camera);
    }

    static public float closestNodeDistance(final GraphWriteMethods graph, Vector3f point) {
        float closestDist = Float.MAX_VALUE;

        final int vertexCount = graph.getVertexCount();
        for (int position = 0; position < vertexCount; position++) {
            final int vertexId = graph.getVertex(position);
            
            // Get attribute id for each coord
            final int xId = graph.getAttribute(GraphElementType.VERTEX, "x");
            final int yId = graph.getAttribute(GraphElementType.VERTEX, "y");
            final int zId = graph.getAttribute(GraphElementType.VERTEX, "z");

            // Calculate difference between each coord of current vertex and given point
            final float dx = point.getX() - graph.getFloatValue(xId, vertexId);
            final float dy = point.getY() - graph.getFloatValue(yId, vertexId);
            final float dz = point.getZ() - graph.getFloatValue(zId, vertexId);
            
            // Calculate distance between current vertex and given point
            final float currentDistance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (currentDistance < closestDist || closestDist == Float.MAX_VALUE) {
                closestDist = currentDistance;
            }

        }
        return closestDist;
    }
}
