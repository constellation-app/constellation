/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.sql;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

/**
 *
 * @author twilight_sparkle A framework for SQL Queries used in data access
 * plugins that connect to SQL databases.
 */
public class SqlQuery {

    private final StringBuilder query = new StringBuilder();
    private final Connection connection;
    private int currentArgument = 0;
    private final Map<Integer, String> stringArguments = new HashMap<>();
    private final Map<Integer, Integer> integerArguments = new HashMap<>();
    private final Map<Integer, Double> doubleArguments = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger(SqlQuery.class.getName());

    // Constants representing different type of value comparisons that can be done
    // in an SqlQuery using = or LIKE.
    public enum MatchType {

        MATCH_EXACT,
        MATCH_BEGINS_WITH,
        MATCH_ENDS_WITH,
        MATCH_CONTAINS,;
    }

    // Constants representing different type of text matching that can be done
    // in an SqlQuery using MATCH AGAINST on a fulltext index.
    public enum TextMatchType {

        MATCH_EXACT,
        MATCH_PHRASE,
        MATCH_WORDS,;
    }

    public void addArgument(final String argument) {
        query.append("?");
        stringArguments.put(++currentArgument, argument);
    }

    public void addArgument(final int argument) {
        query.append("?");
        integerArguments.put(++currentArgument, argument);
    }

    public void addArgument(final double argument) {
        query.append("?");
        doubleArguments.put(++currentArgument, argument);
    }

    public SqlQuery(final Connection connection) {
        this.connection = connection;
    }

    public ResultSet executeQuery() throws SQLException {
        return execute(true);
    }

    public ResultSet executeStatement() throws SQLException {
        return execute(false);
    }

    private ResultSet execute(final boolean isQuery) throws SQLException {

        // append a semicolon to the query if necessary.
        String s = query.toString();
        if (s.charAt(s.length() - 1) != ';') {
            s = s + SeparatorConstants.SEMICOLON;
        }

        // prepare the statement and add the arguments of various types.
        try (final PreparedStatement statement = connection.prepareStatement(s)) {
            for (final Entry<Integer, String> arg : stringArguments.entrySet()) {
                statement.setString(arg.getKey(), arg.getValue());
            }
            for (final Entry<Integer, Integer> arg : integerArguments.entrySet()) {
                statement.setInt(arg.getKey(), arg.getValue());
            }
            for (final Entry<Integer, Double> arg : doubleArguments.entrySet()) {
                statement.setDouble(arg.getKey(), arg.getValue());
            }

            LOGGER.fine(statement.toString());

            // execute the query and return the results.
            if (isQuery) {
                return statement.executeQuery();
            } else {
                statement.execute();
                return null;
            }
        }
    }

    // Helper method to qualify an SQL column name with the name of the table it belongs to,
    // in order to avoid naming conflicts when performing joins.
    public static String qualifyColumnWithTableName(final String tableName, final String columnName, final boolean addAsClause) {
        final StringBuilder qualifiedName = new StringBuilder(tableName);
        qualifiedName.append(SeparatorConstants.PERIOD);
        qualifiedName.append(columnName);
        if (addAsClause) {
            qualifiedName.append(" AS ");
            qualifiedName.append(columnName);
        }
        return qualifiedName.toString();
    }

    // Helper method to qualify a list of SQL column names with the name of the table they belong to,
    // in order to avoid naming conflicts when performing joins.
    public static List<String> qualifyColumnsWithTableName(final String tableName, final List<String> columnNames, final boolean addAsClause) {
        final List<String> qualifiedNames = new ArrayList<>(columnNames.size());
        for (final String columnName : columnNames) {
            qualifiedNames.add(qualifyColumnWithTableName(tableName, columnName, addAsClause));
        }
        return qualifiedNames;
    }

    // Helper method for generating random names for temporary tables.
    public static String tempTableName() {
        return "TEMP" + SeparatorConstants.UNDERSCORE + UUID.randomUUID().toString().replaceAll(SeparatorConstants.HYPHEN, SeparatorConstants.UNDERSCORE);
    }

