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

import au.gov.asd.tac.constellation.graph.value.constants.StringConstant;
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;
import au.gov.asd.tac.constellation.graph.value.readables.DoubleReadable;
import au.gov.asd.tac.constellation.graph.value.readables.FloatReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.readables.LongReadable;
import au.gov.asd.tac.constellation.graph.value.readables.StringReadable;

/**
 *
 * @author sirius
 */
public interface StringOperation {
    boolean execute(String p1, String p2);
    
    default void register(OperatorRegistry registry) {
        
        registry.register(StringReadable.class, StringReadable.class, BooleanReadable.class, (p1, p2) -> { 
            return () -> execute(p1.readString(), p2.readString()); 
        });
        
        registry.register(StringConstant.class, StringConstant.class, BooleanReadable.class, (p1, p2) -> { 
            final var p1String = p1.readString();
            final var p2String = p2.readString();
            final var result = execute(p1String, p2String);
            return () -> result; 
        });
    }
}
