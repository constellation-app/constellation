/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.markers;

import au.gov.asd.tac.constellation.views.mapview2.MapView;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

/**
 * Parent of all markers
 *
 * @author altair1673
 */
public abstract class AbstractMarker {

    // The svg path for the actual marker
    protected final SVGPath markerPath;
    protected int markerID = 0;

    // Id list contains ids of all nodes this marker represents
    protected List<Integer> idList = new ArrayList<>();
    protected boolean isSelected = false;

    private double x = 0;
    private double y = 0;
    protected double xOffset;
    protected double yOffset;

    protected MapView parent;

    public enum MarkerType {
        POINT_MARKER,
        LINE_MARKER,
        POLYGON_MARKER,
        CLUSTER_MARKER,
        SELECTED,
        NO_MARKER
    }

    protected MarkerType type;

    protected AbstractMarker(final MapView parent, final int markerID, final int nodeId, final double xOffset, final double yOffset, final MarkerType type) {
        this.markerID = markerID;
        this.parent = parent;
        idList.add(nodeId);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.type = type;

        markerPath = new SVGPath();
    }

    /**
     * Gets the amount of nodes the marker represents
     *
     * @return
     */
    public int getWeight() {
        return idList.size();
    }

    public MarkerType getType() {
        return type;
    }

    /**
     * Adds the id of a graph node to the idList array which means that this
     * marker represents that node in the map view
     *
     * @param id
     */
    public void addNodeID(final int id) {
        idList.add(id);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public List<Integer> getConnectedNodeIdList() {
        return idList;
    }

    public void setMarkerPosition(final double mapWidth, final double mapHeight) {
        // Do nothing here as this gets overidden in child classes
    }

    public Shape getMarker() {
        return markerPath;
    }

    public int getMarkerId() {
        return markerID;
    }

    protected void setX(final double x) {
        this.x = x;
    }

    protected void setY(final double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void select() {

    }

    public void deselect() {

    }

    public void changeMarkerColour(final String option) {

    }
}
