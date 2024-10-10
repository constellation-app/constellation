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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Toggle;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class DateTimeRangeInputPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(DateTimeRangeInputPaneNGTest.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    /**
     * Test of setPeriod method, of class DateTimeRangeInputPane.
     */
    @Test
    public void testSetPeriod() {
        System.out.println("testsetPeriod");

        final PluginParameter parameter = new PluginParameter<>(new DateTimeRangeParameterValue(), new DateTimeRangeParameterType(), DateTimeRangeParameterType.ID);
        final DateTimeRangeInputPane instance = new DateTimeRangeInputPane(parameter);

        final ZoneId zi = ZoneId.systemDefault();

        final int[] daysToTest = {1, 2, 3, 4, 7, 14}; // Matches options on pane's relativeButtons
        for (int day : daysToTest) {
            testSetPeriodHelper(0, day, zi, instance);
        }

        final int[] monthsToTest = {1, 3, 6, 12, 24}; // Matches options on pane's relativeButtons
        for (final int month : monthsToTest) {
            testSetPeriodHelper(month, 0, zi, instance);
        }
    }

    private void testSetPeriodHelper(final int month, final int day, final ZoneId zi, final DateTimeRangeInputPane instance) {
        final Period period = Period.of(0, month, day);

        final ZonedDateTime zdt1 = ZonedDateTime.now(zi);
        final ZonedDateTime zdt0 = zdt1.minus(period);
        instance.setPeriod(period, zi);

        assertEquals(instance.getDatePickers().get(0).getValue(), zdt0.toLocalDate());
        assertEquals(instance.getDatePickers().get(1).getValue(), zdt1.toLocalDate());
    }

    /**
     * Test of setAbsolute method, of class DateTimeRangeInputPane.
     */
    @Test
    public void testSetAbsolute() {
        System.out.println("setAbsolute");

        final PluginParameter parameter = new PluginParameter<>(new DateTimeRangeParameterValue(), new DateTimeRangeParameterType(), DateTimeRangeParameterType.ID);
        final DateTimeRangeInputPane instance = new DateTimeRangeInputPane(parameter);

        // Create zoned date times one month apart
        final Period period = Period.ofMonths(1);
        final ZoneId zi = ZoneId.systemDefault();

        final ZonedDateTime zdt1 = ZonedDateTime.now(zi);
        final ZonedDateTime zdt0 = zdt1.minus(period);

        instance.setAbsolute(zdt0, zdt1);

        // Assert individual date pickers have correct value
        assertEquals(instance.getDatePickers().get(0).getValue(), zdt0.toLocalDate());
        assertEquals(instance.getDatePickers().get(1).getValue(), zdt1.toLocalDate());

        // Assert results of getAbsoluteRange match too
        final ZonedDateTime[] result = instance.getAbsoluteRange(zi);
        // Converting to local date as some precision in the milliseconds is lost 
        assertEquals(result[0].toLocalDate(), zdt0.toLocalDate());
        assertEquals(result[1].toLocalDate(), zdt1.toLocalDate());
    }

    /**
     * Test of clearRangeButtons method, of class DateTimeRangeInputPane.
     */
    @Test
    public void testClearRangeButtons() {
        System.out.println("clearRangeButtons");

        final PluginParameter parameter = new PluginParameter<>(new DateTimeRangeParameterValue(), new DateTimeRangeParameterType(), DateTimeRangeParameterType.ID);
        final DateTimeRangeInputPane instance = new DateTimeRangeInputPane(parameter);

        instance.clearRangeButtons();
        // Assert all toggles are false now
        for (final Toggle button : instance.getDateRangeGroup().getToggles()) {
            assertFalse(button.isSelected());
        }
    }

    @Test
    public void testDatePickerFuncitions() {
        System.out.println("datePickerFuncitions");

        final PluginParameter parameter = new PluginParameter<>(new DateTimeRangeParameterValue(), new DateTimeRangeParameterType(), DateTimeRangeParameterType.ID);
        final DateTimeRangeInputPane instance = new DateTimeRangeInputPane(parameter);

        final DatePicker datePicker = instance.getDatePickers().get(0);

        final String validDate = "2000-01-01";
        final String emptyString = "";
        final String invalideDate = "invalid";

        // Assert valid set value is actually set
        final LocalDate expected = LocalDate.MAX;
        datePicker.setValue(expected);
        assertEquals(datePicker.getValue(), expected);

        // Assert null value is set
        datePicker.setValue(null);
        assertNull(datePicker.getValue());

        // Assert when valid text value is set, then empty, vlaue remains as original valid text
        datePicker.getEditor().setText(validDate);
        assertEquals(datePicker.getEditor().getText(), validDate);
        datePicker.getEditor().setText("");
        assertEquals(datePicker.getEditor().getText(), validDate);

        // Assert valid date string is correclty converted to local date
        final LocalDate expectedDate = LocalDate.parse(validDate, DATE_FORMATTER);
        assertEquals(datePicker.getConverter().fromString(validDate), expectedDate);

        // Assert null is returned from empty and invalid date string
        assertNull(datePicker.getConverter().fromString(emptyString));
        assertNull(datePicker.getConverter().fromString(invalideDate));
    }
}
