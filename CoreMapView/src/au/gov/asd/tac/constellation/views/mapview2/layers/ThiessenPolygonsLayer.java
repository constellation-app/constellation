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
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.utilities.IntersectionNode;
import au.gov.asd.tac.constellation.views.mapview2.utilities.Vec3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Draws a zone around markers containing the point closest to them
 *
 * @author altair1673
 */
public class ThiessenPolygonsLayer extends AbstractMapLayer {

    private final Group layer;

    private int nodeID = 1;

    private static final double NODE_X_OFFSET = 97;
    private static final double NODE_Y_OFFSET = 93;


    // All markers on the map
    private final List<AbstractMarker> markers;

    // Map to hold the intersection points
    private final Map<String, IntersectionNode> intersectionMap = new HashMap<>();

    // Map to hold line and all intersection points on the line
    private final Map<Line, List<IntersectionNode>> lineMap = new HashMap<>();

    // Map to hold the point markers
    private final Map<Integer, AbstractMarker> nodesOnScreen = new HashMap<>();

    // Map to hold the bisector lines, key is the ids of the markers they split
    private final Map<String, Line> bisectorLines = new HashMap<>();

    // Map to hold the final shortened bisector lines
    private final Map<String, Line> finalBisectorLines = new HashMap<>();

    private final Map<Integer, IntersectionNode> relevantIntersections = new HashMap<>();

    // Array of intersections to hold the corners
    private final List<IntersectionNode> corners = new ArrayList<>();

    // Lines reperesting the edges of the map
    private final Line top;
    private final Line left;
    private final Line right;
    private final Line bottom;

    // IDs of the lines at the edge
    private static final String TOP_ID = "-1,-2";
    private static final String BOTTOM_ID = "-3,-4";
    private static final String LEFT_ID = "-5,-6";
    private static final String RIGHT_ID = "-7,-8";

    private static final double LINE_EXTEND = 0.5;

    public ThiessenPolygonsLayer(final MapView parent, final int id, final List<AbstractMarker> markers) {
        super(parent, id);
        layer = new Group();

        // Instantiate border lines
        top = new Line();
        top.setStartX(0);
        top.setStartY(0);
        top.setEndX(MapView.MAP_WIDTH);
        top.setEndY(0);
        top.setStroke(Color.RED);

        layer.getChildren().add(top);

        left = new Line();
        left.setStartX(0);
        left.setStartY(0);
        left.setEndX(0);
        left.setEndY(MapView.MAP_HEIGHT);
        left.setStroke(Color.RED);

        layer.getChildren().add(left);

        right = new Line();
        right.setStartX(MapView.MAP_WIDTH);
        right.setStartY(0);
        right.setEndX(MapView.MAP_WIDTH);
        right.setEndY(MapView.MAP_HEIGHT);
        right.setStroke(Color.RED);

        layer.getChildren().add(right);

        bottom = new Line();
        bottom.setStartX(0);
        bottom.setStartY(MapView.MAP_HEIGHT);
        bottom.setEndX(MapView.MAP_WIDTH);
        bottom.setEndY(MapView.MAP_HEIGHT);
        bottom.setStroke(Color.RED);

        layer.getChildren().add(bottom);

        this.markers = markers;

    }

    /**
     * Sort out the User and Point markers and store it in a map with an ID
     *
     * @param markers
     */
    private void sortNodes(final List<AbstractMarker> markers) {
        nodesOnScreen.clear();

        for (final AbstractMarker marker : markers) {
            nodesOnScreen.put(nodeID++, marker);
        }

    }

