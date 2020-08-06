/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.stringType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
 */
public class StringValueStore implements ValueStore<StringValue> {

    private static final String[] EMPTY_VALUES = new String[0];
    
    private String[] values;
    
    public StringValueStore() {
        values = EMPTY_VALUES;
    }
    
    public StringValueStore(String[] values) {
        this.values = values;
    }
    
    @Override
    public StringValue createValue() {
        return new StringValue();
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
    public void write(int id, StringValue value) {
        values[id] = value.readString();
    }

    @Override
    public void read(int id, StringValue value) {
        value.writeString(values[id]);
    }
}
