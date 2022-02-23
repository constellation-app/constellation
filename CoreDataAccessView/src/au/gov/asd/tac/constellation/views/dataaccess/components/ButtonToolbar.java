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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.listeners.ExecuteListener;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import au.gov.asd.tac.constellation.views.qualitycontrol.widget.QualityControlAutoButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Creates the button toolbar in the data access view that contains the help,
 * add, favourites and execute buttons.
 *
 * @author formalhaunt
 */
public class ButtonToolbar {

    private static final Logger LOGGER = Logger.getLogger(ButtonToolbar.class.getName());
    private static final String DATA_ACCESS_VIEW_TOP_COMPONENT_CLASS_NAME
            = DataAccessViewTopComponent.class.getName();

    private static final String NO_PLUGINS_SELECTED_TITLE = "No plugins selected.";

    private static final String MANAGE_FAVOURITES_TITLE = "Manage Favourites";
    private static final String FAVOURITES_DIALOG_MSG_FORMAT;

    private static final String ADD_FAVOURITE = "Add";
    private static final String REMOVE_FAVOURITE = "Remove";

    private static final String EXECUTE_GO = "Go";
    private static final String EXECUTE_STOP = "Stop";
    private static final String EXECUTE_CONTINUE = "Continue";
    private static final String EXECUTE_CALCULATING = "Calculating";

    private static final String GO_STYLE = "-fx-background-color: rgb(64,180,64); -fx-padding: 2 5 2 5;";
    private static final String STOP_STYLE = "-fx-background-color: rgb(180,64,64); -fx-padding: 2 5 2 5;";
    private static final String CONTINUE_STYLE = "-fx-background-color: rgb(255,180,0); -fx-padding: 2 5 2 5;";
    private static final String CALCULATING_STYLE = "-fx-background-color: rgb(0,100,255); -fx-padding: 2 5 2 5;";
    private static final String HELP_STYLE = "-fx-border-color: transparent;-fx-background-color: transparent;";

