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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the intersection between edges of the thiessen polygons
 *
 * @author altair1673
 */
public class IntersectionNode {

    private final double x;
    private final double y;

    private final String key;


    private List<Vec3> containedPoints = new ArrayList<>();
    private List<Integer> relevantMarkers = new ArrayList<>();
    private List<IntersectionNode> connectedPoints = new ArrayList<>();
    private List<String> connectedPointIds = new ArrayList<>();

    public IntersectionNode() {
        this.x = 0;
        this.y = 0;
        key = x + "," + y;
    }

    public IntersectionNode(final double x, final double y) {
        this.x = x;
        this.y = y;
        key = x + "," + y;
    }

    public IntersectionNode(final IntersectionNode other) {
        x = other.getX();
        y = other.getY();
        key = other.getKey();

        containedPoints = other.getContainedPoints();
        relevantMarkers = other.getRelevantMarkers();
        connectedPoints = other.getConnectedPoints();
        connectedPointIds = other.getConnectedPointIds();
    }

    public List<Integer> getRelevantMarkers() {
        return new ArrayList<>(relevantMarkers);
    }

    public List<IntersectionNode> getConnectedPoints() {
        return new ArrayList<>(connectedPoints);
    }


    public void addRelevantMarker(final Integer id) {
        if (!relevantMarkers.contains(id)) {
            relevantMarkers.add(id);
        }
    }

    /**
     * Adds connected point if it is not already added
     *
     * @param otherNode - The other intersecionNode that the current node is a
     * neighbour of
     */
    public void addConnectedPoint(final IntersectionNode otherNode) {

        if (otherNode == null) {
            return;
        }

        if (otherNode.getKey().equals(key) || connectedPointIds.contains(key)) {
            return;
        }

        // Check to see if node is already connected
        if (!connectedPointIds.contains(otherNode.getKey())) {
            connectedPointIds.add(otherNode.getKey());
            connectedPoints.add(otherNode);
            otherNode.addConnectedPoint(this);
        }
    }

    /**
     * Holds the intersection nodes in the same location and this one
     *
     * @param x - x coordinate of intersection nodes
     * @param y - y coordinate of intersection nodes
     */
    public void addContainedPoint(final double x, final double y) {
        containedPoints.add(new Vec3(x, y));
    }

    public List<Vec3> getContainedPoints() {
        return containedPoints;
    }

    public String getKey() {
        return key;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public List<String> getConnectedPointIds() {
        return connectedPointIds;
    }
}
