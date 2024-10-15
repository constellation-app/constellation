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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class InfoMapPluginNGTest {

    public InfoMapPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    /**
//     * Test of edit method, of class InfoMapPlugin.
//     */
//    @Test
//    public void testEdit() throws Exception {
//        System.out.println("edit");
//        final GraphWriteMethods wg = null;
//        final PluginInteraction interaction = null;
//        final PluginParameters parameters = null;
//        final InfoMapPlugin instance = new InfoMapPlugin();
//        instance.edit(wg, interaction, parameters);
//    }

    /**
     * Test of createParameters method, of class InfoMapPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        final String[] expResults = {
            PluginParameter.buildId(InfoMapPlugin.class, "connection_type"),
            PluginParameter.buildId(InfoMapPlugin.class, "dynamics"),
            PluginParameter.buildId(InfoMapPlugin.class, "optimisation_level"),
            PluginParameter.buildId(InfoMapPlugin.class, "fast_hierarchical"),
            PluginParameter.buildId(InfoMapPlugin.class, "num_trials")
        };

        final InfoMapPlugin instance = new InfoMapPlugin();
        final PluginParameters result = instance.createParameters();
        
        // Assert all plugin parameters were created
        for (final String param : expResults) {
            assertTrue(result.getParameters().containsKey(param));
        }
    }

}
