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
package au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import java.util.HashSet;
import java.util.Set;

/**
 * A bin that holds integer values representing the number of unique values for
 * all neighbour values adjacent to a single vertex.
 *
 * @author sirius
 */
public class VertexUniqueValuesNeighbourAttributeBin extends IntBin {

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        final Set<Object> uniqueValues = new HashSet<>();
        final int neighbourCount = graph.getVertexNeighbourCount(element);
        for (int n = 0; n < neighbourCount; n++) {
            final int neighbour = graph.getVertexNeighbour(element, n);
            uniqueValues.add(graph.getObjectValue(attribute, neighbour));
        }
        key = uniqueValues.size();
    }

    @Override
    public Bin create() {
        return new VertexUniqueValuesNeighbourAttributeBin();
    }
}
