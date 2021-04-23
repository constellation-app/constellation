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
package au.gov.asd.tac.constellation.plugins.importexport.delimited;

import au.gov.asd.tac.constellation.plugins.importexport.EasyGridPane;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPreferenceKeys;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.io.ImportDelimitedIO;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.List;
import java.util.prefs.Preferences;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

/**
 * This pane holds all parts for importing a delimited file. An import
 * controller handles the importing of files The source and configuration panes
 * handle the input and output of the selected data into a graph.
 *
 * @author aldebaran30701
 */
public class DelimitedImportPane extends BorderPane {

    private final ImportController importController = new ImportController(this);
    private final DelimitedImportTopComponent delimitedImportTopComponent;
    private final ConfigurationPane configurationPane;
    private final SourcePane sourcePane;
    private final EasyGridPane gridPane;
    private final BorderPane root;

    private static final String HELP_CTX = DelimitedImportPane.class.getName();
    private static final Image HELP_IMAGE = UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.BLUEBERRY.getJavaColor());
    private final Preferences importExportPrefs = NbPreferences.forModule(ImportExportPreferenceKeys.class);

    public DelimitedImportPane(final DelimitedImportTopComponent delimitedImportTopComponent) {
        this.delimitedImportTopComponent = delimitedImportTopComponent;
        root = new BorderPane();

        // titled source pane
        sourcePane = new SourcePane(importController);
        final TitledPane titledSourcePane = new TitledPane("Source", sourcePane);
        titledSourcePane.setCollapsible(true);

        // Options menu.
        final Menu optionsMenu = new Menu("Options");
        final MenuItem loadMenuItem = new MenuItem("Load...");
        loadMenuItem.setOnAction(event -> {
            if (importController.hasFiles()) {
                ImportDelimitedIO.loadParameters(this.getParentWindow(), importController);
            } else {
                NotifyDisplayer.display("Select a file first.", NotifyDescriptor.WARNING_MESSAGE);
            }
        });

        // save menu item
        final MenuItem saveMenuItem = new MenuItem("Save...");
        saveMenuItem.setOnAction(event -> {
            ImportDelimitedIO.saveParameters(this.getParentWindow(), importController);
        });

        // the menu item gets called when the checkbox value changes so work around it by using a flag
        final boolean[] userClickedTheCheckboxFirst = new boolean[1];
        userClickedTheCheckboxFirst[0] = false;

        // show schema attributes checkbox
        final CheckBox showSchemaAttributesCheckBox = new CheckBox();
        final boolean showSchemaDefault = importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
        showSchemaAttributesCheckBox.setSelected(showSchemaDefault);
        importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, showSchemaDefault);
        importController.setShowAllSchemaAttributes(showSchemaDefault);
        importController.setClearManuallyAdded(false);
        importController.setDestination(sourcePane.getDestination());

        showSchemaAttributesCheckBox.setOnAction(event -> {
            // the checkbox has its own listener
            boolean newPreference = !importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
            importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, newPreference);
            importController.setShowAllSchemaAttributes(newPreference);
            importController.setClearManuallyAdded(false);
            importController.setDestination(sourcePane.getDestination());
            userClickedTheCheckboxFirst[0] = true;
            showSchemaAttributesCheckBox.setSelected(newPreference);
        });

        // show schema attributes menu item
        final MenuItem showSchemaAttributesItem = new MenuItem("Show all schema attributes", showSchemaAttributesCheckBox);
        showSchemaAttributesItem.setOnAction(event -> {
            // ignore if the checkbox was clicked
            if (!userClickedTheCheckboxFirst[0]) {
                final boolean newPreference = !importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
                importExportPrefs.putBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, newPreference);
                importController.setShowAllSchemaAttributes(newPreference);
                importController.setClearManuallyAdded(false);
                importController.setDestination(sourcePane.getDestination());
                final int saveResultsItemIndex = optionsMenu.getItems().indexOf(showSchemaAttributesItem);
                ((CheckBox) optionsMenu.getItems().get(saveResultsItemIndex).getGraphic()).setSelected(newPreference);
            }
            userClickedTheCheckboxFirst[0] = false;
        });
        optionsMenu.getItems().addAll(loadMenuItem, saveMenuItem, showSchemaAttributesItem);

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
        final Button helpButton = new Button("", new ImageView(HELP_IMAGE));
        AnchorPane.setTopAnchor(helpButton, 0.0);
        AnchorPane.setRightAnchor(helpButton, 0.0);
        helpButton.setOnAction(event -> {
            new HelpCtx(HELP_CTX).display();
        });

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        menuToolbar.getChildren().add(helpButton);

        // titled configuration pane
        configurationPane = new ConfigurationPane(importController);
        final TitledPane titledConfigurationPane = new TitledPane("Configuration", configurationPane);
        titledConfigurationPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        titledConfigurationPane.setMinSize(0, 0);
        titledConfigurationPane.setCollapsible(false);

        // gridpane for configuration pane
        gridPane = new EasyGridPane();
        gridPane.addColumn(0, titledConfigurationPane);
        gridPane.setPadding(new Insets(0, 0, 10, 0));
        gridPane.addColumnConstraint(true, HPos.LEFT, Priority.ALWAYS, Double.MAX_VALUE, 200, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.BOTTOM, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);

        // actionpane for holding import and cancel buttons
        final ActionPane actionPane = new ActionPane(importController);
        actionPane.setMinSize(0, 40);
        actionPane.prefWidthProperty().bind(this.widthProperty());
        actionPane.setPadding(new Insets(0, 0, 20, 0));

        importController.setConfigurationPane(configurationPane);

        // Setting help keyevent to f1
        setOnKeyPressed(event -> {
            final KeyCode c = event.getCode();
            if (c == KeyCode.F1) {
                new HelpCtx(HELP_CTX).display();
            }
        });

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
        delimitedImportTopComponent.close();
    }

    public SourcePane getSourcePane() {
        return sourcePane;
    }

    public Window getParentWindow() {
        return this.getScene().getWindow();
    }
}
