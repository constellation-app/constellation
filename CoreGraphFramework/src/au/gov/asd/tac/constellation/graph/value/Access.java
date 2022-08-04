/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.value.readables.ByteReadable;
import au.gov.asd.tac.constellation.graph.value.readables.CharReadable;
import au.gov.asd.tac.constellation.graph.value.readables.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.readables.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.readables.ShortReadable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class Access {

    private static final Access DEFAULT = new Access();

    public static Access getDefault() {
        return DEFAULT;
    }

    static {
        Access.getDefault().getRegistry(CharReadable.class)
                .register(ByteReadable.class, s -> () -> (char) s.readShort())
                .register(ShortReadable.class, s -> () -> (char) s.readShort())
                .register(IntReadable.class, s -> () -> (char) s.readInt())
                .register(LongReadable.class, s -> () -> (char) s.readLong())
                .register(FloatReadable.class, s -> () -> (char) s.readFloat())
                .register(DoubleReadable.class, s -> () -> (char) s.readDouble());

        Access.getDefault().getRegistry(ByteReadable.class)
                .register(ShortReadable.class, s -> () -> (byte) s.readShort())
                .register(IntReadable.class, s -> () -> (byte) s.readInt())
                .register(LongReadable.class, s -> () -> (byte) s.readLong())
                .register(FloatReadable.class, s -> () -> (byte) s.readFloat())
                .register(DoubleReadable.class, s -> () -> (byte) s.readDouble());

        Access.getDefault().getRegistry(ShortReadable.class)
                .register(IntReadable.class, s -> () -> (byte) s.readInt())
                .register(LongReadable.class, s -> () -> (byte) s.readLong())
                .register(FloatReadable.class, s -> () -> (byte) s.readFloat())
                .register(DoubleReadable.class, s -> () -> (byte) s.readDouble());

        Access.getDefault().getRegistry(IntReadable.class)
                .register(LongReadable.class, s -> () -> (byte) s.readLong())
                .register(FloatReadable.class, s -> () -> (byte) s.readFloat())
                .register(DoubleReadable.class, s -> () -> (byte) s.readDouble());

        Access.getDefault().getRegistry(LongReadable.class)
                .register(FloatReadable.class, s -> () -> (byte) s.readFloat())
                .register(DoubleReadable.class, s -> () -> (byte) s.readDouble());

        Access.getDefault().getRegistry(FloatReadable.class)
                .register(DoubleReadable.class, s -> () -> (byte) s.readDouble());
    }

    private final Map<Class<?>, AccessRegistry<?>> registries = new HashMap<>();

    public final <D> AccessRegistry<D> getRegistry(Class<D> destinationClass) {
        synchronized (registries) {
            AccessRegistry<D> registry = (AccessRegistry<D>) registries.get(destinationClass);
            if (registry == null) {
                registry = new AccessRegistry<>(destinationClass);
                registries.put(destinationClass, registry);
            }
            return registry;
        }
    }
}
