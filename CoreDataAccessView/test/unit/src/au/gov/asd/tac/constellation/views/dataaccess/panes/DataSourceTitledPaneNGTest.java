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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPaneListener;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class DataSourceTitledPaneNGTest extends ConstellationTest {
    
    private static final String PLUGIN_NAME = "plugin name";
    private static final String GLOBAL_PARAM_1 = "global param 1";
    private static final String GLOBAL_PARAM_2 = "global param 2";

    private static MockedStatic<Platform> platformMockedStatic;
    
    private final DataAccessPlugin plugin = mock(DataAccessPlugin.class);
    private final ImageView dataSourceIcon = mock(ImageView.class);
    private final PluginParametersPaneListener top = mock(PluginParametersPaneListener.class);
    private final PluginParameters dataSourceParameters = mock(PluginParameters.class);
    private final ExecutorService executorService = mock(ExecutorService.class);

    private DataSourceTitledPane dataSourceTitledPane;

    public DataSourceTitledPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // This prevents the runnable at the bottom of create params to run!!
        // Create params is call by the constructor which is why this needs to be
        // here.
        platformMockedStatic = Mockito.mockStatic(Platform.class);
        platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                .then(mockInvocation -> null);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        platformMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        reset(plugin, dataSourceIcon, top);
        platformMockedStatic.reset();
        
        when(plugin.getName()).thenReturn(PLUGIN_NAME);
        when(dataSourceParameters.copy()).thenReturn(dataSourceParameters);

        // The following is rather ugly. Mockito static mocks only apply to the
        // current thread. Because the parameter creation happen in a executor
        // service we have to get a little creative.
        // TODO Look at refactoring the code so that we can unit test the individual
        //      parts of the runnable and somehow mock its execution during construction
        //      so that object creation is not so difficult.
        doAnswer((Answer<Object>) (InvocationOnMock invocation) -> {
            final Object[] args = invocation.getArguments();
            final Runnable runnable = (Runnable) args[0];
            try (final MockedStatic<DefaultPluginParameters> defaultPluginParamsMockedStatic1 = Mockito.mockStatic(DefaultPluginParameters.class)) {
                defaultPluginParamsMockedStatic1.when(() -> DefaultPluginParameters.getDefaultParameters(plugin)).thenReturn(dataSourceParameters);
                runnable.run();
            }
            return null;
        }).when(executorService).execute(any());

        // The constructor makes a call to create parameters but we can't insert the
        // mock executor service until after the object is created. So whilst the
        // parametersCreated flag will be true, the dataSourceParameter property
        // will still be null because the static mock declaration above will only
        // be triggered once the mock executor service is injected.
        // However after this calls to setParameterValues will be testable.
        dataSourceTitledPane = spy(new DataSourceTitledPane(plugin, dataSourceIcon, top,
                Set.of(GLOBAL_PARAM_1, GLOBAL_PARAM_2)));

        when(dataSourceTitledPane.getParamCreator()).thenReturn(executorService);
    }

    @Test
    public void heirarchialUpdate() {
        dataSourceTitledPane.hierarchicalUpdate();

        verify(top, times(1)).hierarchicalUpdate();
    }

    @Test
    public void getPlugin() {
        assertSame(dataSourceTitledPane.getPlugin(), plugin);
    }

    @Test
    public void setParametersNonNullPerPluginParamMap() {
        final String actionKey = "actionKey";
        final String nonActionKey = "nonActionKey";
        final String value = "value";

        final PluginParameter<?> nonActionPluginParameter = mock(PluginParameter.class);
        final PluginParameter<?> actionPluginParameter = mock(PluginParameter.class);

        when(nonActionPluginParameter.getId()).thenReturn("Not ActionParameterType.ID");
        when(actionPluginParameter.getId()).thenReturn(ActionParameterType.ID);

        final Map<String, PluginParameter<?>> parameters = Map.of(
                actionKey, actionPluginParameter,
                nonActionKey, nonActionPluginParameter
        );

        when(dataSourceParameters.hasParameter(actionKey)).thenReturn(true);
        when(dataSourceParameters.hasParameter(nonActionKey)).thenReturn(true);
        when(dataSourceParameters.getParameters()).thenReturn(parameters);

        final Map<String, String> perPluginParamMap = Map.of(
                actionKey, value,
                nonActionKey, value,
                GLOBAL_PARAM_1, value,
                GLOBAL_PARAM_2, value
        );

        dataSourceTitledPane.setParameterValues(perPluginParamMap);

        // The set parameters happens in its own thread. We add the loop to ensure
        // there isn't a race condition happening.
        PluginParameters pluginParameters;
        int counter = 0; // need a safety catch so it doesn't get stuck
        while ((pluginParameters = dataSourceTitledPane.getParameters()) == null && counter < 3) {
            Thread.yield();
            counter++;
        };

        assertSame(pluginParameters, dataSourceParameters);

        verify(nonActionPluginParameter).setStringValue(value);
        verify(actionPluginParameter, times(0)).setStringValue(value);
    }

    @Test
    public void setParametersNullPerPluginParamMap() {
        dataSourceTitledPane.setParameterValues(null);

        // The set parameters happens in its own thread. We add the loop to ensure
        // there isn't a race condition happening.
        PluginParameters pluginParameters;
        int counter = 0; // need a safety catch so it doesn't get stuck
        while ((pluginParameters = dataSourceTitledPane.getParameters()) == null && counter < 3) {
            Thread.yield();
            counter++;
        };

        assertSame(pluginParameters, dataSourceParameters);

        assertFalse(dataSourceTitledPane.isQueryEnabled());
    }
}
