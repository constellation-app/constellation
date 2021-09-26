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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.views.dataaccess.api.DataAccessPaneState;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessTabPaneNGTest {
    private DataAccessPane dataAccessPane;
    private Map<String, List<DataAccessPlugin>> plugins;
    
    private DataAccessTabPane dataAccessTabPane;
    
    public DataAccessTabPaneNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        FxToolkit.registerPrimaryStage();
        
        dataAccessPane = mock(DataAccessPane.class);
        
        plugins = Map.of();
        
        dataAccessTabPane = spy(new DataAccessTabPane(dataAccessPane, plugins));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        FxToolkit.cleanupStages();
    }
    
    @Test
    public void init() {
        assertSame(dataAccessTabPane.getDataAccessPane(), dataAccessPane);
        assertSame(dataAccessTabPane.getPlugins(), plugins);
        
        assertNotNull(dataAccessTabPane.getTabPane());
        assertEquals(dataAccessTabPane.getTabPane().getSide(), Side.TOP);
    }
    
    @Test
    public void newTab() {
        try (final MockedConstruction<QueryPhasePane> mockedQueryPhasePane =
                Mockito.mockConstruction(QueryPhasePane.class, (queryPhasePaneMock, cntxt) -> {
                    final List<Object> expectedArgs = new ArrayList<>();
                    expectedArgs.add(plugins);
                    expectedArgs.add(dataAccessPane);
                    expectedArgs.add(null);
                    
                    assertEquals(cntxt.arguments(), expectedArgs);
        })) {
            doNothing().when(dataAccessTabPane).newTab(any(QueryPhasePane.class));
            
            final QueryPhasePane pane = dataAccessTabPane.newTab();
            
            assertNotNull(pane);
            
            assertEquals(mockedQueryPhasePane.constructed().size(), 1);
            assertSame(mockedQueryPhasePane.constructed().get(0), pane);
            
            verify(dataAccessTabPane).newTab(pane);
        }
    }
    
    @Test
    public void newTab_provide_query_pane() {
        verifyNewTab(true, true, true, false, true);
        
        verifyNewTab(true, false, false, false, true);
        verifyNewTab(true, false, true, false, true);
        verifyNewTab(true, true, false, true, true);
        
        verifyNewTab(false, true, true, false, false);
        verifyNewTab(false, false, true, false, false);
        verifyNewTab(false, true, false, false, false);
        
        verifyNewTab(false, false, false, false, false);
    }

    @Test
    public void runTabs() {
        
    }
    
    private void verifyNewTab(final boolean tabHasEnabledPlugins,
                              final boolean isExecuteButtonIsGo,
                              final boolean isExecuteButtonDisabled,
                              final boolean expectGraphDependentMenuItemsEnabled,
                              final boolean expectPluginDependentMenuItemsEnabled) {
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        
        final EventHandler<Event> onCloseEventHandler = mock(EventHandler.class);
        
        final MenuItem runMenuItem = mock(MenuItem.class);
        final MenuItem runFromHereMenuItem = mock(MenuItem.class);
        final MenuItem runToHereMenuItem = mock(MenuItem.class);
        final MenuItem deactivateAllPluginsMenuItem = mock(MenuItem.class);
        
        final ContextMenu contextMenu = mock(ContextMenu.class);
        
        final TabPane tabPane = mock(TabPane.class);
        final ObservableList<Tab> tabs = FXCollections.observableArrayList();
        
        doNothing().when(dataAccessTabPane).updateTabMenu(any(Tab.class), anyBoolean(), anyBoolean());
        when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);
        when(tabPane.getTabs()).thenReturn(tabs);
        
        final ButtonToolbar buttonToolbar = mock(ButtonToolbar.class);
        final Button executeButton = mock(Button.class);
        
        when(dataAccessPane.getButtonToolbar()).thenReturn(buttonToolbar);
        when(buttonToolbar.getExecuteButton()).thenReturn(executeButton);
        when(executeButton.isDisabled()).thenReturn(isExecuteButtonDisabled);
        
        try (
                final MockedConstruction<Tab> mockedTab =
                        Mockito.mockConstruction(Tab.class, (tabMock, cntxt) -> {
                            final List<Object> expectedArgs = new ArrayList<>();
                            expectedArgs.add("Step 1");

                            assertEquals(cntxt.arguments(), expectedArgs);

                            when(tabMock.getOnClosed()).thenReturn(onCloseEventHandler);
                });
                final MockedConstruction<TabContextMenu> mockedTabContextMenu =
                        Mockito.mockConstruction(TabContextMenu.class, (contextMenuMock, cntxt) -> {
                            final List<Object> expectedArgs = new ArrayList<>();
                            expectedArgs.add(dataAccessTabPane);
                            expectedArgs.add(mockedTab.constructed().get(0));

                            assertEquals(cntxt.arguments(), expectedArgs);
                            
                            when(contextMenuMock.getContextMenu()).thenReturn(contextMenu);
                            
                            when(contextMenuMock.getRunMenuItem()).thenReturn(runMenuItem);
                            when(contextMenuMock.getRunFromHereMenuItem()).thenReturn(runFromHereMenuItem);
                            when(contextMenuMock.getRunToHereMenuItem()).thenReturn(runToHereMenuItem);
                            when(contextMenuMock.getDeactivateAllPluginsMenuItem()).thenReturn(deactivateAllPluginsMenuItem);
                });
                final MockedStatic<DataAccessPaneState> dapMockedStatic =
                        Mockito.mockStatic(DataAccessPaneState.class);
                final MockedStatic<DataAccessTabPane> datpStateMockedStatic =
                        Mockito.mockStatic(DataAccessTabPane.class);
                
            ) {
            
            dapMockedStatic.when(DataAccessPaneState::isExecuteButtonIsGo).thenReturn(isExecuteButtonIsGo);
            datpStateMockedStatic.when(() -> DataAccessTabPane.tabHasEnabledPlugins(any(Tab.class))).thenReturn(tabHasEnabledPlugins);
            
            dataAccessTabPane.newTab(queryPhasePane);
            
            assertEquals(mockedTab.constructed().size(), 1);
            assertEquals(mockedTabContextMenu.constructed().size(), 1);

            final TabContextMenu newTabContextMenu = mockedTabContextMenu.constructed().get(0);
            
            verify(newTabContextMenu).init();
            
            verify(queryPhasePane).addGraphDependentMenuItems(runMenuItem, runFromHereMenuItem, runToHereMenuItem);
            verify(queryPhasePane).addPluginDependentMenuItems(deactivateAllPluginsMenuItem);
            
            final Tab newTab = mockedTab.constructed().get(0);
            
            verify(newTab).setContextMenu(contextMenu);
            verify(newTab).setClosable(true);
            
            final ArgumentCaptor<ScrollPane> scrollPaneCaptor = ArgumentCaptor.forClass(ScrollPane.class);
            verify(newTab).setContent(scrollPaneCaptor.capture());
            assertTrue(scrollPaneCaptor.getValue().isFitToWidth());
            assertEquals(scrollPaneCaptor.getValue().getContent(), queryPhasePane);
            assertEquals(scrollPaneCaptor.getValue().getStyle(), "-fx-background-color: black;");
            
            final ArgumentCaptor<Tooltip> tooltipCaptor = ArgumentCaptor.forClass(Tooltip.class);
            verify(newTab).setTooltip(tooltipCaptor.capture());
            assertEquals(tooltipCaptor.getValue().getText(), "Right click for more options");

            verify(dataAccessTabPane).updateTabMenu(newTab, expectGraphDependentMenuItemsEnabled, expectPluginDependentMenuItemsEnabled);
            
            assertEquals(tabs, FXCollections.observableArrayList(newTab));
            
            final ArgumentCaptor<EventHandler<Event>> onCloseCaptor = ArgumentCaptor.forClass(EventHandler.class);
            verify(newTab).setOnClosed(onCloseCaptor.capture());
            
            final Event event = mock(Event.class);
            onCloseCaptor.getValue().handle(event);
            
            verify(newTab).setText("Step 1");
            verify(onCloseEventHandler).handle(event);
        }
    }
}