    // Helper method for creating single column tables and populating them with a list of values
    // for queries where the selection criteria involves comparing against this list of values (allows joins to be used
    // instead of a massive or).
    public static String createAndFillQueryTable(final Connection connection, final List<String> data, final String columnName, final String columnType) throws SQLException {
        final SqlQuery tableCreation = new SqlQuery(connection);
        final Map<String, String> columnDefintions = new HashMap<>();
        columnDefintions.put(columnName, columnType);
        final String tableName = SqlQuery.tempTableName();
        tableCreation.appendCreateTableCaluse(tableName, columnDefintions, false);
        tableCreation.executeStatement();
        final SqlQuery tablePopulation = new SqlQuery(connection);
        tablePopulation.appendInsertIntoSingleColumnCaluse(tableName, columnName, data);
        tablePopulation.executeStatement();
        return tableName;
    }

    // Helper method for dropping temporarily created tables
    public static void dropQueryTable(final Connection connection, final String tableName) throws SQLException {
        final SqlQuery tableDeletion = new SqlQuery(connection);
        tableDeletion.appendDropTableClause(tableName, false);
        tableDeletion.executeStatement();
    }

    // Used to append arbitrary sql to the query. Should only be used for non
    // user entered content otherwise it can not be sanitised.
    public void append(String queryText) {
        query.append(queryText);
    }

    // Used to append a create table clause to this sql query.
    // Note that the table name, column names and type definitions are assumed not to be user input and hence
    // are not sanitized.
    public void appendCreateTableCaluse(final String tableName, final Map<String, String> columnNamesAndTypes, final boolean isTemporary) {
        query.append(isTemporary ? "CREATE TEMPORARY TABLE " : "CREATE TABLE ")
                .append(tableName)
                .append("(");
        for (final Entry<String, String> columnDefinition : columnNamesAndTypes.entrySet()) {
            query.append(columnDefinition.getKey())
                    .append(" ")
                    .append(columnDefinition.getValue())
                    .append(",");
        }
        // remove the last comma
        query.replace(query.length() - 1, query.length(), "");
        query.append(")");
        query.append(" CHARACTER SET utf8 COLLATE utf8_unicode_ci");
    }

    // Used to append a drop table clause to this sql query.
    // Note that the table name is assumed not to be user input and hence
    // is not sanitized.
    public void appendDropTableClause(final String tableName, final boolean isTemporary) {
        query.append(isTemporary ? "DROP TEMPORARY TABLE " : "DROP TABLE ");
        query.append(tableName);
    }

    // Used to append a clause which inserts a list of data into a single named column to this sql query.
    // Note that the table name and column name are assumed not to be user input and hence
    // are not sanitized. The data being inserted is santized.
    public void appendInsertIntoSingleColumnCaluse(final String tableName, final String columnName, final List<String> data) {
        query.append("INSERT INTO ");
        query.append(tableName)
                .append("(")
                .append(columnName)
                .append(") VALUES");
        for (final String value : data) {
            query.append("(");
            addArgument(value);
            query.append("),");
        }
        // remove the last comma
        query.replace(query.length() - 1, query.length(), "");
    }

    // Used to append a select clause to this sql query.
    // Note that the column names are assumed not to be user input and hence
    // are not sanitized.
    public void appendSelectClause(final Iterable<String> selectionColumns, final boolean distinct) {
        query.append("SELECT ");
        if (distinct) {
            query.append("DISTINCT ");
        }
        // Add all the columns we want to query
        for (final String column : selectionColumns) {
            query.append(column);
            query.append(", ");
        }
        // remove the last comma
        query.replace(query.length() - 2, query.length(), " ");
    }

    // Used to append a from clause to this sql query.
    // Note that the table name is assumed not to be user input and hence is not
    // sanitized.
    public void appendFromClause(final String tableName) {
        query.append("FROM ")
                .append(tableName)
                .append(" ");
    }

    // Used to append an empty where clause to this sql query.
    // All selection criteria logic should be added manually or
    // by using other helper functions in this class.
    public void appendWhereClause() {
        query.append("WHERE ");
    }

    // Used to append an empty union clause to this sql query.
    // All query clauses for the query to union with should be added manually or
    // by using other helper functions in this class.
    public void appendUnionClause() {
        query.append("UNION ");
    }

    // Used to append a simple join clause to this sql query where two columns are joined on equality.
    // Note that the table and column names are assumed not to be user input and hence are not
    // sanitized.
    public void appendJoinOnEqualClause(final String joinTable, final String leftColumn, final String rightColumn) {
        query.append("JOIN ")
                .append(joinTable)
                .append(" ON ")
                .append(leftColumn)
                .append(" = ")
                .append(rightColumn)
                .append(" ");
    }

