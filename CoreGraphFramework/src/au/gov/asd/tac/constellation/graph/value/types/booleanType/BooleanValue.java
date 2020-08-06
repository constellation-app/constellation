/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.booleanType;

import au.gov.asd.tac.constellation.graph.value.converters.Copyable;
import au.gov.asd.tac.constellation.graph.value.types.LogicConverters;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringWritable;
import au.gov.asd.tac.constellation.graph.value.types.stringType.StringReadable;

/**
 *
 * @author darren
 */
public class BooleanValue implements Copyable, BooleanReadable, BooleanWritable, StringReadable, StringWritable, Comparable<BooleanValue> {

    static {
        LogicConverters.register();
    }
    
    private boolean value = false;

    @Override
    public Object copy() {
        var copy = new BooleanValue();
        copy.value = value;
        return copy;
    }
    
    @Override
    public String readString() {
        return String.valueOf(value);
    }
    
    @Override
    public void writeString(String value) {
        this.value = Boolean.valueOf(value);
    }
    
    @Override
    public boolean readBoolean() {
        return value;
    }
    
    @Override
    public void writeBoolean(boolean value) {
        this.value = value;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof BooleanValue) {
            return value == ((BooleanValue)other).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
    
    @Override
    public int compareTo(BooleanValue value) {
        return Boolean.compare(this.value, value.value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
