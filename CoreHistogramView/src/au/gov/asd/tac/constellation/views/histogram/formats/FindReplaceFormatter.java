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
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.histogram.Bin;
import au.gov.asd.tac.constellation.views.histogram.bins.StringBin;
import java.util.regex.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * A BinFormatter that allows the user to bin by attribute values after they
 * have undergone a a find/replace substitution.
 *
 * @author sirius
 */
@ServiceProvider(service = BinFormatter.class)
public class FindReplaceFormatter extends BinFormatter {

    public static final String FIND_PARAMETER_ID = PluginParameter.buildId(FindReplaceFormatter.class, "find");
    public static final String REPLACE_PARAMETER_ID = PluginParameter.buildId(FindReplaceFormatter.class, "replace");

    public FindReplaceFormatter() {
        super("Find & Replace", 10);
    }

    @Override
    public PluginParameters createParameters() {
        PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> findParameter = StringParameterType.build(FIND_PARAMETER_ID);
        findParameter.setName("Find");
        findParameter.setStringValue("");
        params.addParameter(findParameter);

        final PluginParameter<StringParameterValue> replaceParameter = StringParameterType.build(REPLACE_PARAMETER_ID);
        replaceParameter.setName("Replace");
        replaceParameter.setStringValue("");
        params.addParameter(replaceParameter);

        return params;
    }

    @Override
    public boolean appliesToBin(Bin bin) {
        return bin instanceof StringBin;
    }

    @Override
    public Bin createBin(final GraphReadMethods graph, final int attribute, final PluginParameters parameters, final Bin bin) {
        String find = parameters.getParameters().get(FIND_PARAMETER_ID).getStringValue();
        String replace = parameters.getParameters().get(REPLACE_PARAMETER_ID).getStringValue();

        Pattern pattern = Pattern.compile(find);
        return new FindReplaceFormatBin((StringBin) bin, pattern, replace);
    }

    private class FindReplaceFormatBin extends StringBin {

        private final StringBin bin;
        private final Pattern pattern;
        private final String replace;

        public FindReplaceFormatBin(StringBin bin, Pattern pattern, String replace) {
            this.bin = bin;
            this.pattern = pattern;
            this.replace = replace;
        }

        @Override
        public void setKey(GraphReadMethods graph, int attribute, int element) {
            bin.setKey(graph, attribute, element);

            String k = bin.getKey();

            if (k == null) {
                key = null;
            } else {
                key = pattern.matcher(k).replaceAll(replace);
            }
        }

        @Override
        public Bin create() {
            return new FindReplaceFormatBin(bin, pattern, replace);
        }
    }
}
