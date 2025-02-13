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
package au.gov.asd.tac.constellation.graph.value.types.booleans;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.LogicConverters;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringReadable;
import au.gov.asd.tac.constellation.graph.value.types.strings.StringWritable;

/**
 *
 * @author sirius
 */
public class BooleanValue implements Copyable, BooleanReadable, BooleanWritable, StringReadable, StringWritable, Comparable<BooleanValue> {

    static {
        LogicConverters.register();
    }

    private boolean value = false;

    @Override
    public Object copy() {
        final BooleanValue copy = new BooleanValue();
        copy.value = value;
        return copy;
    }

    @Override
    public String readString() {
        return String.valueOf(value);
    }

    @Override
    public void writeString(final String value) {
        this.value = Boolean.parseBoolean(value);
    }

    @Override
    public boolean readBoolean() {
        return value;
    }

    @Override
    public void writeBoolean(final boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object other) {
        return other != null && this.getClass() == other.getClass() && value == ((BooleanValue) other).value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public int compareTo(final BooleanValue value) {
        return Boolean.compare(this.value, value.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
