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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.NODE_TYPES_PARAMETER_ID;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.N_PARAMETER_ID;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.RANDOM_WEIGHTS_PARAMETER_ID;
import static au.gov.asd.tac.constellation.testing.construction.CompleteGraphBuilderPlugin.TRANSACTION_TYPES_PARAMETER_ID;
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
    
    public CompleteGraphBuilderPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        graph = new StoreGraph(schema);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
}
