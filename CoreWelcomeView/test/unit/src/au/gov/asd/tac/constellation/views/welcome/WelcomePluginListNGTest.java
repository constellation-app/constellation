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
package au.gov.asd.tac.constellation.views.welcome;

import au.gov.asd.tac.constellation.views.welcome.plugins.AddModeWelcomePlugin;
import au.gov.asd.tac.constellation.views.welcome.plugins.GettingStartedWelcomePlugin;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for WelcomePluginList
 * 
 * @author Delphinus8821
 */
public class WelcomePluginListNGTest {

    private static final Logger LOGGER = Logger.getLogger(WelcomePluginListNGTest.class.getName());

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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getTopPlugins method, of class WelcomePluginList.
     */
    @Test
    public void testGetTopPlugins() {
        System.out.println("getTopPlugins");
        final WelcomePluginList instance = new WelcomePluginList();

        final List result = instance.getTopPlugins();
        assertTrue(result.size() == 7);
        assertEquals(result.get(0).getClass(), AddModeWelcomePlugin.class);
    }

    /**
     * Test of getSidePlugins method, of class WelcomePluginList.
     */
    @Test
    public void testGetSidePlugins() {
        System.out.println("getSidePlugins");
        final WelcomePluginList instance = new WelcomePluginList();

        final List result = instance.getSidePlugins();
        assertTrue(result.size() == 4);
        assertEquals(result.get(0).getClass(), GettingStartedWelcomePlugin.class);
    }

}
