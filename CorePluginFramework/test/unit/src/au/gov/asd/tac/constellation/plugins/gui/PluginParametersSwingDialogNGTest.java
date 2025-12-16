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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.JFXPanel;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.DialogDisplayer;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class PluginParametersSwingDialogNGTest {

    private static final Logger LOGGER = Logger.getLogger(PluginParametersSwingDialogNGTest.class.getName());

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

    /**
     * Test of showAndReturnDisplay method, of class PluginParametersSwingDialog.
     */
    @Test
    public void testShowAndReturnDisplay() {
        System.out.println("showAndReturnDisplay");
        final boolean focused = false;
        final int expResult = -1;
        final PluginParameters params = spy(new PluginParameters());

        try (MockedConstruction<JFXPanel> mockConstructor = Mockito.mockConstruction(JFXPanel.class); final MockedStatic<DialogDisplayer> mockDialogDisplayer = Mockito.mockStatic(DialogDisplayer.class, Mockito.CALLS_REAL_METHODS)) {
            final PluginParametersSwingDialog instance = new PluginParametersSwingDialog("", params);

            final Object result = instance.showAndReturnDisplay(focused);
            assertEquals(result, expResult);

            assertEquals(1, mockConstructor.constructed().size());
            final JFXPanel panel = mockConstructor.constructed().get(0);
            verify(panel).setScene(any());
            verify(panel).setPreferredSize(any());

            mockDialogDisplayer.verify(() -> DialogDisplayer.getDefault(), times(1));
        }

    }

    /**
     * Test of storeRecentParameterValues method, of class PluginParametersSwingDialog.
     */
    @Test
    public void testStoreRecentParameterValues() {
        System.out.println("storeRecentParameterValues");
        final PluginParameters params = spy(new PluginParameters());

        try (MockedConstruction<JFXPanel> mockConstructor = Mockito.mockConstruction(JFXPanel.class)) {
            final PluginParametersSwingDialog instance = new PluginParametersSwingDialog("", params);
            instance.storeRecentParameterValues();

            assertEquals(1, mockConstructor.constructed().size());
            final JFXPanel panel = mockConstructor.constructed().get(0);

            verify(panel).setScene(any());
            verify(panel).setPreferredSize(any());
            verify(params).storeRecentValues();
        }
    }
}
