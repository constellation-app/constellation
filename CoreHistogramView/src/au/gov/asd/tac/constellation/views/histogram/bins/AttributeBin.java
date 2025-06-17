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
package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.attribute.interaction.AbstractAttributeInteraction;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import java.util.Comparator;

/**
 * A bin that holds arbitrary object values.
 *
 * @author twilight_sparkle
 */
public class AttributeBin extends ObjectBin {

    private String attributeType;
    private Comparator<Object> comparator;

    public AttributeBin() {
        this(null, null);
    }

    public AttributeBin(final String attributeType, final Comparator<Object> comparator) {
        this.attributeType = attributeType;
        this.comparator = comparator;
    }

    public String getAttributeType() {
        return attributeType;
    }

    @Override
    public int compareTo(Bin o) {
        final AttributeBin bin = (AttributeBin) o;
        return comparator.compare(key, bin.key);
    }

    @Override
    public void prepareForPresentation() {
        final AbstractAttributeInteraction<?> attributeInteraction = AbstractAttributeInteraction.getInteraction(attributeType);
        label = key == null ? null : attributeInteraction.getDisplayText(key);
    }

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        key = graph.getObjectValue(attribute, element);
    }

    @Override
    public void init(GraphReadMethods graph, int attributeId) {
        attributeType = graph.getAttributeType(attributeId);
        final AbstractAttributeInteraction<?> attributeInteraction = AbstractAttributeInteraction.getInteraction(attributeType);
        comparator = attributeInteraction.getComparator();
    }

    @Override
    public Bin create() {
        return new AttributeBin(attributeType, comparator);
    }
}
