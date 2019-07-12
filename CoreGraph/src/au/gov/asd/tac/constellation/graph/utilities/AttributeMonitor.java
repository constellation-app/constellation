/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.SchemaAttribute;

/**
 * An AttributeMonitor monitors the life cycle of a single attribute on a graph.
 *
 * @author sirius
 */
public class AttributeMonitor {

    private final GraphElementType elementType;
    private final String name;

    private AttributeTransition transition = AttributeTransition.UNDEFINED;

    private int id;
    private long uid;

    private long attributeModificationCounter;
    private long valueModificationCounter;

    public AttributeMonitor(GraphElementType elementType, String attributeName) {
        this.elementType = elementType;
        this.name = attributeName;
    }

    public AttributeMonitor(GraphElementType elementType, String attributeName, GraphReadMethods graph) {
        this(elementType, attributeName);
        update(graph);
    }

    public AttributeMonitor(SchemaAttribute schemaAttribute) {
        this(schemaAttribute.getElementType(), schemaAttribute.getName());
    }

    public AttributeMonitor(SchemaAttribute schemaAttribute, GraphReadMethods graph) {
        this(schemaAttribute);
        update(graph);
    }

    public void reset() {
        transition = AttributeTransition.UNDEFINED;
    }

    public AttributeTransition reset(GraphReadMethods graph) {
        transition = AttributeTransition.UNDEFINED;
        return update(graph);
    }

    public AttributeState getState() {
        return transition.currentState;
    }

    public AttributeTransition getTransition() {
        return transition;
    }

