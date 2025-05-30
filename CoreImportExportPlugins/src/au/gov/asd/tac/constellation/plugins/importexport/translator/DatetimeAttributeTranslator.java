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

import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalFormatting;
import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
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
    public static final String TIMEZONE_PARAMETER_ID = PluginParameter.buildId(DatetimeAttributeTranslator.class, "timezone");
    public static final String CONVERTED_TIMEZONE_PARAMETER_ID = PluginParameter.buildId(DatetimeAttributeTranslator.class, "convertedtimezone");

    private static final String CUSTOM = "CUSTOM";
    private static final String EXCEL = "EXCEL";
    private static final String EPOCH = "EPOCH (Unix)";


    private static final Map<String, String> DATETIME_FORMATS = new LinkedHashMap<>();

    static {
        DATETIME_FORMATS.put("MM/d/yyyy H:mm", "MM/d/yyyy H:mm");
        DATETIME_FORMATS.put("MM/d/yyyy H:mm:ss", "MM/d/yyyy H:mm:ss");
        DATETIME_FORMATS.put("d/MM/yyyy H:mm", "d/MM/yyyy H:mm");
        DATETIME_FORMATS.put("d/MM/yyyy H:mm:ss", "d/MM/yyyy H:mm:ss");
        DATETIME_FORMATS.put("yyyy-MM-d H:mm", "yyyy-MM-d H:mm");
        DATETIME_FORMATS.put("yyyy-MM-d H:mm:ss", "yyyy-MM-d H:mm:ss");
        DATETIME_FORMATS.put("yyyy-MM-d'T'H:mm:ss'Z'", "yyyy-MM-d'T'H:mm:ss'Z'");
        DATETIME_FORMATS.put("yyyy-MM-d'T'H:mm:ss.SSS'Z'", "yyyy-MM-d'T'H:mm:ss.SSS'Z'");
        DATETIME_FORMATS.put("yyyyMMdd Hmmss'Z'", "yyyyMMdd Hmmss'Z'");
        DATETIME_FORMATS.put("yyyyMMddHHmmss", "yyyyMMddHHmmss");
        DATETIME_FORMATS.put(EPOCH, null);
        DATETIME_FORMATS.put(EXCEL, null);
        DATETIME_FORMATS.put(CUSTOM, null);
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

        final ObservableList<ZoneId> timeZones = FXCollections.observableArrayList();
        ZoneId.getAvailableZoneIds().forEach(id -> timeZones.add(ZoneId.of(id)));
        final ObservableList<ZoneId> timeZonesSorted = timeZones.sorted(zoneIdComparator);

        final List<String> timeZonesSortedString = FXCollections.observableArrayList();
        timeZonesSortedString.add("");
        timeZonesSorted.forEach(id -> timeZonesSortedString.add(TimeZoneUtilities.getTimeZoneAsString(id)));

        final PluginParameter<SingleChoiceParameterValue> timeZoneParam = SingleChoiceParameterType.build(TIMEZONE_PARAMETER_ID);
        timeZoneParam.setName("Time Zone");
        timeZoneParam.setDescription("The time zone the dates represent");
        SingleChoiceParameterType.setOptions(timeZoneParam, timeZonesSortedString);
        SingleChoiceParameterType.setChoice(timeZoneParam, timeZonesSortedString.get(0));
        parameters.addParameter(timeZoneParam);

        final PluginParameter<SingleChoiceParameterValue> convertedTimeZoneParam = SingleChoiceParameterType.build(CONVERTED_TIMEZONE_PARAMETER_ID);
        convertedTimeZoneParam.setName("Display Time Zone");
        convertedTimeZoneParam.setDescription("The time zone to convert and display the dates");
        SingleChoiceParameterType.setOptions(convertedTimeZoneParam, timeZonesSortedString);
        SingleChoiceParameterType.setChoice(convertedTimeZoneParam, timeZonesSortedString.get(0));
        parameters.addParameter(convertedTimeZoneParam);

        parameters.addController(FORMAT_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> params, final ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                params.get(CUSTOM_PARAMETER_ID).setEnabled(master.getStringValue().equals(CUSTOM));
                enableDisableTimeZone(master, params);
            }
        });

        parameters.addController(CUSTOM_PARAMETER_ID, (final PluginParameter<?> master, final Map<String, PluginParameter<?>> params, final ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                enableDisableTimeZone(master, params);
            }
        });

        return parameters;
    }

    private void enableDisableTimeZone(final PluginParameter<?> master, final Map<String, PluginParameter<?>> params){
        // Disables the Time Zone field if the Date time Format or Custom Format contains a Time Zone while it's not an EPOCH time
        final String format =  master.getStringValue();
        params.get(TIMEZONE_PARAMETER_ID).setEnabled(!containsTimeZone(format) && !format.equals(EPOCH));
    }

    private boolean containsTimeZone(final String format){
        return Pattern.matches(".*[XxZzOV']$", format);
    }


    @Override
    public String translate(final String value, final PluginParameters parameters) {

        try {
            String format = parameters.getParameters().get(FORMAT_PARAMETER_ID).getStringValue();
            final ZonedDateTime zonedDateTime;
            switch (format) {
                case EPOCH -> {
                    // using Double.valueOf(value).longValue() rather than Long.parseLong(value)
                    // allows Epoch times in scietific notation to be parsed through successfully
                    zonedDateTime = TemporalFormatting.zonedDateTimeFromLong(Double.valueOf(value).longValue());
                    return translateFromZonedDateTime(zonedDateTime, parameters);
                }
                case EXCEL -> {
                    // "GMT" is used here to avoid it using the user's local time zone.
                    // If the user has selected a time zone, it is applied seperately in `translateFromZonedDateTime`
                    zonedDateTime = TemporalFormatting.zonedDateTimeFromLongMilli(DateUtil.getJavaDate(Double.parseDouble(value), TimeZone.getTimeZone("GMT")).getTime());
                    return translateFromZonedDateTime(zonedDateTime, parameters);
                }
                case CUSTOM -> {
                    format = parameters.getParameters().get(CUSTOM_PARAMETER_ID).getStringValue();
                    return translateFromTemporalAccessorDateTime(value, format, parameters);
                }
                default -> {
                    format = DATETIME_FORMATS.get(format);
                    return translateFromTemporalAccessorDateTime(value, format, parameters);
                }
            }
        } catch (final DateTimeException | IllegalArgumentException ex) {
            return "ERROR";
        }
    }

    private String translateFromZonedDateTime(final ZonedDateTime zonedDateTime, final PluginParameters parameters) {
        final PluginParameter timeZoneParam = parameters.getParameters().get(TIMEZONE_PARAMETER_ID);
        final PluginParameter convertedTimeZoneParam = parameters.getParameters().get(CONVERTED_TIMEZONE_PARAMETER_ID);

        final String timeZoneString = timeZoneParam.getStringValue();
        if (timeZoneParam.isEnabled() && StringUtils.isNotBlank(timeZoneString)) {
            ZonedDateTime dateTimeInSpecifiedTimeZone = zonedDateTime.withZoneSameLocal(getTimeZoneId(timeZoneString));
            return TemporalFormatting.ZONED_DATE_TIME_FORMATTER.format(convertToDisplayTimeZone(dateTimeInSpecifiedTimeZone, convertedTimeZoneParam.getStringValue()));
        } else {
            return TemporalFormatting.formatAsZonedDateTime(convertToDisplayTimeZone(zonedDateTime, convertedTimeZoneParam.getStringValue()));
        }
    }

    private String translateFromTemporalAccessorDateTime(final String value, final String format, final PluginParameters parameters){
        final PluginParameter timeZoneParam = parameters.getParameters().get(TIMEZONE_PARAMETER_ID);
        final PluginParameter convertedTimeZoneParam = parameters.getParameters().get(CONVERTED_TIMEZONE_PARAMETER_ID);

        final DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        final String timeZoneString = timeZoneParam.getStringValue();
        // ZonedDateTime.parse requires a time zone identifier in the string (`value`)
        // hence zonedDateTime = ZonedDateTime.parse(value, df); doesn't work for all other formats
        if (timeZoneParam.isEnabled() && StringUtils.isNotBlank(timeZoneString)) {
            final ZonedDateTime dateTimeInSpecifiedTimeZone = ZonedDateTime.parse(value, df.withZone(getTimeZoneId(timeZoneString)));
            return TemporalFormatting.ZONED_DATE_TIME_FORMATTER.format(convertToDisplayTimeZone(dateTimeInSpecifiedTimeZone, convertedTimeZoneParam.getStringValue()));


        } else {
            final TemporalAccessor temporalAccessorDateTime = df.parse(value);
            if (StringUtils.isNotBlank(convertedTimeZoneParam.getStringValue())) {
                final LocalDateTime localDateTime = LocalDateTime.parse(value, df);
                final ZoneId zoneId = (!containsTimeZone(format) || format.endsWith("'Z'")) ? ZoneId.of("UTC") : ZoneId.from(temporalAccessorDateTime);
                final ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
                return TemporalFormatting.ZONED_DATE_TIME_FORMATTER.format(convertToDisplayTimeZone(zonedDateTime, convertedTimeZoneParam.getStringValue()));
            }
            return TemporalFormatting.formatAsZonedDateTime(temporalAccessorDateTime);
        }
    }

    private ZonedDateTime convertToDisplayTimeZone(final ZonedDateTime dateTime, final String displayTimeZone){
        return StringUtils.isNotBlank(displayTimeZone) ? dateTime.withZoneSameInstant(getTimeZoneId(displayTimeZone)) : dateTime;
    }

    private ZoneId getTimeZoneId(final String timeZone){
        return TimeZone.getTimeZone(StringUtils.substringBetween(timeZone, "[", "]")).toZoneId();
    }

    private final Comparator<ZoneId> zoneIdComparator = (t1, t2) -> {
        final int offsetCompare = Integer.compare(TimeZone.getTimeZone(t1).getRawOffset(), TimeZone.getTimeZone(t2).getRawOffset());
        return offsetCompare != 0 ? offsetCompare : t1.getId().compareTo(t2.getId());
    };

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
