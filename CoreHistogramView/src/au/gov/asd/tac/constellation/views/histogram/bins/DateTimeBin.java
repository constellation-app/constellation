/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A bin that holds datetime values.
 *
 * @author sirius
 */
public class DateTimeBin extends ObjectBin {

    protected DateTimeFormatter formatter;

    public DateTimeBin() {
    }

    public DateTimeBin(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void init(GraphReadMethods graph, final int attributeId) {
        formatter = TemporalFormatting.ZONED_DATE_TIME_FORMATTER;
    }

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        key = graph.getObjectValue(attribute, element);
    }

    @Override
    public void prepareForPresentation() {
        label = key == null ? null : formatter.format((ZonedDateTime) key);
    }

    @Override
    public Bin create() {
        return new DateTimeBin(formatter);
    }
}
