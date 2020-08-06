/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.doubleType;

import java.util.Arrays;
import au.gov.asd.tac.constellation.graph.value.ValueStore;

/**
 *
 * @author darren
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
