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
package au.gov.asd.tac.constellation.plugins.importexport.translator;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import org.openide.util.lookup.ServiceProvider;

/**
 * A SubstringAttributeTranslator translates an input field value by replacing
 * it with a specified subsequence of the input field value.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeTranslator.class)
public class SubstringAttributeTranslator extends AttributeTranslator {

    public static final String FIRST_PARAMETER_ID = PluginParameter.buildId(SubstringAttributeTranslator.class, "first");
    public static final String LAST_PARAMETER_ID = PluginParameter.buildId(SubstringAttributeTranslator.class, "last");

    public SubstringAttributeTranslator() {
        super("Substring", 100);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<IntegerParameterValue> startParam = IntegerParameterType.build(FIRST_PARAMETER_ID);
        startParam.setName("First");
        startParam.setDescription("The position of the first character to include");
        startParam.setIntegerValue(0);
        parameters.addParameter(startParam);

        final PluginParameter<IntegerParameterValue> stopParam = IntegerParameterType.build(LAST_PARAMETER_ID);
        stopParam.setName("Last");
        stopParam.setDescription("The position of the last character to include");
        stopParam.setIntegerValue(1000);
        parameters.addParameter(stopParam);

        return parameters;
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {

        if (value == null) {
            return null;
        } else {

            int first = parameters.getParameters().get(FIRST_PARAMETER_ID).getIntegerValue();
            if (first < 0) {
                first = 0;
            }
            if (first >= value.length()) {
                return "";
            }

            int last = parameters.getParameters().get(LAST_PARAMETER_ID).getIntegerValue();
            if (last < first) {
                return "";
            }
            if (last >= value.length() - 1) {
                return value.substring(first);
            }

            return value.substring(first, last + 1);
        }
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        final int first = parameters.getParameters().get(FIRST_PARAMETER_ID).getIntegerValue();
        final int last = parameters.getParameters().get(LAST_PARAMETER_ID).getIntegerValue();

        return String.format("%d,%d", first, last);
    }

    @Override
    public void setParameterValues(final PluginParameters parameters, final String values) {
        final String[] vals = values.split(",");
        parameters.getParameters().get(FIRST_PARAMETER_ID).setIntegerValue(Integer.parseInt(vals[0]));
        parameters.getParameters().get(LAST_PARAMETER_ID).setIntegerValue(Integer.parseInt(vals[1]));
    }
}
