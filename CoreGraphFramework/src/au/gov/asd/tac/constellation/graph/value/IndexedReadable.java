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

import au.gov.asd.tac.constellation.graph.value.types.integerType.IntValue;

/**
 *
 * @author sirius
 */
public interface IndexedReadable<V> extends ValueProvider<V> {
    
    void read(int id, V value);
    
    default Readable<V> createReadable(IntValue indexValue) {
        return new Readable<V>() {
            @Override
            public V createValue() {
                return IndexedReadable.this.createValue();
            }

            @Override
            public void read(V value) {
                IndexedReadable.this.read(indexValue.readInt(), value);
            }
        };
    }
}
