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
package au.gov.asd.tac.constellation.graph.value.types.doubles;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.ArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.floats.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.types.floats.FloatWritable;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntReadable;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntWritable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringReadable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringWritable;

/**
 *
 * @author sirius
 */
public class DoubleValue implements Copyable, DoubleReadable, DoubleWritable, FloatReadable, FloatWritable, IntReadable, IntWritable, StringReadable, StringWritable, Comparable<DoubleValue> {

    static {
        ArithmeticConverters.register();
    }

    private double value = 0;

    @Override
    public Object copy() {
        final DoubleValue copy = new DoubleValue();
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
        return (float) value;
    }

    @Override
    public void writeFloat(float value) {
        this.value = value;
    }

    @Override
    public int readInt() {
        return (int) value;
    }

    @Override
    public void writeInt(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            return value == ((DoubleValue) other).value;
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
