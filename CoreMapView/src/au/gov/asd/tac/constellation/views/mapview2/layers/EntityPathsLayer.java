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

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.views.mapview2.MapView;
import au.gov.asd.tac.constellation.views.mapview2.markers.AbstractMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.LineMarker;
import au.gov.asd.tac.constellation.views.mapview2.markers.PointMarker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Draws line marker from one node to another
 *
 * @author altair1673
 */
public class EntityPathsLayer extends AbstractPathsLayer {


    public EntityPathsLayer(final MapView parent, final int id, final Map<String, AbstractMarker> queriedMarkers) {
        super(parent, id, queriedMarkers);
    }

    /**
     * For every queried markers add all its connected neighbours to the idList
     *
     * @param idList
     */
    private void extractQueriedMarkersAndNeighbours(final List<Integer> idList) {
        for (final AbstractMarker value : queriedMarkers.values()) {
            if (value instanceof PointMarker) {
                final PointMarker m = (PointMarker) value;
                m.getConnectedNodeIdList().forEach(id -> idList.add(id));
            }
        }

    }

    /**
     * Sets up the line markers for the entity paths
     */
    @Override
    public void setUp() {
        entityPaths.getChildren().clear();

        // Get current readable graph
        final GraphReadMethods graph = currentGraph.getReadableGraph();

        // Get attribute Ids for type and datetime
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);

        final int transDateTimeAttrId = TemporalConcept.TransactionAttribute.DATETIME.get(graph);

        final List<Integer> idList = new ArrayList<>();

        final double lineMarkerXOffset = 1;
        final double lineMarkerYOffset = 149;

        extractQueriedMarkersAndNeighbours(idList);

        // For all connected neighbours
        for (int i = 0; i < idList.size(); i++) {

            // Get the vertex ID from the graph
            final int vertexID = graph.getVertex(idList.get(i));

            // Get the type attribute of the vertex
            final SchemaVertexType vertexType = graph.getObjectValue(vertexTypeAttributeId, vertexID);

            // If the type is a "location"
            if (vertexType != null && vertexType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {

                // Get the neighbour count of that vertex
                final int neighbourCount = graph.getVertexNeighbourCount(vertexID);

                // For every neighbour
                for (int neighbourPos = 0; neighbourPos < neighbourCount; neighbourPos++) {

                    // Get the neighbour id
                    final int neighbourID = graph.getVertexNeighbour(vertexID, neighbourPos);

                    // Get the the type attribute from the neighbour
                    final SchemaVertexType neighbourType = graph.getObjectValue(vertexTypeAttributeId, neighbourID);

                    // If the type is a "location"
                    if (neighbourType != null && !neighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {

                        final Set<Long> locationDateTimes = new HashSet<>();

                        final int neighbourLinkID = graph.getLink(vertexID, neighbourID);

                        // Get the ammount of transactions connecting the two vertices
                        final int neighbourLinkTransactionCount = graph.getLinkTransactionCount(neighbourLinkID);

                        // For every transaction
                        for (int neighbourLinkTransPos = 0; neighbourLinkTransPos < neighbourLinkTransactionCount; neighbourLinkTransPos++) {
                            final int neighbourLinkTransID = graph.getLinkTransaction(neighbourLinkID, neighbourLinkTransPos);

                            // Extract its time and store it
                            final long neighbourLinkTransDateTime = graph.getLongValue(transDateTimeAttrId, neighbourLinkTransID);
                            locationDateTimes.add(neighbourLinkTransDateTime);
                        }

                        // For all second neighbours
                        final List<Integer> validSecondNeighbours = new ArrayList<>();
                        final int secondNeighbourCount = graph.getVertexNeighbourCount(neighbourID);

                        for (int secondNeighbourPos = 0; secondNeighbourPos < secondNeighbourCount; secondNeighbourPos++) {
                            final int secondNeighbourID = graph.getVertexNeighbour(neighbourID, secondNeighbourPos);

                            // Get the type attribute
                            final SchemaVertexType secondNeighbourType = graph.getObjectValue(vertexTypeAttributeId, secondNeighbourID);

                            // Add velid second neighbours to array
                            if (secondNeighbourType != null && secondNeighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                                validSecondNeighbours.add(secondNeighbourID);
                            }
                        }

                        // Get lattitud and logitude attribute
                        final int lonID2 = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                        final int latID2 = SpatialConcept.VertexAttribute.LATITUDE.get(graph);

                        // For each datetime transaction
                        locationDateTimes.forEach(locationDateTime -> {
                            int pathSecondNeighbour = GraphConstants.NOT_FOUND;
                            long closestTimeDifference = Long.MAX_VALUE;

                            // For all the second neighbours
                            for (final int secondNeighbourId : validSecondNeighbours) {
                                // Get the transaction count between neighbour and second neighbour
                                final int secondNeighbourLinkId = graph.getLink(neighbourID, secondNeighbourId);
                                final int secondNeighbourLinkTransactionCount = graph.getLinkTransactionCount(secondNeighbourLinkId);

                                // For every transaction
                                for (int secondNeighbourLinkTransactionPosition = 0; secondNeighbourLinkTransactionPosition < secondNeighbourLinkTransactionCount; secondNeighbourLinkTransactionPosition++) {

                                    final int secondNeighbourLinkTransactionId = graph.getLinkTransaction(secondNeighbourLinkId, secondNeighbourLinkTransactionPosition);
                                    final long secondNeighbourLinkTransactionDateTime = graph.getLongValue(transDateTimeAttrId, secondNeighbourLinkTransactionId);
                                    final long timeDifference = secondNeighbourLinkTransactionDateTime - locationDateTime;

                                    // If the time difference between the transaction of the second neigbour and the transaction of the first neighbour
                                    // is less than the closestTimeDifference then store it
                                    if (timeDifference > 0 && timeDifference < closestTimeDifference) {
                                        closestTimeDifference = timeDifference;
                                        pathSecondNeighbour = secondNeighbourId;
                                    }
                                }
                            }

                            // Draw a line from neighbour to second neighbour
                            if (pathSecondNeighbour != GraphConstants.NOT_FOUND) {
                                final float sourceLat = graph.getObjectValue(latID2, vertexID);
                                final float sourceLon = graph.getObjectValue(lonID2, vertexID);

                                final float destLat = graph.getObjectValue(latID2, pathSecondNeighbour);
                                final float destLon = graph.getObjectValue(lonID2, pathSecondNeighbour);

                                final String coordinateKey = (double) sourceLat + "," + (double) sourceLon + "," + (double) destLat + "," + (double) destLon;

                                final LineMarker l = new LineMarker(parent, parent.getNewMarkerID(), vertexID, (float) sourceLat, (float) sourceLon, (float) destLat, (float) destLon, lineMarkerXOffset, lineMarkerYOffset);
                                if (!parent.getAllMarkers().keySet().contains(coordinateKey)) {
                                    l.setMarkerPosition(MapView.MAP_WIDTH, MapView.MAP_HEIGHT);
                                    entityPaths.getChildren().add(l.getMarker());
                                }
                            }

                        });

                    }

                }

            }
        }

    }


}
