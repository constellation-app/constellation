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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Date Time Range Test
 *
 * @author ruby_crucis
 */
public class DateTimeRangeNGTest {

    private static final long SECONDS_IN_ONE_DAY = 60 * 60 * 24;
    private static final long MILLISECONDS_IN_ONE_DAY = 1000 * 60 * 60 * 24;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    @Test
    public void testRelativeRangeTimeStartMonth() {
        final DateTimeRange dt = new DateTimeRange(Period.of(0, 1, 0), ZoneId.of("UTC"));
        assertEquals(dt.toString(), "P1M UTC");
    }

    @Test
    public void testRelativeRangeTimeStartDay() {
        final DateTimeRange dt = new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC"));
        assertEquals(dt.toString(), "P1D UTC");
    }

    @Test
    public void testRelativeRangeTimeStartBoth() {
        final DateTimeRange dt = new DateTimeRange(Period.of(0, 3, 4), ZoneId.of("UTC"));
        assertEquals(dt.toString(), "P3M4D UTC");
    }

    @Test
    public void absoluteRange() {
        final Date d0 = new Date(MILLISECONDS_IN_ONE_DAY);
        final Date d1 = new Date(MILLISECONDS_IN_ONE_DAY + MILLISECONDS_IN_ONE_DAY);
        final DateTimeRange dtr = new DateTimeRange(d0, d1);
        assertEquals(dtr.toString(), "1970-01-02T00:00Z[UTC];1970-01-03T00:00Z[UTC]");
    }

    @Test
    public void absoluteRangePlus1Second() {
        final Date d0 = new Date(MILLISECONDS_IN_ONE_DAY + 1000);
        final Date d1 = new Date(MILLISECONDS_IN_ONE_DAY + MILLISECONDS_IN_ONE_DAY);
        final DateTimeRange dtr = new DateTimeRange(d0, d1);
        assertEquals(dtr.toString(), "1970-01-02T00:00:01Z[UTC];1970-01-03T00:00Z[UTC]");
    }

    @Test
    public void parseAbsoluteRangeUTC() {
        final DateTimeRange dtr = DateTimeRange.parse("1970-01-02T00:00:00Z[UTC];1970-01-03T00:00:00Z[UTC]");
        final ZonedDateTime[] dates = dtr.getZonedStartEnd();
        assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
        assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
    }

    @Test
    public void parseAbsoluteRangeTZ() {
        final DateTimeRange dtr = DateTimeRange.parse("1970-01-02T00:00:00Z[UTC];1970-01-03T00:00:00Z[UTC]");
        final ZonedDateTime[] dates = dtr.getZonedStartEnd();
        assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
        assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
    }

    @Test
    public void parseNewZ() {
        final DateTimeRange dtr = DateTimeRange.parse("1970-01-02T00:00:00Z;1970-01-03T00:00:00Z");
        final ZonedDateTime[] dates = dtr.getZonedStartEnd();
        assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
        assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
    }

    @Test
    public void parseNewTZ() {
        final DateTimeRange dtr = DateTimeRange.parse("1970-01-02T10:00:00+10:00[Australia/Sydney];1970-01-03T10:00:00+10:00[Australia/Sydney]");
        final ZonedDateTime[] dates = dtr.getZonedStartEnd();
        assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
        assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
    }

    @Test
    public void parseNewZError() {
        DateTimeRange.parse("abc;def");
    }
    
