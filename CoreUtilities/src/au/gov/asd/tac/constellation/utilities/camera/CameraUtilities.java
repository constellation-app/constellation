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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Mathf;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.AxisConstants;

/**
 *
 * @author twilight_sparkle
 */
public class CameraUtilities {

    // Enforce a minimum distance so the user isn't overwhelmed,
    // and the label is probably visible.
    private static final float MIN_ZOOM_DISTANCE = 16F;

    private CameraUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static void refocusOnXAxis(final Camera camera, final BoundingBox bb, final boolean reverseDirection) {
        refocus(camera, reverseDirection ? AxisConstants.X_NEGATIVE : AxisConstants.X_POSITIVE, bb);
    }

    public static void refocusOnYAxis(final Camera camera, final BoundingBox bb, final boolean reverseDirection) {
        refocus(camera, reverseDirection ? AxisConstants.Y_NEGATIVE : AxisConstants.Y_POSITIVE, bb);
    }

    public static void refocusOnZAxis(final Camera camera, final BoundingBox bb, final boolean reverseDirection) {
        refocus(camera, reverseDirection ? AxisConstants.Z_NEGATIVE : AxisConstants.Z_POSITIVE, bb);
    }

    public static void moveEyeToOrigin(final Camera camera) {
        camera.lookAtCentre.subtract(camera.lookAtEye);
        camera.lookAtCentre.normalize();
        camera.lookAtEye.set(0, 0, 0);
    }
    
    /*
     * A helper method to refocuses the camera to look in a default direction at a specified
     * region.
     *
     * @param camera The camera object to refocus
     * @param axis A constant reference to specify fixed axis alignment
     * @param region The bounding box representing the region the camera should
     * be looking at
    */
    public static void refocus(final Camera camera, final AxisConstants axis, final BoundingBox region) {
        refocus(camera, axis.getForward(), axis.getUp(), region);
    }

    /**
     * Refocuses the camera to look in a specified direction at a specified
     * region.
     *
     * @param camera The camera object to refocus
     * @param unitEye A unit vector pointing in the direction the camera should
     * be looking
     * @param up A vector pointing in the desired up direction
     * @param region The bounding box representing the region the camera should
     * be looking at
     */
    public static void refocus(final Camera camera, final Vector3f unitEye, final Vector3f up, final BoundingBox region) {

        // If the graph is very small (for instance, one node), then the bounding box radius will be zero
        // and the graph will be closer than the near clipping plane.
        float cameraDistance = region.getCameraDistance(Camera.FIELD_OF_VIEW, camera.getMix());
        if (cameraDistance < MIN_ZOOM_DISTANCE) {
            cameraDistance = MIN_ZOOM_DISTANCE;
        }

        if (cameraDistance >= Camera.PERSPECTIVE_FAR) {
            cameraDistance = Camera.PERSPECTIVE_FAR - 1;
        }

        final Vector3f centre = region.getCentre(camera.getMix());
        final Vector3f eye = new Vector3f(unitEye);
        eye.scale(cameraDistance);
        eye.add(centre);

        setPreviousToCurrent(camera);
        camera.lookAtEye.set(eye);
        camera.lookAtCentre.set(centre);
        camera.lookAtUp.set(up);
        camera.lookAtRotation.set(centre);

        // The first time the scene is reset (when objectFrame is still null), there is no need for animation:
        // the scene has not previously been drawn.
        if (camera.getObjectFrame() == null) {
            camera.setObjectFrame(new Frame());
            camera.getObjectFrame().setForwardVector(0, 0, 1);
        }
    }

    public static void changeMixRatio(final Camera camera, final boolean increaseMix, final boolean toLimit) {
        if (toLimit) {
            camera.setMixRatio(increaseMix ? Camera.MIX_RATIO_MAX : Camera.MIX_RATIO_MIN);
        } else if (increaseMix && camera.getMixRatio() < Camera.MIX_RATIO_MAX) {
            camera.setMixRatio(camera.getMixRatio() + 1);
        } else if (!increaseMix && camera.getMixRatio() > Camera.MIX_RATIO_MIN) {
            camera.setMixRatio(camera.getMixRatio() - 1);
        }
    }

    public static void setRotationCentre(final Camera camera, final Vector3f rotationCentre) {
        camera.lookAtRotation.set(rotationCentre);
    }

    public static void pan(final Camera camera, final float xShift, final float yShift) {
        final Frame frame = new Frame(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp);
        frame.translateLocal(xShift, yShift, 0);
        final Vector3f tr = frame.getOrigin();
        camera.lookAtEye.add(tr);
        camera.lookAtCentre.add(tr);
    }

    /**
     * Sets the camera's current position to its previous position.
     *
     * @param camera
     */
    public static void setCurrentToPrevious(final Camera camera) {
        camera.lookAtEye.set(camera.lookAtPreviousEye);
        camera.lookAtCentre.set(camera.lookAtPreviousCentre);
        camera.lookAtUp.set(camera.lookAtPreviousUp);
        camera.lookAtRotation.set(camera.lookAtPreviousRotation);
    }

