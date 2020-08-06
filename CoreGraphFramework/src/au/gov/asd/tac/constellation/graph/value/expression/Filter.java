/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.expression;

import au.gov.asd.tac.constellation.graph.value.IndexedReadable;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.Readable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringWritable;

/**
 *
 * @author darren
 */
public class Filter {
    
    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, Class<O> operationClass, ConverterRegistry converterRegistry) {
        final P1 parameter1Value = parameter1.createValue();
        final var operation = converterRegistry.convert(parameter1Value, operationClass);

        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(int index, V value) {
                parameter1.read(index, parameter1Value);
                operation.read(value);
            }
        };
    }
    
    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, Class<O> operationClass) {
        return createFilter(parameter1, operationClass, ConverterRegistry.getDefault());
    }
    
    public static <P1, P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, IndexedReadable<P2> parameter2, Class<O> operationClass, ConverterRegistry converterRegistry) {
        final P1 parameter1Value = parameter1.createValue();
        final P2 parameter2Value = parameter2.createValue();
        final var operation = converterRegistry.convert(parameter1Value, parameter2Value, operationClass);

        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(int index, V value) {
                parameter1.read(index, parameter1Value);
                parameter2.read(index, parameter2Value);
                operation.read(value);
            }
        };
    }
    
    public static <P1, P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, IndexedReadable<P2> parameter2, Class<O> operationClass) {
        return createFilter(parameter1, parameter2, operationClass, ConverterRegistry.getDefault());
    }
    
    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, String parameter2, Class<O> operationClass, ConverterRegistry converterRegistry) {
        final var parameter1Value = parameter1.createValue();
        final var parameter2Value = parameter1.createValue();
        
        final var stringWritable = converterRegistry.convert(parameter2Value, StringWritable.class);
        stringWritable.writeString(parameter2);
        
        final var operation = converterRegistry.convert(parameter1Value, parameter2Value, operationClass);
        
        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(int index, V value) {
                parameter1.read(index, parameter1Value);
                operation.read(value);
            }
        };
    }
    
    public static <P1, V, O extends Readable<V>> IndexedReadable<V> createFilter(IndexedReadable<P1> parameter1, String parameter2, Class<O> operationClass) {
        return createFilter(parameter1, parameter2, operationClass, ConverterRegistry.getDefault());
    }
    
    public static <P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(String parameter1, IndexedReadable<P2> parameter2, Class<O> operationClass, ConverterRegistry converterRegistry) {
        final var parameter1Value = parameter2.createValue();
        final var parameter2Value = parameter2.createValue();
        
        final var stringWritable = converterRegistry.convert(parameter1Value, StringWritable.class);
        stringWritable.writeString(parameter1);
        
        final var operation = converterRegistry.convert(parameter1Value, parameter2Value, operationClass);
        
        return new IndexedReadable<V>() {
            @Override
            public V createValue() {
                return operation.createValue();
            }

            @Override
            public void read(int index, V value) {
                parameter2.read(index, parameter2Value);
                operation.read(value);
            }
        };
    }
    
    public static <P2, V, O extends Readable<V>> IndexedReadable<V> createFilter(String parameter1, IndexedReadable<P2> parameter2, Class<O> operationClass) {
        return createFilter(parameter1, parameter2, operationClass, ConverterRegistry.getDefault());
    }
}
