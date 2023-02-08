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
package au.gov.asd.tac.constellation.views.mapview2.layers;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.UserPointMarker;
import au.gov.tac.constellation.views.mapview2.utillities.IntersectionNode;
import au.gov.tac.constellation.views.mapview2.utillities.Vec3;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/**
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer extends AbstractMapLayer {

    private final Group layer;
    private final Group debugLayer;

    private int nodeID = 1;

    private final double nodeXOffset = 97;
    private final double nodeYOffset = 93;

    private static final Logger LOGGER = Logger.getLogger("ThiessenPolygons");

    // All markers on the map
    private List<AbstractMarker> markers = new ArrayList<>();

    // Map to hold the intersection points
    private final Map<String, IntersectionNode> intersectionMap = new HashMap<String, IntersectionNode>();

    // Map to hold line and all intersection points on the line
    private Map<Line, ArrayList<IntersectionNode>> lineMap = new HashMap<Line, ArrayList<IntersectionNode>>();

    // Map to hold the point markers
    private final Map<Integer, AbstractMarker> nodesOnScreen = new HashMap<Integer, AbstractMarker>();

    // Map to hold the bisector lines, key is the ids of the markers they split
    private Map<String, Line> bisectorLines = new HashMap<String, Line>();

    // Map to hold the final shortened bisector lines
    private Map<String, Line> finalBisectorLines = new HashMap<String, Line>();

    private Map<Integer, IntersectionNode> relevantIntersections = new HashMap<Integer, IntersectionNode>();

    // Array of intersections to hold the corners
    private List<IntersectionNode> corners = new ArrayList<IntersectionNode>();

    // Lines reperesting the edges of the map
    private final Line top;
    private final Line left;
    private final Line right;
    private final Line bottom;

    // IDs of the lines at the edge
    private final String topID = "-1,-2";
    private final String bottomID = "-3,-4";
    private final String leftID = "-5,-6";
    private final String rightID = "-7,-8";

    private final double lineExtend = 0.5;

    public ThiessenPolygonsLayer(MapView parent, int id, List<AbstractMarker> markers) {
        super(parent, id);
        layer = new Group();
        debugLayer = new Group();

        // Instantiate border lines
        top = new Line();
        top.setStartX(0);
        top.setStartY(0);
        top.setEndX(MapView.mapWidth);
        top.setEndY(0);
        top.setStroke(Color.RED);
        //top.setStrokeWidth(5);
        layer.getChildren().add(top);

        left = new Line();
        left.setStartX(0);
        left.setStartY(0);
        left.setEndX(0);
        left.setEndY(MapView.mapHeight);
        left.setStroke(Color.RED);
        //left.setStrokeWidth(5);
        layer.getChildren().add(left);

        right = new Line();
        right.setStartX(MapView.mapWidth);
        right.setStartY(0);
        right.setEndX(MapView.mapWidth);
        right.setEndY(MapView.mapHeight);
        right.setStroke(Color.RED);
        //right.setStrokeWidth(5);
        layer.getChildren().add(right);

        bottom = new Line();
        bottom.setStartX(0);
        bottom.setStartY(MapView.mapHeight);
        bottom.setEndX(MapView.mapWidth);
        bottom.setEndY(MapView.mapHeight);
        bottom.setStroke(Color.RED);
        //bottom.setStrokeWidth(5);
        layer.getChildren().add(bottom);

        this.markers = markers;

    }

    /**
     * Sort out the User and Point markers and store it in a map with an ID
     *
     * @param markers
     */
    private void sortNodes(List<AbstractMarker> markers) {
        nodesOnScreen.clear();

        for (AbstractMarker marker : markers) {
            if (marker instanceof PointMarker) {
                PointMarker p = (PointMarker) marker;

                nodesOnScreen.put(nodeID++, p);
            } else if (marker instanceof UserPointMarker) {
                UserPointMarker p = (UserPointMarker) marker;
                nodesOnScreen.put(nodeID++, p);
            }

        }

    }

    /**
     * Calculate the bisectors between all the markers
     */
    private void calculateBisectors() {
        bisectorLines.clear();

        // Set to store markers pairs whose bisectors haev already been calculated
        Set<String> calculatedPairs = new HashSet<>();

        // Loop through markers
        for (Integer id : nodesOnScreen.keySet()) {

            // Loop through markers for marker
            for (Integer id2 : nodesOnScreen.keySet()) {
                // If the markers don't have the same ID value
                if (id.intValue() != id2.intValue()) {
                    // Create the pair keys
                    String idPair = id + "," + id2;
                    String idPair2 = id2 + "," + id;

                    // If the bisector hasn't been calculated yet
                    if (!calculatedPairs.contains(idPair) && !calculatedPairs.contains(idPair2)) {
                        // Add the keys to the set
                        calculatedPairs.add(idPair);
                        calculatedPairs.add(idPair2);

                        // Get the 2 markers we are bisecting
                        AbstractMarker node1 = nodesOnScreen.get(id);

                        AbstractMarker node2 = nodesOnScreen.get(id2);

                        // Get coordinates from markers
                        Vec3 coords1 = new Vec3(node1.getX() - nodeXOffset, node1.getY() + nodeYOffset);
                        Vec3 coords2 = new Vec3(node2.getX() - nodeXOffset, node2.getY() + nodeYOffset);

                        // Calculate the midpoint bewteen the 2 points
                        Vec3 midPoint = new Vec3((coords1.x + coords2.x) / 2, (coords1.y + coords2.y) / 2);

                        // Calculating slope of line between 2 points
                        Vec3 slope = new Vec3((coords2.y - coords1.y), (coords2.x - coords1.x));

                        // Reciprocal of the slope
                        double reciprocal = -1 * (slope.y / slope.x);

                        LOGGER.log(Level.SEVERE, "Reciprocal: " + reciprocal);

                        // the b is the y intercept for the formula y = ax + b
                        double b = midPoint.y - (reciprocal * midPoint.x);

                        // Get the start and end of the bisectorline
                        Vec3 lineStart = new Vec3(0, b);
                        Vec3 lineEnd = new Vec3(-b / reciprocal, 0);

                        // Get length of the line
                        double distance = Vec3.getDistance(lineStart, lineEnd);

                        // Get normalized direction vector of the line
                        Vec3 directVect = new Vec3((lineEnd.x - lineStart.x) / distance, (lineEnd.y - lineStart.y) / distance);

                        // Extend line in both directions
                        lineStart.x = midPoint.x + (directVect.x * 1500);
                        lineStart.y = midPoint.y + (directVect.y * 1500);

                        lineEnd.x = midPoint.x - (directVect.x * 1500);
                        lineEnd.y = midPoint.y - (directVect.y * 1500);

                        // If the line is either horizontal or vertical then make the ends and start of the line th edges of the map
                        if (slope.x == 0 && slope.y != 0) {
                            lineStart.x = midPoint.x;
                            lineStart.y = 0;
                            lineEnd.x = midPoint.x;
                            lineEnd.y = MapView.mapHeight;
                        } else if (slope.y == 0 && slope.x != 0) {
                            lineStart.x = 0;
                            lineStart.y = midPoint.y;
                            lineEnd.x = MapView.mapWidth;
                            lineEnd.y = midPoint.y;
                        }

                        Line line = new Line();
                        line.setStartX(lineStart.x);
                        line.setStartY(lineStart.y);

                        line.setEndX(lineEnd.x);
                        line.setEndY(lineEnd.y);
                        
                        line.setStroke(Color.RED);

                        //debugLayer.getChildren().add(line);
                        // Add bisector lines to the map along with the ids of the markers it bisects as the key
                        bisectorLines.put(idPair, line);
                    }
                }
            }
        }
    }

    /**
     * Gets rid of irrelevant bisector line and shortens relevant ones
     */
    private void shortenBisectorLines() {
        // For each bisector key
        for (String key : bisectorLines.keySet()) {
            // get id os related markers
            Integer id1 = Integer.parseInt(key.split(",")[0]);
            Integer id2 = Integer.parseInt(key.split(",")[1]);

            // Get the actual line
            Line bisector = bisectorLines.get(key);

            // Get start and end of line
            Vec3 start = new Vec3(bisector.getStartX(), bisector.getStartY());
            Vec3 end = new Vec3(bisector.getEndX(), bisector.getEndY());

            // Get distance of line and then calculate normalized direction vector
            double distance = Vec3.getDistance(start, end);
            Vec3 dirVect = new Vec3((end.x - start.x) / distance, (end.y - start.y) / distance);

            // Array to hold the start and end points of the shortened line
            Vec3[] shortLine = {null, null};

            int index = 0;

            // Get the positions of the markers involved.
            Vec3 marker1 = new Vec3(nodesOnScreen.get(id1).getX() - nodeXOffset, nodesOnScreen.get(id1).getY() + nodeYOffset);
            Vec3 marker2 = new Vec3(nodesOnScreen.get(id2).getX() - nodeXOffset, nodesOnScreen.get(id2).getY() + nodeYOffset);

            // for the distance of the line starting from the "start"
            for (double i = 0; i < distance; i = i + lineExtend) {

                // If the starting coordinate is outside the map then move the start point in the direction of the end point
                if (start.x > MapView.mapWidth + 5 || start.x < -5 || start.y < -2 || start.y > MapView.mapHeight + 2) {
                    start.x += dirVect.x;
                    start.y += dirVect.y;
                    continue;
                }

                Integer shortestDistanceID = null;

                // For every marker on screen
                for (Integer id : nodesOnScreen.keySet()) {
                    // If the marker is not one of the 2 markers that the line bisects
                    if (id.intValue() != id1.intValue() && id.intValue() != id2.intValue()) {
                        // Get the position fo the marker
                        Vec3 markerPos = new Vec3(nodesOnScreen.get(id).getX() - nodeXOffset, nodesOnScreen.get(id).getY() + nodeYOffset);

                        // If the distance bettween this marker and the current strarting position is less than the distance
                        // from start to either of the markers involved
                        if (Vec3.getDistance(start, markerPos) < Vec3.getDistance(start, marker1) && Vec3.getDistance(start, markerPos) < Vec3.getDistance(start, marker2)) {
                            // Record the marker the current start pos is closest to
                            shortestDistanceID = id;
                            break;
                        }
                    }

                }

                // If the current start is not closes to anyother marker except the ones being bisected
                // Index represents if we are calculating the start or end of the line
                if (shortestDistanceID == null && index == 0) {
                    // Record the start position is shortLine array
                    shortLine[index] = new Vec3(start.x - (lineExtend * dirVect.x), start.y - (lineExtend * dirVect.y)); // shortLie[index] -= start * dis
                    ++index;
                } // If index is 1 it means we are at the end of the line so record the line end
                else if (shortestDistanceID != null && index == 1) {

                    shortLine[index] = new Vec3(start.x, start.y);

                    break;
                }

                // Move the start point along the direction vector
                start.x += lineExtend * dirVect.x;
                start.y += lineExtend * dirVect.y;


            }

            // If the end is not on the line then manually set it
            if (shortLine[1] == null && shortLine[0] != null) {
                shortLine[1] = end;
            }


            if (shortLine[0] != null && shortLine[1] != null) {

                // Create the new shortened line with the 2 calculated ends
                Line l = new Line();
                //Line l2 = new Line();
                l.setStartX(shortLine[0].x);
                l.setStartY(shortLine[0].y);

                l.setEndX(shortLine[1].x);
                l.setEndY(shortLine[1].y);

                /*l2.setStartX(shortLine[0].x);
                l2.setStartY(shortLine[0].y);

                l2.setEndX(shortLine[1].x);
                l2.setEndY(shortLine[1].y);*/

                l.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //LOGGER.log(Level.SEVERE, "Start x: " + l.getStartX() + " Start y: " + l.getStartY());
                        //LOGGER.log(Level.SEVERE, "End x: " + l.getEndX() + " End y: " + l.getEndY());
                    }

                });
                //l2.setStrokeWidth(0.5);
                //layer.getChildren().add(l2);

                // Put line in a new map with the key from this iteration of the main for loop
                finalBisectorLines.put(key, l);
                // Create a new entry in the lineMap map with the line as the key and an empty array of intersection nodes as the value
                lineMap.put(finalBisectorLines.get(key), new ArrayList<IntersectionNode>());

            }

        }

        // Addd in the edges of the map
        finalBisectorLines.put(topID, top);
        finalBisectorLines.put(bottomID, bottom);
        finalBisectorLines.put(leftID, left);
        finalBisectorLines.put(rightID, right);

        // Add in a lineMap input for each edge
        lineMap.put(finalBisectorLines.get(topID), new ArrayList<IntersectionNode>());
        lineMap.put(finalBisectorLines.get(bottomID), new ArrayList<IntersectionNode>());
        lineMap.put(finalBisectorLines.get(leftID), new ArrayList<IntersectionNode>());
        lineMap.put(finalBisectorLines.get(rightID), new ArrayList<IntersectionNode>());

    }

    /**
     * Calculates the intersection points between the bisectors
     */
    private void calculateIntersectionCircles() {

        // For every bisector
        for (String bisectID1 : finalBisectorLines.keySet()) {
            // Get the bisector line
            Line bisect1 = finalBisectorLines.get(bisectID1);
            String intersectionPoint;

            // Loop through all other bisectors for each bisector
            for (String bisectID2 : finalBisectorLines.keySet()) {

                // Get second bisector line
                Line bisect2 = finalBisectorLines.get(bisectID2);

                // If the two lines are the left and right edges or the top and bottom edges then continue
                if ((bisectID1.equals(topID) && bisectID2.equals(bottomID)) || (bisectID1.equals(bottomID) && bisectID2.equals(topID))) {
                    continue;
                }
                if ((bisectID1.equals(leftID) && bisectID2.equals(rightID)) || (bisectID1.equals(rightID) && bisectID2.equals(leftID))) {
                    continue;
                }

                // If the lines biset1 and bisect2 are not the same and the lines intersect
                if (!bisectID1.equals(bisectID2) && doesIntersect(bisect1, bisect2)) {
                    // Calculate the slopes of the two lines
                    Vec3 slope = new Vec3((bisect1.getEndY() - bisect1.getStartY()), (bisect1.getEndX() - bisect1.getStartX()));
                    Vec3 slope2 = new Vec3((bisect2.getEndY() - bisect2.getStartY()), (bisect2.getEndX() - bisect2.getStartX()));

                    // For formula y = mx + b, represent both bisectors with this formula
                    // calculate the different variables for the formula
                    double m1 = slope.x / slope.y;

                    double b1 = bisect1.getStartY() - (m1 * bisect1.getStartX());

                    double m2 = slope2.x / slope2.y;

                    double b2 = bisect2.getStartY() - (m2 * bisect2.getStartX());

                    // If lines are completely vertical or horizontal then they don't intersect
                    if (slope.y == 0 && slope2.y == 0) {
                        continue;
                    }

                    double x;
                    double y;

                    // If either line is either vertical or horizontal then tweak the formula slightly to calculate intersection point
                    if (slope.y == 0) {
                        x = bisect1.getStartX();
                        y = m2 * x + b2;
                    } else if (slope2.y == 0) {
                        x = bisect2.getStartX();
                        y = m1 * x + b1;
                    } else {
                        x = (b2 - b1) / (m1 - m2);

                        y = m1 * x + b1;
                    }

                    // Round interseciton point to avoid floating point errors
                    final double roundedX = Math.round(x * 10000) / 10000;
                    final double roundedY = Math.round(y * 10000) / 10000;


                    intersectionPoint = roundedX + "," + roundedY;

                    if (intersectionPoint.equals("NaN,NaN")) {

                        continue;
                    }

                    if (!(bisect1.contains(x, y) && bisect2.contains(x, y))) {
                        continue;
                    }

                    // If intersection point occurs outside the mapn then continue
                    if (roundedX < 0 || roundedX > MapView.mapWidth || roundedY < 0 || roundedY > MapView.mapHeight) {
                        continue;
                    }

                    // If the intersection map doesn't contain the intersection point
                    if (!intersectionMap.containsKey(intersectionPoint)) {
                        // Create new entry in the intersection node map
                        IntersectionNode intersectionNode = new IntersectionNode(roundedX, roundedY);

                        // Add the current intersection coordinate to the intersectionNode contained points array
                        intersectionNode.addContainedPoint(x, y);

                        // If the bisector is not on the edge then add the id of the markers seperated by the bisector
                        // to the intersectionNode's relevant marker container
                        if (!isEdgeLine(bisectID1)) {
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID1.split(",")[0]));
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID1.split(",")[1]));
                        }

                        // Do the same with the second line
                        if (!isEdgeLine(bisectID2)) {
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID2.split(",")[0]));
                            intersectionNode.addRelevantMarker(Integer.parseInt(bisectID2.split(",")[1]));
                        }

                        // Put the intersectionNode in the intersectionMap with its coordinate as the key
                        intersectionMap.put(intersectionPoint, intersectionNode);
                    } // Same thing here except that the intersectionNode already exists in the intersectionMap
                    else {
                        IntersectionNode intersectionNode = intersectionMap.get(intersectionPoint);
                        intersectionNode.addContainedPoint(x, y);

                        if (!isEdgeLine(bisectID1)) {
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID1.split(",")[0]));
                            intersectionNode.addRelevantMarker(Integer.parseInt(bisectID1.split(",")[1]));
                        }

                        if (!isEdgeLine(bisectID2)) {
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID2.split(",")[0]));
                            intersectionNode.addRelevantMarker(Integer.parseInt(bisectID2.split(",")[1]));
                        }

                    }

                    // Get the relevant marker IDs for this intersection
                    Integer[] relevantMarkerIDS = {Integer.parseInt(bisectID1.split(",")[0]), Integer.parseInt(bisectID1.split(",")[1]), Integer.parseInt(bisectID2.split(",")[0]), Integer.parseInt(bisectID2.split(",")[1])};

                    // Loop through the relevant markers and calculate which intersection is closest to which marker
                    for (Integer id : relevantMarkerIDS) {
                        // If the marker exists on screen
                        if (nodesOnScreen.containsKey(id)) {
                            // relevant intersections map contains the marker id then replace its value with the current intersectionNode
                            if (relevantIntersections.containsKey(id)) {
                                relevantIntersections.replace(id, intersectionMap.get(intersectionPoint));

                            } // else make a new entry in the map with the marker id being the key and the intersectionNode being the value
                            else {
                                relevantIntersections.put(id, intersectionMap.get(intersectionPoint));
                            }
                        }
                    }

                    // If the intersection point is at a corner
                    if ((roundedX == 0 && roundedY == 0) || (roundedX == Math.round(MapView.mapWidth) && roundedY == 0) || (roundedX == 0 && roundedY == MapView.mapHeight) || (roundedX == Math.round(MapView.mapWidth) && roundedY == MapView.mapHeight)) {
                        // Then add the node to the corners array created earlier
                        if (!corners.contains(intersectionMap.get(intersectionPoint))) {

                            corners.add(intersectionMap.get(intersectionPoint));
                        }
                    }

                    // If the lineMap entry for bisect2 doesn't have this intersectionNode in its value array then add it
                    if (!lineMap.get(bisect2).contains(intersectionMap.get(intersectionPoint))) {
                        lineMap.get(bisect2).add(intersectionMap.get(intersectionPoint));

                    }

                    // For every intersectionNode in the array in lineMap for key bisect2, add the current intersectionPoint to
                    // their connected points array
                    for (int i = 0; i < lineMap.get(bisect2).size(); ++i) {
                        lineMap.get(bisect2).get(i).addConnectedPoint(intersectionMap.get(intersectionPoint));
                    }

                    }


            }

        }


    }


    private boolean doesIntersect(Line l1, Line l2) {

        LOGGER.log(Level.SEVERE, "Line 1 rise: " + (l1.getEndY() - l1.getStartY()) + " Line 2 rise: " + (l2.getEndY() - l2.getStartY()));

        boolean intersects = l1.intersects(l2.getBoundsInLocal()) && l2.intersects(l1.getBoundsInLocal());


        return intersects;
    }

    /**
     * For polygons that will contain segments of map edges as well as a corner
     * some separate logic needs to be run since corners are intersection of map
     * edges and not bisectors
     */
    private void connectMarkersToCorners() {
        // For every corner intersection
        for (IntersectionNode n : corners) {
            boolean neighboursFound = false;

            // get its location
            double x = n.getX();
            double y = n.getY();

            IntersectionNode nearest1 = null;
            IntersectionNode nearest2 = null;

            double distanceNearest1 = Double.MAX_VALUE;
            double distanceNearest2 = Double.MAX_VALUE;

            // Calculate the nearest intersection points to the corner in both directions that go out from the corner
            for (IntersectionNode neighbour : n.getConnectedPoints()) {
                if (neighbour.getKey().equals(n.getKey())) {
                    continue;
                }

                // Calculate distance between corner and next closet neighbour
                double distance = Vec3.getDistance(new Vec3(x, y), new Vec3(neighbour.getX(), neighbour.getY()));

                // Nearest neighbour for horizontal neihgbour
                if (neighbour.getX() == x) {
                    if (distance < distanceNearest1) {
                        distanceNearest1 = distance;
                        nearest1 = neighbour;
                    }
                } // nearest neighbour for vertical neighbours
                else if (neighbour.getY() == y) {
                    if (distance < distanceNearest2) {
                        distanceNearest2 = distance;
                        nearest2 = neighbour;
                    }
                }
            }

            // If the nearest neighbours both have relevant markers they belong to then valid neighbours have been found
            if (!nearest1.getRelevantMarkers().isEmpty() && !nearest2.getRelevantMarkers().isEmpty()) {
                neighboursFound = true;
            }

            // If valid neighbours haven't been found meaning the neighbour of the corner was another corner
            IntersectionNode grandParent1 = n;
            IntersectionNode grandParent2 = n;
            IntersectionNode parent1 = nearest1;
            IntersectionNode parent2 = nearest2;

            while (!neighboursFound) {
                distanceNearest1 = Double.MAX_VALUE;
                distanceNearest2 = Double.MAX_VALUE;

                // If the first neighbour is empty
                if (nearest1.getRelevantMarkers().isEmpty()) {
                    // Loop through all its connected points
                    for (IntersectionNode neighbour : parent1.getConnectedPoints()) {
                        if (neighbour.getKey().equals(parent1.getKey())) {
                            continue;
                        }

                        double distance = Vec3.getDistance(new Vec3(neighbour.getX(), neighbour.getY()), new Vec3(parent1.getX(), parent1.getY()));

                        // Find nearest distance of neighbour that is not in the direction of parent
                        if ((neighbour.getX() != grandParent1.getX() || neighbour.getY() != grandParent1.getY()) && distance < distanceNearest1) {
                            distanceNearest1 = distance;
                            nearest1 = neighbour;
                        }
                    }

                    // If the nearest intersectionNode is STILL empty therefore it is another corner then set up the loop
                    // to run again but with this newly found corner as the parent
                    if (nearest1.getRelevantMarkers().isEmpty()) {
                        grandParent1 = parent1;
                        parent1 = nearest1;
                    }
                }

                // Same logic as above just with the other neighbour if it is a corner
                if (nearest2.getRelevantMarkers().isEmpty()) {
                    for (IntersectionNode neighbour : parent2.getConnectedPoints()) {

                        if (neighbour.getKey().equals(parent2.getKey())) {
                            continue;
                        }

                        double distance = Vec3.getDistance(new Vec3(neighbour.getX(), neighbour.getY()), new Vec3(parent2.getX(), parent2.getY()));

                        if ((neighbour.getX() != grandParent2.getX() || neighbour.getY() != grandParent2.getY()) && distance < distanceNearest2) {
                            distanceNearest2 = distance;
                            nearest2 = neighbour;
                        }
                    }

                    if (nearest2.getRelevantMarkers().isEmpty()) {
                        grandParent2 = parent2;
                        parent2 = nearest2;
                    }
                }

                // Check to see if valid neighbours have been found
                if (!nearest1.getRelevantMarkers().isEmpty() && !nearest2.getRelevantMarkers().isEmpty()) {
                    neighboursFound = true;
                }

            }

            List<Integer> relevantMarkerIds = new ArrayList<Integer>();

            // Loop through the relevant markers for one of the neighbours
            for (Integer id : nearest1.getRelevantMarkers()) {
                // If the other neighbour also has this as a relevant marker
                // Then add it to the relevantMarkerIds above
                if (nearest2.getRelevantMarkers().contains(id)) {
                    if (!n.getRelevantMarkers().contains(id)) {
                        //n.getRelevantMarkers().add(id);
                        relevantMarkerIds.add(id);
                    }
                }
            }

            // Out of the relevant markers the one related to the corner will be the closest to the corner
            if (relevantMarkerIds.size() > 1) {
                double distance = Double.MAX_VALUE;
                int id = relevantMarkerIds.get(0);

                // calculate closest marker to conrer
                for (Integer markerID : relevantMarkerIds) {
                    double markerX = nodesOnScreen.get(markerID).getX();
                    double markerY = nodesOnScreen.get(markerID).getY();

                    if (Vec3.getDistance(new Vec3(markerX, markerY), new Vec3(n.getX(), n.getY())) < distance) {
                        distance = Vec3.getDistance(new Vec3(markerX, markerY), new Vec3(n.getX(), n.getY()));
                        id = markerID;
                    }
                }

                // Finally we can add the relevant marker to the corner
                n.getRelevantMarkers().add(id);
            } // If there is only one marker in the relevantMarkerIds list then just add the first one to the corner
            else if (!relevantMarkerIds.isEmpty()) {
                n.getRelevantMarkers().add(relevantMarkerIds.get(0));
            }


        }
    }

    /**
     * Create the actual shape of the polygon with all the information
     * calculated above
     */
    private void createPolygons() {
        // A visited set to make sure we dont't visit the same vertice twice for 1 shape
        Set<String> visited = new HashSet<>();

        final ConstellationColor[] palette = ConstellationColor.createPalette(nodeID);

        // For every marker on screen
        for (Integer markerID : nodesOnScreen.keySet()) {
            boolean completedShape = false;
            visited.clear();

            // Create the shape graphic
            Polygon markerZone = new Polygon();

            markerZone.setStroke(palette[markerID].getJavaFXColor());
            markerZone.setFill(palette[markerID].getJavaFXColor());
            markerZone.setOpacity(0.5);

            markerZone.setMouseTransparent(true);

            // Get closes intersection point to the marker
            IntersectionNode relevantIntersection = relevantIntersections.get(markerID);

            while (!completedShape) {
                // Add the intersection node to the visited set
                visited.add(relevantIntersection.getKey());

                // Add the starting intersectionNode as the starting point of the shape
                markerZone.getPoints().addAll(new Double[]{relevantIntersection.getX(), relevantIntersection.getY()});

                // for all the intersectionNode's connected neighbours
                for (int i = 0; i < relevantIntersection.getConnectedPoints().size(); ++i) {

                    IntersectionNode neighbor = relevantIntersection.getConnectedPoints().get(i);

                    // If the neighbour has not ben visited and is relevant to the current marker in the itereaction
                    if (!visited.contains(neighbor.getKey()) && neighbor.getRelevantMarkers().contains(markerID)) {
                        // This new marker will be visited in the next iteration
                        relevantIntersection = neighbor;
                        break;
                    }
                    // If no connected points were found that relate to the current marker it means the shape has been completed
                    if (i == relevantIntersection.getConnectedPoints().size() - 1) {
                        completedShape = true;
                    }
                }

            }

            // Add the marker to the graphics layer
            layer.getChildren().add(markerZone);

        }
    }

    /**
     *
     * @param lineID
     * @return boolean indicating if line is an edge line
     */
    private boolean isEdgeLine(String lineID) {
        return lineID.equals(topID) || lineID.equals(bottomID) || lineID.equals(leftID) || lineID.equals(rightID);
    }

    @Override
    public void setUp() {
        layer.getChildren().clear();

        // Extract point markers and user point markers from all markers
        sortNodes(markers);

        if (nodesOnScreen.size() > 1) {
            // calculate and shorten bisector lines
        calculateBisectors();
        shortenBisectorLines();

            // calculate intersections
        calculateIntersectionCircles();

            // Connect corners to relevant markers
            connectMarkersToCorners();

            // create the polygons
            createPolygons();

        } // If only 1 marker exists on screen no calculations are needed the shape will jus be a rectangle with one colour
        else if (nodesOnScreen.size() == 1) {
            final ConstellationColor[] palette = ConstellationColor.createPalette(1);

            Rectangle r = new Rectangle();
            r.setX(0);
            r.setY(0);
            r.setWidth(MapView.mapWidth);
            r.setHeight(MapView.mapHeight);
            r.setFill(palette[0].getJavaFXColor());
            r.setStroke(palette[0].getJavaFXColor());
            r.setOpacity(0.5);
            r.setMouseTransparent(true);
            layer.getChildren().add(r);
        }


        LOGGER.log(Level.SEVERE, "Intersection Map Count: " + intersectionMap.size());

    }

    @Override
    public Group getLayer() {
        return layer;
    }
}
