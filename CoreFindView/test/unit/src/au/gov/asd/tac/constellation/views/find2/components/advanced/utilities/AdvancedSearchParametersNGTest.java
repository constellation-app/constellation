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
package au.gov.asd.tac.constellation.views.find2.components.advanced.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import java.util.ArrayList;
import java.util.List;
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
public class AdvancedSearchParametersNGTest {

    private AdvancedSearchParameters paramatersOne;
    private AdvancedSearchParameters paramatersTwo;

    private List<FindCriteriaValues> criteriaValuesListOne = new ArrayList<>();
    private List<FindCriteriaValues> criteriaValuesListTwo = new ArrayList<>();

    ;

    public AdvancedSearchParametersNGTest() {
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
     * Test of copyParameters method, of class AdvancedSearchParameters.
     */
    @Test
    public void testCopyParameters() {
        System.out.println("copyParameters");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        paramatersTwo = new AdvancedSearchParameters();
        paramatersTwo.copyParameters(paramatersOne);
        assertEquals(paramatersOne, paramatersTwo);

    }

    /**
     * Test of getCriteriaValuesList method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetCriteriaValuesList() {
        System.out.println("getCriteriaValuesList");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        assertEquals(criteriaValuesListOne, paramatersOne.getCriteriaValuesList());
    }

    /**
     * Test of getGraphElementType method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetGraphElementType() {
        System.out.println("getGraphElementType");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        assertEquals(GraphElementType.VERTEX, paramatersOne.getGraphElementType());
    }

    /**
     * Test of getAllOrAny method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetAllOrAny() {
        System.out.println("getAllOrAny");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        assertEquals("Any", paramatersOne.getAllOrAny());
    }

    /**
     * Test of getCurrentSelection method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetCurrentSelection() {
        System.out.println("getCurrentSelection");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        assertEquals("Ignore", paramatersOne.getCurrentSelection());
    }

    /**
     * Test of isSearchAllGraphs method, of class AdvancedSearchParameters.
     */
    @Test
    public void testIsSearchAllGraphs() {
        System.out.println("isSearchAllGraphs");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        assertEquals(false, paramatersOne.isSearchAllGraphs());
    }

    /**
     * Test of equals method, of class AdvancedSearchParameters.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Ignore", false);
        paramatersTwo = new AdvancedSearchParameters();
        paramatersTwo.copyParameters(paramatersOne);

        boolean resultOne = paramatersOne.equals(paramatersTwo);
        assertEquals(resultOne, true);

        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.VERTEX, "All", "Ignore", true);
        boolean resultTwo = paramatersOne.equals(paramatersTwo);
        assertEquals(resultTwo, false);
    }

    private void populateCriteriaLists() {

        FindCriteriaValues valueOne = new StringCriteriaValues("string", "Identifier", "Is", "one", true, false);
        FindCriteriaValues valueTwo = new StringCriteriaValues("string", "Identifier", "Is", "two", true, false);
        criteriaValuesListOne.add(valueOne);
        criteriaValuesListOne.add(valueTwo);

        FindCriteriaValues valueThree = new StringCriteriaValues("string", "Identifier", "Is", "three", true, false);
        FindCriteriaValues valueFour = new StringCriteriaValues("string", "Identifier", "Is", "four", true, false);
        criteriaValuesListTwo.add(valueThree);
        criteriaValuesListTwo.add(valueFour);
    }

}
