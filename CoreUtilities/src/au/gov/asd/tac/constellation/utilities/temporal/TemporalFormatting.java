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
package au.gov.asd.tac.constellation.utilities.temporal;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * A collection of utilities relating to the formatting of temporal attributes.
 *
 * @author twilight_sparkle
 */
public class TemporalFormatting {

    public static final int YEAR_FORMAT_LENGTH = 4;

    public static final int MONTH_FORMAT_LENGTH = 2;
    public static final int MONTH_FOMART_START_POSITION = YEAR_FORMAT_LENGTH + 1;
    public static final int YEAR_MONTH_FORMAT_LENGTH = MONTH_FOMART_START_POSITION + MONTH_FORMAT_LENGTH;

    public static final int DAY_FORMAT_LENGTH = 2;
    public static final int DAY_FOMART_START_POSITION = YEAR_MONTH_FORMAT_LENGTH + 1;
    public static final int DATE_FORMAT_LENGTH = DAY_FOMART_START_POSITION + DAY_FORMAT_LENGTH;

    public static final int HOUR_FORMAT_LENGTH = 2;
    public static final int HOUR_FOMART_START_POSITION = DATE_FORMAT_LENGTH + 1;
    public static final int DATE_HOUR_FORMAT_LENGTH = HOUR_FOMART_START_POSITION + HOUR_FORMAT_LENGTH;

    public static final int MINUTE_FORMAT_LENGTH = 2;
    public static final int MINUTE_FORMAT_START_POSITION = HOUR_FORMAT_LENGTH + 1;
    public static final int DATE_MINUTE_FORMAT_START_POSITION = DATE_HOUR_FORMAT_LENGTH + 1;
    public static final int HOUR_MINUTE_FORMAT_LENGTH = MINUTE_FORMAT_START_POSITION + MINUTE_FORMAT_LENGTH;
    public static final int DATE_HOUR_MINUTE_FORMAT_LENGTH = DATE_MINUTE_FORMAT_START_POSITION + MINUTE_FORMAT_LENGTH;

    public static final int SECOND_FORMAT_LENGTH = 2;
    public static final int SECOND_FORMAT_START_POSITION = HOUR_MINUTE_FORMAT_LENGTH + 1;
    public static final int DATE_SECOND_FORMAT_START_POSITION = DATE_HOUR_MINUTE_FORMAT_LENGTH + 1;
    public static final int HMS_FORMAT_LENGTH = SECOND_FORMAT_START_POSITION + SECOND_FORMAT_LENGTH;
    public static final int DATE_HMS_FORMAT_LENGTH = DATE_SECOND_FORMAT_START_POSITION + SECOND_FORMAT_LENGTH;

    public static final int MILLISECOND_FORMAT_LENGTH = 3;
    public static final int MILLISECOND_FORMAT_START_POSITION = HMS_FORMAT_LENGTH + 1;
    public static final int DATE_MILLISECOND_FORMAT_START_POSITION = DATE_HMS_FORMAT_LENGTH + 1;
    public static final int TIME_FORMAT_LENGTH = MILLISECOND_FORMAT_START_POSITION + MILLISECOND_FORMAT_LENGTH;
    public static final int DATE_TIME_FORMAT_LENGTH = DATE_MILLISECOND_FORMAT_START_POSITION + MILLISECOND_FORMAT_LENGTH;

    public static final int ZONE_OFFSET_FORMAT_LENGTH = 6;
    public static final int ZONE_OFFSET_FORMAT_START_POSITION = DATE_TIME_FORMAT_LENGTH + 1;
    public static final int ZONE_OFFSET_DATE_TIME_FORMAT_LENGTH = ZONE_OFFSET_FORMAT_START_POSITION + ZONE_OFFSET_FORMAT_LENGTH;

    public static final int ZONE_NAME_FORMAT_START_POSITION = ZONE_OFFSET_DATE_TIME_FORMAT_LENGTH + 2;

