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
package au.gov.asd.tac.constellation.views.attributecalculator.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author twilight_sparkle
 */
public class CalculatorContextManager {

    final GraphReadMethods graph;
    GraphElementType elementType;
    final GraphElementType outermostElementType;
    private final Stack<Integer> elementIds = new Stack<>();
    private final Stack<GraphElementType> elementTypes = new Stack<>();
    private final List<AbstractCalculatorValue> dependantValues = new ArrayList<>();

    public CalculatorContextManager(final GraphReadMethods graph, final GraphElementType elementType) {
        this.graph = graph;
        outermostElementType = elementType;
        this.elementType = outermostElementType;
    }

    public int current() {
        return elementIds.peek();
    }

    public void enter(final int elementId) {
        enter(elementId, elementType);
    }

    public void enter(final int elementId, final GraphElementType elementType) {
        elementIds.push(elementId);
        elementTypes.push(elementType);
        this.elementType = elementType;
        updateDependantValues();
    }

    public void require(GraphElementType elementType) {
        if (this.elementType != elementType) {
            if (elementType == GraphElementType.VERTEX) {
                throw new RuntimeException("Attribute Calculator Error: using node function in transaction context");
            } else {
                throw new RuntimeException("Attribute Calculator Error: using transaction function in node context");
            }
        }
    }

    public void require(Collection<GraphElementType> elementTypes) {
        if (!elementTypes.contains(elementType)) {
            if (elementType != GraphElementType.VERTEX) {
                throw new RuntimeException("Attribute Calculator Error: using node function in transaction context");
            } else {
                throw new RuntimeException("Attribute Calculator Error: using transaction function in node context");
            }
        }
    }

    public void exit() {
        elementIds.pop();
        elementTypes.pop();
        elementType = elementTypes.isEmpty() ? outermostElementType : elementTypes.peek();
        updateDependantValues();
    }

    private void updateDependantValues() {
        if (elementIds.isEmpty()) {
            return;
        }
        for (AbstractCalculatorValue value : dependantValues) {
            value.updateValue(graph, elementType, current());
        }
    }

    public void addDependantValue(AbstractCalculatorValue value) {
        dependantValues.add(value);
    }

}
