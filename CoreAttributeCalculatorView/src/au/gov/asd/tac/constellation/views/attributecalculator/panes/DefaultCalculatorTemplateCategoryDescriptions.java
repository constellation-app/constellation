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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author twilight_sparkle
 */
public class DefaultCalculatorTemplateCategoryDescriptions extends AbstractCalculatorTemplateCategoryDescriptions {

    private static final Map<String, String[]> TEMPLATE_DESCRIPTIONS = new HashMap<>();
    private static final Map<String, String[]> TEMPLATE_USAGE_EXAMPLES = new HashMap<>();

    private static final String COMPLETE_SCRIPTS = "Complete Scripts";
    private static final String[] COMPLETE_SCRIPTS_DESCRIPTION = {
        "Entire scripts that will perform useful functions. You can also save and load your own scripts to this list."
    };

    private static final String NODE_ATTRIBUTES = "Node Attributes";
    private static final String[] NODE_ATTRIBUTES_DESCRIPTION = {
        "These will give the value that each node has for the attribute."
    };

    private static final String TRANSACTION_ATTRIBUTES = "Transaction Attributes";
    private static final String[] TRANSACTION_ATTRIBUTES_DESCRIPTION = {
        "These will give the value that each transaction has for the attribute."
    };

    private static final String SOURCE_NODE_ATTRIBUTES = "Source Node Attributes";
    private static final String[] SOURCE_NODE_ATTRIBUTES_DESCRIPTION = {
        "These will give the value that the source node of each transaction has for the attribute."
    };

    private static final String DEST_NODE_ATTRIBUTES = "Destination Node Attributes";
    private static final String[] DEST_NODE_ATTRIBUTES_DESCRIPTION = {
        "These will give the value that the destination node of each transaction has for the attribute."
    };

    private static final String GRAPH_PROPERTIES = "Graph Properties";
    private static final String[] GRAPH_PROPERTIES_DESCRIPTION = {
        "Properties of the graph as a whole, such as the number of nodes and transactions. These remain constant."
    };

    private static final String NODE_PROPERTIES = "Node Properties";
    private static final String[] NODE_PROPERTIES_DESCRIPTION = {
        "Structural properties of nodes, such as the number of neighbours.",
        "These will give the value that each node has for the property."
    };

    private static final String SOURCE_NODE_PROPERTIES = "Source Node Properties";
    private static final String[] SOURCE_NODE_PROPERTIES_DESCRIPTION = {
        "Structural properties of nodes, such as the number of neighbours.",
        "These will give the value that the source node of each transaction has for the property."
    };

    private static final String DEST_NODE_PROPERTIES = "Destination Node Properties";
    private static final String[] DEST_NODE_PROPERTIES_DESCRIPTION = {
        "Structural properties of nodes, such as the number of neighbours.",
        "These will give the value that the destination node of each transaction has for the property."
    };

    private static final String TRANSACTION_PROPERTIES = "Transaction/Edge/Link Properties";
    private static final String[] TRANSACTION_PROPERTIES_DESCRIPTION = {
        "Structural properties of transactions, edges, and links.",
        "These will give the value that each transaction, edge, or link (depending on which the property applies to) has for the property."
    };

    private static final String GRAPH_ANALYSIS = "Graph Analysis";
    private static final String[] GRAPH_ANALYSIS_DESCRIPTION
            = {
                "Functions which perform a calculation for nodes or transactions across the entire graph.",
                "Attributes and properties used inside these functions will be evaluated with respect to each node/transaction in the graph",};

    private static final String NODE_NEIGHBOUR_ANALYSIS = "Node Neighbour Analysis";
    private static final String[] NODE_NEIGHBOUR_ANALYSIS_DESCRIPTION
            = {
                "Functions which perform a calculation for each neighbour of a node.",
                "Attributes and properties used inside these functions will be evaluated with respect to the current node's neighbours.",};

    private static final String NODE_LINK_ANALYSIS = "Node Link Analysis";
    private static final String[] NODE_LINK_ANALYSIS_DESCRIPTION
            = {
                "Functions which perform a calculation for each transaction, edge, or link, incident with a node.",
                "Attributes and properties used inside these functions will be evaluated with respect to the transactions/edges/links incident with the current node. Hence there is a switch from node to transaction context inside this function.",};

