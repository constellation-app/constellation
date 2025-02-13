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
package au.gov.asd.tac.constellation.graph.value.operations;

import au.gov.asd.tac.constellation.graph.value.OperatorRegistry;
import au.gov.asd.tac.constellation.graph.value.Operators;
import au.gov.asd.tac.constellation.graph.value.StringOperation;
import au.gov.asd.tac.constellation.graph.value.constants.StringConstant;
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;
import au.gov.asd.tac.constellation.graph.value.readables.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;

/**
 *
 * @author sirius
 */
public class EndsWith {

    // Explicitly calling the constructor on a String appears to be necessary to
    // allow the registry to find the operator.
    public static final String NAME = new String("ENDS_WITH");

    private EndsWith() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static final StringOperation STRING_OPERATION = (p1, p2) -> p1 != null && p2 != null && p1.endsWith(p2);

    public static void register(final Operators operators) {
        final OperatorRegistry registry = operators.getRegistry(NAME);
        STRING_OPERATION.register(registry);

        // e.g. Used when query is as follows: x endswith '1'
        registry.register(FloatReadable.class, StringConstant.class, BooleanReadable.class, (p1, p2)
                -> () -> Float.toString(p1.readFloat()).endsWith(p2.readString())
        );

        // e.g. Used when query is as follows: x endswith 6.1
        registry.register(FloatReadable.class, FloatReadable.class, BooleanReadable.class, (p1, p2)
                -> () -> Float.toString(p1.readFloat()).endsWith(Float.toString(p2.readFloat()))
        );

        // e.g. Used when query is as follows: x endswith 6
        registry.register(FloatReadable.class, IntReadable.class, BooleanReadable.class, (p1, p2)
                -> () -> Float.toString(p1.readFloat()).endsWith(Integer.toString(p2.readInt()))
        );

        registry.register(IntReadable.class, StringConstant.class, BooleanReadable.class, (p1, p2)
                -> () -> Integer.toString(p1.readInt()).endsWith(p2.readString())
        );

        registry.register(IntReadable.class, IntReadable.class, BooleanReadable.class, (p1, p2)
                -> () -> Integer.toString(p1.readInt()).endsWith(Integer.toString(p2.readInt()))
        );

        registry.register(IntReadable.class, FloatReadable.class, BooleanReadable.class, (p1, p2)
                -> () -> Integer.toString(p1.readInt()).endsWith(Float.toString(p2.readFloat()))
        );
    }

    static {
        register(Operators.getDefault());
    }
}
