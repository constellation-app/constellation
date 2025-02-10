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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.PluginFinder;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TabContextMenuNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(TabContextMenuNGTest.class.getName());
    
    private DataAccessTabPane dataAccessTabPane;
    private Tab tab;
    
    private TabContextMenu tabContextMenu;

    @BeforeClass   
    public void setUpClass() throws Exception {
     
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }
    
// Causing issues with headless tests 
    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }
    
    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessTabPane = mock(DataAccessTabPane.class);
        tab = mock(Tab.class);
        
        tabContextMenu = spy(new TabContextMenu(dataAccessTabPane, tab));
    }
    
    @Test
    public void init() {
        tabContextMenu.init();

        
        // Deactivate All Plugins
        
        assertEquals(tabContextMenu.getDeactivateAllPluginsMenuItem().getText(), "Deactivate all plugins");
        verifyDeactivateAllPluginsMenuItemAction(tabContextMenu.getDeactivateAllPluginsMenuItem());
        
        // Find Plugin
        
        assertEquals(tabContextMenu.getFindPluginMenuItem().getText(), "Find plugin...");
        verifyFindPluginMenuItemAction(tabContextMenu.getFindPluginMenuItem());
        
        // Open All Sections
        
        assertEquals(tabContextMenu.getOpenAllSectionsMenuItem().getText(), "Open all sections");
        verifyExpansionActions(tabContextMenu.getOpenAllSectionsMenuItem(), true, false);
        
        // Close All Sections
        
        assertEquals(tabContextMenu.getCloseAllSectionsMenuItem().getText(), "Close all sections");
        verifyExpansionActions(tabContextMenu.getCloseAllSectionsMenuItem(), false, false);
        
        // Run Menu Items
        
        final ObservableList<Tab> tabs = FXCollections.observableArrayList(
                mock(Tab.class),
                mock(Tab.class),
                tab,
                mock(Tab.class)
        );
        
        assertEquals(tabContextMenu.getRunMenuItem().getText(), "Run this tab only");
        verifyRunActions(tabContextMenu.getRunMenuItem(), tabs, 2, 2);
        
        assertEquals(tabContextMenu.getRunFromHereMenuItem().getText(), "Run from this tab");
        verifyRunActions(tabContextMenu.getRunFromHereMenuItem(), tabs, 2, 3);
        
        assertEquals(tabContextMenu.getRunToHereMenuItem().getText(), "Run to this tab");
        verifyRunActions(tabContextMenu.getRunToHereMenuItem(), tabs, 0, 2);
        
        // Context Menu
        
        assertEquals(tabContextMenu.getContextMenu().getItems().get(0), tabContextMenu.getDeactivateAllPluginsMenuItem());
        assertTrue(tabContextMenu.getContextMenu().getItems().get(1) instanceof SeparatorMenuItem);
        assertEquals(tabContextMenu.getContextMenu().getItems().get(2), tabContextMenu.getFindPluginMenuItem());
        assertEquals(tabContextMenu.getContextMenu().getItems().get(3), tabContextMenu.getOpenAllSectionsMenuItem());
        assertEquals(tabContextMenu.getContextMenu().getItems().get(4), tabContextMenu.getCloseAllSectionsMenuItem());
        assertTrue(tabContextMenu.getContextMenu().getItems().get(5) instanceof SeparatorMenuItem);
        assertEquals(tabContextMenu.getContextMenu().getItems().get(6), tabContextMenu.getRunMenuItem());
        assertEquals(tabContextMenu.getContextMenu().getItems().get(7), tabContextMenu.getRunFromHereMenuItem());
        assertEquals(tabContextMenu.getContextMenu().getItems().get(8), tabContextMenu.getRunToHereMenuItem());
    }
    
    /**
     * Verifies that the deactivate all plugins menu item will set the validity of all
     * plugins that are enabled to false when it is clicked.
     *
     * @param deactivateAllPluginsMenuItem the deactivate all plugins menu item
     */
    private void verifyDeactivateAllPluginsMenuItemAction(final MenuItem deactivateAllPluginsMenuItem) {
        try (final MockedStatic<DataAccessTabPane> tabPaneMockedStatic =
                Mockito.mockStatic(DataAccessTabPane.class)) {
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
            
            tabPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab))
                    .thenReturn(queryPhasePane);
            
            final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
            final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
            
            when(queryPhasePane.getDataAccessPanes())
                .thenReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2));
            
            when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
            when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);
            
            final ActionEvent actionEvent = mock(ActionEvent.class);
            
            deactivateAllPluginsMenuItem.getOnAction().handle(actionEvent);
            
            verify(dataSourceTitledPane2).validityChanged(false);
            verify(actionEvent).consume();
        }
    }
    
    /**
     * Verify the find plugin is run when the menu item is clicked.
     *
     * @param findPluginMenuItem the find plugin menu item
     */
    private void verifyFindPluginMenuItemAction(final MenuItem findPluginMenuItem) {
        try (
                final MockedStatic<DataAccessTabPane> tabPaneMockedStatic =
                        Mockito.mockStatic(DataAccessTabPane.class);
                final MockedStatic<Platform> platformMockedStatic =
                        Mockito.mockStatic(Platform.class);
            ) {
            
            final DataAccessPane dataAccessPane = mock(DataAccessPane.class);
            when(dataAccessTabPane.getDataAccessPane()).thenReturn(dataAccessPane);
            
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
            tabPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab))
                    .thenReturn(queryPhasePane);
            
            platformMockedStatic.when(() -> Platform.runLater(any(Runnable.class)))
                    .thenAnswer(iom -> {
                        // The code is run in the JavaFX thread, so intercept it and run locally
                        final Runnable action = iom.getArgument(0);
                        
                        // The code constructs a plugin finder and then calls find. Need to inject
                        // a mock into that construction
                        final MockedConstruction<PluginFinder> mockedPluginFinders =
                                Mockito.mockConstruction(PluginFinder.class);
                        
                        action.run();
                        
                        // Verify the plugin finder was created and used as expected
                        assertEquals(mockedPluginFinders.constructed().size(), 1);
                        verify(mockedPluginFinders.constructed().get(0)).find(queryPhasePane);
                        
                        return null;
                    });
            
            final ActionEvent actionEvent = mock(ActionEvent.class);
            
            findPluginMenuItem.getOnAction().handle(actionEvent);
            
            verify(actionEvent).consume();
        }
    }
    
    /**
     * Verifies that {@link QueryPhasePane#setHeadingsExpanded(boolean, boolean)} is
     * called with the passed values when the passed menu item is clicked.
     *
     * @param expansionMenuItem the menu item to click
     * @param expandHeadingSection true if the heading section should be expanded, false otherwise
     * @param expandChildrenAlso true if the children should also be expanded, false otherwise
     */
    private void verifyExpansionActions(final MenuItem expansionMenuItem,
                                        final boolean expandHeadingSection,
                                        final boolean expandChildrenAlso) {
        try (final MockedStatic<DataAccessTabPane> tabPaneMockedStatic =
                Mockito.mockStatic(DataAccessTabPane.class)) {
            final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
            tabPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab))
                    .thenReturn(queryPhasePane);
            
            
            final ActionEvent actionEvent = mock(ActionEvent.class);
            
            expansionMenuItem.getOnAction().handle(actionEvent);
            
            verify(queryPhasePane).setHeadingsExpanded(expandHeadingSection, expandChildrenAlso);
            verify(actionEvent).consume();
        }
    }
    
    /**
     * Verify that the {@link DataAccessTabPane#runTabs(int, int)} method is called
     * with the correct tab indexes based on the menu item action passed.
     *
     * @param runMenuItem the menu item to click
     * @param tabs the tabs that are on the tab pane, {@link tab} must be one of these
     * @param expectedFirstTab the index of the first tab to be run
     * @param expectedLastTab the index of the last tab to be run
     */
    private void verifyRunActions(final MenuItem runMenuItem,
                                  final ObservableList<Tab> tabs,
                                  final int expectedFirstTab,
                                  final int expectedLastTab) {
        final TabPane tabPane = mock(TabPane.class);
        when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);
        
        when(tabPane.getTabs()).thenReturn(tabs);
        
        final ActionEvent actionEvent = mock(ActionEvent.class);
        
        runMenuItem.getOnAction().handle(actionEvent);
        
        verify(dataAccessTabPane).runTabs(expectedFirstTab, expectedLastTab);
        verify(actionEvent).consume();
    }
}
