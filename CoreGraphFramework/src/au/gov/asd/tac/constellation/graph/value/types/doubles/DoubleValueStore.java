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
package au.gov.asd.tac.constellation.graph.value.types.doubles;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author sirius
 */
public class DoubleValueStore implements ValueStore<DoubleValue> {

    private static final double[] EMPTY_VALUES = new double[0];
    
    private double[] values;
    
    public DoubleValueStore() {
        values = EMPTY_VALUES;
    }
    
    public DoubleValueStore(double[] values) {
        this.values = values;
    }
    
    @Override
    public DoubleValue createValue() {
        return new DoubleValue();
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
    public void write(int id, DoubleValue value) {
        values[id] = value.readDouble();
    }

    @Override
    public void read(int id, DoubleValue value) {
        value.writeDouble(values[id]);
    }
}
