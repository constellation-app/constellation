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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * A collection of utilities relating to the display, formatting and ordering of
 * time-zones, as well as their use with respect to zoned date time objects.
 *
 * @author twilight_sparkle
 */
public class TemporalUtilities {

    public static final ZoneId UTC = TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId();

    public static final Comparator<ZoneId> ZONE_ID_COMPARATOR = (t1, t2) -> {
        final int offsetCompare = Integer.compare(TimeZone.getTimeZone(t1).getRawOffset(), TimeZone.getTimeZone(t2).getRawOffset());
        return offsetCompare != 0 ? offsetCompare : t1.getId().compareTo(t2.getId());
    };
    
    private TemporalUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static String getTimeZoneAsString(final ZoneId timeZone) {
        return getTimeZoneAsString(null, timeZone);
    }

    public static String getTimeZoneAsString(final LocalDateTime ldt, final ZoneId timeZone) {
        if (timeZone == null) {
            return null;
        }
        return ZonedDateTime.of(ldt != null ? ldt : LocalDateTime.now(), timeZone).format(TemporalFormatting.TIME_ZONE_FORMATTER);
    }
    
    /**
     * Converts a LocalDate to a Date Object
     * @param localDate
     * @return a Date
     */
    public static Date localDateToDate(LocalDate localDate){
        return new Date(localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000);
    }
    
    /**
     * Converts a Date to a LocalDate Object
     * ZoneID
     * @param date
     * @return a LocalDate
     */
    public static LocalDate dateToLocalDate(Date date){
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
