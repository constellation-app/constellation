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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;

/**
 * An AttributeValueMonitor monitors the life cycle of a single attribute on a
 * graph.
 *
 * @author sirius
 */
public class AttributeValueMonitor extends Monitor {

    private final GraphElementType elementType;
    private final String name;

    private int id;
    private long uid;

    private long attributeModificationCounter;

    public AttributeValueMonitor(final GraphElementType elementType, final String attributeName) {
        this.elementType = elementType;
        this.name = attributeName;
    }

    public AttributeValueMonitor(final GraphElementType elementType, final String attributeName, final GraphReadMethods graph) {
        this(elementType, attributeName);
        update(graph);
    }

    public AttributeValueMonitor(final SchemaAttribute schemaAttribute) {
        this(schemaAttribute.getElementType(), schemaAttribute.getName());
    }

    public AttributeValueMonitor(final SchemaAttribute schemaAttribute, final GraphReadMethods graph) {
        this(schemaAttribute);
        update(graph);
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    @Override
    public void reset() {
        super.reset();
        id = -1;
        uid = -1L;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public long getUid() {
        return uid;
    }

    @Override
    public final MonitorTransition update(final GraphReadMethods graph) {
        // If we are passed a null graph then just go the the undefined state
        if (graph == null) {
            reset();
        } else if (!graph.getId().equals(currentGraphId)) {
            reset();
            currentGraphId = graph.getId();
            updateFromUndefined(graph);
        } else {
            // Update based on the current state
            switch (transition.getCurrentState()) {
                case UNDEFINED -> updateFromUndefined(graph);
                case MISSING -> updateFromMissing(graph);
                case PRESENT -> updateFromPresent(graph);
                default -> {
                    // do nothing
                }
            }
        }
        return transition;
    }

    private void updateFromUndefined(final GraphReadMethods graph) {
        id = graph.getAttribute(elementType, name);
        attributeModificationCounter = graph.getAttributeModificationCounter();
        if (id == Graph.NOT_FOUND) {
            transition = MonitorTransition.UNDEFINED_TO_MISSING;
        } else {
            transition = MonitorTransition.UNDEFINED_TO_PRESENT;
            uid = graph.getAttributeUID(id);
            modificationCounter = graph.getValueModificationCounter(id);
        }
    }

    private void updateFromMissing(final GraphReadMethods graph) {
        final long currentAttributeModificationCounter = attributeModificationCounter;
        attributeModificationCounter = graph.getAttributeModificationCounter();
        if (currentAttributeModificationCounter == attributeModificationCounter) {
            transition = MonitorTransition.STILL_MISSING;
        } else {
            id = graph.getAttribute(elementType, name);
            if (id == Graph.NOT_FOUND) {
                transition = MonitorTransition.STILL_MISSING;
            } else {
                transition = MonitorTransition.ADDED;
                uid = graph.getAttributeUID(id);
                modificationCounter = graph.getValueModificationCounter(id);
            }
        }
    }

    private void updateFromPresent(final GraphReadMethods graph) {
        // Update the attribute modification counter
        final long currentAttributeModificationCounter = attributeModificationCounter;
        attributeModificationCounter = graph.getAttributeModificationCounter();

        // No attributes have been added or removed
        if (currentAttributeModificationCounter == attributeModificationCounter) {
            // Update the value modification counter
            final long currentValueModificationCounter = modificationCounter;
            modificationCounter = graph.getValueModificationCounter(id);

            // This attribute has no changed values
            if (currentValueModificationCounter == modificationCounter) {
                transition = MonitorTransition.UNCHANGED;              
            } else { // This attribute has changed values
                transition = MonitorTransition.CHANGED;
            }           
        } else { // Attributes have been added or removed
            id = graph.getAttribute(elementType, name);

            // This attribute has been removed
            if (id == Graph.NOT_FOUND) {
                transition = MonitorTransition.REMOVED;               
            } else { // This attribute is still present (perhaps removed and then re-added)
                final long currentUID = uid;
                uid = graph.getAttributeUID(id);

                // This attribute has not been removed and then re-added
                if (currentUID == uid) {
                    final long currentValueModificationCounter = modificationCounter;
                    modificationCounter = graph.getValueModificationCounter(id);

                    // This attribute has no changed values
                    if (currentValueModificationCounter == modificationCounter) {
                        transition = MonitorTransition.UNCHANGED;                      
                    } else {  // This attribute has changed values
                        transition = MonitorTransition.CHANGED;
                    }                   
                } else { // This attribute has been removed and then re-added
                    transition = MonitorTransition.REMOVED_AND_ADDED;
                    modificationCounter = graph.getValueModificationCounter(id);
                }
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append("AttributeValueMonitor[");
        out.append("name=").append(name);
        out.append(",elementType=").append(elementType);
        out.append(",transition=").append(transition);
        out.append(",id=").append(id);
        out.append(",uid=").append(uid);
        out.append(",attributeModificationCounter=").append(attributeModificationCounter);
        out.append(",valueModificationCounter=").append(modificationCounter);
        out.append("]");
        return out.toString();
    }
}
