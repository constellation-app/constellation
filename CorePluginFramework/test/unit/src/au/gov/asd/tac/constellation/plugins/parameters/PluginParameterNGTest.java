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
package au.gov.asd.tac.constellation.plugins.parameters;

import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
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
public class PluginParameterNGTest {
    
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
     * Test of setName method, of class PluginParameter.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertEquals(paramSpy.getName(), "my string parameter");
        paramSpy.setName("new name");
        assertEquals(paramSpy.getName(), "new name");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.NAME);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setName("new name");
        assertEquals(paramSpy.getName(), "new name");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.NAME);
    }

    /**
     * Test of setVisible method, of class PluginParameter.
     */
    @Test
    public void testSetVisible() {
        System.out.println("setVisible");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertTrue(paramSpy.isVisible());
        paramSpy.setVisible(false);
        assertFalse(paramSpy.isVisible());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VISIBLE);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setVisible(false);
        assertFalse(paramSpy.isVisible());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VISIBLE);
    }

    /**
     * Test of suppressEvent method, of class PluginParameter.
     */
    @Test
    public void testSuppressEvent() {
        System.out.println("suppressEvent");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        for (final ParameterChange changeType : ParameterChange.values()) {
            assertFalse(stringParam.eventIsSuppressed(changeType));
        }
        
        stringParam.suppressEvent(true, Arrays.asList(ParameterChange.VALUE));
        for (final ParameterChange changeType : ParameterChange.values()) {
            if (changeType == ParameterChange.VALUE) {
                assertTrue(stringParam.eventIsSuppressed(changeType));
            } else {
                assertFalse(stringParam.eventIsSuppressed(changeType));
                
            }
        }
        
        stringParam.suppressEvent(true, Collections.emptyList());
        for (final ParameterChange changeType : ParameterChange.values()) {
            assertTrue(stringParam.eventIsSuppressed(changeType));
        }
    }
    
    /**
     * Test of setEnabled method, of class PluginParameter.
     */
    @Test
    public void testSetEnabled() {
        System.out.println("setEnabled");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertTrue(paramSpy.isEnabled());
        paramSpy.setEnabled(false);
        assertFalse(paramSpy.isEnabled());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ENABLED);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setEnabled(false);
        assertFalse(paramSpy.isEnabled());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ENABLED);
    }

    /**
     * Test of setProperty method, of class PluginParameter.
     */
    @Test
    public void testSetProperty() {
        System.out.println("setProperty");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");     
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertNull(paramSpy.getProperty("test"));
        
        paramSpy.setProperty("test", "value");
        assertEquals(paramSpy.getProperty("test"), "value");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.PROPERTY);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setProperty("test", "value");
        assertEquals(paramSpy.getProperty("test"), "value");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.PROPERTY);
        
        paramSpy.setProperty("test", null);
        assertNull(paramSpy.getProperty("test"));
        verify(paramSpy, times(2)).fireChangeEvent(ParameterChange.PROPERTY);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setProperty("test", null);
        assertNull(paramSpy.getProperty("test"));
        verify(paramSpy, times(2)).fireChangeEvent(ParameterChange.PROPERTY);
    }

    /**
     * Test of setDescription method, of class PluginParameter.
     */
    @Test
    public void testSetDescription() {
        System.out.println("setDescription");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertNull(paramSpy.getDescription());
        paramSpy.setDescription("a description");
        assertEquals(paramSpy.getDescription(), "a description");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.DESCRIPTION);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setDescription("a description");
        assertEquals(paramSpy.getDescription(), "a description");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.DESCRIPTION);
    }

    /**
     * Test of setIcon method, of class PluginParameter.
     */
    @Test
    public void testSetIcon() {
        System.out.println("setIcon");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertNull(paramSpy.getIcon());
        paramSpy.setIcon("an icon");
        assertEquals(paramSpy.getIcon(), "an icon");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ICON);
        // this "change" shouldn't do anything (no additional events fired)
        paramSpy.setIcon("an icon");
        assertEquals(paramSpy.getIcon(), "an icon");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ICON);
    }
    
    /**
     * Test of setHelpID method, of class PluginParameter.
     */
    @Test
    public void testSetHelpID() {
        System.out.println("setHelpID");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        assertNull(stringParam.getHelpID());
        stringParam.setHelpID("some help");
        assertEquals(stringParam.getHelpID(), "some help");
    }

    /**
     * Test of loadToRecentValue method, of class PluginParameter.
     */
    @Test
    public void testLoadToRecentValue() {
        System.out.println("loadToRecentValue");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        assertEquals(stringParam.getStringValue(), "test");
        
        // no recent value stored so load should fail
        assertFalse(stringParam.loadToRecentValue());
        assertEquals(stringParam.getStringValue(), "test");
        
        stringParam.setStringValue("another value");
        // despite value change, because no value has been stored, load should still fail
        assertFalse(stringParam.loadToRecentValue());
        assertEquals(stringParam.getStringValue(), "another value");
        
        stringParam.storeRecentValue();
        stringParam.setStringValue("yet another value");
        // value has finally been stored so this should succeed
        assertTrue(stringParam.loadToRecentValue());
        assertEquals(stringParam.getStringValue(), "another value");
        
    }

    /**
     * Test of setError method, of class PluginParameter.
     */
    @Test
    public void testSetError() {
        System.out.println("setError");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> paramSpy = spy(stringParam);
        assertNull(paramSpy.getError());
        paramSpy.setError("an error");
        assertEquals(paramSpy.getError(), "an error");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ERROR);
        // error changes but since the error state hasn't, no change event fired
        paramSpy.setError("another error");
        assertEquals(paramSpy.getError(), "another error");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ERROR);
        // error is same so nothing changes
        paramSpy.setError("another error");
        assertEquals(paramSpy.getError(), "another error");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.ERROR);
        // error status does change so change event fired
        paramSpy.setError(null);
        assertNull(paramSpy.getError());
        verify(paramSpy, times(2)).fireChangeEvent(ParameterChange.ERROR);
    }

    /**
     * Test of copy method, of class PluginParameter.
     */
    @Test
    public void testCopy() {
        System.out.println("copy");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        final PluginParameter<StringParameterValue> stringParamCopy = stringParam.copy();
        assertEquals(stringParam.getId(), stringParamCopy.getId());
        assertEquals(stringParam.getType(), stringParamCopy.getType());
        assertEquals(stringParam.getParameterValue(), stringParamCopy.getParameterValue());
        assertEquals(stringParam.getDescription(), stringParamCopy.getDescription());
        stringParam.setDescription("a description");
        assertNotEquals(stringParam.getDescription(), stringParamCopy.getDescription());
    }

    /**
     * Test of setObjectValue method, of class PluginParameter.
     */
    @Test
    public void testSetObjectValue() {
        System.out.println("setObjectValue");
        
        final PluginParameter<IntegerParameterValue> integerParam = new PluginParameter<>(new IntegerParameterValue(), new IntegerParameterType(), "my integer parameter");
        final PluginParameter<IntegerParameterValue> paramSpy = spy(integerParam);
        assertEquals(paramSpy.getParameterValue().get(), 0);
        
        paramSpy.setObjectValue(1);
        assertEquals(paramSpy.getObjectValue(), 1);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        
        // no change so set wasn't successful (and therefore no event fired)
        paramSpy.setObjectValue(1);
        assertEquals(paramSpy.getObjectValue(), 1);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of setStringValue method, of class PluginParameter.
     */
    @Test
    public void testSetStringValue() {
        System.out.println("setStringValue");
        
        final PluginParameter<IntegerParameterValue> integerParam = new PluginParameter<>(new IntegerParameterValue(), new IntegerParameterType(), "my integer parameter");
        final PluginParameter<IntegerParameterValue> paramSpy = spy(integerParam);
        assertEquals(paramSpy.getParameterValue().get(), 0);
        
        paramSpy.setStringValue("1");
        assertEquals(paramSpy.getStringValue(), "1");
        assertNull(paramSpy.getError());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        
        // no change so set wasn't successful (and therefore no event fired)
        paramSpy.setStringValue("1");
        assertEquals(paramSpy.getStringValue(), "1");
        assertNull(paramSpy.getError());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        
        // invalid integer passes so value remains same with error noted
        paramSpy.setStringValue("Not an integer");
        assertEquals(paramSpy.getStringValue(), "1");
        assertEquals(paramSpy.getError(), "Not a valid integer");
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }
    
    /**
     * Test of setBooleanValue method, of class PluginParameter.
     */
    @Test
    public void testSetBooleanValue() {
        System.out.println("setBooleanValue");
        
        final PluginParameter<BooleanParameterValue> booleanParam = new PluginParameter<>(new BooleanParameterValue(), new BooleanParameterType(), "my boolean parameter");
        final PluginParameter<BooleanParameterValue> paramSpy = spy(booleanParam);
        assertFalse(paramSpy.getBooleanValue());
        paramSpy.setBooleanValue(true);
        assertTrue(paramSpy.getBooleanValue());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setBooleanValue(true);
        assertTrue(paramSpy.getBooleanValue());
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of setColorValue method, of class PluginParameter.
     */
    @Test
    public void testSetColorValue() {
        System.out.println("setColorValue");
        
        final PluginParameter<ColorParameterValue> colorParam = new PluginParameter<>(new ColorParameterValue(), new ColorParameterType(), "my color parameter");
        final PluginParameter<ColorParameterValue> paramSpy = spy(colorParam);
        assertEquals(paramSpy.getColorValue(), ConstellationColor.CLOUDS);
        paramSpy.setColorValue(ConstellationColor.BANANA);
        assertEquals(paramSpy.getColorValue(), ConstellationColor.BANANA);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setColorValue(ConstellationColor.BANANA);
        assertEquals(paramSpy.getColorValue(), ConstellationColor.BANANA);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }
    
    /**
     * Test of setDateTimeRangeValue method, of class PluginParameter.
     */
    @Test
    public void testSetDateTimeRangeValue() {
        System.out.println("setDateTimeRangeValue");
        
        final PluginParameter<DateTimeRangeParameterValue> datetimeRangeParam = new PluginParameter<>(new DateTimeRangeParameterValue(), new DateTimeRangeParameterType(), "my datetime range parameter");
        final PluginParameter<DateTimeRangeParameterValue> paramSpy = spy(datetimeRangeParam);
        assertEquals(paramSpy.getDateTimeRangeValue(), new DateTimeRange(Period.ofDays(1), ZoneId.of("UTC")));
        final DateTimeRange dtr = new DateTimeRange(Period.of(0, 0, 1), ZoneId.of("UTC"));
        paramSpy.setDateTimeRangeValue(dtr);
        assertEquals(paramSpy.getDateTimeRangeValue(), dtr);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setDateTimeRangeValue(dtr);
        assertEquals(paramSpy.getDateTimeRangeValue(), dtr);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of setIntegerValue method, of class PluginParameter.
     */
    @Test
    public void testSetIntegerValue() {
        System.out.println("setIntegerValue");
        
        final PluginParameter<IntegerParameterValue> integerParam = new PluginParameter<>(new IntegerParameterValue(), new IntegerParameterType(), "my integer parameter");
        final PluginParameter<IntegerParameterValue> paramSpy = spy(integerParam);
        assertEquals(paramSpy.getIntegerValue(), 0);
        paramSpy.setIntegerValue(1);
        assertEquals(paramSpy.getIntegerValue(), 1);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setIntegerValue(1);
        assertEquals(paramSpy.getIntegerValue(), 1);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of setFloatValue method, of class PluginParameter.
     */
    @Test
    public void testSetFloatValue() {
        System.out.println("setFloatValue");
        
        final PluginParameter<FloatParameterValue> floatParam = new PluginParameter<>(new FloatParameterValue(), new FloatParameterType(), "my float parameter");
        final PluginParameter<FloatParameterValue> paramSpy = spy(floatParam);
        assertEquals(paramSpy.getFloatValue(), 0F);
        paramSpy.setFloatValue(1F);
        assertEquals(paramSpy.getFloatValue(), 1F);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setFloatValue(1F);
        assertEquals(paramSpy.getFloatValue(), 1F);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of setLocalDateValue method, of class PluginParameter.
     */
    @Test
    public void testSetLocalDateValue() {
        System.out.println("setLocalDateValue");
        
        final PluginParameter<LocalDateParameterValue> localDateParam = new PluginParameter<>(new LocalDateParameterValue(), new LocalDateParameterType(), "my local date parameter");
        final PluginParameter<LocalDateParameterValue> paramSpy = spy(localDateParam);
        final LocalDate ld = LocalDate.ofYearDay(1970, 2);
        assertEquals(paramSpy.getLocalDateValue(), LocalDate.now());
        paramSpy.setLocalDateValue(ld);
        assertEquals(paramSpy.getLocalDateValue(), ld);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setLocalDateValue(ld);
        assertEquals(paramSpy.getLocalDateValue(), ld);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of setNumberValue method, of class PluginParameter.
     */
    @Test
    public void testSetNumberValue() {
        System.out.println("setNumberValue");
        
        final PluginParameter<IntegerParameterValue> numberParam = new PluginParameter<>(new IntegerParameterValue(), new IntegerParameterType(), "my number parameter");
        final PluginParameter<IntegerParameterValue> paramSpy = spy(numberParam);
        assertEquals(paramSpy.getNumberValue(), 0);
        paramSpy.setNumberValue(1);
        assertEquals(paramSpy.getNumberValue(), 1);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
        // value not changed, no change event triggered
        paramSpy.setNumberValue(1);
        assertEquals(paramSpy.getNumberValue(), 1);
        verify(paramSpy, times(1)).fireChangeEvent(ParameterChange.VALUE);
    }

    /**
     * Test of buildId method, of class PluginParameter.
     */
    @Test
    public void testBuildId() {
        System.out.println("buildId");
        
        // obviously note plugin classes but working within the bounds of the classes we can access
        assertEquals(PluginParameter.buildId(PluginParameter.class, "test"), "PluginParameter.test");
        assertEquals(PluginParameter.buildId(PluginParameter.class, "anothertest"), "PluginParameter.anothertest");
        assertEquals(PluginParameter.buildId(ConstellationColor.class, "test"), "ConstellationColor.test");
    }

    /**
     * Test of setRequestBodyExampleJson method, of class PluginParameter.
     */
    @Test
    public void testSetRequestBodyExampleJson() {
        System.out.println("setRequestBodyExampleJson");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        assertNull(stringParam.getRequestBodyExampleJson());
        stringParam.setRequestBodyExampleJson("json request body example");
        assertEquals(stringParam.getRequestBodyExampleJson(), "json request body example");
        // this "change" shouldn't do anything
        stringParam.setRequestBodyExampleJson("json request body example");
        assertEquals(stringParam.getRequestBodyExampleJson(), "json request body example");
    }

    /**
     * Test of setRequired method, of class PluginParameter.
     */
    @Test
    public void testSetRequired() {
        System.out.println("setRequired");
        
        final PluginParameter<StringParameterValue> stringParam = new PluginParameter<>(new StringParameterValue("test"), new StringParameterType(), "my string parameter");
        assertFalse(stringParam.isRequired());
        stringParam.setRequired(true);
        assertTrue(stringParam.isRequired());
    }    
}