    /**
     * Calculate the bisectors between all the markers
     */
    private void calculateBisectors() {
        bisectorLines.clear();

        final Object[] keyArray = nodesOnScreen.keySet().toArray();

        // Loop through markers
        for (int i = 0; i < keyArray.length; i++) {
            final Integer id = (Integer) keyArray[i];
            // Loop through markers for marker
            for (int j = i + 1; j < keyArray.length; j++) {

                final Integer id2 = (Integer) keyArray[j];

                // Create the pair keys
                final String idPair = id + SeparatorConstants.COMMA + id2;

                // Get the 2 markers we are bisecting
                final AbstractMarker node1 = nodesOnScreen.get(id);

                final AbstractMarker node2 = nodesOnScreen.get(id2);

                // Get coordinates from markers
                final Vec3 coords1 = new Vec3(node1.getX() - NODE_X_OFFSET, node1.getY() + NODE_Y_OFFSET);
                final Vec3 coords2 = new Vec3(node2.getX() - NODE_X_OFFSET, node2.getY() + NODE_Y_OFFSET);

                // Calculate the midpoint bewteen the 2 points
                final Vec3 midPoint = new Vec3((coords1.getX() + coords2.getX()) / 2, (coords1.getY() + coords2.getY()) / 2);

                // Calculating slope of line between 2 points
                final Vec3 slope = new Vec3((coords2.getY() - coords1.getY()), (coords2.getX() - coords1.getX()));

                // Reciprocal of the slope
                final double reciprocal = -1 * (slope.getY() / slope.getX());


                // the b is the y intercept for the formula y = ax + b
                final double b = midPoint.getY() - (reciprocal * midPoint.getX());

                // Get the start and end of the bisectorline
                final Vec3 lineStart = new Vec3(0, b);
                final Vec3 lineEnd = new Vec3(-b / reciprocal, 0);

                // Get length of the line
                final double distance = Vec3.getDistance(lineStart, lineEnd);

                // Get normalized direction vector of the line
                final Vec3 directVect = new Vec3((lineEnd.getX() - lineStart.getX()) / distance, (lineEnd.getY() - lineStart.getY()) / distance);

                // Extend line in both directions
                lineStart.setX(midPoint.getX() + (directVect.getX() * 1500));
                lineStart.setY(midPoint.getY() + (directVect.getY() * 1500));

                lineEnd.setX(midPoint.getX() - (directVect.getX() * 1500));
                lineEnd.setY(midPoint.getY() - (directVect.getY() * 1500));

                // If the line is either horizontal or vertical then make the ends and start of the line th edges of the map
                if (slope.getX() == 0 && slope.getY() != 0) {
                    lineStart.setX(midPoint.getX());
                    lineStart.setY(0);
                    lineEnd.setX(midPoint.getX());
                    lineEnd.setY(MapView.MAP_HEIGHT);
                } else if (slope.getY() == 0 && slope.getX() != 0) {
                    lineStart.setX(0);
                    lineStart.setY(midPoint.getY());
                    lineEnd.setX(MapView.MAP_WIDTH);
                    lineEnd.setY(midPoint.getY());
                }

                final Line line = new Line();
                line.setStartX(lineStart.getX());
                line.setStartY(lineStart.getY());

                line.setEndX(lineEnd.getX());
                line.setEndY(lineEnd.getY());

                line.setStroke(Color.RED);

                // Add bisector lines to the map along with the ids of the markers it bisects as the key
                bisectorLines.put(idPair, line);
            }
        }
    }

