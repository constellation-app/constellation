/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.DATETIME_RANGE_PARAMETER;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.QUERY_NAME_PARAMETER;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.TIMESTAMP_FORMAT;
import java.time.Instant;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for CoreGlobalParameters.
 *
 * @author sol695510
 */
public class CoreGlobalParametersNGTest {

    public CoreGlobalParametersNGTest() {
        // Intentionally left blank.
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Intentionally left blank.
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Intentionally left blank.
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Intentionally left blank.
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Intentionally left blank.
    }

    /**
     * Test of buildParameterList.
     */
    @Test
    public void testBuildParameterList() {
        System.out.println("testBuildParameterList");

        final CoreGlobalParameters instance = new CoreGlobalParameters();

        final PluginParameters previous = new PluginParameters();
        final List<GlobalParameters.PositionalPluginParameter> positionalPluginParametersList = instance.buildParameterList(previous);

        assertEquals(positionalPluginParametersList.size(), 2);
        assertEquals(positionalPluginParametersList.get(0).getParameter().getName(), "Query Name");
        assertEquals(positionalPluginParametersList.get(0).getParameter().getDescription(), "A reference name for the query");
        assertEquals(positionalPluginParametersList.get(1).getParameter().getName(), "Range");
        assertEquals(positionalPluginParametersList.get(1).getParameter().getDescription(), "The date and time range to query");
        assertEquals(positionalPluginParametersList.get(1).getParameter().getHelpID(), CoreGlobalParameters.class.getName());
    }

    /**
     * Test of updateParameterList, when 'previous' is not null.
     */
    @Test
    public void testUpdateParameterList_previousNotNull() {
        System.out.println("testUpdateParameterList_previousNotNull");

        final CoreGlobalParameters instance = new CoreGlobalParameters();

        final PluginParameters previous = new PluginParameters();
        previous.addParameter(QUERY_NAME_PARAMETER);
        previous.addParameter(DATETIME_RANGE_PARAMETER);

        final List<GlobalParameters.PositionalPluginParameter> positionalPluginParametersList = instance.getParameterList(previous);
        instance.updateParameterList(previous);
        final Instant instant = Instant.now();

        assertEquals(positionalPluginParametersList.get(0).getParameter().getStringValue(), String.format("%s at %s", System.getProperty("user.name"), TIMESTAMP_FORMAT.format(instant)));
        assertEquals(positionalPluginParametersList.get(1).getParameter().getDateTimeRangeValue(), previous.getParameters().get(DATETIME_RANGE_PARAMETER_ID).getDateTimeRangeValue());
    }

    /**
     * Test of updateParameterList, when 'previous' is null.
     */
    @Test
    public void testUpdateParameterList_previousNull() {
        System.out.println("testUpdateParameterList_previousNull");

        final CoreGlobalParameters instance = new CoreGlobalParameters();

        final PluginParameters previous = null;

        final List<GlobalParameters.PositionalPluginParameter> positionalPluginParametersList = instance.getParameterList(previous);
        instance.updateParameterList(previous);
        final Instant instant = Instant.now();

        assertEquals(positionalPluginParametersList.get(0).getParameter().getStringValue(), String.format("%s at %s", System.getProperty("user.name"), TIMESTAMP_FORMAT.format(instant)));
        assertEquals(positionalPluginParametersList.get(1).getParameter().getDateTimeRangeValue(), DATETIME_RANGE_PARAMETER.getDateTimeRangeValue());
    }
}
