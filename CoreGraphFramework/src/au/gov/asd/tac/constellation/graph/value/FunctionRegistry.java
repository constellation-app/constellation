/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author sirius
 */
public class FunctionRegistry {
    
    private static final FunctionRegistry DEFAULT = new FunctionRegistry();
    
    public static FunctionRegistry getDefault() {
        return DEFAULT;
    }
    
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Function<?, ?>>> functions = new HashMap<>();
    private final Map<Class<?>, Map<Class<?>, Map<Class<?>, BiFunction<?, ?, ?>>>> biFunctions = new HashMap<>();
    
    public <P> void registerImplementation(Class<? extends P> implementationClass, Class<P> interfaceClass) {
        implementations.put(implementationClass, interfaceClass);
    }
    
    public <P, R> void register(Class<? extends P> parameterClass, Class<R> resultClass, Function<P, ? extends R> function) {
        var parameterEntries = functions.get(parameterClass);
        if (parameterEntries == null) {
            parameterEntries = new HashMap<>();
            functions.put(parameterClass, parameterEntries);
        }
        parameterEntries.put(resultClass, function);
    }
    
    public <P, R> void register(List<Class<? extends P>> parameterClasses, Class<R> resultClass, Function<P, ? extends R> function) {
        parameterClasses.forEach(parameterClass -> {
            register(parameterClass, resultClass, function);
        });
    }
    
    public <P1, P2, R> void register(Class<? extends P1> parameter1Class, Class<? extends P2> parameter2Class, Class<R> resultClass, BiFunction<P1, P2, ? extends R> biFunction) {
        var parameter1Entries = biFunctions.get(parameter1Class);
        if (parameter1Entries == null) {
            parameter1Entries = new HashMap<>();
            biFunctions.put(parameter1Class, parameter1Entries);
        }
        var parameter2Entries = parameter1Entries.get(parameter2Class);
        if (parameter2Entries == null) {
            parameter2Entries = new HashMap<>();
            parameter1Entries.put(parameter2Class, parameter2Entries);
        }
        parameter2Entries.put(resultClass, biFunction);
    }
    
    public <P1, P2, R> void register(List<Class<? extends P1>> parameter1Classes, List<Class<? extends P2>> parameter2Classes, Class<R> resultClass, BiFunction<P1, P2, ? extends R> biFunction) {
        parameter1Classes.forEach(parameter1Class -> {
            parameter2Classes.forEach(parameter2Class -> {
                register(parameter1Class, parameter2Class, resultClass, biFunction);
            });
        });
    }
    
    public <P, R> R create(P parameter, Class<R> resultClass) {
        final var interfaceClass = implementations.getOrDefault(parameter.getClass(), parameter.getClass());
        
        final var parameterEntries = functions.get(interfaceClass);
        if (parameterEntries != null) {
            final var function = (Function<P, R>)parameterEntries.get(resultClass);
            if (function != null) {
                return function.apply(parameter);
            }
        }

        if (resultClass.isAssignableFrom(interfaceClass)) {
            return (R)parameter;
        }
        
        return null;
    }
    
    public <P1, P2, R> R create(P1 parameter1, P2 parameter2, Class<R> resultClass) {
        final var interface1Class = implementations.getOrDefault(parameter1.getClass(), parameter1.getClass());
        
        final var parameter1Entries = biFunctions.get(interface1Class);
        if (parameter1Entries != null) {
            final var interface2Class = implementations.getOrDefault(parameter2.getClass(), parameter2.getClass());
            final var parameter2Entries = parameter1Entries.get(interface2Class);
            if (parameter2Entries != null) {
                final var biFunction = (BiFunction<P1, P2, R>)parameter2Entries.get(resultClass);
                if (biFunction != null) {
                    return biFunction.apply(parameter1, parameter2);
                }
            }
        }
        
        return null;
    }
}
