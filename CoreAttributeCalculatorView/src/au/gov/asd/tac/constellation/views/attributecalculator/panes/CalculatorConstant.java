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
package au.gov.asd.tac.constellation.views.attributecalculator.panes;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;

/**
 * A Number of 'constants' which can be inserted into the calculator's script
 * window and reference either AttributeCalculator utility functions, python
 * inbuilt functions, or python operators. Note that there is no nice way to
 * really automatically generate the list of utility functions, even using
 * reflection, as description strings still need to be written for each method
 * etc. While it would be theoretically possible to cook something up to do
 * this, as these constants are part of the user interface I think manual entry
 * is the better solution (even if slightly prone to mistake) as utility
 * functions can then be added, moved and removed from the insertion menu as
 * desired.
 *
 * @author twilight_sparkle
 */
public enum CalculatorConstant {

    HAS_NEIGHBOUR(GraphElementType.VERTEX, "has_neighbour(condition)", "Has Neighbour with Property", CalculatorTemplateDescription.HAS_NEIGHBOUR, "Node Neighbour Analysis"),
    COUNT_NEIGHBOURS(GraphElementType.VERTEX, "count_neighbours(condition)", "Count Neighbours with Property", CalculatorTemplateDescription.COUNT_NEIGHBOURS, "Node Neighbour Analysis"),
    FOR_NEIGHBOURS(GraphElementType.VERTEX, "for_neighbours(condition, calculation)", "For Neighbours", CalculatorTemplateDescription.FOR_NEIGHBOURS, "Node Neighbour Analysis"),
    //    FOR_NEIGHBOURS_PROPERTY(GraphElementType.VERTEX, "for_neighbours(condition, calculation)", "For Neighbours with Property", "Node Neighbour Analysis"),
    HAS_NODE_AT_DISTANCE(GraphElementType.VERTEX, "has_node_at_distance(condition, distance)", "Has Node at Distance with Property", CalculatorTemplateDescription.HAS_NODE_AT_DISTANCE, "Node Neighbour Analysis"),
    COUNT_NODES_AT_DISTANCE(GraphElementType.VERTEX, "count_nodes_at_distance(condition, distance)", "Count Nodes at Distance with Property", CalculatorTemplateDescription.COUNT_NODES_AT_DISTANCE, "Node Neighbour Analysis"),
    FOR_NODES_AT_DISTANCE(GraphElementType.VERTEX, "for_nodes_at_distance(condition, calculation, distance)", "For Nodes at Distance", CalculatorTemplateDescription.FOR_NODES_AT_DISTANCE, "Node Neighbour Analysis"),
    //    FOR_NODES_AT_DISTANCE_PROPERTY(GraphElementType.VERTEX, "for_nodes_at_distance(condition, calculation, distance)", "For Nodes at Distance with Property", "Node Neighbour Analysis"),
    GET_DISTANCES(GraphElementType.VERTEX, "get_node_distances(condition)", "Get Distances of Nodes with Property", CalculatorTemplateDescription.GET_DISTANCES, "Node Neighbour Analysis"),
    HAS_TRANSACTION(GraphElementType.VERTEX, "has_transaction(condition, node_condition)", "Has Transaction with Property", CalculatorTemplateDescription.HAS_TRANSACTION, "Node Link Analysis"),
    HAS_OUTGOING_TRANSACTION(GraphElementType.VERTEX, "has_outgoing_transaction(condition, node_condition)", "Has Outgoing Transaction with Property", CalculatorTemplateDescription.HAS_OUTGOING_TRANSACTION, "Node Link Analysis"),
    HAS_INCOMING_TRANSACTION(GraphElementType.VERTEX, "has_incoming_transaction(condition, node_condition)", "Has Incoming Transaction with Property", CalculatorTemplateDescription.HAS_INCOMING_TRANSACTION, "Node Link Analysis"),
    HAS_UNDIRECTED_TRANSACTION(GraphElementType.VERTEX, "has_undirected_transaction(condition, node_condition)", "Has Undirected Transaction with Property", CalculatorTemplateDescription.HAS_UNDIRECTED_TRANSACTION, "Node Link Analysis"),
    HAS_PARALLEL_TRANSACTION(GraphElementType.TRANSACTION, "has_parallel_transaction(condition)", "Has Parallel Transaction with Property", CalculatorTemplateDescription.HAS_PARALLEL_TRANSACTION, "Transaction Analysis"),
    HAS_EDGE(GraphElementType.VERTEX, "has_edge(condition, node_condition)", "Has Edge with Property", CalculatorTemplateDescription.HAS_EDGE, "Node Link Analysis"),
    HAS_LINK(GraphElementType.VERTEX, "has_link(condition, node_condition)", "Has Link with Property", CalculatorTemplateDescription.HAS_LINK, "Node Link Analysis"),
    COUNT_TRANSACTIONS(GraphElementType.VERTEX, "count_transactions(condition, node_condition)", "Count Transactions with Property", CalculatorTemplateDescription.COUNT_TRANSACTIONS, "Node Link Analysis"),
    COUNT_OUTGOING_TRANSACTIONS(GraphElementType.VERTEX, "count_outgoing_transactions(condition, node_condition)", "Count Outgoing Transactions with Property", CalculatorTemplateDescription.COUNT_OUTGOING_TRANSACTIONS, "Node Link Analysis"),
    COUNT_INCOMING_TRANSACTIONS(GraphElementType.VERTEX, "count_incoming_transactions(condition, node_condition)", "Count Incoming Transactions with Property", CalculatorTemplateDescription.COUNT_INCOMING_TRANSACTIONS, "Node Link Analysis"),
    COUNT_UNDIRECTED_TRANSACTIONS(GraphElementType.VERTEX, "count_undirected_transactions(condition, node_condition)", "Count Undirected Transactions with Property", CalculatorTemplateDescription.COUNT_UNDIRECTED_TRANSACTIONS, "Node Link Analysis"),
    COUNT_PARALLEL_TRANSACTIONS(GraphElementType.TRANSACTION, "count_parallel_transactions(condition)", "Count Parallel Transactions with Property", CalculatorTemplateDescription.COUNT_PARALLEL_TRANSACTIONS, "Transaction Analysis"),
    COUNT_EDGES(GraphElementType.VERTEX, "count_edges(condition, node_condition)", "Count Edges with Property", CalculatorTemplateDescription.COUNT_EDGES, "Node Link Analysis"),
    COUNT_LINKS(GraphElementType.VERTEX, "count_links(condition, node_condition)", "Count Links with Property", CalculatorTemplateDescription.COUNT_LINKS, "Node Link Analysis"),
    FOR_TRANSACTIONS(GraphElementType.VERTEX, "for_transactions(condition, node_condition, calculation)", "For Transactions", CalculatorTemplateDescription.FOR_TRANSACTIONS, "Node Link Analysis"),
    FOR_OUTGOING_TRANSACTIONS(GraphElementType.VERTEX, "for_outgoing_transactions(condition, node_condition, calculation)", "For Outgoing Transactions", CalculatorTemplateDescription.FOR_OUTGOING_TRANSACTIONS, "Node Link Analysis"),
    FOR_INCOMING_TRANSACTIONS(GraphElementType.VERTEX, "for_incoming_transactions(condition, node_condition, calculation)", "For Incoming Transactions", CalculatorTemplateDescription.FOR_INCOMING_TRANSACTIONS, "Node Link Analysis"),
    FOR_UNDIRECTED_TRANSACTIONS(GraphElementType.VERTEX, "for_undirected_transactions(condition, node_condition, calculation)", "For Undirected Transactions", CalculatorTemplateDescription.FOR_UNDIRECTED_TRANSACTIONS, "Node Link Analysis"),
    FOR_PARALLEL_TRANSACTIONS(GraphElementType.TRANSACTION, "for_parallel_transactions(condition, computation)", "For Parallel Transactions", CalculatorTemplateDescription.FOR_PARALLEL_TRANSACTIONS, "Transaction Analysis"),
    FOR_EDGES(GraphElementType.VERTEX, "for_edges(condition, node_condition, calculation)", "For Edges", CalculatorTemplateDescription.FOR_EDGES, "Node Link Analysis"),
    FOR_LINKS(GraphElementType.VERTEX, "for_links(condition, node_condition, calculation)", "For Links", CalculatorTemplateDescription.FOR_LINKS, "Node Link Analysis"),
    //    FOR_TRANSACTIONS_PROPERTY(GraphElementType.VERTEX, "for_transactions(condition, calculation)", "For Transactions with Property", "Node Link Analysis"),
    //    FOR_OUTGOING_TRANSACTIONS_PROPERTY(GraphElementType.VERTEX, "for_outgoing_transactions(condition, calculation)", "For Outgoing Transactions with Property", "Node Link Analysis"),
    //    FOR_INCOMING_TRANSACTIONS_PROPERTY(GraphElementType.VERTEX, "for_incoming_transactions(condition, calculation)", "For Incoming Transactions with Property", "Node Link Analysis"),
    //    FOR_UNDIRECTED_TRANSACTIONS_PROPERTY(GraphElementType.VERTEX, "for_undirected_transactions(condition, calculation)", "For Undirected Transactions with Property", "Node Link Analysis"),
    //    FOR_EDGES_PROPERTY(GraphElementType.VERTEX, "for_edges(condition, calculation)", "For Edges with Property", "Node Link Analysis"),
    //    FOR_LINKS_PROPERTY(GraphElementType.VERTEX, "for_links(condition, calculation)", "For Links with Property", "Node Link Analysis"),

