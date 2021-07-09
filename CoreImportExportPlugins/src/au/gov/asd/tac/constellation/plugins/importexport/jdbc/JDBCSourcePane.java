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

import au.gov.asd.tac.constellation.plugins.importexport.EasyGridPane;
import au.gov.asd.tac.constellation.plugins.importexport.ImportController;
import au.gov.asd.tac.constellation.plugins.importexport.SourcePane;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class JDBCSourcePane extends SourcePane {

    private static final Logger LOGGER = Logger.getLogger(JDBCSourcePane.class.getName());
    private static final int GRIDPANE_MIN_WIDTH = 200;
    private static final int CONNPANE_PREF_HEIGHT = 200;
    private static final int SCROLLPANE_PREF_HEIGHT = 200;
    private static final int SCROLLPANE_PREF_WIDTH = 400;
    private static final int LARGE_SCROLLPANE_PREF_HEIGHT = 400;
    private static final int LARGE_SCROLLPANE_PREF_WIDTH = 800;
    private static final int ADD_CONNECTION_PANE_HEIGHT = 255;
    private static final int ADD_DRIVER_PANE_HEIGHT = 150;
    private static final int MANAGE_CONNECTIONS_PANE_HEIGHT = 345;
    private static final Insets GRIDPANE_PADDING = new Insets(5);
    private static final int GAP = 10;
    private static final String ACTION_CANCEL = "Cancel";
    private static final String TITLE_JDBC_IMPORT = "JDBC Import";

    private final ComboBox<JDBCConnection> dbConnectionComboBox;

    public JDBCSourcePane(final JDBCImportController importController) {
        super(importController);

        final JDBCDriverManager driverManager = JDBCDriverManager.getDriverManager();
        final JDBCConnectionManager connectionManager = JDBCConnectionManager.getConnectionManager();

        final Label fileLabel = new Label("Connection:");
        GridPane.setConstraints(fileLabel, 0, 0, 1, 1, HPos.LEFT, VPos.TOP);

        final ObservableList<JDBCConnection> connections = FXCollections.observableArrayList();
        connections.clear();
        connections.addAll(connectionManager.getConnections());

        dbConnectionComboBox = new ComboBox<>();
        dbConnectionComboBox.setItems(connections);
        dbConnectionComboBox.setOnAction((final ActionEvent t)
                -> importController.setDBConnection(dbConnectionComboBox.getSelectionModel().getSelectedItem()));
        GridPane.setConstraints(dbConnectionComboBox, 1, 0, 1, 1, HPos.LEFT, VPos.TOP);

        final EventHandler<ActionEvent> manageConnectionsAction;
        manageConnectionsAction = (final ActionEvent t) -> {
            final Stage dialog = new Stage();
            final ComboBox<JDBCDriver> driver = new ComboBox<>();
            final BorderPane root = new BorderPane();
            final EasyGridPane gridPane = new EasyGridPane();
            gridPane.addColumnConstraint(true, HPos.LEFT, Priority.ALWAYS, Double.MAX_VALUE, GRIDPANE_MIN_WIDTH,
                    GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0,
                    GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.addRowConstraint(true, VPos.TOP, Priority.ALWAYS, Double.MAX_VALUE, 0,
                    GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.addRowConstraint(true, VPos.BOTTOM, Priority.ALWAYS, Double.MAX_VALUE, 0,
                    GridPane.USE_COMPUTED_SIZE, -1);
            gridPane.setPadding(GRIDPANE_PADDING);
            gridPane.setHgap(GAP);
            gridPane.setVgap(GAP);
            final TabPane tp = new TabPane();
            final Tab connectionsTab = new Tab("Connections");
            connectionsTab.setClosable(false);
            final EasyGridPane connectionsPane = new EasyGridPane();
            connectionsPane.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
            connectionsPane.setPadding(GRIDPANE_PADDING);
            connectionsPane.setHgap(GAP);
            connectionsPane.setVgap(GAP);
            final TableView connectionsTable = new TableView();
            connectionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            connectionsTable.getSelectionModel().setCellSelectionEnabled(false);
            connectionsTable.prefHeightProperty().set(CONNPANE_PREF_HEIGHT);
            connectionsTable.prefWidthProperty().bind(dialog.widthProperty());
            final TableColumn<String, JDBCConnection> connectionName = new TableColumn<>("Name");
            connectionName.setCellValueFactory(new PropertyValueFactory<>("connectionName"));
            final TableColumn<String, JDBCConnection> connectionString = new TableColumn<>("Connection String");
            connectionString.setCellValueFactory(new PropertyValueFactory<>("connectionString"));
            connectionsTable.getColumns().addAll(connectionName, connectionString);
            connectionsTable.getItems().addAll(connectionManager.getConnections());
            connectionsTable.getSortOrder().add(connectionName);
            connectionsPane.add(connectionsTable, 0, 0, 4, 1);

            final EventHandler<ActionEvent> addConnectionAction
                    = (final ActionEvent event) -> addOrModifyConnection(connectionsTable, null);

            final Button openAddConnectionWindowButton = new Button("Add");
            openAddConnectionWindowButton.setOnAction(addConnectionAction);
            connectionsPane.add(openAddConnectionWindowButton, 0, 1, 1, 1);

            //Modify Connection Button
            final EventHandler<ActionEvent> editConnectionAction = (final ActionEvent event) -> {
                final JDBCConnection connection = (JDBCConnection) connectionsTable.getSelectionModel().getSelectedItem();
                if (connection != null) {
                    addOrModifyConnection(connectionsTable, connection);
                } else {
                    NotifyDisplayer.displayAlert("Manage Connections", "Select a connection to modify", "", AlertType.INFORMATION);
                }
            };
            final Button editConnectionButton = new Button("Modify");
            editConnectionButton.setOnAction(editConnectionAction);
            connectionsPane.add(editConnectionButton, 1, 1, 1, 1);

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
            connectionsPane.add(removeBtn, 2, 1, 1, 1);

            final Button buttonCancel2 = new Button(ACTION_CANCEL);
            buttonCancel2.setOnAction((final ActionEvent event) -> {
                event.consume();
                final Stage stage = (Stage) buttonCancel2.getScene().getWindow();
                stage.close();
            });
            connectionsPane.add(buttonCancel2, 3, 1, 1, 1);

            final Tab driversTab = new Tab("Drivers");
            driversTab.setClosable(false);
            final EasyGridPane driversTabGridPane = new EasyGridPane();
            driversTabGridPane.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
            driversTabGridPane.setPadding(GRIDPANE_PADDING);
            driversTabGridPane.setHgap(GAP);
            driversTabGridPane.setVgap(GAP);
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
            driversTabGridPane.add(driverTable, 0, 0, 4, 1);
            final EventHandler<ActionEvent> removebtnAction = (final ActionEvent t1) -> {
                final JDBCDriver d = (JDBCDriver) driverTable.getSelectionModel().getSelectedItem();
                if (d != null) {
                    if (driverManager.isDriverUsed(d.getName())) {
                        final Optional<ButtonType> res = NotifyDisplayer.displayConfirmationAlert(TITLE_JDBC_IMPORT, "Remove Driver", "Connections exist using this Driver.\nThe "
                                + "connections that use this driver will be deleted, do you want to proceed?");
                        if (!res.isPresent() || res.get() == ButtonType.NO) {
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
            };

            final Button addDriverButton = new Button("Add");
            addDriverButton.setOnAction((final ActionEvent t1) -> {
                final Stage d = new Stage();
                final BorderPane r = new BorderPane();
                final EasyGridPane gp = new EasyGridPane();
                gp.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
                gp.setPadding(GRIDPANE_PADDING);
                gp.setHgap(GAP);
                gp.setVgap(GAP);
                final Label jarLabel = new Label("Driver");
                gp.add(jarLabel, 0, 0, 1, 1);
                final TextField j = new TextField();
                gp.add(j, 1, 0, 1, 1);
                final Label nameLabel = new Label("Name");
                gp.add(nameLabel, 0, 1, 1, 1);
                final ComboBox driverName = new ComboBox();
                gp.add(driverName, 1, 1, 1, 1);
                final Button chooser = new Button(" ... ");
                chooser.setOnAction((final ActionEvent t2) -> {
                    final FileChooser cho = new FileChooser();
                    cho.getExtensionFilters().add(new ExtensionFilter(".jar", "*.jar"));

                    final File f = cho.showOpenDialog(d);
                    if (f != null) {
                        try {
                            j.setText(f.getCanonicalPath());
                            driverName.getItems().clear();
                            driverName.getItems().addAll(JDBCDriver.getDrivers(f));
                            driverName.getSelectionModel().selectFirst();
                        } catch (final IOException ex) {
                            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                        }
                    }
                });
                gp.add(chooser, 2, 0, 1, 1);
                final Button add = new Button("Add");
                add.setOnAction((final ActionEvent t2) -> {
                    if (driverName.getSelectionModel().getSelectedItem() != null) {
                        if (driverManager.isDriverUsed((String) driverName.getSelectionModel().getSelectedItem())) {
                            final Optional<ButtonType> res = NotifyDisplayer.displayConfirmationAlert(TITLE_JDBC_IMPORT,
                                    "Add Driver", "This Driver already exists.\n Do you want to overwrite?");
                            if (!res.isPresent() || res.get() == ButtonType.NO) {
                                return;
                            }
                        }
                        driverManager.addDriver((String) driverName.getSelectionModel().getSelectedItem(), new File(j.getText()));
                        driverTable.getItems().clear();
                        driverTable.getItems().addAll(driverManager.getDrivers());
                        driver.getItems().clear();
                        driver.getItems().addAll(driverManager.getDrivers());
                        d.close();
                    }
                });
                gp.add(add, 0, 2, 1, 1);
                final Button buttonCancel = new Button(ACTION_CANCEL);
                buttonCancel.setOnAction((final ActionEvent event) -> {
                    event.consume();
                    final Stage stage = (Stage) buttonCancel.getScene().getWindow();
                    stage.close();
                });
                gp.add(buttonCancel, 2, 2, 1, 1);

                final ScrollPane sp = new ScrollPane(gp);
                sp.setFitToWidth(true);
                sp.setPrefHeight(SCROLLPANE_PREF_HEIGHT);
                sp.setPrefWidth(SCROLLPANE_PREF_WIDTH);
                sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                r.setCenter(sp);

                final Scene scene2 = new Scene(r);
                scene2.setFill(Color.WHITESMOKE);
                scene2.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
                d.setScene(scene2);
                d.setTitle("Add Driver");
                d.centerOnScreen();
                d.initOwner(dialog);
                d.initModality(Modality.APPLICATION_MODAL);
                d.setAlwaysOnTop(true);
                d.setWidth(LARGE_SCROLLPANE_PREF_WIDTH);
                d.setHeight(ADD_DRIVER_PANE_HEIGHT);
                d.showAndWait();
            });
            driversTabGridPane.add(addDriverButton, 0, 1, 1, 1);

            final Button removeBtn1 = new Button("Remove");
            removeBtn1.setOnAction(removebtnAction);
            driversTabGridPane.add(removeBtn1, 2, 1, 1, 1);

            final Button buttonCancel = new Button(ACTION_CANCEL);
            buttonCancel.setOnAction((final ActionEvent event) -> {
                event.consume();
                final Stage stage = (Stage) buttonCancel.getScene().getWindow();
                stage.close();
            });
            driversTabGridPane.add(buttonCancel, 3, 1, 1, 1);

            driversTab.setContent(driversTabGridPane);
            connectionsTab.setContent(connectionsPane);
            tp.getTabs().addAll(driversTab, connectionsTab);
            gridPane.getChildren().addAll(tp);
            final ScrollPane sp = new ScrollPane(gridPane);
            sp.setFitToWidth(true);
            sp.setPrefHeight(LARGE_SCROLLPANE_PREF_HEIGHT);
            sp.setPrefWidth(LARGE_SCROLLPANE_PREF_WIDTH);
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            root.setCenter(sp);

            final Scene scene3 = new Scene(root);
            scene3.setFill(Color.WHITESMOKE);
            scene3.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
            dialog.setScene(scene3);
            dialog.setTitle("Manage Connections");
            dialog.centerOnScreen();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setAlwaysOnTop(true);
            dialog.setWidth(LARGE_SCROLLPANE_PREF_WIDTH);
            dialog.setHeight(MANAGE_CONNECTIONS_PANE_HEIGHT);
            dialog.showAndWait();
        };

        final Button manageConnectionsBtn = new Button("Manage connections");
        manageConnectionsBtn.setOnAction(manageConnectionsAction);
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

        final Button queryButton = new Button("Query");
        queryButton.setOnAction((final ActionEvent t) -> {
            if (!query.getText().isBlank() && dbConnectionComboBox.getValue() != null) {
                importController.setDBConnection(dbConnectionComboBox.getValue());
                importController.setQuery(query.getText());
                importController.setUsername(username.getText());
                importController.setPassword(password.getText());
                importController.updateSampleData();
            }
        });
        GridPane.setConstraints(queryButton, 2, 4, 2, 1, HPos.RIGHT, VPos.TOP);

        getChildren().addAll(fileLabel, dbConnectionComboBox, manageConnectionsBtn, usernameLabel, username,
                passwordLabel, password, queryLabel, query, destinationLabel, graphComboBox, queryButton);
    }

    /**
     * The import controller has been modified: update the GUI to match.
     *
     * @param importController The ImportController.
     */
    @Override
    public void update(final ImportController importController) {
        graphComboBox.getSelectionModel().select(((JDBCImportController) importController).getDestination());

    }

    private void addOrModifyConnection(TableView<JDBCConnection> connectionsTable, JDBCConnection connection) {
        final boolean add = (connection == null);
        final ComboBox<JDBCDriver> driversComboBox = new ComboBox<>();
        final JDBCDriverManager driverManager = JDBCDriverManager.getDriverManager();
        final JDBCConnectionManager connectionManager = JDBCConnectionManager.getConnectionManager();
        final ObservableList<JDBCConnection> connections = FXCollections.observableArrayList();
        connections.clear();
        connections.addAll(connectionManager.getConnections());
        final Stage dialog = new Stage();

        final Stage stage = new Stage();
        // add stuff here.
        final BorderPane r = new BorderPane();
        final EasyGridPane gp = new EasyGridPane();
        gp.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
        gp.setPadding(GRIDPANE_PADDING);
        gp.setHgap(GAP);
        gp.setVgap(GAP);
        final Label nameLabel = new Label("Connection Name");
        gp.add(nameLabel, 0, 0, 1, 1);
        final TextField cn = new TextField(add ? "" : connection.getConnectionName());
        gp.add(cn, 1, 0, 2, 1);
        final Label driverLabel = new Label("Driver");
        gp.add(driverLabel, 0, 1, 1, 1);
        driversComboBox.getItems().clear();
        driversComboBox.getItems().addAll(driverManager.getDrivers());
        if (!add) {
            driversComboBox.getSelectionModel().select(connection.getDriver());
        }
        gp.add(driversComboBox, 1, 1, 1, 1);
        final Label connectionStringLabel = new Label("Connection String");
        gp.add(connectionStringLabel, 0, 2, 1, 1);
        final TextField connectionStringF = new TextField(add ? "" : connection.getConnectionString());
        gp.add(connectionStringF, 1, 2, 2, 1);
        final Label usernameLabel = new Label("Username");
        gp.add(usernameLabel, 0, 3, 1, 1);
        final TextField username = new TextField();
        gp.add(username, 1, 3, 2, 1);
        final Label passwordLabel = new Label("Password");
        gp.add(passwordLabel, 0, 4, 1, 1);
        final PasswordField password = new PasswordField();
        gp.add(password, 1, 4, 2, 1);
        final Button addConnection = new Button(add ? "Add" : "Save");
        addConnection.setOnAction((final ActionEvent t2) -> {

            if (add && connectionsTable.getItems().stream().anyMatch(v -> v.getConnectionName().equals(cn.getText()))) {
                final Optional<ButtonType> res = NotifyDisplayer.displayConfirmationAlert(TITLE_JDBC_IMPORT, "Add Connection", "There exist another connection with this name "
                        + cn.getText() + ". Do you want to overwrite it?");
                if (!res.isPresent() || res.get() == ButtonType.NO) {
                    return;
                }
            }

            if (!add && !cn.getText().isBlank() && !cn.getText().equals(connection.getConnectionName())
                    && connectionsTable.getItems().stream().anyMatch(v -> v.getConnectionName().equals(cn.getText()))) {
                NotifyDisplayer.displayAlert(TITLE_JDBC_IMPORT, "Modify Connection", "There exist another connection with the name "
                        + cn.getText() + ". Choose a different name to proceed.", AlertType.CONFIRMATION);
                return;
            }

            if (!cn.getText().isBlank() && driversComboBox.getValue() != null
                    && !connectionStringF.getText().isBlank()
                    && ((add && connectionManager.addConnection(cn.getText(), driversComboBox.getValue(), username.getText(),
                            password.getText(), connectionStringF.getText()))
                    || (!add && connectionManager.updateConnection(connection.getConnectionName(), cn.getText(), driversComboBox.getValue(), username.getText(),
                            password.getText(), connectionStringF.getText())))) {
                connectionsTable.getItems().clear();
                connectionsTable.getItems().addAll(connectionManager.getConnections());
                connections.clear();
                connections.addAll(connectionManager.getConnections());
                stage.close();
            }
        });
        gp.add(addConnection, 0, 5, 1, 1);
        final Button test = new Button("Test");
        test.setOnAction(t -> {
            if (!cn.getText().isBlank()
                    && driversComboBox.getValue() != null
                    && !connectionStringF.getText().isBlank()
                    && connectionManager.testConnection(cn.getText(), driversComboBox.getValue(), username.getText(),
                            password.getText(), connectionStringF.getText())) {
                NotifyDisplayer.displayAlert(TITLE_JDBC_IMPORT, "Connection Success", "", AlertType.INFORMATION);
            }
        });
        gp.add(test, 1, 5, 1, 1);

        final Button buttonCancel = new Button(ACTION_CANCEL);
        buttonCancel.setOnAction((final ActionEvent event) -> {
            event.consume();
            Stage currentStage = (Stage) buttonCancel.getScene().getWindow();
            currentStage.close();
        });
        gp.add(buttonCancel, 2, 5, 1, 1);

        final ScrollPane sp = new ScrollPane(gp);
        sp.setFitToWidth(true);
        sp.setPrefHeight(SCROLLPANE_PREF_HEIGHT);
        sp.setPrefWidth(SCROLLPANE_PREF_WIDTH);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        r.setCenter(sp);

        final Scene scene1 = new Scene(r);
        scene1.setFill(Color.WHITESMOKE);
        scene1.getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
        stage.setScene(scene1);
        stage.setTitle(add ? "Add Connection" : "Modify Connection");
        stage.centerOnScreen();
        stage.initOwner(dialog);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setWidth(LARGE_SCROLLPANE_PREF_WIDTH);
        stage.setHeight(ADD_CONNECTION_PANE_HEIGHT);
        stage.showAndWait();
    }
}
