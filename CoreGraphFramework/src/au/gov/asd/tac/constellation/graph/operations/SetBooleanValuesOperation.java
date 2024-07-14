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
package au.gov.asd.tac.constellation.graph.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.BitSet;

/**
 * The SetBooleanValuesOperation is used when the need arises to change a large
 * number of boolean attribute values on a single attribute, such as when the
 * selection on the graph changes. By implementing the boolean changes as a
 * GraphOperation, significant space can be saved on the undo/redo stack.
 *
 * @author sirius
 */
public class SetBooleanValuesOperation extends GraphOperation {

    private final GraphReadMethods graph;
    private final int attribute;
    private final int capacity;
    private int changeCount = 0;
    private final BitSet bitSet;

    public SetBooleanValuesOperation(final GraphReadMethods graph, final GraphElementType elementType, final int attribute) {
        this.graph = graph;
        this.attribute = attribute;
        this.capacity = elementType.getElementCapacity(graph);
        this.bitSet = new BitSet(500);
    }

    public void setValue(final int element, final boolean value) {
        final boolean changed = value != graph.getBooleanValue(attribute, element);
        if (changed != bitSet.get(element)) {
            if (changed) {
                changeCount++;
            } else {
                changeCount--;
            }
            bitSet.set(element, changed);
        }
    }

    @Override
    public void execute(final GraphWriteMethods graph) {
        for (int element = bitSet.nextSetBit(0); element >= 0; element = bitSet.nextSetBit(element + 1)) {
            graph.setBooleanValue(attribute, element, !graph.getBooleanValue(attribute, element));
        }
    }

    @Override
    public void undo(final GraphWriteMethods graph) {
        for (int element = bitSet.nextSetBit(0); element >= 0; element = bitSet.nextSetBit(element + 1)) {
            graph.setBooleanValue(attribute, element, !graph.getBooleanValue(attribute, element));
        }
    }

    @Override
    public boolean isMoreEfficient() {
        return changeCount * 8 > capacity;
    }

    @Override
    public int size() {
        return capacity / 8;
    }
}
