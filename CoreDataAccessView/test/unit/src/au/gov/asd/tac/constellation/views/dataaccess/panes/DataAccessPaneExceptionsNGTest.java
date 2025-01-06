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
package au.gov.asd.tac.constellation.views.dataaccess.panes;

import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessPaneExceptionsNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessPaneExceptionsNGTest.class.getName());

    private DataAccessViewTopComponent dataAccessViewTopComponent;

    private DataAccessPane dataAccessPane;

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
        dataAccessViewTopComponent = mock(DataAccessViewTopComponent.class);

        dataAccessPane = spy(new DataAccessPane(dataAccessViewTopComponent));
    }

    @Test
    public void init() {
        assertSame(dataAccessPane.getParentComponent(), dataAccessViewTopComponent);

        assertNotNull(dataAccessPane.getOptionsMenuBar());
        assertNotNull(dataAccessPane.getButtonToolbar());

        assertNotNull(dataAccessPane.getSearchPluginTextField());
        assertEquals(dataAccessPane.getSearchPluginTextField().promptTextProperty().get(),
                "Type to search for a plugin");

        assertNotNull(dataAccessPane.getDataAccessTabPane());
        assertEquals(dataAccessPane.getDataAccessTabPane().getTabPane().getTabs().size(), 1);

        // TODO listener for date/range params
        // TODO update called
    }

    @Test
    public void testExceptions() throws Exception {
        MockedStatic<DataAccessPaneState> dataAccessPaneStateMockedStatic = Mockito.mockStatic(DataAccessPaneState.class);
        
         dataAccessPaneStateMockedStatic.when(() -> DataAccessPaneState
                    .getPlugins())
                    .thenThrow(new ExecutionException("test") {
                 });
         
        final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
        when(dataAccessPane.createCombo(any())).thenCallRealMethod();

        KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "Ctrl", "A", KeyCode.A, false, true, false, false);
        KeyCombination keyCombination = dataAccessPane.createCombo(keyEvent);
        assertTrue(keyCombination != null);
        assertTrue(keyCombination.getDisplayText().equals("Ctrl+A"));

        KeyEvent keyEvent1 = new KeyEvent(KeyEvent.KEY_PRESSED, "Shift", "A", KeyCode.A, true, false, false, false);
        KeyCombination keyCombination1 = dataAccessPane.createCombo(keyEvent1);
        assertTrue(keyCombination1 != null);
        assertTrue(keyCombination1.getDisplayText().equals("Shift+A"));

        KeyEvent keyEvent2 = new KeyEvent(KeyEvent.KEY_PRESSED, "Alt", "A", KeyCode.A, false, false, true, false);
        KeyCombination keyCombination2 = dataAccessPane.createCombo(keyEvent2);
        assertTrue(keyCombination2 != null);
        assertTrue(keyCombination2.getDisplayText().equals("Alt+A"));

        KeyEvent keyEvent3 = new KeyEvent(KeyEvent.KEY_PRESSED, "Meta", "A", KeyCode.A, false, false, false, true);
        KeyCombination keyCombination3 = dataAccessPane.createCombo(keyEvent3);
        assertTrue(keyCombination3 != null);
        assertTrue(keyCombination3.getDisplayText().equals("Meta+A"));

    }
    
}