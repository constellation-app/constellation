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
package au.gov.asd.tac.constellation.graph.versioning;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Attribute Update Provider
 *
 * @author twilight_sparkle
 */
public abstract class AttributeUpdateProvider implements UpdateProvider {

    private static final int ATTRIBUTE_UPDATE_PRIORITY = 0;
    private final AttributeUpdateItem updateItem;

    public final class AttributeUpdateItem extends UpdateItem {

        @Override
        public boolean appliesToGraph(final StoreGraph graph) {
            return true;
        }

        @Override
        public int getPriority() {
            return ATTRIBUTE_UPDATE_PRIORITY;
        }

        @Override
        public String getName() {
            return getAttributeDescription().getName();
        }
    }

    protected AttributeUpdateProvider() {
        updateItem = new AttributeUpdateItem();
    }

    @Override
    public UpdateItem getVersionedItem() {
        return updateItem;
    }

    @Override
    public int getFromVersionNumber() {
        return getAttributeDescription().getVersion();
    }

    @Override
    public int getToVersionNumber() {
        return getUpdatedAttributeDescription().getVersion();
    }

    protected abstract AttributeDescription getAttributeDescription();

    protected abstract AttributeDescription getUpdatedAttributeDescription();

    @Override
    public void configure(final StoreGraph graph) {
        graph.setAttributeRegistry(AttributeRegistry.copyWithRegsitrations(graph.getAttributeRegistry(), Arrays.asList(getAttributeDescription().getClass())));
    }

    @Override
    public void update(final StoreGraph graph) {
        graph.setAttributeRegistry(AttributeRegistry.copyWithRegsitrations(graph.getAttributeRegistry(), Arrays.asList(getUpdatedAttributeDescription().getClass())));
        for (final GraphElementType elementType : GraphElementType.values()) {
            final Map<Attribute, Object[]> values = new HashMap<>();
            for (int i = 0; i < graph.getAttributeCount(elementType); i++) {
                final int attrId = graph.getAttribute(elementType, i);
                final Attribute attr = new GraphAttribute(graph, attrId);
                if (attr.getAttributeType().equals(getVersionedItem().getName())) {
                    final int elementCount = elementType.getElementCount(graph);
                    final Object[] attrValues = new Object[elementCount];
                    values.put(attr, attrValues);
                    for (int j = 0; j < elementCount; j++) {
                        final int elementId = elementType.getElement(graph, j);
                        attrValues[j] = updateAttributeValue(graph.getObjectValue(attrId, elementId));
                    }
                }
            }
            values.forEach((attr, attrValues) -> {
                final boolean isKey = graph.isPrimaryKey(attr.getId());
                if (isKey) {
                    final int[] key = graph.getPrimaryKey(elementType);
                    final int[] newKey = new int[key.length - 1];
                    int pos = 0;
                    for (final int k : key) {
                        if (k != attr.getId()) {
                            newKey[pos++] = k;
                        }
                    }
                    graph.setPrimaryKey(elementType, newKey);
                }
                graph.removeAttribute(attr.getId());

                // Add the new attribute.
                // Don't forget to update the default value separately.
                final int newAttrId = graph.addAttribute(elementType, attr.getAttributeType(), attr.getName(), attr.getDescription(), updateAttributeValue(attr.getDefaultValue()), null);
                if (isKey) {
                    final int[] key = graph.getPrimaryKey(elementType);
                    final int[] newKey = Arrays.copyOf(key, key.length + 1);
                    newKey[key.length] = newAttrId;
                    graph.setPrimaryKey(elementType, newKey);
                }
                for (int i = 0; i < attrValues.length; i++) {
                    graph.setObjectValue(newAttrId, i, attrValues[i]);
                }
            });
        }
    }

    protected abstract Object updateAttributeValue(final Object value);
}
