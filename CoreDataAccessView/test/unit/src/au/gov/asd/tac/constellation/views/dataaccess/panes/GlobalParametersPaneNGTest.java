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

import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.GlobalParameters;
import java.awt.GraphicsEnvironment;
import java.util.Map;
import java.util.Set;
import javafx.embed.swing.JFXPanel;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class GlobalParametersPaneNGTest {
    private static MockedStatic<PluginParametersPane> pluginParametersPaneMockedStatic;
    private static MockedStatic<GlobalParameters> globalParametersMockedStatic;
    
    private GlobalParametersPane globalParametersPane;
    private PluginParameters parameters;
    
    public GlobalParametersPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // TODO Find a better solution for this. Because of this limitation these tests
        //      will not be run on the CI server.
        if (!GraphicsEnvironment.isHeadless()) {
            // Interestingly once you throw the skip exception it doesn't call the tear down class
            // so we need to instantiate the static mocks only once we know we will be running the
            // tests.
            pluginParametersPaneMockedStatic = Mockito.mockStatic(PluginParametersPane.class);
            globalParametersMockedStatic = Mockito.mockStatic(GlobalParameters.class);
            
            new JFXPanel();
        } else {
            throw new SkipException("This class requires the build to have a display present.");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        pluginParametersPaneMockedStatic.close();
        globalParametersMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        pluginParametersPaneMockedStatic.reset();
        globalParametersMockedStatic.reset();
        
        parameters = mock(PluginParameters.class);
        
        globalParametersMockedStatic.when(() -> GlobalParameters.getParameters(parameters)).thenReturn(parameters);
        pluginParametersPaneMockedStatic.when(() -> PluginParametersPane.buildPane(eq(parameters), isNull(), isNull())).thenReturn(null);
        
        globalParametersPane = new GlobalParametersPane(parameters);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void constructor() {
        assertTrue(globalParametersPane.isExpanded());
        assertEquals(globalParametersPane.getText(), "Global Parameters");
        assertFalse(globalParametersPane.isCollapsible());
        assertTrue(globalParametersPane.getStyleClass().contains("titled-pane-heading"));
    }
    
    @Test
    public void getParams() {
        assertSame(globalParametersPane.getParams(), parameters);
    }
    
    @Test
    public void getParamLabels() {
        final PluginParameter param1 = mock(PluginParameter.class);
        final PluginParameter param2 = mock(PluginParameter.class);
        
        when(parameters.getParameters())
                .thenReturn(Map.of("hello", param1, "world", param2));
        
        assertEquals(globalParametersPane.getParamLabels(), Set.of("hello", "world"));
    }
}
