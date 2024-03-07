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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.templates.FilterPlugin.FilterType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.FilterPlugin.FilterTypeParameterValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Filter Plugin Test
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class FilterPluginNGTest extends ConstellationTest {

    import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class FilterPluginImpl extends FilterPlugin {

        @Override
        protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
            // intentionally left blank
        }
    }

    /**
     * Test of createParameters method, of class FilterPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        final FilterPlugin instance = new FilterPluginImpl();
        final PluginParameters result = instance.createParameters();

        assertTrue(result.getParameters().containsKey(FilterPluginImpl.FILTER_TYPE_PARAMETER_ID));
    }

    /**
     * Test of values method, of class FilterType.
     */
    @Test
    public void testValues() {
        System.out.println("values");

        final FilterType[] expResult = new FilterType[3];
        expResult[0] = FilterType.REMOVE_FILTER;
        expResult[1] = FilterType.DESELECT_FILTER;
        expResult[2] = FilterType.SELECT_FILTER;

        final FilterType[] result = FilterType.values();
        assertEquals(result, expResult);
    }

    /**
     * Test of valueOf method, of class FilterType.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");

        final String string = "REMOVE_FILTER";
        final FilterType expResult = FilterType.REMOVE_FILTER;
        final FilterType result = FilterType.valueOf(string);

        assertEquals(result, expResult);
    }

    /**
     * Test of getFilterTypeName method, of class FilterType.
     */
    @Test
    public void testGetFilterTypeName() {
        System.out.println("getFilterTypeName");

        final FilterType instance = FilterType.REMOVE_FILTER;
        final String expResult = "Remove";
        final String result = instance.getFilterTypeName();

        assertEquals(result, expResult);
    }

    /**
     * Test of getFilterType method, of class FilterTypeParameterValue.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");

        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final FilterType expResult = null;
        final FilterType result = instance.getFilterType();

        assertEquals(result, expResult);
    }

    /**
     * Test of validateString method, of class FilterTypeParameterValue.
     */
    @Test
    public void testValidateStringWhenValid() {
        System.out.println("validateStringWhenValid");

        final String s = "Remove";
        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final String expResult = null;
        final String result = instance.validateString(s);

        assertEquals(result, expResult);
    }

    /**
     * Test of validateString method, of class FilterTypeParameterValue.
     */
    @Test
    public void testValidateStringWhenInvalid() {
        System.out.println("validateStringWhenInvalid");

        final String s = "Foo";
        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final String expResult = FilterPlugin.FILTER_TYPE_VALUE_PROVIDED_DOES_NOT_MATCH;
        final String result = instance.validateString(s);

        assertEquals(result, expResult);
    }

    /**
     * Test of setStringValue method, of class FilterTypeParameterValue.
     */
    @Test
    public void testSetStringValueWhenValid() {
        System.out.println("setStringValueWhenValid");

        final String s = "Remove";
        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final boolean expResult = true;
        final boolean result = instance.setStringValue(s);

        assertEquals(result, expResult);
    }

    /**
     * Test of setStringValue method, of class FilterTypeParameterValue.
     */
    @Test
    public void testSetStringValueWhenInvalid() {
        System.out.println("setStringValueWhenInvalid");

        final String s = "Foo";
        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final boolean expResult = false;
        final boolean result = instance.setStringValue(s);

        assertEquals(result, expResult);
    }

    /**
     * Test of getObjectValue method, of class FilterTypeParameterValue.
     */
    @Test
    public void testGetObjectValue() {
        System.out.println("getObjectValue");

        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final Object expResult = instance;
        final Object result = instance.getObjectValue();

        assertEquals(result, expResult);
    }

    /**
     * Test of setObjectValue method, of class FilterTypeParameterValue.
     */
    @Test
    public void testSetObjectValueWithValidType() {
        System.out.println("setObjectValue");

        final Object o = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final boolean expResult = true;
        final boolean result = instance.setObjectValue(o);

        assertEquals(result, expResult);
    }

    /**
     * Test of createCopy method, of class FilterTypeParameterValue.
     */
    @Test
    public void testCreateCopy() {
        System.out.println("createCopy");

        final FilterTypeParameterValue instance = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final ParameterValue expResult = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final ParameterValue result = instance.createCopy();

        assertEquals(result, expResult);
    }

    /**
     * Test of hashCode method, of class FilterTypeParameterValue.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");

        final FilterTypeParameterValue instance = new FilterTypeParameterValue();
        final int expResult = 119;
        final int result = instance.hashCode();

        assertEquals(result, expResult);
    }

    /**
     * Test of equals method, of class FilterTypeParameterValue.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        final Object object = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final FilterTypeParameterValue instance = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final boolean expResult = true;
        final boolean result = instance.equals(object);

        assertEquals(result, expResult);
    }

    /**
     * Test of toString method, of class FilterTypeParameterValue.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        final FilterTypeParameterValue instance = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final String expResult = "Remove";
        final String result = instance.toString();

        assertEquals(result, expResult);
    }

    /**
     * Test of compareTo method, of class FilterTypeParameterValue.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");

        final FilterTypeParameterValue filterTypeParameter = new FilterTypeParameterValue(FilterType.REMOVE_FILTER);
        final FilterTypeParameterValue instance = new FilterTypeParameterValue(FilterType.SELECT_FILTER);
        final int expResult = 2;
        final int result = instance.compareTo(filterTypeParameter);

        assertEquals(result, expResult);
    }

}
