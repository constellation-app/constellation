/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.objectType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
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
        return new ObjectValue<V>();
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
        value.writeObject((V)values[id]);
    }
}
