/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.floatType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
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
