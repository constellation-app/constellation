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
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author altair1673
 */
public class ClusterMarker extends AbstractMarker {

    // The marker to surround a cluster
    private Circle cluster;

    // Text to denote how many in each cluster
    private Text numNodes;

    // the nodes inside a cluster
    // These are not the marker objects themselves but the graphical marker elements on screen
    private final ArrayList<Node> nodes;

    public ClusterMarker(final MapView parent, final ArrayList<Node> nodes) {
        super(parent, -99, -99, 0, 0, AbstractMarker.MarkerType.CLUSTER_MARKER);

        isSelected = false;
        this.nodes = nodes;

        createClusterMarkers();
    }

    /**
     * Generates a circle based on how many markers are within a cluster
     */
    private void createClusterMarkers() {
        if (nodes.isEmpty()) {
            return;
        }

        double minRadius = 15;
        Vec3 clusterCenter = new Vec3();

        // Get the center of each marker on the map
        nodes.forEach(node -> {

            double nodeX = node.getBoundsInParent().getCenterX();
            double nodeY = node.getBoundsInParent().getCenterY();

            clusterCenter.addVector(new Vec3(nodeX, nodeY));
        });

        // Get average location of all markers
        clusterCenter.divVector(nodes.size());

        double diameter = 0;
        double maxDistance = Double.MIN_VALUE;

        // Coordinates of the node firthest away from each other
        double farthestNode1X = 0;
        double farthestNode1Y = 0;

        double farthestNode2X = 0;
        double farthestNode2Y = 0;

        if (nodes.size() > 0) {
            final Vec3 minPosition = new Vec3(
                    Float.MAX_VALUE, Float.MAX_VALUE);
            final Vec3 maxPosition = new Vec3(
                    Float.MIN_VALUE, Float.MIN_VALUE);

            // Loop through all nodes
            for (int i = 0; i < nodes.size(); ++i) {
                Node node = nodes.get(i);

                // Get its coordinates
                double nodeX = node.getBoundsInParent().getCenterX();
                double nodeY = node.getBoundsInParent().getCenterY();

                Vec3 position = new Vec3(nodeX, nodeY);

                // Loop through all nodes
                for (int j = 0; j < nodes.size(); ++j) {
                    Node node2 = nodes.get(j);

                    // If the 2 nodes are not the same
                    if (node != node2) {

                        // Get the distance between the 2 nodes
                        double node2X = node2.getBoundsInParent().getCenterX();
                        double node2Y = node2.getBoundsInParent().getCenterY();

                        double distance = Math.sqrt(Math.pow(node2X - nodeX, 2) + Math.pow(node2Y - nodeY, 2));

                        // Store the nodes that have the largest distance
                        if (maxDistance < distance) {
                            maxDistance = distance;
                            farthestNode1X = nodeX;
                            farthestNode1Y = nodeY;
                            farthestNode2X = node2X;
                            farthestNode2Y = node2Y;
                        }
                    }
                }

                // Find center or furthest nodes
                clusterCenter.setX((farthestNode1X + farthestNode2X) / 2);
                clusterCenter.setY((farthestNode1Y + farthestNode2Y) / 2);

                // If there is only 1 marker
                if (clusterCenter.getX() == 0 && clusterCenter.getY() == 0) {
                    clusterCenter.setX(node.getBoundsInParent().getCenterX());
                    clusterCenter.setY(node.getBoundsInParent().getCenterY());
                }

                // Store the min and max position
                minPosition.setX(Math.min(position.getX(), minPosition.getX()));
                minPosition.setY(Math.min(position.getY(), minPosition.getY()));
                maxPosition.setX(Math.max(position.getX(), maxPosition.getX()));
                maxPosition.setY(Math.max(position.getY(), maxPosition.getY()));
            }

            // Calclate diameter of the circle
            diameter = Math.sqrt(Math.pow((maxPosition.getX() - minPosition.getX()), 2)
                    + Math.pow((maxPosition.getY() - minPosition.getY()), 2));

        }
        double clusterRadius = Math.max((float) diameter / 2, minRadius);

        // Generate the cluster circle
        cluster = new Circle();
        cluster.setCenterX(clusterCenter.getX());
        cluster.setCenterY(clusterCenter.getY());
        cluster.setRadius(clusterRadius);
        cluster.setFill(Color.DARKBLUE);
        cluster.setOpacity(0.6);
        cluster.setMouseTransparent(true);

        numNodes = new Text(clusterCenter.getX() - 5, clusterCenter.getY() + 5, "" + nodes.size());
        numNodes.setFill(Color.YELLOW);
        numNodes.setFont(new Font(20));
        numNodes.setMouseTransparent(true);

    }

    @Override
    public Shape getMarker() {
        return cluster;
    }

    public Text getClusterValues() {
        return numNodes;
    }

}
