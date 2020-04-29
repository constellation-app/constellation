/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.TimeZone;

/**
 * A collection of utilities relating to the display, formatting and ordering of
 * time-zones, as well as their use with respect to zoned date time objects.
 *
 * @author twilight_sparkle
 */
public class TimeZoneUtilities {

    public static final ZoneId UTC = TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId();

    public static final Comparator<ZoneId> ZONE_ID_COMPARATOR = (t1, t2) -> {
        final int offsetCompare = Integer.compare(TimeZone.getTimeZone(t1).getRawOffset(), TimeZone.getTimeZone(t2).getRawOffset());
        return offsetCompare != 0 ? offsetCompare : t1.getId().compareTo(t2.getId());
    };

    public static String getTimeZoneAsString(final ZoneId timeZone) {
        return getTimeZoneAsString(null, timeZone);
    }

    public static String getTimeZoneAsString(LocalDateTime ldt, final ZoneId timeZone) {
        if (timeZone == null) {
            return null;
        }
        if (ldt == null) {
            ldt = LocalDateTime.now();
        }
        return ZonedDateTime.of(ldt, timeZone).format(TemporalFormatting.TIME_ZONE_FORMATTER);
    }
}
