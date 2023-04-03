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
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FloatCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.IconCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import java.time.ZonedDateTime;
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
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
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
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        assertEquals(criteriaValuesListOne, paramatersOne.getCriteriaValuesList());
    }

    /**
     * Test of getGraphElementType method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetGraphElementType() {
        System.out.println("getGraphElementType");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        assertEquals(GraphElementType.VERTEX, paramatersOne.getGraphElementType());
    }

    /**
     * Test of getAllOrAny method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetAllOrAny() {
        System.out.println("getAllOrAny");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        assertEquals("Any", paramatersOne.getAllOrAny());
    }

    /**
     * Test of getCurrentSelection method, of class AdvancedSearchParameters.
     */
    @Test
    public void testGetCurrentSelection() {
        System.out.println("getCurrentSelection");
        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        assertEquals("Replace Selection", paramatersOne.getPostSearchAction());
    }

    /**
     * Test of equals method, of class AdvancedSearchParameters.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        populateCriteriaLists();
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        paramatersTwo = new AdvancedSearchParameters();
        paramatersTwo.copyParameters(paramatersOne);

        boolean result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, true);

        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.VERTEX, "All", "Replace Selection", "All Open Graphs");
        result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, false);

        //float , color, boolean, zoned datetime, icon
        FindCriteriaValues valueOne = new FloatCriteriaValues("float", "x", "Is", 1);
        FindCriteriaValues valueTwo = new FloatCriteriaValues("float", "x", "Is", 6);
        criteriaValuesListOne.clear();
        criteriaValuesListTwo.clear();
        criteriaValuesListOne.add(valueOne);
        criteriaValuesListTwo.add(valueTwo);
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, false);

        valueOne = new BooleanCriteriaValues("boolean", "dim", "Is", true);
        valueTwo = new BooleanCriteriaValues("boolean", "dim", "Is", false);
        criteriaValuesListOne.clear();
        criteriaValuesListTwo.clear();
        criteriaValuesListOne.add(valueOne);
        criteriaValuesListTwo.add(valueTwo);
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, false);

        valueOne = new ColorCriteriaValues("color", "color", "Is", ConstellationColor.BLUE);
        valueTwo = new ColorCriteriaValues("color", "color", "Is", ConstellationColor.GREEN);
        criteriaValuesListOne.clear();
        criteriaValuesListTwo.clear();
        criteriaValuesListOne.add(valueOne);
        criteriaValuesListTwo.add(valueTwo);
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, false);

        valueOne = new IconCriteriaValues("icon", "background_icon", "Is", IconManager.getIcon("Flag.Australia"));
        valueTwo = new IconCriteriaValues("icon", "background_icon", "Is", IconManager.getIcon("Flag.England"));
        criteriaValuesListOne.clear();
        criteriaValuesListTwo.clear();
        criteriaValuesListOne.add(valueOne);
        criteriaValuesListTwo.add(valueTwo);
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");
        result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, false);

        ZonedDateTime testTimeOne = ZonedDateTime.now();
        ZonedDateTime testTimeTwo = ZonedDateTime.now().plusHours(2);
        valueOne = new DateTimeCriteriaValues("datetime", "DateTime", "Occured On", formatDateTime(testTimeOne));
        valueTwo = new DateTimeCriteriaValues("datetime", "DateTime", "Occured On", formatDateTime(testTimeTwo));
        criteriaValuesListOne.clear();
        criteriaValuesListTwo.clear();
        criteriaValuesListOne.add(valueOne);
        criteriaValuesListTwo.add(valueTwo);
        paramatersOne = new AdvancedSearchParameters(criteriaValuesListOne, GraphElementType.TRANSACTION, "Any", "Replace Selection", "Current Graph");
        paramatersTwo = new AdvancedSearchParameters(criteriaValuesListTwo, GraphElementType.TRANSACTION, "Any", "Replace Selection", "Current Graph");
        result = paramatersOne.equals(paramatersTwo);
        assertEquals(result, false);

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

    private String formatDateTime(ZonedDateTime dateTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(dateTime.getYear()));
        sb.append("-");
        sb.append(addZero(dateTime.getMonthValue()));
        sb.append("-");
        sb.append(addZero(dateTime.getDayOfMonth()));
        sb.append(" ");
        sb.append(addZero(dateTime.getHour()));
        sb.append(":");
        sb.append(addZero(dateTime.getMinute()));
        sb.append(":");
        sb.append(addZero(dateTime.getSecond()));
        sb.append(".");
        sb.append(Integer.toString(dateTime.getNano()).substring(0, 3));
        sb.append(" ");
        sb.append(dateTime.getOffset());
        sb.append(" [");
        sb.append(dateTime.getZone());
        sb.append("]");

        return sb.toString();
    }

    private String addZero(int number) {
        String newNumber = (number < 10 ? newNumber = "0" + Integer.toString(number) : Integer.toString(number));
        return newNumber;
    }

}
