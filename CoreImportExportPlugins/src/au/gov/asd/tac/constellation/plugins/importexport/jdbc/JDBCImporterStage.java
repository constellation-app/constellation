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

import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.HelpCtx;

public class JDBCImporterStage extends Stage {

    private static final String HELP_CTX = JDBCImporterStage.class.getName();

    private final ImportController importController = new ImportController(this);
    private final SourcePane sourcePane;
    private final ConfigurationPane configurationPane;

    @StaticResource
    private static final String JDBC_IMPORTER_ICON_PATH = "au/gov/asd/tac/constellation/plugins/importexport/jdbc/resources/jdbc_import.png";

    public JDBCImporterStage() {
        final BorderPane root = new BorderPane();
        final double hPadding = 100;
        final EasyGridPane gridPane = new EasyGridPane();
        gridPane.addColumnConstraint(true, HPos.LEFT, Priority.ALWAYS, Double.MAX_VALUE, 200, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
        gridPane.addRowConstraint(true, VPos.BOTTOM, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);

        sourcePane = new SourcePane(importController, this);
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

        gridPane.addColumn(0, titledSourcePane, titledConfigurationPane, actionPane);

        final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        final double screenHeight = visualBounds.getHeight();

        final ScrollPane sp = new ScrollPane(gridPane);
        sp.setFitToWidth(true);

        sp.setPrefHeight(screenHeight - hPadding);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setCenter(sp);
        root.setCenter(new VBox(sp));

        final Scene scene = new Scene(root);
        scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
        scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getOutputFontSize()));
        scene.setFill(Color.WHITESMOKE);
        scene.setOnKeyPressed(event -> {
            final KeyCode c = event.getCode();
            if (c == KeyCode.F1) {
                new HelpCtx(HELP_CTX).display();
            }
        });

        setScene(scene);
        setTitle("Import from JDBC Source");
        getIcons().add(new Image(JDBC_IMPORTER_ICON_PATH));
        JDBCImporterStage.this.centerOnScreen();
    }

    public void update(final ImportController importController, final List<ImportDefinition> definitions) {
        sourcePane.update(importController);
        configurationPane.update(definitions);
    }

    public SourcePane getSourcePane() {
        return sourcePane;
    }
}
