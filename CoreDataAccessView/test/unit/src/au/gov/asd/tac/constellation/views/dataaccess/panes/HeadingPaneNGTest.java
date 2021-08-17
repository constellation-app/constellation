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
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.embed.swing.JFXPanel;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Auriga2
 */
public class HeadingPaneNGTest {

    private static MockedStatic<DataAccessPreferences> dataAccessPreferencesMockedStatic;
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
        // TODO Find a better solution for this. Because of this limitation these tests
        //      will not be run on the CI server.

        if (!GraphicsEnvironment.isHeadless()) {
            // Interestingly once you throw the skip exception it doesn't call the tear down class
            // so we need to instantiate the static mocks only once we know we will be running the
            // tests.
            dataAccessPreferencesMockedStatic = Mockito.mockStatic(DataAccessPreferences.class);

            new JFXPanel();
        } else {
            throw new SkipException("This class requires the build to have a display present.");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        dataAccessPreferencesMockedStatic.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        pluginsList = Arrays.asList(dataAccessPlugin, dataAccessPlugin, dataAccessPlugin);

        dataAccessPreferencesMockedStatic.reset();

        headingPane = new HeadingPane(headingText, pluginsList, top, globalParamLabels);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
        dataAccessPreferencesMockedStatic.when(() -> DataAccessPreferences.isExpanded(headingText, true)).thenReturn(false);

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
        final TestListener listener = new TestListener();

        final HeadingPane listenedHeadingPane = new HeadingPane(headingText, pluginsList, listener, globalParamLabels);

        assertFalse(listener.hasHierarchicalUpdated());
        listenedHeadingPane.hierarchicalUpdate();
        assertTrue(listener.hasHierarchicalUpdated());
    }

    /**
     * Test heading pane sends message to listeners when the pane is enabled and
     * the validity has changed.
     */
    @Test
    public void testValidityChanged_paneIsQueryEnabled() {
        System.out.println("testValidityChanged_paneIsQueryEnabled");
        final TestListener listener = new TestListener();

        final HeadingPane listenedHeadingPane = new HeadingPane(headingText, pluginsList, listener, globalParamLabels);

        assertFalse(listener.hasValidityUpdated());
        listenedHeadingPane.validityChanged(true);
        assertTrue(listener.hasValidityUpdated());
    }

    /**
     * Test heading pane does not send a message to listeners when the pane is
     * disabled and the validity has changed.
     */
    @Test
    public void testValidityChanged_paneIsNotQueryEnabled() {
        System.out.println("testValidityChanged_paneIsNotQueryEnabled");
        final TestListener listener = new TestListener();

        final HeadingPane listenedHeadingPane = new HeadingPane(headingText, pluginsList, listener, globalParamLabels);

        assertFalse(listener.hasValidityUpdated());
        listenedHeadingPane.validityChanged(false);
        assertFalse(listener.hasValidityUpdated());
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

    }
}
