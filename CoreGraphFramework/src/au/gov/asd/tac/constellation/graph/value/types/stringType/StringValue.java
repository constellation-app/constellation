/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.stringType;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.converters.Copyable;

/**
 *
 * @author darren
 */
public class StringValue implements Copyable, StringReadable, StringWritable {

    static {
        final ConverterRegistry r = ConverterRegistry.getDefault();
        StringConverters.register(r, StringValue.class, StringValue.class);
    }
    
    private String value = null;
    
    @Override
    public Object copy() {
        var copy = new StringValue();
        copy.value = value;
        return copy;
    }
    
    @Override
    public String readString() {
        return value;
    }
    
    @Override
    public void writeString(String value) {
        this.value = value;
    }
}
