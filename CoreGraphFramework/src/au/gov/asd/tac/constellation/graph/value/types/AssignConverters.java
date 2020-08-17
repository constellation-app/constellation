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
package au.gov.asd.tac.constellation.graph.value.types;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanAssignConverters;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanValue;
import au.gov.asd.tac.constellation.graph.value.types.byteType.ByteValue;
import au.gov.asd.tac.constellation.graph.value.types.charType.CharValue;
import au.gov.asd.tac.constellation.graph.value.types.doubleType.DoubleAssignConverters;
import au.gov.asd.tac.constellation.graph.value.types.doubleType.DoubleValue;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatAssignConverters;
import au.gov.asd.tac.constellation.graph.value.types.floatType.FloatValue;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntAssignConverters;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntValue;
import au.gov.asd.tac.constellation.graph.value.types.longType.LongAssignConverters;
import au.gov.asd.tac.constellation.graph.value.types.longType.LongValue;
import au.gov.asd.tac.constellation.graph.value.types.shortType.ShortValue;

/**
 *
 * @author sirius
 */
public class AssignConverters {
    
    private static boolean REGISTERED = false;
    
    public static synchronized void register() {
        if (!REGISTERED) {
            register(ConverterRegistry.getDefault());
            REGISTERED = true;
        }
    }
    
    public static void register(ConverterRegistry r) {
        BooleanAssignConverters.register(r, BooleanValue.class, BooleanValue.class);
        
        DoubleAssignConverters.register(r, DoubleValue.class, DoubleValue.class);
        
        DoubleAssignConverters.register(r, DoubleValue.class, FloatValue.class);
        DoubleAssignConverters.register(r, DoubleValue.class, LongValue.class);
        DoubleAssignConverters.register(r, DoubleValue.class, IntValue.class);
        DoubleAssignConverters.register(r, DoubleValue.class, ShortValue.class);
        DoubleAssignConverters.register(r, DoubleValue.class, ByteValue.class);
        DoubleAssignConverters.register(r, DoubleValue.class, CharValue.class);
        
        DoubleAssignConverters.register(r, FloatValue.class, DoubleValue.class);
        DoubleAssignConverters.register(r, LongValue.class, DoubleValue.class);
        DoubleAssignConverters.register(r, IntValue.class, DoubleValue.class);
        DoubleAssignConverters.register(r, ShortValue.class, DoubleValue.class);
        DoubleAssignConverters.register(r, ByteValue.class, DoubleValue.class);
        DoubleAssignConverters.register(r, CharValue.class, DoubleValue.class);
        
        FloatAssignConverters.register(r, FloatValue.class, FloatValue.class);
        
        FloatAssignConverters.register(r, FloatValue.class, LongValue.class);
        FloatAssignConverters.register(r, FloatValue.class, IntValue.class);
        FloatAssignConverters.register(r, FloatValue.class, ShortValue.class);
        FloatAssignConverters.register(r, FloatValue.class, ByteValue.class);
        FloatAssignConverters.register(r, FloatValue.class, CharValue.class);
        
        FloatAssignConverters.register(r, LongValue.class, FloatValue.class);
        FloatAssignConverters.register(r, IntValue.class, FloatValue.class);
        FloatAssignConverters.register(r, ShortValue.class, FloatValue.class);
        FloatAssignConverters.register(r, ByteValue.class, FloatValue.class);
        FloatAssignConverters.register(r, CharValue.class, FloatValue.class);
        
        LongAssignConverters.register(r, LongValue.class, LongValue.class);
        
        LongAssignConverters.register(r, LongValue.class, IntValue.class);
        LongAssignConverters.register(r, LongValue.class, ShortValue.class);
        LongAssignConverters.register(r, LongValue.class, ByteValue.class);
        LongAssignConverters.register(r, LongValue.class, CharValue.class);
        
        LongAssignConverters.register(r, IntValue.class, LongValue.class);
        LongAssignConverters.register(r, ShortValue.class, LongValue.class);
        LongAssignConverters.register(r, ByteValue.class, LongValue.class);
        LongAssignConverters.register(r, CharValue.class, LongValue.class);
        
        IntAssignConverters.register(r, IntValue.class, IntValue.class);
        
        IntAssignConverters.register(r, IntValue.class, ShortValue.class);
        IntAssignConverters.register(r, IntValue.class, ByteValue.class);
        IntAssignConverters.register(r, IntValue.class, CharValue.class);
        
        IntAssignConverters.register(r, ShortValue.class, IntValue.class);
        IntAssignConverters.register(r, ByteValue.class,  IntValue.class);
        IntAssignConverters.register(r, CharValue.class, IntValue.class);
        
        IntAssignConverters.register(r, ShortValue.class, ShortValue.class);
        
        IntAssignConverters.register(r, ShortValue.class, ByteValue.class);
        IntAssignConverters.register(r, ShortValue.class, CharValue.class);
        
        IntAssignConverters.register(r, ByteValue.class,  ShortValue.class);
        IntAssignConverters.register(r, CharValue.class, ShortValue.class);
        
        
        IntAssignConverters.register(r, ByteValue.class, ByteValue.class);
        
        IntAssignConverters.register(r, ByteValue.class, CharValue.class);
        
        IntAssignConverters.register(r, CharValue.class, ByteValue.class);
        
        
        IntAssignConverters.register(r, CharValue.class, CharValue.class);
    }
}
