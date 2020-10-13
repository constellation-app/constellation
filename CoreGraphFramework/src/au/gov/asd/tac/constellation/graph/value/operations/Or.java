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
package au.gov.asd.tac.constellation.graph.value.operations;

import au.gov.asd.tac.constellation.graph.value.OperatorRegistry;
import au.gov.asd.tac.constellation.graph.value.Operators;
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;

/**
 *
 * @author sirius
 */
public class Or {

    public static final String NAME = "OR";

    private Or() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static void register(Operators operators) {
        final OperatorRegistry registry = operators.getRegistry(NAME);

        registry.register(BooleanReadable.class, BooleanReadable.class, BooleanReadable.class, (p1, p2) -> {
            return () -> p1.readBoolean() || p2.readBoolean();
        });
    }

    static {
        register(Operators.getDefault());
    }
}
