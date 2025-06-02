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
package au.gov.asd.tac.constellation.graph.value.stores;

import au.gov.asd.tac.constellation.graph.value.Store;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.writables.LongWritable;
import java.util.Arrays;

/**
 *
 * @author sirius
 */
public class LongStore implements Store {

    private long[] values;

    public LongStore(final int capacity) {
        values = new long[capacity];
    }

    public LongStore(final long... values) {
        this.values = values;
    }

    public void setCapacity(final int capacity) {
        values = Arrays.copyOf(values, capacity);
    }

    @Override
    public Object createVariable(final IntReadable indexReadable) {
        return new Value(indexReadable);
    }

    private class Value implements LongReadable, LongWritable {

        private final IntReadable indexReadable;

        public Value(final IntReadable indexReadable) {
            this.indexReadable = indexReadable;
        }

        @Override
        public long readLong() {
            return values[indexReadable.readInt()];
        }

        @Override
        public void writeLong(final long value) {
            values[indexReadable.readInt()] = value;
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
