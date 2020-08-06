/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.charType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
 */
public class CharValueStore implements ValueStore<CharValue> {

    private static final char[] EMPTY_VALUES = new char[0];
    
    private char[] values;
    
    public CharValueStore() {
        values = EMPTY_VALUES;
    }
    
    public CharValueStore(char[] values) {
        this.values = values;
    }
    
    @Override
    public CharValue createValue() {
        return new CharValue();
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
    public void write(int id, CharValue value) {
        values[id] = value.readChar();
    }

    @Override
    public void read(int id, CharValue value) {
        value.writeChar(values[id]);
    }
}
