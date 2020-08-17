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
package au.gov.asd.tac.constellation.graph.value.types.shortType;

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
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringReadable;

/**
 *
 * @author sirius
 */
public class ShortValue implements Copyable, ShortReadable, ShortWritable, IntReadable, IntWritable, LongReadable, LongWritable, ByteReadable, ByteWritable, DoubleReadable, DoubleWritable, FloatReadable, FloatWritable, StringReadable, StringWritable, Comparable<ShortValue> {

    static {
        ArithmeticConverters.register();
    }
    
    private short value = 0;
    
    @Override
    public Object copy() {
        var copy = new ShortValue();
        copy.value = value;
        return copy;
    }
    
    @Override
    public String readString() {
        return String.valueOf(value);
    }
    
    @Override
    public void writeString(String value) {
        this.value = Short.valueOf(value);
    }
    
    @Override
    public byte readByte() {
        return (byte)value;
    }
    
    @Override
    public void writeByte(byte value) {
        this.value = value;
    }
    
    @Override
    public short readShort() {
        return value;
    }
    
    @Override
    public void writeShort(short value) {
        this.value = value;
    }
    
    @Override
    public int readInt() {
        return value;
    }
    
    @Override
    public void writeInt(int value) {
        this.value = (short)value;
    }
    
    @Override
    public long readLong() {
        return value;
    }
    
    @Override
    public void writeLong(long value) {
        this.value = (short)value;
    }
    
    @Override
    public double readDouble() {
        return value;
    }
    
    @Override
    public void writeDouble(double value) {
        this.value = (short)value;
    }
    
    @Override
    public float readFloat() {
        return value;
    }
    
    @Override
    public void writeFloat(float value) {
        this.value = (short)value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof ShortValue) {
            return value == ((ShortValue)other).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }
    
    @Override
    public int compareTo(ShortValue value) {
        return Short.compare(this.value, value.value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
