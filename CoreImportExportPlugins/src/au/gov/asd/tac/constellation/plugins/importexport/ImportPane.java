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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.openide.util.NbPreferences;

/**
 * This pane holds all parts for importing a delimited file. An import
 * controller handles the importing of files The source and configuration panes
 * handle the input and output of the selected data into a graph.
 *
 * @author aldebaran30701
 */
public class ImportPane extends BorderPane {

    private final static Insets GRIDPANE_PADDING = new Insets(0, 0, 10, 0);
    private final static Insets ACTIONPANE_PADDING = new Insets(0, 0, 20, 0);
    private final static int GRIDPANE_CONSTRAINT = 200;
    private final static int ACTIONPANE_MIN_HEIGHT = 40;
    private final static Image HELP_IMAGE = UserInterfaceIconProvider.HELP.buildImage(16,
            ConstellationColor.BLUEBERRY.getJavaColor());

    protected final Preferences importExportPrefs = NbPreferences.forModule(ImportExportPreferenceKeys.class);
    protected final Menu optionsMenu;
    protected final MenuItem loadMenuItem;
    protected final MenuItem saveMenuItem;
    protected final CheckBox showSchemaAttributesCheckBox;
    protected final MenuItem showSchemaAttributesItem;
    protected final Button helpButton;

    protected ImportController importController;
    protected ImportTopComponent importTopComponent;
    protected ConfigurationPane configurationPane;
    protected SourcePane sourcePane;
    protected EasyGridPane gridPane;
    protected BorderPane root;

    public ImportPane(final ImportTopComponent importTopComponent, final ImportController controller,
            final ConfigurationPane configurationPane, final SourcePane sourcePane) {
        this.importTopComponent = importTopComponent;
        this.importController = controller;
        this.configurationPane = configurationPane;
        this.sourcePane = sourcePane;
        root = new BorderPane();

        // titled source pane
        final TitledPane titledSourcePane = new TitledPane("Source and Destination", sourcePane);
        titledSourcePane.setCollapsible(true);

        // Options menu
        optionsMenu = new Menu("Options");
        loadMenuItem = new MenuItem("Load...");

        // save menu item
        saveMenuItem = new MenuItem("Save...");

        // the menu item gets called when the checkbox value changes so work around it by using a flag
        final boolean[] userClickedTheCheckboxFirst = new boolean[1];
        userClickedTheCheckboxFirst[0] = false;

        // show schema attributes checkbox
        showSchemaAttributesCheckBox = new CheckBox();
        final boolean showSchemaDefault = importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES,
                ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
        showSchemaAttributesCheckBox.setSelected(showSchemaDefault);
        importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, showSchemaDefault);
        importController.setShowAllSchemaAttributes(showSchemaDefault);
        importController.setClearManuallyAdded(false);
        importController.setDestination(sourcePane.getDestination());

        showSchemaAttributesCheckBox.setOnAction(event -> {
            // the checkbox has its own listener
            final boolean newPreference = !importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES,
                    ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
            importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, newPreference);
            importController.setShowAllSchemaAttributes(newPreference);
            importController.setClearManuallyAdded(false);
            importController.setDestination(sourcePane.getDestination());
            userClickedTheCheckboxFirst[0] = true;
            showSchemaAttributesCheckBox.setSelected(newPreference);
        });

        // show schema attributes menu item
        showSchemaAttributesItem = new MenuItem("Show all schema attributes", showSchemaAttributesCheckBox);
        showSchemaAttributesItem.setOnAction((ActionEvent event) -> {
            // ignore if the checkbox was clicked
            if (!userClickedTheCheckboxFirst[0]) {
                final boolean newPreference = !importExportPrefs.getBoolean(
                        ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES,
                        ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
                importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, newPreference);
                importController.setShowAllSchemaAttributes(newPreference);
                importController.setClearManuallyAdded(false);
                importController.setDestination(sourcePane.getDestination());
                final int saveResultsItemIndex = optionsMenu.getItems().indexOf(showSchemaAttributesItem);
                ((CheckBox) optionsMenu.getItems().get(saveResultsItemIndex).getGraphic()).setSelected(newPreference);
            }
            userClickedTheCheckboxFirst[0] = false;
        });

        // setting up menu bar
        final AnchorPane menuToolbar = new AnchorPane();
        final MenuBar menuBar = new MenuBar();
        AnchorPane.setTopAnchor(menuBar, 0.0);
        AnchorPane.setLeftAnchor(menuBar, 0.0);
        menuBar.getMenus().add(optionsMenu);

        // hide the menu bar background now that its anchored to the left
        menuBar.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        menuToolbar.getChildren().add(menuBar);

        // setting up help button
        helpButton = new Button("", new ImageView(HELP_IMAGE));
        AnchorPane.setTopAnchor(helpButton, 0.0);
        AnchorPane.setRightAnchor(helpButton, 0.0);

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        menuToolbar.getChildren().add(helpButton);

        // titled configuration pane
        final TitledPane titledConfigurationPane = new TitledPane("Configuration", configurationPane);
        titledConfigurationPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        titledConfigurationPane.setMinSize(0, 0);
        titledConfigurationPane.setCollapsible(false);

        // gridpane for configuration pane
        gridPane = new EasyGridPane();
        gridPane.addColumn(0, titledConfigurationPane);
        gridPane.setPadding(GRIDPANE_PADDING);
        gridPane.addColumnConstraint(true, HPos.LEFT, Priority.ALWAYS, Double.MAX_VALUE, GRIDPANE_CONSTRAINT, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.BOTTOM, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);

        // actionpane for holding import and cancel buttons
        final ActionPane actionPane = new ActionPane(importController);
        actionPane.setMinSize(0, ACTIONPANE_MIN_HEIGHT);
        actionPane.prefWidthProperty().bind(this.widthProperty());
        actionPane.setPadding(ACTIONPANE_PADDING);

        importController.setConfigurationPane(configurationPane);

        root.setCenter(new VBox(menuToolbar, titledSourcePane, gridPane, actionPane));
        setCenter(root);
    }

    public void update(final ImportController importController, final List<ImportDefinition> definitions) {
        sourcePane.update(importController);
        configurationPane.update(definitions);
    }

    public void updateSourcePane() {
        sourcePane.update(importController);
    }

    public void close() {
        importTopComponent.close();
    }

    public SourcePane getSourcePane() {
        return sourcePane;
    }

    public Window getParentWindow() {
        return this.getScene().getWindow();
    }
}
