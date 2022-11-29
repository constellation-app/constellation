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
package au.gov.asd.tac.constellation.utilities.temporal;

/**
 * A collection of temporal constants for converting between time units etc.
 *
 * @author twilight_sparkle
 */
public class TemporalConstants {

    public static final int NANOSECONDS_IN_MILLISECOND = 1000000;
    public static final int MILLISECONDS_IN_SECOND = 1000;
    // Used for converting legacy representations, eg. java Date and Calendar objects, whose long representations are milliseconds since epoch, to longs which represent days since epoch.
    public static final long MILLISECONDS_IN_DAY = 24 * 3600 * 1000L;

    //DateTime formats
    public static final String MILLISEC_FORMAT = "SSS";
    public static final String SEC_MILLISEC_FORMAT = "s.SSS";
    public static final String HOUR_MIN_SEC_FORMAT = "HH:mm:ss";
    public static final String HOUR_MIN_FORMAT = "HH:mm";
    public static final String DAY_MONTH_HOUR_MIN_FORMAT = "d MMM HH:mm";
    public static final String DAY_MONTH_FORMAT = "d MMM";
    public static final String MONTH_YEAR_FORMAT = "MMM yyyy";
    public static final String YEAR_FORMAT = "yyyy";

    public static final String DATE_TIME_FULL_FORMAT = "DDMMYYYYHHMMSS";
}
