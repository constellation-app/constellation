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
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin String values by a substring of
 * their original values.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class SubstringFormatter extends BinFormatter {

    public static final String START_PARAMETER_ID = PluginParameter.buildId(SubstringFormatter.class, "start");
    public static final String END_PARAMETER_ID = PluginParameter.buildId(SubstringFormatter.class, "end");

    public SubstringFormatter() {
        super("Substring", 0);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> startParameter = StringParameterType.build(START_PARAMETER_ID);
        startParameter.setName("Start");
        startParameter.setStringValue("");
        params.addParameter(startParameter);

        final PluginParameter<StringParameterValue> endParameter = StringParameterType.build(END_PARAMETER_ID);
        endParameter.setName("End");
        endParameter.setStringValue("");
        params.addParameter(endParameter);

        return params;
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof StringBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        String startString = parameters.getParameters().get(START_PARAMETER_ID).getStringValue();
        int start;
        try {
            start = Integer.parseInt(startString);
        } catch (NumberFormatException ex) {
            start = 0;
        }

        String endString = parameters.getParameters().get(END_PARAMETER_ID).getStringValue();
        int end;
        try {
            end = Integer.parseInt(endString);
        } catch (NumberFormatException ex) {
            end = Integer.MAX_VALUE;
        }

        return new SubstringFormatBin((StringBin) bin, start, end);
    }

    private class SubstringFormatBin extends StringBin {

        private final StringBin bin;
        private final int start;
        private final int end;

        public SubstringFormatBin(StringBin bin, int start, int end) {
            this.bin = bin;
            this.start = start;
            this.end = end;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);

            String k = bin.getKey();

            if (k == null) {
                key = null;
            } else {
                final int l = k.length();

                int s = start;
                if (s < 0) {
                    s += l;
                }
                if (s > l) {
                    s = l;
                } else if (s < 0) {
                    s = 0;
                } else {
                    // Do nothing
                }

                int e = end;
                if (e < 0) {
                    e += l;
                }
                if (e > l) {
                    e = l;
                }
                if (e < s) {
                    e = s;
                }

                key = k.substring(s, e);
            }
        }

        @Override
        public Bin create() {
            return new SubstringFormatBin(bin, start, end);
        }
    }
}
