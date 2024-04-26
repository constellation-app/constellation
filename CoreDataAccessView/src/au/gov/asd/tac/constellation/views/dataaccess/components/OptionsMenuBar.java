/*
 * Copyright 2010-2023 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.log.LogPreferences;
import au.gov.asd.tac.constellation.views.dataaccess.io.DataAccessParametersIoProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Creates a options menu that is added to the Data Access view panel.
 *
 * @author formalhaunt
 */
public class OptionsMenuBar {

    private static final ImageView SETTINGS_ICON;
    private static final ImageView SAVE_TEMPLATE_ICON;
    private static final ImageView LOAD_TEMPLATE_ICON;
    private static final ImageView SAVE_RESULTS_ICON;
    private static final ImageView UNCHECKED_ICON;
    private static final ImageView LOGGER_ICON;

    private static final String LOAD_MENU_ITEM_TEXT = "Load Templates";
    private static final String SAVE_MENU_ITEM_TEXT = "Save Templates";
    private static final String SAVE_RESULTS_MENU_ITEM_TEXT = "Save Results";
    private static final String DESELECT_PLUGINS_ON_EXECUTION_MENU_ITEM_TEXT = "Deselect On Go";
    private static final String CONNECTION_LOGGING_TEXT = "Connection Logging";
    private static final String TITLE = "Folder to save data access results to";

    private static final String OPTIONS_MENU_TEXT = "Workflow Options";
    private static final String ICON_SET = JavafxStyleManager.isDarkTheme() ? "Light" : "Dark";

    static {
        SETTINGS_ICON = new ImageView(new Image(
                OptionsMenuBar.class.getResourceAsStream("resources/DataAccessSettings.png")));
        SETTINGS_ICON.setFitHeight(20);
        SETTINGS_ICON.setFitWidth(20);

        SAVE_TEMPLATE_ICON = new ImageView(new Image(
                OptionsMenuBar.class.getResourceAsStream("resources/DataAccessSaveTemplate" + ICON_SET + ".png")));
        SAVE_TEMPLATE_ICON.setFitHeight(15);
        SAVE_TEMPLATE_ICON.setFitWidth(15);

        LOAD_TEMPLATE_ICON = new ImageView(new Image(
                OptionsMenuBar.class.getResourceAsStream("resources/DataAccessLoadTemplate" + ICON_SET + ".png")));
        LOAD_TEMPLATE_ICON.setFitHeight(15);
        LOAD_TEMPLATE_ICON.setFitWidth(15);

        SAVE_RESULTS_ICON = new ImageView(new Image(
                OptionsMenuBar.class.getResourceAsStream("resources/DataAccessSaveResults" + ICON_SET + ".png")));
        SAVE_RESULTS_ICON.setFitHeight(15);
        SAVE_RESULTS_ICON.setFitWidth(15);

        UNCHECKED_ICON = new ImageView(new Image(
                OptionsMenuBar.class.getResourceAsStream("resources/DataAccessUnchecked" + ICON_SET + ".png")));
        UNCHECKED_ICON.setFitHeight(15);
        UNCHECKED_ICON.setFitWidth(15);

        LOGGER_ICON = new ImageView(new Image(
                OptionsMenuBar.class.getResourceAsStream("resources/DataAccessConnectionLogging" + ICON_SET + ".png")));
        LOGGER_ICON.setFitHeight(15);
        LOGGER_ICON.setFitWidth(15);
    }

    private final DataAccessPane dataAccessPane;

    private MenuBar menuBar;

    private Menu optionsMenu;

    private MenuItem loadMenuItem;
    private MenuItem saveMenuItem;

    private CheckMenuItem saveResultsItem;
    private CheckMenuItem deselectPluginsOnExecutionMenuItem;
    private CheckMenuItem connectionLoggingMenuItem;

    /**
     * Creates a new option menu bar.
     *
     * @param dataAccessPane the data access pane that the menu bar will be added to
     */
    public OptionsMenuBar(final DataAccessPane dataAccessPane) {
        this.dataAccessPane = dataAccessPane;
    }

    /**
     * Initializes the options menu. Until this method is called, all menu UI components will be null.
     */
    public void init() {

        ////////////////////
        // Load Menu
        ////////////////////
        loadMenuItem = new MenuItem(LOAD_MENU_ITEM_TEXT, LOAD_TEMPLATE_ICON);
        loadMenuItem.setOnAction(event -> {
            DataAccessParametersIoProvider.loadParameters(dataAccessPane);

            event.consume();
        });

        ////////////////////
        // Save Menu
        ////////////////////
        saveMenuItem = new MenuItem(SAVE_MENU_ITEM_TEXT, SAVE_TEMPLATE_ICON);
        saveMenuItem.setOnAction(event -> {
            DataAccessParametersIoProvider.saveParameters(
                    dataAccessPane.getDataAccessTabPane().getTabPane()
            );

            event.consume();
        });

        ////////////////////
        // Save Results Menu
        ////////////////////
        saveResultsItem = new CheckMenuItem(SAVE_RESULTS_MENU_ITEM_TEXT, SAVE_RESULTS_ICON);
        saveResultsItem.setSelected(DataAccessPreferenceUtilities.getDataAccessResultsDir() != null);
        saveResultsItem.selectedProperty().addListener(new SaveResultsListener());

        ////////////////////////////////////////
        // De-Select Plugins On Execution Menu
        ////////////////////////////////////////
        deselectPluginsOnExecutionMenuItem = new CheckMenuItem(
                DESELECT_PLUGINS_ON_EXECUTION_MENU_ITEM_TEXT,
                UNCHECKED_ICON
        );
        deselectPluginsOnExecutionMenuItem.setSelected(
                DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled()
        );
        deselectPluginsOnExecutionMenuItem.setOnAction(event -> {
            DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(
                    deselectPluginsOnExecutionMenuItem.isSelected()
            );

            event.consume();
        });

        ////////////////////////////////////////
        // Connection Logging Menu
        ////////////////////////////////////////
        connectionLoggingMenuItem = new CheckMenuItem(
                CONNECTION_LOGGING_TEXT,
                LOGGER_ICON
        );
        connectionLoggingMenuItem.setSelected(
                LogPreferences.isConnectionLoggingEnabled()
        );
        connectionLoggingMenuItem.setOnAction(event -> {
            LogPreferences.setConnectionLogging(
                    connectionLoggingMenuItem.isSelected()
            );

            event.consume();
        });

        ////////////////////
        // Menu Setup
        ////////////////////
        optionsMenu = new Menu(OPTIONS_MENU_TEXT, SETTINGS_ICON);
        optionsMenu.getItems().addAll(loadMenuItem, saveMenuItem, saveResultsItem,
                connectionLoggingMenuItem, deselectPluginsOnExecutionMenuItem);
        optionsMenu.setId("options-menu");
        optionsMenu.addEventHandler(Menu.ON_SHOWING, event -> updateMenuEntry());
        menuBar = new MenuBar();
        menuBar.getMenus().add(optionsMenu);
        menuBar.setMinHeight(36);
        menuBar.setPadding(new Insets(4));
    }

