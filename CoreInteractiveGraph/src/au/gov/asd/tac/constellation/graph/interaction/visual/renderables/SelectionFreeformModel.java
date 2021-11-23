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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A logical model for drawing selection freeform as an annotation layer on top
 * of a graph.
 *
 * @author CrucisGamma
 */
public class SelectionFreeformModel {

    private List<Point> points = null;

    private static final int BIG_NUMBER = 1000000;
    private static final int BIG_NEGATIVE_NUMBER = -BIG_NUMBER;
    private static final int MAX_POINTS = 50;

    private static final Point ZERO_POINT = new Point(0, 0);
    private int leftMost = BIG_NUMBER;
    private int rightMost = BIG_NEGATIVE_NUMBER;
    private int bottomMost = BIG_NEGATIVE_NUMBER;
    private int topMost = BIG_NUMBER;

    private float leftBoxCameraCoordinates;
    private float rightBoxCameraCoordinates;
    private float topBoxCameraCoordinates;
    private float bottomBoxCameraCoordinates;

    public SelectionFreeformModel() {
        this.points = new ArrayList<>();
    }

    /**
     * Create a model with the specified points.
     *
     * @param startPoint The starting corner of the selection freeform.
     * @param endPoint The end corner of the selection freeform (diagonally
     * opposite to the start corner).
     */
    public SelectionFreeformModel(final Point startPoint, final Point endPoint) {
        this.points = new ArrayList<>();
        points.add(startPoint);
        points.add(endPoint);
    }

    /**
     * Add a new point to an existing model
     *
     * @param nextPoint The next point the user wants to add to the start
     * corner).
     */
    private static final int THRESHOLD = 8;

    public void addPoint(final Point nextPoint) {
        if (nextPoint == null) {
            return;
        }

        if (getNumPoints() < 2) {
            points.add(nextPoint);
            return;
        }

        if (Math.abs(getEndPoint().getX() - nextPoint.getX()) > THRESHOLD
                && Math.abs(getEndPoint().getY() - nextPoint.getY()) > THRESHOLD) {
            if (leftMost > nextPoint.getX()) {
                leftMost = (int) nextPoint.getX();
            }
            if (rightMost < nextPoint.getX()) {
                rightMost = (int) nextPoint.getX();
            }
            if (topMost > nextPoint.getY()) {
                topMost = (int) nextPoint.getY();
            }
            if (bottomMost < nextPoint.getY()) {
                bottomMost = (int) nextPoint.getY();
            }
            points.add(nextPoint);
        }
    }

    /**
     * Create a model with no corner points signifying that no selection
     * freeform should be displayed.
     *
     * @return A model with no corner points.
     */
    public static SelectionFreeformModel getClearModel() {
        return new SelectionFreeformModel(null, null);
    }

    public int getNumPoints() {
        if (this.points == null) {
            return 0;
        }

        for (int i = 0; i < this.points.size(); i++) {
            if (this.points.get(i) == null) {
                return 0;
            }
        }

        if (points.size() > MAX_POINTS - 2) {
            return MAX_POINTS - 2;
        }
        return points.size();
    }

    /**
     * Is the model clear
     *
     * @return Whether or not this model is clear.
     */
    public boolean isClear() {
        return getNumPoints() > 0;
    }

    /**
     * Get the starting corner point of the selection freeform described by this
     * model
     *
     * @return A point indicating the start of the selection freeform, or a
     * zeroised point if the model is clear (represents no selection freeform).
     */
    public Point getStartPoint() {
        if (!isClear()) {
            return ZERO_POINT;
        }
        return getPoint(0);
    }

    /**
     * Get the end corner point of the selection freeform (diagonally opposite
     * to the start corner) described by this model
     *
     * @return A 3D vector representing the end of the selection freeform, or
     * <code>null</code> if the model is clear (represents no selection
     * freeform).
     */
    public Point getEndPoint() {
        if (!isClear()) {
            return null;
        }
        return getPoint(getNumPoints() - 1);
    }

    /**
     * Get the starting corner point of the selection freeform described by this
     * model
     *
     * @return A 3D vector representing the start of the selection freeform, or
     * <code>null</code> if the model is clear (represents no selection
     * freeform).
     */
    public final Point getPoint(final int i) {
        if (i < 0 || i >= getNumPoints()) {
            return getStartPoint();
        }
        return points.get(i);
    }

    /**
     * Get the end corner point of the selection freeform (diagonally opposite
     * to the start corner) described by this model
     *
     * @return A 3D vector representing the end of the selection freeform, or
     * <code>null</code> if the model is clear (represents no selection
     * freeform).
     */
    public void resetModel() {
        leftMost = BIG_NUMBER;
        rightMost = BIG_NEGATIVE_NUMBER;
        bottomMost = BIG_NEGATIVE_NUMBER;
        topMost = BIG_NUMBER;
        this.points = new ArrayList<>();
    }

    /**
     * Get the left,top,right or bottom most values
     *
     * @return An int for the left most point
     */
    public int getLeftMost() {
        return leftMost;
    }

    public int getRightMost() {
        return rightMost;
    }

    public int getTopMost() {
        return topMost;
    }

    public int getBottomMost() {
        return bottomMost;
    }

    public Float[] getTransformedVertices() {
        final int width = rightMost - leftMost;
        final int height = bottomMost - topMost;
        final float newWidth = rightBoxCameraCoordinates - leftBoxCameraCoordinates;
        final float newHeight = bottomBoxCameraCoordinates - topBoxCameraCoordinates;

        List<Float> vertices = new ArrayList<>();

        for (int i = 0; i < MAX_POINTS; i++) {
            final float x = (getPoint(i).x - leftMost) / ((float) width) * newWidth + leftBoxCameraCoordinates;
            final float y = (getPoint(i).y - topMost) / ((float) height) * newHeight + topBoxCameraCoordinates;

            vertices.add(x);
            vertices.add(y);
        }

        return vertices.toArray(new Float[0]);
    }

    public void setWindowBoxToCameraBox(final float[] boxCameraCoordinates) {
        leftBoxCameraCoordinates = boxCameraCoordinates[0];
        rightBoxCameraCoordinates = boxCameraCoordinates[1];
        topBoxCameraCoordinates = boxCameraCoordinates[2];
        bottomBoxCameraCoordinates = boxCameraCoordinates[3];
    }
}