    /**
     * Sets the camera's previous position to its current position.
     *
     * @param camera
     */
    public static void setPreviousToCurrent(final Camera camera) {
        camera.lookAtPreviousEye.set(camera.lookAtEye);
        camera.lookAtPreviousCentre.set(camera.lookAtCentre);
        camera.lookAtPreviousUp.set(camera.lookAtUp);
        camera.lookAtPreviousRotation.set(camera.lookAtRotation);
    }

    /**
     * Spins the camera in place about the axis between it and the eye.
     *
     * @param camera The camera to spin
     * @param spinDegrees The number of degrees to spin.
     */
    public static void spin(final Camera camera, final float spinDegrees) {
        rotate(camera, 0, 0, spinDegrees);
    }

    /**
     * Rotates the camera around its centre of rotation, about its 'up' vector
     * and the axis mutually orthogonal to the 'up' and 'eye' vectors.
     *
     * @param camera the camera to perform rotation with.
     * @param xDegrees the amount in degrees to rotate in the x-direction (that
     * is about the axis orthogonal to both the 'up' and 'eye' vectors).
     * @param yDegrees the amount in degrees to rotate in the y-direction (that
     * is about the 'up' vector).
     *
     */
    public static void rotate(final Camera camera, final float xDegrees, final float yDegrees) {
        rotate(camera, xDegrees, yDegrees, 0);
    }

    public static void rotate(final Camera camera, final float xDegrees, final float yDegrees, final float zDegrees) {
        // Use a frame to move the eye and centre relative to the rotation point.
        final Frame frame = new Frame(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp);
        frame.setOrigin(camera.lookAtRotation);
        final Vector3f localCentre = new Vector3f();
        final Vector3f localEye = new Vector3f();
        frame.worldToLocal(camera.lookAtCentre, localCentre);
        frame.worldToLocal(camera.lookAtEye, localEye);

        if (xDegrees != 0) {
            frame.rotateLocal((float) Mathf.degToRad(xDegrees), 1.0F, 0.0F, 0.0F);
        }
        if (yDegrees != 0) {
            frame.rotateLocal((float) Mathf.degToRad(yDegrees), 0.0F, 1.0F, 0.0F);
        }
        if (zDegrees != 0) {
            frame.rotateLocal((float) Mathf.degToRad(zDegrees), 0.0F, 0.0F, 1.0F);
        }

        // Retrieve the rotated points from the frame.
        frame.localToWorld(localCentre, camera.lookAtCentre);
        frame.localToWorld(localEye, camera.lookAtEye);
        frame.getUpVector(camera.lookAtUp);
    }

    public static void zoom(final Camera camera, final int zoomAmount, final Vector3f zoomDirection, final float distanceToClosestNode) {
        zoomDirection.normalize();

        if (distanceToClosestNode < Float.MAX_VALUE) {
            if (zoomAmount > 0) {
                zoomDirection.scale(Math.max(zoomAmount, zoomAmount * distanceToClosestNode * 0.1F));
            } else {
                zoomDirection.scale(Math.min(zoomAmount, zoomAmount * distanceToClosestNode * 0.1F));
            }
        } else {
            zoomDirection.scale(zoomAmount);
        }

        final Frame frame = new Frame(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp);
        frame.translateLocal(zoomDirection.getX(), zoomDirection.getY(), zoomDirection.getZ());
        final Vector3f tr = frame.getOrigin();
        camera.lookAtEye.subtract(tr);
        camera.lookAtCentre.subtract(tr);
    }

    /**
     * Zoom so that the bounds of the bounding box are visible.
     * <p>
     * The direction of the zoom in 3D depends on the current lookat position.
     * If the display is in 2D mode, the eye is moved so that eye->centre is
     * parallel to the z-axis
     *
     * @param camera
     * @param box
     */
    public static void zoomToBoundingBox(final Camera camera, final BoundingBox box) {
        if (!box.isEmpty()) {
            final Vector3f centre = new Vector3f(box.getCentre(camera.getMix()));
            final Vector3f toEye = Vector3f.subtract(camera.lookAtEye, camera.lookAtCentre);
            toEye.normalize();

            final float cameraDistance = Math.max(box.getCameraDistance(Camera.FIELD_OF_VIEW, camera.getMix()), MIN_ZOOM_DISTANCE);
            toEye.scale(cameraDistance);
            final Vector3f eye = Vector3f.add(centre, toEye);

            setPreviousToCurrent(camera);
            camera.lookAtEye.set(eye);
            camera.lookAtCentre.set(centre);
            camera.lookAtRotation.set(centre);
        }
    }

    public static Vector3f getFocusVector(final Camera camera) {
        final Vector3f focusVector = new Vector3f(camera.lookAtCentre);
        focusVector.subtract(camera.lookAtEye);
        return focusVector;
    }
}
