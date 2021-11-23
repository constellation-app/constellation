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
package au.gov.asd.tac.constellation.graph.attribute.utilities;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Gets the active graph's attributes or attribute names based on the specified
 * graphElementType
 *
 * @author formalhaut69
 */
public class AttributeUtilities {
    
    private AttributeUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns the names of the attributes that match the specified
     * graphElementType and attributeDescription. Returns all the names of every
     * attribute if no attributeDescription is specified
     *
     * @param graphElementType
     * @param attributeDescription
     * @return
     */
    public static List<String> getAttributeNames(final GraphElementType graphElementType, String attributeDescription) {
        final List<String> attributeNames = new ArrayList<>();
        for (Attribute attribute : getAttributes(graphElementType)) {
            if (attribute.getAttributeType().equals(attributeDescription) || "".equals(attributeDescription)) {
                attributeNames.add(attribute.getName());
            }
        }
        return attributeNames;
    }

    /**
     * Returns the attributes that match the graphElementType
     *
     * @param graphElementType
     * @return
     */
    public static List<Attribute> getAttributes(final GraphElementType graphElementType) {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        final List<Attribute> attributes = new ArrayList<>();
        if (graph != null && graph.getSchema() != null) {
            final ReadableGraph readableGraph = graph.getReadableGraph();
            try {
                final int attributeCount = readableGraph.getAttributeCount(graphElementType);
                for (int i = 0; i < attributeCount; i++) {
                    attributes.add(new GraphAttribute(readableGraph, readableGraph.getAttribute(graphElementType, i)));
                }
            } finally {
                readableGraph.release();
            }
        }
        return attributes;
    }
}
