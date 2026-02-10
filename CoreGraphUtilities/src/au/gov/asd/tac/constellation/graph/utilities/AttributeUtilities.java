/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
     * Return the attribute id's for a {@link GraphElementType} used by a graph. 
     * Only returns ids for attributes registered by the graph's schema so no custom attributes will be returned.
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
     * Get all the vertex attributes on the given graph
     *
     * @param graph The graph
     * @return Map of attribute names
     */
    public static Map<String, Integer> getVertexAttributes(final GraphReadMethods graph) {
        final Map<String, Integer> attributeIds = new TreeMap<>();

        if (graph == null) {
            return attributeIds;
        }
        
        for (int i = 0; i < graph.getAttributeCount(GraphElementType.VERTEX); i++) {
            final int attributeId = graph.getAttribute(GraphElementType.VERTEX, i);
            attributeIds.put(graph.getAttributeName(attributeId), attributeId);          
        }

        return attributeIds;
    }
    
    /**
     * Get a list of the names of all the attributes for a given element type on the active graph.
     * 
     * @param elementType The graph element type to get attributes from
     * @return a list of attribute names for the given graph element type on the active graph
     */
    public static List<String> getAttributeNames(final GraphElementType elementType) {
        try (final ReadableGraph rg = GraphManager.getDefault().getActiveGraph().getReadableGraph()) {
            return getAttributeNames(rg, elementType);
        }
    }
    
    /**
     * Get a list of the names of all the attributes for a given element type on the given graph.
     * 
     * @param graph The graph to extract attribute names from
     * @param elementType The graph element type to get attributes from
     * @return a list of attribute names for the given graph element type on the given graph
     */
    public static List<String> getAttributeNames(final GraphReadMethods graph, final GraphElementType elementType) {
        final List<String> attributeNames = new ArrayList<>();
        for (int i = 0; i < graph.getAttributeCount(elementType); i++) {
            attributeNames.add(graph.getAttributeName(graph.getAttribute(elementType, i)));
        }
        
        return attributeNames;
    }
}