    public static final String ERROR_PARSING_DATE_MESSAGE = "Error Parsing Date {0}: {1}";

    private static final ZonedDateTime EPOCH_UTC = ZonedDateTime.ofInstant(Instant.EPOCH, TimeZoneUtilities.UTC);
    
    /**
     * A UTC date time formatter much like DateTimeFormatter.ISO_INSTANT but
     * with the guarantee that milliseconds will not appear. The format is
     * yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral("T")
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(SeparatorConstants.COLON)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(SeparatorConstants.COLON)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral("Z")
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    /**
     * A UTC date time formatter much like DateTimeFormatter.ISO_INSTANT but
     * with the guarantee that milliseconds will always appear. The format is
     * yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final DateTimeFormatter UTC_DATE_TIME_WITH_MILLISECONDS_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral("T")
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(SeparatorConstants.COLON)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(SeparatorConstants.COLON)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral(SeparatorConstants.PERIOD)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .appendLiteral("Z")
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    /**
     * A Formatter for converting LocalDate objects into Strings. It defines the
     * string representation of CONSTELLATION's DateAttributeDescription.
     * <p>
     * The format is 'yyyy-mm-dd'.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * A Formatter for converting LocalTime objects into Strings. It defines the
     * string representation of CONSTELLATION's TimeAttributeDescription.
     * <p>
     * The format is 'hh:mm:ss.SSS'.
     */
    public static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(SeparatorConstants.COLON)
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(SeparatorConstants.COLON)
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral(SeparatorConstants.PERIOD)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();

    /**
     * A Formatter for converting LocalDateTime objects into Strings. It defines
     * the string representation of CONSTELLATION's
     * LocalDateTimeAttributeDescription.
     * <p>
     * The format is 'yyyy-mm-dd hh:mm:ss.SSS'.
     */
    public static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(TemporalFormatting.DATE_FORMATTER)
            .appendLiteral(" ")
            .append(TemporalFormatting.TIME_FORMATTER)
            .toFormatter();

    /**
     * A Formatter for converting Temporal objects with ZoneIds into Strings
     * representing those ZoneIds. It defines the string representation of
     * CONSTELLATION's TimeZoneAttributeDescription.
     * <p>
     * The format is '+zzzz [regionId]', with the regionId in brackets being
     * optional.
     */
    public static final DateTimeFormatter TIME_ZONE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOffset("+HH:MM", "+00:00")
            .optionalStart()
            .appendLiteral(" ")
            .appendLiteral("[")
            .appendZoneRegionId()
            .appendLiteral("]")
            .optionalEnd()
            .toFormatter();

    /**
     * A Formatter for converting ZonedDateTime objects into Strings. It defines
     * the string representation of CONSTELLATION's
     * DateTimeAttributeDescription.
     * <p>
     * The format is 'yyyy-mm-dd hh:mm:ss.SSS +zzzz [regionId]', with the
     * regionId in brackets being optional.
     */
    public static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(DATE_FORMATTER)
            .appendLiteral(" ")
            .append(TIME_FORMATTER)
            .appendLiteral(" ")
            .append(TIME_ZONE_FORMATTER)
            .toFormatter();

    private TemporalFormatting() {
        //Do nothing
    }
    
    /**
     * Takes a partially complete date-time string and adds default values for
     * the remaining fields to convert it to a CONSTELLATION format date-time
     * string.
     * <p>
     * This is useful when processing string-based temporal data that doesn't
     * include certain fields such as milliseconds or a time-zone.
     * <p>
     * If the input is null or it does not make sense to convert it, then it
     * will simply be returned.
     *
     * @param dateTimeString the incomplete date time string.
     * @return a completed date time string.
     */
    public static String completeZonedDateTimeString(final String dateTimeString) {
        if (dateTimeString == null || dateTimeString.length() >= ZONE_OFFSET_DATE_TIME_FORMAT_LENGTH) {
            return dateTimeString;
        }
        final StringBuilder completedDateTimeString = new StringBuilder(dateTimeString);
        completeDateString(completedDateTimeString);
        completeTimeInDateTimeString(completedDateTimeString);
        completeZoneInDateTimeString(completedDateTimeString);
        return completedDateTimeString.toString();
    }

