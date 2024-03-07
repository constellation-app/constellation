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
package au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues;

import au.gov.asd.tac.constellation.views.find.components.advanced.criteriavalues.FindCriteriaValues;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Atlas139mkm
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class FindCriteriaValuesNGTest extends ConstellationTest {

    private final String typeString = "Type";
    private final String attributeString = "Attribute";
    private final String filterString = "Filter";

    private FindCriteriaValues findCriteriaValue = new FindCriteriaValues(typeString, attributeString, filterString);

    public FindCriteriaValuesNGTest() {
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
     * Test of getAttributeType method, of class FindCriteriaValues.
     */
    @Test
    public void testGetAttributeType() {
        System.out.println("getAttributeType");
        assertEquals(typeString, findCriteriaValue.getAttributeType());
    }

    /**
     * Test of getAttribute method, of class FindCriteriaValues.
     */
    @Test
    public void testGetAttribute() {
        System.out.println("getAttribute");
        assertEquals(attributeString, findCriteriaValue.getAttribute());
    }

    /**
     * Test of getFilter method, of class FindCriteriaValues.
     */
    @Test
    public void testGetFilter() {
        System.out.println("getFilter");
        assertEquals(filterString, findCriteriaValue.getFilter());

    }

}
