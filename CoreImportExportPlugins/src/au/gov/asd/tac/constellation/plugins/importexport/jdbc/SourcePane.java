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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersPane;
import au.gov.asd.tac.constellation.plugins.importexport.EasyGridPane;
import au.gov.asd.tac.constellation.plugins.importexport.GraphDestination;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDestination;
import au.gov.asd.tac.constellation.plugins.importexport.SchemaDestination;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SourcePane extends GridPane {

    private final ComboBox<ImportDestination<?>> graphComboBox;
    private final ComboBox<JDBCConnection> dbConnectionComboBox;
    private final Pane parametersPane = new Pane();
    private final ImportController importController;

    public SourcePane(final ImportController importController, final Stage parentStage) {

        final JDBCDriverManager driverManager = JDBCDriverManager.getDriverManager();
        final JDBCConnectionManager connectionManager = JDBCConnectionManager.getConnectionManager();

        this.importController = importController;

        setMinHeight(USE_PREF_SIZE);
        setMinWidth(0);
        setPadding(new Insets(5));
        setHgap(10);
        setVgap(10);

        final ColumnConstraints column0Constraints = new ColumnConstraints();
        column0Constraints.setHgrow(Priority.NEVER);

        final ColumnConstraints column1Constraints = new ColumnConstraints();
        column1Constraints.setHgrow(Priority.ALWAYS);
        column1Constraints.setMinWidth(0);

        final ColumnConstraints column2Constraints = new ColumnConstraints();
        column2Constraints.setHgrow(Priority.NEVER);

        getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);

        final Label fileLabel = new Label("Connection:");
        GridPane.setConstraints(fileLabel, 0, 0, 1, 1, HPos.LEFT, VPos.TOP);

        final ObservableList<JDBCConnection> connections = FXCollections.observableArrayList();
        connections.clear();
        connections.addAll(connectionManager.getConnections());

        dbConnectionComboBox = new ComboBox<>();
        dbConnectionComboBox.setItems(connections);
        dbConnectionComboBox.setOnAction((final ActionEvent t) -> {
            importController.setDBConnection(dbConnectionComboBox.getSelectionModel().getSelectedItem());
        });
        GridPane.setConstraints(dbConnectionComboBox, 1, 0, 1, 1, HPos.LEFT, VPos.TOP);

        final Button manageConnectionsBtn = new Button("Manage connections");
        manageConnectionsBtn.setOnAction((final ActionEvent t) -> {
            final Stage dialog = new Stage();
            final ComboBox<JDBCDriver> driver = new ComboBox<>();

            final BorderPane root = new BorderPane();
            final EasyGridPane gridPane = new EasyGridPane();
            gridPane.addColumnConstraint(true, HPos.LEFT, Priority.ALWAYS, Double.MAX_VALUE, 200, GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.addRowConstraint(true, VPos.BOTTOM, Priority.ALWAYS, Double.MAX_VALUE, 0, GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.setPadding(new Insets(5));
            gridPane.setHgap(10);
            gridPane.setVgap(10);

            final TabPane tp = new TabPane();
            final Tab connectionsTab = new Tab("Connections");
            connectionsTab.setClosable(false);

            final EasyGridPane connectionsPane = new EasyGridPane();
            connectionsPane.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
            connectionsPane.setPadding(new Insets(5));
            connectionsPane.setHgap(10);
            connectionsPane.setVgap(10);
            final TableView connectionsTable = new TableView();
            connectionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            connectionsTable.getSelectionModel().setCellSelectionEnabled(false);
            connectionsTable.prefHeightProperty().set(200);
            connectionsTable.prefWidthProperty().bind(dialog.widthProperty());

            final TableColumn<String, JDBCConnection> connectionName = new TableColumn<>("Name");
            connectionName.setCellValueFactory(new PropertyValueFactory<>("connectionName"));

            final TableColumn<String, JDBCConnection> connectionString = new TableColumn<>("Connection String");
            connectionString.setCellValueFactory(new PropertyValueFactory<>("connectionString"));

            connectionsTable.getColumns().addAll(connectionName, connectionString);

            connectionsTable.getItems().addAll(connectionManager.getConnections());
            connectionsTable.getSortOrder().add(connectionName);

            connectionsPane.add(connectionsTable, 0, 0, 2, 1);

            final Button removeBtn = new Button("Remove");
            removeBtn.setOnAction((final ActionEvent t1) -> {
                final JDBCConnection d = (JDBCConnection) connectionsTable.getSelectionModel().getSelectedItem();
                if (d != null) {
                    connectionManager.deleteConnection(d.getConnectionName());
                    connectionsTable.getItems().clear();
                    connectionsTable.getItems().addAll(connectionManager.getConnections());
                    connections.clear();
                    connections.addAll(connectionManager.getConnections());
                }
            });
            connectionsPane.add(removeBtn, 0, 1, 1, 1);
            final Button addBtn = new Button("Add");
            addBtn.setOnAction((final ActionEvent t1) -> {
                final Stage d = new Stage();
                // add stuff here.
                final BorderPane r = new BorderPane();
                final EasyGridPane gp = new EasyGridPane();

                gp.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
                gp.setPadding(new Insets(5));
                gp.setHgap(10);
                gp.setVgap(10);
                final Label nameLabel = new Label("Connection Name");
                gp.add(nameLabel, 0, 0, 1, 1);
                final TextField cn = new TextField();
                gp.add(cn, 1, 0, 2, 1);

                final Label driverLabel = new Label("Driver");
                gp.add(driverLabel, 0, 1, 1, 1);

                driver.getItems().clear();
                driver.getItems().addAll(driverManager.getDrivers());
                gp.add(driver, 1, 1, 1, 1);

                final Label connectionStringLabel = new Label("Connection String");
                gp.add(connectionStringLabel, 0, 2, 1, 1);
                final TextField connectionStringF = new TextField();
                gp.add(connectionStringF, 1, 2, 2, 1);

                final Label usernameLabel = new Label("Username");
                gp.add(usernameLabel, 0, 3, 1, 1);
                final TextField username = new TextField();
                gp.add(username, 1, 3, 2, 1);

                final Label passwordLabel = new Label("Password");
                gp.add(passwordLabel, 0, 4, 1, 1);
                final PasswordField password = new PasswordField();
                gp.add(password, 1, 4, 2, 1);

                final Button add = new Button("Add");
                add.setOnAction((final ActionEvent t2) -> {
                    if (!cn.getText().isBlank()
                            && driver.getValue() != null
                            && !connectionStringF.getText().isBlank()
                            && !username.getText().isBlank()
                            && !password.getText().isBlank()) {
                        if (connectionManager.addConnection(cn.getText(), driver.getValue(), username.getText(), password.getText(), connectionStringF.getText())) {
                            connectionsTable.getItems().clear();
                            connectionsTable.getItems().addAll(connectionManager.getConnections());
                            connections.clear();
                            connections.addAll(connectionManager.getConnections());
                            d.close();
                        } else {
                            // check connection is valid.

                        }
                    }
                });

                gp.add(add, 0, 5, 1, 1);

                final Button test = new Button("Test");
                test.setOnAction((final ActionEvent t2) -> {
                    if (!cn.getText().isBlank()
                            && driver.getValue() != null
                            && !connectionStringF.getText().isBlank()
                            && !username.getText().isBlank()
                            && !password.getText().isBlank()) {

                        if (connectionManager.testConnection(cn.getText(), driver.getValue(), username.getText(), password.getText(), connectionStringF.getText())) {
                            final Alert a = new Alert(AlertType.INFORMATION, "Connection success", ButtonType.OK);
                            a.showAndWait();
                        }
                    }
                });

                gp.add(test, 1, 5, 1, 1);

                final ScrollPane sp = new ScrollPane(gp);
                sp.setFitToWidth(true);

                sp.setPrefHeight(200);
                sp.setPrefWidth(400);
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                r.setCenter(sp);
                final Scene scene = new Scene(r);
                scene.setFill(Color.WHITESMOKE);
                scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
                scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

                d.setScene(scene);
                d.setTitle("Add Connection");
                d.centerOnScreen();
                d.initOwner(dialog);
                d.initModality(Modality.APPLICATION_MODAL);
                d.showAndWait();

            });
            connectionsPane.add(addBtn, 1, 1, 1, 1);

            final Tab driversTab = new Tab("Drivers");
            driversTab.setClosable(false);

            final EasyGridPane dtRoot = new EasyGridPane();
            dtRoot.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
            dtRoot.setPadding(new Insets(5));
            dtRoot.setHgap(10);
            dtRoot.setVgap(10);
            final TableView driverTable = new TableView();
            driverTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            driverTable.getSelectionModel().setCellSelectionEnabled(false);
            driverTable.prefHeightProperty().bind(connectionsTable.heightProperty());
            driverTable.prefWidthProperty().bind(connectionsTable.widthProperty());

            final TableColumn<String, JDBCDriver> name = new TableColumn<>("Name");
            name.setCellValueFactory(new PropertyValueFactory<>("name"));
            final TableColumn<String, JDBCDriver> jar = new TableColumn<>("Filename");
            jar.setCellValueFactory(new PropertyValueFactory<>("filename"));

            driverTable.getColumns().addAll(name, jar);

            driverTable.getItems().addAll(driverManager.getDrivers());
            driverTable.getSortOrder().add(name);

            dtRoot.add(driverTable, 0, 0, 2, 1);

            final Button removeBtn1 = new Button("Remove");
            removeBtn1.setOnAction((final ActionEvent t1) -> {
                final JDBCDriver d = (JDBCDriver) driverTable.getSelectionModel().getSelectedItem();
                if (d != null) {
                    if (driverManager.isDriverUsed(d.getName())) {
                        final TextArea a = new TextArea();
                        a.setWrapText(true);
                        a.setEditable(false);
                        a.setText("Connections exist using this Driver.\nThe connections that use this driver will be deleted, do you want to proceed?");
                        final Alert b = new Alert(AlertType.CONFIRMATION, "", ButtonType.NO, ButtonType.YES);
                        b.getDialogPane().setContent(a);
                        final Optional<ButtonType> res = b.showAndWait();
                        if (res == null || !res.isPresent() || res.get() == ButtonType.NO) {
                            return;
                        }
                    }

                    driverManager.removeConnectionsWithDriver(d.getName());
                    driverManager.deleteDriver(d.getName());
                    driverTable.getItems().clear();
                    driverTable.getItems().addAll(driverManager.getDrivers());
                    driver.getItems().clear();
                    driver.getItems().addAll(driverManager.getDrivers());

                    connectionsTable.getItems().clear();
                    connectionsTable.getItems().addAll(connectionManager.getConnections());
                    connections.clear();
                    connections.addAll(connectionManager.getConnections());
                }
            });
            dtRoot.add(removeBtn1, 0, 1, 1, 1);
            final Button addBtn1 = new Button("Add");
            addBtn1.setOnAction((final ActionEvent t1) -> {
                final Stage d = new Stage();
                // add stuff here.
                final BorderPane r = new BorderPane();
                final EasyGridPane gp = new EasyGridPane();

                gp.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
                gp.setPadding(new Insets(5));
                gp.setHgap(10);
                gp.setVgap(10);
                final Label nameLabel = new Label("Name");
                gp.add(nameLabel, 0, 0, 1, 1);
                final ComboBox driverName = new ComboBox();
                gp.add(driverName, 1, 0, 2, 1);

                final Label jarLabel = new Label("Driver");
                gp.add(jarLabel, 0, 1, 1, 1);

                final TextField j = new TextField();
                gp.add(j, 1, 1, 1, 1);
                final Button chooser = new Button("..");
                chooser.setOnAction((final ActionEvent t2) -> {
                    final FileChooser cho = new FileChooser();
                    cho.setSelectedExtensionFilter(new ExtensionFilter("Driver jar", "*.jar"));
                    final File f = cho.showOpenDialog(new Stage());
                    if (f != null) {
                        try {
                            j.setText(f.getCanonicalPath());

                            driverName.getItems().clear();
                            driverName.getItems().addAll(JDBCDriver.getDrivers(f));

                        } catch (final IOException ex) {
                        }
                    }

                });
                gp.add(chooser, 2, 1, 1, 1);

                final Button add = new Button("Add");
                add.setOnAction((final ActionEvent t2) -> {
                    if (driverName.getSelectionModel().getSelectedItem() != null) {
                        driverManager.addDriver((String) driverName.getSelectionModel().getSelectedItem(), new File(j.getText()));
                        driverTable.getItems().clear();
                        driverTable.getItems().addAll(driverManager.getDrivers());
                        driver.getItems().clear();
                        driver.getItems().addAll(driverManager.getDrivers());
                        d.close();
                    }
                });

                gp.add(add, 0, 2, 1, 1);

                final ScrollPane sp = new ScrollPane(gp);
                sp.setFitToWidth(true);

                sp.setPrefHeight(200);
                sp.setPrefWidth(400);
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                r.setCenter(sp);
                final Scene scene = new Scene(r);
                scene.setFill(Color.WHITESMOKE);
                scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
                scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

                d.setScene(scene);
                d.setTitle("Add Driver");
                d.centerOnScreen();
                d.initOwner(dialog);
                d.initModality(Modality.APPLICATION_MODAL);
                d.showAndWait();

            });
            dtRoot.add(addBtn1, 1, 1, 1, 1);

            driversTab.setContent(dtRoot);

            connectionsTab.setContent(connectionsPane);
            tp.getTabs().addAll(connectionsTab, driversTab);

            gridPane.getChildren().addAll(tp);

            final ScrollPane sp = new ScrollPane(gridPane);
            sp.setFitToWidth(true);

            sp.setPrefHeight(400);
            sp.setPrefWidth(800);
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            root.setCenter(sp);
            final Scene scene = new Scene(root);
            scene.setFill(Color.WHITESMOKE);
            scene.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
            scene.rootProperty().get().setStyle(String.format("-fx-font-size:%d;", FontUtilities.getApplicationFontSize()));

            dialog.setScene(scene);
            dialog.setTitle("Manage Connections");
            dialog.centerOnScreen();
            dialog.initOwner(parentStage);
            dialog.sizeToScene();
            dialog.initModality(Modality.APPLICATION_MODAL);

            dialog.showAndWait();

        });
        GridPane.setConstraints(manageConnectionsBtn, 2, 0, 1, 1, HPos.LEFT, VPos.TOP);

        final Label usernameLabel = new Label("Username:");
        GridPane.setConstraints(usernameLabel, 0, 1, 1, 1, HPos.LEFT, VPos.TOP);

        final TextField username = new TextField();
        GridPane.setConstraints(username, 1, 1, 2, 1, HPos.LEFT, VPos.TOP);

        final Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel, 0, 2, 1, 1, HPos.LEFT, VPos.TOP);

        final PasswordField password = new PasswordField();
        GridPane.setConstraints(password, 1, 2, 2, 1, HPos.LEFT, VPos.TOP);

        final Label queryLabel = new Label("Query:");
        GridPane.setConstraints(queryLabel, 0, 3, 1, 1, HPos.LEFT, VPos.TOP);

        final TextArea query = new TextArea();
        query.setPrefRowCount(5);
        GridPane.setConstraints(query, 1, 3, 2, 1, HPos.LEFT, VPos.TOP);

        final Label destinationLabel = new Label("Destination:");
        GridPane.setConstraints(destinationLabel, 0, 4, 1, 1, HPos.LEFT, VPos.TOP);

        final ObservableList<ImportDestination<?>> destinations = FXCollections.observableArrayList();

        final Map<String, Graph> graphs = GraphManager.getDefault().getAllGraphs();
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        ImportDestination<?> defaultDestination = null;
        for (final Graph graph : graphs.values()) {
            final GraphDestination destination = new GraphDestination(graph);
            destinations.add(destination);
            if (graph == activeGraph) {
                defaultDestination = destination;
            }
        }

        final Map<String, SchemaFactory> schemaFactories = SchemaFactoryUtilities.getSchemaFactories();
        for (final SchemaFactory schemaFactory : schemaFactories.values()) {
            final SchemaDestination destination = new SchemaDestination(schemaFactory);
            destinations.add(destination);
            if (defaultDestination == null) {
                defaultDestination = destination;
            }
        }

        graphComboBox = new ComboBox<>();
        graphComboBox.setItems(destinations);
        graphComboBox.setOnAction((final ActionEvent t) -> {
            importController.setDestination(graphComboBox.getSelectionModel().getSelectedItem());
        });
        graphComboBox.getSelectionModel().select(defaultDestination);
        importController.setDestination(defaultDestination);
        GridPane.setConstraints(graphComboBox, 1, 4, 2, 1, HPos.LEFT, VPos.TOP);

        final Button sampleButton = new Button("Query");
        sampleButton.setOnAction((final ActionEvent t) -> {
            if (!query.getText().isBlank() && dbConnectionComboBox.getValue() != null) {
                importController.setDBConnection(dbConnectionComboBox.getValue());
                importController.setQuery(query.getText());
                importController.setUsername(username.getText());
                importController.setPassword(password.getText());
                importController.updateSampleData();
            }
        });
        GridPane.setConstraints(sampleButton, 2, 4, 2, 1, HPos.LEFT, VPos.TOP);

        getChildren().addAll(fileLabel, dbConnectionComboBox, manageConnectionsBtn, usernameLabel, username, passwordLabel, password, queryLabel, query, destinationLabel, graphComboBox, sampleButton);

    }

    public void setParameters(final PluginParameters parameters) {
        parametersPane.getChildren().clear();
        if (parameters != null) {
            final PluginParametersPane pluginParametersPane = PluginParametersPane.buildPane(parameters, null);
            parametersPane.getChildren().add(pluginParametersPane);
        }
    }

    /**
     * The import controller has been modified: update the GUI to match.
     *
     * @param importController The ImportController.
     */
    void update(final ImportController importController) {
        graphComboBox.getSelectionModel().select(importController.getDestination());

    }

    /**
     * Return the selected destination
     *
     * @return ImportDestination destination
     */
    public final ImportDestination<?> getDestination() {
        return graphComboBox.getSelectionModel().getSelectedItem();
    }
}
