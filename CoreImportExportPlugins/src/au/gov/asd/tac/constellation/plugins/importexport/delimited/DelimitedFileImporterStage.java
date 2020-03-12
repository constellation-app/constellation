/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPreferenceKeys;
import au.gov.asd.tac.constellation.plugins.importexport.delimited.io.ImportDelimitedIO;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbPreferences;

/**
 * The DelimitedFileImportState provides a window that will hold the entire
 * delimited file import UI.
 *
 * @author sirius
 */
public class DelimitedFileImporterStage extends Stage {

    private static final String HELP_CTX = DelimitedFileImporterStage.class.getName();

    private final ImportController importController = new ImportController(this);
    private final SourcePane sourcePane;
    private final ConfigurationPane configurationPane;

    private final Preferences importExportPrefs = NbPreferences.forModule(ImportExportPreferenceKeys.class);

    @StaticResource
    private static final String DELIMITED_IMPORTER_ICON_PATH = "au/gov/asd/tac/constellation/plugins/importexport/delimited/resources/import-delimited.png";
    private static final Image HELP_IMAGE = UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.AZURE.getJavaColor());

    public DelimitedFileImporterStage() {
        final BorderPane root = new BorderPane();
        final double hPadding = 100;
        final EasyGridPane gridPane = new EasyGridPane();
        gridPane.addColumnConstraint(true, HPos.LEFT, Priority.ALWAYS, Double.MAX_VALUE, 200, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.BOTTOM, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);

        sourcePane = new SourcePane(importController);
        final TitledPane titledSourcePane = new TitledPane("Source", sourcePane);
        titledSourcePane.setCollapsible(true);

        configurationPane = new ConfigurationPane(importController);
        final TitledPane titledConfigurationPane = new TitledPane("Configuration", configurationPane);
        titledConfigurationPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        titledConfigurationPane.setMinSize(0, 0);
        titledConfigurationPane.setCollapsible(false);

        final ActionPane actionPane = new ActionPane(importController);
        actionPane.setMinSize(0, 10);

        importController.setConfigurationPane(configurationPane);

        // Options menu.
        final Menu optionsMenu = new Menu("Options");
        final MenuItem loadMenuItem = new MenuItem("Load...");
        loadMenuItem.setOnAction(event -> {
            if (importController.hasFiles()) {
                ImportDelimitedIO.loadParameters(this, importController);
            } else {
                final NotifyDescriptor nd = new NotifyDescriptor.Message("Select a file first.", NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        });

        final MenuItem saveMenuItem = new MenuItem("Save...");
        saveMenuItem.setOnAction(event -> {
            ImportDelimitedIO.saveParameters(this, importController);
        });

        // the menu item gets called when the checkbox value changes so work around it by using a flag
        final boolean[] userClickedTheCheckboxFirst = new boolean[1];
        userClickedTheCheckboxFirst[0] = false;

        final CheckBox showSchemaAttributesCheckBox = new CheckBox();
        showSchemaAttributesCheckBox.setSelected(importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES));
        showSchemaAttributesCheckBox.setOnAction(event -> {
            // the checkbox has its own listener
            boolean newPreference = !importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
            importController.setShowAllSchemaAttributes(newPreference);
            importController.setClearManuallyAdded(false);
            importController.setDestination(sourcePane.getDestination());
            userClickedTheCheckboxFirst[0] = true;
        });

        final MenuItem showSchemaAttributesItem = new MenuItem("Show all schema attributes", showSchemaAttributesCheckBox);
        showSchemaAttributesItem.setOnAction(event -> {
            // ignore if the checkbox was clicked
            if (!userClickedTheCheckboxFirst[0]) {
                final boolean newPreference = !importExportPrefs.getBoolean(ImportExportPreferenceKeys.SHOW_SCHEMA_ATTRIBUTES, ImportExportPreferenceKeys.DEFAULT_SHOW_SCHEMA_ATTRIBUTES);
                importController.setShowAllSchemaAttributes(newPreference);
                importController.setClearManuallyAdded(false);
                importController.setDestination(sourcePane.getDestination());
                final int saveResultsItemIndex = optionsMenu.getItems().indexOf(showSchemaAttributesItem);
                ((CheckBox) optionsMenu.getItems().get(saveResultsItemIndex).getGraphic()).setSelected(newPreference);
            }
            userClickedTheCheckboxFirst[0] = false;
        });
        optionsMenu.getItems().addAll(loadMenuItem, saveMenuItem, showSchemaAttributesItem);

        final AnchorPane menuToolbar = new AnchorPane();
        final MenuBar menuBar = new MenuBar();
        AnchorPane.setTopAnchor(menuBar, 0.0);
        AnchorPane.setLeftAnchor(menuBar, 0.0);
        menuBar.getMenus().add(optionsMenu);

        // hide the menu bar background now that its anchored to the left
        menuBar.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        menuToolbar.getChildren().add(menuBar);

        final Button helpButton = new Button("", new ImageView(HELP_IMAGE));
        AnchorPane.setTopAnchor(helpButton, 0.0);
        AnchorPane.setRightAnchor(helpButton, 0.0);
        helpButton.setOnAction(event -> {
            new HelpCtx(HELP_CTX).display();
        });

        // Get rid of the ugly button look so the icon stands alone.
        helpButton.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        menuToolbar.getChildren().add(helpButton);

        gridPane.addColumn(0, titledSourcePane, titledConfigurationPane, actionPane);

        final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        final double screenHeight = visualBounds.getHeight();

        final ScrollPane sp = new ScrollPane(gridPane);
        sp.setFitToWidth(true);

        sp.setPrefHeight(screenHeight - hPadding);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        root.setCenter(sp);
        root.setCenter(new VBox(menuToolbar, sp));

//        Scene scene = new Scene(root, 700, 1050, Color.WHITESMOKE);
        final Scene scene = new Scene(root);
        scene.setFill(Color.WHITESMOKE);
        scene.setOnKeyPressed((event) -> {
            final KeyCode c = event.getCode();
            if (c == KeyCode.F1) {
                new HelpCtx(HELP_CTX).display();
            }
        });

        setScene(scene);
        setTitle("Import from Delimited File");
        getIcons().add(new Image(DELIMITED_IMPORTER_ICON_PATH));
        centerOnScreen();
    }

    public void update(final ImportController importController, final List<ImportDefinition> definitions) {
        sourcePane.update(importController);
        configurationPane.update(definitions);
    }

    public SourcePane getSourcePane() {
        return sourcePane;
    }
}
