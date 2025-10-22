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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import static org.mockito.Mockito.spy;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class NumberInputPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(NumberInputPaneNGTest.class.getName());

    private final String id = "integerParameter";
    private final PluginParameter<IntegerParameterType.IntegerParameterValue> maxMinParam = IntegerParameterType.build(id);
        
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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        maxMinParam.setName("Max Min");
        maxMinParam.setDescription("Test Max Min");
        maxMinParam.setIntegerValue(Integer.MAX_VALUE);
    }
    

    @Test
    public void testNumberInputPane_integermaxmin() {
        // NumberInputPane defaults the max to Integer.MAX_VALUE and min to Integer.MIN_VALUE
        final NumberInputPane numberInputPane = spy(new NumberInputPane(maxMinParam));
        final Node field = numberInputPane.getChildren().get(0);
        assertTrue(field instanceof Spinner);
        if (field instanceof Spinner) {
            final Spinner spinner = (Spinner) field;
            final int intValue = Integer.parseInt(spinner.getValueFactory().getValue().toString());
            assertTrue(intValue == Integer.MAX_VALUE);
            spinner.decrement();
            assertTrue(Integer.parseInt(spinner.getValueFactory().getValue().toString()) == Integer.MAX_VALUE - 1);
            spinner.increment();
            assertTrue(Integer.parseInt(spinner.getValueFactory().getValue().toString()) == Integer.MAX_VALUE);
            spinner.increment();
            // Cannot go above max value, should stay at MAX_VALUE
            assertTrue(Integer.parseInt(spinner.getValueFactory().getValue().toString()) == Integer.MAX_VALUE);
            spinner.getValueFactory().setValue(Integer.MIN_VALUE);
            assertTrue(Integer.parseInt(spinner.getValueFactory().getValue().toString()) == Integer.MIN_VALUE);
            spinner.decrement();
            // Cannot go below min, should stay at min
            assertTrue(Integer.parseInt(spinner.getValueFactory().getValue().toString()) == Integer.MIN_VALUE);
        }
        
    }
}
