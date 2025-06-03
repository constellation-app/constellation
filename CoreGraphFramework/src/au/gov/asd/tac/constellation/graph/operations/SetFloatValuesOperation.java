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
package au.gov.asd.tac.constellation.graph.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.util.Arrays;

/**
 * The SetFloatValuesOperation is used when the need arises to change a large
 * number of float attribute values on a single attribute, such as when the x,
 * y, or z attributes on the graph changes. By implementing the float changes as
 * a GraphOperation, significant space can be saved on the undo/redo stack.
 *
 * @author sirius
 */
public class SetFloatValuesOperation extends GraphOperation {

    private final GraphReadMethods graph;
    private final int attribute;
    private int[] changes;
    private int changeCount = 0;
    private int lowestChange = Integer.MAX_VALUE;
    private int highestChange = Integer.MIN_VALUE;
    private int offset;

    public SetFloatValuesOperation(final GraphReadMethods graph, final GraphElementType elementType, final int attribute) {
        this.graph = graph;
        this.attribute = attribute;
        changes = new int[elementType.getElementCapacity(graph)];
    }

    /**
     * Set a value for an element
     *
     * @param id The id of the element
     * @param value The value of the element
     */
    public void setValue(final int id, final float value) {
        final float currentValue = graph.getFloatValue(attribute, id);
        if (currentValue != value) {
            if (changes[id] == 0) {
                changeCount++;
            }
            changes[id] = Float.floatToRawIntBits(value) ^ Float.floatToRawIntBits(currentValue);
            if (id > highestChange) {
                highestChange = id;
            }
            if (id < lowestChange) {
                lowestChange = id;
            }
        } else if (changes[id] != 0) {
            changeCount--;
            changes[id] = 0;
        }
    }

    public void finish() {
        if (lowestChange != 0) {
            if (lowestChange > highestChange) {
                lowestChange = highestChange;
                changes = new int[0];
            } else {
                offset = lowestChange;
                changes = Arrays.copyOfRange(changes, lowestChange, highestChange + 1);
            }
        }
    }

    @Override
    public void execute(final GraphWriteMethods graph) {
        for (int i = 0; i < changes.length; i++) {
            if (changes[i] != 0) {
                final int element = offset + i;
                graph.setFloatValue(attribute, element, Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(attribute, element)) ^ changes[i]));
            }
        }
    }

    @Override
    public void undo(final GraphWriteMethods graph) {
        for (int i = 0; i < changes.length; i++) {
            if (changes[i] != 0) {
                final int element = offset + i;
                graph.setFloatValue(attribute, element, Float.intBitsToFloat(Float.floatToRawIntBits(graph.getFloatValue(attribute, element)) ^ changes[i]));
            }
        }
    }

    @Override
    public boolean isMoreEfficient() {
        return changeCount * 3 > size();
    }

    @Override
    public int size() {
        return changes.length * 4;
    }
}
