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
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class HeadingPaneNGTest {
    private static final Logger LOGGER = Logger.getLogger(HeadingPaneNGTest.class.getName());

    private static MockedStatic<DataAccessPreferenceUtilities> dataAccessPreferenceUtilitiesMockedStatic;
    private final PluginParametersPaneListener top = mock(PluginParametersPaneListener.class);
    private final DataAccessPlugin dataAccessPlugin = mock(DataAccessPlugin.class);
    private HeadingPane headingPane;
    private final String headingText = "Heading Text";
    private List<DataAccessPlugin> pluginsList;
    private final Set<String> globalParamLabels = new HashSet<>(Arrays.asList("ParamLabel_1", "ParamLabel_2"));

    public HeadingPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        dataAccessPreferenceUtilitiesMockedStatic = Mockito.mockStatic(DataAccessPreferenceUtilities.class);
        
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        dataAccessPreferenceUtilitiesMockedStatic.close();
        
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        pluginsList = Arrays.asList(dataAccessPlugin, dataAccessPlugin, dataAccessPlugin);

        dataAccessPreferenceUtilitiesMockedStatic.reset();

        headingPane = new HeadingPane(headingText, pluginsList, top, globalParamLabels);
    }

    /**
     * Test of getDataSources method, of class HeadingPane.
     */
    @Test
    public void testGetDataSources() {
        System.out.println("getDataSources");
        List<DataSourceTitledPane> dataSources = headingPane.getDataSources();
        assertEquals(dataSources.size(), 3);
    }

    /**
     * Test of validityChanged method, of class HeadingPane, when enabled.
     */
    @Test
    public void testValidityChanged_enabled() {
        System.out.println("testValidityChanged_enabled");
        headingPane.validityChanged(true);
        assertTrue(headingPane.isExpanded());
    }

    /**
     * Test of validityChanged method, of class HeadingPane, when disabled.
     */
    @Test
    public void testValidityChanged_disabled() {
        System.out.println("testValidityChanged_disabled");

        String headingText = "Heading Text 2";
        dataAccessPreferenceUtilitiesMockedStatic.when(() -> DataAccessPreferenceUtilities.isExpanded(headingText, true)).thenReturn(false);

        final HeadingPane instance = new HeadingPane(headingText, pluginsList, top, globalParamLabels);
        instance.validityChanged(false);
        assertFalse(instance.isExpanded());
    }

    /**
     * Test of hierarchicalUpdate method, of class HeadingPane, when pane is not
     * QueryEnabled.
     */
    @Test
    public void testHierarchicalUpdate_paneIsNotQueryEnabled() {
        System.out.println("testHierarchicalUpdate_paneIsNotQueryEnabled");
        headingPane.hierarchicalUpdate();
        verify(top, times(1)).hierarchicalUpdate();
        assertEquals(headingPane.getBoxes().getChildren().size(), 3);
    }
    
    /**
     * Test of hierarchicalUpdate method, of class HeadingPane,with listeners
     * when pane is not QueryEnabled.
     */
    @Test
    public void testHierarchicalUpdate_paneIsNotQueryEnabled_withlisteners() {
        System.out.println("testHierarchicalUpdate_paneIsNotQueryEnabled_withlisteners");
        final TestListener listener = new TestListener();
        
        final HeadingPane listenedHeadingPane = new HeadingPane(headingText, pluginsList, listener, globalParamLabels);

        assertFalse(listener.hasHierarchicalUpdated());
        listenedHeadingPane.hierarchicalUpdate();
        assertTrue(listener.hasHierarchicalUpdated());
    }

    /**
     * Test listener of type pluginParametersPaneListener so triggering of
     * listeners can be tested.
     */
    private static class TestListener implements PluginParametersPaneListener {

        private boolean didHierarchicalUpdate = false;
        private boolean didValidityUpdate = false;

        public boolean hasHierarchicalUpdated() {
            return didHierarchicalUpdate;
        }

        public boolean hasValidityUpdated() {
            return didValidityUpdate;
        }

        @Override
        public void validityChanged(boolean valid) {
            didValidityUpdate = true;
        }

        @Override
        public void hierarchicalUpdate() {
            didHierarchicalUpdate = true;
        }

        @Override
        public void notifyRequiredParameterChange(PluginParameter<?> parameter, boolean currentlySatisfied) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    /**
     * TODO: Test of hierarchicalUpdate method, of class HeadingPane, when pane is
     * QueryEnabled. Tricky because HeadingPane is creating a List of new
     * DataSourceTitledPanes in the constructor.
     */
}
