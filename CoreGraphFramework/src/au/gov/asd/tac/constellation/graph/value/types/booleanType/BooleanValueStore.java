/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.booleanType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
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
