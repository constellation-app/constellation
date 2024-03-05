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

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.visual.InteractiveGLVisualProcessor;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.preferences.DeveloperPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.Point;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Quasar985
 */
public class ZoomUtilities {

    /**
     * Function for zooming a camera's view by a given amount in a given direction.
     *
     * @param graph the graph that holds the camera
     * @param zoomMagnitude the amount of units to zoom by
     * @param zoomDirection the direction to zoom to as a 3d vector
     * @param distanceToClosestNode the distance to the closest node, used to influence zoom amount
     * @throws InterruptedException
     * @throws PluginException
     */
    static public void zoom(final GraphWriteMethods graph, final int zoomMagnitude, final Vector3f zoomDirection, float distanceToClosestNode) throws InterruptedException, PluginException {

        final Camera oldCamera = VisualGraphUtilities.getCamera(graph);
        // Graph wont update for some reason unless you do this
        final Camera camera = new Camera(oldCamera);

        // Distance is divided by 5 to closer match scrolling zoom
        //CameraUtilities.zoom(camera, zoomAmount, zoomDirection, distanceToClosestNode / 5);
        CameraUtilities.zoom(camera, zoomMagnitude, zoomDirection, distanceToClosestNode);

        // Skip the animation, just set the new camera position
        VisualGraphUtilities.setCamera(graph, camera);
    }

    /**
     * Function for zooming a camera's view by a given amount in a given direction. Variation of zoom that calculates
     * the distanceToClosestNode itself
     *
     * @param graph the graph that holds the camera
     * @param zoomMagnitude the amount of units to zoom by
     * @param zoomDirection the direction to zoom to as a 3d vector
     * @throws InterruptedException
     * @throws PluginException
     */
    static public void zoom(final GraphWriteMethods graph, final int zoomMagnitude, final Vector3f zoomDirection) throws InterruptedException, PluginException {

        final Camera camera = VisualGraphUtilities.getCamera(graph);

        final float dist = closestNodeToCamera(graph);
        final float distanceToClosestNode = (dist != Float.MAX_VALUE) ? dist : CameraUtilities.getFocusVector(camera).getLength();

        zoom(graph, zoomMagnitude, zoomDirection, distanceToClosestNode);
    }

    /**
     * Function that finds the distance of the closest node to the given point
     *
     * @param graph the graph that the nodes are on
     * @return
     */
//    static public float closestNodeToCamera(final GraphWriteMethods graph) {
//        float closestDist = Float.MAX_VALUE;
//        
//        // Null Checking
//        if (graph == null) {
//            return closestDist;
//        }
//        final Camera camera = VisualGraphUtilities.getCamera(graph);
//        if (camera == null) {
//            return closestDist;
//        }
//
//        final Vector3f point = camera.lookAtCentre;
//
//        final int vertexCount = graph.getVertexCount();
//        for (int position = 0; position < vertexCount; position++) {
//            final int vertexId = graph.getVertex(position);
//
//            // Get attribute id for each coord
//            final int xId = graph.getAttribute(GraphElementType.VERTEX, "x");
//            final int yId = graph.getAttribute(GraphElementType.VERTEX, "y");
//            final int zId = graph.getAttribute(GraphElementType.VERTEX, "z");
//
//            // Calculate difference between each coord of current vertex and given point
//            final float dx = point.getX() - graph.getFloatValue(xId, vertexId);
//            final float dy = point.getY() - graph.getFloatValue(yId, vertexId);
//            final float dz = point.getZ() - graph.getFloatValue(zId, vertexId);
//
//            // Calculate distance between current vertex and given point
//            final float currentDistance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
//
//            if (currentDistance < closestDist || closestDist == Float.MAX_VALUE) {
//                closestDist = currentDistance;
//            }
//
//        }
//        return closestDist;
//    }
    static public float closestNodeToCamera(final GraphWriteMethods graph) {

        if (graph == null) {
            return Float.MAX_VALUE;
        }

        final Camera camera = VisualGraphUtilities.getCamera(graph);

        if (camera == null) {
            return Float.MAX_VALUE;
        }

        // Creates object responsible for getting closest node to camera, relative to point (0, 0) on the screen (top left corner of graph view)
        final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
        if (prefs == null) {
            return Float.MAX_VALUE;
        }

        final InteractiveGLVisualProcessor processor = new InteractiveGLVisualProcessor(prefs.getBoolean(DeveloperPreferenceKeys.DEBUG_GL, DeveloperPreferenceKeys.DEBUG_GL_DEFAULT), prefs.getBoolean(DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES, DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES_DEFAULT));
        if (processor == null) {
            return Float.MAX_VALUE;
        }

        final Vector3f closest = processor.closestNodeCameraCoordinates(graph, camera, new Point(0, 0));

        return closest == null ? Float.MAX_VALUE : closest.getLength();
    }
}
