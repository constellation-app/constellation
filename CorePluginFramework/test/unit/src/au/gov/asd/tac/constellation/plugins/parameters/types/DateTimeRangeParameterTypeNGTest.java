/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import java.time.Period;
import java.time.ZoneId;
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
public class DateTimeRangeParameterTypeNGTest {
    
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
     * Test of build method, of class DateTimeRangeParameterType.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        
        final PluginParameter<DateTimeRangeParameterValue> dateTimeRangeParam = DateTimeRangeParameterType.build("My Datetime");
        
        assertEquals(dateTimeRangeParam.getParameterValue(), new DateTimeRangeParameterValue());
        assertEquals(dateTimeRangeParam.getId(), "My Datetime");
        assertEquals(dateTimeRangeParam.getType().getId(), "datetimerange");
    }

    /**
     * Test of validateString method, of class DateTimeRangeParameterType.
     */
    @Test
    public void testValidateString() {
        System.out.println("validateString");
        
        final DateTimeRangeParameterType instance = new DateTimeRangeParameterType();
        final PluginParameter<DateTimeRangeParameterValue> dateTimeRangeParam = DateTimeRangeParameterType.build("My Datetime");
        assertNull(instance.validateString(dateTimeRangeParam, "1970-01-02T00:00:00Z[UTC];1970-01-03T00:00:00Z[UTC]"));
        assertNull(instance.validateString(dateTimeRangeParam, "P1Y"));
    }
    
    /**
     * Test of set method, of class DateTimeRangeParameterValue.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue();
        // the default range
        assertEquals(dateTimeRangeValue.get(), new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC")));
        
        assertTrue(dateTimeRangeValue.set(DateTimeRange.parse("P1Y")));
        assertEquals(dateTimeRangeValue.get(), new DateTimeRange(Period.of(1, 0, 0), ZoneId.of("UTC")));
        // value attempting to set is the same
        assertFalse(dateTimeRangeValue.set(DateTimeRange.parse("P1Y")));
        assertEquals(dateTimeRangeValue.get(), new DateTimeRange(Period.of(1, 0, 0), ZoneId.of("UTC")));
    }
    
    /**
     * Test of setStringValue method, of class DateTimeRangeParameterValue.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue();
        // the default range
        assertEquals(dateTimeRangeValue.get(), new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC")));
        
        assertTrue(dateTimeRangeValue.setStringValue("P1Y"));
        assertEquals(dateTimeRangeValue.get(), new DateTimeRange(Period.of(1, 0, 0), ZoneId.of("UTC")));
        assertTrue(dateTimeRangeValue.setStringValue(""));
        // reset to default
        assertEquals(dateTimeRangeValue.get(), new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC")));
    }
    
    /**
     * Test of setObjectValue method, of class DateTimeRangeParameterValue.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue();
        // the default range
        assertEquals(dateTimeRangeValue.getObjectValue(), new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC")));
        
        assertTrue(dateTimeRangeValue.setObjectValue(DateTimeRange.parse("P1Y")));
        assertEquals(dateTimeRangeValue.getObjectValue(), new DateTimeRange(Period.of(1, 0, 0), ZoneId.of("UTC")));
        assertTrue(dateTimeRangeValue.setObjectValue(null));
        // reset to default
        assertEquals(dateTimeRangeValue.getObjectValue(), new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC")));
    }
    
    /**
     * Test of setObjectValue method, of class DateTimeRangeParameterValue. Unexpected Type
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Unexpected class class java.lang.Boolean")
    public void testSetObjectValueUnexpectedType() {
        System.out.println("setObjectValueUnexpectedType");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue();
        dateTimeRangeValue.setObjectValue(true);
    }
    
    /**
     * Test of createCopy method, of class DateTimeRangeParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue(DateTimeRange.parse("P1Y"));
        final ParameterValue dateTimeRangeCopy = dateTimeRangeValue.createCopy();
        assertTrue(dateTimeRangeValue.equals(dateTimeRangeCopy));
        
        dateTimeRangeValue.setObjectValue(null);
        assertFalse(dateTimeRangeValue.equals(dateTimeRangeCopy));
    }
    
    /**
     * Test of equals method, of class DateTimeRangeParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue();
        dateTimeRangeValue.setStringValue("P1Y");
        final DateTimeRangeParameterValue comp1 = new DateTimeRangeParameterValue();
        final DateTimeRangeParameterValue comp2 = new DateTimeRangeParameterValue(DateTimeRange.parse("P1Y"));
        
        assertFalse(dateTimeRangeValue.equals(null));
        assertFalse(dateTimeRangeValue.equals(true));
        assertFalse(dateTimeRangeValue.equals(comp1));
        assertTrue(dateTimeRangeValue.equals(comp2));
    }
    
    /**
     * Test of toString method, of class DateTimeRangeParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        
        final DateTimeRangeParameterValue dateTimeRangeValue = new DateTimeRangeParameterValue();
        assertEquals(dateTimeRangeValue.toString(), "");
        dateTimeRangeValue.setStringValue("P1Y");
        assertEquals(dateTimeRangeValue.toString(), "P1Y UTC");
    }
}
