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
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.AttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import java.time.ZonedDateTime;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin elements by the hour of day,
 * ignoring all other information in the datetime.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class DateTimeHourFormatter extends BinFormatter {

    public DateTimeHourFormatter() {
        super("Hour Of Day", 2);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof AttributeBin && ((AttributeBin) bin).getAttributeType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        return new DateTimeHourBin((AttributeBin) bin);
    }

    private static final int NULL_HOUR = -1;

    private class DateTimeHourBin extends IntBin {

        private final AttributeBin bin;

        public DateTimeHourBin(AttributeBin bin) {
            this.bin = bin;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            key = bin.getKeyAsObject() == null ? NULL_HOUR : ((ZonedDateTime) bin.getKeyAsObject()).getHour();
        }

        @Override
        public void prepareForPresentation() {
            label = bin.getKeyAsObject() == null ? null : String.format("%02d00h", key);
        }

        @Override
        public Bin create() {
            return new DateTimeHourBin(bin);
        }
    }
}