    /**
     * Gets rid of irrelevant bisector line and shortens relevant ones
     */
    private void shortenBisectorLines() {
        // For each bisector key
        for (final String key : bisectorLines.keySet()) {
            // get id os related markers
            final Integer id1 = Integer.parseInt(key.split(SeparatorConstants.COMMA)[0]);
            final Integer id2 = Integer.parseInt(key.split(SeparatorConstants.COMMA)[1]);

            // Get the actual line
            final Line bisector = bisectorLines.get(key);

            // Get start and end of line
            final Vec3 start = new Vec3(bisector.getStartX(), bisector.getStartY());
            final Vec3 end = new Vec3(bisector.getEndX(), bisector.getEndY());

            // Get distance of line and then calculate normalized direction vector
            final double distance = Vec3.getDistance(start, end);
            final Vec3 dirVect = new Vec3((end.getX() - start.getX()) / distance, (end.getY() - start.getY()) / distance);

            // Array to hold the start and end points of the shortened line
            final Vec3[] shortLine = {null, null};

            int index = 0;

            // Get the positions of the markers involved.
            final Vec3 marker1 = new Vec3(nodesOnScreen.get(id1).getX() - NODE_X_OFFSET, nodesOnScreen.get(id1).getY() + NODE_Y_OFFSET);
            final Vec3 marker2 = new Vec3(nodesOnScreen.get(id2).getX() - NODE_X_OFFSET, nodesOnScreen.get(id2).getY() + NODE_Y_OFFSET);

            // for the distance of the line starting from the "start"
            for (double i = 0; i < distance; i += LINE_EXTEND) {

                // If the starting coordinate is outside the map then move the start point in the direction of the end point
                if (start.getX() > MapView.MAP_WIDTH + 5 || start.getX() < -5 || start.getY() < -2 || start.getY() > MapView.MAP_HEIGHT + 2) {
                    start.setX(start.getX() + dirVect.getX());
                    start.setY(start.getY() + dirVect.getY());
                    continue;
                }

                Integer shortestDistanceID = null;

                // For every marker on screen
                for (final Integer id : nodesOnScreen.keySet()) {
                    // If the marker is not one of the 2 markers that the line bisects
                    if (id.intValue() != id1.intValue() && id.intValue() != id2.intValue()) {
                        // Get the position of the marker
                        final Vec3 markerPos = new Vec3(nodesOnScreen.get(id).getX() - NODE_X_OFFSET, nodesOnScreen.get(id).getY() + NODE_Y_OFFSET);

                        // If the distance bettween this marker and the current strarting position is less than the distance
                        // from start to either of the markers involved
                        if (Vec3.getDistance(start, markerPos) < Vec3.getDistance(start, marker1) && Vec3.getDistance(start, markerPos) < Vec3.getDistance(start, marker2)) {
                            // Record the marker the current start pos is closest to
                            shortestDistanceID = id;
                            break;
                        }
                    }

                }

                // If the current start is not closest to anyother marker except the ones being bisected
                // Index represents if we are calculating the start or end of the line
                if (shortestDistanceID == null && index == 0) {
                    // Record the start position is shortLine array
                    shortLine[index] = new Vec3(start.getX() - (LINE_EXTEND * dirVect.getX()), start.getY() - (LINE_EXTEND * dirVect.getY()));
                    index++;
                    // If index is 1 it means we are at the end of the line so record the line end
                } else if (shortestDistanceID != null && index == 1) {
                    shortLine[index] = new Vec3(start.getX(), start.getY());
                    break;
                }

                // Move the start point along the direction vector
                start.setX(start.getX() + LINE_EXTEND * dirVect.getX());
                start.setY(start.getY() + LINE_EXTEND * dirVect.getY());
            }


            if (shortLine[0] != null) {
                // If the end is not on the line then manually set it
                if (shortLine[1] == null) {
                    shortLine[1] = end;
                }

                // Create the new shortened line with the 2 calculated ends
                final Line l = new Line();

                l.setStartX(shortLine[0].getX());
                l.setStartY(shortLine[0].getY());

                l.setEndX(shortLine[1].getX());
                l.setEndY(shortLine[1].getY());

                // Put line in a new map with the key from this iteration of the main for loop
                finalBisectorLines.put(key, l);
                // Create a new entry in the lineMap map with the line as the key and an empty array of intersection nodes as the value
                lineMap.put(finalBisectorLines.get(key), new ArrayList<IntersectionNode>());

            }

        }

        // Addd in the edges of the map
        finalBisectorLines.put(TOP_ID, top);
        finalBisectorLines.put(BOTTOM_ID, bottom);
        finalBisectorLines.put(LEFT_ID, left);
        finalBisectorLines.put(RIGHT_ID, right);

        // Add in a lineMap input for each edge
        lineMap.put(finalBisectorLines.get(TOP_ID), new ArrayList<>());
        lineMap.put(finalBisectorLines.get(BOTTOM_ID), new ArrayList<>());
        lineMap.put(finalBisectorLines.get(LEFT_ID), new ArrayList<>());
        lineMap.put(finalBisectorLines.get(RIGHT_ID), new ArrayList<>());

    }

