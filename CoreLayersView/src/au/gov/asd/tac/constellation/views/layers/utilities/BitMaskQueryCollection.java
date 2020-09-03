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

import au.gov.asd.tac.constellation.graph.Graph;
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
    private final List<BitMaskQuery> queries;
    
    private final IntValue index = new IntValue();

    private final List<BitMaskQuery> activeQueries = new ArrayList<>();    
    private final List<BitMaskQuery> updateQueries = new ArrayList<>();
    
    public BitMaskQueryCollection(List<BitMaskQuery> queries) {
        this.queries = queries;
    }
    
    public void setActiveQueries(long activeQueryBitMask) {
        activeQueries.clear();
        for (BitMaskQuery bitMaskQuery : queries) {
            if (bitMaskQuery.isActive(activeQueryBitMask)) {
                activeQueries.add(bitMaskQuery);
            }
        }
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

    public void updateBitMasks(GraphWriteMethods graph, int bitMaskAttributeId, int visibleAttributeId, GraphElementType elementType, long activeQueriesBitMask) {
        setActiveQueries(activeQueriesBitMask);
        if (update(graph)) {
            final int elementCount = elementType.getElementCount(graph);
            for (int position = 0; position < elementCount; position++) {
                index.writeInt(position);
                final int elementId = elementType.getElement(graph, position);
                if (bitMaskAttributeId == Graph.NOT_FOUND) {
                    final long bitMask = 0;
                    final long updatedBitMask = updateBitMask(bitMask);
                    graph.setFloatValue(visibleAttributeId, elementId, updatedBitMask == 0 ? 0 : 1000000000);
                } else {
                    final long bitMask = graph.getLongValue(bitMaskAttributeId, elementId);
                    final long updatedBitMask = updateBitMask(bitMask);
                    graph.setLongValue(bitMaskAttributeId, elementId, updatedBitMask);
                    graph.setFloatValue(visibleAttributeId, elementId, updatedBitMask == 0 ? 0 : 1000000000);
                }
                
            }
        }
    }
}
