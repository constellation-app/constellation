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
package au.gov.asd.tac.constellation.graph.monitor;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A ManualGraphMonitor monitors a graph for changes and alerts various
 * listeners depending on the type of change that has occurred.
 *
 * @author sirius
 */
public class ManualGraphMonitor {

    protected static final boolean VERBOSE = false;

    // The most recently seen modification counters for the currently active graph
    private long globalModificationCounter = Long.MIN_VALUE;
    private long structureModificationCounter = Long.MIN_VALUE;
    private long attributeModificationCounter = Long.MIN_VALUE;

    // Listeners that get run for structure/attribute changes respectively
    private ListenerRecord structureListener, attributeListener;

    // Attribute records mapped by their elementTypes and labels
    // This map is iterated through each time we need to rediscover the attributes so a LinkedHashMap is faster
    private final Map<AttributeKey, AttributeRecord> attributes = new LinkedHashMap<>();

    // A list of attributes that need to be monitored each update
    private int updateAttributesCount = 0;
    private AttributeRecord[] updateAttributes = new AttributeRecord[16];

    private final Map<GraphMonitorListener, ListenerRecord> listeners = new HashMap<>();

    private long currentUpdate = 0;

    public interface GraphMonitorListener {

        public void graphChanged(GraphReadMethods graph);
    }

    /**
     * Sets the listener that gets called when the structure of the graph
     * changes. This occurs whenever vertices or transactions are added or
     * removed.
     *
     * @param listener the new listener.
     */
    public final void setStructureListener(GraphMonitorListener listener) {
        if (structureListener != null) {
            removeListener(structureListener);
        }
        structureListener = listener == null ? null : addListener(listener);
    }

    /**
     * Sets the listener that gets called when attributes are added or removed.
     *
     * @param listener the new listener.
     */
    public final void setAttributeListener(GraphMonitorListener listener) {
        if (attributeListener != null) {
            removeListener(attributeListener);
        }
        attributeListener = listener == null ? null : addListener(listener);
    }

    public void setAttributeListener(final GraphElementType elementType, final Collection<String> labels, GraphMonitorListener listener) {
        for (String label : labels) {
            setAttributeListener(elementType, label, listener);
        }
    }

    public void setAttributeListener(final GraphElementType elementType, final String label, GraphMonitorListener listener) {

        if (label == null) {
            return;
        }

        // Create a key for this attribute
        AttributeKey attributeKey = new AttributeKey(elementType, label);

        // Are we removing this attribute
        if (listener == null) {

            // Remove any existing attribute record
            AttributeRecord attributeRecord = attributes.remove(attributeKey);
            if (attributeRecord != null) {
                removeListener(attributeRecord.listener);
                if (attributeRecord.updateIndex >= 0) {
                    updateAttributes[attributeRecord.updateIndex] = updateAttributes[--updateAttributesCount];
                    updateAttributes[updateAttributesCount] = null;
                }
            }

            // Are we setting a listener
        } else {
            AttributeRecord attributeRecord = attributes.get(attributeKey);
            if (attributeRecord == null) {
                attributeRecord = new AttributeRecord(elementType, label, addListener(listener));
                attributes.put(attributeKey, attributeRecord);
            } else {
                removeListener(attributeRecord.listener);
                attributeRecord.listener = addListener(listener);
            }

        }
    }

    public final void setGraph(GraphReadMethods graph) {
        globalModificationCounter = graph.getGlobalModificationCounter();
        structureModificationCounter = graph.getStructureModificationCounter();
        attributeModificationCounter = graph.getAttributeModificationCounter();

        if (updateAttributes.length < attributes.size()) {
            updateAttributes = new AttributeRecord[attributes.size()];
        }

        resetAttributes(graph);

        if (structureListener != null) {
            structureListener.lastUpdate = -1;
        }

        if (attributeListener != null) {
            attributeListener.lastUpdate = -1;
        }
    }

