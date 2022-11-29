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

import au.gov.asd.tac.constellation.graph.value.ComparisonOperation;
import au.gov.asd.tac.constellation.graph.value.OperatorRegistry;
import au.gov.asd.tac.constellation.graph.value.Operators;

/**
 *
 * @author sirius
 */
public class LessThanOrEquals {

    // Explicitly calling the constructor on a String appears to be necessary to
    // allow the registry to find the operator.
    public static final String NAME = new String("LESS_THAN_OR_EQUALS");

    private LessThanOrEquals() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static final ComparisonOperation COMPARISON_OPERATION = new ComparisonOperation() {
        @Override
        public boolean execute(double p1, double p2) {
            return p1 <= p2;
        }

        @Override
        public boolean execute(float p1, float p2) {
            return p1 <= p2;
        }

        @Override
        public boolean execute(long p1, long p2) {
            return p1 <= p2;
        }

        @Override
        public boolean execute(int p1, int p2) {
            return p1 <= p2;
        }

        @Override
        public boolean execute(String p1, String p2) {
            if (p1 == null) {
                return true;
            } else {
                return p2 != null && p1.compareTo(p2) <= 0;
            }
        }
    };

    public static void register(Operators operators) {
        final OperatorRegistry registry = operators.getRegistry(NAME);
        COMPARISON_OPERATION.register(registry);
    }

    static {
        register(Operators.getDefault());
    }
}
