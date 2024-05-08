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
package au.gov.asd.tac.constellation.views.histogram.formats;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.FloatBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin float values into logarithmic
 * buckets.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class FloatRangeLogarithmicBinFormatter extends BinFormatter {

    public static final String BASE_PARAMETER_ID = PluginParameter.buildId(FloatRangeLogarithmicBinFormatter.class, "base");

    public FloatRangeLogarithmicBinFormatter() {
        super("Bin Into Logarithmic Buckets", 0);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof FloatBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        float base = parameters.getParameters().get(BASE_PARAMETER_ID).getFloatValue();
        return new FloatRangeBin((FloatBin) bin, base);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<FloatParameterValue> base = FloatParameterType.build(BASE_PARAMETER_ID);
        base.setName("Base");
        base.setFloatValue(10.0F);
        params.addParameter(base);

        return params;
    }

    private class FloatRangeBin extends FloatBin {

        private final FloatBin bin;
        private final float base;
        private float end;

        public FloatRangeBin(FloatBin bin, float base) {
            this.bin = bin;
            this.base = base;
        }

        @Override
        public void prepareForPresentation() {
            if (key == 0) {
                label = "0";
            } else {
                label = key + " <= x < " + end;
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
            double keyD = (sign * Math.pow(base, Math.floor(Math.log10(Math.abs(bin.getKey())) / Math.log10(base))));
            key = (float) keyD;
            end = (float) (keyD * base);
        }

        @Override
        public Bin create() {
            return new FloatRangeBin(bin, base);
        }
    }
}
