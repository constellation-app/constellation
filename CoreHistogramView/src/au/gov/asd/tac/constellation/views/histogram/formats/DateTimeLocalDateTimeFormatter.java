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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.AttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.ObjectBin;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin elements by date only, ignoring
 * the time component.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class DateTimeLocalDateTimeFormatter extends BinFormatter {

    public DateTimeLocalDateTimeFormatter() {
        super("Local Date-Time", 1);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof AttributeBin attributeBin && attributeBin.getAttributeType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        return new DateTimeDateBin((AttributeBin) bin);
    }

    private class DateTimeDateBin extends ObjectBin {

        private final AttributeBin bin;
        private final DateTimeFormatter formatter = TemporalFormatting.LOCAL_DATE_TIME_FORMATTER;

        public DateTimeDateBin(AttributeBin bin) {
            this.bin = bin;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            key = bin.getKeyAsObject() == null ? null : ((ZonedDateTime) bin.getKeyAsObject()).toLocalDateTime();
        }

        @Override
        public void prepareForPresentation() {
            label = key == null ? null : formatter.format((LocalDateTime) key);
        }

        @Override
        public Bin create() {
            return new DateTimeDateBin(bin);
        }
    }
}
