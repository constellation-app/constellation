/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.byteType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
 */
public class ByteValueStore implements ValueStore<ByteValue> {

    private static final int[] EMPTY_VALUES = new int[0];
    
    private int[] values;
    
    public ByteValueStore() {
        values = EMPTY_VALUES;
    }
    
    public ByteValueStore(int[] values) {
        this.values = values;
    }
    
    @Override
    public ByteValue createValue() {
        return new ByteValue();
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
    public void write(int id, ByteValue value) {
        values[id] = value.readInt();
    }

    @Override
    public void read(int id, ByteValue value) {
        value.writeInt(values[id]);
    }
}
