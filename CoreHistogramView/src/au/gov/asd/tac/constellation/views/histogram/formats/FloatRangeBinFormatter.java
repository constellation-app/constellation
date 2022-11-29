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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.FloatBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin float values into ranges of a
 * specified size.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class FloatRangeBinFormatter extends BinFormatter {

    public static final String ZERO_PARAMETER_ID = PluginParameter.buildId(FloatRangeBinFormatter.class, "zero");
    public static final String BUCKET_PARAMETER_ID = PluginParameter.buildId(FloatRangeBinFormatter.class, "bucket");

    public FloatRangeBinFormatter() {
        super("Bin Into Buckets", 0);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof FloatBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        float zero = parameters.getParameters().get(ZERO_PARAMETER_ID).getFloatValue();
        float bucketSize = parameters.getParameters().get(BUCKET_PARAMETER_ID).getFloatValue();
        return new FloatRangeBin((FloatBin) bin, zero, bucketSize);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<FloatParameterValue> zeroParameter = FloatParameterType.build(ZERO_PARAMETER_ID);
        zeroParameter.setName("Zero Point");
        zeroParameter.setFloatValue(0.0F);
        params.addParameter(zeroParameter);

        final PluginParameter<FloatParameterValue> bucketParameter = FloatParameterType.build(BUCKET_PARAMETER_ID);
        bucketParameter.setName("Bucket Size");
        bucketParameter.setFloatValue(10.0F);
        params.addParameter(bucketParameter);

        return params;
    }

    private class FloatRangeBin extends FloatBin {

        private final FloatBin bin;
        private final float zero;
        private final float bucketSize;
        private float end;

        public FloatRangeBin(FloatBin bin, float zero, float bucketSize) {
            this.bin = bin;
            this.zero = zero;
            this.bucketSize = bucketSize;
        }

        @Override
        public void prepareForPresentation() {
            label = key + " <= x < " + end;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            int bucketNumber = (int) Math.floor((bin.getKey() - zero) / bucketSize);
            key = bucketNumber * bucketSize + zero;
            end = key + bucketSize;
        }

        @Override
        public Bin create() {
            return new FloatRangeBin(bin, zero, bucketSize);
        }
    }
}
