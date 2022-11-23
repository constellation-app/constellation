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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 *
 * @author altair1673
 */
public class ClusterMarkerBuilder {

    private List<ClusterMarker> clusterCircles = new ArrayList<>();
    private List<Text> clusterValues = new ArrayList<>();

    private ArrayList<ArrayList<Node>> pointMarkerClusters = new ArrayList<ArrayList<Node>>();
    private Set<Node> clusteredPointMarkers = new HashSet<>();
    private MapView parent = null;

    public ClusterMarkerBuilder() {

    }

    public ClusterMarkerBuilder(MapView parent) {
        this.parent = parent;
    }

    private void calculateClusters(Group pointMarkerGroup) {
        clusterCircles.clear();
        clusterValues.clear();
        clusteredPointMarkers.clear();
        pointMarkerClusters.clear();
        for (int i = 0; i < pointMarkerGroup.getChildren().size(); ++i) {

            if (!clusteredPointMarkers.contains(pointMarkerGroup.getChildren().get(i))) {
                clusteredPointMarkers.add(pointMarkerGroup.getChildren().get(i));
                ArrayList<Node> clusterArray = new ArrayList<>();
                clusterArray.add(pointMarkerGroup.getChildren().get(i));
                for (int j = 0; j < pointMarkerGroup.getChildren().size(); ++j) {
                    if (i != j && !clusteredPointMarkers.contains(pointMarkerGroup.getChildren().get(j))) {
                        double distance = getNodeDistance(pointMarkerGroup.getChildren().get(i), pointMarkerGroup.getChildren().get(j));

                        if (distance < 150) {
                            clusteredPointMarkers.add(pointMarkerGroup.getChildren().get(j));
                            clusterArray.add(pointMarkerGroup.getChildren().get(j));
                        }
                    }
                }
                pointMarkerClusters.add(clusterArray);
            }
        }
    }

    private double getNodeDistance(Node n1, Node n2) {
        double x1 = n1.localToScreen(n1.getBoundsInLocal().getCenterX(), n1.getBoundsInLocal().getCenterY()).getX();
        double y1 = n1.localToScreen(n1.getBoundsInLocal().getCenterX(), n1.getBoundsInLocal().getCenterY()).getY();

        double x2 = n2.localToScreen(n2.getBoundsInLocal().getCenterX(), n2.getBoundsInLocal().getCenterY()).getX();
        double y2 = n2.localToScreen(n2.getBoundsInLocal().getCenterX(), n2.getBoundsInLocal().getCenterY()).getY();

        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

        //LOGGER.log(Level.SEVERE, "Test X1: " + x1);
        //LOGGER.log(Level.SEVERE, "Test Distance: " + distance);
        return distance;
    }

    public void update(Group pointMarkerGroup) {
        calculateClusters(pointMarkerGroup);
        pointMarkerClusters.forEach(c -> {
            clusterCircles.add(new ClusterMarker(parent, c));
            clusterValues.add(clusterCircles.get(clusterCircles.size() - 1).getClusterValues());
        });

    }

    public List<ClusterMarker> getClusterMarkers() {
        return clusterCircles;
    }

    public List<Text> getClusterValues() {
        return clusterValues;
    }
}
