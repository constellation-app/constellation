/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.objectType;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;

/**
 *
 * @author darren
 */
public class ObjectValue<V> implements Copyable, ObjectReadable<V>, ObjectWritable<V> {

    private V value = null;
    
    @Override
    public Object copy() {
        var copy = new ObjectValue<V>();
        copy.value = value;
        return copy;
    }
    
    @Override
    public V readObject() {
        return value;
    }
    
    @Override
    public void writeObject(V value) {
        this.value = value;
    }
}
