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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.RawAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.AttributeBin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Histogram formatter for displaying the identifier of a RawData.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = BinFormatter.class)
public class RawIdentifierFormatter extends BinFormatter {

    public RawIdentifierFormatter() {
        super("Raw Identifier", 1);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof AttributeBin && ((AttributeBin) bin).getAttributeType().equals(RawAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        return new RawIdentifierFormatter.RawIdentifierFormatBin((AttributeBin) bin);
    }

    /**
     * A Histogram bin for storing RawData identifiers.
     */
    private class RawIdentifierFormatBin extends StringBin {

        private final AttributeBin bin;

        public RawIdentifierFormatBin(AttributeBin bin) {
            this.bin = bin;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            key = bin.getKeyAsObject() instanceof RawData ? ((RawData) bin.getKeyAsObject()).getRawIdentifier() : null;
        }

        @Override
        public void prepareForPresentation() {
            label = key == null ? null : key;
        }

        @Override
        public Bin create() {
            return new RawIdentifierFormatBin(bin);
        }
    }
}
