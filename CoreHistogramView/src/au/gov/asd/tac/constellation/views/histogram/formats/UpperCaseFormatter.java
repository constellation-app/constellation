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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin String values by their uppercase
 * equivalents.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class UpperCaseFormatter extends BinFormatter {

    public UpperCaseFormatter() {
        super("Upper Case", 1);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof StringBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        return new UpperCaseFormatBin((StringBin) bin);
    }

    private class UpperCaseFormatBin extends StringBin {

        private final StringBin bin;

        public UpperCaseFormatBin(StringBin bin) {
            this.bin = bin;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            key = bin.getKey() == null ? null : bin.getKey().toUpperCase();
        }

        @Override
        public Bin create() {
            return new UpperCaseFormatBin(bin);
        }
    }
}
