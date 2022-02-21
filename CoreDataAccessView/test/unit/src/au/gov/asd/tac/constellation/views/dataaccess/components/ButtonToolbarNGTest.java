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

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.views.dataaccess.listeners.ExecuteListener;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataSourceTitledPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.GlobalParametersPane;
import au.gov.asd.tac.constellation.views.dataaccess.panes.QueryPhasePane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import au.gov.asd.tac.constellation.views.qualitycontrol.daemon.QualityControlState;
import au.gov.asd.tac.constellation.views.qualitycontrol.widget.QualityControlAutoButton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
public class ButtonToolbarNGTest {
    private static final Logger LOGGER = Logger.getLogger(ButtonToolbarNGTest.class.getName());
    
    private DataAccessPane dataAccessPane;
    
    private ButtonToolbar buttonToolbar;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

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
        dataAccessPane = mock(DataAccessPane.class);
        
        buttonToolbar = spy(new ButtonToolbar(dataAccessPane));
    }
    
    @Test
    public void init() {
        final QualityControlAutoButton qualityControlAutoButtonCopy = new QualityControlAutoButton() {
            @Override
            protected void update(QualityControlState state) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public QualityControlAutoButton copy() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        final QualityControlAutoButton qualityControlAutoButton = new QualityControlAutoButton() {
            @Override
            protected void update(QualityControlState state) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public QualityControlAutoButton copy() {
                return qualityControlAutoButtonCopy;
            }
        };
        
        try (final MockedStatic<Lookup> lookupMockedStatic = Mockito.mockStatic(Lookup.class)) {
            
            final Lookup lookup = mock(Lookup.class);
            lookupMockedStatic.when(Lookup::getDefault).thenReturn(lookup);
            
            when(lookup.lookup(QualityControlAutoButton.class)).thenReturn(qualityControlAutoButton);
            
            buttonToolbar.init();
        }
        
        // Help Button
        
        verifyButton(
                buttonToolbar.getHelpButton(),
                "",
                "-fx-border-color: transparent;-fx-background-color: transparent;",
                "Display help for Data Access",
                UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor())
        );
        assertEquals(buttonToolbar.getHelpButton().getPadding(), new Insets(0, 8, 0, 0));
        
        // Add Button
        
        verifyButton(
                buttonToolbar.getAddButton(),
                "",
                "",
                "Add new run tab",
                UserInterfaceIconProvider.ADD.buildImage(16)
        );
        
        verifyAddTabAction(buttonToolbar.getAddButton());
        
        // Favourite Button
        
        verifyButton(
                buttonToolbar.getFavouriteButton(),
                "",
                "",
                "Manage your favourites",
                AnalyticIconProvider.STAR.buildImage(16, ConstellationColor.YELLOW.getJavaColor())
        );
        
        doNothing().when(buttonToolbar).manageFavourites();
        
        final ActionEvent actionEvent = mock(ActionEvent.class);
        buttonToolbar.getFavouriteButton().getOnAction().handle(actionEvent);
        
        verify(buttonToolbar).manageFavourites();
        verify(actionEvent).consume();
        
        
        // Execute Button
        
        verifyButton(
                buttonToolbar.getExecuteButtonTop(),
                "Go",
                "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;",
                null,
                null
        );
        
        verifyButton(
                buttonToolbar.getExecuteButtonBottom(),
                "Go",
                "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;",
                null,
                null
        );
        
        assertTrue(buttonToolbar.getExecuteButtonTop().getOnAction() instanceof ExecuteListener);
        assertTrue(buttonToolbar.getExecuteButtonBottom().getOnAction() instanceof ExecuteListener);
        assertSame(buttonToolbar.getExecuteButtonTop().getOnAction(), buttonToolbar.getExecuteButtonBottom().getOnAction());
        
        // Help, Add and Favourite HBox
        
        assertEquals(
                buttonToolbar.getHelpAddFavHBox().getChildren(),
                List.of(
                        buttonToolbar.getHelpButton(),
                        buttonToolbar.getAddButton(),
                        buttonToolbar.getFavouriteButton()
                )
        );
        assertEquals(buttonToolbar.getHelpAddFavHBox().getSpacing(), 4.0);
        
        // Rab Region Execute HBox Top
        
        assertSame(
                buttonToolbar.getRabRegionExectueHBoxTop().getChildren().get(0),
                qualityControlAutoButton
        );
        
        final Region topSpacer = (Region) buttonToolbar.getRabRegionExectueHBoxTop().getChildren().get(1);
        assertEquals(topSpacer.getMinWidth(), 20.0);
        assertEquals(topSpacer.getMinHeight(), 0.0);
        
        assertEquals(
                buttonToolbar.getRabRegionExectueHBoxTop().getChildren().get(2),
                buttonToolbar.getExecuteButtonTop()
        );
        
        // Rab Region Execute HBox Bottom
        
        assertSame(
                buttonToolbar.getRabRegionExectueHBoxBottom().getChildren().get(0),
                qualityControlAutoButtonCopy
        );
        
        final Region bottomSpacer = (Region) buttonToolbar.getRabRegionExectueHBoxBottom().getChildren().get(1);
        assertEquals(bottomSpacer.getMinWidth(), 27.0);
        assertEquals(bottomSpacer.getMinHeight(), 0.0);
        
        assertEquals(
                buttonToolbar.getRabRegionExectueHBoxBottom().getChildren().get(2),
                buttonToolbar.getExecuteButtonBottom()
        );
        
        // Options Toolbar
        
        assertEquals(buttonToolbar.getOptionsToolbar().getPadding(), new Insets(4));
        assertEquals(buttonToolbar.getOptionsToolbar().getHgap(), 4.0);
        assertEquals(buttonToolbar.getOptionsToolbar().getVgap(), 4.0);
        assertEquals(buttonToolbar.getOptionsToolbar().getRowCount(), 1);
        assertEquals(buttonToolbar.getOptionsToolbar().getColumnCount(), 2);
        assertEquals(
                buttonToolbar.getOptionsToolbar().getChildren(),
                FXCollections.observableArrayList(
                        buttonToolbar.getHelpAddFavHBox(),
                        buttonToolbar.getRabRegionExectueHBoxTop()
                )
        );
    }
    
    @Test
    public void changeExecuteButtonState_withoutDisableFlag() {
        final Button executeButton = mock(Button.class);
        doReturn(executeButton).when(buttonToolbar).getExecuteButtonTop();
        doReturn(executeButton).when(buttonToolbar).getExecuteButtonBottom();
        
        when(executeButton.isDisable()).thenReturn(false);
        
        buttonToolbar.changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.STOP);
        
        WaitForAsyncUtils.waitForFxEvents();
        
        verify(executeButton, times(2)).setText("Stop");
        verify(executeButton, times(2)).setStyle("-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;");
        verify(executeButton, times(2)).setDisable(false);
    }
    
    @Test
    public void changeExecuteButtonState_withDisableFlag() {
        final Button executeButton = mock(Button.class);
        doReturn(executeButton).when(buttonToolbar).getExecuteButtonTop();
        doReturn(executeButton).when(buttonToolbar).getExecuteButtonBottom();
        
        buttonToolbar.changeExecuteButtonState(ButtonToolbar.ExecuteButtonState.STOP, false);
        
        WaitForAsyncUtils.waitForFxEvents();
        
        verify(executeButton, times(2)).setText("Stop");
        verify(executeButton, times(2)).setStyle("-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;");
        verify(executeButton, times(2)).setDisable(false);
    }
    
    @Test
    public void resizingPanes() {
        buttonToolbar.init();
        
        buttonToolbar.handleShrinkingPane();
        
        assertEquals(
                buttonToolbar.getOptionsToolbar().getChildren(),
                FXCollections.observableArrayList(
                        buttonToolbar.getRabRegionExectueHBoxTop(),
                        buttonToolbar.getHelpAddFavHBox()
                )
        );
        
        assertEquals(buttonToolbar.getOptionsToolbar().getRowCount(), 2);
        assertEquals(buttonToolbar.getOptionsToolbar().getColumnCount(), 1);
        
        assertEquals(GridPane.getHalignment(buttonToolbar.getHelpAddFavHBox()), HPos.LEFT);
        
        buttonToolbar.handleGrowingPane();
        
        assertEquals(
                buttonToolbar.getOptionsToolbar().getChildren(),
                FXCollections.observableArrayList(
                        buttonToolbar.getHelpAddFavHBox(),
                        buttonToolbar.getRabRegionExectueHBoxTop()
                )
        );
        
        assertEquals(buttonToolbar.getOptionsToolbar().getRowCount(), 1);
        assertEquals(buttonToolbar.getOptionsToolbar().getColumnCount(), 2);
        
        assertEquals(GridPane.getHalignment(buttonToolbar.getHelpAddFavHBox()), HPos.CENTER);
    }
    
    @Test
    public void manageFavourites_no_plugins_selected() {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final QueryPhasePane currentQueryPhasePane = mock(QueryPhasePane.class);
        
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getQueryPhasePaneOfCurrentTab()).thenReturn(currentQueryPhasePane);
        when(currentQueryPhasePane.getDataAccessPanes()).thenReturn(List.of());
        
        try (final MockedStatic<NotifyDisplayer> notifyDisplayerMockedStatic =
                Mockito.mockStatic(NotifyDisplayer.class)) {
            buttonToolbar.manageFavourites();
            
            notifyDisplayerMockedStatic.verify(() -> NotifyDisplayer
                    .display("No plugins selected.", NotifyDescriptor.WARNING_MESSAGE));
        }
    }
    
    @Test
    public void manageFavourites_plugins_user_selects_add() {
        final String pluginTitle = "myPlugin";
        
        verifyManageFavouritesWithUserInput(
                pluginTitle,
                "Add",
                () -> DataAccessPreferenceUtilities.setFavourite(pluginTitle, true));
    }
    
    @Test
    public void manageFavourites_plugins_user_selects_remove() {
        final String pluginTitle = "myPlugin";
        
        verifyManageFavouritesWithUserInput(
                pluginTitle,
                "Remove",
                () -> DataAccessPreferenceUtilities.setFavourite(pluginTitle, false));
    }
    
    @Test
    public void manageFavourites_plugins_user_selects_cancel() {
        final String pluginTitle = "myPlugin";
        
        verifyManageFavouritesWithUserInput(
                pluginTitle,
                NotifyDescriptor.CANCEL_OPTION,
                null);
    }
    
    @Test
    public void executeButtonStates() {
        assertEquals(ButtonToolbar.ExecuteButtonState.GO.getText(), "Go");
        assertEquals(ButtonToolbar.ExecuteButtonState.GO.getStyle(), "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;");
        
        assertEquals(ButtonToolbar.ExecuteButtonState.STOP.getText(), "Stop");
        assertEquals(ButtonToolbar.ExecuteButtonState.STOP.getStyle(), "-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;");
        
        assertEquals(ButtonToolbar.ExecuteButtonState.CONTINUE.getText(), "Continue");
        assertEquals(ButtonToolbar.ExecuteButtonState.CONTINUE.getStyle(), "-fx-background-color: rgb(255,180,0); -fx-padding: 2 5 2 5;");
        
        assertEquals(ButtonToolbar.ExecuteButtonState.CALCULATING.getText(), "Calculating");
        assertEquals(ButtonToolbar.ExecuteButtonState.CALCULATING.getStyle(), "-fx-background-color: rgb(0,100,255); -fx-padding: 2 5 2 5;");
    }
    
    /**
     * Verifies that when manage favourites is called, depending on the user action
     * against the dialog, different outcomes will occur.
     *
     * @param pluginTitle the plugin title of the only selected pane that is enabled
     * @param selectedOption the option that the user selection in the dialog
     * @param preferenceUtilsVerification the verification against {@link DataAccessPreferenceUtilities}
     */
    private void verifyManageFavouritesWithUserInput(final String pluginTitle,
                                                     final Object selectedOption,
                                                     final Verification preferenceUtilsVerification) {
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        final QueryPhasePane currentQueryPhasePane = mock(QueryPhasePane.class);
        
        final DataSourceTitledPane dataSourceTitledPane1 = mock(DataSourceTitledPane.class);
        final DataSourceTitledPane dataSourceTitledPane2 = mock(DataSourceTitledPane.class);
        
        final Plugin plugin = mock(Plugin.class);
        
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        when(dataAccessTabPane.getQueryPhasePaneOfCurrentTab()).thenReturn(currentQueryPhasePane);
        when(currentQueryPhasePane.getDataAccessPanes())
                .thenReturn(List.of(dataSourceTitledPane1, dataSourceTitledPane2));
        
        when(dataSourceTitledPane1.isQueryEnabled()).thenReturn(false);
        when(dataSourceTitledPane2.isQueryEnabled()).thenReturn(true);
        
        when(dataSourceTitledPane2.getPlugin()).thenReturn(plugin);
        when(plugin.getName()).thenReturn(pluginTitle);
        
        try (
                final MockedStatic<NotifyDisplayer> notifyDisplayerMockedStatic =
                        Mockito.mockStatic(NotifyDisplayer.class);
                final MockedStatic<DataAccessPreferenceUtilities> prefUtilsMockedStatic =
                        Mockito.mockStatic(DataAccessPreferenceUtilities.class);
        ) {
            notifyDisplayerMockedStatic.when(() -> NotifyDisplayer.displayAndWait(any(NotifyDescriptor.class)))
                    .thenAnswer(iom -> {
                        final NotifyDescriptor descriptor = iom.getArgument(0);
                        
                        final String expectedMessage = "Add or remove plugins from your favourites category.\n\n"
                            + "The following plugins were selected:\n"
                            + pluginTitle + "\n"
                            + "\nNote that you need to restart before changes take effect.";
            
                        assertEquals(descriptor.getMessage(), expectedMessage);
                        assertEquals(descriptor.getTitle(), "Manage Favourites");
                        assertEquals(descriptor.getOptionType(), NotifyDescriptor.DEFAULT_OPTION);
                        assertEquals(descriptor.getMessageType(), NotifyDescriptor.QUESTION_MESSAGE);
                        assertEquals(descriptor.getOptions(), new Object[]{"Add", "Remove", NotifyDescriptor.CANCEL_OPTION});
                        assertEquals(descriptor.getValue(), NotifyDescriptor.OK_OPTION);
                        
                        return CompletableFuture.completedFuture(selectedOption);
                    });
            
            buttonToolbar.manageFavourites();
            
            if (preferenceUtilsVerification != null) {
                prefUtilsMockedStatic.verify(preferenceUtilsVerification);
            } else {
                prefUtilsMockedStatic.verifyNoInteractions();
            }
        }
    }
    
    /**
     * Verifies that the add button creates a new tab using the global plugin configuration
     * from the last tab if available.
     *
     * @param addButton the button to verify
     */
    private void verifyAddTabAction(final Button addButton) {
        final ActionEvent actionEvent = mock(ActionEvent.class);
        
        final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
        when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
        
        // Set up the current tab pane. The code will take the last tab, i.e. tab2
        final Tab tab1 = mock(Tab.class);
        final Tab tab2 = mock(Tab.class);
        final ObservableList<Tab> tabs = FXCollections.observableArrayList(tab1, tab2);
        doReturn(tabs).when(buttonToolbar).getTabs();
        
        final QueryPhasePane queryPhasePane = mock(QueryPhasePane.class);
        final GlobalParametersPane globalParametersPane = mock(GlobalParametersPane.class);
        final PluginParameters pluginParameters = mock(PluginParameters.class);
        
        when(queryPhasePane.getGlobalParametersPane()).thenReturn(globalParametersPane);
        when(globalParametersPane.getParams()).thenReturn(pluginParameters);
        
        try (final MockedStatic<DataAccessTabPane> tabPaneMockedStatic =
                Mockito.mockStatic(DataAccessTabPane.class)) {
            // There will be no interaction with tab1 so only mock out tab2
            tabPaneMockedStatic.when(() -> DataAccessTabPane.getQueryPhasePane(tab2))
                    .thenReturn(queryPhasePane);
            
            // Run the action
            addButton.getOnAction().handle(actionEvent);
            
            // Verify a new tab is created with the existing last tab's parameters
            verify(dataAccessTabPane).newTab(pluginParameters, "");
            verify(actionEvent).consume();
        }
    }
    
    /**
     * Verifies the passed button has the correct text, style, tool tip and icon.
     *
     * @param button the button to test
     * @param text the text that should be on the button
     * @param style the style the button should have
     * @param tooltipText the tool tip text the button should have or null if
     *     no tool tip should be set
     * @param icon the icon that should be on the button or null if no icon
     *     should be set
     */
    private void verifyButton(final Button button,
                              final String text,
                              final String style,
                              final String tooltipText,
                              final Image icon) {
        assertEquals(button.getText(), text);
        assertEquals(button.getStyle(), style);
        
        if (tooltipText != null) {
            assertEquals(button.getTooltip().getText(), tooltipText);
        } else {
            assertNull(button.getTooltip());
        }
        
        if (icon != null) {
            assertTrue(isImageEqual(
                    ((ImageView) button.getGraphic()).getImage(), icon
            ));
        } else {
            assertNull(button.getGraphic());
        }
    }
    
    /**
     * Verifies that two JavaFX images are equal. Unfortunately they don't provide
     * a nice way to do this so we check pixel by pixel.
     *
     * @param firstImage the first image to compare
     * @param secondImage the second image to compare
     * @return true if the images are the same, false otherwise
     */
    private static boolean isImageEqual(Image firstImage, Image secondImage) {
        // Prevent `NullPointerException`
        if(firstImage != null && secondImage == null) {
            return false;
        }
        
        if(firstImage == null) {
            return secondImage == null;
        }

        // Compare images size
        if(firstImage.getWidth() != secondImage.getWidth()) {
            return false;
        }
        
        if(firstImage.getHeight() != secondImage.getHeight()) {
            return false;
        }

        // Compare images color
        for(int x = 0; x < firstImage.getWidth(); x++){
            for(int y = 0; y < firstImage.getHeight(); y++){
                int firstArgb = firstImage.getPixelReader().getArgb(x, y);
                int secondArgb = secondImage.getPixelReader().getArgb(x, y);

                if(firstArgb != secondArgb) return false;
            }
        }

        return true;
    }
}
