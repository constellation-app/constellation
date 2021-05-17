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
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.importexport.ImportController;
import au.gov.asd.tac.constellation.plugins.importexport.ImportDefinition;
import au.gov.asd.tac.constellation.plugins.importexport.SchemaDestination;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert.AlertType;
import org.apache.commons.lang3.StringUtils;

public class JDBCImportController extends ImportController {

    private static final Logger LOGGER = Logger.getLogger(JDBCImportController.class.getName());
    private static final String QUERY_LIMIT_TEXT = " limit ";
    private static final String SELECT_TEXT = "select ";

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
        return connection != null;
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
    public List<File> processImport() throws PluginException {
        final List<ImportDefinition> definitions = configurationPane.createDefinitions();
        final Graph importGraph = currentDestination.getGraph();
        final boolean schema = schemaInitialised;

        if (currentDestination instanceof SchemaDestination) {
            final GraphManagerListener graphListener = new GraphManagerListener() {
                private boolean opened;

                @Override
                public void graphOpened(final Graph graph) {
                    // Do nothing
                }

                @Override
                public void graphClosed(final Graph graph) {
                    // Do nothing
                }

                @Override
                public synchronized void newActiveGraph(final Graph graph) {
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
            };

            GraphManager.getDefault().addGraphManagerListener(graphListener);
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
        return Collections.emptyList();
    }

    @Override
    protected void updateSampleData() {
        final String queryCopy = this.query;
        if (connection == null || StringUtils.isBlank(queryCopy)) {
            currentColumns = new String[0];
            currentData = new ArrayList<>();
        } else {
            try (final Connection dbConnection = connection.getConnection(username, password);
                    final PreparedStatement ps = dbConnection.prepareStatement(queryCopy);
                    final ResultSet rs = ps.executeQuery()) {

                if (!query.toLowerCase(Locale.ENGLISH).contains(QUERY_LIMIT_TEXT) && query.toLowerCase(Locale.ENGLISH).startsWith(SELECT_TEXT)) {
                    query += QUERY_LIMIT_TEXT + PREVIEW_ROW_LIMIT;
                }
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
            } catch (final MalformedURLException | ClassNotFoundException | SQLException | NoSuchMethodException
                    | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException ex) {
                NotifyDisplayer.displayAlert("JDBC Import", "Query Error", ex.getMessage(), AlertType.ERROR);
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            }
        }

        if (configurationPane != null) {
            configurationPane.setSampleData(currentColumns, currentData);
        }
    }
}
