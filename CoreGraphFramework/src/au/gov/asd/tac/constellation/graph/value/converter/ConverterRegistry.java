/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.converter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author darren
 */
public class ConverterRegistry {
    
    private static final ConverterRegistry DEFAULT = new ConverterRegistry();
    
    public static ConverterRegistry getDefault() {
        return DEFAULT;
    }
    
    private final Map<Class<?>, Map<Class<?>, Converter<?, ?>>> converters = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Map<Class<?>, Biconverter<?, ?, ?>>>> biConverters = new HashMap<>();
    
    public <S, D> void register(Class<S> sourceClass, Class<D> destinationClass, Converter<? super S, ? extends D> converter) {
        var sourceConverters = converters.get(sourceClass);
        if (sourceConverters == null) {
            sourceConverters = new HashMap<>();
            converters.put(sourceClass, sourceConverters);
        }
        sourceConverters.put(destinationClass, converter);
    }
    
    public <S1, S2, D> void register(Class<S1> source1Class, Class<S2> source2Class, Class<D> destinationClass, Biconverter<? super S1, ? super S2, ? extends D> converter) {
        var source1Converters = biConverters.get(source1Class);
        if (source1Converters == null) {
            source1Converters = new HashMap<>();
            biConverters.put(source1Class, source1Converters);
        }
        var source2Converters = source1Converters.get(source2Class);
        if (source2Converters == null) {
            source2Converters = new HashMap<>();
            source1Converters.put(source2Class, source2Converters);
        }
        source2Converters.put(destinationClass, converter);
    }
    
    public <D> D convert(Object sourceObject, Class<? extends D> destinationClass) {
        var sourceConverters = converters.get(sourceObject.getClass());
        if (sourceConverters != null) {
            var converter = (Converter<Object, D>)sourceConverters.get(destinationClass);
            if (converter != null) {
                return converter.convert(sourceObject);
            }
        }
    
        if (destinationClass.isAssignableFrom(sourceObject.getClass())) {
            return (D)sourceObject;
        }
        
        return null;
    }
    
    public <D> D convert(Object source1Object, Object source2Object, Class<? extends D> destinationClass) {
        var source1Converters = biConverters.get(source1Object.getClass());
        if (source1Converters == null) {
            return null;
        }
        var source2Converters = source1Converters.get(source2Object.getClass());
        if (source2Converters == null) {
            return null;
        } 
        var converter = (Biconverter<Object, Object, D>)source2Converters.get(destinationClass);
        return converter == null ? null : converter.convert(source1Object, source2Object);
    }
}
