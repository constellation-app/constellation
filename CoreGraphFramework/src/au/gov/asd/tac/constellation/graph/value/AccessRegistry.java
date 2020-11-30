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
package au.gov.asd.tac.constellation.graph.value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author sirius
 */
public class AccessRegistry<D> {

    private final Class<D> destinationClass;
    private final List<FunctionRecord<?, D>> functions = new ArrayList<>();

    public AccessRegistry(Class<D> destinationClass) {
        this.destinationClass = destinationClass;
    }

    public <S> AccessRegistry<D> register(Class<S> sourceClass, Function<? super S, ? extends D> function) {
        functions.add(new FunctionRecord<>(sourceClass, destinationClass, function));
        return this;
    }

    public D convert(Object source) {
        final Class<?> sourceClass = source.getClass();
        final List<FunctionRecord<?, D>> applicableRecords = new ArrayList<>();
        functions.forEach(function -> {
            if (function.isApplicable(sourceClass)) {
                boolean insert = true;
                int count = 0;
                while (count < applicableRecords.size()) {
                    switch (function.compareTo(applicableRecords.get(count))) {
                        case -1:
                            insert = false;
                            break;
                        case 0:
                            break;
                        case 1:
                            applicableRecords.remove(count);
                            count -= 1;
                            break;
                        default:
                            // Default case added - S131
                            break;
                    }
                    count++;
                }
                if (insert) {
                    applicableRecords.add(function);
                }
            }
        });
        switch (applicableRecords.size()) {
            case 0:
                return destinationClass.isAssignableFrom(sourceClass) ? (D) source : null;
            case 1:
                return applicableRecords.get(0).apply(source);
            default:
                throw new IllegalArgumentException("Ambiguous operator, requested " + sourceClass.getCanonicalName() + " -> " + destinationClass.getCanonicalName() + ", found " + applicableRecords);
        }
    }

    private static class FunctionRecord<S, D> implements Comparable<FunctionRecord<S, D>> {

        private final Class<S> sourceClass;
        private final Class<D> destinationClass;
        private final Function<? super S, ? extends D> function;

        public FunctionRecord(Class<S> sourceClass, Class<D> destinationClass, Function<? super S, ? extends D> function) {
            this.sourceClass = sourceClass;
            this.destinationClass = destinationClass;
            this.function = function;
        }

        public boolean isApplicable(Class<?> sourceClass) {
            return this.sourceClass.isAssignableFrom(sourceClass);
        }

        public D apply(Object parameter) {
            return function.apply((S) parameter);
        }

        @Override
        public int compareTo(FunctionRecord functionRecord) {
            if (sourceClass == functionRecord.sourceClass) {
                return 0;
            } else if (sourceClass.isAssignableFrom(functionRecord.sourceClass)) {
                return -1;
            } else if (functionRecord.sourceClass.isAssignableFrom(sourceClass)) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return sourceClass.getCanonicalName() + " -> " + destinationClass.getCanonicalName();
        }
    }
}
