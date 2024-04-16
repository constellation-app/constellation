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
package au.gov.asd.tac.constellation.views.scripting.graph;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;

/**
 * A representation of an attribute for use with scripting.
 *
 * @author cygnus_x-1
 */
public class SAttribute {

    private final GraphReadMethods readableGraph;
    private final int id;
    private final GraphElementType elementType;
    private final String attributeType;
    private final String name;
    private final String description;
    private final Object defaultValue;
    private final String mergerId;

    public SAttribute(final GraphReadMethods readableGraph, final int id, final GraphElementType elementType, 
            final String attributeType, final String name, final String description, final Object defaultValue, 
            final String mergerId) {
        this.readableGraph = readableGraph;
        this.id = id;
        this.elementType = elementType;
        this.attributeType = attributeType;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.mergerId = mergerId;
    }

    public SAttribute(final GraphReadMethods readableGraph, final int id) {
        this(readableGraph, id, readableGraph.getAttributeElementType(id), readableGraph.getAttributeType(id),
                readableGraph.getAttributeName(id), readableGraph.getAttributeDescription(id), readableGraph.getAttributeDefaultValue(id),
                readableGraph.getAttributeMerger(id) == null ? null : readableGraph.getAttributeMerger(id).getId());
    }

    public SAttribute(final GraphReadMethods readableGraph, final GraphElementType elementType, final String name) {
        this(readableGraph, readableGraph.getAttribute(elementType, name));
    }

    private GraphWriteMethods writableGraph() {
        return (GraphWriteMethods) readableGraph;
    }

    /**
     * Get the id of this attribute.
     *
     * @return the id of this attribute.
     */
    public int id() {
        return id;
    }

    /**
     * Get the element type of this attribute.
     *
     * @return the element type of this attribute.
     */
    public GraphElementType elementType() {
        return elementType;
    }

    /**
     * Get the type of this attribute.
     *
     * @return the type of this attribute.
     */
    public String attributeType() {
        return attributeType;
    }

    /**
     * Get the name of this attribute.
     *
     * @return the name of this attribute.
     */
    public String name() {
        return name;
    }

    /**
     * Get a description of this attribute.
     *
     * @return a description of this attribute.
     */
    public String description() {
        return description;
    }

    /**
     * Get the default value of this attribute.
     *
     * @return the default value of this attribute.
     */
    public Object defaultValue() {
        return defaultValue;
    }

    /**
     * Get the id of the merger for this attribute.
     *
     * @return the merger id of this attribute.
     */
    public String mergerId() {
        return mergerId;
    }
}
