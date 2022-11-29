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
package au.gov.asd.tac.constellation.graph.value.types.objects;

import au.gov.asd.tac.constellation.graph.value.ValueStore;
import java.util.Arrays;

/**
 *
 * @author sirius
 */
public class ObjectValueStore<V> implements ValueStore<ObjectValue<V>> {

    private static final Object[] EMPTY_VALUES = new Object[0];

    private Object[] values = EMPTY_VALUES;

    public ObjectValueStore() {
        this.values = EMPTY_VALUES;
    }

    public ObjectValueStore(V[] values) {
        this.values = values;
    }

    @Override
    public ObjectValue<V> createValue() {
        return new ObjectValue<>();
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
    public void write(int id, ObjectValue<V> value) {
        values[id] = value.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(int id, ObjectValue<V> value) {
        value.writeObject((V) values[id]);
    }
}