    private static final String TRANSACTION_ANALYSIS = "Transaction Analysis";
    private static final String[] TRANSACTION_ANALYSIS_DESCRIPTION
            = {
                "Functions which perform a calculation for each transaction related in a certain way to another link/edge/transaction",
                "Attributes and properties used inside these functions will be evaluated with respect to the transactions related to the current link/edge/transaction.",};

    private static final String LOGICAL_OPERATORS = "Logical Operators";
    private static final String[] LOGICAL_OPERATORS_DESCRIPTION = {
        "Python logical operators which allow for complex expressions to be built using boolean (True/False) attributes and expressions."
    };

    private static final String COMPARISON_OPERATORS = "Comparison Operators";
    private static final String[] COMPARISON_OPERATORS_DESCRIPTION = {
        "Python comparison operators which compare two expressions and return a boolean (True/False) value.",
        "These are particularly useful for comparing attributes and graph properties to specific values."
    };

    private static final String ARITHMETIC_OPERATORS = "Arithmetic Operators";
    private static final String[] ARITHMETIC_OPERATORS_DESCRIPTION = {
        "Basic arithmetic operations like add and subtract.",
        "These are primarily used to perform arithmetic with numerical attributes and graph properties."
    };

    private static final String NUMERICAL_FUNCTIONS = "Numerical Functions";
    private static final String[] NUMERICAL_FUNCTIONS_DESCRIPTION = {
        "Python numerical functions which operate on numbers and lists of numbers.",
        "These provide more advanced functionality than the basic arithmetic operators."
    };

    private static final String STRING_METHODS = "String Methods";
    private static final String[] STRING_METHODS_DESCRIPTION = {
        "Python string methods.",
        "These functions are used to perform an operation on a string, such as convert it to lower case, or determine whether it starts with a given substring."
    };

    private static final String LIST_METHODS = "List Methods";
    private static final String[] LIST_METHODS_DESCRIPTIONS = {
        "Python list methods.",
        "These functions are used to perform an operation on a list, such as get a sublist, or count the number of occurrences of an element in the list."
    };

    private static final String TYPE_CONVERSIONS = "Type Conversion";
    private static final String[] TYPE_CONVERSIONS_DESCRIPTION = {
        "Python type conversions.",
        "These functions are used to convert the type of an expression, for example converting a decimal calculation to an integer so it can be stored in an integer attribute."
    };

    private static final String DATETIME_PROCESSING = "DateTime Processing";
    private static final String[] DATETIME_PROCESSING_DESCRIPTION = {
        "Functions to process Date, Time and DateTime attributes.",
        "These include functions to extract the year, month, weekday etc. from a temporal attribute. Also included are functions to calculate the numbe of hours, minutes, seconds etc. in a duration, that is the difference between two temporal attributes."
    };

    private static final String[] COMPLETE_SCRIPTS_EXAMPLES = {};
    private static final String[] NODE_ATTRIBUTES_EXAMPLES = {};
    private static final String[] TRANSACTION_ATTRIBUTES_EXAMPLES = {};
    private static final String[] SOURCE_NODE_ATTRIBUTES_EXAMPLES = {};
    private static final String[] DEST_NODE_ATTRIBUTES_EXAMPLES = {};
    private static final String[] GRAPH_PROPERTIES_EXAMPLES = {};
    private static final String[] NODE_PROPERTIES_EXAMPLES = {};
    private static final String[] SOURCE_NODE_PROPERTIES_EXAMPLES = {};
    private static final String[] DEST_NODE_PROPERTIES_EXAMPLES = {};
    private static final String[] TRANSACTION_PROPERTIES_EXAMPLES = {};
    private static final String[] GRAPH_ANALYSIS_EXAMPLES = {};
    private static final String[] NODE_NEIGHBOUR_ANALYSIS_EXAMPLES = {};
    private static final String[] NODE_LINK_ANALYSIS_EXAMPLES = {};
    private static final String[] TRANSACTION_ANALYSIS_EXAMPLES = {};
    private static final String[] LOGICAL_OPERATORS_EXAMPLES = {};
    private static final String[] COMPARISON_OPERATORS_EXAMPLES = {};
    private static final String[] ARITHMETIC_OPERATORS_EXAMPLES = {};
    private static final String[] NUMERICAL_FUNCTIONS_EXAMPLES = {};
    private static final String[] STRING_METHODS_EXAMPLES = {};
    private static final String[] LIST_METHODS_EXAMPLESS = {};
    private static final String[] TYPE_CONVERSIONS_EXAMPLES = {};
    private static final String[] DATETIME_PROCESSING_EXAMPLES = {};

