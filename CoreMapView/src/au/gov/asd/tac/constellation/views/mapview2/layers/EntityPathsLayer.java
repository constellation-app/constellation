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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
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
import java.util.logging.Level;
import javafx.application.Platform;

/**
 *
 * @author altair1673
 */
public class EntityPathsLayer extends AbstractPathsLayer {


    public EntityPathsLayer(MapView parent, int id, Map<String, AbstractMarker> queriedMarkers) {
        super(parent, id, queriedMarkers);
    }

    @Override
    public void setUp() {
        entityPaths.getChildren().clear();
        GraphReadMethods graph = parent.getCurrentGraph().getReadableGraph();
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);

        final int transDateTimeAttrId = TemporalConcept.TransactionAttribute.DATETIME.get(graph);

        //LOGGER.log(Level.SEVERE, "TRANSACTION MY G");
        final List<Integer> idList = new ArrayList<Integer>();

        for (Object value : queriedMarkers.values()) {
            AbstractMarker m = (AbstractMarker) value;

            if (m instanceof PointMarker) {
                m.getIdList().forEach(id -> idList.add(id));
            }
        }

        for (int i = 0; i < idList.size(); ++i) {
            int vertexID = graph.getVertex(idList.get(i));

                final SchemaVertexType vertexType = graph.getObjectValue(vertexTypeAttributeId, vertexID);

                if (vertexType != null && vertexType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                    final int neighbourCount = graph.getVertexNeighbourCount(vertexID);

                    for (int neighbourPos = 0; neighbourPos < neighbourCount; ++neighbourPos) {
                        final int neighbourID = graph.getVertexNeighbour(vertexID, neighbourPos);
                        final SchemaVertexType neighbourType = graph.getObjectValue(vertexTypeAttributeId, neighbourID);
                        if (neighbourType != null && !neighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                            final Set<Long> locationDateTimes = new HashSet<>();

                            final int neighbourLinkID = graph.getLink(vertexID, neighbourID);

                            final int neighbourLinkTransactionCount = graph.getLinkTransactionCount(neighbourLinkID);

                            for (int neighbourLinkTransPos = 0; neighbourLinkTransPos < neighbourLinkTransactionCount; ++neighbourLinkTransPos) {
                                final int neighbourLinkTransID = graph.getLinkTransaction(neighbourLinkID, neighbourLinkTransPos);

                                final long neighbourLinkTransDateTime = graph.getLongValue(transDateTimeAttrId, neighbourLinkTransID);

                                locationDateTimes.add(neighbourLinkTransDateTime);
                            }

                            final List<Integer> validSecondNeighbours = new ArrayList<>();
                            final int secondNeighbourCount = graph.getVertexNeighbourCount(neighbourID);

                            for (int secondNeighbourPos = 0; secondNeighbourPos < secondNeighbourCount; ++secondNeighbourPos) {
                                final int secondNeighbourID = graph.getVertexNeighbour(neighbourID, secondNeighbourPos);

                                final SchemaVertexType secondNeighbourType = graph.getObjectValue(vertexTypeAttributeId, secondNeighbourID);

                                if (secondNeighbourType != null && secondNeighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                                    validSecondNeighbours.add(secondNeighbourID);
                                }
                            }
                            final int lonID2 = SpatialConcept.VertexAttribute.LONGITUDE.get(graph);
                            final int latID2 = SpatialConcept.VertexAttribute.LATITUDE.get(graph);
                            locationDateTimes.forEach(locationDateTime -> {
                                int pathSecondNeighbour = GraphConstants.NOT_FOUND;
                                long closestTimeDifference = Long.MAX_VALUE;

                                for (final int secondNeighbourId : validSecondNeighbours) {
                                    final int secondNeighbourLinkId = graph.getLink(neighbourID, secondNeighbourId);
                                    final int secondNeighbourLinkTransactionCount = graph.getLinkTransactionCount(secondNeighbourLinkId);
                                    for (int secondNeighbourLinkTransactionPosition = 0; secondNeighbourLinkTransactionPosition < secondNeighbourLinkTransactionCount; secondNeighbourLinkTransactionPosition++) {
                                        final int secondNeighbourLinkTransactionId = graph.getLinkTransaction(secondNeighbourLinkId, secondNeighbourLinkTransactionPosition);
                                        final long secondNeighbourLinkTransactionDateTime = graph.getLongValue(transDateTimeAttrId, secondNeighbourLinkTransactionId);
                                        final long timeDifference = secondNeighbourLinkTransactionDateTime - locationDateTime;
                                        if (timeDifference > 0 && timeDifference < closestTimeDifference) {
                                            closestTimeDifference = timeDifference;
                                            pathSecondNeighbour = secondNeighbourId;
                                        }
                                    }
                                }

                                if (pathSecondNeighbour != GraphConstants.NOT_FOUND) {
                                    final float sourceLat = graph.getObjectValue(latID2, vertexID);
                                    final float sourceLon = graph.getObjectValue(lonID2, vertexID);

                                    final float destLat = graph.getObjectValue(latID2, pathSecondNeighbour);
                                    final float destLon = graph.getObjectValue(lonID2, pathSecondNeighbour);

                                    String coordinateKey = (double) sourceLat + "," + (double) sourceLon + "," + (double) destLat + "," + (double) destLon;

                                    LineMarker l = new LineMarker(parent, parent.getNewMarkerID(), vertexID, (float) sourceLat, (float) sourceLon, (float) destLat, (float) destLon, 0, 149);
                                    if (!parent.getAllMarkers().keySet().contains(coordinateKey)) {
                                        //parent.addMarkerToHashMap(coordinateKey, l);

                                        l.setMarkerPosition(MapView.mapWidth, MapView.mapHeight);
                                        entityPaths.getChildren().add(l.getMarker());
                                    } else {
                                        //parent.getAllMarkers().get(coordinateKey).addNodeID(vertexID);
                                    }
                                }

                            });

                        }

                    }

                }
            }

    }


}