    /*
    * Show the amount of time remaining in which Connection Logging will be active.
    * After a fixed timeout period, Connection Logging will be automatically disabled.
     */
    private void updateMenuEntry() {
        connectionLoggingMenuItem.setSelected(LogPreferences.isConnectionLoggingEnabled());
        final long remainingTime = LogPreferences.logTimeRemaining();
        final long remainingMinutes = remainingTime / 60000;
        final long remainingSeconds = remainingTime / 1000;
        final String remainingText = remainingMinutes > 0 ? "  \u23F3 " + remainingMinutes + "m" : "  \u23F3 " + remainingSeconds + "s";
        final String remainingMessage = LogPreferences.isConnectionLoggingEnabled() ? remainingText : "";
        connectionLoggingMenuItem.setText(CONNECTION_LOGGING_TEXT + remainingMessage);
    }

    /**
     * Get the data access pane that this options menu will be attached to.
     *
     * @return the data access pane
     */
    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }

    /**
     * Get the options menu bar for the data access panel.
     *
     * @return the menu bar
     */
    public MenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Get the workflow options menu.
     *
     * @return the workflow options menu
     */
    public Menu getOptionsMenu() {
        return optionsMenu;
    }

    /**
     * Get the load menu item that loads previously save data access view tabs into the UI from a JSON file.
     *
     * @return the load menu item
     */
    public MenuItem getLoadMenuItem() {
        return loadMenuItem;
    }

    /**
     * Get the menu item that save the current tab structure to a JSON file so that it can be loaded back in at a later stage.
     *
     * @return the save menu item
     */
    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    /**
     * A checkbox menu item representing if the results of running a tab's plugins should be saved.
     *
     * @return the save results menu item
     */
    public CheckMenuItem getSaveResultsItem() {
        return saveResultsItem;
    }

    /**
     * Gets a checkbox menu item that when selected will cause all selected plugins in the data access view to be de-selected once the execute button has been clicked and the plugins completed.
     *
     * @return the de-select plugins on execution menu item
     */
    public CheckMenuItem getDeselectPluginsOnExecutionMenuItem() {
        return deselectPluginsOnExecutionMenuItem;
    }

    /**
     * Gets the checkbox menu item to control whether connection logging should occur.
     *
     * @return the connection logging menu item
     */
    public CheckMenuItem getConnectionLoggingMenuItem() {
        return connectionLoggingMenuItem;
    }

    /**
     * This is a listener that is attached to the save results menu item and listens for changes in the selection.
     */
    protected class SaveResultsListener implements ChangeListener<Boolean> {

        private CompletableFuture<Void> lastChange = CompletableFuture.completedFuture(null);

        /**
         * If the save result menu checkbox becomes selected then present the user with a directory chooser to select the save directory. If the user cancels then de-select the save result menu checkbox.
         * <p/>
         * If the save result menu checkbox becomes de-selected then clear the users save directory preferences.
         *
         * @param observable the {@code ObservableValue} which value changed
         * @param oldValue the old value of the save results menu checkbox
         * @param newValue the new value of the save results menu checkbox
         */
        @Override
        public void changed(final ObservableValue<? extends Boolean> observable,
                final Boolean oldValue,
                final Boolean newValue) {
            if (newValue) {
                lastChange = FileChooser.openOpenDialog(getDataAccessResultsFileChooser()).thenAccept(optionalFolder
                        -> optionalFolder.ifPresentOrElse(
                                folder -> DataAccessPreferenceUtilities.setDataAccessResultsDir(folder),
                                () -> Platform.runLater(() -> getSaveResultsItem().setSelected(false))));
            } else {
                lastChange = CompletableFuture.completedFuture(null);
                DataAccessPreferenceUtilities.setDataAccessResultsDir(null);
            }
        }

        /**
         * This is primarily present for testing purposes to ensure all work in the changed method is complete before verifying functionality.
         *
         * @return the future work to be completed by the lister after its last call
         */
        public CompletableFuture getLastChange() {
            return lastChange;
        }

        /**
         * Creates the data access results directory chooser.
         *
         * @return the data access results directory file chooser
         */
        public FileChooserBuilder getDataAccessResultsFileChooser() {
            return new FileChooserBuilder(TITLE)
                    .setTitle(TITLE)
                    .setDirectoriesOnly(true)
                    .setAcceptAllFileFilterUsed(false);
        }
    }
}