    private static final ImageView HELP_ICON = new ImageView(UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor()));
    private static final ImageView ADD_ICON = new ImageView(UserInterfaceIconProvider.ADD.buildImage(16));
    private static final ImageView FAVOURITE_ICON = new ImageView(AnalyticIconProvider.STAR.buildImage(16, ConstellationColor.YELLOW.getJavaColor()));

    private static final String HELP_TOOLTIP = "Display help for Data Access";
    private static final String ADD_TOOLTIP = "Add new run tab";
    private static final String FAVOURITE_TOOLTIP = "Manage your favourites";

    static {
        FAVOURITES_DIALOG_MSG_FORMAT = new StringBuilder(300)
                .append("Add or remove plugins from your favourites category.\n\n")
                .append("The following plugins were selected:\n")
                .append("%s")
                .append("\n\nNote that you need to restart before changes take effect.")
                .toString();
    }

    private final DataAccessPane dataAccessPane;

    private Button helpButton;
    private Button addButton;
    private Button favouriteButton;

    private Button executeButtonTop;
    private Button executeButtonBottom;

    private GridPane optionsToolbar;

    private HBox helpAddFavHBox;
    private HBox rabRegionExectueHBoxTop;
    private HBox rabRegionExectueHBoxBottom;

    /**
     * Creates a new data access view button toolbar.
     *
     * @param dataAccessPane the data access pane that the toolbar will be added
     * to
     */
    public ButtonToolbar(final DataAccessPane dataAccessPane) {
        this.dataAccessPane = dataAccessPane;
    }

    /**
     * Initializes the button toolbar. Until this method is called, all toolbar
     * UI components will be null.
     */
    public void init() {

        ////////////////////
        // Help Button
        ////////////////////
        helpButton = new Button("", HELP_ICON);
        helpButton.paddingProperty().set(new Insets(0, 8, 0, 0));
        helpButton.setTooltip(new Tooltip(HELP_TOOLTIP));
        helpButton.setOnAction(actionEvent -> {
            new HelpCtx(DATA_ACCESS_VIEW_TOP_COMPONENT_CLASS_NAME).display();

            actionEvent.consume();
        });

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle(HELP_STYLE);

        ////////////////////
        // Add Button
        ////////////////////
        addButton = new Button("", ADD_ICON);
        addButton.setTooltip(new Tooltip(ADD_TOOLTIP));
        addButton.setOnAction(actionEvent -> {
            final PluginParameters previousGlobals = getTabs().size() > 0 ?
                    DataAccessTabPane.getQueryPhasePane(getTabs().get(getTabs().size() - 1))
                        .getGlobalParametersPane().getParams() : null;
            
            dataAccessPane.getDataAccessTabPane().newTab(previousGlobals, "");
            
            actionEvent.consume();
        });

        ////////////////////
        // Favourite Button
        ////////////////////
        favouriteButton = new Button("", FAVOURITE_ICON);
        favouriteButton.setTooltip(new Tooltip(FAVOURITE_TOOLTIP));
        favouriteButton.setOnAction(actionEvent -> {
            manageFavourites();

            actionEvent.consume();
        });

        ////////////////////
        // Execute Button
        ////////////////////
        final ExecuteListener executeButtonListener = new ExecuteListener(dataAccessPane);

        executeButtonTop = new Button(EXECUTE_GO);
        executeButtonTop.setStyle(GO_STYLE);
        executeButtonTop.setOnAction(executeButtonListener);

        executeButtonBottom = new Button(EXECUTE_GO);
        executeButtonBottom.setStyle(GO_STYLE);
        executeButtonBottom.setOnAction(executeButtonListener);

        ////////////////////
        // Button Container
        ////////////////////
        helpAddFavHBox = new HBox(helpButton, addButton, favouriteButton);
        helpAddFavHBox.setSpacing(4);

        rabRegionExectueHBoxTop = new HBox();
        rabRegionExectueHBoxTop.setSpacing(4);

        rabRegionExectueHBoxBottom = new HBox();
        rabRegionExectueHBoxBottom.setAlignment(Pos.TOP_RIGHT);
        rabRegionExectueHBoxBottom.setPadding(new Insets(10));

        Optional.ofNullable(Lookup.getDefault().lookup(QualityControlAutoButton.class))
                .ifPresent(button -> {
                    rabRegionExectueHBoxTop.getChildren().add(button);
                    rabRegionExectueHBoxBottom.getChildren().add(button.copy());
                });

        // add some padding between the Go button and the previous button to avoid accidental clicking
        final Region regionTop = new Region();
        regionTop.setMinSize(20, 0);

        final Region regionBottom = new Region();
        regionBottom.setMinSize(27, 0);

        rabRegionExectueHBoxTop.getChildren().addAll(regionTop, executeButtonTop);
        rabRegionExectueHBoxBottom.getChildren().addAll(regionBottom, executeButtonBottom);

        optionsToolbar = new GridPane();
        optionsToolbar.setPadding(new Insets(4));
        optionsToolbar.setHgap(4);
        optionsToolbar.setVgap(4);
        optionsToolbar.add(helpAddFavHBox, 0, 0);
        optionsToolbar.add(rabRegionExectueHBoxTop, 1, 0);
    }

    /**
     * Change the execute buttons state, changing the text, style and ensuring
     * it enablement state remains unchanged.
     *
     * @param state the new state to set the execute button to
     */
    public void changeExecuteButtonState(final ExecuteButtonState state) {
        changeExecuteButtonState(state, getExecuteButtonTop().isDisable());
    }

    /**
     * Change the execute buttons state, changing the text, style and ensuring
     * it disable status is set to the passed flag.
     *
     * @param state the new state to set the execute button to
     * @param disable true if the button should be disabled, false otherwise
     */
    public void changeExecuteButtonState(final ExecuteButtonState state,
            final boolean disable) {
        Platform.runLater(() -> {
            getExecuteButtonTop().setText(state.getText());
            getExecuteButtonTop().setStyle(state.getStyle());
            getExecuteButtonTop().setDisable(disable);

            getExecuteButtonBottom().setText(state.getText());
            getExecuteButtonBottom().setStyle(state.getStyle());
            getExecuteButtonBottom().setDisable(disable);
        });
    }

    /**
     * Sets the grid pane to be a 2 x 1 grid or single column.
     */
    public void handleShrinkingPane() {
        getOptionsToolbar().getChildren().remove(getHelpAddFavHBox());
        getOptionsToolbar().getChildren().remove(getRabRegionExectueHBoxTop());

        getOptionsToolbar().add(getRabRegionExectueHBoxTop(), 0, 0);
        getOptionsToolbar().add(getHelpAddFavHBox(), 0, 1);

        GridPane.setHalignment(getHelpAddFavHBox(), HPos.LEFT);
    }

    /**
     * Sets the grid pane to be 1 x 2 grid or a single row.
     */
    public void handleGrowingPane() {
        getOptionsToolbar().getChildren().remove(getHelpAddFavHBox());
        getOptionsToolbar().getChildren().remove(getRabRegionExectueHBoxTop());

        getOptionsToolbar().add(getHelpAddFavHBox(), 0, 0);
        getOptionsToolbar().add(getRabRegionExectueHBoxTop(), 1, 0);

        GridPane.setHalignment(getHelpAddFavHBox(), HPos.CENTER);
    }

    /**
     * Get the help button that displays help information for the data access
     * view.
     *
     * @return the help button
     */
    public Button getHelpButton() {
        return helpButton;
    }

    /**
     * Gets the add button that adds a new tab to the data access view's tab
     * pane.
     *
     * @return the add button
     */
    public Button getAddButton() {
        return addButton;
    }

    /**
     * Gets the favourite button that allows a user to add or remove selected
     * plugins in the current tab to/from their favourite preferences.
     *
     * @return the favourite button
     */
    public Button getFavouriteButton() {
        return favouriteButton;
    }

    /**
     * Gets the execute button at the top of the data access view which when
     * clicked will run all the selected plugins.
     *
     * @return the top execute button
     */
    public Button getExecuteButtonTop() {
        return executeButtonTop;
    }

    /**
     * Gets the execute button at the bottom of the data access view which when
     * clicked will run all the selected plugins.
     *
     * @return the bottom execute button
     */
    public Button getExecuteButtonBottom() {
        return executeButtonBottom;
    }

    /**
     * The options toolbar contains the {@link #getHelpAddFavHBox()} and
     * {@link #getRabRegionExectueHBoxTop()}, displayed in a 1 x 2 grid.
     *
     * @return the options toolbar
     */
    public GridPane getOptionsToolbar() {
        return optionsToolbar;
    }

    /**
     * A horizontal box containing the help, add and favourite buttons.
     *
     * @return the help, add and favourite buttons in a horizontal box
     */
    public HBox getHelpAddFavHBox() {
        return helpAddFavHBox;
    }

    /**
     * A horizontal box containing the top execute button.
     *
     * @return the top execute button in a horizontal box
     */
    public HBox getRabRegionExectueHBoxTop() {
        return rabRegionExectueHBoxTop;
    }

    /**
     * A horizontal box containing the bottom execute button.
     *
     * @return the bottom execute button in a horizontal box
     */
    public HBox getRabRegionExectueHBoxBottom() {
        return rabRegionExectueHBoxBottom;
    }

    /**
     * The data access pane in the data access view.
     *
     * @return the current data access pane
     */
    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }

    /**
     * Add and remove plugins from the favourites section. Collects all selected
     * plugins from the current tab and asks the user through a dialog if they
     * want to add or remove the selected plugins to/from their favourites.
     */
    protected void manageFavourites() {
        // Get a list of the selected plugins
        final List<String> selectedPlugins = new ArrayList<>();
        getDataAccessPane().getDataAccessTabPane().getQueryPhasePaneOfCurrentTab()
                .getDataAccessPanes().stream().forEach(dataSourceTitledPane -> {
                    if (dataSourceTitledPane.isQueryEnabled()) {
                        selectedPlugins.add(dataSourceTitledPane.getPlugin().getName());
                    }
                });

        if (selectedPlugins.isEmpty()) {
            NotifyDisplayer.display(
                    NO_PLUGINS_SELECTED_TITLE,
                    NotifyDescriptor.WARNING_MESSAGE
            );
        } else {
            final String message = String.format(
                    FAVOURITES_DIALOG_MSG_FORMAT,
                    selectedPlugins.stream()
                            .collect(Collectors.joining(SeparatorConstants.NEWLINE))
            );

            // Display a dialog asking if they want to add or remove the selected
            // plugins from their favourites
            final NotifyDescriptor nde = new NotifyDescriptor(
                    message,
                    MANAGE_FAVOURITES_TITLE,
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[]{
                        ADD_FAVOURITE,
                        REMOVE_FAVOURITE,
                        NotifyDescriptor.CANCEL_OPTION
                    },
                    NotifyDescriptor.OK_OPTION
            );

            NotifyDisplayer.displayAndWait(nde)
                    .thenAccept(option -> {
                        try {
                          // If the users seclection was cancel, then do nothing. Otherwise add or
                          // remove the selected plugins from the favourites based on the users selection
                            final String optionValue = (String) ((CompletableFuture) option).get();
                            if (option != NotifyDescriptor.CANCEL_OPTION) {
                                selectedPlugins.stream().forEach(name
                                        -> DataAccessPreferenceUtilities.setFavourite(name, ADD_FAVOURITE.equals(optionValue))
                                );
                            }
                        } catch (final Throwable throwable) {
                            LOGGER.log(Level.INFO,("Failed to get user option to add or remove favourites"),
                                    throwable);
                        }
                    });
        }
    }

    /**
     * Convenience method for accessing the tabs in the current data access tab
     * pane.
     *
     * @return a list of currently open tabs in the data access view
     */
    protected ObservableList<Tab> getTabs() {
        return dataAccessPane.getDataAccessTabPane().getTabPane().getTabs();
    }

    /**
     * The states that the execute button can be in. Each state has a different
     * display text and style that should be applied to the execute button.
     */
    public enum ExecuteButtonState {
        GO(EXECUTE_GO, GO_STYLE),
        STOP(EXECUTE_STOP, STOP_STYLE),
        CONTINUE(EXECUTE_CONTINUE, CONTINUE_STYLE),
        CALCULATING(EXECUTE_CALCULATING, CALCULATING_STYLE);

        private final String text;
        private final String style;

        /**
         * Creates a new execute button state.
         *
         * @param text the text on the execute button when in this state
         * @param style the style on the execute button when in this state
         */
        private ExecuteButtonState(final String text, final String style) {
            this.text = text;
            this.style = style;
        }

        public String getText() {
            return text;
        }

        public String getStyle() {
            return style;
        }
    }
}
