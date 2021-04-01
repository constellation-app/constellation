/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.plugins.importexport.EasyGridPane;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
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
import org.openide.util.HelpCtx;

public class JDBCImportPane extends BorderPane {

    private static final String HELP_CTX = JDBCImportPane.class.getName();
    private static final Image HELP_IMAGE = UserInterfaceIconProvider.HELP.buildImage(16, ConstellationColor.AZURE.getJavaColor());

    private final ImportController importController = new ImportController(this);
    private final JDBCImportTopComponent jdbcImportTopComponent;
    private final ConfigurationPane configurationPane;
    private final SourcePane sourcePane;
    private final EasyGridPane gridPane;
    private final BorderPane root;

    public JDBCImportPane(final JDBCImportTopComponent jdbcImportTopComponent) {
        this.jdbcImportTopComponent = jdbcImportTopComponent;
        root = new BorderPane();

        final Menu helpMenu = new Menu(""); // blank menu
        final AnchorPane menuToolbar = new AnchorPane();
        final MenuBar menuBar = new MenuBar();
        AnchorPane.setTopAnchor(menuBar, 0.0);
        AnchorPane.setLeftAnchor(menuBar, 0.0);
        menuBar.getMenus().add(helpMenu);

        // hide the menu bar background now that its anchored to the left
        menuBar.setStyle("-fx-border-color: transparent;-fx-background-color: transparent;");
        menuToolbar.getChildren().add(menuBar);

        sourcePane = new SourcePane(importController);
        final TitledPane titledSourcePane = new TitledPane("Source", sourcePane);
        titledSourcePane.setCollapsible(true);

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

        importController.setConfigurationPane(configurationPane);

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

    public SourcePane getSourcePane() {
        return sourcePane;
    }

    public void updateSourcePane() {
        sourcePane.update(importController);
    }

    public void close() {
        jdbcImportTopComponent.close();
    }

    public Window getParentWindow() {
        return this.getScene().getWindow();
    }
}