    public GraphElementType getElementType() {
        return elementType;
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

    public final AttributeTransition update(GraphReadMethods graph) {
        switch (transition.currentState) {

            case UNDEFINED:
                updateFromUndefined(graph);
                break;

            case MISSING:
                updateFromMissing(graph);
                break;

            case PRESENT:
                updateFromPresent(graph);
                break;
        }

        return transition;
    }

    public static void update(GraphReadMethods graph, AttributeMonitor... attributeMonitors) {
        for (AttributeMonitor attributeMonitor : attributeMonitors) {
            attributeMonitor.update(graph);
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("AttributeMonitor[");
        out.append("name=").append(name);
        out.append(",elementType=").append(elementType);
        out.append(",transition=").append(transition);
        out.append(",id=").append(id);
        out.append(",uid=").append(uid);
        out.append(",attributeModificationCounter=").append(attributeModificationCounter);
        out.append(",valueModificationCounter=").append(valueModificationCounter);
        out.append("]");
        return out.toString();
    }

    private void updateFromUndefined(GraphReadMethods graph) {
        id = graph.getAttribute(elementType, name);
        attributeModificationCounter = graph.getAttributeModificationCounter();
        if (id == Graph.NOT_FOUND) {
            transition = AttributeTransition.UNDEFINED_TO_MISSING;
        } else {
            transition = AttributeTransition.UNDEFINED_TO_PRESENT;
            uid = graph.getAttributeUID(id);
            valueModificationCounter = graph.getValueModificationCounter(id);
        }
    }

    private void updateFromMissing(GraphReadMethods graph) {
        final long currentAttributeModificationCounter = attributeModificationCounter;
        attributeModificationCounter = graph.getAttributeModificationCounter();
        if (currentAttributeModificationCounter == attributeModificationCounter) {
            transition = AttributeTransition.STILL_MISSING;
        } else {
            id = graph.getAttribute(elementType, name);
            if (id == Graph.NOT_FOUND) {
                transition = AttributeTransition.STILL_MISSING;
            } else {
                transition = AttributeTransition.ADDED;
                uid = graph.getAttributeUID(id);
                valueModificationCounter = graph.getValueModificationCounter(id);
            }
        }
    }

    private void updateFromPresent(GraphReadMethods graph) {

        // Update the attribute modification counter
        final long currentAttributeModificationCounter = attributeModificationCounter;
        attributeModificationCounter = graph.getAttributeModificationCounter();

        // No attributes have been added or removed
        if (currentAttributeModificationCounter == attributeModificationCounter) {

            // Update the value modification counter
            final long currentValueModificationCounter = valueModificationCounter;
            valueModificationCounter = graph.getValueModificationCounter(id);

            // This attribute has no changed values
            if (currentValueModificationCounter == valueModificationCounter) {
                transition = AttributeTransition.UNCHANGED;

                // This attribute has changed values
            } else {
                transition = AttributeTransition.CHANGED;
            }

            // Attributes have been added or removed
        } else {
            id = graph.getAttribute(elementType, name);

            // This attribute has been removed
            if (id == Graph.NOT_FOUND) {
                transition = AttributeTransition.REMOVED;

                // This attribute is still present (perhaps removed and then re-added)
            } else {
                final long currentUID = uid;
                uid = graph.getAttributeUID(id);

                // This attribute has not been removed and then re-added
                if (currentUID == uid) {
                    final long currentValueModificationCounter = valueModificationCounter;
                    valueModificationCounter = graph.getValueModificationCounter(id);

                    // This attribute has no changed values
                    if (currentValueModificationCounter == valueModificationCounter) {
                        transition = AttributeTransition.UNCHANGED;

                        // This attribute has changed values
                    } else {
                        transition = AttributeTransition.CHANGED;
                    }

                    // This attribute has been removed and then re-added
                } else {
                    transition = AttributeTransition.REMOVED_AND_ADDED;
                    valueModificationCounter = graph.getValueModificationCounter(id);
                }
            }
        }
    }

    public static enum AttributeState {

        UNDEFINED,
        MISSING,
        PRESENT;
    }

    public static enum AttributeTransition {

        UNDEFINED(AttributeState.UNDEFINED, AttributeState.UNDEFINED),
        UNDEFINED_TO_MISSING(AttributeState.UNDEFINED, AttributeState.MISSING),
        UNDEFINED_TO_PRESENT(AttributeState.UNDEFINED, AttributeState.PRESENT),
        CHANGED(AttributeState.PRESENT, AttributeState.PRESENT),
        ADDED(AttributeState.MISSING, AttributeState.PRESENT),
        REMOVED_AND_ADDED(AttributeState.PRESENT, AttributeState.PRESENT),
        UNCHANGED(AttributeState.PRESENT, AttributeState.PRESENT),
        STILL_MISSING(AttributeState.MISSING, AttributeState.MISSING),
        REMOVED(AttributeState.PRESENT, AttributeState.MISSING);

        private AttributeTransition(AttributeState previousState, AttributeState currentState) {
            this.previousState = previousState;
            this.currentState = currentState;
            this.mask = 1 << ordinal();
        }

        private final AttributeState previousState;
        private final AttributeState currentState;
        private final int mask;

        public AttributeState getPreviousState() {
            return previousState;
        }

        public AttributeState getCurrentState() {
            return currentState;
        }
    }

    public static class AttributeTransitionFilter {

        private final int mask;

        public AttributeTransitionFilter(AttributeTransition... attributeTransitions) {
            int mask = 0;
            for (AttributeTransition attributeTransition : attributeTransitions) {
                mask |= attributeTransition.mask;
            }
            this.mask = mask;
        }

        public boolean matchesTransition(AttributeTransition attributeTransition) {
            return (mask & attributeTransition.mask) != 0;
        }

        public boolean matchesTransition(AttributeMonitor attributeMonitor) {
            return (mask & attributeMonitor.getTransition().mask) != 0;
        }

        public boolean matchesTransitions(AttributeMonitor... attributeMonitors) {
            for (AttributeMonitor attributeMonitor : attributeMonitors) {
                if (matchesTransition(attributeMonitor)) {
                    return true;
                }
            }
            return false;
        }
    }
}
