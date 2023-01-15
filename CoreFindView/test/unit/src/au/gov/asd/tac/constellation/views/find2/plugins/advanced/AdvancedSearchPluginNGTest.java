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
package au.gov.asd.tac.constellation.views.find2.plugins.advanced;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.BooleanCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.ColorCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.DateTimeCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FindCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.FloatCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.IconCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.criteriavalues.StringCriteriaValues;
import au.gov.asd.tac.constellation.views.find2.components.advanced.utilities.AdvancedSearchParameters;
import au.gov.asd.tac.constellation.views.find2.utilities.ActiveFindResultsList;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
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
public class AdvancedSearchPluginNGTest {

    private Map<String, Graph> graphMap = new HashMap<>();
    private Graph graph;
    private Graph graph2;
    private int selectedV, selectedT;
    private int labelV, identifierV, xV, dimV, colorV, iconV, dateTimeT, labelT, identiferT, widthT;
    private int vxId1, vxId2, vxId3, vxId4, vxId5UpperCase, vxId6, vxId7, vxId8, txId1, txId2, txId3, txId4;

    private ZonedDateTime testTime = ZonedDateTime.of(2022, 02, 02, 22, 22, 22, 222222222, ZoneId.systemDefault());
    private ZonedDateTime plus1YearTestTime = ZonedDateTime.of(2022, 02, 02, 22, 22, 22, 222222222, ZoneId.systemDefault()).plusYears(1);
    private ZonedDateTime plus2YearTestTime = ZonedDateTime.of(2022, 02, 02, 22, 22, 22, 222222222, ZoneId.systemDefault()).plusYears(2);
    private static final Logger LOG = Logger.getLogger(AdvancedSearchPluginNGTest.class.getName());

