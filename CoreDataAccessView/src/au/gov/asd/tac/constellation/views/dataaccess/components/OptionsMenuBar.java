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

import au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent;
import au.gov.asd.tac.constellation.views.dataaccess.io.DataAccessPreferencesIoProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author formalhaunt
 */
public final class OptionsMenuBar {
    private final ImageView SETTINGS_ICON = new ImageView(new Image(
            DataAccessViewTopComponent.class.getResourceAsStream("resources/DataAccessSettings.png")));
    private final ImageView SAVE_TEMPLATE_ICON = new ImageView(new Image(
            DataAccessViewTopComponent.class.getResourceAsStream("resources/DataAccessSaveTemplate.png")));
    private final ImageView LOAD_TEMPLATE_ICON = new ImageView(new Image(
            DataAccessViewTopComponent.class.getResourceAsStream("resources/DataAccessLoadTemplate.png")));
    private final ImageView SAVE_RESULTS_ICON = new ImageView(new Image(
            DataAccessViewTopComponent.class.getResourceAsStream("resources/DataAccessSaveResults.png")));
    private final ImageView UNCHECKED_ICON = new ImageView(new Image(
            DataAccessViewTopComponent.class.getResourceAsStream("resources/DataAccessUnchecked.png")));
    
    private static final String LOAD_MENU_ITEM_TEXT = "Load Templates";
    private static final String SAVE_MENU_ITEM_TEXT = "Save Templates";
    private static final String SAVE_RESULTS_MENU_ITEM_TEXT = "Save Results";
    private static final String DESELECT_PLUGINS_ON_EXECUTION_MENU_ITEM_TEXT = "Deselect On Go";
    
    private static final String OPTIONS_MENU_TEXT = "Workflow Options";
    
    private final DataAccessPane dataAccessPane;
    
    private MenuBar menuBar;
    
    private Menu optionsMenu;
    
    private MenuItem loadMenuItem;
    private MenuItem saveMenuItem;
    
    private CheckMenuItem saveResultsItem;
    private CheckMenuItem deselectPluginsOnExecution;
    
    /**
     * 
     * @param dataAccessPane 
     */
    public OptionsMenuBar(final DataAccessPane dataAccessPane) {
        this.dataAccessPane = dataAccessPane;
    }
    
    /**
     * 
     */
    public void init() {
        
        ////////////////////
        // Load Menu
        ////////////////////
        
        LOAD_TEMPLATE_ICON.setFitHeight(15);
        LOAD_TEMPLATE_ICON.setFitWidth(15);
        
        loadMenuItem = new MenuItem(LOAD_MENU_ITEM_TEXT, LOAD_TEMPLATE_ICON);
        loadMenuItem.setOnAction(event -> {
            DataAccessPreferencesIoProvider.loadParameters(dataAccessPane);
        });

        ////////////////////
        // Save Menu
        ////////////////////
        
        SAVE_TEMPLATE_ICON.setFitHeight(15);
        SAVE_TEMPLATE_ICON.setFitWidth(15);
        
        saveMenuItem = new MenuItem(SAVE_MENU_ITEM_TEXT, SAVE_TEMPLATE_ICON);
        saveMenuItem.setOnAction(event -> {
            DataAccessPreferencesIoProvider.saveParameters(
                    dataAccessPane.getDataAccessTabPane().getTabPane()
            );
        });

        ////////////////////
        // Save Results Menu
        ////////////////////
        
        SAVE_RESULTS_ICON.setFitHeight(15);
        SAVE_RESULTS_ICON.setFitWidth(15);
        
        saveResultsItem = new CheckMenuItem(SAVE_RESULTS_MENU_ITEM_TEXT, SAVE_RESULTS_ICON);
        saveResultsItem.setSelected(DataAccessPreferenceUtilities.getDataAccessResultsDir() != null);
        saveResultsItem.selectedProperty().addListener((ov, oldValue, newValue) -> {
            if (newValue) {
                final DataAccessResultsDirChooser dirChooser = new DataAccessResultsDirChooser();
                
                if(dirChooser.openAndSaveToPreferences() == null) {
                    saveResultsItem.setSelected(false);
                }
            } else {
                DataAccessPreferenceUtilities.setDataAccessResultsDir(null);
            }
        });

        ////////////////////////////////////////
        // De-Select Plugins On Execution Menu
        ////////////////////////////////////////
        
        UNCHECKED_ICON.setFitHeight(15);
        UNCHECKED_ICON.setFitWidth(15);
        
        deselectPluginsOnExecution = new CheckMenuItem(DESELECT_PLUGINS_ON_EXECUTION_MENU_ITEM_TEXT, UNCHECKED_ICON);
        deselectPluginsOnExecution.setSelected(DataAccessPreferenceUtilities.isDeselectPluginsOnExecuteEnabled()
        );
        deselectPluginsOnExecution.setOnAction(event -> {
            DataAccessPreferenceUtilities.setDeselectPluginsOnExecute(
                    deselectPluginsOnExecution.isSelected()
            );
        });
        
        ////////////////////
        // Menu Setup
        ////////////////////
        
        SETTINGS_ICON.setFitHeight(20);
        SETTINGS_ICON.setFitWidth(20);
        
        optionsMenu = new Menu(OPTIONS_MENU_TEXT, SETTINGS_ICON);
        optionsMenu.getItems().addAll(loadMenuItem, saveMenuItem, saveResultsItem,
                deselectPluginsOnExecution);
        
        menuBar = new MenuBar();
        menuBar.getMenus().add(optionsMenu);
        menuBar.setMinHeight(36);
        menuBar.setPadding(new Insets(4));
    }

    public DataAccessPane getDataAccessPane() {
        return dataAccessPane;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public Menu getOptionsMenu() {
        return optionsMenu;
    }

    public MenuItem getLoadMenuItem() {
        return loadMenuItem;
    }

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public CheckMenuItem getSaveResultsItem() {
        return saveResultsItem;
    }

    public CheckMenuItem getDeselectPluginsOnExecution() {
        return deselectPluginsOnExecution;
    }
}
