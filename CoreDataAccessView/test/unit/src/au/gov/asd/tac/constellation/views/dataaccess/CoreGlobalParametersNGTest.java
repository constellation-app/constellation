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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.DATETIME_RANGE_PARAMETER;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.DATETIME_RANGE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.QUERY_NAME_PARAMETER;
import static au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters.TIMESTAMP_FORMAT;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewPreferenceKeys;
import java.time.Instant;
import java.util.List;
import java.util.prefs.Preferences;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for CoreGlobalParameters.
 *
 * @author sol695510
 */
public class CoreGlobalParametersNGTest {

    // Mock of nbPreferences is created to make LookupPluginsTask behave correctly
    private static MockedStatic<NbPreferences> nbPreferencesStatic;
    private static final Preferences preferenceMock = mock(Preferences.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            nbPreferencesStatic = Mockito.mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
            nbPreferencesStatic.when(() -> NbPreferences.forModule(DataAccessViewPreferenceKeys.class)).thenReturn(preferenceMock);

            when(preferenceMock.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn("");
            when(preferenceMock.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn("");
        } catch (Exception e) {
            System.out.println("Error creating static mock of NbPreferences");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        nbPreferencesStatic.close();
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
