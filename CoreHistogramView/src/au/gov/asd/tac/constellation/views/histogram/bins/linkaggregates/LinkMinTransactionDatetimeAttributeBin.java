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
package au.gov.asd.tac.constellation.views.histogram.bins.linkaggregates;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.DateTimeBin;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A bin that holds datetime values representing the minimum of all transaction
 * values under a single link.
 *
 * @author sirius
 */
public class LinkMinTransactionDatetimeAttributeBin extends DateTimeBin {

    public LinkMinTransactionDatetimeAttributeBin() {
    }

    public LinkMinTransactionDatetimeAttributeBin(DateTimeFormatter formatter) {
        super(formatter);
    }

    private static final ZonedDateTime MAX_DATE_TIME = TemporalFormatting.zonedDateTimeFromLong(Integer.MAX_VALUE);

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        ZonedDateTime currentMin = MAX_DATE_TIME;
        final int transactionCount = graph.getLinkTransactionCount(element);
        for (int t = 0; t < transactionCount; t++) {
            final int transaction = graph.getLinkTransaction(element, t);
            final ZonedDateTime zdt = graph.getObjectValue(attribute, transaction);
            if (zdt != null && zdt.isBefore(currentMin)) {
                currentMin = zdt;
            }
        }
        key = currentMin.equals(MAX_DATE_TIME) ? null : currentMin;
    }

    @Override
    public Bin create() {
        return new LinkMinTransactionDatetimeAttributeBin(formatter);
    }
}
