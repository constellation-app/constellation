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

import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
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
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.DateUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 * Datetime Attribute Translator
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeTranslator.class)
public class DatetimeAttributeTranslator extends AttributeTranslator {

    public static final String FORMAT_PARAMETER_ID = PluginParameter.buildId(DatetimeAttributeTranslator.class, "format");
    public static final String CUSTOM_PARAMETER_ID = PluginParameter.buildId(DatetimeAttributeTranslator.class, "custom");

    private static final String CUSTOM = "CUSTOM";
    private static final String EXCEL = "EXCEL";

    private static final Map<String, String> DATETIME_FORMATS = new LinkedHashMap<>();

    static {
        DATETIME_FORMATS.put("MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm:ss");
        DATETIME_FORMATS.put("dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm:ss");
        DATETIME_FORMATS.put("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss");
        DATETIME_FORMATS.put("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'");
        DATETIME_FORMATS.put("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DATETIME_FORMATS.put("yyyyMMdd HHmmss'Z'", "yyyyMMdd HHmmss'Z'");
        DATETIME_FORMATS.put("yyyyMMddHHmmss", "yyyyMMddHHmmss");
        DATETIME_FORMATS.put("EPOCH", null);
        DATETIME_FORMATS.put(CUSTOM, null);
        DATETIME_FORMATS.put(EXCEL, null);
    }

    public DatetimeAttributeTranslator() {
        super("Datetime", 50, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME);
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> formatParam = SingleChoiceParameterType.build(FORMAT_PARAMETER_ID);
        formatParam.setName("Datetime Format");
        formatParam.setDescription("The datetime format");
        final List<String> datetimeLabels = new ArrayList<>(DATETIME_FORMATS.keySet());
        SingleChoiceParameterType.setOptions(formatParam, datetimeLabels);
        SingleChoiceParameterType.setChoice(formatParam, datetimeLabels.get(0));
        parameters.addParameter(formatParam);

        final PluginParameter<StringParameterValue> customParam = StringParameterType.build(CUSTOM_PARAMETER_ID);
        customParam.setName("Custom Format");
        customParam.setDescription("A custom datetime format");
        // customParam should be enabled and editable if "CUSTOM" format has been specified.
        customParam.setEnabled(formatParam.getStringValue().equals(CUSTOM));
        customParam.setStringValue("");
        parameters.addParameter(customParam);

        parameters.addController(FORMAT_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> params, final ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                final PluginParameter<?> slave = params.get(CUSTOM_PARAMETER_ID);
                slave.setEnabled(master.getStringValue().equals(CUSTOM));
            }
        });

        return parameters;
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {

        try {
            String format = parameters.getParameters().get(FORMAT_PARAMETER_ID).getStringValue();

            final TemporalAccessor dateTime;
            switch (format) {
                case "EPOCH":
                    dateTime = TemporalFormatting.zonedDateTimeFromLong(Long.parseLong(value));
                    break;
                case CUSTOM: {
                    format = parameters.getParameters().get(CUSTOM_PARAMETER_ID).getStringValue();
                    final DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
                    dateTime = df.parse(value);
                    break;
                }
                case EXCEL: {
                    dateTime = TemporalFormatting.zonedDateTimeFromLong(DateUtil.getJavaDate(Double.parseDouble(value)).getTime());
                    break;
                }
                default: {
                    format = DATETIME_FORMATS.get(format);
                    final DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
                    dateTime = df.parse(value);
                    break;
                }
            }

            return TemporalFormatting.formatAsZonedDateTime(dateTime);

        } catch (final DateTimeException | IllegalArgumentException ex) {
            return "ERROR";
        }
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        final String label = parameters.getParameters().get(FORMAT_PARAMETER_ID).getStringValue();
        String format = null;
        if (DATETIME_FORMATS.containsKey(label)) {
            format = DATETIME_FORMATS.get(label);
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
