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
package au.gov.asd.tac.constellation.graph.value.types.bytes;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.ArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.doubles.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.types.doubles.DoubleWritable;
import au.gov.asd.tac.constellation.graph.value.types.floats.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.types.floats.FloatWritable;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntReadable;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntWritable;
import au.gov.asd.tac.constellation.graph.value.types.longs.LongReadable;
import au.gov.asd.tac.constellation.graph.value.types.longs.LongWritable;
import au.gov.asd.tac.constellation.graph.value.types.shorts.ShortReadable;
import au.gov.asd.tac.constellation.graph.value.types.shorts.ShortWritable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringReadable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringWritable;

/**
 *
 * @author sirius
 */
public class ByteValue implements Copyable, ByteReadable, ByteWritable, ShortReadable, ShortWritable, IntReadable, IntWritable, LongReadable, LongWritable, DoubleReadable, DoubleWritable, FloatReadable, FloatWritable, StringReadable, StringWritable, Comparable<ByteValue> {

    static {
        ArithmeticConverters.register();
    }

    private byte value = 0;

    @Override
    public Object copy() {
        final ByteValue copy = new ByteValue();
        copy.value = value;
        return copy;
    }

    @Override
    public String readString() {
        return String.valueOf(value);
    }

    @Override
    public void writeString(String value) {
        this.value = Byte.valueOf(value);
    }

    @Override
    public byte readByte() {
        return value;
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
        this.value = (byte) value;
    }

    @Override
    public int readInt() {
        return value;
    }

    @Override
    public void writeInt(int value) {
        this.value = (byte) value;
    }

    @Override
    public long readLong() {
        return value;
    }

    @Override
    public void writeLong(long value) {
        this.value = (byte) value;
    }

    @Override
    public double readDouble() {
        return value;
    }

    @Override
    public void writeDouble(double value) {
        this.value = (byte) value;
    }

    @Override
    public float readFloat() {
        return value;
    }

    @Override
    public void writeFloat(float value) {
        this.value = (byte) value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            return value == ((ByteValue) other).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    @Override
    public int compareTo(ByteValue value) {
        return Byte.compare(this.value, value.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
