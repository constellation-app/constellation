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
package au.gov.asd.tac.constellation.graph.attribute;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Zoned DateTime Attribute Description Test.
 *
 * @author algol
 */
public class ZonedDateTimeAttributeDescriptionNGTest {

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

    @Test
    public void testParseConstellationStringWithZone() {
        final ZonedDateTimeAttributeDescription instance = new ZonedDateTimeAttributeDescription();
        final String value = "2016-11-03 23:16:27.000 +00:00 [UTC]";
        final ZonedDateTime expResult = ZonedDateTime.of(2016, 11, 3, 23, 16, 27, 0, ZoneId.of("UTC"));
        final ZonedDateTime result = instance.convertFromString(value);
        assertEquals(result, expResult);
    }

    @Test
    public void testParseConstellationStringWithoutZone() {
        final ZonedDateTimeAttributeDescription instance = new ZonedDateTimeAttributeDescription();
        final String value = "2016-11-03 23:16:27.000";
        final ZonedDateTime expResult = ZonedDateTime.of(2016, 11, 3, 23, 16, 27, 0, ZoneId.of("UTC"));
        final ZonedDateTime result = instance.convertFromString(value);
        assertEquals(result, expResult);
    }

    @Test
    public void testParseIsoZ() {
        final ZonedDateTimeAttributeDescription instance = new ZonedDateTimeAttributeDescription();
        final String value = "2016-11-03T23:16:27.000Z";
        final ZonedDateTime expResult = ZonedDateTime.of(2016, 11, 3, 23, 16, 27, 0, ZoneId.of("UTC"));
        final ZonedDateTime result = instance.convertFromString(value);
        assertEquals(result, expResult);
    }

    @Test
    public void testParseIsoZWithSpace() {
        final ZonedDateTimeAttributeDescription instance = new ZonedDateTimeAttributeDescription();
        final String value = "2016-11-03 23:16:27.000Z";
        final ZonedDateTime expResult = ZonedDateTime.of(2016, 11, 3, 23, 16, 27, 0, ZoneId.of("UTC"));
        final ZonedDateTime result = instance.convertFromString(value);
        assertEquals(result, expResult);
    }

    @Test
    public void testParseIsoZoneLetterIgnored() {
        final ZonedDateTimeAttributeDescription instance = new ZonedDateTimeAttributeDescription();
        final String value = "2016-11-03T23:16:27.000A";
        final ZonedDateTime expResult = ZonedDateTime.of(2016, 11, 3, 23, 16, 27, 0, ZoneId.of("UTC"));
        final ZonedDateTime result = instance.convertFromString(value);
        assertEquals(result, expResult);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParseWithInvalidDate() {
        final ZonedDateTimeAttributeDescription instance = new ZonedDateTimeAttributeDescription();
        final String value = "0000-00-00T00:00:00.000";
        instance.convertFromString(value);
    }
}