    GRAPH_HAS_NODE(null, "graph_has_node(condition)", "Graph Has Node", CalculatorTemplateDescription.GRAPH_HAS_NODE, "Graph Analysis"),
    GRAPH_COUNT_NODES(null, "graph_count_nodes(condition)", "Count Nodes in Graph", CalculatorTemplateDescription.GRAPH_COUNT_NODES, "Graph Analysis"),
    GRAPH_FOR_NODES(null, "graph_for_nodes(condition, calculation)", "For Nodes in Graph", CalculatorTemplateDescription.GRAPH_FOR_NODES, "Graph Analysis"),
    GRAPH_HAS_TRANSACTION(null, "graph_has_transaction(condition)", "Graph Has Transaction", CalculatorTemplateDescription.GRAPH_HAS_TRANSACTION, "Graph Analysis"),
    GRAPH_COUNT_TRANSACTIONS(null, "graph_count_transactions(condition)", "Count Transactions in Graph", CalculatorTemplateDescription.GRAPH_COUNT_TRANSACTIONS, "Graph Analysis"),
    GRAPH_FOR_TRANSACTIONS(null, "graph_for_transactions(condition, caclulation)", "For Transactions in Graph", CalculatorTemplateDescription.GRAPH_FOR_TRANSACTIONS, "Graph Analysis"),
    OPERATOR_OR(null, "or", "or", CalculatorTemplateDescription.OPERATOR_OR, "Logical Operators"),
    OPERATOR_AND(null, "and", "and", CalculatorTemplateDescription.OPERATOR_AND, "Logical Operators"),
    OPERATOR_NOT(null, "not", "not", CalculatorTemplateDescription.OPERATOR_NOT, "Logical Operators"),
    OPERATOR_EQ(null, "==", "Equal (==)", CalculatorTemplateDescription.OPERATOR_EQ, "Comparison Operators"),
    OPERATOR_NEQ(null, "!=", "Not Equal (!=)", CalculatorTemplateDescription.OPERATOR_NEQ, "Comparison Operators"),
    OPERATOR_LESS(null, "<", "Less than (<)", CalculatorTemplateDescription.OPERATOR_LESS, "Comparison Operators"),
    OPERATOR_GREAT(null, ">", "Greater than (>)", CalculatorTemplateDescription.OPERATOR_GREAT, "Comparison Operators"),
    OPERATOR_LEQ(null, "<=", "Less than or Equal (<=)", CalculatorTemplateDescription.OPERATOR_LEQ, "Comparison Operators"),
    OPERATOR_GEQ(null, ">=", "Greater than or Equal (>=)", CalculatorTemplateDescription.OPERATOR_GEQ, "Comparison Operators"),
    OPERATOR_IN(null, "in", "Is Contained in (in)", CalculatorTemplateDescription.OPERATOR_IN, "Comparison Operators"),
    OPERATOR_PLUS(null, "+", "Add (+)", CalculatorTemplateDescription.OPERATOR_PLUS, "Arithmetic Operators"),
    OPERATOR_SUB(null, SeparatorConstants.HYPHEN, "Subtract (-)", CalculatorTemplateDescription.OPERATOR_SUB, "Arithmetic Operators"),
    OPERATOR_MUL(null, "*", "Multiply (*)", CalculatorTemplateDescription.OPERATOR_MUL, "Arithmetic Operators"),
    OPERATOR_DIV(null, "/", "Divide (/)", CalculatorTemplateDescription.OPERATOR_DIV, "Arithmetic Operators"),
    OPERATOR_POW(null, "**", "To the Power of (**)", CalculatorTemplateDescription.OPERATOR_POW, "Arithmetic Operators"),
    OPERATOR_MOD(null, "%", "Modulo (%)", CalculatorTemplateDescription.OPERATOR_MOD, "Arithmetic Operators"),
    FUNCTION_SUM(null, "sum(list_of_numbers)", "Sum", CalculatorTemplateDescription.FUNCTION_SUM, "Numerical Functions"),
    FUNCTION_MAX(null, "max(list_of_numbers)", "Maximum", CalculatorTemplateDescription.FUNCTION_MAX, "Numerical Functions"),
    FUNCTION_MIN(null, "min(list_of_numbers)", "Minimum", CalculatorTemplateDescription.FUNCTION_MIN, "Numerical Functions"),
    FUNCTION_MEDIAN(null, "median(list_of_numbers)", "Median", CalculatorTemplateDescription.FUNCTION_MEDIAN, "Numerical Functions"),
    FUNCTION_MEAN(null, "mean(list_of_numbers)", "Mean", CalculatorTemplateDescription.FUNCTION_MEAN, "Numerical Functions"),
    FUNCTION_MODE(null, "mode(list_of_numbers)", "Mode", CalculatorTemplateDescription.FUNCTION_MODE, "Numerical Functions"),
    FUNCTION_LOG(null, "log(number)", "Natural Logarithm", CalculatorTemplateDescription.FUNCTION_LOG, "Numerical Functions"),
    FUNCTION_SQRT(null, "sqrt(number)", "Square Root", CalculatorTemplateDescription.FUNCTION_SQRT, "Numerical Functions"),
    FUNCTION_ABS(null, "abs(number)", "Absolute Value", CalculatorTemplateDescription.FUNCTION_ABS, "Numerical Functions"),
    FUCNTION_LOWER(null, ".lower()", "To Lower Case", CalculatorTemplateDescription.FUNCTION_LOWER, "String Methods"),
    FUCNTION_UPPER(null, ".upper()", "To Upper Case", CalculatorTemplateDescription.FUNCTION_UPPER, "String Methods"),
    FUCNTION_STARTSWITH(null, ".startswith(prefix)", "Starts with Prefix", CalculatorTemplateDescription.FUNCTION_STARTSWITH, "String Methods"),
    FUCNTION_ENDSWITH(null, ".endswith(suffix)", "Ends with Suffix", CalculatorTemplateDescription.FUNCTION_ENDSWITH, "String Methods"),
    FUCNTION_SPLIT(null, ".split(substring)", "Split on Substring", CalculatorTemplateDescription.FUNCTION_SPLIT, "String Methods"),
    FUNCTION_SUBSTRING(null, "[index_start:index_end]", "Get Substring", CalculatorTemplateDescription.FUNCTION_SUBSTRING, "String Methods"),
    FUNCTION_STRLEN(null, "len(string)", "Length of String", CalculatorTemplateDescription.FUNCTION_STRLEN, "String Methods"),
    FUNCTION_LISTELEMENT(null, "[index]", "Get Element", CalculatorTemplateDescription.FUNCTION_LISTELEMENT, "List Methods"),
    FUNCTION_SUBLIST(null, "[index_start:index_end]", "Get Sublist", CalculatorTemplateDescription.FUNCTION_SUBLIST, "List Methods"),
    FUNCTION_COUNT(null, ".count(element)", "Count Element", CalculatorTemplateDescription.FUNCTION_COUNT, "List Methods"),
    FUNCTION_LISTINDEX(null, ".index(element)", "Get Index", CalculatorTemplateDescription.FUNCTION_LISTINDEX, "List Methods"),
    FUNCTION_LEN(null, "len(some_list)", "Length of List", CalculatorTemplateDescription.FUNCTION_LISTLEN, "List Methods"),
    FUNCTION_ALL(null, "all(some_list)", "Is Every Element True?", CalculatorTemplateDescription.FUNCTION_ALL, "List Methods"),
    FUNCTION_ANY(null, "any(some_list)", "Is Any Element True?", CalculatorTemplateDescription.FUNCTION_ANY, "List Methods"),
    FUNCTION_SORT(null, "sorted(some_list)", "Sort a List", CalculatorTemplateDescription.FUNCTION_SORT, "List Methods"),
    FUNCTION_DEFINEDVALUES(null, "defined_values(some_list)", "Get Defined Values (No Nulls)", CalculatorTemplateDescription.FUNCTION_DEFINEDVALUES, "List Methods"),
    FUNCTION_MAP(null, "map(function, some_list)", "Apply Function to a List", CalculatorTemplateDescription.FUNCTION_MAP, "List Methods"),
    FUNCTION_BOOL(null, "bool(value)", "Convert to Boolean (True/False)", CalculatorTemplateDescription.FUNCTION_BOOL, "Type Conversion"),
    FUNCTION_INT(null, "int(value)", "Convert to Integer", CalculatorTemplateDescription.FUNCTION_INT, "Type Conversion"),
    FUNCTION_FLOAT(null, "float(value)", "Convert to Float (Decimal Number)", CalculatorTemplateDescription.FUNCTION_FLOAT, "Type Conversion"),
    FUNCTION_STR(null, "str(value)", "Convert to String", CalculatorTemplateDescription.FUNCTION_STRING, "Type Conversion"),
    FUNCTION_LIST(null, "list(collection)", "Convert to List", CalculatorTemplateDescription.FUNCTION_LIST, "Type Conversion"),
    FUNCTION_SET(null, "set(collection)", "Convert to Set", CalculatorTemplateDescription.FUNCTION_SET, "Type Conversion"),
    FUNCTION_TIME_YEAR(null, "year(datetime)", "Get Year", CalculatorTemplateDescription.FUNCTION_TIME_YEAR, "DateTime Processing"),
    FUNCTION_TIME_MONTH(null, "month(datetime)", "Get Month", CalculatorTemplateDescription.FUNCTION_TIME_MONTH, "DateTime Processing"),
    FUNCTION_TIME_DAY(null, "day(datetime)", "Get Day of Month", CalculatorTemplateDescription.FUNCTION_TIME_DAY, "DateTime Processing"),
    FUNCTION_TIME_HOUR(null, "hour(datetime)", "Get Hour", CalculatorTemplateDescription.FUNCTION_TIME_HOUR, "DateTime Processing"),
    FUNCTION_TIME_MIN(null, "minute(datetime)", "Get Minute", CalculatorTemplateDescription.FUNCTION_TIME_MIN, "DateTime Processing"),
    FUNCTION_TIME_SEC(null, "second(datetime)", "Get Second", CalculatorTemplateDescription.FUNCTION_TIME_SEC, "DateTime Processing"),
    FUNCTION_TIME_WEEKDAY(null, "weekday(datetime)", "Get Weekday", CalculatorTemplateDescription.FUNCTION_TIME_WEEKDAY, "DateTime Processing"),
    FUNCTION_TIME_MONTH_NAME(null, "month_name(datetime)", "Get Month Name", CalculatorTemplateDescription.FUNCTION_TIME_MONTH_NAME, "DateTime Processing"),
    FUNCTION_TIME_WEEKDAY_NAME(null, "weekday_name(datetime)", "Get Weekday Name", CalculatorTemplateDescription.FUNCTION_TIME_WEEKDAY_NAME, "DateTime Processing"),
    FUNCTION_TIME_FROM_DATE(null, "time_from_date(datetime)", "Get Time", CalculatorTemplateDescription.FUNCTION_TIME_FROM_DATE, "DateTime Processing"),
    FUNCTION_TIME_STRING_FROM_DATE(null, "time_string_from_date(datetime)", "Get Time as String", CalculatorTemplateDescription.FUNCTION_TIME_STRING_FROM_DATE, "DateTime Processing"),
    FUNCTION_TIME_DATE_AS_STRING(null, "date_as_string(datetime)", "Get Date as String", CalculatorTemplateDescription.FUNCTION_TIME_DATE_AS_STRING, "DateTime Processing"),
    FUNCTION_TIME_NOW(null, "now()", "Current DateTime", CalculatorTemplateDescription.FUNCTION_TIME_NOW, "DateTime Processing"),
    FUNCTION_TIME_DAYS(null, "days(time_difference)", "Number of Days", CalculatorTemplateDescription.FUNCTION_TIME_DAYS, "DateTime Processing"),
    FUNCTION_TIME_HOURS(null, "hours(time_difference)", "Number of Hours", CalculatorTemplateDescription.FUNCTION_TIME_HOURS, "DateTime Processing"),
    FUNCTION_TIME_MINS(null, "minutes(time_difference)", "Number of Minutes", CalculatorTemplateDescription.FUNCTION_TIME_MINUTES, "DateTime Processing"),
    FUNCTION_TIME_SECS(null, "seconds(time_difference)", "Number of Seconds", CalculatorTemplateDescription.FUNCTION_TIME_SECONDS, "DateTime Processing"),;

