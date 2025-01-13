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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Schema Attribute Utilities
 *
 * @author arcturus
 */
public class AttributeUtilities {
    
    private AttributeUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return the attribute id's for a {@link GraphElementType} used by a graph
     *
     * @param graph The graph
     * @param graphElementType The element type
     * @return Set of attribute id's being used on a given graph
     */
    public static Map<String, Integer> getRegisteredAttributeIdsFromGraph(final GraphReadMethods graph, final GraphElementType graphElementType) {
        final Map<String, Integer> attributeIds = new TreeMap<>();

        final Schema schema = graph.getSchema();
        if (schema != null) {
            final SchemaFactory factory = schema.getFactory();

            final Map<String, SchemaAttribute> attrsMap = factory.getRegisteredAttributes(graphElementType);
            attrsMap.values().stream().forEach(schemaAttribute -> {
                final int attributeId = graph.getAttribute(graphElementType, schemaAttribute.getName());
                if (attributeId != Graph.NOT_FOUND) {
                    attributeIds.put(schemaAttribute.getName(), attributeId);
                }
            });
        }

        return attributeIds;
    }

    /**
     * Return the date time attributes for a {@link GraphElementType} used by a
     * graph
     *
     * @param graph The graph
     * @param graphElementType The element type
     * @return Set of date time attributes used on a given graph
     */
    public static Set<String> getDateTimeAttributes(final Graph graph, final GraphElementType graphElementType) {
        final Set<String> datetimeAttributes = new TreeSet<>();

        if (graph != null && graph.getSchema() != null) {
            final SchemaFactory factory = graph.getSchema().getFactory();
            final Map<String, SchemaAttribute> attributesMap = factory.getRegisteredAttributes(graphElementType);
            attributesMap.values().stream().forEach((SchemaAttribute schemaAttribute) -> {
                if (schemaAttribute.getAttributeType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME)) {
                    datetimeAttributes.add(schemaAttribute.getName());
                }
            });
        }

        return datetimeAttributes;
    }

    /**
     * Return a set of types used by a graph
     *
     * @param graph The graph
     * @return Set of types used by a graph
     */
    public static Set<String> getTypesUsedByGraph(final Graph graph) {
        final List<String> types;
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            final GraphRecordStore recordstore = GraphRecordStoreUtilities.getVertices(rg, false, false, false);
            types = recordstore.getAll(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE);
        }

        return types != null ? new TreeSet<>(types) : new TreeSet<>();
    }

    /**
     * Return a set of vertex attributes
     *
     * @param graph The graph
     * @return Map of attribute names
     */
    public static Map<String, Integer> getVertexAttributes(final GraphReadMethods graph) {
        final Map<String, Integer> attributeIds = new TreeMap<>();

        if (graph == null) {
            return attributeIds;
        }

        int attributeCount = graph.getAttributeCount(GraphElementType.VERTEX);
        for (int i = 0; i < attributeCount; i++) {

            final Attribute attr = new GraphAttribute(graph, graph.getAttribute(GraphElementType.VERTEX, i));

            attributeIds.put(attr.getName(), attr.getId());
            
        }

        return attributeIds;
    }
}
