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
package au.gov.tac.constellation.views.mapview2.utillities;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author altair1673
 */
public class IntersectionNode {

    private final double x;
    private final double y;

    private final String key;

    private static final Logger LOGGER = Logger.getLogger("IntersectionNode");

    private final List<Vec3> containedPoints = new ArrayList<Vec3>();
    private final List<Integer> relevantMarkers = new ArrayList<Integer>();
    private final List<IntersectionNode> connectedPoints = new ArrayList<IntersectionNode>();
    private final List<String> connectedPointIds = new ArrayList<String>();

    public IntersectionNode() {
        this.x = 0;
        this.y = 0;
        key = x + "," + y;
    }

    public IntersectionNode(double x, double y) {
        this.x = x;
        this.y = y;
        key = x + "," + y;
    }

    public List<Integer> getRelevantMarkers() {
        return relevantMarkers;
    }

    public List<IntersectionNode> getConnectedPoints() {
        return connectedPoints;
    }


    public void addRelevantMarker(Integer id) {
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
    public void addConnectedPoint(IntersectionNode otherNode) {

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

    public void addContainedPoint(double x, double y) {
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
}
