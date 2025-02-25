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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class MultiChoiceInputPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(MultiChoiceInputPaneNGTest.class.getName());

    private static final String PLANETS_PARAMETER_ID
            = PluginParameter.buildId(MultiChoiceInputPaneNGTest.class, "planets");
    private final PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue> planetOptions = MultiChoiceParameterType.build(PLANETS_PARAMETER_ID);
    final PluginParameters params = new PluginParameters();

    public MultiChoiceInputPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        if (!planetOptions.getName().equals("Planets")) {
            planetOptions.setName("Planets");
            planetOptions.setDescription("Some planets");
            planets = Arrays.asList("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Coruscant");
            MultiChoiceParameterType.setOptions(planetOptions, planets);
            final List<String> checked = new ArrayList<>();
            checked.add("Earth");
            MultiChoiceParameterType.setChoices(planetOptions, checked);
            params.addParameter(planetOptions);
        }
    }
    private List<String> planets;

    @AfterMethod
    public void tearDownMethod() throws Exception {
        
    }

    @Test
    public void multiChoiceInputPane_constructorWithOptions() {
        MultiChoiceInputPane multiChoiceInputPane = spy(
                new MultiChoiceInputPane((PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue>) params.getParameters().get(PLANETS_PARAMETER_ID)));
        assertTrue(multiChoiceInputPane.input.getAllMenuItems().size() == planets.size());
        assertTrue(multiChoiceInputPane.getFieldValue().size() == planetOptions.getMultiChoiceValue().getChoicesData().size());
        assertTrue(multiChoiceInputPane.getFieldValue().getFirst().toString().equals(planetOptions.getMultiChoiceValue().getChoicesData().getFirst().toString()));
    }
    
    @Test
    public void multiChoiceInputPane_getFieldListener() {
        MultiChoiceInputPane multiChoiceInputPane = spy(
                new MultiChoiceInputPane((PluginParameter<MultiChoiceParameterType.MultiChoiceParameterValue>) params.getParameters().get(PLANETS_PARAMETER_ID)));
        
        ConstellationInputListener<List<ParameterValue>> fieldChangeListenerMock = mock(ConstellationInputListener.class);
        doReturn(fieldChangeListenerMock).when(multiChoiceInputPane).getFieldChangeListener(planetOptions);        
        ConstellationInputListener<List<ParameterValue>> fieldChangeListener = multiChoiceInputPane.getFieldChangeListener(planetOptions);        
        assertTrue(multiChoiceInputPane.getFieldChangeListener(planetOptions) == fieldChangeListenerMock);
    }
}
