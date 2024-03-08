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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.visual.InteractiveGLVisualProcessor;
import au.gov.asd.tac.constellation.graph.visual.utilities.VisualGraphUtilities;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.CameraUtilities;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.util.stream.Stream;

/**
 *
 * @author Quasar985
 */
/**
 * Class for shared functionality of Zoom plugins. Contains functionality for gathering parameters needed to call
 * CameraUtiltities.zoom with what info plugins are able to access.
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
    public static void zoom(final GraphWriteMethods graph, final int zoomMagnitude, final Vector3f zoomDirection, final float distanceToClosestNode) throws InterruptedException, PluginException {

        final Camera oldCamera = VisualGraphUtilities.getCamera(graph);
        // Screen wont update for some reason unless you do this
        final Camera camera = new Camera(oldCamera);

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
    public static void zoom(final GraphWriteMethods graph, final int zoomMagnitude, final Vector3f zoomDirection) throws InterruptedException, PluginException {

        final Camera camera = VisualGraphUtilities.getCamera(graph);

        final Vector3f closestNode = closestNodeCameraCoordinates(graph);
        final float distanceToClosestNode = (closestNode != null) ? closestNode.getLength() : CameraUtilities.getFocusVector(camera).getLength();

        zoom(graph, zoomMagnitude, zoomDirection, distanceToClosestNode);
    }

    /**
     * Function for finding the closest node to the camera in a given graph
     *
     * @param graph the graph that holds the camera and nodes
     */
    public static Vector3f closestNodeCameraCoordinates(final GraphReadMethods graph) {

        if (graph == null) {
            return null;
        }

        final Camera camera = VisualGraphUtilities.getCamera(graph);

        if (camera == null) {
            return null;
        }

        // Calculate the height and width of the viewing frustrum as a function of distance from the camera
        final float verticalScale = (float) (Math.tan(Math.toRadians(Camera.FIELD_OF_VIEW / 2.0)));
        final float horizontalScale = verticalScale;

        // Iterate through the camera locations of each node in the graph
        final Stream<InteractiveGLVisualProcessor.NodeCameraDistance> nodeCameraDistances = VisualGraphUtilities.streamVertexSceneLocations(graph, camera)
                .parallel()
                .map(vector -> new InteractiveGLVisualProcessor.NodeCameraDistance(vector, horizontalScale, verticalScale));

        final InteractiveGLVisualProcessor.NodeCameraDistance closest = nodeCameraDistances.parallel().reduce(new InteractiveGLVisualProcessor.NodeCameraDistance(), (ncd1, ncd2) -> InteractiveGLVisualProcessor.NodeCameraDistance.getClosestNode(ncd1, ncd2));

        return closest.GetNodeLocation();
    }
}