    public AdvancedSearchPluginNGTest() {
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
     * Test of edit method, of class AdvancedSearchPlugin.
     */
    @Test
    public void testEdit() throws Exception {
        System.out.println("edit");

        setupGraph();
        GraphElementType elementType = GraphElementType.VERTEX;
        final String all = "All";
        final String any = "Any";
        final String replace = "Replace Selection";
        final String addTo = "Add To Selection";
        final String removeFrom = "Remove From Selection";
        final String searchLocation = "Current Graph";

        //STRING | VERTEX | MATCH ANY CRITERIA | INGORE CURRENT SELECTION | SEARCH 1 GRAPH
        //Is
        List<FindCriteriaValues> findCriteriaValuesList = new ArrayList<>();
        StringCriteriaValues stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Is", "identifer name", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        AdvancedSearchParameters parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        AdvancedSearchPlugin advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        ReadableGraph rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is not
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Is Not", "identifer name", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Contains
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Contains", "identifer", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Doesn't Contain
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Doesn't Contain", "identifer", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Begins With
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Begins With", "iden", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Ends With
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Ends With", "name", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Matches (Regex)
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Matches (Regex)", "den", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Float
        //Is
        findCriteriaValuesList = new ArrayList<>();
        FloatCriteriaValues floatCriteriaValues = new FloatCriteriaValues("float", "x", "Is", 1);
        findCriteriaValuesList.add(floatCriteriaValues);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is Not
        findCriteriaValuesList = new ArrayList<>();
        floatCriteriaValues = new FloatCriteriaValues("float", "x", "Is Not", 1);
        findCriteriaValuesList.add(floatCriteriaValues);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is Less Than
        findCriteriaValuesList = new ArrayList<>();
        floatCriteriaValues = new FloatCriteriaValues("float", "x", "Is Less Than", 4);
        findCriteriaValuesList.add(floatCriteriaValues);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is Greater Than
        findCriteriaValuesList = new ArrayList<>();
        floatCriteriaValues = new FloatCriteriaValues("float", "x", "Is Greater Than", 0);
        findCriteriaValuesList.add(floatCriteriaValues);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is Between
        findCriteriaValuesList = new ArrayList<>();
        floatCriteriaValues = new FloatCriteriaValues("float", "x", "Is Between", 0, 4);
        findCriteriaValuesList.add(floatCriteriaValues);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Boolean
        //Is
        findCriteriaValuesList = new ArrayList<>();
        BooleanCriteriaValues booleanCriteriaValue = new BooleanCriteriaValues("boolean", "dim", "Is", true);
        findCriteriaValuesList.add(booleanCriteriaValue);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Constellation Color
        //Is
        findCriteriaValuesList = new ArrayList<>();
        ColorCriteriaValues colorCriteriaValue = new ColorCriteriaValues("color", "color", "Is", ConstellationColor.BLUE);
        findCriteriaValuesList.add(colorCriteriaValue);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is Not
        findCriteriaValuesList = new ArrayList<>();
        colorCriteriaValue = new ColorCriteriaValues("color", "color", "Is Not", ConstellationColor.BLUE);
        findCriteriaValuesList.add(colorCriteriaValue);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Constellation Icon
        //Is
        findCriteriaValuesList = new ArrayList<>();
        IconCriteriaValues iconCriteriaValue = new IconCriteriaValues("icon", "background_icon", "Is", IconManager.getIcon("Flag.Australia"));
        findCriteriaValuesList.add(iconCriteriaValue);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //Is Not
        findCriteriaValuesList = new ArrayList<>();
        iconCriteriaValue = new IconCriteriaValues("icon", "background_icon", "Is Not", IconManager.getIcon("Flag.Australia"));
        findCriteriaValuesList.add(iconCriteriaValue);
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        rg.close();

        //DateTime
        //Occured On
        findCriteriaValuesList = new ArrayList<>();
        DateTimeCriteriaValues dateTimeCriteriaValue = new DateTimeCriteriaValues("datetime", "DateTime", "Occured On", formatDateTime(testTime));
        findCriteriaValuesList.add(dateTimeCriteriaValue);

        elementType = GraphElementType.TRANSACTION;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        LOG.log(Level.SEVERE, dateTimeCriteriaValue.getDateTimeStringPrimaryValue());
        LOG.log(Level.SEVERE, dateTimeCriteriaValue.getDateTimeStringPrimaryValue());
        LOG.log(Level.SEVERE, rg.getObjectValue(dateTimeT, txId1).toString());

        assertEquals(rg.getBooleanValue(selectedT, txId1), true);
        assertEquals(rg.getBooleanValue(selectedT, txId2), true);
        assertEquals(rg.getBooleanValue(selectedT, txId3), false);
        assertEquals(rg.getBooleanValue(selectedT, txId4), false);
        rg.close();

        //Didn't Occur On
        findCriteriaValuesList = new ArrayList<>();
        dateTimeCriteriaValue = new DateTimeCriteriaValues("datetime", "DateTime", "Didn't Occur On", formatDateTime(testTime));
        findCriteriaValuesList.add(dateTimeCriteriaValue);

        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedT, txId1), true);
        assertEquals(rg.getBooleanValue(selectedT, txId2), true);
        assertEquals(rg.getBooleanValue(selectedT, txId3), true);
        assertEquals(rg.getBooleanValue(selectedT, txId4), true);
        rg.close();

