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
package au.gov.asd.tac.constellation.plugins.importexport.delimited.parser;

import au.gov.asd.tac.constellation.plugins.importexport.delimited.RefreshRequest;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openide.util.lookup.ServiceProvider;

/**
 * A SQLLiteImportFileParser implements an ImportFileParser that can parse
 * SQLLite files.
 *
 * @author sirius
 */
@ServiceProvider(service = ImportFileParser.class)
public class SQLiteImportFileParser extends ImportFileParser {

    private static final String SELECT_QUERY = "SELECT * FROM ?";
    private static final String SELECT_QUERY_WITH_LIMIT = "SELECT * FROM ? LIMIT ?";

    public static final String TABLE_PARAMETER_ID = PluginParameter.buildId(SQLiteImportFileParser.class, "table");

    public SQLiteImportFileParser() {
        super("SQLite", 5);
    }

    @Override
    public PluginParameters getParameters(final RefreshRequest refreshRequest) {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> tableParameter = SingleChoiceParameterType.build(TABLE_PARAMETER_ID);
        tableParameter.setName("Table");
        tableParameter.setDescription("The SQLite table to import");
        SingleChoiceParameterType.setOptions(tableParameter, Collections.emptyList());
        params.addParameter(tableParameter);

        params.addController(TABLE_PARAMETER_ID, (PluginParameter<?> master, Map<String, PluginParameter<?>> parameters, ParameterChange change) -> {
            if (change == ParameterChange.VALUE) {
                refreshRequest.refresh();
            }
        });

        return params;
    }

    @Override
    public void updateParameters(final PluginParameters parameters, final List<InputSource> inputs) {
        final String currentChoice = parameters.getStringValue(TABLE_PARAMETER_ID);

        try {
            Class.forName("org.sqlite.JDBC");

            final Set<String> tableChoices = new TreeSet<>();

            for (InputSource inputSource : inputs) {
                try {
                    final File file = inputSource.getFile();
                    if (file != null) {
                        try (final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getCanonicalPath())) {
                            try (final PreparedStatement statement = connection.prepareStatement("SELECT name FROM sqlite_master where type = 'table'")) {
                                try (final ResultSet tables = statement.executeQuery()) {
                                    while (tables.next()) {
                                        tableChoices.add(tables.getString("name"));
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException | SQLException ex) {
                    ex.printStackTrace();
                }
            }
            @SuppressWarnings("unchecked") //Table Parameter is created as a SingleChoiceParameter in this class on line 62.
            PluginParameter<SingleChoiceParameterValue> tableSC = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(TABLE_PARAMETER_ID);
            SingleChoiceParameterType.setOptions((PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(TABLE_PARAMETER_ID), new ArrayList<>(tableChoices));

            if (currentChoice != null && tableChoices.contains(currentChoice)) {
                parameters.setStringValue(TABLE_PARAMETER_ID, currentChoice);
            } else {
                parameters.setStringValue(TABLE_PARAMETER_ID, null);
            }
            return;

        } catch (ClassNotFoundException ex) {

        }
        @SuppressWarnings("unchecked") //Table Parameter is created as a SingleChoiceParameter in this class on line 62.
        PluginParameter<SingleChoiceParameterValue> tableSC = (PluginParameter<SingleChoiceParameterValue>) parameters.getParameters().get(TABLE_PARAMETER_ID);
        SingleChoiceParameterType.setOptions(tableSC, Collections.emptyList());
    }

    @Override
    public List<String[]> parse(final InputSource input, final PluginParameters parameters) throws IOException {
        final String tableName = parameters.getParameters().get(TABLE_PARAMETER_ID).getStringValue();
        if (tableName != null) {
            return readTable(input, tableName, -1);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String[]> preview(final InputSource input, final PluginParameters parameters, final int limit) throws IOException {
        final String tableName = parameters.getParameters().get(TABLE_PARAMETER_ID).getStringValue();
        if (tableName != null) {
            return readTable(input, tableName, limit);
        } else {
            return Collections.emptyList();
        }
    }

    private static List<String[]> readTable(final InputSource input, final String tableName, final int limit) {
        final List<String[]> result = new ArrayList<>();

        try {
            Class.forName("org.sqlite.JDBC");

            final File file = input.getFile();
            if (file != null) {
                try (final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + file.getCanonicalPath())) {
                    try (final PreparedStatement statement = connection.prepareStatement(limit >= 0 ? SELECT_QUERY_WITH_LIMIT : SELECT_QUERY)) {
                        statement.setString(1, tableName);
                        if (limit >= 0) {
                            statement.setInt(2, limit);
                        }

                        try (final ResultSet resultSet = statement.executeQuery()) {
                            final ResultSetMetaData metaData = resultSet.getMetaData();
                            final List<String> columnNames = new ArrayList<>(metaData.getColumnCount());
                            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                                columnNames.add(metaData.getColumnLabel(i));
                            }
                            result.add(columnNames.toArray(new String[columnNames.size()]));

                            while (resultSet.next()) {
                                final String[] row = new String[columnNames.size()];
                                for (int i = 1; i <= row.length; i++) {
                                    row[i - 1] = resultSet.getString(i);
                                }
                                result.add(row);
                            }
                        }
                    }
                }
            }

        } catch (IOException | ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
