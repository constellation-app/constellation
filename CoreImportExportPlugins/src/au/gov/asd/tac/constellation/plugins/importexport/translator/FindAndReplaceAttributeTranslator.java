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
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import org.openide.util.lookup.ServiceProvider;

/**
 * A FindAndReplaceAttributeTranslator translates the input field by finding
 * sequences of characters in the input field value that match a specified
 * regular expression and replacing them with a specified sequence of
 * characters.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeTranslator.class)
public class FindAndReplaceAttributeTranslator extends AttributeTranslator {

    public static final String FIND_PARAMETER_ID = PluginParameter.buildId(FindAndReplaceAttributeTranslator.class, "find");
    public static final String REPLACE_PARAMETER_ID = PluginParameter.buildId(FindAndReplaceAttributeTranslator.class, "replace");

    public FindAndReplaceAttributeTranslator() {
        super("Find & Replace", 200);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> regexParam = StringParameterType.build(FIND_PARAMETER_ID);
        regexParam.setName("Find");
        regexParam.setDescription("The pattern to find (regular expression)");
        regexParam.setStringValue("");
        parameters.addParameter(regexParam);

        final PluginParameter<StringParameterValue> replaceParam = StringParameterType.build(REPLACE_PARAMETER_ID);
        replaceParam.setName("Replace With");
        replaceParam.setDescription("Replace each pattern with");
        replaceParam.setStringValue("");
        parameters.addParameter(replaceParam);

        return parameters;
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {

        final String regex = parameters.getParameters().get(FIND_PARAMETER_ID).getStringValue();
        final String replace = parameters.getParameters().get(REPLACE_PARAMETER_ID).getStringValue();

        if (value == null) {
            return null;
        } else {
            return value.replaceAll(regex, replace);
        }
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        final String regex = parameters.getParameters().get(FIND_PARAMETER_ID).getStringValue();
        final String replace = parameters.getParameters().get(REPLACE_PARAMETER_ID).getStringValue();

        return String.format("%s%n%s", regex, replace);
    }

    @Override
    public void setParameterValues(final PluginParameters parameters, final String values) {
        final String[] vals = values.split(SeparatorConstants.NEWLINE);
        parameters.getParameters().get(FIND_PARAMETER_ID).setStringValue(vals[0]);
        parameters.getParameters().get(REPLACE_PARAMETER_ID).setStringValue(vals[1]);
    }
}
