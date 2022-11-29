/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.mapview.utilities.GraphElement;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * A visualisation of the paths between connected locations on the graph.
 *
 * @author algol
 */
@ServiceProvider(service = MapLayer.class, position = 500)
public class LocationPathsLayer extends AbstractPathsLayer {

    @Override
    public String getName() {
        return "Location Paths";
    }

    @Override
    public List<Tuple<GraphElement, GraphElement>> getPathsForElement(final ReadableGraph graph, final GraphElement element) {
        final List<Tuple<GraphElement, GraphElement>> paths = new ArrayList<>();

        if (element.getType() == GraphElementType.VERTEX) {
            final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);

            final SchemaVertexType vertexType = graph.getObjectValue(vertexTypeAttributeId, element.getId());
            if (vertexType != null && vertexType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                final int neighbourCount = graph.getVertexNeighbourCount(element.getId());
                for (int neighbourPosition = 0; neighbourPosition < neighbourCount; neighbourPosition++) {
                    final int neighbourId = graph.getVertexNeighbour(element.getId(), neighbourPosition);
                    final SchemaVertexType neighbourType = graph.getObjectValue(vertexTypeAttributeId, neighbourId);
                    if (neighbourType != null && neighbourType.isSubTypeOf(AnalyticConcept.VertexType.LOCATION)) {
                        final int neighbourLinkId = graph.getLink(element.getId(), neighbourId);
                        final int outgoingDirection = element.getId() < neighbourId ? GraphConstants.UPHILL : GraphConstants.DOWNHILL;
                        final int linkOutgoingTransactionCount = graph.getLinkTransactionCount(neighbourLinkId, outgoingDirection);
                        for (int i = 0; i < linkOutgoingTransactionCount; i++) {
                            paths.add(Tuple.create(element, new GraphElement(neighbourId, GraphElementType.VERTEX)));
                        }
                    }
                }
            }
        }

        return paths;
    }

    @Override
    public boolean drawPathsToOffscreenMarkers() {
        return true;
    }
}
