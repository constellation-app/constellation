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
package au.gov.asd.tac.constellation.views.histogram.bins.neighbouraggregates;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.DoubleBin;

/**
 * A bin that holds double values representing the average of all neighbour
 * values adjacent to a single vertex.
 *
 * @author sirius
 */
public class VertexAverageNeighbourDoubleAttributeBin extends DoubleBin {

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        final int transactionCount = graph.getVertexNeighbourCount(element);
        if (transactionCount == 0) {
            key = 0;
        } else {
            double sum = 0;
            for (int t = 0; t < transactionCount; t++) {
                final int transaction = graph.getVertexNeighbour(element, t);
                sum += graph.getDoubleValue(attribute, transaction);
            }
            key = sum / transactionCount;
        }
    }

    @Override
    public Bin create() {
        return new VertexAverageNeighbourDoubleAttributeBin();
    }
}
