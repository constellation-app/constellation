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

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin integer values into logarithmic
 * buckets.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class IntegerLogarithmicRangeBinFormatter extends BinFormatter {

    public static final String BASE_PARAMETER_ID = PluginParameter.buildId(IntegerLogarithmicRangeBinFormatter.class, "base");

    public IntegerLogarithmicRangeBinFormatter() {
        super("Bin Into Logarithmic Buckets", 0);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof IntBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        final int base = parameters != null ? parameters.getParameters().get(BASE_PARAMETER_ID).getIntegerValue() : createParameters().getParameters().get(BASE_PARAMETER_ID).getIntegerValue();

        return new IntRangeBin((IntBin) bin, base);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> baseParameter = IntegerParameterType.build(BASE_PARAMETER_ID);
        baseParameter.setName("Logairthmic Base");
        baseParameter.setIntegerValue(10);
        params.addParameter(baseParameter);

        return params;
    }

    private class IntRangeBin extends IntBin {

        private final IntBin bin;
        private final int base;
        private int end;

        public IntRangeBin(IntBin bin, int base) {
            this.bin = bin;
            this.base = base;
        }

        @Override
        public void prepareForPresentation() {
            if (key == 0) {
                label = "0";
            } else {
                label = key + " to " + end;
            }
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            if (bin.getKey() == 0) {
                key = 0;
                return;
            }
            int sign = (bin.getKey() < 0 ? -1 : 1);
            key = (int) (sign * Math.pow(base, Math.floor(Math.log10(Math.abs(bin.getKey())) / Math.log10(base))));
            end = (key * base) - sign;
        }

        @Override
        public Bin create() {
            return new IntRangeBin(bin, base);
        }
    }
}
