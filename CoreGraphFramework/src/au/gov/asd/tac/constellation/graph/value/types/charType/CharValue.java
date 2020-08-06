/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.charType;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.ArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.byteType.ByteReadable;
import au.gov.asd.tac.constellation.graph.value.types.byteType.ByteWritable;
import au.gov.asd.tac.constellation.graph.value.types.doubleType.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.types.doubleType.DoubleWritable;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatWritable;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntReadable;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntWritable;
import au.gov.asd.tac.constellation.graph.value.types.longType.LongReadable;
import au.gov.asd.tac.constellation.graph.value.types.longType.LongWritable;
import au.gov.asd.tac.constellation.graph.value.types.shortType.ShortReadable;
import au.gov.asd.tac.constellation.graph.value.types.shortType.ShortWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringReadable;

/**
 *
 * @author darren
 */
public class CharValue implements Copyable, CharReadable, CharWritable, ByteReadable, ByteWritable, ShortReadable, ShortWritable, IntReadable, IntWritable, LongReadable, LongWritable, DoubleReadable, DoubleWritable, FloatReadable, FloatWritable, StringReadable, StringWritable, Comparable<CharValue> {

    static {
        ArithmeticConverters.register();
    }
    
    private char value = 0;
    
    @Override
    public Object copy() {
        var copy = new CharValue();
        copy.value = value;
        return copy;
    }
    
    @Override
    public String readString() {
        return String.valueOf(value);
    }
    
    @Override
    public void writeString(String value) {
        if (value.length() != 1) {
            throw new IllegalArgumentException("Attempt to convert a string with length != 1 to character: " + value);
        }
        this.value = value.charAt(0);
    }
    
    @Override
    public char readChar() {
        return value;
    }
    
    @Override
    public void writeChar(char value) {
        this.value = value;
    }
    
    @Override
    public byte readByte() {
        return (byte)value;
    }
    
    @Override
    public void writeByte(byte value) {
        this.value = (char)value;
    }
    
    @Override
    public short readShort() {
        return (short)value;
    }
    
    @Override
    public void writeShort(short value) {
        this.value = (char)value;
    }
    
    @Override
    public int readInt() {
        return value;
    }
    
    @Override
    public void writeInt(int value) {
        this.value = (char)value;
    }
    
    @Override
    public long readLong() {
        return value;
    }
    
    @Override
    public void writeLong(long value) {
        this.value = (char)value;
    }
    
    @Override
    public double readDouble() {
        return value;
    }
    
    @Override
    public void writeDouble(double value) {
        this.value = (char)value;
    }
    
    @Override
    public float readFloat() {
        return value;
    }
    
    @Override
    public void writeFloat(float value) {
        this.value = (char)value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof CharValue) {
            return value == ((CharValue)other).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Character.hashCode(value);
    }
    
    @Override
    public int compareTo(CharValue value) {
        return Character.compare(this.value, value.value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
