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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.NODE_TYPES_PARAMETER_ID;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.N_PARAMETER_ID;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.TRANSACTION_TYPES_PARAMETER_ID;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class CompleteGraphBuilderPluginNGTest {

    private StoreGraph graph;
    private static final Logger LOGGER = Logger.getLogger(CompleteGraphBuilderPluginNGTest.class.getName());

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
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createParameters method, of class CompleteGraphBuilderPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        final CompleteGraphBuilderPlugin instance = new CompleteGraphBuilderPlugin();

        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 4);
        assertTrue(params.getParameters().containsKey(N_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(RANDOM_WEIGHTS_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(NODE_TYPES_PARAMETER_ID));
        assertTrue(params.getParameters().containsKey(TRANSACTION_TYPES_PARAMETER_ID));
    }

    /**
     * Test of updateParameters method, of class CompleteGraphBuilderPlugin. Null graph
     */
    @Test
    public void testUpdateParametersNullGraph() {
        System.out.println("updateParametersNullGraph");

        final CompleteGraphBuilderPlugin instance = new CompleteGraphBuilderPlugin();

        final PluginParameters params = instance.createParameters();
        final PluginParameter<MultiChoiceParameterValue> nAttribute = (PluginParameter<MultiChoiceParameterValue>) params.getParameters().get(NODE_TYPES_PARAMETER_ID);
        final PluginParameter<MultiChoiceParameterValue> tAttribute = (PluginParameter<MultiChoiceParameterValue>) params.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);

        assertTrue(MultiChoiceParameterType.getOptions(nAttribute).isEmpty());
        assertTrue(MultiChoiceParameterType.getOptions(tAttribute).isEmpty());

        instance.updateParameters(null, params);
        assertTrue(MultiChoiceParameterType.getOptions(nAttribute).isEmpty());
        assertTrue(MultiChoiceParameterType.getOptions(tAttribute).isEmpty());
    }

    /**
     * Test of updateParameters method, of class CompleteGraphBuilderPlugin.
     */
    @Test
    public void testUpdateParameters() {
        System.out.println("updateParameters");

        final CompleteGraphBuilderPlugin instance = new CompleteGraphBuilderPlugin();

        final PluginParameters params = instance.createParameters();
        final PluginParameter<MultiChoiceParameterValue> nAttribute = (PluginParameter<MultiChoiceParameterValue>) params.getParameters().get(NODE_TYPES_PARAMETER_ID);
        final PluginParameter<MultiChoiceParameterValue> tAttribute = (PluginParameter<MultiChoiceParameterValue>) params.getParameters().get(TRANSACTION_TYPES_PARAMETER_ID);

        assertTrue(MultiChoiceParameterType.getOptions(nAttribute).isEmpty());
        assertTrue(MultiChoiceParameterType.getOptions(tAttribute).isEmpty());

        instance.updateParameters(new DualGraph(graph.getSchema(), graph), params);
        assertEquals(MultiChoiceParameterType.getOptions(nAttribute).size(), 27);
        assertEquals(MultiChoiceParameterType.getChoices(nAttribute).size(), 1);
        assertEquals(MultiChoiceParameterType.getOptions(tAttribute).size(), 9);
        assertEquals(MultiChoiceParameterType.getChoices(tAttribute).size(), 1);
    }

    /**
     * Test of showWarning method, of class CompleteGraphBuilderPlugin.
     */
    @Test
    public void testShowWarning() {
        System.out.println("showWarning");

        final CompleteGraphBuilderPlugin instance = new CompleteGraphBuilderPlugin();
        // Run function, expect default answer of true (user did not click OK)
        assertTrue(instance.showWarning(0L, true));
    }

    /**
     * Test of edit method, of class CompleteGraphBuilderPlugin.
     *
     * @throws Exception
     */
    @Test
    public void testEditWithWarning() throws Exception {
        System.out.println("editWithWarning");

        // Set up mocks
        final int numNodes = 1000;
        final boolean isRandomWeights = true;
        final long numTransactions = numNodes * (numNodes - 1) * (isRandomWeights ? 25 : 1);

        final CompleteGraphBuilderPlugin instance = spy(new CompleteGraphBuilderPlugin());
        final GraphWriteMethods mockGraph = mock(GraphWriteMethods.class);
        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);

        final Map<String, PluginParameter<?>> params = mock(Map.class);
        final PluginParameter mockParam = mock(PluginParameter.class);

        when(parameters.getParameters()).thenReturn(params);
        when(params.get(CompleteGraphBuilderPlugin.N_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParam.getIntegerValue()).thenReturn(numNodes);
        when(params.get(CompleteGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID)).thenReturn(mockParam);
        when(mockParam.getBooleanValue()).thenReturn(isRandomWeights);

        when(instance.showWarning(numTransactions, isRandomWeights)).thenReturn(true);

        // Run function        
        instance.edit(mockGraph, interaction, parameters);

        // verify
        verify(instance, times(1)).showWarning(numTransactions, isRandomWeights);
        verify(parameters, times(1)).getParameters();
        verify(params, times(1)).get(CompleteGraphBuilderPlugin.N_PARAMETER_ID);
        verify(params, times(1)).get(CompleteGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID);
        verify(mockParam, times(1)).getIntegerValue();
        verify(mockParam, times(1)).getBooleanValue();
    }
}
