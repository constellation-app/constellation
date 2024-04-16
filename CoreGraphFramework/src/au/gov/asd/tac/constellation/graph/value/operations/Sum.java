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

import au.gov.asd.tac.constellation.graph.value.ArithmeticOperation;
import au.gov.asd.tac.constellation.graph.value.OperatorRegistry;
import au.gov.asd.tac.constellation.graph.value.Operators;
import au.gov.asd.tac.constellation.graph.value.constants.StringConstant;
import au.gov.asd.tac.constellation.graph.value.readables.StringReadable;

/**
 *
 * @author sirius
 */
public class Sum {

    // Explicitly calling the constructor on a String appears to be necessary to
    // allow the registry to find the operator. 
    public static final String NAME = new String("SUM");

    private Sum() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static final ArithmeticOperation ARITHMETIC_OPERATION = new ArithmeticOperation() {
        @Override
        public double execute(final double p1, final double p2) {
            return p1 + p2;
        }

        @Override
        public float execute(final float p1, final float p2) {
            return p1 + p2;
        }

        @Override
        public long execute(final long p1, final long p2) {
            return p1 + p2;
        }

        @Override
        public int execute(final int p1, final int p2) {
            return p1 + p2;
        }
    };

    public static void register(final Operators operators) {
        final OperatorRegistry registry = operators.getRegistry(NAME);
        ARITHMETIC_OPERATION.register(registry);

        registry.register(StringReadable.class, StringReadable.class, StringReadable.class, (p1, p2) -> () -> p1.readString() + p2.readString());
        registry.register(StringConstant.class, StringConstant.class, StringConstant.class, (p1, p2) -> () -> p1.readString() + p2.readString());
    }

    static {
        register(Operators.getDefault());
    }
}
