/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.BooleanBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin elements whether or not the
 * attribute value is the default value for that attribute.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class DefaultValueFormatter extends BinFormatter {

    public DefaultValueFormatter() {
        super("Has Default Value", 10);
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        String defaultValue = attribute == Graph.NOT_FOUND ? "" : String.valueOf(graph.getAttributeDefaultValue(attribute));
        return new DefaultValueFormatBin(defaultValue);
    }

    private class DefaultValueFormatBin extends BooleanBin {

        private final String defaultValue;

        public DefaultValueFormatBin(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public void prepareForPresentation() {
            label = key ? ("Other") : ("Default (" + defaultValue + ")");
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            key = attribute == Graph.NOT_FOUND || !graph.isDefaultValue(attribute, element);
        }

        @Override
        public Bin create() {
            return new DefaultValueFormatBin(defaultValue);
        }
    }
}
