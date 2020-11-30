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
package au.gov.asd.tac.constellation.graph.value.types.booleans;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author sirius
 */
public class BooleanValueStore implements ValueStore<BooleanValue> {

    private boolean[] values = new boolean[0];
    
    public BooleanValueStore() {}
    
    public BooleanValueStore(boolean[] values) {
        this.values = values;
    }
    
    @Override
    public BooleanValue createValue() {
        return new BooleanValue();
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
    public void write(int id, BooleanValue value) {
        values[id] = value.readBoolean();
    }

    @Override
    public void read(int id, BooleanValue value) {
        value.writeBoolean(values[id]);
    }
}
