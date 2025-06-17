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
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.io.Serializable;

/**
 * This class holds the state of the camera used by the renderer.
 * <p>
 * A Camera is described by four vectors; the 'eye' at which the camera itself
 * is located, the 'centre' at which the camera is focused, the 'up' which
 * points vertically upwards relative to the camera, and the 'rotation' at which
 * the camera will be rotated about if so requested.
 * <p>
 * These camera objects contain these four vectors, as well as previous values
 * of these vectors, information about low and high visibility thresholds (which
 * can be thought of as filters on the lens), information giving the mix between
 * primary and alternate coordinates, and some internal metrics such as a
 * bounding box and a frame which aid in computations.
 *
 * @author algol
 */
public final class Camera implements Serializable {

    public static final float PERSPECTIVE_NEAR = 1;
    public static final float PERSPECTIVE_FAR = 500000;

    public static final float FIELD_OF_VIEW = 35;

    public static final int MIX_RATIO_MIN = 0;
    public static final int MIX_RATIO_MAX = 20;

    public final Vector3f lookAtEye;
    public final Vector3f lookAtCentre;
    public final Vector3f lookAtUp;
    public final Vector3f lookAtRotation;

    public final Vector3f lookAtPreviousEye;
    public final Vector3f lookAtPreviousCentre;
    public final Vector3f lookAtPreviousUp;
    public final Vector3f lookAtPreviousRotation;
    public final BoundingBox boundingBox;

    /**
     * The name of the graph attribute used to hold the instance.
     */
    public static final String ATTRIBUTE_NAME = "camera";

    // The scene.
    private Frame objectFrame;

    // Visibility; low and high range 0..1.
    private float visibilityLow;
    private float visibilityHigh;

    private int mixRatio;

    @Override
    public String toString() {
        return String.format("Camera[eye: %s; centre: %s; up: %s]", lookAtEye, lookAtCentre, lookAtUp);
    }

    /**
     * Construct a new Camera.
     */
    public Camera() {
        lookAtEye = new Vector3f(0, 0, 10);
        lookAtCentre = new Vector3f(0, 0, 0);
        lookAtUp = new Vector3f(0, 1, 0);
        lookAtRotation = new Vector3f(lookAtCentre);
        lookAtPreviousEye = new Vector3f(lookAtEye);
        lookAtPreviousCentre = new Vector3f(lookAtCentre);
        lookAtPreviousUp = new Vector3f(lookAtUp);
        lookAtPreviousRotation = new Vector3f(lookAtRotation);
        boundingBox = new BoundingBox();

        visibilityLow = 0;
        visibilityHigh = 1;

        objectFrame = new Frame();
        objectFrame.setForwardVector(0, 0, 1);
    }

    /**
     * Construct a new Camera from an existing Camera using a deep copy.
     *
     * @param camera The original Camera.
     */
    public Camera(Camera camera) {
        if (camera == null) {
            camera = new Camera();
        }
        lookAtCentre = new Vector3f(camera.lookAtCentre);
        lookAtEye = new Vector3f(camera.lookAtEye);
        lookAtUp = new Vector3f(camera.lookAtUp);
        lookAtRotation = new Vector3f(camera.lookAtRotation);
        lookAtPreviousEye = new Vector3f(camera.lookAtPreviousEye);
        lookAtPreviousCentre = new Vector3f(camera.lookAtPreviousCentre);
        lookAtPreviousUp = new Vector3f(camera.lookAtPreviousUp);
        lookAtPreviousRotation = new Vector3f(camera.lookAtPreviousRotation);

        objectFrame = camera.objectFrame == null ? null : new Frame(camera.objectFrame);

        visibilityLow = camera.visibilityLow;
        visibilityHigh = camera.visibilityHigh;
        mixRatio = camera.mixRatio;
        boundingBox = camera.boundingBox;
    }

    public Frame getObjectFrame() {
        return objectFrame;
    }

    public void setObjectFrame(final Frame objectFrame) {
        this.objectFrame = objectFrame;
    }

    public float getVisibilityLow() {
        return visibilityLow;
    }

    public void setVisibilityLow(final float visibilityLow) {
        this.visibilityLow = visibilityLow;
    }

    public float getVisibilityHigh() {
        return visibilityHigh;
    }

    public void setVisibilityHigh(final float visibilityHigh) {
        this.visibilityHigh = visibilityHigh;
    }

    public int getMixRatio() {
        return mixRatio;
    }

    public void setMixRatio(final int mixRatio) {
        this.mixRatio = mixRatio;
    }

    /**
     * Get the mix between primary and alternate coordinates that this camera is
     * viewing the world in.
     * <p>
     * This value is between 0 and 1 and describes the convex combination of
     * primary and alternate coordinates. Hence if this is value is 0, the
     * Camera is viewing the world solely in primary coordinates (typically
     * given by x, y, z attributes), while if it is 1, the Camera is viewing the
     * world solely in alternate coordinates (typically given by x2, y2, z2
     * attributes).
     *
     * @return A mix value between 0 and 1.
     */
    public float getMix() {
        return ((float) mixRatio) / (MIX_RATIO_MAX - MIX_RATIO_MIN);
    }
    
    /**
     * Method used for testing to check if camera values are equal
     * 
     * @param camera the camera to compare to this instance
     * @return true if the cameras are the same, false otherwise
     */
    public boolean areSame(final Camera camera) {
        return lookAtEye.areSame(camera.lookAtEye) 
                && lookAtCentre.areSame(camera.lookAtCentre) 
                && lookAtUp.areSame(camera.lookAtUp) 
                && lookAtRotation.areSame(camera.lookAtRotation) 
                && lookAtPreviousEye.areSame(camera.lookAtPreviousEye)
                && lookAtPreviousCentre.areSame(camera.lookAtPreviousCentre) 
                && lookAtPreviousUp.areSame(camera.lookAtPreviousUp) 
                && lookAtPreviousRotation.areSame(camera.lookAtPreviousRotation) 
                && visibilityLow == camera.visibilityLow
                && visibilityHigh == camera.visibilityHigh
                && mixRatio == camera.mixRatio
                && objectFrame.areSame(camera.objectFrame)
                && boundingBox.areSame(camera.boundingBox);
        
    }
}
