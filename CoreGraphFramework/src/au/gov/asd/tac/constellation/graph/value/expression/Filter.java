/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.value.expression;

import au.gov.asd.tac.constellation.graph.value.IndexedReadable;
import au.gov.asd.tac.constellation.graph.value.Readable;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringWritable;

/**
 *
 * @author sirius
 */
public class Filter {

    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, Class<O> operationClass, ConverterRegistry converterRegistry) {
        final P1 parameter1Value = parameter1.createValue();
        final O operation = converterRegistry.convert(parameter1Value, operationClass);

        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(final int index, final V value) {
                parameter1.read(index, parameter1Value);
                operation.read(value);
            }
        };
    }

    private Filter() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(final IndexedReadable<P1> parameter1, final Class<O> operationClass) {
        return createFilter(parameter1, operationClass, ConverterRegistry.getDefault());
    }

    public static <P1, P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(final IndexedReadable<P1> parameter1, final IndexedReadable<P2> parameter2, 
            final Class<O> operationClass, final ConverterRegistry converterRegistry) {
        final P1 parameter1Value = parameter1.createValue();
        final P2 parameter2Value = parameter2.createValue();
        final O operation = converterRegistry.convert(parameter1Value, parameter2Value, operationClass);

        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(final int index, final V value) {
                parameter1.read(index, parameter1Value);
                parameter2.read(index, parameter2Value);
                operation.read(value);
            }
        };
    }

    public static <P1, P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(final IndexedReadable<P1> parameter1, final IndexedReadable<P2> parameter2, 
            final Class<O> operationClass) {
        return createFilter(parameter1, parameter2, operationClass, ConverterRegistry.getDefault());
    }

    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(final IndexedReadable<P1> parameter1, final String parameter2, 
            final Class<O> operationClass, ConverterRegistry converterRegistry) {
        final P1 parameter1Value = parameter1.createValue();
        final P1 parameter2Value = parameter1.createValue();

        final StringWritable stringWritable = converterRegistry.convert(parameter2Value, StringWritable.class);
        stringWritable.writeString(parameter2);

        final O operation = converterRegistry.convert(parameter1Value, parameter2Value, operationClass);

        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(final int index, final V value) {
                parameter1.read(index, parameter1Value);
                operation.read(value);
            }
        };
    }

    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(final IndexedReadable<P1> parameter1, final String parameter2, 
            final Class<O> operationClass) {
        return createFilter(parameter1, parameter2, operationClass, ConverterRegistry.getDefault());
    }

    public static <P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(final String parameter1, final IndexedReadable<P2> parameter2, 
            final Class<O> operationClass, ConverterRegistry converterRegistry) {
        final P2 parameter1Value = parameter2.createValue();
        final P2 parameter2Value = parameter2.createValue();

        final StringWritable stringWritable = converterRegistry.convert(parameter1Value, StringWritable.class);
        stringWritable.writeString(parameter1);

        final O operation = converterRegistry.convert(parameter1Value, parameter2Value, operationClass);

        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(final int index, final V value) {
                parameter2.read(index, parameter2Value);
                operation.read(value);
            }
        };
    }

    public static <P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(final String parameter1, final IndexedReadable<P2> parameter2, 
            final Class<O> operationClass) {
        return createFilter(parameter1, parameter2, operationClass, ConverterRegistry.getDefault());
    }
}
