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
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.DateTimeBin;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A bin that holds datetime values representing the maximum of all neighbour
 * values adjacent to a single vertex.
 *
 * @author sirius
 */
public class VertexMaxNeighbourDatetimeAttributeBin extends DateTimeBin {

    public VertexMaxNeighbourDatetimeAttributeBin() {
    }

    public VertexMaxNeighbourDatetimeAttributeBin(DateTimeFormatter formatter) {
        super(formatter);
    }

    private static final ZonedDateTime MIN_DATE_TIME = TemporalFormatting.zonedDateTimeFromLong(Integer.MIN_VALUE);

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        ZonedDateTime currentMax = MIN_DATE_TIME;
        final int transactionCount = graph.getVertexNeighbourCount(element);
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = graph.getVertexNeighbour(element, t);
            final ZonedDateTime zdt = graph.getObjectValue(attribute, transaction);
            if (zdt.isAfter(currentMax)) {
                currentMax = zdt;
            }
        }
        key = currentMax.equals(MIN_DATE_TIME) ? null : currentMax;
    }

    @Override
    public Bin create() {
        return new VertexMaxNeighbourDatetimeAttributeBin(formatter);
    }
}
