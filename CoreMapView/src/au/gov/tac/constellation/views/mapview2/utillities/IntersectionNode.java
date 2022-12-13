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

/**
 *
 * @author altair1673
 */
public class IntersectionNode {

    private final double x;
    private final double y;

    private final String key;

    private final List<Integer> relevantMarkers = new ArrayList<Integer>();
    private final List<IntersectionNode> connectedPoints = new ArrayList<IntersectionNode>();

    public IntersectionNode() {
        this.x = 0;
        this.y = 0;
        key = x + "-" + y;
    }

    public IntersectionNode(double x, double y) {
        this.x = x;
        this.y = y;
        key = x + "-" + y;
    }

    public List<Integer> getRelevantMarkers() {
        return relevantMarkers;
    }

    public List<IntersectionNode> getConnectedPoints() {
        return connectedPoints;
    }

    public void addConncectedPoint(IntersectionNode otherNode) {
        connectedPoints.add(otherNode);
        otherNode.getConnectedPoints().add(this);
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
