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
import au.gov.asd.tac.constellation.plugins.importexport.ImportSingleton;
import au.gov.asd.tac.constellation.plugins.importexport.SourcePane;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import org.apache.commons.lang3.StringUtils;

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
    private static final String TITLE_JDBC_IMPORT = "Database Import";
    private static final String ADD_CONNECTION = "Add Connection";
    private static final String MODIFY_CONNECTION = "Modify Connection";
    private static final String ADD_DRIVER = "Add Driver";
    private static final String CLEAR_RESULTS = "Clear";

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

            final GridPane addRemModGp = new GridPane();
            addRemModGp.setHgap(GAP);

            final Button openAddConnectionWindowButton = new Button("Add");
            openAddConnectionWindowButton.setOnAction(addConnectionAction);
            addRemModGp.add(openAddConnectionWindowButton, 0, 1, 1, 1);

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
            editConnectionButton.setAlignment(Pos.CENTER_LEFT);
            editConnectionButton.setOnAction(editConnectionAction);
            addRemModGp.add(editConnectionButton, 1, 1, 1, 1);

            final Button removeBtn = new Button("Remove");
            removeBtn.setOnAction((final ActionEvent t1) -> {
                final JDBCConnection d = (JDBCConnection) connectionsTable.getSelectionModel().getSelectedItem();
                if (d != null) {
                    connectionManager.deleteConnection(d.getConnectionName());
                    connectionsTable.getItems().clear();
                    connectionsTable.getItems().addAll(connectionManager.getConnections());
                    connections.clear();
                    connections.addAll(connectionManager.getConnections());
                    dbConnectionComboBox.setItems(connections);
                }
            });
            addRemModGp.add(removeBtn, 2, 1, 1, 1);
            connectionsPane.add(addRemModGp, 0, 1, 1, 1);

            final Button buttonOk2 = new Button("OK");
            buttonOk2.setOnAction((final ActionEvent event) -> {
                event.consume();
                final Stage stage = (Stage) buttonOk2.getScene().getWindow();
                stage.close();
            });
            connectionsPane.add(buttonOk2, 3, 1, 1, 1);

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
            addDriverButton.setOnAction((final ActionEvent t1) -> openAddDriverDialog(driverManager, driverTable, driver, dialog));
            driversTabGridPane.add(addDriverButton, 0, 1, 1, 1);

            final Button removeBtn1 = new Button("Remove");
            removeBtn1.setOnAction(removebtnAction);
            driversTabGridPane.add(removeBtn1, 1, 1, 1, 1);

            final Button buttonOk = new Button("OK");
            buttonOk.setOnAction((final ActionEvent event) -> {
                event.consume();
                final Stage stage = (Stage) buttonOk.getScene().getWindow();
                stage.close();
            });
            driversTabGridPane.add(buttonOk, 3, 1, 1, 1);

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
            scene3.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
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

        final Button clearButton = new Button(CLEAR_RESULTS);
        clearButton.setOnAction(e -> {
            query.setText("");
            ImportSingleton.getDefault().triggerClearDataFlag();
        });

        final Button queryButton = new Button("Query");
        queryButton.setOnAction((final ActionEvent t) -> {
            if (!query.getText().isBlank() && dbConnectionComboBox.getValue() != null) {

                importController.setDBConnection(dbConnectionComboBox.getValue());
                importController.setQuery(query.getText());
                importController.setUsername(username.getText());
                importController.setPassword(password.getText());
                importController.updateSampleData();
            } else {
                NotifyDisplayer.displayAlert(TITLE_JDBC_IMPORT, "No Query Entered",
                        "Please enter a query.", Alert.AlertType.ERROR);
            }
        });

        final GridPane buttonPane = new GridPane();
        buttonPane.add(clearButton, 0, 0);
        buttonPane.add(queryButton, 1, 0);
        buttonPane.setHgap(10);
        buttonPane.setPadding(new Insets(0, 0, 0, 25));

        GridPane.setConstraints(buttonPane, 2, 4, 3, 1, HPos.RIGHT, VPos.TOP);

        getChildren().addAll(fileLabel, dbConnectionComboBox, manageConnectionsBtn, usernameLabel, username,
                passwordLabel, password, queryLabel, query, destinationLabel, graphComboBox, buttonPane);
    }

    private void openAddDriverDialog(final JDBCDriverManager driverManager, final TableView driverTable, final ComboBox<JDBCDriver> driver, final Stage dialog) {
        final Stage d = new Stage();
        final BorderPane r = new BorderPane();
        final EasyGridPane gp = new EasyGridPane();
        gp.getColumnConstraints().addAll(column0Constraints, column1Constraints, column2Constraints);
        gp.setPadding(GRIDPANE_PADDING);
        gp.setHgap(GAP);
        gp.setVgap(GAP);
        final Label jarLabel = new Label("Driver");
        gp.add(jarLabel, 0, 0, 1, 1);
        final TextField driverFilePath = new TextField();
        driverFilePath.setPromptText("Select or enter the JDBC driver JAR file");
        driverFilePath.setFocusTraversable(false);
        gp.add(driverFilePath, 1, 0, 1, 1);

        final Label nameLabel = new Label("Name");
        gp.add(nameLabel, 0, 1, 1, 1);
        final ComboBox driverName = new ComboBox();
        gp.add(driverName, 1, 1, 1, 1);
        final Button chooser = new Button(" ... ");
        chooser.setOnAction((final ActionEvent t2) -> {
            final FileChooser cho = new FileChooser();
            cho.getExtensionFilters().add(new ExtensionFilter(FileExtensionConstants.JAR, "*.jar"));

            final File f = cho.showOpenDialog(d);

            if (f != null) {
                try {
                    driverFilePath.setText(f.getCanonicalPath());
                    setDriver(f, driverName);
                } catch (final IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        });
        // Allow manual editing on the driver path
        driverFilePath.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.isBlank(driverFilePath.getText())) {
                final File f = new File(newValue);
                if (f != null) {
                    setDriver(f, driverName);
                }
            }
        });
        gp.add(chooser, 2, 0, 1, 1);
        final Button add = new Button("Add");
        add.setOnAction((final ActionEvent t2) -> {
            if (!validateDriverParams(driverFilePath, driverName)) {
                return;
            }

            if (driverName.getSelectionModel().getSelectedItem() != null) {
                if (driverManager.isDriverUsed((String) driverName.getSelectionModel().getSelectedItem())) {
                    final Optional<ButtonType> res = NotifyDisplayer.displayConfirmationAlert(TITLE_JDBC_IMPORT,
                            ADD_DRIVER, "This Driver already exists.\n Do you want to overwrite?");
                    if (!res.isPresent() || res.get() == ButtonType.NO) {
                        return;
                    }
                }
                driverManager.addDriver((String) driverName.getSelectionModel().getSelectedItem(), new File(driverFilePath.getText()));
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
        scene2.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        d.setScene(scene2);
        d.setTitle(ADD_DRIVER);
        d.centerOnScreen();
        d.initOwner(dialog);
        d.initModality(Modality.APPLICATION_MODAL);
        d.setAlwaysOnTop(true);
        d.setWidth(LARGE_SCROLLPANE_PREF_WIDTH);
        d.setHeight(ADD_DRIVER_PANE_HEIGHT);
        d.showAndWait();
    }

    private void setDriver(final File f, final ComboBox driverName) {
        driverName.getItems().clear();
        driverName.getItems().addAll(JDBCDriver.getDrivers(f));
        driverName.getSelectionModel().selectFirst();
    }

    /**
     * The import controller has been modified: update the GUI to match.
     *
     * @param importController The ImportController.
     */
    @Override
    public void update(final ImportController importController) {
        graphComboBox.getItems().stream()
                .filter(importDestination -> importController.getDestination().toString().equals(importDestination.toString()))
                .findAny()
                .ifPresent(graphComboBox.getSelectionModel()::select);
    }

    private boolean validateDriverParams(final TextField driverFilePath, final ComboBox driverName) {
        final StringJoiner missingParamsMsgs = new StringJoiner(System.lineSeparator() + System.lineSeparator());

        if (StringUtils.isBlank(driverFilePath.getText())) {
            missingParamsMsgs.add("Driver - please select or enter the JDBC driver JAR file before continuing.");
        } else if (driverName.getValue() == null) {
            missingParamsMsgs.add("Driver Name - please select or enter a valid JDBC driver JAR file to populate the driver name.");
        }
        if (missingParamsMsgs.length() > 0) {
            NotifyDisplayer.displayAlert(ADD_DRIVER,
                    "Missing driver parameters",
                    missingParamsMsgs.toString(),
                    AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean validateConnectionParams(final TextField cn, final TextField connectionStringF, final ComboBox<JDBCDriver> driversComboBox) {
        final StringJoiner missingParamsMsgs = new StringJoiner(System.lineSeparator() + System.lineSeparator());

        if (StringUtils.isBlank(cn.getText())) {
            missingParamsMsgs.add("\tConnection Name - please enter a name before continuing, it can be any name of your choosing.");
        }
        if (StringUtils.isBlank(connectionStringF.getText())) {
            missingParamsMsgs.add("\tConnection String - please enter a connection string before continuing. "
                    + "The connection string will contain the database type, host, port, and database of the db in the format "
                    + "\"jdbc:[database name]://[host]:[port]/[database name]\"" + System.lineSeparator()
                    + "e.g. jdbc:mysql://localhost:3306/employees for mysql or jdbc:postgresql://localhost:5432/test for postgres.");
        }
        if (driversComboBox.getValue() == null) {
            missingParamsMsgs.add("\tConnection Driver - please select a driver before continuing.");
        }

        if (missingParamsMsgs.length() > 0) {
            NotifyDisplayer.displayAlert(ADD_CONNECTION,
                    "Missing connection parameters",
                    "The connection name, driver, and connection string are all required fields, you are missing the following:"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + missingParamsMsgs.toString(),
                    AlertType.ERROR);
            return false;
        }
        return true;
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
        cn.setPromptText("Enter a name for your connection");
        cn.setFocusTraversable(false);
        cn.focusedProperty().addListener((obs, oldVal, newVal) -> {
            cn.setStyle(""); 
            if (!newVal && StringUtils.isBlank(cn.getText())){
                cn.setStyle("-fx-text-box-border: red;");  
            }
        });
        gp.add(cn, 1, 0, 2, 1);
        final Label driverLabel = new Label("Driver");
        gp.add(driverLabel, 0, 1, 1, 1);
        driversComboBox.getItems().clear();
        driversComboBox.getItems().addAll(driverManager.getDrivers());
        if (!driverManager.getDrivers().isEmpty()) {
            driversComboBox.getSelectionModel().select(driversComboBox.getItems().get(0));
        }
        if (!add) {
            driversComboBox.getSelectionModel().select(connection.getDriver());
        }
        gp.add(driversComboBox, 1, 1, 1, 1);
        final Label connectionStringLabel = new Label("Connection String");
        gp.add(connectionStringLabel, 0, 2, 1, 1);
        final TextField connectionStringF = new TextField(add ? "" : connection.getConnectionString());
        connectionStringF.setPromptText("Enter a URL to connect to, eg. jdbc:sqlite:C:/my_folder/database.sqlite");
        connectionStringF.setFocusTraversable(false);
        connectionStringF.focusedProperty().addListener((obs, oldVal, newVal) -> {
            connectionStringF.setStyle(""); 
            if (!newVal && StringUtils.isBlank(connectionStringF.getText())){
                connectionStringF.setStyle("-fx-text-box-border: red;");  
            }
        });
        gp.add(connectionStringF, 1, 2, 2, 1);
        final Label usernameLabel = new Label("Username");
        gp.add(usernameLabel, 0, 3, 1, 1);
        final TextField username = new TextField();
        username.setPromptText("Optional: Set a username");
        username.setFocusTraversable(false);
        gp.add(username, 1, 3, 2, 1);
        final Label passwordLabel = new Label("Password");
        gp.add(passwordLabel, 0, 4, 1, 1);
        final PasswordField password = new PasswordField();
        password.setPromptText("Optional: Set a password");
        password.setFocusTraversable(false);

        gp.add(password, 1, 4, 2, 1);
        final Button addConnection = new Button(add ? "Add" : "Save");
        addConnection.setOnAction((final ActionEvent t2) -> {
            if (!validateConnectionParams(cn, connectionStringF, driversComboBox)){
                return;
            }

            if (add && connectionsTable.getItems().stream().anyMatch(v -> v.getConnectionName().equals(cn.getText()))) {
                final Optional<ButtonType> res = NotifyDisplayer.displayConfirmationAlert(TITLE_JDBC_IMPORT, ADD_CONNECTION, "There exist another connection with this name "
                        + cn.getText() + ". Do you want to overwrite it?");
                if (!res.isPresent() || res.get() == ButtonType.NO) {
                    return;
                }
            }

            if (!add && !cn.getText().isBlank() && !cn.getText().equals(connection.getConnectionName())
                    && connectionsTable.getItems().stream().anyMatch(v -> v.getConnectionName().equals(cn.getText()))) {
                NotifyDisplayer.displayAlert(TITLE_JDBC_IMPORT, MODIFY_CONNECTION, "There exist another connection with the name "
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
                dbConnectionComboBox.setItems(connections);
                stage.close();
            }
        });
        gp.add(addConnection, 1, 5, 1, 1);
        final Button test = new Button("Test Connection");
        test.setOnAction(t -> {
            if (!validateConnectionParams(cn, connectionStringF, driversComboBox)){
                return;
            }
            if (!cn.getText().isBlank()
                    && driversComboBox.getValue() != null
                    && !connectionStringF.getText().isBlank()
                    && connectionManager.testConnection(cn.getText(), driversComboBox.getValue(), username.getText(),
                            password.getText(), connectionStringF.getText())) {
                NotifyDisplayer.displayAlert(TITLE_JDBC_IMPORT, "Connection Success", "", AlertType.INFORMATION);
            }
        });
        gp.add(test, 0, 5, 1, 1);

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
        scene1.getStylesheets().addAll(JavafxStyleManager.getMainStyleSheet());
        stage.setScene(scene1);
        stage.setTitle(add ? ADD_CONNECTION : MODIFY_CONNECTION);
        stage.centerOnScreen();
        stage.initOwner(dialog);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setWidth(LARGE_SCROLLPANE_PREF_WIDTH);
        stage.setHeight(ADD_CONNECTION_PANE_HEIGHT);
        stage.showAndWait();
    }
}
