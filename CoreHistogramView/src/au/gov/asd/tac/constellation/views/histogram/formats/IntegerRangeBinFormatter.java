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
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.IntBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin integer values into buckets with a
 * specified range.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class IntegerRangeBinFormatter extends BinFormatter {

    public static final String ZERO_PARAMETER_ID = PluginParameter.buildId(IntegerRangeBinFormatter.class, "zero");
    public static final String BUCKET_PARAMETER_ID = PluginParameter.buildId(IntegerRangeBinFormatter.class, "bucket");

    public IntegerRangeBinFormatter() {
        super("Bin Into Buckets", 0);
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof IntBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        final int zero = parameters != null ? parameters.getParameters().get(ZERO_PARAMETER_ID).getIntegerValue() : createParameters().getParameters().get(ZERO_PARAMETER_ID).getIntegerValue();
        final int bucketSize = parameters != null ? parameters.getParameters().get(BUCKET_PARAMETER_ID).getIntegerValue() : createParameters().getParameters().get(BUCKET_PARAMETER_ID).getIntegerValue();

        return new IntRangeBin((IntBin) bin, zero, bucketSize);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<IntegerParameterValue> zeroParameter = IntegerParameterType.build(ZERO_PARAMETER_ID);
        zeroParameter.setName("Zero Point");
        zeroParameter.setIntegerValue(0);
        params.addParameter(zeroParameter);

        final PluginParameter<IntegerParameterValue> bucketParameter = IntegerParameterType.build(BUCKET_PARAMETER_ID);
        bucketParameter.setName("Bucket Size");
        bucketParameter.setIntegerValue(10);
        params.addParameter(bucketParameter);

        return params;
    }

    private class IntRangeBin extends IntBin {

        private final IntBin bin;
        private final int zero;
        private final int bucketSize;
        private int end;

        public IntRangeBin(IntBin bin, int zero, int bucketSize) {
            this.bin = bin;
            this.zero = zero;
            this.bucketSize = bucketSize;
        }

        @Override
        public void prepareForPresentation() {
            label = key + " - " + end;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);
            int error = (bin.getKey() - zero) % bucketSize;
            if (error < 0) {
                error += bucketSize;
            }
            key = bin.getKey() - error;
            end = key + bucketSize - 1;
        }

        @Override
        public Bin create() {
            return new IntRangeBin(bin, zero, bucketSize);
        }
    }
}
