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
import au.gov.asd.tac.constellation.views.mapview2.MapViewTopComponent;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javafx.scene.Group;
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


    private Circle cluster;
    private Text numNodes;

    private ArrayList<Node> nodes;

    public ClusterMarker(MapView parent, ArrayList<Node> nodes) {
        super(parent, -99, -99, 0, 0, AbstractMarker.MarkerType.CLUSTER_MARKER);

        isSelected = false;
        this.nodes = nodes;

        createClusterMarkers();
    }

    private void createClusterMarkers() {
        if (nodes.isEmpty()) {
            return;
        }

        double MIN_RADIUS = 15;
        Vec3 clusterCenter = new Vec3();
        nodes.forEach(node -> {

            double nodeX = node.getBoundsInParent().getCenterX();
            double nodeY = node.getBoundsInParent().getCenterY();

            clusterCenter.addVector(new Vec3(nodeX, nodeY));
        });
        clusterCenter.divVector(nodes.size());

        double diameter = 0;
        double maxDistance = Double.MIN_VALUE;
        double farthestNode1X = 0;
        double farthestNode1Y = 0;

        double farthestNode2X = 0;
        double farthestNode2Y = 0;
        if (nodes.size() > 0) {
            final Vec3 minPosition = new Vec3(
                    Float.MAX_VALUE, Float.MAX_VALUE);
            final Vec3 maxPosition = new Vec3(
                    Float.MIN_VALUE, Float.MIN_VALUE);
            for (int i = 0; i < nodes.size(); ++i) {
                Node node = nodes.get(i);
                double nodeX = node.getBoundsInParent().getCenterX();
                double nodeY = node.getBoundsInParent().getCenterY();

                Vec3 position = new Vec3(nodeX, nodeY);
                for (int j = 0; j < nodes.size(); ++j) {
                    Node node2 = nodes.get(j);
                    if (node != node2) {
                        double node2X = node2.getBoundsInParent().getCenterX();
                        double node2Y = node2.getBoundsInParent().getCenterY();

                        double distance = Math.sqrt(Math.pow(node2X - nodeX, 2) + Math.pow(node2Y - nodeY, 2));

                        if (maxDistance < distance) {
                            maxDistance = distance;
                            farthestNode1X = nodeX;
                            farthestNode1Y = nodeY;
                            farthestNode2X = node2X;
                            farthestNode2Y = node2Y;
                            //clusterCenter.x = (nodeX + node2X) / 2;
                            //clusterCenter.y = (nodeY + node2Y) / 2;
                        }
                    }
                }

                clusterCenter.x = (farthestNode1X + farthestNode2X) / 2;
                clusterCenter.y = (farthestNode1Y + farthestNode2Y) / 2;

                if (clusterCenter.x == 0 && clusterCenter.y == 0) {
                    clusterCenter.x = node.getBoundsInParent().getCenterX();
                    clusterCenter.y = node.getBoundsInParent().getCenterY();
                }

                minPosition.x = Math.min(position.x, minPosition.x);
                minPosition.y = Math.min(position.y, minPosition.y);
                maxPosition.x = Math.max(position.x, maxPosition.x);
                maxPosition.y = Math.max(position.y, maxPosition.y);
            }
            diameter = Math.sqrt(Math.pow((maxPosition.x - minPosition.x), 2)
                    + Math.pow((maxPosition.y - minPosition.y), 2));

            /*diameter = Math.sqrt(Math.pow((farthestNode2X - farthestNode1X), 2)
                    + Math.pow((farthestNode2Y - farthestNode1Y), 2));*/
        }
        double clusterRadius = Math.max((float) diameter / 2, MIN_RADIUS);

        cluster = new Circle();
        cluster.setCenterX(clusterCenter.x);
        cluster.setCenterY(clusterCenter.y);
        cluster.setRadius(clusterRadius);
        cluster.setFill(Color.DARKBLUE);
        cluster.setOpacity(0.6);
        cluster.setMouseTransparent(true);

        numNodes = new Text(clusterCenter.x - 5, clusterCenter.y + 5, "" + nodes.size());
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
