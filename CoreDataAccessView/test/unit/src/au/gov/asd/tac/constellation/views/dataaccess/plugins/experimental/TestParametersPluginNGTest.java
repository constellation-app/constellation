/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental.TestParametersPlugin.GraphElementTypeParameterValue;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TestParametersPluginNGTest {

    /**
     * Test of getType method, of class TestParametersPlugin.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        final TestParametersPlugin instance = new TestParametersPlugin();
        final String expResult = DataAccessPluginCoreType.DEVELOPER;
        final String result = instance.getType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getPosition method, of class TestParametersPlugin.
     */
    @Test
    public void testGetPosition() {
        System.out.println("getPosition");
        final TestParametersPlugin instance = new TestParametersPlugin();
        final int expResult = Integer.MAX_VALUE - 10;
        final int result = instance.getPosition();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDescription method, of class TestParametersPlugin.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        final TestParametersPlugin instance = new TestParametersPlugin();
        final String expResult = "Test the various input UIs";
        final String result = instance.getDescription();
        assertEquals(result, expResult);
    }

    /**
     * Test of createParameters method, of class TestParametersPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        // Test correct amount of parameters are added
        final int expResult = 20;
        assertEquals(result.getParameters().size(), expResult);

        // Test correct controllers are added by name
        assertTrue(result.hasController(TestParametersPlugin.SELECTED_PARAMETER_ID));
        assertTrue(result.hasController(TestParametersPlugin.REFRESH_PARAMETER_ID));

        // Test created parameters exist in the set of parameters
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.SELECTED_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.TEST1_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.TEST2_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.PASSWORD_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.LOCAL_DATE_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.ELEMENT_TYPE_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.ROBOT_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.REFRESH_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.PLANETS_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.DICE_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.PROBABILITY_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.INPUT_FILE_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.OUTPUT_FILE_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.COLOR_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.CRASH_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.INTERACTION_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.LEVEL_PARAMETER_ID));
        assertTrue(result.getParameters().containsKey(TestParametersPlugin.SLEEP_PARAMETER_ID));
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing of
     * debug pluginException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testQueryException1() throws InterruptedException, PluginException {
        System.out.println("throw pluginexception1");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("Debug");

        try {
            instance.query(recordStore, interaction, result);
        } catch (final PluginException ex) {
            assertEquals(ex.getNotificationLevel(), PluginNotificationLevel.DEBUG);
            throw ex;
        }
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing of
     * info pluginException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testQueryException2() throws InterruptedException, PluginException {
        System.out.println("throw pluginexception2");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("Info");

        try {
            instance.query(recordStore, interaction, result);
        } catch (final PluginException ex) {
            assertEquals(ex.getNotificationLevel(), PluginNotificationLevel.INFO);
            throw ex;
        }
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing of
     * warning pluginException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testQueryException3() throws InterruptedException, PluginException {
        System.out.println("throw pluginexception3");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("Warning");

        try {
            instance.query(recordStore, interaction, result);
        } catch (final PluginException ex) {
            assertEquals(ex.getNotificationLevel(), PluginNotificationLevel.WARNING);
            throw ex;
        }
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing of
     * error pluginException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testQueryException4() throws InterruptedException, PluginException {
        System.out.println("throw pluginexception4");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("Error");

        try {
            instance.query(recordStore, interaction, result);
        } catch (final PluginException ex) {
            assertEquals(ex.getNotificationLevel(), PluginNotificationLevel.ERROR);
            throw ex;
        }
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing of
     * fatal pluginException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testQueryException5() throws InterruptedException, PluginException {
        System.out.println("throw pluginexception5");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("Fatal");

        try {
            instance.query(recordStore, interaction, result);
        } catch (final PluginException ex) {
            assertEquals(ex.getNotificationLevel(), PluginNotificationLevel.FATAL);
            throw ex;
        }
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests not throwing
     * of pluginException
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testQueryException6() throws InterruptedException, PluginException {
        System.out.println("throw pluginexception6");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("None");

        instance.query(recordStore, interaction, result);
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests querying
     * results from graph
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testQueryResults() throws InterruptedException, PluginException {
        System.out.println("Test Query Results");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.LEVEL_PARAMETER_ID).setStringValue("None");

        final RecordStore queryResults = instance.query(recordStore, interaction, result);
        // Test the amount of entries in the RecordStore
        assertEquals(queryResults.size(), 1);

        final DateTimeRange dtr = result.getDateTimeRangeValue(CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID);
        final ZonedDateTime[] dtrStartEnd = dtr.getZonedStartEnd();

        // Test the values entered
        assertEquals(queryResults.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.RAW), "name1@domain1.com");
        assertEquals(queryResults.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE), "Email");
        assertEquals(queryResults.get(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.COMMENT), "TESTPARAMETERSPLUGIN");
        assertEquals(queryResults.get(GraphRecordStoreUtilities.SOURCE + TemporalConcept.VertexAttribute.LAST_SEEN), DateTimeFormatter.ISO_INSTANT.format(dtrStartEnd[0]).replace("Z", ".000Z"));

        assertEquals(queryResults.get(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.RAW), "name2@domain2.com");
        assertEquals(queryResults.get(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.TYPE), "Email");
        assertEquals(queryResults.get(GraphRecordStoreUtilities.DESTINATION + AnalyticConcept.VertexAttribute.COMMENT), "TESTPARAMETERSPLUGIN");
        assertEquals(queryResults.get(GraphRecordStoreUtilities.DESTINATION + TemporalConcept.VertexAttribute.LAST_SEEN), DateTimeFormatter.ISO_INSTANT.format(dtrStartEnd[1]).replace("Z", ".000Z"));

    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests not throwing
     * runtime exception
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testQueryCrash1() throws InterruptedException, PluginException {
        System.out.println("test query crash1");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = mock(DefaultPluginInteraction.class);
        doNothing().when(interaction).setProgress(anyInt(), anyInt(), anyBoolean());

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.CRASH_PARAMETER_ID).setBooleanValue(false);

        instance.query(recordStore, interaction, result);
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing
     * runtime exception
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = RuntimeException.class)
    public void testQueryCrash2() throws InterruptedException, PluginException {
        System.out.println("test query crash2");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        final GraphRecordStore recordStore = new GraphRecordStore();
        final DefaultPluginInteraction interaction = new DefaultPluginInteraction(null, null);

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        result.getParameters().get(TestParametersPlugin.CRASH_PARAMETER_ID).setBooleanValue(true);

        instance.query(recordStore, interaction, result);
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing
     * runtime exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateParametersGraphElementType1() {
        System.out.println("test CreateParameters graph element type1");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution - Should throw an exception here
        final GraphElementTypeParameterValue graphElementType = new GraphElementTypeParameterValue();
        graphElementType.setObjectValue((Object) "Unusable String Object");
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing
     * runtime exception
     */
    @Test
    public void testCreateParametersGraphElementType2() {
        System.out.println("test CreateParameters graph element type2");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        final GraphElementTypeParameterValue graphElementType = new GraphElementTypeParameterValue();
        for (final GraphElementType elementType : GraphElementType.values()) {
            graphElementType.setObjectValue(elementType);
            result.getParameters().get(TestParametersPlugin.ELEMENT_TYPE_PARAMETER_ID).setObjectValue(graphElementType);
            // Ensure it is still selected as the correct value
            assertEquals(SingleChoiceParameterType.getChoice((PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue>) result.getParameters().get(TestParametersPlugin.ELEMENT_TYPE_PARAMETER_ID)), elementType.getShortLabel());
        }
    }

    /**
     * Test of query method, of class TestParametersPlugin. Tests throwing
     * runtime exception
     */
    @Test
    public void testGraphElementType1() {
        System.out.println("test graph element type1");

        final TestParametersPlugin instance = new TestParametersPlugin();
        final PluginParameters result = instance.createParameters();

        // Set plugin query name here before execution
        result.getParameters().get(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID).setStringValue("TESTPARAMETERSPLUGIN");

        // Set plugin parameters here before execution
        final GraphElementTypeParameterValue graphElementType = new GraphElementTypeParameterValue();
        assertTrue(graphElementType.set(GraphElementType.META));

        // return false when setting same attribute
        assertFalse(graphElementType.set(GraphElementType.META));

        assertTrue(graphElementType.setObjectValue(GraphElementType.VERTEX));

        // return false when setting same attribute
        assertFalse(graphElementType.setObjectValue(GraphElementType.VERTEX));

        for (final GraphElementType elementType : GraphElementType.values()) {
            graphElementType.setObjectValue(elementType);
            result.getParameters().get(TestParametersPlugin.ELEMENT_TYPE_PARAMETER_ID).setObjectValue(graphElementType);
            // Ensure it is still selected as the correct value
            assertEquals(SingleChoiceParameterType.getChoice((PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue>) result.getParameters().get(TestParametersPlugin.ELEMENT_TYPE_PARAMETER_ID)), elementType.getShortLabel());
        }
    }
}
