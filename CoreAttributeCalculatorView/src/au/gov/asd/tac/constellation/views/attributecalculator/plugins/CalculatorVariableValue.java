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
package au.gov.asd.tac.constellation.views.attributecalculator.plugins;

import au.gov.asd.tac.constellation.views.attributecalculator.utilities.AbstractCalculatorValue;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 *
 * @author twilight_sparkle
 */
public class CalculatorVariableValue extends AbstractCalculatorValue {

    private final CalculatorVariable variable;

    public CalculatorVariableValue(final CalculatorVariable variable) {
        this.variable = variable;
    }

    @Override
    public void updateValue(final GraphReadMethods graph, final GraphElementType elementType, final int elementId) {
        if (!checkElementType(elementType)) {
            return;
        }
        val = variable.getValue(graph, elementType, elementId);
    }

//    @Override
//    public Object val() {
//        if (val == INVALID_IN_NODE_CONTEXT) {
//            throw new RuntimeException("Attribute Calculator Error: using transaction variable in node context");
//        } else if (val == INVALID_IN_TRANSACTION_CONTEXT) {
//            throw new RuntimeException("Attribute Calculator Error: using node variable in transaction context");
//        }
//        return val;
//    }
    public boolean checkElementType(final GraphElementType elementType) {
        if (variable.getElementType() == null) {
            return true;
        }
        if (variable.getElementType() == GraphElementType.VERTEX && elementType != GraphElementType.VERTEX) {
            val = INVALID_IN_TRANSACTION_CONTEXT;
            return false;
        } else if (variable.getElementType() != GraphElementType.VERTEX && elementType == GraphElementType.VERTEX) {
            val = INVALID_IN_NODE_CONTEXT;
            return false;
        }
        return true;
    }

//    @Override
//    public Object val() {
//        return val;
//    }
}