    /**
     * Takes a partially complete local date-time string and adds default values
     * for the remaining fields to convert it to a CONSTELLATION format local
     * date-time string.
     * <p>
     * This is useful when processing string-based temporal data that doesn't
     * include certain fields such as milliseconds.
     * <p>
     * If the input is null or it does not make sense to convert it, then it
     * will simply be returned.
     *
     * @param localDateTimeString the incomplete date time string.
     * @return a completed date time string.
     */
    public static String completeLocalDateTimeString(final String localDateTimeString) {
        if (localDateTimeString == null || localDateTimeString.length() == DATE_TIME_FORMAT_LENGTH) {
            return localDateTimeString;
        }
        final StringBuilder completedLocalDateTimeString = new StringBuilder(localDateTimeString);
        completeDateString(completedLocalDateTimeString);
        completeTimeInDateTimeString(completedLocalDateTimeString);
        return completedLocalDateTimeString.toString();
    }

    /**
     * Takes a partially complete date string and adds default values for the
     * remaining fields to convert it to a CONSTELLATION format date string.
     * <p>
     * This is useful when processing string-based temporal data that doesn't
     * include certain fields such as the day of the month.
     * <p>
     * If the input is null or it does not make sense to convert it, then it
     * will simply be returned.
     *
     * @param dateString the incomplete date string.
     * @return a completed date string.
     */
    public static String completeDateString(final String dateString) {
        if (dateString == null || dateString.length() == DATE_FORMAT_LENGTH) {
            return dateString;
        }
        final StringBuilder completedDateString = new StringBuilder(dateString);
        completeDateString(completedDateString);
        return completedDateString.toString();
    }

    /**
     * Takes a partially complete time string and adds default values for the
     * remaining fields to convert it to a CONSTELLATION format time string.
     * <p>
     * This is useful when processing string-based temporal data that doesn't
     * include certain fields such as milliseconds.
     * <p>
     * If the input is null or it does not make sense to convert it, then it
     * will simply be returned.
     *
     * @param timeString this incomplete time string.
     * @return a completed time string.
     */
    public static String completeTimeString(final String timeString) {
        if (timeString == null || timeString.length() == TIME_FORMAT_LENGTH) {
            return timeString;
        }
        final StringBuilder completedTimeString = new StringBuilder(timeString);
        completeTimeString(completedTimeString);
        return completedTimeString.toString();
    }

    @SuppressWarnings("fallthrough")
    private static void completeDateString(final StringBuilder dateTimeStringBuilder) {
        final int currentLength = dateTimeStringBuilder.length();
        switch (currentLength) {
            case 0:
                dateTimeStringBuilder.append("1970");
            case YEAR_FORMAT_LENGTH:
                dateTimeStringBuilder.append("-01");
            case YEAR_MONTH_FORMAT_LENGTH:
                dateTimeStringBuilder.append("-01");
            default:
                break;
        }
    }

    @SuppressWarnings("fallthrough")
    private static void completeTimeString(final StringBuilder timeStringBuilder) {
        final int currentLength = timeStringBuilder.length();
        switch (currentLength) {
            case 0:
                timeStringBuilder.append("00");
            case HOUR_FORMAT_LENGTH:
                timeStringBuilder.append(":00");
            case HOUR_MINUTE_FORMAT_LENGTH:
                timeStringBuilder.append(":00");
            case HMS_FORMAT_LENGTH:
                timeStringBuilder.append(".000");
            default:
                break;
        }
    }

