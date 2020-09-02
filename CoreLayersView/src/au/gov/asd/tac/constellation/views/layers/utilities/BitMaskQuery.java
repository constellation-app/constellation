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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.value.Access;
import au.gov.asd.tac.constellation.graph.value.readables.BooleanReadable;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;

/**
 *
 * @author sirius
 */
public class BitMaskQuery {
    private final Query query;
    private final int bitIndex;
    private final long mask;
    private BooleanReadable result;

    public BitMaskQuery(Query query, int bitIndex) {
        this.query = query;
        this.bitIndex = bitIndex;
        this.mask = 0xFFFFFFFFFFFFFFFFL ^ (1L << bitIndex);
    }
    
    public boolean update(GraphReadMethods graph, IntReadable index) {
        if(query.requiresUpdate(graph)) {
            final Object compiledExpresssion = query.compile(graph, index);
            this.result = Access.getDefault().getRegistry(BooleanReadable.class).convert(compiledExpresssion);
            return true;
        } else {
            return false;
        }
    }
    
    public long updateBitMask(long original) {
        if (result.readBoolean()) {
            return original | (1L << bitIndex);
        } else {
            return original & mask;
        }
    }
}
