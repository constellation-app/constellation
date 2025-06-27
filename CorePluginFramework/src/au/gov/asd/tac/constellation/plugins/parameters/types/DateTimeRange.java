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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A date time range, with the start and end times truncated to the second.
 * <p>
 * This can be an absolute time range with fixed values for start and end, or a
 * period relative to "now".
 *
 * @author algol
 */
public class DateTimeRange {

    private static final Logger LOGGER = Logger.getLogger(DateTimeRange.class.getName());

    // If period is null, zstart and zend contain an absolute range.
    // If period is not null, the range is relative, and zstart has the ZoneId the the absolute values are displayed in.
    private final Period period;
    private final ZonedDateTime zstart;
    private final ZonedDateTime zend;
    
    private static final Pattern SEP_PATTERN = Pattern.compile(SeparatorConstants.SEMICOLON);

    /**
     * A range with the specified ZoneDateTime instances.
     *
     * @param zstart the start of the time range.
     * @param zend the end of the time range.
     */
    public DateTimeRange(final ZonedDateTime zstart, final ZonedDateTime zend) {
        this.period = null;
        this.zstart = zstart.truncatedTo(ChronoUnit.SECONDS);
        this.zend = zend.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * A range with the specified start and end Date instances.
     *
     * @param start the start of the range.
     * @param end the end of the range.
     */
    public DateTimeRange(final Date start, final Date end) {
        this.period = null;
        this.zstart = ZonedDateTime.ofInstant(start.toInstant(), ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);
        this.zend = ZonedDateTime.ofInstant(end.toInstant(), ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * A relative date range.
     * <p>
     * When the start and end dates are retrieved, the end date will be "now",
     * and the start date will be the period before the end date.
     *
     * @param period the period that represents the time range.
     * @param zi the id of the time zone.
     */
    public DateTimeRange(final Period period, final ZoneId zi) {
        this.period = period;
        this.zstart = ZonedDateTime.now(zi);
        this.zend = null;
    }

    /**
     * A range which is identical to the given range
     *
     * @param dtr the DateTimeRange to copy.
     */
    public DateTimeRange(final DateTimeRange dtr) {
        period = dtr.period;
        zstart = dtr.zstart;
        zend = dtr.zend;
    }

    /**
     * The relative period of this instance.
     * <p>
     * If the period is null, this instance is absolute; use getZonedStartEnd
     * instead.
     *
     * @return The relative Period of this instance, or null if the range is
     * absolute.
     */
    public Period getPeriod() {
        return period;
    }

    public ZoneId getZoneId() {
        return zstart.getZone();
    }

    /**
     * If the range is absolute, start and end are returned as-is.
     * <p>
     * If the range is relative, end is "now" and start is the relative datetime
     * prior to start.
     *
     * @return A ZonedDateTime[2] containing the start,end of the range.
     */
    public ZonedDateTime[] getZonedStartEnd() {
        final ZonedDateTime z0;
        final ZonedDateTime z1;
        if (period != null) {
            z1 = ZonedDateTime.now(zstart.getZone()).truncatedTo(ChronoUnit.SECONDS);
            z0 = z1.minus(period);
        } else {
            z0 = zstart;
            z1 = zend;
        }

        return new ZonedDateTime[]{z0, z1};
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        } else if (this.getClass() != other.getClass()) {
            return false;
        } else {
            final DateTimeRange otherRange = (DateTimeRange) other;
            if (period != null) {
                return otherRange.period != null && period.equals(otherRange.period);
            } else {
                return zstart.equals(otherRange.zstart) && zend.equals(otherRange.zend);
            }
        }
    }

    @Override
    public int hashCode() {
        final int hash;
        if (period != null) {
            hash = period.hashCode();
        } else {
            hash = zstart.hashCode() ^ zend.hashCode();
        }

        return hash;
    }

    /**
     * Parse the output of toString().
     * <p>
     * If a DateTimeParseException is thrown, a default DateTimeRange value will
     * be returned.
     *
     * @param s The string to be parsed.
     *
     * @return A new DateTimeRange.
     */
    public static DateTimeRange parse(final String s) {
        if (s.startsWith("P")) {
            // Relative range.
            try {
                final String[] split = s.split(" ");
                final Period period = Period.parse(split[0]);

                // If no zone was specified, use UTC.
                final String zone = split.length > 1 ? split[1] : "UTC";
                final ZoneId zi = ZoneId.of(zone);

                return new DateTimeRange(period, zi);
            } catch (final DateTimeParseException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            // Default period when we couldn't parse the value.
            final Period period = Period.ofDays(1);
            final ZoneId zi = ZoneId.of("UTC");

            return new DateTimeRange(period, zi);
        } else {
            // Absolute range.
            try {
                final String[] startEnd = SEP_PATTERN.split(s);
                final ZonedDateTime zstart;
                final ZonedDateTime zend;
                if (s.contains(SeparatorConstants.SEMICOLON)) {
                    zstart = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(startEnd[0], ZonedDateTime::from);
                    zend = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(startEnd[1], ZonedDateTime::from);

                    return new DateTimeRange(zstart, zend);
                }
            } catch (final DateTimeParseException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            final ZonedDateTime zend = ZonedDateTime.now(ZoneId.of("UTC"));
            final ZonedDateTime zstart = zend.minusDays(1);

            return new DateTimeRange(zstart, zend);
        }
    }

    /**
     * Absolute ranges return two ISO8601 dates, relative ranges return an
     * ISO8601 duration; both of these have a timezone appended.
     *
     * @return A representation of the range.
     */
    @Override
    public String toString() {
        if (period != null) {
            // The period followed by the ZoneId that absolute values are displayed in.
            return String.format("%s %s", period.toString(), zstart.getZone());
        } else {
            // The absolute values separated by SEP.
            return String.format("%s%s%s", zstart, SeparatorConstants.SEMICOLON, zend);
        }
    }
}