    @SuppressWarnings("fallthrough")
    private static void completeTimeInDateTimeString(final StringBuilder dateTimeStringBuilder) {
        final int currentLength = dateTimeStringBuilder.length();
        switch (currentLength) {
            case DATE_FORMAT_LENGTH:
                dateTimeStringBuilder.append(" 00");
            case DATE_HOUR_FORMAT_LENGTH:
                dateTimeStringBuilder.append(":00");
            case DATE_HOUR_MINUTE_FORMAT_LENGTH:
                dateTimeStringBuilder.append(":00");
            case DATE_HMS_FORMAT_LENGTH:
                dateTimeStringBuilder.append(".000");
            default:
                break;
        }
    }

    private static void completeZoneInDateTimeString(final StringBuilder dateTimeStringBuilder) {
        final int currentLength = dateTimeStringBuilder.length();
        if (currentLength == DATE_TIME_FORMAT_LENGTH) {
            dateTimeStringBuilder.append(" +00:00 [UTC]");
        }
    }

    /**
     * Formats the given temporal accessor as a date-time string that conforms
     * to the specifications of CONSTELLATION's DateTimeAttributeDescription.
     * <p>
     * Note that the temporal accessor need not have all the fields required by
     * the desired format - where they are missing, defaults will be used from
     * the ZonedDateTime corresponding to the beginning of the epoch in UTC.
     *
     * @param accessor The temporal accessor to format.
     * @return A string in a format consistent with
     * DateTimeAttributeDescription.getString() and return null if null was
     * passed as the accessor.
     */
    public static String formatAsZonedDateTime(final TemporalAccessor accessor) {
        return accessor != null ? ZONED_DATE_TIME_FORMATTER.format(withDefaults(accessor)) : null;
    }

    /**
     * Formats the given temporal accessor as a local date-time string that
     * conforms to the specifications of CONSTELLATION's
     * LocalDateTimeAttributeDescription.
     * <p>
     * Note that the temporal accessor need not have all the fields required by
     * the desired format - where they are missing, defaults will be used from
     * the ZonedDateTime corresponding to the beginning of the epoch in UTC.
     *
     * @param accessor The temporal accessor to format.
     * @return A string in a format consistent with
     * DateTimeAttributeDescription.getString() and return null if null was
     * passed as the accessor.
     */
    public static String formatAsLocalDateTime(final TemporalAccessor accessor) {
        return accessor != null ? LOCAL_DATE_TIME_FORMATTER.format(withDefaults(accessor)) : null;
    }

    /**
     * Formats the given temporal accessor as a local date string that conforms
     * to the specifications of CONSTELLATION's DateAttributeDescription.
     * <p>
     * Note that the temporal accessor need not have all the fields required by
     * the desired format - where they are missing, defaults will be used from
     * the ZonedDateTime corresponding to the beginning of the epoch in UTC.
     *
     * @param accessor The temporal accessor to format.
     * @return A string in a format consistent with
     * DateTimeAttributeDescription.getString() and return null if null was
     * passed as the accessor.
     */
    public static String formatAsDate(final TemporalAccessor accessor) {
        return accessor != null ? DATE_FORMATTER.format(withDefaults(accessor)) : null;
    }

    /**
     * Formats the given temporal accessor as a local time string that conforms
     * to the specifications of CONSTELLATION's TimeAttributeDescription.
     * <p>
     * Note that the temporal accessor need not have all the fields required by
     * the desired format - where they are missing, defaults will be used from
     * the ZonedDateTime corresponding to the beginning of the epoch in UTC.
     *
     * @param accessor The temporal accessor to format.
     * @return A string in a format consistent with
     * DateTimeAttributeDescription.getString() and return null if null was
     * passed as the accessor.
     */
    public static String formatAsTime(final TemporalAccessor accessor) {
        return accessor != null ? TIME_FORMATTER.format(withDefaults(accessor)) : null;
    }

