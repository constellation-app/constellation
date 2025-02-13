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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewPreferenceKeys;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodeType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.DataAccessPreQueryValidation;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class of LookupPluginsTask.
 *
 * @author formalhaunt
 */
public class LookupPluginsTaskNGTest {

    private Lookup defaultLookup;
    private static MockedStatic<Lookup> lookupMockedStatic;
    private static MockedStatic<DataAccessPluginType> dapTypeMockedStatic;

    private static MockedStatic<NbPreferences> nbPreferencesStatic;
    private static final Preferences preferenceMock = mock(Preferences.class, Mockito.CALLS_REAL_METHODS);
    private static Preferences preferenceReal;

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            nbPreferencesStatic = Mockito.mockStatic(NbPreferences.class, Mockito.CALLS_REAL_METHODS);
            nbPreferencesStatic.when(() -> NbPreferences.forModule(DataAccessViewPreferenceKeys.class)).thenReturn(preferenceMock);
        } catch (Exception e) {
            System.out.println("Error creating static mock of NbPreferences");
        }

        // PREFERENCES should be a mock if done right
        preferenceReal = LookupPluginsTask.getPreferences();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        nbPreferencesStatic.close();
    }

    @Test
    public void get() {
        System.out.println("get");
        
        // Setup other static mocks
        defaultLookup = mock(Lookup.class);
        lookupMockedStatic = Mockito.mockStatic(Lookup.class);
        dapTypeMockedStatic = Mockito.mockStatic(DataAccessPluginType.class);

        lookupMockedStatic.when(Lookup::getDefault).thenReturn(defaultLookup);
        dapTypeMockedStatic.when(DataAccessPluginType::getTypes).thenReturn(List.of(
                DataAccessPluginCoreType.DEVELOPER,
                DataAccessPluginCoreType.UTILITY));

        if (preferenceReal != null && mockingDetails(preferenceReal).isMock()) {
            when(preferenceReal.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn("");
            when(preferenceReal.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn("");
        }
        // Plugin Mocks
        // All of the mock creations are being coupled with an interface to force the getClass() call on them to be different. Plugin 1 is disabled and so will be ignored.
        final DataAccessPlugin plugin1 = mock(DataAccessPlugin.class, withSettings().extraInterfaces(DataAccessPluginType.class));
        when(plugin1.isEnabled()).thenReturn(false);

        // Plugin 2 is of a type that is not in the listed types above, so it will be ignored.
        final DataAccessPlugin plugin2 = mock(DataAccessPlugin.class, withSettings().extraInterfaces(DataAccessPreQueryValidation.class));
        when(plugin2.isEnabled()).thenReturn(true);
        when(plugin2.getType()).thenReturn(DataAccessPluginCoreType.IMPORT);

        // Plugin 3 is a favourite and should be present in the favourite list.
        final DataAccessPlugin plugin3 = mock(DataAccessPlugin.class, withSettings().extraInterfaces(MergeNodeType.class));
        when(plugin3.isEnabled()).thenReturn(true);
        when(plugin3.getType()).thenReturn(DataAccessPluginCoreType.DEVELOPER);
        when(plugin3.getName()).thenReturn("Plugin 3");
        DataAccessPreferenceUtilities.setFavourite("Plugin 3", true);
        when(plugin3.getOverriddenPlugins()).thenReturn(Collections.emptyList());

        // Plugin 4 will be present but not a favourite.
        final DataAccessPlugin plugin4 = mock(DataAccessPlugin.class, withSettings().extraInterfaces(MergeTransactionType.class));
        when(plugin4.isEnabled()).thenReturn(true);
        when(plugin4.getType()).thenReturn(DataAccessPluginCoreType.UTILITY);
        when(plugin4.getName()).thenReturn("Plugin 4");

        // Overriden by plugin 4 so should not be present in the returned map.
        final DataAccessPlugin plugin5 = mock(DataAccessPlugin.class, withSettings().extraInterfaces(Comparable.class));
        when(plugin5.isEnabled()).thenReturn(true);
        when(plugin5.getType()).thenReturn(DataAccessPluginCoreType.DEVELOPER);
        when(plugin5.getName()).thenReturn("Plugin 5");
        when(plugin5.getOverriddenPlugins()).thenReturn(Collections.emptyList());

        // Overriden by plugin 4 so should not be present in the returned map.
        final DataAccessPlugin plugin6 = mock(DataAccessPlugin.class, withSettings().extraInterfaces(Serializable.class));
        when(plugin6.isEnabled()).thenReturn(true);
        when(plugin6.getType()).thenReturn(DataAccessPluginCoreType.DEVELOPER);
        when(plugin6.getName()).thenReturn("Plugin 6");
        when(plugin6.getOverriddenPlugins()).thenReturn(Collections.emptyList());

        // Plugin 4 overrides plugin 5 and plugin 6.
        when(plugin4.getOverriddenPlugins()).thenReturn(List.of(plugin5.getClass().getName(), plugin6.getClass().getName()));

        doReturn(List.of(
                plugin1,
                plugin2,
                plugin3,
                plugin4,
                plugin5,
                plugin6))
                .when(defaultLookup).lookupAll(DataAccessPlugin.class);

        final Map<String, Pair<Integer, List<DataAccessPlugin>>> expectedPlugins = Map.of(
                "Developer", new Pair(Integer.MAX_VALUE, List.of(plugin3)),
                "Utility", new Pair(Integer.MAX_VALUE, List.of(plugin4)),
                "Favourites", new Pair(Integer.MAX_VALUE, List.of(plugin3)));

        // Assert expected plugins match actual
        assertEquals(new LookupPluginsTask().get(), expectedPlugins);

        // Close static mocks
        lookupMockedStatic.close();
        dapTypeMockedStatic.close();
    }

    @Test
    public void getCategoriesNotBlank() {
        System.out.println("getCategoriesNotBlank");
        
        // Mocks
        final String visibleString = "[Import,Utility,Developer]";
        final String hiddenString = "[Clean]";
        if (preferenceReal != null && mockingDetails(preferenceReal).isMock()) {
            when(preferenceReal.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn(visibleString);
            when(preferenceReal.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV)).thenReturn(hiddenString);

            // Assert mocks work properly
            assertEquals(preferenceReal.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV), visibleString);
            assertEquals(preferenceReal.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV), hiddenString);
        }

        // Expected
        final String[] visibleCategoriesArray = (visibleString.replace("[", "").replace("]", "")).split(SeparatorConstants.COMMA);

        final Map<String, Pair<Integer, List<DataAccessPlugin>>> expectedPlugins = new LinkedHashMap<>();
        final Map<String, List<DataAccessPlugin>> allPlugins = DataAccessUtilities.getAllPlugins();

        for (int i = 0; i < visibleCategoriesArray.length; i++) {
            expectedPlugins.put(visibleCategoriesArray[i].trim(), new Pair<>(i, allPlugins.get(visibleCategoriesArray[i].trim())));
        }

        // Assert actual is expected
        assertEquals(new LookupPluginsTask().get(), expectedPlugins);
    }
}
