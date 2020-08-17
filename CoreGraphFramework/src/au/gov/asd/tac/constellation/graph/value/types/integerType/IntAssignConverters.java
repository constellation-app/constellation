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
package au.gov.asd.tac.constellation.graph.value.types.integerType;

import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.Assign;

/**
 *
 * @author sirius
 */
public class IntAssignConverters {
    
    public static <P1 extends IntWritable, P2 extends IntReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, Assign.class, new AssignConverter());
    }
    
    public static class AssignConverter implements Biconverter<IntWritable, IntReadable, Assign> {
        @Override
        public Assign convert(IntWritable source1, IntReadable source2) {
            return () -> {
                source1.writeInt(source2.readInt());
            };
        }
    }
}
