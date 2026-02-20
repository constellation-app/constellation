/*
 * Copyright 2010-2025 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.graph.value.converter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class ConverterRegistry {

    private static final ConverterRegistry DEFAULT = new ConverterRegistry();

    public static ConverterRegistry getDefault() {
        return DEFAULT;
    }

    private final Map<Class<?>, Map<Class<?>, Converter<?, ?>>> converters = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Map<Class<?>, Biconverter<?, ?, ?>>>> biConverters = new HashMap<>();

    public <S, D> void register(final Class<S> sourceClass, final Class<D> destinationClass, final Converter<? super S, ? extends D> converter) {
        Map<Class<?>, Converter<?, ?>> sourceConverters = converters.get(sourceClass);
        if (sourceConverters == null) {
            sourceConverters = new HashMap<>();
            converters.put(sourceClass, sourceConverters);
        }
        sourceConverters.put(destinationClass, converter);
    }

    public <S1, S2, D> void register(final Class<S1> source1Class, final Class<S2> source2Class, final Class<D> destinationClass, final Biconverter<? super S1, ? super S2, ? extends D> converter) {
        Map<Class<?>, Map<Class<?>, Biconverter<?, ?, ?>>> source1Converters = biConverters.get(source1Class);
        if (source1Converters == null) {
            source1Converters = new HashMap<>();
            biConverters.put(source1Class, source1Converters);
        }
        Map<Class<?>, Biconverter<?, ?, ?>> source2Converters = source1Converters.get(source2Class);
        if (source2Converters == null) {
            source2Converters = new HashMap<>();
            source1Converters.put(source2Class, source2Converters);
        }
        source2Converters.put(destinationClass, converter);
    }

    public <D> D convert(final Object sourceObject, final Class<? extends D> destinationClass) {
        final Map<Class<?>, Converter<?, ?>> sourceConverters = converters.get(sourceObject.getClass());
        if (sourceConverters != null) {
            final Converter<Object, D> converter = (Converter<Object, D>) sourceConverters.get(destinationClass);
            if (converter != null) {
                return converter.convert(sourceObject);
            }
        }

        if (destinationClass.isAssignableFrom(sourceObject.getClass())) {
            return (D) sourceObject;
        }

        return null;
    }

    public <D> D convert(final Object source1Object, final Object source2Object, final Class<? extends D> destinationClass) {
        final Map<Class<?>, Map<Class<?>, Biconverter<?, ?, ?>>> source1Converters = biConverters.get(source1Object.getClass());
        if (source1Converters == null) {
            return null;
        }
        final Map<Class<?>, Biconverter<?, ?, ?>> source2Converters = source1Converters.get(source2Object.getClass());
        if (source2Converters == null) {
            return null;
        }
        final Biconverter<Object, Object, D> converter = (Biconverter<Object, Object, D>) source2Converters.get(destinationClass);
        return converter == null ? null : converter.convert(source1Object, source2Object);
    }
}