    // Used to append a logical disjunction clause to this sql query
    // where a specific column is compared for text matches using MATCH AGAINST on a list of specific values.
    // Note that the table and column name are assumed not to be user input and hence are not
    // sanitized. The values are sanitized.
    public void appendDisjunctiveTextMatchClause(final String columnName, final List<String> values, final TextMatchType matchType, final boolean lastClause) {
        if (matchType == TextMatchType.MATCH_EXACT) {
            appendDisjunctiveComparisonClause(columnName, values, MatchType.MATCH_EXACT, lastClause);
            return;
        }
        query.append("MATCH(")
                .append(columnName)
                .append(") AGAINST (");
        final StringBuilder matchCondition = new StringBuilder();
        for (final String value : values) {
            matchCondition.append("(");
            switch (matchType) {
                case MATCH_PHRASE -> matchCondition.append("\"")
                        .append(value)
                        .append("\"");
                case MATCH_WORDS -> {
                    final String[] words = value.split(" ");
                    for (final String word : words) {
                        matchCondition.append("+")
                                .append(word)
                                .append(" ");
                    }
                }
                default -> {
                    // do nothing
                }
            }
            matchCondition.append(")");
        }
        addArgument(matchCondition.toString());
        query.append(" IN BOOLEAN MODE) OR ");
        // Remove the last 'OR ' if necessary
        if (lastClause) {
            query.replace(query.length() - 3, query.length(), "");
        }
    }

    // Used to append a logical disjunction clause to this sql query
    // where a specific column is compared using = or LIKE to a list of specific values.
    // Note that the table and column name are assumed not to be user input and hence are not
    // sanitized. The values are sanitized.
    public void appendUnionComparisonClause(final String columnName, final List<? extends Object> values, final MatchType matchType) {
        final String currentQuery = query.toString();
        for (Object value : values) {
            query.append(columnName);
            query.append(matchType == MatchType.MATCH_EXACT ? " = " : " LIKE ");
            
            switch (matchType) {
                case MATCH_BEGINS_WITH -> value = value + "%";
                case MATCH_ENDS_WITH -> value = "%" + value;
                case MATCH_CONTAINS -> value = "%" + value + "%";
                default -> {
                    // do nothing
                }
            }
            switch (value) {
                case String stringValue -> addArgument(stringValue);
                case Integer integerValue -> addArgument(integerValue);
                case Double doubleValue -> addArgument(doubleValue);
                default -> {
                    // do nothing
                }
            }
            query.append(" UNION ");
            query.append(currentQuery);
        }
        // Remove the last 'UNION ...' if necessary
        query.replace(query.length() - currentQuery.length() - 6, query.length(), "");
    }

    // Used to append a logical disjunction clause to this sql query
    // where a specific column is compared using = or LIKE to a list of specific values.
    // Note that the table and column name are assumed not to be user input and hence are not
    // sanitized. The values are sanitized.
    public void appendDisjunctiveComparisonClause(final String columnName, final List<? extends Object> values, final MatchType matchType, final boolean lastClause) {
        for (Object value : values) {
            query.append(columnName);
            query.append(matchType == MatchType.MATCH_EXACT ? " = " : " LIKE ");
            
            switch (matchType) {
                case MATCH_BEGINS_WITH -> value = value + "%";
                case MATCH_ENDS_WITH -> value = "%" + value;
                case MATCH_CONTAINS -> value = "%" + value + "%";
                default -> {
                    // do nothing
                }
            }
            switch (value) {
                case String stringValue -> addArgument(stringValue);
                case Integer integerValue -> addArgument(integerValue);
                case Double doubleValue -> addArgument(doubleValue);
                default -> {
                    // do nothing
                }
            }
            query.append(" OR ");
        }
        // Remove the last 'OR ' if necessary
        if (lastClause) {
            query.replace(query.length() - 3, query.length(), "");
        }
    }

    // Used to append a simple order clause to this sql query
    // Note that the column name is assumed not to be user input and hence is not
    // sanitized.
    public void appendOrderClause(final String orderColumn, final boolean isDescending) {
        query.append("ORDER BY ");
        query.append(orderColumn);
        if (isDescending) {
            query.append(" DESC");
        }
    }
}