        //Occured Before
        findCriteriaValuesList = new ArrayList<>();
        dateTimeCriteriaValue = new DateTimeCriteriaValues("datetime", "DateTime", "Occured Before", formatDateTime(plus1YearTestTime));
        findCriteriaValuesList.add(dateTimeCriteriaValue);

        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedT, txId1), true);
        assertEquals(rg.getBooleanValue(selectedT, txId2), true);
        assertEquals(rg.getBooleanValue(selectedT, txId3), true);
        assertEquals(rg.getBooleanValue(selectedT, txId4), true);
        rg.close();

        //Occured After
        findCriteriaValuesList = new ArrayList<>();
        dateTimeCriteriaValue = new DateTimeCriteriaValues("datetime", "DateTime", "Occured After", formatDateTime(plus1YearTestTime));
        findCriteriaValuesList.add(dateTimeCriteriaValue);

        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedT, txId1), true);
        assertEquals(rg.getBooleanValue(selectedT, txId2), true);
        assertEquals(rg.getBooleanValue(selectedT, txId3), true);
        assertEquals(rg.getBooleanValue(selectedT, txId4), true);
        rg.close();

        //Occured Between
        findCriteriaValuesList = new ArrayList<>();
        dateTimeCriteriaValue = new DateTimeCriteriaValues("datetime", "DateTime", "Occured Between", formatDateTime(testTime), formatDateTime(plus2YearTestTime));
        findCriteriaValuesList.add(dateTimeCriteriaValue);

        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedT, txId1), true);
        assertEquals(rg.getBooleanValue(selectedT, txId2), true);
        assertEquals(rg.getBooleanValue(selectedT, txId3), true);
        assertEquals(rg.getBooleanValue(selectedT, txId4), true);
        rg.close();

        //Test multiple criteria (any)
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Is", "identifer name", true, false);
        iconCriteriaValue = new IconCriteriaValues("icon", "background_icon", "Is", IconManager.getIcon("Flag.Australia"));

        findCriteriaValuesList.add(stringCriteriaValue1);
        findCriteriaValuesList.add(iconCriteriaValue);

        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId6), false);

        rg.close();

        //Test multiple criteria (any)
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Is", "a vertex", true, false);
        iconCriteriaValue = new IconCriteriaValues("icon", "background_icon", "Is", IconManager.getIcon("Flag.England"));

        findCriteriaValuesList.add(stringCriteriaValue1);
        findCriteriaValuesList.add(iconCriteriaValue);

        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, all, replace, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId4), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId6), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId7), false);

        rg.close();

        //Test find next
        findCriteriaValuesList = new ArrayList<>();
        stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Is", "identifer name", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);

        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, false, true);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        AdvancedFindGraphSelectionPlugin findGraphSelectionPlugin = new AdvancedFindGraphSelectionPlugin(parameters, false, true);
        ActiveFindResultsList.getAdvancedResultsList().incrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);

        rg.close();

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, false, true);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        findGraphSelectionPlugin = new AdvancedFindGraphSelectionPlugin(parameters, false, true);
        ActiveFindResultsList.getAdvancedResultsList().incrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);

        rg.close();

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, false, true);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        findGraphSelectionPlugin = new AdvancedFindGraphSelectionPlugin(parameters, false, true);
        ActiveFindResultsList.getAdvancedResultsList().incrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);

        rg.close();

        //Test find prev
        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, false, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        findGraphSelectionPlugin = new AdvancedFindGraphSelectionPlugin(parameters, false, false);
        ActiveFindResultsList.getAdvancedResultsList().decrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);

        rg.close();

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, false, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);
        findGraphSelectionPlugin = new AdvancedFindGraphSelectionPlugin(parameters, false, false);
        ActiveFindResultsList.getAdvancedResultsList().decrementCurrentIndex();
        PluginExecution.withPlugin(findGraphSelectionPlugin).executeNow(graph);
        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);

        rg.close();

        //Test add to
        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, addTo, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);

        WritableGraph wg = graph.getWritableGraph("", true);
        wg.setBooleanValue(selectedV, vxId6, true);
        wg.setBooleanValue(selectedV, vxId7, true);
        wg.commit();

        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId6), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId7), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), true);

        rg.close();

        //Test Remove From
        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, removeFrom, searchLocation);

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);

        rg = graph.getReadableGraph();

        //all but vxId6 match the criteria, so vxId6 remains selected
        assertEquals(rg.getBooleanValue(selectedV, vxId6), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId7), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId1), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), false);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);

        rg.close();

        //Test Find In
        elementType = GraphElementType.VERTEX;
        parameters = new AdvancedSearchParameters(findCriteriaValuesList, elementType, any, replace, "Current Selection");

        advancedSearchPlugin = new AdvancedSearchPlugin(parameters, true, false);
        PluginExecution.withPlugin(advancedSearchPlugin).executeNow(graph);

        wg = graph.getWritableGraph("", true);
        wg.setBooleanValue(selectedV, vxId1, true);
        wg.setBooleanValue(selectedV, vxId2, true);
        wg.commit();

        rg = graph.getReadableGraph();

        assertEquals(rg.getBooleanValue(selectedV, vxId1), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId2), true);
        assertEquals(rg.getBooleanValue(selectedV, vxId3), false);

        rg.close();

    }

    /**
     * Test of getName method, of class AdvancedSearchPlugin.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        List<FindCriteriaValues> findCriteriaValuesList = new ArrayList<>();
        StringCriteriaValues stringCriteriaValue1 = new StringCriteriaValues("string", "Identifier", "Is", "identifer name", true, false);
        findCriteriaValuesList.add(stringCriteriaValue1);
        AdvancedSearchParameters parameters = new AdvancedSearchParameters(findCriteriaValuesList, GraphElementType.VERTEX, "Any", "Replace Selection", "Current Graph");

        AdvancedSearchPlugin instance = new AdvancedSearchPlugin(parameters, true, false);
        String expResult = "Find: Advanced Search";
        String result = instance.getName();
        assertEquals(result, expResult);
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

    private void setupGraph() {
        graph = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());
        graph2 = new DualGraph(SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema());

        graphMap.put(graph.getId(), graph);
        graphMap.put(graph2.getId(), graph2);
        try {

            WritableGraph wg = graph.getWritableGraph("", true);

            // Create Selected Attributes
            selectedV = VisualConcept.VertexAttribute.SELECTED.ensure(wg);
            labelV = VisualConcept.VertexAttribute.LABEL.ensure(wg);
            identifierV = VisualConcept.VertexAttribute.IDENTIFIER.ensure(wg);
            xV = VisualConcept.VertexAttribute.X.ensure(wg);
            dimV = VisualConcept.VertexAttribute.DIMMED.ensure(wg);
            colorV = VisualConcept.VertexAttribute.COLOR.ensure(wg);
            iconV = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(wg);

            selectedT = VisualConcept.TransactionAttribute.SELECTED.ensure(wg);
            labelT = VisualConcept.TransactionAttribute.LABEL.ensure(wg);
            identiferT = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(wg);
            widthT = VisualConcept.TransactionAttribute.WIDTH.ensure(wg);
            dateTimeT = TemporalConcept.TransactionAttribute.DATETIME.ensure(wg);

            vxId1 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId1, false);
            wg.setStringValue(labelV, vxId1, "label name");
            wg.setStringValue(identifierV, vxId1, "identifer name");
            wg.setFloatValue(xV, vxId1, 1);
            wg.setBooleanValue(dimV, vxId1, true);
            wg.setObjectValue(colorV, vxId1, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId1, IconManager.getIcon("Flag.Australia"));

            vxId2 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId2, false);
            wg.setStringValue(labelV, vxId2, "label name");
            wg.setStringValue(identifierV, vxId2, "identifer name");
            wg.setFloatValue(xV, vxId2, 1);
            wg.setBooleanValue(dimV, vxId2, true);
            wg.setObjectValue(colorV, vxId2, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId2, IconManager.getIcon("Flag.Australia"));

            vxId3 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId3, false);
            wg.setStringValue(labelV, vxId3, "label name");
            wg.setStringValue(identifierV, vxId3, "identifer name");
            wg.setFloatValue(xV, vxId3, 1);
            wg.setBooleanValue(dimV, vxId3, true);
            wg.setObjectValue(colorV, vxId3, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId3, IconManager.getIcon("Flag.Australia"));

            vxId4 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId4, false);
            wg.setStringValue(labelV, vxId4, "label name");
            wg.setStringValue(identifierV, vxId4, "identifer name");
            wg.setFloatValue(xV, vxId4, 1);
            wg.setBooleanValue(dimV, vxId4, true);
            wg.setObjectValue(colorV, vxId4, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId4, IconManager.getIcon("Flag.Australia"));

            vxId5UpperCase = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId5UpperCase, false);
            wg.setStringValue(labelV, vxId5UpperCase, "LABEL NAME");
            wg.setStringValue(identifierV, vxId5UpperCase, "IDENTIFIER NAME");
            wg.setFloatValue(xV, vxId5UpperCase, 1);
            wg.setBooleanValue(dimV, vxId5UpperCase, true);
            wg.setObjectValue(colorV, vxId5UpperCase, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId5UpperCase, IconManager.getIcon("Flag.Australia"));

            vxId6 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId6, false);
            wg.setStringValue(labelV, vxId6, "test");
            wg.setStringValue(identifierV, vxId6, "a vertex");
            wg.setFloatValue(xV, vxId6, 1);
            wg.setBooleanValue(dimV, vxId6, true);
            wg.setObjectValue(colorV, vxId6, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId6, IconManager.getIcon("Flag.England"));

            vxId7 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId7, false);
            wg.setStringValue(labelV, vxId7, "experiment");
            wg.setStringValue(identifierV, vxId7, "identifer name");
            wg.setFloatValue(xV, vxId7, 1);
            wg.setBooleanValue(dimV, vxId7, true);
            wg.setObjectValue(colorV, vxId7, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId7, IconManager.getIcon("Flag.England"));

            vxId8 = wg.addVertex();
            wg.setBooleanValue(selectedV, vxId8, false);
            wg.setStringValue(labelV, vxId8, "test");
            wg.setStringValue(identifierV, vxId8, "a node");
            wg.setFloatValue(xV, vxId8, 1);
            wg.setBooleanValue(dimV, vxId8, true);
            wg.setObjectValue(colorV, vxId8, ConstellationColor.BLUE);
            wg.setObjectValue(iconV, vxId8, IconManager.getIcon("Flag.Australia"));

            txId1 = wg.addTransaction(vxId1, vxId1, false);
            wg.setBooleanValue(selectedT, txId1, false);
            wg.setStringValue(labelT, txId1, "label name");
            wg.setStringValue(identiferT, txId1, "identifer name");
            wg.setFloatValue(widthT, txId1, 1);
            wg.setObjectValue(dateTimeT, txId1, testTime);

            txId2 = wg.addTransaction(vxId1, vxId2, false);
            wg.setBooleanValue(selectedT, txId2, false);
            wg.setStringValue(labelT, txId2, "label name");
            wg.setStringValue(identiferT, txId2, "identifer name");
            wg.setFloatValue(widthT, txId2, 1);
            wg.setObjectValue(dateTimeT, txId2, testTime);

            txId3 = wg.addTransaction(vxId1, vxId3, false);
            wg.setBooleanValue(selectedT, txId3, false);
            wg.setStringValue(labelT, txId3, "label name");
            wg.setStringValue(identiferT, txId3, "identifer name");
            wg.setFloatValue(widthT, txId3, 1);
            wg.setObjectValue(dateTimeT, txId3, plus1YearTestTime);

            txId4 = wg.addTransaction(vxId1, vxId4, false);
            wg.setBooleanValue(selectedT, txId4, false);
            wg.setStringValue(labelT, txId4, "label name");
            wg.setStringValue(identiferT, txId4, "identifer name");
            wg.setFloatValue(widthT, txId4, 1);
            wg.setObjectValue(dateTimeT, txId4, plus2YearTestTime);

            wg.commit();

        } catch (final InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        }
    }

}
