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
package au.gov.asd.tac.constellation.graph.value.types;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.types.bytes.ByteValue;
import au.gov.asd.tac.constellation.graph.value.types.chars.CharValue;
import au.gov.asd.tac.constellation.graph.value.types.doubles.DoubleArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.doubles.DoubleValue;
import au.gov.asd.tac.constellation.graph.value.types.floats.FloatArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.floats.FloatValue;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntValue;
import au.gov.asd.tac.constellation.graph.value.types.longs.LongArithmeticConverters;
import au.gov.asd.tac.constellation.graph.value.types.longs.LongValue;
import au.gov.asd.tac.constellation.graph.value.types.shorts.ShortValue;

/**
 *
 * @author sirius
 */
public class ArithmeticConverters {

    private static boolean registered = false;

    private ArithmeticConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static synchronized void register() {
        if (!registered) {
            register(ConverterRegistry.getDefault());
            registered = true;
        }
    }

    public static void register(final ConverterRegistry r) {
        DoubleArithmeticConverters.register(r, DoubleValue.class);
        DoubleArithmeticConverters.register(r, DoubleValue.class, DoubleValue.class);

        DoubleArithmeticConverters.register(r, DoubleValue.class, FloatValue.class);
        DoubleArithmeticConverters.register(r, DoubleValue.class, LongValue.class);
        DoubleArithmeticConverters.register(r, DoubleValue.class, IntValue.class);
        DoubleArithmeticConverters.register(r, DoubleValue.class, ShortValue.class);
        DoubleArithmeticConverters.register(r, DoubleValue.class, ByteValue.class);
        DoubleArithmeticConverters.register(r, DoubleValue.class, CharValue.class);

        DoubleArithmeticConverters.register(r, FloatValue.class, DoubleValue.class);
        DoubleArithmeticConverters.register(r, LongValue.class, DoubleValue.class);
        DoubleArithmeticConverters.register(r, IntValue.class, DoubleValue.class);
        DoubleArithmeticConverters.register(r, ShortValue.class, DoubleValue.class);
        DoubleArithmeticConverters.register(r, ByteValue.class, DoubleValue.class);
        DoubleArithmeticConverters.register(r, CharValue.class, DoubleValue.class);

        FloatArithmeticConverters.register(r, FloatValue.class);
        FloatArithmeticConverters.register(r, FloatValue.class, FloatValue.class);

        FloatArithmeticConverters.register(r, FloatValue.class, LongValue.class);
        FloatArithmeticConverters.register(r, FloatValue.class, IntValue.class);
        FloatArithmeticConverters.register(r, FloatValue.class, ShortValue.class);
        FloatArithmeticConverters.register(r, FloatValue.class, ByteValue.class);
        FloatArithmeticConverters.register(r, FloatValue.class, CharValue.class);

        FloatArithmeticConverters.register(r, LongValue.class, FloatValue.class);
        FloatArithmeticConverters.register(r, IntValue.class, FloatValue.class);
        FloatArithmeticConverters.register(r, ShortValue.class, FloatValue.class);
        FloatArithmeticConverters.register(r, ByteValue.class, FloatValue.class);
        FloatArithmeticConverters.register(r, CharValue.class, FloatValue.class);

        LongArithmeticConverters.register(r, LongValue.class);
        LongArithmeticConverters.register(r, LongValue.class, LongValue.class);

        LongArithmeticConverters.register(r, LongValue.class, IntValue.class);
        LongArithmeticConverters.register(r, LongValue.class, ShortValue.class);
        LongArithmeticConverters.register(r, LongValue.class, ByteValue.class);
        LongArithmeticConverters.register(r, LongValue.class, CharValue.class);

        LongArithmeticConverters.register(r, IntValue.class, LongValue.class);
        LongArithmeticConverters.register(r, ShortValue.class, LongValue.class);
        LongArithmeticConverters.register(r, ByteValue.class, LongValue.class);
        LongArithmeticConverters.register(r, CharValue.class, LongValue.class);

        IntArithmeticConverters.register(r, IntValue.class);
        IntArithmeticConverters.register(r, IntValue.class, IntValue.class);

        IntArithmeticConverters.register(r, IntValue.class, ShortValue.class);
        IntArithmeticConverters.register(r, IntValue.class, ByteValue.class);
        IntArithmeticConverters.register(r, IntValue.class, CharValue.class);

        IntArithmeticConverters.register(r, ShortValue.class, IntValue.class);
        IntArithmeticConverters.register(r, ByteValue.class, IntValue.class);
        IntArithmeticConverters.register(r, CharValue.class, IntValue.class);

        IntArithmeticConverters.register(r, ShortValue.class);
        IntArithmeticConverters.register(r, ShortValue.class, ShortValue.class);

        IntArithmeticConverters.register(r, ShortValue.class, ByteValue.class);
        IntArithmeticConverters.register(r, ShortValue.class, CharValue.class);

        IntArithmeticConverters.register(r, ByteValue.class, ShortValue.class);
        IntArithmeticConverters.register(r, CharValue.class, ShortValue.class);

        IntArithmeticConverters.register(r, ByteValue.class);
        IntArithmeticConverters.register(r, ByteValue.class, ByteValue.class);

        IntArithmeticConverters.register(r, ByteValue.class, CharValue.class);

        IntArithmeticConverters.register(r, CharValue.class, ByteValue.class);

        IntArithmeticConverters.register(r, CharValue.class);
        IntArithmeticConverters.register(r, CharValue.class, CharValue.class);
    }
}
