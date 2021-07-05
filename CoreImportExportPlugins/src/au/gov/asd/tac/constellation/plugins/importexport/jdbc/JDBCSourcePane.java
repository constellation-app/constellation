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
    private static final Insets GRIDPANE_PADDING = new Insets(5);
    private static final int GAP = 10;

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
            final EventHandler<ActionEvent> addConnectionAction = (final ActionEvent t1) -> {
                final Stage d = new Stage();
                // add stuff here.
                final BorderPane r = new BorderPane();
                final EasyGridPane gp = new EasyGridPane();
                gp.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
                gp.setPadding(GRIDPANE_PADDING);
                gp.setHgap(GAP);
                gp.setVgap(GAP);
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
                final Button addConnection = new Button("Add");
                addConnection.setOnAction((final ActionEvent t2) -> {
                    if (!cn.getText().isBlank() && driver.getValue() != null
                            && !connectionStringF.getText().isBlank()
                            && connectionManager.addConnection(cn.getText(), driver.getValue(), username.getText(),
                                    password.getText(), connectionStringF.getText())) {
                        connectionsTable.getItems().clear();
                        connectionsTable.getItems().addAll(connectionManager.getConnections());
                        connections.clear();
                        connections.addAll(connectionManager.getConnections());
                        d.close();
                    }
                });
                gp.add(addConnection, 0, 5, 1, 1);
                final Button test = new Button("Test");
                test.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent t2) {
                        if (!cn.getText().isBlank()
                                && driver.getValue() != null
                                && !connectionStringF.getText().isBlank()
                                && connectionManager.testConnection(cn.getText(), driver.getValue(), username.getText(),
                                        password.getText(), connectionStringF.getText())) {
                            NotifyDisplayer.displayAlert("JDBC Import", "Connection Success", "", AlertType.INFORMATION);
                        }
                    }
                });
                gp.add(test, 1, 5, 1, 1);
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
                d.setScene(scene1);
                d.setTitle("Add Connection");
                d.centerOnScreen();
                d.initOwner(dialog);
                d.initModality(Modality.APPLICATION_MODAL);
                d.showAndWait();
            };
            final Button openAddConnectionWindowButton = new Button("Add");
            openAddConnectionWindowButton.setOnAction(addConnectionAction);
            connectionsPane.add(openAddConnectionWindowButton, 1, 1, 1, 1);
            final Tab driversTab = new Tab("Drivers");
            driversTab.setClosable(false);
            final EasyGridPane dtRoot = new EasyGridPane();
            dtRoot.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
            dtRoot.setPadding(GRIDPANE_PADDING);
            dtRoot.setHgap(GAP);
            dtRoot.setVgap(GAP);
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
            final EventHandler<ActionEvent> removebtnAction = (final ActionEvent t1) -> {
                final JDBCDriver d = (JDBCDriver) driverTable.getSelectionModel().getSelectedItem();
                if (d != null) {
                    if (driverManager.isDriverUsed(d.getName())) {
                        final TextArea a = new TextArea();
                        a.setWrapText(true);
                        a.setEditable(false);
                        a.setText("Connections exist using this Driver.\nThe connections that use this "
                                + "driver will be deleted, do you want to proceed?");
                        final Alert b = new Alert(AlertType.CONFIRMATION, "", ButtonType.NO, ButtonType.YES);
                        b.getDialogPane().getStylesheets().add(JavafxStyleManager.getMainStyleSheet());
                        b.getDialogPane().setContent(a);
                        final Optional<ButtonType> res = b.showAndWait();
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
            final Button removeBtn1 = new Button("Remove");
            removeBtn1.setOnAction(removebtnAction);
            dtRoot.add(removeBtn1, 0, 1, 1, 1);
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
                gp.add(j, 1, 0, 2, 1);
                final Label nameLabel = new Label("Name");
                gp.add(nameLabel, 0, 1, 1, 1);
                final ComboBox driverName = new ComboBox();
                gp.add(driverName, 1, 1, 1, 1);
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
                d.showAndWait();
            });
            dtRoot.add(addDriverButton, 1, 1, 1, 1);
            driversTab.setContent(dtRoot);
            connectionsTab.setContent(connectionsPane);
            tp.getTabs().addAll(connectionsTab, driversTab);
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
            dialog.sizeToScene();
            dialog.initModality(Modality.APPLICATION_MODAL);
            //dialog.setAlwaysOnTop(true); // Test if dialog appears behind - maybe dialog.toFront() is suffice to solve the issue.
            dialog.toFront();
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

}
