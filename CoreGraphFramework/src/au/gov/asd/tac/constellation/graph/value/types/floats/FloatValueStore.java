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
package au.gov.asd.tac.constellation.graph.value.types.floats;

import au.gov.asd.tac.constellation.graph.value.ValueStore;
import java.util.Arrays;

/**
 *
 * @author sirius
 */
public class FloatValueStore implements ValueStore<FloatValue> {

    private static final float[] EMPTY_VALUES = new float[0];

    private float[] values;

    public FloatValueStore() {
        values = EMPTY_VALUES;
    }

    public FloatValueStore(float[] values) {
        this.values = values;
    }

    @Override
    public FloatValue createValue() {
        return new FloatValue();
    }

    @Override
    public int getCapacity() {
        return values.length;
    }

    @Override
    public void setCapacity(int capacity) {
        values = Arrays.copyOf(values, capacity);
    }

    @Override
    public void write(int id, FloatValue value) {
        values[id] = value.readFloat();
    }

    @Override
    public void read(int id, FloatValue value) {
        value.writeFloat(values[id]);
    }
}
