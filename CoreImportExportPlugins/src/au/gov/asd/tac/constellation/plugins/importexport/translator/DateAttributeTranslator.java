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
package au.gov.asd.tac.constellation.plugins.importexport.translator;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * Date Attribute Translator
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeTranslator.class)
public class DateAttributeTranslator extends AttributeTranslator {

    public static final String FORMAT_PARAMETER_ID = PluginParameter.buildId(DateAttributeTranslator.class, "format");
    public static final String CUSTOM_PARAMETER_ID = PluginParameter.buildId(DateAttributeTranslator.class, "custom");

    private static final Map<String, String> DATE_FORMATS = new LinkedHashMap<>();
    private static final DateTimeFormatter RESULT_FORMAT = TemporalFormatting.DATE_FORMATTER;

    static {
        DATE_FORMATS.put("yyyy-MM-dd", "yyyy-MM-dd");
        DATE_FORMATS.put("MM/dd/yyyy", "MM/dd/yyyy");
        DATE_FORMATS.put("dd/MM/yyyy", "dd/MM/yyyy");
        DATE_FORMATS.put("yyyyMMdd", "yyyyMMdd");
        DATE_FORMATS.put("CUSTOM", null);
    }

    public DateAttributeTranslator() {
        super("Date", 50, "date");
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> formatParam = SingleChoiceParameterType.build(FORMAT_PARAMETER_ID);
        formatParam.setName("Date Format");
        formatParam.setDescription("The date format");
        final List<String> datetimeLabels = new ArrayList<>(DATE_FORMATS.keySet());
        SingleChoiceParameterType.setOptions(formatParam, datetimeLabels);
        SingleChoiceParameterType.setChoice(formatParam, datetimeLabels.get(0));
        parameters.addParameter(formatParam);

        final PluginParameter<StringParameterValue> customParam = StringParameterType.build(CUSTOM_PARAMETER_ID);
        customParam.setName("Custom Format");
        customParam.setDescription("A custom date format");
        customParam.setEnabled(false);
        customParam.setStringValue("");
        parameters.addParameter(customParam);

        parameters.addController(FORMAT_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> params, final ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                final PluginParameter<?> slave = params.get(CUSTOM_PARAMETER_ID);
                slave.setEnabled("CUSTOM".equals(master.getStringValue()));
            }
        });

        return parameters;
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {

        try {
            String format = parameters.getParameters().get(FORMAT_PARAMETER_ID).getStringValue();
            format = DATE_FORMATS.get(format);

            if (format == null) {
                format = parameters.getParameters().get(CUSTOM_PARAMETER_ID).getStringValue();
            }

            final DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
            return RESULT_FORMAT.format(df.parse(value));

        } catch (final DateTimeException ex) {
            return "ERROR";
        }
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        final String label = parameters.getParameters().get(FORMAT_PARAMETER_ID).getStringValue();
        String format = null;
        if (DATE_FORMATS.containsKey(label)) {
            format = DATE_FORMATS.get(label);
            if (format == null) {
                format = parameters.getParameters().get(CUSTOM_PARAMETER_ID).getStringValue();
            }
        }

        return String.format("d:%s%nc:%s", label, format);
    }

    @Override
    public void setParameterValues(final PluginParameters parameters, final String values) {
        final String[] vals = values.split(SeparatorConstants.NEWLINE);
        for (final String val : vals) {
            if (val.startsWith("d:")) {
                parameters.getParameters().get(FORMAT_PARAMETER_ID).setStringValue(val.substring(2));
            } else if (val.startsWith("c:")) {
                parameters.getParameters().get(CUSTOM_PARAMETER_ID).setStringValue(val.substring(2));
            } else {
                // Do nothing
            }
        }
    }
}