    private final GraphElementType elementType;
    private final CalculatorTemplateDescription description;
    private final String constantValue;
    private final String constantLabel;
    private final String[] directory;
    private final int[] selectionIndices;

    private static int[] getDefaultSelectionIndex(String name) {
        if (!name.contains("(")) {
            final int[] selIndices = {name.length()};
            return selIndices;
        } else {
            final int startIndex = name.indexOf('(') + 1;
            final int endIndex = name.contains(",") ? name.indexOf(',') : name.indexOf(')');
            // If the function takes no parameters, don't select anything just move the cursor to the end of the inserted text
            if (startIndex == endIndex) {
                final int[] selIndeices = {name.length()};
                return selIndeices;
            } else {
                final int[] selIndices = {startIndex, endIndex};
                return selIndices;
            }
        }
    }

    private CalculatorConstant(GraphElementType elementType, String variableName, String variableLabel, CalculatorTemplateDescription description, String... directory) {
        this(elementType, variableName, variableLabel, description, getDefaultSelectionIndex(variableName), directory);
    }

    private CalculatorConstant(GraphElementType elementType, String variableName, String variableLabel, CalculatorTemplateDescription description, int[] selectionIndices, String... directory) {
        this.elementType = elementType;
        this.constantValue = variableName;
        this.constantLabel = variableLabel;
        this.description = description;
        this.selectionIndices = selectionIndices;
        this.directory = directory;
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public String getConstantLabel() {
        return constantLabel;
    }

    public CalculatorTemplateDescription getDescription() {
        return description;
    }

    public String[] getDirectory() {
        return directory;
    }

    public int[] getSelectionIndices() {
        return selectionIndices;
    }

    public String getDirectoryString() {
        StringBuilder directoryString = new StringBuilder();
        for (String s : directory) {
            directoryString.append(s);
        }
        return directoryString.toString();
    }

}