    static {
        TEMPLATE_DESCRIPTIONS.put(COMPLETE_SCRIPTS, COMPLETE_SCRIPTS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(NODE_ATTRIBUTES, NODE_ATTRIBUTES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(TRANSACTION_ATTRIBUTES, TRANSACTION_ATTRIBUTES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(SOURCE_NODE_ATTRIBUTES, SOURCE_NODE_ATTRIBUTES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(DEST_NODE_ATTRIBUTES, DEST_NODE_ATTRIBUTES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(GRAPH_PROPERTIES, GRAPH_PROPERTIES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(NODE_PROPERTIES, NODE_PROPERTIES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(SOURCE_NODE_PROPERTIES, SOURCE_NODE_PROPERTIES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(DEST_NODE_PROPERTIES, DEST_NODE_PROPERTIES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(TRANSACTION_PROPERTIES, TRANSACTION_PROPERTIES_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(GRAPH_ANALYSIS, GRAPH_ANALYSIS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(NODE_NEIGHBOUR_ANALYSIS, NODE_NEIGHBOUR_ANALYSIS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(NODE_LINK_ANALYSIS, NODE_LINK_ANALYSIS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(TRANSACTION_ANALYSIS, TRANSACTION_ANALYSIS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(LOGICAL_OPERATORS, LOGICAL_OPERATORS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(COMPARISON_OPERATORS, COMPARISON_OPERATORS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(ARITHMETIC_OPERATORS, ARITHMETIC_OPERATORS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(NUMERICAL_FUNCTIONS, NUMERICAL_FUNCTIONS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(STRING_METHODS, STRING_METHODS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(LIST_METHODS, LIST_METHODS_DESCRIPTIONS);
        TEMPLATE_DESCRIPTIONS.put(TYPE_CONVERSIONS, TYPE_CONVERSIONS_DESCRIPTION);
        TEMPLATE_DESCRIPTIONS.put(DATETIME_PROCESSING, DATETIME_PROCESSING_DESCRIPTION);
        TEMPLATE_USAGE_EXAMPLES.put(COMPLETE_SCRIPTS, COMPLETE_SCRIPTS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(NODE_ATTRIBUTES, NODE_ATTRIBUTES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(TRANSACTION_ATTRIBUTES, TRANSACTION_ATTRIBUTES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(SOURCE_NODE_ATTRIBUTES, SOURCE_NODE_ATTRIBUTES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(DEST_NODE_ATTRIBUTES, DEST_NODE_ATTRIBUTES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(GRAPH_PROPERTIES, GRAPH_PROPERTIES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(NODE_PROPERTIES, NODE_PROPERTIES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(SOURCE_NODE_PROPERTIES, SOURCE_NODE_PROPERTIES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(DEST_NODE_PROPERTIES, DEST_NODE_PROPERTIES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(TRANSACTION_PROPERTIES, TRANSACTION_PROPERTIES_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(GRAPH_ANALYSIS, GRAPH_ANALYSIS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(NODE_NEIGHBOUR_ANALYSIS, NODE_NEIGHBOUR_ANALYSIS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(NODE_LINK_ANALYSIS, NODE_LINK_ANALYSIS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(TRANSACTION_ANALYSIS, TRANSACTION_ANALYSIS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(LOGICAL_OPERATORS, LOGICAL_OPERATORS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(COMPARISON_OPERATORS, COMPARISON_OPERATORS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(ARITHMETIC_OPERATORS, ARITHMETIC_OPERATORS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(NUMERICAL_FUNCTIONS, NUMERICAL_FUNCTIONS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(STRING_METHODS, STRING_METHODS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(LIST_METHODS, LIST_METHODS_EXAMPLESS);
        TEMPLATE_USAGE_EXAMPLES.put(TYPE_CONVERSIONS, TYPE_CONVERSIONS_EXAMPLES);
        TEMPLATE_USAGE_EXAMPLES.put(DATETIME_PROCESSING, DATETIME_PROCESSING_EXAMPLES);
    }

    public DefaultCalculatorTemplateCategoryDescriptions() {
    }

    @Override
    public String[] getUsageExamples(String key) {
        return TEMPLATE_USAGE_EXAMPLES.get(key);
    }

    @Override
    public String[] getDescriptions(String key) {
        return TEMPLATE_DESCRIPTIONS.get(key);
    }

}
