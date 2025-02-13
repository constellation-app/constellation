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
package au.gov.asd.tac.constellation.graph.interaction.framework;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.Point;

/**
 * Methods that translate from gestures in window coordinates, to values/actions
 * that make sense for the graph. The implementation details will be particular
 * to each VisualManager/VisualProcessor pair. For example, selecting nodes in a
 * box is an operation that theoretically makes sense for any visualisation of a
 * graph that allows interaction via swing events in window coordinates. However
 * the actual vertices that get selected given a specific box will depend
 * entirely on the visualisation's given method of projecting the 3D locations
 * of vertices from the graph into window coordinates. A visualisation is also
 * technically free to do whatever it likes, e.g. always show all vertices in a
 * grid (regardless of x,y,z on the graph) and hence box selection would work
 * very differently in this case
 *
 * Note that a key feature of the methods of this class is that they take inputs
 * in window coordinates, and produce outcomes or return results based on either
 * camera or graph coordinates. It is precisely the projection between window
 * coordinates and these other two coordinate systems that is specific to a give
 * VisualInteraction implementation. The projection between camera coordinates
 * and graph coordinates, however, is considered fixed throughout the
 * application (as the Camera is a part of the graph). Methods that work between
 * these two coordinate systems only (and do not involve window coordinates)
 * belong in static utility classes, CameraUtilities being the most prominent
 * example.
 *
 * @author twilight_sparkle
 */
public interface VisualInteraction {

    /**
     * Calculate the direction vector in which to zoom, in camera coordinates,
     * based on a point at which a zoom was requested, in window coordinates.
     *
     * @param point The point in window coordinates at which a zoom was
     * requested.
     * @return A Vector holding the direction to zoom in camera coordinates.
     */
    public Vector3f convertZoomPointToDirection(final Point point);

    /**
     * Calculates the camera coordinates of the node in the graph which is
     * 'closest' to the supplied point in window coordinates.
     *
     * Note that implementations may interpret 'closest' to prefer nodes that
     * are visible to those that aren't, but this should always return the
     * camera coordinates of a node unless the supplied graph is empty.
     *
     * @param graph The graph to search
     * @param camera The current Camera
     * @param point The point in window coordinates
     * @return A vector giving the camera coordiantes of the 'closest' node, or
     * null if the graph is empty.
     */
    public Vector3f closestNodeCameraCoordinates(final GraphReadMethods graph, final Camera camera, final Point point);

    /**
     * Unprojects the supplied point from window coordinates to graph
     * coordinates, placing it on the plane through a point just in front of the
     * camera and normal to the direction the camera is looking.
     *
     * @param camera The current camera.
     * @param point The point in window coordinates.
     * @return A vector representing the supplied point in graph coordinates
     */
    public Vector3f windowToGraphCoordinates(final Camera camera, final Point point);

    /**
     * Converts a translation between two points in window coordinates to an
     * amount of spin. This is needed when translation gestures are used to
     * rotate the graph.
     *
     * @param from The starting point of the translation
     * @param to The end point of the translation.
     * @return A float representing the number of degrees to spin the graph
     * based on the supplied translation.
     */
    public float convertTranslationToSpin(final Point from, final Point to);

    /**
     * Converts a translation between two points in window coordinates to a
     * vector in camera coordinates to pan, having regard to the node which is
     * closest to the camera. This is needed when translation gestures are used
     * to pan the graph's camera. Note that the z-component of the pan vector
     * should usually be zero.
     *
     * @param from The starting point of the translation
     * @param to The end point of the translation.
     * @param panReferencePoint
     * @return A vector in camera coordinates representing the pan that should
     * be performed.
     */
    public Vector3f convertTranslationToPan(final Point from, final Point to, final Vector3f panReferencePoint);

    /**
     * Converts the translation between two points in window coordinates, with
     * consideration to the supplied position of a node in graph coordinates, to
     * a vector in graph coordinates indicating the translation that should be
     * applied to the supplied node (and any other nodes being dragged).
     *
     * @param camera The current camera.
     * @param nodeLocation The graph coordinates of the primary node being
     * dragged.
     * @param from The starting point of the translation
     * @param to The end point of the translation.
     * @return A vector in graph coordinates indicating the translation that
     * should be applied to any nodes being dragged.
     */
    public Vector3f convertTranslationToDrag(final Camera camera, final Vector3f nodeLocation, final Point from, final Point to);

    /**
     * Converts the rectangle described by the four input parameters from window
     * coordinates to camera coordinates.
     *
     * @param left The left edge of the rectangle in window coordinates
     * @param right The right edge of the rectangle in window coordinates
     * @param top The top edge of the rectangle in window coordinates
     * @param bottom The bottom edge of the rectangle in window coordinates
     * @return A float array of length four describing the left, right, top and
     * bottom edges of the rectangle in camera coordinates.
     */
    public float[] windowBoxToCameraBox(final int left, final int right, final int top, final int bottom);

    /**
     * Returns the global DPI Scale factor used to convert mouse coordinates to
     * the correct values.
     *
     * @return A float value describing the scale factor
     */
    public float getDPIScalingFactor();
}