    /**
     * Update this ManualGraphMonitor
     *
     * @param graph the graph that this monitor is monitoring.
     * @param produceEvents should the update notify the listeners?
     */
    public void update(GraphReadMethods graph, boolean produceEvents) {

        // Move on to the next update
        currentUpdate++;

        if (VERBOSE) {
            System.out.println("GRAPH MONITOR: update()");
        }

        final long oldGlobalModificationCounter = globalModificationCounter;
        globalModificationCounter = graph.getGlobalModificationCounter();
        if (globalModificationCounter != oldGlobalModificationCounter) {

            final long oldStructureModificationCounter = structureModificationCounter;
            structureModificationCounter = graph.getStructureModificationCounter();
            if (structureModificationCounter != oldStructureModificationCounter) {

                if (VERBOSE) {
                    System.out.println("GRAPH MONITOR: structure changed");
                }

                if (produceEvents && structureListener != null) {
                    structureListener.graphChanged(graph);
                }
            }

            final long oldAttributeModificationCounter = attributeModificationCounter;
            attributeModificationCounter = graph.getAttributeModificationCounter();
            if (attributeModificationCounter != oldAttributeModificationCounter) {

                if (VERBOSE) {
                    System.out.println("GRAPH MONITOR: attributes added/removed");
                }

                if (produceEvents && attributeListener != null) {
                    attributeListener.graphChanged(graph);
                }

                resetAttributes(graph);

            } else {
                updateAttributes(graph, produceEvents);
            }
        }
    }

    private void resetAttributes(GraphReadMethods graph) {

        if (VERBOSE) {
            System.out.println("GRAPH MONITOR: recreating attributes");
        }

        updateAttributesCount = 0;
        for (Entry<AttributeKey, AttributeRecord> entry : attributes.entrySet()) {

            AttributeRecord attributeRecord = entry.getValue();

            attributeRecord.attributeId = graph.getAttribute(attributeRecord.elementType, attributeRecord.label);

            if (attributeRecord.attributeId == Graph.NOT_FOUND) {
                attributeRecord.updateIndex = -1;
            } else {
                attributeRecord.modificationCounter = graph.getValueModificationCounter(attributeRecord.attributeId);
                attributeRecord.updateIndex = updateAttributesCount;
                updateAttributes[updateAttributesCount++] = attributeRecord;
                attributeRecord.listener.lastUpdate = -1;
            }
        }
    }

    private void updateAttributes(GraphReadMethods graph, boolean produceEvents) {
        AttributeRecord[] copy = Arrays.copyOf(updateAttributes, updateAttributesCount);

        for (AttributeRecord attributeRecord : copy) {

            // Has the modification counter changed?
            long oldModificationCounter = attributeRecord.modificationCounter;
            attributeRecord.modificationCounter = graph.getValueModificationCounter(attributeRecord.attributeId);
            if (attributeRecord.modificationCounter != oldModificationCounter) {

                if (VERBOSE) {
                    System.out.println("GRAPH MONITOR: attribute changed: " + attributeRecord.elementType + " " + attributeRecord.label);
                }

                if (produceEvents && attributeRecord.listener != null) {
                    attributeRecord.listener.graphChanged(graph);
                }
            }
        }
    }

    private static final class AttributeKey {

        private final GraphElementType elementType;
        private final String label;

        public AttributeKey(GraphElementType elementType, String label) {
            this.elementType = elementType;
            this.label = label;
        }

        @Override
        public int hashCode() {
            return elementType.hashCode() * 113 + label.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AttributeKey) {
                AttributeKey attributeKey = (AttributeKey) other;
                return elementType == attributeKey.elementType && label.equals(attributeKey.label);
            }
            return false;
        }
    }

    private static final class AttributeRecord {

        final GraphElementType elementType;
        final String label;

        private ListenerRecord listener;
        private int attributeId;
        private long modificationCounter;

        // the position in the updateAttributes array or -1 if no such attribute exists in the current graph
        private int updateIndex;

        public AttributeRecord(GraphElementType elementType, String label, ListenerRecord listener) {
            this.elementType = elementType;
            this.label = label;
            this.listener = listener;
        }
    }

    protected class ListenerRecord {

        private final GraphMonitorListener listener;
        private long lastUpdate = -1;
        private int listenerCount = 1;

        public ListenerRecord(GraphMonitorListener listener) {
            this.listener = listener;
        }

        public void graphChanged(GraphReadMethods graph) {
            if (lastUpdate != currentUpdate) {
                lastUpdate = currentUpdate;
                listener.graphChanged(graph);
            }
        }

        public void graphChangedAlways(GraphReadMethods graph) {
            listener.graphChanged(graph);
        }
    }

    protected ListenerRecord addListener(GraphMonitorListener listener) {
        ListenerRecord listenerRecord = listeners.get(listener);
        if (listenerRecord == null) {
            listenerRecord = new ListenerRecord(listener);
            listeners.put(listener, listenerRecord);
            return listenerRecord;
        } else {
            listenerRecord.listenerCount++;
            return listenerRecord;
        }
    }

    protected void removeListener(ListenerRecord listenerRecord) {
        if (--listenerRecord.listenerCount == 0) {
            listeners.remove(listenerRecord.listener);
        }
    }
}