    /**
     * Calculates the intersection points between the bisectors
     */
    private void calculateIntersectionCircles() {

        // For every bisector
        for (int i = 0; i < finalBisectorLines.keySet().toArray().length; i++) {
            final String bisectID1 = (String) finalBisectorLines.keySet().toArray()[i];
            // Get the bisector line
            final Line bisect1 = finalBisectorLines.get(bisectID1);
            String intersectionPoint;

            // Loop through all other bisectors for each bisector
            for (int j = 0; j < finalBisectorLines.keySet().toArray().length; j++) {
                final String bisectID2 = (String) finalBisectorLines.keySet().toArray()[j];

                // Get second bisector line
                final Line bisect2 = finalBisectorLines.get(bisectID2);

                final boolean topAndBottomEdges = (bisectID1.equals(TOP_ID) && bisectID2.equals(BOTTOM_ID)) || (bisectID1.equals(BOTTOM_ID) && bisectID2.equals(TOP_ID));
                final boolean leftAndRightEdges = (bisectID1.equals(LEFT_ID) && bisectID2.equals(RIGHT_ID)) || (bisectID1.equals(RIGHT_ID) && bisectID2.equals(LEFT_ID));

                // If the two lines are the left and right edges or the top and bottom edges then continue
                if (topAndBottomEdges && leftAndRightEdges) {
                    continue;
                }

                // If the lines biset1 and bisect2 are not the same and the lines intersect
                if (!bisectID1.equals(bisectID2) && doesIntersect(bisect1, bisect2)) {
                    // Calculate the slopes of the two lines
                    final Vec3 slope = new Vec3((bisect1.getEndY() - bisect1.getStartY()), (bisect1.getEndX() - bisect1.getStartX()));
                    final Vec3 slope2 = new Vec3((bisect2.getEndY() - bisect2.getStartY()), (bisect2.getEndX() - bisect2.getStartX()));

                    // For formula y = mx + b, represent both bisectors with this formula
                    // calculate the different variables for the formula
                    final double m1 = slope.getX() / slope.getY();

                    final double b1 = bisect1.getStartY() - (m1 * bisect1.getStartX());

                    final double m2 = slope2.getX() / slope2.getY();

                    final double b2 = bisect2.getStartY() - (m2 * bisect2.getStartX());

                    // If lines are completely vertical or horizontal then they don't intersect
                    if (slope.getY() == 0 && slope2.getY() == 0) {
                        continue;
                    }

                    final double x;
                    final double y;

                    // If either line is either vertical or horizontal then tweak the formula slightly to calculate intersection point
                    if (slope.getY() == 0) {
                        x = bisect1.getStartX();
                        y = m2 * x + b2;
                    } else if (slope2.getY() == 0) {
                        x = bisect2.getStartX();
                        y = m1 * x + b1;
                    } else {
                        x = (b2 - b1) / (m1 - m2);

                        y = m1 * x + b1;
                    }

                    // Round interseciton point to avoid floating point errors
                    // Change variables to double if things break in the future
                    final int roundedX = (int) (Math.round(x * 10000) / 10000);
                    final int roundedY = (int) (Math.round(y * 10000) / 10000);


                    intersectionPoint = roundedX + "," + roundedY;


                    if ("NaN,NaN".equals(intersectionPoint)
                            || !(bisect1.contains(x, y) && bisect2.contains(x, y))
                            || roundedX < 0 || roundedX > MapView.MAP_WIDTH
                            || roundedY < 0 || roundedY > MapView.MAP_HEIGHT) {

                        continue;
                    }

                    // If the intersection map doesn't contain the intersection point
                    if (!intersectionMap.containsKey(intersectionPoint)) {
                        // Create new entry in the intersection node map
                        final IntersectionNode intersectionNode = new IntersectionNode(roundedX, roundedY);

                        // Put the intersectionNode in the intersectionMap with its coordinate as the key
                        intersectionMap.put(intersectionPoint, intersectionNode);
                    }

                    final IntersectionNode intersectionNode = intersectionMap.get(intersectionPoint);
                    // Add the current intersection coordinate to the intersectionNode contained points array
                    intersectionNode.addContainedPoint(x, y);

                    // If the bisector is not on the edge then add the id of the markers seperated by the bisector
                    // to the intersectionNode's relevant marker container
                    if (!isEdgeLine(bisectID1)) {
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID1.split(SeparatorConstants.COMMA)[0]));
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID1.split(SeparatorConstants.COMMA)[1]));
                    }

