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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.importexport.ImportController;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.SchemaDestination;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert.AlertType;

public class JDBCImportController extends ImportController {

    private JDBCConnection connection;
    private String query;
    private String username;
    private String password;

    public JDBCImportController() {
        super();
        schemaInitialised = true;
    }

    public void setImportPane(final JDBCImportPane importPane) {
        this.importPane = importPane;
        super.setImportPane(importPane);
    }

    public boolean hasDBConnection() {
        return !(connection == null);
    }

    public void setDBConnection(final JDBCConnection connection) {
        this.connection = connection;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public List processImport() throws IOException, InterruptedException, PluginException {
        final List<ImportDefinition> definitions = configurationPane.createDefinitions();
        final List<String> importList = new ArrayList<>();
        importList.add(query);
        final Graph importGraph = currentDestination.getGraph();
        final boolean schema = schemaInitialised;

        if (currentDestination instanceof SchemaDestination) {
            GraphManager.getDefault().addGraphManagerListener(new GraphManagerListener() {
                boolean opened = false;

                @Override
                public void graphOpened(Graph graph) {
                }

                @Override
                public void graphClosed(Graph graph) {
                }

                @Override
                public synchronized void newActiveGraph(Graph graph) {
                    if (graph == importGraph && !opened) {
                        opened = true;
                        GraphManager.getDefault().removeGraphManagerListener(this);
                        PluginExecutor.startWith(ImportJDBCPlugin.class.getName(), false)
                                .set(ImportJDBCPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                                .set(ImportJDBCPlugin.CONNECTION_PARAMETER_ID, connection)
                                .set(ImportJDBCPlugin.SCHEMA_PARAMETER_ID, schema)
                                .set(ImportJDBCPlugin.QUERY_PARAMETER_ID, query)
                                .set(ImportJDBCPlugin.USERNAME_PARAMETER_ID, username)
                                .set(ImportJDBCPlugin.PASSWORD_PARAMETER_ID, password)
                                .executeWriteLater(importGraph);
                    }
                }
            });
            GraphOpener.getDefault().openGraph(importGraph, "graph");
        } else {
            PluginExecutor.startWith(ImportJDBCPlugin.class.getName(), false)
                    .set(ImportJDBCPlugin.DEFINITIONS_PARAMETER_ID, definitions)
                    .set(ImportJDBCPlugin.CONNECTION_PARAMETER_ID, connection)
                    .set(ImportJDBCPlugin.QUERY_PARAMETER_ID, query)
                    .set(ImportJDBCPlugin.USERNAME_PARAMETER_ID, username)
                    .set(ImportJDBCPlugin.PASSWORD_PARAMETER_ID, password)
                    .set(ImportJDBCPlugin.SCHEMA_PARAMETER_ID, schema)
                    .executeWriteLater(importGraph);
        }
        return importList;
    }

    @Override
    protected void updateSampleData() {
        if (connection == null || query == null || query.isBlank()) {
            currentColumns = new String[0];
            currentData = new ArrayList<>();
        } else {
            try {
                try (final Connection dbConnection = connection.getConnection(username, password)) {
                    if (!query.toLowerCase().contains(" limit ") && query.toLowerCase().startsWith("select ")) {
                        query += " limit " + PREVIEW_ROW_LIMIT;
                    }
                    try (final PreparedStatement ps = dbConnection.prepareStatement(query)) {
                        try (final ResultSet rs = ps.executeQuery()) {
                            int count = 0;
                            currentData.clear();
                            while (rs.next() && count < PREVIEW_ROW_LIMIT) {
                                count++;
                                final String[] d = new String[ps.getMetaData().getColumnCount()];
                                for (int i = 0; i < ps.getMetaData().getColumnCount(); i++) {
                                    d[i] = rs.getString(i + 1);
                                }
                                currentData.add(d);
                            }
                            currentColumns = new String[ps.getMetaData().getColumnCount() + 1];
                            currentColumns[0] = "Row";
                            final ResultSetMetaData rsmd = ps.getMetaData();
                            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                                currentColumns[i + 1] = rsmd.getColumnName(i + 1);
                            }
                        }
                    }
                }
            } catch (final MalformedURLException | ClassNotFoundException | SQLException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException ex) {
                NotifyDisplayer.displayAlert("JDBC Import", "Query Error", ex.getMessage(), AlertType.ERROR);
            }
        }

        if (configurationPane != null) {
            configurationPane.setSampleData(currentColumns, currentData);
        }
    }
}
