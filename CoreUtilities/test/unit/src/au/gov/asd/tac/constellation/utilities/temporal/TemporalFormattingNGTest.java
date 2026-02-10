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
package au.gov.asd.tac.constellation.utilities.temporal;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.TimeZone;
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
    private static final DateTimeFormatter MY_DATETIME_FORMATTER1 = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter MY_DATETIME_FORMATTER2 = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss x yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter MY_DATETIME_FORMATTER3 = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
    
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

    /**
     * Test of completeZonedDateTimeString method, of class TemporalFormatting. Invalid Input 
     */
    @Test
    public void testCompleteZonedDateTimeStringInvalidInput() {
        System.out.println("testCompleteZonedDateTimeStringInvalidInput");
        
        final String nullResult = TemporalFormatting.completeZonedDateTimeString(null);
        final String tooBigResult = TemporalFormatting.completeZonedDateTimeString("2017-06-09 18:53:4500000000000");
        assertEquals(nullResult, null);
        assertEquals(tooBigResult, "2017-06-09 18:53:4500000000000");
    }
    
    /**
     * Test of completeZonedDateTimeString method, of class TemporalFormatting.
     */
    @Test
    public void testCompleteZonedDateTimeString() {
        System.out.println("testCompleteZonedDateTimeString");
        
        final String dateTimeString = "2017-06-09T18:53:45Z";
        final String expResult = "2017-06-09T18:53:45.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertNotEquals(result, expResult); // the complete zoned date time string currently does not accept the 'Z' and instead requires the offset and region.
    }

    /**
     * Test of completeZonedDateTimeString method, of class TemporalFormatting. Date input.
     */
    @Test
    public void testCompleteZonedDateTimeStringWithDate() {
        System.out.println("testCompleteZonedDateTimeStringWithDate");
        
        final String dateTimeString = "2017-06-09";
        final String expResult = "2017-06-09 00:00:00.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertEquals(result, expResult);
    }

    /**
     * Test of completeZonedDateTimeString method, of class TemporalFormatting. DateTime input
     */
    @Test
    public void testCompleteZonedDateTimeStringWithDateAndTime() {
        System.out.println("testCompleteZonedDateTimeStringWithDateAndTime");
        
        final String dateTimeString = "2017-06-09 18:53";
        final String expResult = "2017-06-09 18:53:00.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertEquals(result, expResult);
    }

    /**
     * Test of completeZonedDateTimeString method, of class TemporalFormatting. DateTime input with seconds
     */
    @Test
    public void testCompleteZonedDateTimeStringWithDateTimeAndSeconds() {
        System.out.println("testCompleteZonedDateTimeStringWithDateTimeAndSeconds");
        
        final String dateTimeString = "2017-06-09 18:53:45";
        final String expResult = "2017-06-09 18:53:45.000 +00:00 [UTC]";
        final String result = TemporalFormatting.completeZonedDateTimeString(dateTimeString);
        assertEquals(result, expResult);
    }
    
    /**
     * Test of completeLocalDateTimeString method, of class TemporalFormatting. Invalid Input 
     */
    @Test
    public void testCompleteLocalDateTimeStringInvalidInput() {
        System.out.println("testCompleteLocalDateTimeStringInvalidInput");
        
        final String nullResult = TemporalFormatting.completeLocalDateTimeString(null);
        final String tooBigResult = TemporalFormatting.completeLocalDateTimeString("2017-06-09 18:53:450000");
        assertEquals(nullResult, null);
        assertEquals(tooBigResult, "2017-06-09 18:53:450000");
    }
    
    /**
     * Test of completeLocalDateTimeString method, of class TemporalFormatting.
     */
    @Test
    public void testCompleteLocalDateTimeString() {
        System.out.println("testCompleteLocalDateTimeString");
        
        final String dateTimeString = "2017-06-09";
        final String expResult = "2017-06-09 00:00:00.000";
        final String result = TemporalFormatting.completeLocalDateTimeString(dateTimeString);
        assertEquals(result, expResult); 
    }
    
    /**
     * Test of completeLocalDateTimeString method, of class TemporalFormatting. DateTime input with seconds
     */
    @Test
    public void testCompleteLocalDateTimeStringWithDateTimeAndSeconds() {
        System.out.println("testCompleteLocalDateTimeStringWithDateTimeAndSeconds");
        
        final String dateTimeString = "2017-06-09 18:53:45";
        final String expResult = "2017-06-09 18:53:45.000";
        final String result = TemporalFormatting.completeLocalDateTimeString(dateTimeString);
        assertEquals(result, expResult); 
    }
    
    /**
     * Test of completeDateString method, of class TemporalFormatting. Invalid Input 
     */
    @Test
    public void testCompleteDateStringInvalidInput() {
        System.out.println("testCompleteDateStringInvalidInput");
        
        final String nullResult = TemporalFormatting.completeDateString(null);
        final String tooBigResult = TemporalFormatting.completeDateString("2017-06-09");
        assertEquals(nullResult, null);
        assertEquals(tooBigResult, "2017-06-09");
    }
    
    /**
     * Test of completeDateString method, of class TemporalFormatting.
     */
    @Test
    public void testCompleteDateString() {
        System.out.println("testCompleteDateString");
        
        final String result1 = TemporalFormatting.completeDateString("");
        final String result2 = TemporalFormatting.completeDateString("2017");
        final String result3 = TemporalFormatting.completeDateString("2017-07");
        assertEquals(result1, "1970-01-01"); 
        assertEquals(result2, "2017-01-01"); 
        assertEquals(result3, "2017-07-01"); 
    }
    
    /**
     * Test of completeTimeString method, of class TemporalFormatting. Invalid Input 
     */
    @Test
    public void testCompleteTimeStringInvalidInput() {
        System.out.println("testCompleteTimeStringInvalidInput");
        
        final String nullResult = TemporalFormatting.completeTimeString(null);
        final String tooBigResult = TemporalFormatting.completeTimeString("18:53:45.000");
        assertEquals(nullResult, null);
        assertEquals(tooBigResult, "18:53:45.000");
    }
    
    /**
     * Test of completeTimeString method, of class TemporalFormatting.
     */
    @Test
    public void testCompleteTimeString() {
        System.out.println("testCompleteTimeString");
        
        final String result1 = TemporalFormatting.completeTimeString("");
        final String result2 = TemporalFormatting.completeTimeString("01");
        final String result3 = TemporalFormatting.completeTimeString("01:23");
        final String result4 = TemporalFormatting.completeTimeString("01:23:45");
        assertEquals(result1, "00:00:00.000"); 
        assertEquals(result2, "01:00:00.000"); 
        assertEquals(result3, "01:23:00.000"); 
        assertEquals(result4, "01:23:45.000"); 
    }

    /**
     * Test of formatAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testFormatAsZonedDateTime() {
        System.out.println("testFormatAsZonedDateTime");
        
        final String result1 = TemporalFormatting.formatAsZonedDateTime(null);
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER.parse("2016-06-11T22:48:48Z");
        final String result2 = TemporalFormatting.formatAsZonedDateTime(accessor);
        assertEquals(result1, null);
        assertEquals(result2, "2016-06-11 22:48:48.000 +00:00");
    }

    /**
     * Test of formatAsZonedDateTime method, of class TemporalFormatting. DateTime input with milliseconds
     */
    @Test
    public void testFormatAsZonedDateTimeWithMilliseconds() {
        System.out.println("testFormatAsZonedDateTimeWithMilliseconds");
        
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_WITH_MILLISECONDS_FORMATTER
                .parse("2016-06-11T22:48:48.000Z");
        final String expResult = "2016-06-11 22:48:48.000 +00:00";
        final String result = TemporalFormatting.formatAsZonedDateTime(accessor);
        assertEquals(result, expResult);
    }
    
    /**
     * Test of formatAsLocalDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testFormatAsLocalDateTime() {
        System.out.println("testFormatAsLocalDateTime");
        
        final String result1 = TemporalFormatting.formatAsLocalDateTime(null);
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER.parse("2016-06-11T22:48:48Z");
        final String result2 = TemporalFormatting.formatAsLocalDateTime(accessor);
        assertEquals(result1, null);
        assertEquals(result2, "2016-06-11 22:48:48.000");
    }

    /**
     * Test of formatAsLocalDateTime method, of class TemporalFormatting. DateTime input with milliseconds
     */
    @Test
    public void testFormatAsLocalDateTimeWithMilliseconds() {
        System.out.println("testFormatAsLocalDateTimeWithMilliseconds");
        
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_WITH_MILLISECONDS_FORMATTER
                .parse("2016-06-11T22:48:48.000Z");
        final String expResult = "2016-06-11 22:48:48.000";
        final String result = TemporalFormatting.formatAsLocalDateTime(accessor);
        assertEquals(result, expResult);
    }
    
    /**
     * Test of formatAsDate method, of class TemporalFormatting.
     */
    @Test
    public void testFormatAsDate() {
        System.out.println("testFormatAsDate");
        
        final String result1 = TemporalFormatting.formatAsDate(null);
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER.parse("2016-06-11T22:48:48Z");
        final String result2 = TemporalFormatting.formatAsDate(accessor);
        assertEquals(result1, null);
        assertEquals(result2, "2016-06-11");
    }
    
    /**
     * Test of formatAsTime method, of class TemporalFormatting.
     */
    @Test
    public void testFormatAsTime() {
        System.out.println("testFormatAsTime");
        
        final String result1 = TemporalFormatting.formatAsTime(null);
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER.parse("2016-06-11T22:48:48Z");
        final String result2 = TemporalFormatting.formatAsTime(accessor);
        assertEquals(result1, null);
        assertEquals(result2, "22:48:48.000");
    }
    
    /**
     * Test of zonedDateTimeFromLong method, of class TemporalFormatting.
     */
    @Test
    public void testZonedDateTimeFromLong() {
        System.out.println("testZonedDateTimeFromLong");
        
        final ZonedDateTime result1 = TemporalFormatting.zonedDateTimeFromLong(0);
        final ZonedDateTime expectedResult1 = ZonedDateTime.parse("1970-01-01T00:00:00+00:00")
                .withZoneSameInstant(TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId());
        assertEquals(result1, expectedResult1);
        
        final ZonedDateTime result2 = TemporalFormatting.zonedDateTimeFromLong(1000000000L);
        final ZonedDateTime expectedResult2 = ZonedDateTime.parse("2001-09-09T01:46:40+00:00")
                .withZoneSameInstant(TimeZone.getTimeZone(ZoneOffset.UTC).toZoneId());
        assertEquals(result2, expectedResult2);
    }
    
    /**
     * Test of zonedDateTimeStringFromLong method, of class TemporalFormatting.
     */
    @Test
    public void testZonedDateTimeStringFromLong() {
        System.out.println("testZonedDateTimeStringFromLong");
        
        final String result1 = TemporalFormatting.zonedDateTimeStringFromLong(0);
        assertEquals(result1, "1970-01-01 00:00:00.000 +00:00");
        
        final String result2 = TemporalFormatting.zonedDateTimeStringFromLong(1000000000L);
        assertEquals(result2, "2001-09-09 01:46:40.000 +00:00");
    }
    
    /**
     * Test of formatWithCustomFormatter method, of class TemporalFormatting.
     */
    @Test
    public void testFormatWithCustomFormatter() {
        System.out.println("testFormatWithCustomFormatter");
        
        final String result1 = TemporalFormatting.formatWithCustomFormatter(MY_DATETIME_FORMATTER3, null);
        final TemporalAccessor accessor = TemporalFormatting.UTC_DATE_TIME_FORMATTER.parse("2016-06-11T22:48:48Z");
        final String result2 = TemporalFormatting.formatWithCustomFormatter(MY_DATETIME_FORMATTER3, accessor);
        assertEquals(result1, null);
        assertEquals(result2, "20160611");
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
        final String result = TemporalFormatting.formatAsZonedDateTime(accessor);
        final String expResult = "2016-06-11 22:48:48.000 +00:00";
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
        final String result = TemporalFormatting.parseAsZonedDateTime(testDateTime, MY_DATETIME_FORMATTER1, LOGGER);
        final String expResult = "2019-04-23 01:16:42.000 +10:00 [Australia/Sydney]";
        assertEquals(result, expResult);
    }

    /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeWithOffset() {
        String testDateTime = "Tue Apr 23 01:16:42 +1000 2019";
        final String result = TemporalFormatting.parseAsZonedDateTime(testDateTime, MY_DATETIME_FORMATTER2, LOGGER);
        final String expResult = "2019-04-23 01:16:42.000 +10:00";
        assertEquals(result, expResult);
    }

    /**
     * Test of parseAsZonedDateTime method, of class TemporalFormatting.
     */
    @Test
    public void testParseDateTimeWithNoZone() {
        String testDateTime = "20191225";
        final String result = TemporalFormatting.parseAsZonedDateTime(testDateTime, MY_DATETIME_FORMATTER3, LOGGER);
        final String expResult = "2019-12-25 00:00:00.000 +00:00";
        assertEquals(result, expResult);
    }
}
