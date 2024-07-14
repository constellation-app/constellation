/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview.layers;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * A visualisation of the travel paths of entities on the graph.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = MapLayer.class, position = 400)
public class EntityPathsLayer extends AbstractPathsLayer {

    @Override
    public String getName() {
        return "Entity Paths";
    }

    @Override
    public List<Tuple<GraphElement, GraphElement>> getPathsForElement(final ReadableGraph graph, final GraphElement element) {
        final List<Tuple<GraphElement, GraphElement>> paths = new ArrayList<>();

        if (element.getType() == GraphElementType.VERTEX) {
            final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);
            final int transactionDateTimeAttributeId = TemporalConcept.TransactionAttribute.DATETIME.get(graph);

            final SchemaVertexType vertexType = graph.getObjectValue(vertexTypeAttributeId, element.getId());
            if (vertexType != null && vertexType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                final int neighbourCount = graph.getVertexNeighbourCount(element.getId());
                for (int neighbourPosition = 0; neighbourPosition < neighbourCount; neighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(element.getId(), neighbourPosition);
                    final SchemaVertexType neighbourType = graph.getObjectValue(vertexTypeAttributeId, neighbourId);
                    if (neighbourType != null && !neighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                        final Set<Long> locationDateTimes = new HashSet<>();
                        final int neighbourLinkId = graph.getLink(element.getId(), neighbourId);
                        final int neighbourLinkTransactionCount = graph.getLinkTransactionCount(neighbourLinkId);
                        for (int neighbourLinkTransactionPosition = 0; neighbourLinkTransactionPosition < neighbourLinkTransactionCount; neighbourLinkTransactionPosition++) {
                            final int neighbourLinkTransactionId = graph.getLinkTransaction(neighbourLinkId, neighbourLinkTransactionPosition);
                            final long neighbourLinkTransactionDateTime = graph.getLongValue(transactionDateTimeAttributeId, neighbourLinkTransactionId);
                            locationDateTimes.add(neighbourLinkTransactionDateTime);
                        }

                        final List<Integer> validNeighbourNeighbours = new ArrayList<>();
                        final int neighbourNeighbourCount = graph.getVertexNeighbourCount(neighbourId);
                        for (int neighbourNeighbourPosition = 0; neighbourNeighbourPosition < neighbourNeighbourCount; neighbourNeighbourPosition++) {
                            final int neighbourNeighbourId = graph.getVertexNeighbour(neighbourId, neighbourNeighbourPosition);
                            final SchemaVertexType neighbourNeighbourType = graph.getObjectValue(vertexTypeAttributeId, neighbourNeighbourId);
                            if (neighbourNeighbourType != null && neighbourNeighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                                validNeighbourNeighbours.add(neighbourNeighbourId);
                            }
                        }

                        locationDateTimes.forEach(locationDateTime -> {
                            int pathNeighbourNeighbour = GraphConstants.NOT_FOUND;
                            long closestTimeDifference = Long.MAX_VALUE;
                            for (final int neighbourNeighbourId : validNeighbourNeighbours) {
                                final int neighbourNeighbourLinkId = graph.getLink(neighbourId, neighbourNeighbourId);
                                final int neighbourNeighbourLinkTransactionCount = graph.getLinkTransactionCount(neighbourNeighbourLinkId);
                                for (int neighbourNeighbourLinkTransactionPosition = 0; neighbourNeighbourLinkTransactionPosition < neighbourNeighbourLinkTransactionCount; neighbourNeighbourLinkTransactionPosition++) {
                                    final int neighbourNeighbourLinkTransactionId = graph.getLinkTransaction(neighbourNeighbourLinkId, neighbourNeighbourLinkTransactionPosition);
                                    final long neighbourNeighbourLinkTransactionDateTime = graph.getLongValue(transactionDateTimeAttributeId, neighbourNeighbourLinkTransactionId);
                                    final long timeDifference = neighbourNeighbourLinkTransactionDateTime - locationDateTime;
                                    if (timeDifference > 0 && timeDifference < closestTimeDifference) {
                                        closestTimeDifference = timeDifference;
                                        pathNeighbourNeighbour = neighbourNeighbourId;
                                    }
                                }
                            }

                            if (pathNeighbourNeighbour != GraphConstants.NOT_FOUND) {
                                paths.add(Tuple.create(element, new GraphElement(pathNeighbourNeighbour, GraphElementType.VERTEX)));
                            }
                        });
                    }
                }
            }
        }

        return paths;
    }

    @Override
    public boolean drawPathsToOffscreenMarkers() {
        return false;
    }
}
