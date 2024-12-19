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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.components.ButtonToolbar;
import au.gov.asd.tac.constellation.views.dataaccess.components.DataAccessTabPane;
import au.gov.asd.tac.constellation.views.dataaccess.components.OptionsMenuBar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class DataAccessPaneExceptionNGTest {

    private static final Logger LOGGER = Logger.getLogger(DataAccessPaneExceptionNGTest.class.getName());

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

    @Test(expectedExceptions = {ExecutionException.class, IllegalStateException.class})
    public void testException() throws Exception {

        MockedStatic<DataAccessPaneState> dataAccessPaneStateMockedStatic = Mockito.mockStatic(DataAccessPaneState.class);
        dataAccessPaneStateMockedStatic.when(() -> DataAccessPaneState.getPlugins())
                .thenThrow(new ExecutionException("test") {
                });

        DataAccessPane dataAccessPane1 = spy(new DataAccessPane(mock(DataAccessViewTopComponent.class)));

    }
}
