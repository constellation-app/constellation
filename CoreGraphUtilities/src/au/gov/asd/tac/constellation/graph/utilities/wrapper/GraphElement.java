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
package au.gov.asd.tac.constellation.graph.utilities.wrapper;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.LongAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;

/**
 *
 * @author capella
 */
public abstract class GraphElement {

    protected final GraphElementType type;
    protected GraphWrapper graph;
    protected final int id;

    protected GraphElement(GraphElementType type, GraphWrapper graph, int id) {
        this.type = type;
        this.graph = graph;
        this.id = id;
    }

    public final String getStringValue(String attribute) {
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        return graph.getReadableGraph().getStringValue(attributeId, id);
    }

    public final String getStringValue(SchemaAttribute attribute) {
        return getStringValue(attribute.getName());
    }

    public final int getIntValue(String attribute) {
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        return graph.getReadableGraph().getIntValue(attributeId, id);
    }

    public final int getIntValue(SchemaAttribute attribute) {
        return getIntValue(attribute.getName());
    }

    public final long getLongValue(String attribute) {
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        return graph.getReadableGraph().getLongValue(attributeId, id);
    }

    public final long getLongValue(SchemaAttribute attribute) {
        return getLongValue(attribute.getName());
    }

    public final boolean getBooleanValue(String attribute) {
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        return graph.getReadableGraph().getBooleanValue(attributeId, id);
    }

    public final boolean getBooleanValue(SchemaAttribute attribute) {
        return getBooleanValue(attribute.getName());
    }

    public final <T> T getObjectValue(String attribute) {
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        return graph.getReadableGraph().getObjectValue(attributeId, id);
    }

    public final <T> T getObjectValue(SchemaAttribute attribute) {
        return getObjectValue(attribute.getName());
    }

    public final void setStringValue(String attribute, String value) {
        final GraphWriteMethods writableGraph = graph.getWritableGraph();
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        if (attributeId == Graph.NOT_FOUND) {
            attributeId = writableGraph.getSchema() != null ? writableGraph.getSchema().getFactory().ensureAttribute(writableGraph, type, attribute) : Graph.NOT_FOUND;
            if (attributeId == Graph.NOT_FOUND) {
                attributeId = writableGraph.addAttribute(type, StringAttributeDescription.ATTRIBUTE_NAME, attribute, "", null, null);
            }
        }
        writableGraph.setStringValue(attributeId, id, value);
    }

    public final void setStringValue(SchemaAttribute attribute, String value) {
        setStringValue(attribute.getName(), value);
    }

    public final void setIntValue(String attribute, int value) {
        final GraphWriteMethods writableGraph = graph.getWritableGraph();
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        if (attributeId == Graph.NOT_FOUND) {
            attributeId = writableGraph.getSchema() != null ? writableGraph.getSchema().getFactory().ensureAttribute(writableGraph, type, attribute) : Graph.NOT_FOUND;
            if (attributeId == Graph.NOT_FOUND) {
                attributeId = writableGraph.addAttribute(type, IntegerAttributeDescription.ATTRIBUTE_NAME, attribute, "", 0, null);
            }
        }
        writableGraph.setIntValue(attributeId, id, value);
    }

    public final void setIntValue(SchemaAttribute attribute, int value) {
        setIntValue(attribute.getName(), value);
    }

    public final void setLongValue(String attribute, long value) {
        final GraphWriteMethods writableGraph = graph.getWritableGraph();
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        if (attributeId == Graph.NOT_FOUND) {
            attributeId = writableGraph.getSchema() != null ? writableGraph.getSchema().getFactory().ensureAttribute(writableGraph, type, attribute) : Graph.NOT_FOUND;
            if (attributeId == Graph.NOT_FOUND) {
                attributeId = writableGraph.addAttribute(type, LongAttributeDescription.ATTRIBUTE_NAME, attribute, "", 0, null);
            }
        }
        writableGraph.setLongValue(attributeId, id, value);
    }

    public final void setLongValue(SchemaAttribute attribute, long value) {
        setLongValue(attribute.getName(), value);
    }

    public final void setObjectValue(String attribute, Object value) {
        final GraphWriteMethods writableGraph = graph.getWritableGraph();
        int attributeId = graph.getReadableGraph().getAttribute(type, attribute);
        if (attributeId == Graph.NOT_FOUND) {
            attributeId = writableGraph.getSchema() != null ? writableGraph.getSchema().getFactory().ensureAttribute(writableGraph, type, attribute) : Graph.NOT_FOUND;
            if (attributeId == Graph.NOT_FOUND) {
                attributeId = writableGraph.addAttribute(type, ObjectAttributeDescription.ATTRIBUTE_NAME, attribute, "", 0, null);
            }
        }
        writableGraph.setObjectValue(attributeId, id, value);
    }

    public final void setObjectValue(SchemaAttribute attribute, Object value) {
        setObjectValue(attribute.getName(), value);
    }

    public final int getId() {
        return id;
    }

    public final GraphWrapper getGraph() {
        return graph;
    }

    public final GraphElementType getElementType() {
        return type;
    }

    public void deferRemove() {
        graph.deferRemove(this);
    }

    public abstract void unsafeImmediateRemove();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        GraphElement o = (GraphElement) obj;
        return o.getId() == getId() && graph.getReadableGraph().getId().equals(o.graph.getReadableGraph().getId());
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
