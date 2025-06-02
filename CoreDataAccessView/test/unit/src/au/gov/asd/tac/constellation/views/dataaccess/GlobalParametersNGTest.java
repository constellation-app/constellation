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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ActionParameterType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class GlobalParametersNGTest {

    private static MockedStatic<Lookup> lookupStaticMock;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        lookupStaticMock = Mockito.mockStatic(Lookup.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        lookupStaticMock.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        GlobalParameters.clearGlobalParameters();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        lookupStaticMock.reset();
    }

    @Test
    public void getParameters() {
        final PluginParameters previous = mock(PluginParameters.class);
        final Lookup lookup = mock(Lookup.class);

        final PluginParameter param1 = mock(PluginParameter.class);
        final PluginParameter param2 = mock(PluginParameter.class);

        when(param1.getId()).thenReturn("param1");
        when(param2.getId()).thenReturn("param2");
        when(param1.getType()).thenReturn(ActionParameterType.INSTANCE);
        when(param2.getType()).thenReturn(ActionParameterType.INSTANCE);

        final GlobalParameters.PositionalPluginParameter positionalParam1
                = new GlobalParameters.PositionalPluginParameter(param1, 10);
        final GlobalParameters.PositionalPluginParameter positionalParam2
                = new GlobalParameters.PositionalPluginParameter(param2, 5);

        final GlobalParameters globalParameters1 = mock(GlobalParameters.class);
        final GlobalParameters globalParameters2 = mock(GlobalParameters.class);

        when(globalParameters1.getParameterList(previous))
                .thenReturn(List.of(positionalParam1));
        when(globalParameters2.getParameterList(previous))
                .thenReturn(List.of(positionalParam2));

        lookupStaticMock.when(Lookup::getDefault).thenReturn(lookup);

        doReturn(List.of(
                globalParameters1,
                globalParameters2
        ))
                .when(lookup).lookupAll(GlobalParameters.class);

        final PluginParameters actual = GlobalParameters.getParameters(previous);

        assertEquals(actual.getParameters().size(), 2);
        assertSame(actual.getParameters().get("param1"), param1);
        assertSame(actual.getParameters().get("param2"), param2);

        // Calling it a second time to ensure that the post process is not called more than once.
        final PluginParameters actualSecondCall = GlobalParameters.getParameters(previous);

        assertEquals(actualSecondCall.getParameters().size(), 2);
        assertSame(actualSecondCall.getParameters().get("param1"), param1);
        assertSame(actualSecondCall.getParameters().get("param2"), param2);

        final ArgumentCaptor<PluginParameters> captor1 = ArgumentCaptor
                .forClass(PluginParameters.class);
        verify(globalParameters1, times(1)).postProcess(captor1.capture());

        final ArgumentCaptor<PluginParameters> captor2 = ArgumentCaptor
                .forClass(PluginParameters.class);
        verify(globalParameters2, times(1)).postProcess(captor2.capture());

        // It iterates over GlobalParameters list and calls post process on each passing the newly
        // generated global parameters list. i.e. the returned value from the method. This verifies
        // that it passes the same object in each call and a cursory check thats its the same plugin
        // parameters object that was returned.
        assertSame(captor1.getValue(), captor2.getValue());
        assertEquals(captor1.getValue().getParameters().keySet(), Set.of("param1", "param2"));
    }

    @Test
    public void readDataToMap() {
        final Map<String, String> container = new HashMap<>();

        GlobalParameters.readDataToMap(
                GlobalParametersNGTest.class, "./resources/globalParametersTestData", container);

        assertEquals(container, Map.of("abc", "def"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void readDataToMapFileDoesNotExist() {
        GlobalParameters.readDataToMap(
                GlobalParametersNGTest.class, "./resources/doesNotExist", Collections.EMPTY_MAP);
    }
}
