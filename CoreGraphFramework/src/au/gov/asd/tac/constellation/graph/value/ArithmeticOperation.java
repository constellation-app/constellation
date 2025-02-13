/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.value.constants.StringConstant;
import au.gov.asd.tac.constellation.graph.value.readables.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.readables.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.readables.StringReadable;

/**
 *
 * @author sirius
 */
public interface ArithmeticOperation {

    double execute(final double p1, final double p2);

    float execute(final float p1, final float p2);

    long execute(final long p1, final long p2);

    int execute(final int p1, final int p2);

    default void register(final OperatorRegistry registry) {
        registry.register(DoubleReadable.class, DoubleReadable.class, DoubleReadable.class, (p1, p2)
                -> () -> execute(p1.readDouble(), p2.readDouble()));
        registry.register(FloatReadable.class, FloatReadable.class, FloatReadable.class, (p1, p2) -> () -> execute(p1.readFloat(), p2.readFloat()));
        registry.register(LongReadable.class, LongReadable.class, LongReadable.class, (p1, p2) -> () -> execute(p1.readLong(), p2.readLong()));
        registry.register(IntReadable.class, IntReadable.class, IntReadable.class, (p1, p2) -> () -> execute(p1.readInt(), p2.readInt()));

        registry.register(DoubleReadable.class, StringReadable.class, DoubleReadable.class, (p1, p2)
                -> () -> execute(p1.readDouble(), Double.parseDouble(p2.readString())));
        registry.register(FloatReadable.class, StringReadable.class, FloatReadable.class, (p1, p2)
                -> () -> execute(p1.readFloat(), Float.parseFloat(p2.readString())));
        registry.register(LongReadable.class, StringReadable.class, LongReadable.class, (p1, p2)
                -> () -> execute(p1.readLong(), Long.parseLong(p2.readString())));
        registry.register(IntReadable.class, StringReadable.class, IntReadable.class, (p1, p2)
                -> () -> execute(p1.readInt(), Integer.parseInt(p2.readString())));

        registry.register(StringReadable.class, DoubleReadable.class, DoubleReadable.class, (p1, p2)
                -> () -> execute(Double.parseDouble(p1.readString()), p2.readDouble()));
        registry.register(StringReadable.class, FloatReadable.class, FloatReadable.class, (p1, p2)
                -> () -> execute(Float.parseFloat(p1.readString()), p2.readFloat()));
        registry.register(StringReadable.class, LongReadable.class, LongReadable.class, (p1, p2)
                -> () -> execute(Long.parseLong(p1.readString()), p2.readLong()));
        registry.register(StringReadable.class, IntReadable.class, IntReadable.class, (p1, p2)
                -> () -> execute(Integer.parseInt(p1.readString()), p2.readInt()));

        registry.register(DoubleReadable.class, StringConstant.class, DoubleReadable.class, (p1, p2) -> {
            final double p2Double = Double.parseDouble(p2.readString());
            return () -> execute(p1.readDouble(), p2Double);
        });
        registry.register(FloatReadable.class, StringConstant.class, FloatReadable.class, (p1, p2) -> {
            final float p2Float = Float.parseFloat(p2.readString());
            return () -> execute(p1.readFloat(), p2Float);
        });
        registry.register(LongReadable.class, StringConstant.class, LongReadable.class, (p1, p2) -> {
            final long p2Long = Long.parseLong(p2.readString());
            return () -> execute(p1.readLong(), p2Long);
        });
        registry.register(IntReadable.class, StringConstant.class, IntReadable.class, (p1, p2) -> {
            final int p2Int = Integer.parseInt(p2.readString());
            return () -> execute(p1.readInt(), p2Int);
        });

        registry.register(StringConstant.class, DoubleReadable.class, DoubleReadable.class, (p1, p2) -> {
            final double p1Double = Double.parseDouble(p1.readString());
            return () -> execute(p1Double, p2.readDouble());
        });
        registry.register(StringConstant.class, FloatReadable.class, FloatReadable.class, (p1, p2) -> {
            final float p1Float = Float.parseFloat(p1.readString());
            return () -> execute(p1Float, p2.readFloat());
        });
        registry.register(StringConstant.class, LongReadable.class, LongReadable.class, (p1, p2) -> {
            final long p1Long = Long.parseLong(p1.readString());
            return () -> execute(p1Long, p2.readLong());
        });
        registry.register(StringConstant.class, IntReadable.class, IntReadable.class, (p1, p2) -> {
            final int p1Int = Integer.parseInt(p1.readString());
            return () -> execute(p1Int, p2.readInt());
        });
    }
}
