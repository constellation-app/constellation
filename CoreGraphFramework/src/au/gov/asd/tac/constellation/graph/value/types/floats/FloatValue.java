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
package au.gov.asd.tac.constellation.graph.value.types.floats;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.ArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.doubles.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.types.doubles.DoubleWritable;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntReadable;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntWritable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringReadable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringWritable;

/**
 *
 * @author sirius
 */
public class FloatValue implements Copyable, FloatReadable, FloatWritable, DoubleReadable, DoubleWritable, IntReadable, IntWritable, StringReadable, StringWritable, Comparable<FloatValue> {

    static {
        ArithmeticConverters.register();
    }

    private float value = 0;

    @Override
    public Object copy() {
        final FloatValue copy = new FloatValue();
        copy.value = value;
        return copy;
    }

    @Override
    public String readString() {
        return String.valueOf(value);
    }

    @Override
    public void writeString(final String value) {
        this.value = Float.parseFloat(value);
    }

    @Override
    public float readFloat() {
        return value;
    }

    @Override
    public void writeFloat(final float value) {
        this.value = value;
    }

    @Override
    public double readDouble() {
        return value;
    }

    @Override
    public void writeDouble(final double value) {
        this.value = (float) value;
    }

    @Override
    public int readInt() {
        return (int) value;
    }

    @Override
    public void writeInt(final int value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object other) {
        return other != null && this.getClass() == other.getClass() && value == ((FloatValue) other).value;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    @Override
    public int compareTo(final FloatValue value) {
        return Float.compare(this.value, value.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
