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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;

/**
 * A logical model for drawing new lines (lines that are in the process of being
 * created) as an annotation layer on top of a graph.
 *
 * @author twilight_sparkle
 */
public class NewLineModel {

    private final Vector3f startLocation;
    private final Vector3f endLocation;
    private final Camera camera;

    /**
     * Create a model with no endpoints signifying that no line should be
     * displayed.
     *
     * @param camera The camera to associate with the new line model.
     * @return A model with no line endpoints.
     */
    public static NewLineModel getClearModel(final Camera camera) {
        return new NewLineModel(null, null, camera);
    }

    /**
     * Create a model with the specified endpoints and {@link Camera}
     *
     * @param startLocation The origin of the newline
     * @param endLocation The end of the newline
     * @param camera The camera to associate with the new line model
     */
    public NewLineModel(final Vector3f startLocation, final Vector3f endLocation, final Camera camera) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.camera = camera;
    }

    /**
     * Is the model clear, ie. has no endpoints and shouldn't be drawn.
     *
     * @return Whether or not this model is clear.
     */
    public boolean isClear() {
        return endLocation == null;
    }

    /**
     * Get the {@link Camera} associated with this newline model.
     *
     * @return The {@link Camera} associated with the newline model.
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Get the starting location, or origin, of the newline described by this
     * model
     *
     * @return A 3D vector representing the start of the line, or
     * <code>null</code> if the model is clear (represents no newline).
     */
    public Vector3f getStartLocation() {
        return startLocation;
    }

    /**
     * Get the end location of the newline described by this model
     *
     * @return A 3D vector representing the end of the line, or
     * <code>null</code> if this model is clear (represents no newline).
     */
    public Vector3f getEndLocation() {
        return endLocation;
    }

}