                    // Do the same with the second line
                    if (!isEdgeLine(bisectID2)) {
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID2.split(SeparatorConstants.COMMA)[0]));
                        intersectionNode.addRelevantMarker(Integer.parseInt(bisectID2.split(SeparatorConstants.COMMA)[1]));
                    }

                    // Get the relevant marker IDs for this intersection
                    final Integer[] relevantMarkerIDS = {Integer.parseInt(bisectID1.split(SeparatorConstants.COMMA)[0]), Integer.parseInt(bisectID1.split(SeparatorConstants.COMMA)[1]), Integer.parseInt(bisectID2.split(SeparatorConstants.COMMA)[0]), Integer.parseInt(bisectID2.split(SeparatorConstants.COMMA)[1])};

                    // Loop through the relevant markers and calculate which intersection is closest to which marker
                    for (final Integer id : relevantMarkerIDS) {
                        // If the marker exists on screen
                        if (nodesOnScreen.containsKey(id)) {
                            // relevant intersections map contains the marker id then replace its value with the current intersectionNode
                            if (relevantIntersections.containsKey(id)) {
                                relevantIntersections.replace(id, intersectionMap.get(intersectionPoint));

                                // else make a new entry in the map with the marker id being the key and the intersectionNode being the value
                            } else {
                                relevantIntersections.put(id, intersectionMap.get(intersectionPoint));
                            }
                        }
                    }

                    // If the intersection point is at a corner
                    if (((roundedX == 0 && roundedY == 0) || (roundedX == Math.round(MapView.MAP_WIDTH) && roundedY == 0)
                            || (roundedX == 0 && roundedY == MapView.MAP_HEIGHT)
                            || (roundedX == Math.round(MapView.MAP_WIDTH) && roundedY == MapView.MAP_HEIGHT))
                            && !corners.contains(intersectionMap.get(intersectionPoint))) {

                        // Then add the node to the corners array created earlier
                        corners.add(intersectionMap.get(intersectionPoint));

                    }

                    // If the lineMap entry for bisect2 doesn't have this intersectionNode in its value array then add it
                    if (!lineMap.get(bisect2).contains(intersectionMap.get(intersectionPoint))) {
                        lineMap.get(bisect2).add(intersectionMap.get(intersectionPoint));

                    }

                    // For every intersectionNode in the array in lineMap for key bisect2, add the current intersectionPoint to
                    // their connected points array
                    for (int k = 0; k < lineMap.get(bisect2).size(); k++) {
                        lineMap.get(bisect2).get(k).addConnectedPoint(intersectionMap.get(intersectionPoint));
                    }

                }
            }
        }
    }

    private boolean doesIntersect(final Line l1, final Line l2) {
        return l1.intersects(l2.getBoundsInLocal()) && l2.intersects(l1.getBoundsInLocal());
    }

    /**
     * For polygons that will contain segments of map edges as well as a corner
     * some separate logic needs to be run since corners are intersection of map
     * edges and not bisectors
     */
    private void connectMarkersToCorners() {
        // For every corner intersection
        for (final IntersectionNode n : corners) {
            boolean neighboursFound = false;

            // get its location
            final double x = n.getX();
            final double y = n.getY();

            IntersectionNode nearest1 = null;
            IntersectionNode nearest2 = null;

            double distanceNearest1 = Double.MAX_VALUE;
            double distanceNearest2 = Double.MAX_VALUE;

            // Calculate the nearest intersection points to the corner in both directions that go out from the corner
            for (final IntersectionNode neighbour : n.getConnectedPoints()) {
                if (neighbour.getKey().equals(n.getKey())) {
                    continue;
                }

                // Calculate distance between corner and next closet neighbour
                final double distance = Vec3.getDistance(new Vec3(x, y), new Vec3(neighbour.getX(), neighbour.getY()));

                // Nearest neighbour for horizontal neihgbour
                if (neighbour.getX() == x && distance < distanceNearest1) {
                    distanceNearest1 = distance;
                    nearest1 = neighbour;
                    // nearest neighbour for vertical neighbours
                } else if (neighbour.getY() == y && distance < distanceNearest2) {
                    distanceNearest2 = distance;
                    nearest2 = neighbour;
                }
            }

            if (nearest1 == null || nearest2 == null) {
                return;
            }

            // If the nearest neighbours both have relevant markers they belong to then valid neighbours have been found
            if (!nearest1.getRelevantMarkers().isEmpty() && !nearest2.getRelevantMarkers().isEmpty()) {
                neighboursFound = true;
            }


            distanceNearest1 = Double.MAX_VALUE;
            distanceNearest2 = Double.MAX_VALUE;

            if (!neighboursFound) {
                nearest1 = findCornerNeighbours(nearest1, n);
                nearest2 = findCornerNeighbours(nearest2, n);
            }

            final List<Integer> relevantMarkerIds = new ArrayList<>();

            // Loop through the relevant markers for one of the neighbours
            for (final Integer id : nearest1.getRelevantMarkers()) {
                // If the other neighbour also has this as a relevant marker
                // Then add it to the relevantMarkerIds above
                if (nearest2.getRelevantMarkers().contains(id) && !n.getRelevantMarkers().contains(id)) {
                    relevantMarkerIds.add(id);
                }
            }

            // Out of the relevant markers the one related to the corner will be the closest to the corner
            if (relevantMarkerIds.size() > 1) {
                double distance = Double.MAX_VALUE;
                int id = relevantMarkerIds.get(0);

                // calculate closest marker to conrer
                for (final Integer markerID : relevantMarkerIds) {
                    final double markerX = nodesOnScreen.get(markerID).getX();
                    final double markerY = nodesOnScreen.get(markerID).getY();

                    if (Vec3.getDistance(new Vec3(markerX, markerY), new Vec3(n.getX(), n.getY())) < distance) {
                        distance = Vec3.getDistance(new Vec3(markerX, markerY), new Vec3(n.getX(), n.getY()));
                        id = markerID;
                    }
                }

                // Finally we can add the relevant marker to the corner
                n.addRelevantMarker(id);

                // If there is only one marker in the relevantMarkerIds list then just add the first one to the corner
            } else if (!relevantMarkerIds.isEmpty()) {
                n.addRelevantMarker(relevantMarkerIds.get(0));
            }
        }
    }

    private IntersectionNode findCornerNeighbours(IntersectionNode nearest, final IntersectionNode n) {
        double distanceNearest = Double.MAX_VALUE;
        IntersectionNode grandParent = n;
        IntersectionNode parent1 = nearest;
        for (int i = 0; i < 3; i++) {
            if (nearest.getRelevantMarkers().isEmpty()) {
                // Loop through all its connected points
                for (final IntersectionNode neighbour : parent1.getConnectedPoints()) {
                    if (neighbour.getKey().equals(parent1.getKey())) {
                        continue;
                    }

                    final double distance = Vec3.getDistance(new Vec3(neighbour.getX(), neighbour.getY()), new Vec3(parent1.getX(), parent1.getY()));

                    // Find nearest distance of neighbour that is not in the direction of parent
                    if ((neighbour.getX() != grandParent.getX() || neighbour.getY() != grandParent.getY()) && distance < distanceNearest) {
                        distanceNearest = distance;
                        nearest = neighbour;
                    }
                }

                // If the nearest intersectionNode is STILL empty therefore it is another corner then set up the loop
                // to run again but with this newly found corner as the parent
                if (nearest.getRelevantMarkers().isEmpty()) {
                    grandParent = parent1;
                    parent1 = nearest;
                }
            } else {
                break;
            }

        }

        return nearest;
    }

    /**
     * Create the actual shape of the polygon with all the information
     * calculated above
     */
    private void createPolygons() {
        // A visited set to make sure we dont't visit the same vertice twice for 1 shape
        final Set<String> visited = new HashSet<>();

        final ConstellationColor[] palette = ConstellationColor.createPalette(nodeID);

        // For every marker on screen
        for (final Integer markerID : nodesOnScreen.keySet()) {
            boolean completedShape = false;
            visited.clear();

            // Create the shape graphic
            final Polygon markerZone = new Polygon();

            markerZone.setStroke(palette[markerID].getJavaFXColor());
            markerZone.setFill(palette[markerID].getJavaFXColor());
            markerZone.setOpacity(0.5);

            markerZone.setMouseTransparent(true);

            // Get closest intersection point to the marker
            IntersectionNode relevantIntersection = relevantIntersections.get(markerID);

            while (!completedShape) {
                // Add the intersection node to the visited set
                visited.add(relevantIntersection.getKey());

                // Add the intersectionNode as the point of the shape
                markerZone.getPoints().addAll(new Double[]{relevantIntersection.getX(), relevantIntersection.getY()});

                // for all the intersectionNode's connected neighbours
                for (int i = 0; i < relevantIntersection.getConnectedPoints().size(); i++) {

                    final IntersectionNode neighbor = relevantIntersection.getConnectedPoints().get(i);

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
    private boolean isEdgeLine(final String lineID) {
        return lineID.equals(TOP_ID) || lineID.equals(BOTTOM_ID) || lineID.equals(LEFT_ID) || lineID.equals(RIGHT_ID);
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

            // If only 1 marker exists on screen no calculations are needed the shape will just be a rectangle with one colour
        } else if (nodesOnScreen.size() == 1) {
            final ConstellationColor[] palette = ConstellationColor.createPalette(1);

            Rectangle r = new Rectangle();
            r.setX(0);
            r.setY(0);
            r.setWidth(MapView.MAP_WIDTH);
            r.setHeight(MapView.MAP_HEIGHT);
            r.setFill(palette[0].getJavaFXColor());
            r.setStroke(palette[0].getJavaFXColor());
            r.setOpacity(0.5);
            r.setMouseTransparent(true);
            layer.getChildren().add(r);
        }


    }

    @Override
    public Group getLayer() {
        return layer;
    }
}
