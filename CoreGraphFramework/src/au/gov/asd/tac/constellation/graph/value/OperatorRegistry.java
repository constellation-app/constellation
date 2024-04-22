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
package au.gov.asd.tac.constellation.graph.value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author sirius
 */
public class OperatorRegistry {

    private final List<FunctionRecord<?, ?>> functions = new ArrayList<>();
    private final List<BiFunctionRecord<?, ?, ?>> biFunctions = new ArrayList<>();

    public OperatorRegistry(final String name) {
    }

    public final <P, R> OperatorRegistry register(final Class<P> parameterClass, final Class<R> resultClass, final Function<? super P, ? extends R> function) {
        functions.add(new FunctionRecord<>(parameterClass, function));
        return this;
    }

    public final <P1, P2, R> OperatorRegistry register(final Class<P1> parameter1Class, final Class<P2> parameter2Class, final Class<R> resultClass, final BiFunction<? super P1, ? super P2, ? extends R> biFunction) {
        biFunctions.add(new BiFunctionRecord<>(parameter1Class, parameter2Class, biFunction));
        return this;
    }

    public Object apply(final Object parameter) {
        final Class<?> parameterClass = parameter.getClass();
        final List<FunctionRecord<?, ?>> applicableRecords = new ArrayList<>();
        functions.forEach(function -> {
            if (function.isApplicable(parameterClass)) {
                boolean insert = true;
                int count = 0;
                while (count < applicableRecords.size()) {
                    switch (function.compareTo(applicableRecords.get(count))) {
                        case -1 -> insert = false;
                        case 1 -> {
                            applicableRecords.remove(count);
                            count -= 1;
                        }
                        default -> {
                            // do nothing
                        }
                    }
                    count++;
                }
                if (insert) {
                    applicableRecords.add(function);
                }
            }
        });
        switch (applicableRecords.size()) {
            case 0 -> {
                return null;
            }
            case 1 -> {
                return applicableRecords.get(0).apply(parameter);
            }
            default -> throw new IllegalArgumentException("Ambiguous operator");
        }
    }

    public Object apply(final Object parameter1, final Object parameter2) {
        if (parameter1 == null || parameter2 == null) {
            return null;
        }
        final Class<?> parameter1Class = parameter1.getClass();
        final Class<?> parameter2Class = parameter2.getClass();
        final List<BiFunctionRecord<?, ?, ?>> applicableRecords = new ArrayList<>();
        biFunctions.forEach(biFunction -> {
            if (biFunction.isApplicable(parameter1Class, parameter2Class)) {
                boolean insert = true;
                int count = 0;
                while (count < applicableRecords.size()) {
                    switch (biFunction.compareTo(applicableRecords.get(count))) {
                        case -1 -> insert = false;
                        case 1 -> {
                            applicableRecords.remove(count);
                            count -= 1;
                        }
                        default -> {
                            // do nothing
                        }
                    }
                    count++;
                }
                if (insert) {
                    applicableRecords.add(biFunction);
                }
            }
        });
        switch (applicableRecords.size()) {
            case 0 -> {
                return null;
            }
            case 1 -> {
                return applicableRecords.get(0).apply(parameter1, parameter2);
            }
            default -> throw new IllegalArgumentException("Ambiguous operator");
        }
    }

    private static class FunctionRecord<P, R> implements Comparable<FunctionRecord<P, R>> {

        private final Class<P> parameterClass;
        private final Function<? super P, ? extends R> function;

        public FunctionRecord(final Class<P> parameterClass, final Function<? super P, ? extends R> function) {
            this.parameterClass = parameterClass;
            this.function = function;
        }

        public boolean isApplicable(final Class<?> parameter) {
            return parameterClass.isAssignableFrom(parameter);
        }

        // Suppressing warning as the parameter within the registry will always be convertable to type P
        @SuppressWarnings("unchecked")
        public R apply(final Object parameter) {
            return function.apply((P) parameter);
        }

        @Override
        public int compareTo(final FunctionRecord functionRecord) {
            if (parameterClass == functionRecord.parameterClass) {
                return 0;
            } else if (parameterClass.isAssignableFrom(functionRecord.parameterClass)) {
                return -1;
            } else if (functionRecord.parameterClass.isAssignableFrom(parameterClass)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static class BiFunctionRecord<P1, P2, R> implements Comparable<BiFunctionRecord<P1, P2, R>> {

        private final Class<P1> parameter1Class;
        private final Class<P2> parameter2Class;
        private final BiFunction<? super P1, ? super P2, ? extends R> biFunction;

        public BiFunctionRecord(final Class<P1> parameter1Class, final Class<P2> parameter2Class, final BiFunction<? super P1, ? super P2, ? extends R> biFunction) {
            this.parameter1Class = parameter1Class;
            this.parameter2Class = parameter2Class;
            this.biFunction = biFunction;
        }

        public boolean isApplicable(final Class<?> parameter1, final Class<?> parameter2) {
            return parameter1Class.isAssignableFrom(parameter1) && parameter2Class.isAssignableFrom(parameter2);
        }

        // Suppressing warning as the parameters will always be of types P1 and P2
        @SuppressWarnings("unchecked")
        public R apply(final Object parameter1, final Object parameter2) {
            return biFunction.apply((P1) parameter1, (P2) parameter2);
        }

        @Override
        public int compareTo(final BiFunctionRecord functionRecord) {
            if (parameter1Class == functionRecord.parameter1Class) {
                if (parameter2Class == functionRecord.parameter2Class) {
                    return 0;
                } else if (parameter2Class.isAssignableFrom(functionRecord.parameter2Class)) {
                    return -1;
                } else if (functionRecord.parameter2Class.isAssignableFrom(parameter2Class)) {
                    return 1;
                } else {
                    return 0;
                }
            } else if (parameter1Class.isAssignableFrom(functionRecord.parameter1Class)) {
                return parameter2Class.isAssignableFrom(functionRecord.parameter2Class) ? -1 : 0;
            } else if (functionRecord.parameter1Class.isAssignableFrom(parameter1Class)) {
                return functionRecord.parameter2Class.isAssignableFrom(parameter2Class) ? 1 : 0;
            } else {
                return 0;
            }
        }
    }
}
