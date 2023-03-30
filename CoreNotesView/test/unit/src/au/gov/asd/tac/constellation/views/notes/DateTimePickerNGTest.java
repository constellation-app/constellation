/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.views.notes.utilities.DateTimePicker;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class DateTimePickerNGTest {

    private static final Logger LOGGER = Logger.getLogger(DateTimePickerNGTest.class.getName());


    public DateTimePickerNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }


    /**
     * Test of setCurrentDateTime method, of class DateTimePicker.
     */
    @Test
    public void testSetCurrentDateTime() {
        System.out.println("setCurrentDateTime");
        final ZoneId zone = ZoneId.of("Australia/Perth");
        final ZonedDateTime currentTime = ZonedDateTime.now(zone);
        final DateTimePicker instance = new DateTimePicker(true);
        instance.setCurrentDateTime(zone);

        assertEquals(currentTime.getSecond() == instance.getCurrentDateTime().getSecond(), true);
        assertEquals(currentTime.getMinute() == instance.getCurrentDateTime().getMinute(), true);
        assertEquals(currentTime.getHour() == instance.getCurrentDateTime().getHour(), true);
    }

    /**
     * Test of convertCurrentDateTime method, of class DateTimePicker.
     */
    @Test
    public void testConvertCurrentDateTime() {
        System.out.println("convertCurrentDateTime");
        final ZoneId convertTo = ZoneId.of("Australia/Perth");
        final ZoneId currentZone = ZoneId.of("Australia/Perth");
        final DateTimePicker instance = new DateTimePicker(true);

        instance.setCurrentDateTime(currentZone);

        instance.convertCurrentDateTime(convertTo);

        ZonedDateTime currentTime = ZonedDateTime.now(convertTo);

        assertEquals(currentTime.getSecond() == instance.getCurrentDateTime().getSecond(), true);
        assertEquals(currentTime.getMinute() == instance.getCurrentDateTime().getMinute(), true);
        assertEquals(currentTime.getHour() == instance.getCurrentDateTime().getHour(), true);
    }

    /**
     * Test of isActive method, of class DateTimePicker.
     */
    @Test
    public void testIsActive() {
        System.out.println("isActive");
        final DateTimePicker instance = new DateTimePicker(true);
        final boolean expResult = false;
        final boolean result = instance.isActive();
        assertEquals(result, expResult);
        instance.setActive(true);
        assertEquals(instance.isActive(), true);
    }

    /**
     * Test of setActive method, of class DateTimePicker.
     */
    @Test
    public void testSetActive() {
        System.out.println("setActive");
        final boolean active = true;
        final DateTimePicker instance = new DateTimePicker(true);
        instance.setActive(active);

        assertEquals(instance.isActive(), true);
    }

    /**
     * Test of getCurrentDateTime method, of class DateTimePicker.
     */
    @Test
    public void testGetCurrentDateTime() {
        System.out.println("getCurrentDateTime");
        final DateTimePicker instance = new DateTimePicker(true);
        instance.setCurrentDateTime(ZoneId.of("Australia/Adelaide"));
        final ZonedDateTime expResult = ZonedDateTime.now(ZoneId.of("Australia/Adelaide"));
        final ZonedDateTime result = instance.getCurrentDateTime();

        assertEquals(expResult.getSecond() == result.getSecond(), true);
        assertEquals(expResult.getMinute() == result.getMinute(), true);
        assertEquals(expResult.getHour() == result.getHour(), true);
    }

    /**
     * Test of getZoneId method, of class DateTimePicker.
     */
    @Test
    public void testGetZoneId() {
        System.out.println("getZoneId");
        final DateTimePicker instance = new DateTimePicker(true);
        final ZoneId zone = ZoneId.of("Australia/Perth");
        instance.setCurrentDateTime(zone);

        final ZoneId result = instance.getZoneId();
        assertEquals(result, zone);

    }

}
