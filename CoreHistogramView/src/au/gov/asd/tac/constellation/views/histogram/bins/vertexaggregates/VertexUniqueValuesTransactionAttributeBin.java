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
package au.gov.asd.tac.constellation.views.histogram.bins.vertexaggregates;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import java.util.HashSet;
import java.util.Set;

/**
 * A bin that holds integer values representing the number of unique values for
 * all transaction values adjacent to a single vertex.
 *
 * @author sirius
 */
public class VertexUniqueValuesTransactionAttributeBin extends IntBin {

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        final Set<Object> uniqueValues = new HashSet<>();
        final int transactionCount = graph.getVertexTransactionCount(element);
        for (int t = 0; t < transactionCount; t++) {
            final int tranaction = graph.getVertexTransaction(element, t);
            uniqueValues.add(graph.getObjectValue(attribute, tranaction));
        }
        key = uniqueValues.size();
    }

    @Override
    public Bin create() {
        return new VertexUniqueValuesTransactionAttributeBin();
    }
}