    /**
     * Retrieve a ZonedDatetime object in UTC, corresponding to the instant
     * represented by the given long.
     *
     * @param value A long representing an instant in time; the number of
     * seconds since epoch.
     * @return A ZonedDateTime object in UTC, corresponding to the given long.
     */
    public static ZonedDateTime zonedDateTimeFromLong(final long value) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(value), TimeZoneUtilities.UTC);
    }

     /**
     * Retrieve a ZonedDatetime object in UTC, corresponding to the instant
     * represented by the given long.
     * This is required for translating EXCEL date times correctly 
     *
     * @param value A long representing an instant in time; the number of
     * milliseconds since epoch.
     * @return A ZonedDateTime object in UTC, corresponding to the given long.
     */
    public static ZonedDateTime zonedDateTimeFromLongMilli(final long value) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), TimeZoneUtilities.UTC);
    }

    /**
     * Retrieve a String in CONSTELLATION's zoned date-time format,
     * corresponding to the instant represented by the given long.
     *
     * @param value A long representing an instant in time; the number of
     * seconds since epoch.
     * @return A formatted String corresponding to the given long.
     */
    public static String zonedDateTimeStringFromLong(final long value) {
        return formatAsZonedDateTime(zonedDateTimeFromLong(value));
    }

    /**
     * Formats the given temporal accessor with the formatter provided.
     * <p>
     * Note that the temporal accessor need not have all the fields required by
     * the desired format - where they are missing, defaults will be used from
     * the ZonedDateTime corresponding to the beginning of the epoch in UTC.
     *
     * @param formatter The custom date time formatter.
     * @param accessor The temporal accessor to format.
     * @return A string in the date time format requested
     */
    public static String formatWithCustomFormatter(final DateTimeFormatter formatter, final TemporalAccessor accessor) {
        return accessor != null ? formatter.format(withDefaults(accessor)) : null;
    }

    /**
     * Parse the date time value as a ZonedDateTime. Any exception is logged and
     * absorbed as a convenience method so that processing can continue without
     * interruptions.
     *
     * @param value The date time value
     * @param formatter The date time formatter
     * @param logger The logger
     * @return The zoned date time formatter if it can be parsed correctly, the
     * original value if it could not be parsed or null otherwise.
     */
    public static String parseAsZonedDateTime(final String value, final DateTimeFormatter formatter, final Logger logger) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            final TemporalAccessor myDateTime = formatter.parse(value);
            final ZoneId parsedTimeZone = myDateTime.query(TemporalQueries.zoneId());
            final ZoneOffset parsedOffset = myDateTime.query(TemporalQueries.offset());
            if ((parsedTimeZone != null) || (parsedOffset != null)) {
                final ZonedDateTime myZonedDateTime = ZonedDateTime.parse(value, formatter);
                return TemporalFormatting.ZONED_DATE_TIME_FORMATTER.format(myZonedDateTime);
            } else {
                return TemporalFormatting.formatAsZonedDateTime(formatter.parse(value));
            }
        } catch (final DateTimeParseException ex) {
            logger.log(Level.SEVERE, ERROR_PARSING_DATE_MESSAGE, new Object[]{value, ex.getMessage()});
            return value;
        }
    }

    private static TemporalAccessor withDefaults(final TemporalAccessor accessor) {
        return new DelegatingTemporalAccessorWithDefaults(accessor);
    }

    private static class DelegatingTemporalAccessorWithDefaults implements TemporalAccessor {

        private final TemporalAccessor delegate;

        public DelegatingTemporalAccessorWithDefaults(final TemporalAccessor delegate) {
            this.delegate = delegate;
        }

        @Override
        public long getLong(final TemporalField field) {
            if (delegate.isSupported(field)) {
                return delegate.getLong(field);
            }
            return EPOCH_UTC.getLong(field);
        }

        @Override
        public boolean isSupported(final TemporalField field) {
            return delegate.isSupported(field) || EPOCH_UTC.isSupported(field);
        }

    }

}
