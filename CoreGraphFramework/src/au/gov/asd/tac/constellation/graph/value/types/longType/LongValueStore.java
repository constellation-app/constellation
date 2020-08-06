/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.longType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
 */
public class LongValueStore implements ValueStore<LongValue> {

    private static final long[] EMPTY_VALUES = new long[0];
    
    private long[] values;
    
    public LongValueStore() {
        values = EMPTY_VALUES;
    }
    
    public LongValueStore(long[] values) {
        this.values = values;
    }
    
    @Override
    public LongValue createValue() {
        return new LongValue();
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
    public void write(int id, LongValue value) {
        values[id] = value.readLong();
    }

    @Override
    public void read(int id, LongValue value) {
        value.writeLong(values[id]);
    }
}
