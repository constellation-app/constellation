/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;

/**
 *
 * @author sirius
 */
public class And {

    // Explicitly calling the constructor on a String appears to be necessary to
    // allow the registry to find the operator.
    public static final String NAME = new String("AND");

    private And() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static void register(Operators operators) {
        final OperatorRegistry registry = operators.getRegistry(NAME);

        registry.register(BooleanReadable.class, BooleanReadable.class, BooleanReadable.class, (p1, p2)
                -> () -> p1.readBoolean() && p2.readBoolean());
    }

    static {
        register(Operators.getDefault());
    }
}
