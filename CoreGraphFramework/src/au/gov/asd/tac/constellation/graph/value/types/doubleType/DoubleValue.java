/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.doubleType;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.ArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatWritable;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntReadable;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringReadable;

/**
 *
 * @author darren
 */
public class DoubleValue implements Copyable, DoubleReadable, DoubleWritable, FloatReadable, FloatWritable, IntReadable, IntWritable, StringReadable, StringWritable, Comparable<DoubleValue> {

    static {
        ArithmeticConverters.register();
    }
    
    private double value = 0;
    
    @Override
    public Object copy() {
        var copy = new DoubleValue();
        copy.value = value;
        return copy;
    }
    
    @Override
    public String readString() {
        return String.valueOf(value);
    }
    
    @Override
    public void writeString(String value) {
        this.value = Double.valueOf(value);
    }
    
    @Override
    public double readDouble() {
        return value;
    }
    
    @Override
    public void writeDouble(double value) {
        this.value = value;
    }
    
    @Override
    public float readFloat() {
        return (float)value;
    }
    
    @Override
    public void writeFloat(float value) {
        this.value = value;
    }
    
    @Override
    public int readInt() {
        return (int)value;
    }
    
    @Override
    public void writeInt(int value) {
        this.value = value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof DoubleValue) {
            return value == ((DoubleValue)other).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
    
    @Override
    public int compareTo(DoubleValue value) {
        return Double.compare(this.value, value.value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
