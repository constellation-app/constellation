/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.integerType;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.ArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.doubleType.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.types.doubleType.DoubleWritable;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatWritable;
import au.gov.asd.tac.constellation.graph.value.types.longType.LongReadable;
import au.gov.asd.tac.constellation.graph.value.types.longType.LongWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringReadable;

/**
 *
 * @author darren
 */
public class IntValue implements Copyable, IntReadable, IntWritable, LongReadable, LongWritable, DoubleReadable, DoubleWritable, FloatReadable, FloatWritable, StringReadable, StringWritable, Comparable<IntValue> {

    static {
        ArithmeticConverters.register();
    }
    
    private int value = 0;
    
    @Override
    public Object copy() {
        var copy = new IntValue();
        copy.value = value;
        return copy;
    }
    
    @Override
    public String readString() {
        return String.valueOf(value);
    }
    
    @Override
    public void writeString(String value) {
        this.value = Integer.valueOf(value);
    }
    
    @Override
    public int readInt() {
        return value;
    }
    
    @Override
    public void writeInt(int value) {
        this.value = value;
    }
    
    @Override
    public long readLong() {
        return value;
    }
    
    @Override
    public void writeLong(long value) {
        this.value = (int)value;
    }
    
    @Override
    public double readDouble() {
        return value;
    }
    
    @Override
    public void writeDouble(double value) {
        this.value = (int)value;
    }
    
    @Override
    public float readFloat() {
        return value;
    }
    
    @Override
    public void writeFloat(float value) {
        this.value = (int)value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof IntValue) {
            return value == ((IntValue)other).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
    
    @Override
    public int compareTo(IntValue value) {
        return Integer.compare(this.value, value.value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
