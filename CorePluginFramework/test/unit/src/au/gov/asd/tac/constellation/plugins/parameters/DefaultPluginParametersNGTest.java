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
package au.gov.asd.tac.constellation.plugins.parameters;

import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DefaultPluginParametersNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getDefaultParameters method, of class DefaultPluginParameters.
     */
    @Test
    public void testGetDefaultParameters() {
        System.out.println("getDefaultParameters");
        
        final TestWithNoParametersPlugin noParamsPlugin = new TestWithNoParametersPlugin();
        final TestWithParametersPlugin paramsPlugin = new TestWithParametersPlugin();
        final TestWithParametersPlugin anotherParamsPlugin = new TestWithParametersPlugin();
        
        final PluginParameters noParamsResult = DefaultPluginParameters.getDefaultParameters(noParamsPlugin);
        final PluginParameters paramsResult = DefaultPluginParameters.getDefaultParameters(paramsPlugin);
        final PluginParameters anotherParamsResult = DefaultPluginParameters.getDefaultParameters(anotherParamsPlugin);
        assertNull(noParamsResult);
        assertEquals(paramsResult.getParameters().size(), 3);
        assertEquals(anotherParamsResult.getParameters().size(), 3);
    }
    
    private static class TestWithNoParametersPlugin extends SimplePlugin {

        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    private static class TestWithParametersPlugin extends SimplePlugin {
        
        private static final String TEST_PARAMETER_ONE_ID = PluginParameter.buildId(TestWithParametersPlugin.class, "test1");
        private static final String TEST_PARAMETER_TWO_ID = PluginParameter.buildId(TestWithParametersPlugin.class, "test2");
        private static final String TEST_PARAMETER_THREE_ID = PluginParameter.buildId(TestWithParametersPlugin.class, "test3");

        @Override
        public PluginParameters createParameters() {
            final PluginParameters params = new PluginParameters();
            
            final PluginParameter<StringParameterValue> testParameter1 = StringParameterType.build(TEST_PARAMETER_ONE_ID);
            testParameter1.setName("Test Parameter 1");
            params.addParameter(testParameter1);
            
            final PluginParameter<BooleanParameterValue> testParameter2 = BooleanParameterType.build(TEST_PARAMETER_TWO_ID);
            testParameter1.setName("Test Parameter 2");
            params.addParameter(testParameter2);
            
            final PluginParameter<IntegerParameterValue> testParameter3 = IntegerParameterType.build(TEST_PARAMETER_THREE_ID);
            testParameter1.setName("Test Parameter 3");
            params.addParameter(testParameter3);
            
            return params;
        }
             
        @Override
        protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }       
    }
}
