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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class LocalDateParameterTypeNGTest {
    
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
     * Test of build method, of class LocalDateParameterType. One Parameter version
     */
    @Test
    public void testBuildOneParameter() {
        System.out.println("buildOneParameter");
        
        final PluginParameter<LocalDateParameterValue> localDateParam = LocalDateParameterType.build("My Local Date");
        
        assertEquals(localDateParam.getParameterValue(), new LocalDateParameterValue());
        assertEquals(localDateParam.getId(), "My Local Date");
        assertEquals(localDateParam.getType().getId(), "localdate");
    }

    /**
     * Test of build method, of class LocalDateParameterType. Two Parameter version
     */
    @Test
    public void testBuildTwoParameters() {
        System.out.println("buildTwoParameters");
        
        final LocalDateParameterValue localDateValue = new LocalDateParameterValue(LocalDate.ofYearDay(1970, 1));
        final PluginParameter<LocalDateParameterValue> localDateParam = LocalDateParameterType.build("My Local Date", localDateValue);
        
        assertEquals(localDateParam.getParameterValue(), localDateValue);
        assertEquals(localDateParam.getId(), "My Local Date");
        assertEquals(localDateParam.getType().getId(), "localdate");
    }

    /**
     * Test of toDate method, of class LocalDateParameterType.
     */
    @Test
    public void testToDate() {
        System.out.println("toDate");
        
        final Date date = LocalDateParameterType.toDate(LocalDate.ofYearDay(1970, 2));
        assertEquals(date.getTime(), 24 * 60 * 60 * 1000);
    }

    /**
     * Test of toCalendar method, of class LocalDateParameterType.
     */
    @Test
    public void testToCalendar() {
        System.out.println("toCalendar");
        
        final Calendar calendar = LocalDateParameterType.toCalendar(LocalDate.ofYearDay(1970, 2));
        assertEquals(calendar.getTimeInMillis(), 24 * 60 * 60 * 1000);
    }
    
    /**
     * Test of set method, of class LocalDateParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        
        final LocalDateParameterValue localDateValue = new LocalDateParameterValue();
        // the default value
        assertEquals(localDateValue.get(), LocalDate.now());
        
        assertTrue(localDateValue.set(LocalDate.ofYearDay(1970, 2)));
        assertEquals(localDateValue.get(), LocalDate.of(1970, 1, 2));
        // value attempting to set is the same
        assertFalse(localDateValue.set(LocalDate.ofYearDay(1970, 2)));
        assertEquals(localDateValue.get(), LocalDate.of(1970, 1, 2));
    }
    
    /**
     * Test of validateString method, of class LocalDateParameterValue.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");
        
        final LocalDateParameterValue instance = new LocalDateParameterValue();
        assertNull(instance.validateString("1970-01-02"));
        assertEquals(instance.validateString("not a date"), "Text 'not a date' could not be parsed at index 0");
    }
    
    /**
     * Test of setStringValue method, of class LocalDateParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final LocalDateParameterValue localDateValue = new LocalDateParameterValue();
        // the default value
        assertEquals(localDateValue.get(), LocalDate.now());
        
        assertTrue(localDateValue.setStringValue("1970-01-02"));
        assertEquals(localDateValue.get(), LocalDate.of(1970, 1, 2));
        // reset to default
        assertTrue(localDateValue.setStringValue(""));
        assertEquals(localDateValue.get(), LocalDate.now());
    }
    
    /**
     * Test of setObjectValue method, of class LocalDateParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final LocalDateParameterValue localDateValue = new LocalDateParameterValue();
        // the default value
        assertEquals(localDateValue.getObjectValue(), LocalDate.now());
        
        assertTrue(localDateValue.setObjectValue(LocalDate.ofYearDay(1970, 2)));
        assertEquals(localDateValue.getObjectValue(), LocalDate.of(1970, 1, 2));
        assertTrue(localDateValue.setObjectValue(null));
        // reset to default
        assertEquals(localDateValue.getObjectValue(), LocalDate.now());
    }
    
    /**
     * Test of setObjectValue method, of class LocalDateParameterValue. Unexpected Type
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Unexpected class class java.lang.Boolean")
    public void testSetObjectValueUnexpectedType() {
        System.out.println("setObjectValueUnexpectedType");
        
        final LocalDateParameterValue localDateValue = new LocalDateParameterValue();
        localDateValue.setObjectValue(true);
    }
    
    /**
     * Test of createCopy method, of class LocalDateParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final LocalDateParameterValue localDateValueValue = new LocalDateParameterValue(LocalDate.ofYearDay(1970, 2));
        final ParameterValue localDateCopy = localDateValueValue.createCopy();
        assertTrue(localDateValueValue.equals(localDateCopy));
        
        localDateValueValue.setObjectValue(null);
        assertFalse(localDateValueValue.equals(localDateCopy));
    }
    
    /**
     * Test of equals method, of class LocalDateParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final LocalDateParameterValue dateTimeRangeValue = new LocalDateParameterValue();
        dateTimeRangeValue.setStringValue("1970-01-02");
        final LocalDateParameterValue comp1 = new LocalDateParameterValue();
        final LocalDateParameterValue comp2 = new LocalDateParameterValue(LocalDate.ofYearDay(1970, 2));
        
        assertFalse(dateTimeRangeValue.equals(null));
        assertFalse(dateTimeRangeValue.equals(true));
        assertFalse(dateTimeRangeValue.equals(comp1));
        assertTrue(dateTimeRangeValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class LocalDateParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final LocalDateParameterValue localDateValue = new LocalDateParameterValue();
        assertEquals(localDateValue.toString(), "");
        localDateValue.setObjectValue(LocalDate.ofYearDay(1970, 2));
        assertEquals(localDateValue.toString(), "1970-01-02");
    }
}
