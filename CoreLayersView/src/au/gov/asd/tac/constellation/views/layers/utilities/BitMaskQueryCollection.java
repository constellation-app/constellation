/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.value.values.IntValue;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sirius
 */
public class BitMaskQueryCollection {
    private final GraphElementType elementType;
    
    private final BitMaskQuery[] queries = new BitMaskQuery[64];
    
    private final IntValue index = new IntValue();

    private final List<BitMaskQuery> activeQueries = new ArrayList<>();    
    private final List<BitMaskQuery> updateQueries = new ArrayList<>();

    private long activeQueriesBitMask = 0;
    
    public BitMaskQueryCollection(GraphElementType elementType) {
        this.elementType = elementType;
    }
    
    public void setQuery(String query, int bitMaskIndex) {
        queries[bitMaskIndex] = new BitMaskQuery(new Query(elementType, query), bitMaskIndex);
    }
    
    public void setActiveQueries(long activeQueriesBitMask) {
        this.activeQueriesBitMask = activeQueriesBitMask;
        activeQueries.clear();
        for (BitMaskQuery bitMaskQuery : queries) {
            if (bitMaskQuery != null && bitMaskQuery.isActive(activeQueriesBitMask)) {
                activeQueries.add(bitMaskQuery);
            }
        }
    }
    
    public long getActiveQueriesBitMask() {
        return activeQueriesBitMask;
    }
    
    public boolean update(GraphReadMethods graph) {
        updateQueries.clear();
        for (BitMaskQuery activeQuery : activeQueries) {
            if (activeQuery.update(graph, index)) {
                updateQueries.add(activeQuery);
            }
        }
        return !updateQueries.isEmpty();
    }
    
    public long updateBitMask(long bitMask) {
        for (BitMaskQuery updateQuery : updateQueries) {
            bitMask = updateQuery.updateBitMask(bitMask);
        }
        return bitMask;
    }

    public void updateBitMasks(GraphWriteMethods graph, int bitMaskAttributeId, int visibleAttributeId) {
        if (update(graph)) {
            final int elementCount = elementType.getElementCount(graph);
            for (int position = 0; position < elementCount; position++) {
                final int elementId = elementType.getElement(graph, position);
                index.writeInt(elementId);
                final long bitMask = graph.getLongValue(bitMaskAttributeId, elementId);
                final long updatedBitMask = updateBitMask(bitMask);
                graph.setLongValue(bitMaskAttributeId, elementId, updatedBitMask);
                graph.setFloatValue(visibleAttributeId, elementId, (updatedBitMask & activeQueriesBitMask) == 0 ? 0.0f : 1.0f);
            }
        }
    }
}
