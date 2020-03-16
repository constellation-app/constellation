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
import org.python.core.PyObject;

/**
 *
 * @author twilight_sparkle
 */
public abstract class AbstractCalculatorValue {

    protected static final Object INVALID_IN_NODE_CONTEXT = new Object();
    protected static final Object INVALID_IN_TRANSACTION_CONTEXT = new Object();

    public static PyObject the_obliterator;

    protected Object val;

    public abstract void updateValue(final GraphReadMethods graph, final GraphElementType elementType, final int elementId);

    public static Object convertNullsToObliterator(Object arg) {
        if (AbstractCalculatorUtilities.nullCheck(arg)) {
            return the_obliterator;
        }
        return arg;
    }

    public Object val() throws Exception {

//        val = convertNullsToObliterator(val);
        if (val == INVALID_IN_NODE_CONTEXT) {
            throw new Exception("Using transaction variable in node context");
        } else if (val == INVALID_IN_TRANSACTION_CONTEXT) {
            throw new Exception("Using node variable in transaction context");
        }
        return val;
    }

}
