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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.AttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin elements by a custom date format.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class DateTimeCustomFormatter extends BinFormatter {

    public static final String FORMAT_PARAMETER_ID = PluginParameter.buildId(DateTimeCustomFormatter.class, "format");

    public DateTimeCustomFormatter() {
        super("Custom Format", Integer.MAX_VALUE);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> formatParameter = StringParameterType.build(FORMAT_PARAMETER_ID);
        formatParameter.setName("Format");
        formatParameter.setStringValue("yyyy-MM-dd hh:mm:ss");
        params.addParameter(formatParameter);

        return params;
    }

    @Override
    public boolean appliesToBin(final Bin bin) {
        return bin instanceof AttributeBin && ((AttributeBin) bin).getAttributeType().equals(ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, Bin bin) {
        return new DateTimeCustomFormatBin((AttributeBin) bin, DateTimeFormatter.ofPattern(parameters.getParameters().get(FORMAT_PARAMETER_ID).getStringValue()));
    }

    private class DateTimeCustomFormatBin extends StringBin {

        private final AttributeBin bin;
        private final DateTimeFormatter formattter;

        public DateTimeCustomFormatBin(final AttributeBin bin, final DateTimeFormatter formatter) {
            this.bin = bin;
            this.formattter = formatter;
        }

        @Override
        public void setKey(final GraphReadMethods graph, final int attribute, final int element) {
            bin.setKey(graph, attribute, element);
            key = bin.getKeyAsObject() == null ? null : ((ZonedDateTime) bin.getKeyAsObject()).format(formattter);
        }

        @Override
        public Bin create() {
            return new DateTimeCustomFormatBin(bin, formattter);
        }
    }
}
