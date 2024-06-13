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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import java.awt.Point;

/**
 * A logical model for drawing selection boxes as an annotation layer on top of
 * a graph.
 *
 * @author twilight_sparkle
 */
public class SelectionBoxModel {

    private final Point startPoint;
    private final Point endPoint;

    /**
     * Create a model with no corner points signifying that no selection box
     * should be displayed.
     *
     * @return A model with no corner points.
     */
    public static SelectionBoxModel getClearModel() {
        return new SelectionBoxModel(null, null);
    }

    /**
     * Create a model with the specified corner points.
     *
     * @param startPoint The starting corner of the selection box.
     * @param endPoint The end corner of the selection box (diagonally opposite
     * to the start corner).
     */
    public SelectionBoxModel(final Point startPoint, final Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    /**
     * Is the model clear, ie. has no corner points and shouldn't be drawn.
     *
     * @return Whether or not this model is clear.
     */
    public boolean isClear() {
        return endPoint != null;
    }

    /**
     * Get the starting corner point of the selection box described by this
     * model
     *
     * @return A 3D vector representing the start of the selection box, or
     * <code>null</code> if the model is clear (represents no selection box).
     */
    public Point getStartPoint() {
        return startPoint;
    }

    /**
     * Get the end corner point of the selection box (diagonally opposite to the
     * start corner) described by this model
     *
     * @return A 3D vector representing the end of the selection box, or
     * <code>null</code> if the model is clear (represents no selection box).
     */
    public Point getEndPoint() {
        return endPoint;
    }

}
