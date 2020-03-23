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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.logging.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Temporal Formatting Test.
 *
 * @author arcturus
 */
public class TemporalFormattingNGTest {

    private static final Logger LOGGER = Logger.getLogger(TemporalFormattingNGTest.class.getName());
    private static final DateTimeFormatter MY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy");
    private static final DateTimeFormatter MY_DATETIME_FORMATTER2 = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss x yyyy");
    private static final DateTimeFormatter MY_DATETIME_FORMATTER3 = DateTimeFormatter.ofPattern("yyyyMMdd");

    public TemporalFormattingNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of completeZonedDateTimeString method, of class TemporalFormatting.
     */
    @Test
    public void testCompleteZonedDateTimeString() {
        final String dateTimeString = "2017-06-09T18:53:45Z";
        final String expResult = "2017-06-09T18:53:45.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertNotEquals(result, expResult); // the complete zoned date time string currently does not accept the 'Z' and instead requires the offset and region.
    }

    @Test
    public void testCompleteZonedDateTimeStringWithDate() {
        final String dateTimeString = "2017-06-09";
        final String expResult = "2017-06-09 00:00:00.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertEquals(result, expResult);
    }

    @Test
    public void testCompleteZonedDateTimeStringWithDateAndTime() {
        final String dateTimeString = "2017-06-09 18:53";
        final String expResult = "2017-06-09 18:53:00.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertEquals(result, expResult);
    }

    @Test
    public void testCompleteZonedDateTimeStringWithDateTimeAndSeconds() {
        final String dateTimeString = "2017-06-09 18:53:45";
        final String expResult = "2017-06-09 18:53:45.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertEquals(result, expResult);
    }

    /**
     * Test of formatAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testFormatAsZonedDateTime() {
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER
                .parse("2016-06-11T22:48:48Z");
        final String expResult = "2016-06-11 22:48:48.000 +00:00";
        final String result = TemporalFormatting.formatAsZonedDateTime(accessor);
        assertEquals(result, expResult);
    }

    @Test
    public void testFormatAsZonedDateTimeWithMilliseconds() {
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_WITH_MILLISECONDS_FORMATTER
                .parse("2016-06-11T22:48:48.000Z");
        final String expResult = "2016-06-11 22:48:48.000 +00:00";
        final String result = TemporalFormatting.formatAsZonedDateTime(accessor);
        assertEquals(result, expResult);
    }

    @Test
    public void testZonedDateTimeConvertToUtc() {
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2018-02-22T18:38:30+01:00");
        final String expectedUtcDateTime = "2018-02-22T17:38:30Z";

        assertEquals(zonedDateTime.getZone().toString(), "+01:00"); // outputs so I will trust it is a proper zoned date time
        assertEquals(DateTimeFormatter.ISO_INSTANT.format(zonedDateTime), expectedUtcDateTime);
        assertEquals(TemporalFormatting.UTC_DATE_TIME_FORMATTER.format(zonedDateTime), expectedUtcDateTime);
        assertEquals(TemporalFormatting.UTC_DATE_TIME_FORMATTER.format(zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)), expectedUtcDateTime);
        assertEquals(TemporalFormatting.formatWithCustomFormatter(TemporalFormatting.UTC_DATE_TIME_FORMATTER, zonedDateTime), expectedUtcDateTime);

        assertEquals(TemporalFormatting.formatAsZonedDateTime(zonedDateTime), "2018-02-22 18:38:30.000 +01:00");
    }

    /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeForRecordWithValidDate() {
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER.parse("2016-06-11T22:48:48Z");
        final String time = TemporalFormatting.formatAsZonedDateTime(accessor);

        final DateTimeFormatter formatter = TemporalFormatting.UTC_DATE_TIME_FORMATTER;
        final String expResult = "2016-06-11 22:48:48.000 +00:00";
        final String result = TemporalFormatting.parseAsZonedDateTime(time, formatter, LOGGER);
        assertEquals(result, expResult);
    }

    /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeForRecordWithInvalidDate() {
        final DateTimeFormatter formatter = TemporalFormatting.UTC_DATE_TIME_FORMATTER;
        final String expResult = "0000-00-00T00:00:00Z";
        final String result = TemporalFormatting.parseAsZonedDateTime("0000-00-00T00:00:00Z", formatter, LOGGER);
        assertEquals(result, expResult);
    }

    @Test
    public void testParseDateTimeForRecordWithWrongFormat() {
        final DateTimeFormatter formatter = TemporalFormatting.UTC_DATE_TIME_FORMATTER;
        final String expResult = "2016-06-11";
        final String result = TemporalFormatting.parseAsZonedDateTime("2016-06-11", formatter, LOGGER);
        assertEquals(result, expResult);
    }

    /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeForRecordWithNullDate() {
        final DateTimeFormatter formatter = TemporalFormatting.UTC_DATE_TIME_FORMATTER;
        final String expResult = null;
        final String result = TemporalFormatting.parseAsZonedDateTime(null, formatter, LOGGER);
        assertEquals(result, expResult);
    }
    
     /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeWithZone() {
        String testDateTime = "Tue Apr 23 01:16:42 AEST 2019";
        final String result = TemporalFormatting.parseAsZonedDateTime(testDateTime, MY_DATETIME_FORMATTER, LOGGER);
        final String expResult = "2019-04-23 01:16:42.000 +10.00 [Australia/Sydney]";
        assertEquals(result, expResult);
    }
    
     /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeWithOffset() {
        String testDateTime = "Tue Apr 23 01:16:42 +10.00 2019";
        final String result = TemporalFormatting.parseAsZonedDateTime(testDateTime, MY_DATETIME_FORMATTER2, LOGGER);
        final String expResult = "2019-04-23 01:16:42.000 +10.00";
        assertEquals(result, expResult);
    }
    
     /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeWithNoZone() {
        String testDateTime = "20191225";
        final String result = TemporalFormatting.parseAsZonedDateTime(testDateTime, MY_DATETIME_FORMATTER3, LOGGER);
        final String expResult = "2019-12-25 00:00:00.000 +00.00";
        assertEquals(result, expResult);
    }
    
}