    @Test
    public void parseRelativeRange() {
        final DateTimeRange dtr = DateTimeRange.parse("P1D");
        assertEquals(dtr.getPeriod(), Period.of(0, 0, 1));
        assertEquals(dtr.getZoneId(), ZoneId.of("UTC"));
        
        final ZonedDateTime date = ZonedDateTime.of(1970, 1, 3, 0, 0, 0, 0, ZoneId.of("UTC")); 
        try (final MockedStatic<ZonedDateTime> zonedDateTimeMockedStatic = Mockito.mockStatic(ZonedDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            zonedDateTimeMockedStatic.when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(date);
            final ZonedDateTime[] dates = dtr.getZonedStartEnd();
            assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
            assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
        }
    }
    
    @Test
    public void parseRelativeRangeTZ() {
        final DateTimeRange dtr = DateTimeRange.parse("P1D Australia/Sydney");
        assertEquals(dtr.getPeriod(), Period.of(0, 0, 1));
        assertEquals(dtr.getZoneId(), ZoneId.of("Australia/Sydney"));
        
        final ZonedDateTime date = ZonedDateTime.of(1970, 1, 3, 10, 0, 0, 0, ZoneId.of("Australia/Sydney")); 
        try (final MockedStatic<ZonedDateTime> zonedDateTimeMockedStatic = Mockito.mockStatic(ZonedDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            zonedDateTimeMockedStatic.when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(date);
            final ZonedDateTime[] dates = dtr.getZonedStartEnd();
            assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
            assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
        }
    }
    
    @Test
    public void parseBadRelativeRange() {
        final DateTimeRange dtr = DateTimeRange.parse("P bad");
        assertEquals(dtr.getPeriod(), Period.of(0, 0, 1));
        assertEquals(dtr.getZoneId(), ZoneId.of("UTC"));
        
        final ZonedDateTime date = ZonedDateTime.of(1970, 1, 3, 0, 0, 0, 0, ZoneId.of("UTC")); 
        try (final MockedStatic<ZonedDateTime> zonedDateTimeMockedStatic = Mockito.mockStatic(ZonedDateTime.class, Mockito.CALLS_REAL_METHODS)) {
            zonedDateTimeMockedStatic.when(() -> ZonedDateTime.now(any(ZoneId.class)))
                    .thenReturn(date);
            final ZonedDateTime[] dates = dtr.getZonedStartEnd();
            assertEquals(dates[0].toEpochSecond(), SECONDS_IN_ONE_DAY);
            assertEquals(dates[1].toEpochSecond(), SECONDS_IN_ONE_DAY + SECONDS_IN_ONE_DAY);
        }
    }

    @Test
    public void testAbsoluteConversionToString() {
        final DateTimeRange objectValue = new DateTimeRange(new Date(0), new Date(1000));
        final DateTimeRangeParameterValue instance = new DateTimeRangeParameterValue(objectValue);
        final String s = instance.toString();
        instance.setStringValue(s);
        final DateTimeRange result = (DateTimeRange) instance.getObjectValue();
        assertEquals(result, objectValue);
    }

    @Test
    public void testRelativeConversionToString() {
        final DateTimeRange objectValue = new DateTimeRange(Period.of(0, 1, 2), ZoneId.of("UTC"));
        final DateTimeRangeParameterValue instance = new DateTimeRangeParameterValue(objectValue);
        final String s = instance.toString();
        instance.setStringValue(s);
        final DateTimeRange result = (DateTimeRange) instance.getObjectValue();
        assertEquals(result, objectValue);
    }

    @Test
    public void testRelativeNegativeConversionToString() {
        final DateTimeRange objectValue = new DateTimeRange(Period.of(0, 12, -1), ZoneId.of("UTC"));
        final DateTimeRangeParameterValue instance = new DateTimeRangeParameterValue(objectValue);
        final String s = instance.toString();
        instance.setStringValue(s);
        final DateTimeRange result = (DateTimeRange) instance.getObjectValue();
        assertEquals(result, objectValue);
    }
    
    @Test
    public void testEquals() {
        final DateTimeRange datetimeRange = DateTimeRange.parse("1970-01-02T00:00:00Z[UTC];1970-01-03T00:00:00Z[UTC]");
        final DateTimeRange datetimePeriod = DateTimeRange.parse("P1Y");
        final DateTimeRange comp1 = new DateTimeRange(Period.of(1, 0, 0), ZoneId.of("UTC"));
        final DateTimeRange comp2 = new DateTimeRange(ZonedDateTime.of(1970, 1, 2, 0, 0, 0, 0, ZoneId.of("UTC")), 
                ZonedDateTime.of(1970, 1, 3, 0, 0, 0, 0, ZoneId.of("UTC")));
        
        assertFalse(datetimeRange.equals(null));
        assertFalse(datetimeRange.equals(true));
        assertFalse(datetimeRange.equals(comp1));
        assertTrue(datetimeRange.equals(comp2));
        assertTrue(datetimePeriod.equals(comp1));
        assertFalse(datetimePeriod.equals(comp2));
    }
}
