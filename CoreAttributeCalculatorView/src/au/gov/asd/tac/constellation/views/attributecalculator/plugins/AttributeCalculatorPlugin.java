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
package au.gov.asd.tac.constellation.views.attributecalculator.plugins;

import au.gov.asd.tac.constellation.views.attributecalculator.utilities.AbstractCalculatorValue;
import au.gov.asd.tac.constellation.views.attributecalculator.utilities.CalculatorContextManager;
import au.gov.asd.tac.constellation.views.attributecalculator.utilities.AbstractCalculatorUtilities;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

/**
 *
 * @author sirius
 */
public final class AttributeCalculatorPlugin extends SimpleEditPlugin {

    // Script engine will have access to all symbols in these meodules directly these modules, that is "from <module> import *" will be evaluated.
    private static final String[] PYTHON_MODULES_TO_IMPORT_ALL_SYMBOLS_FROM = {"math"};
    // Script engine will have access to these modules, that is "import <module>" will be evaluated.
    private static final String[] PYTHON_MODULES_TO_IMPORT = {"ast"};

    private final GraphElementType elementType;
    private final String editAttribute;
    private final String editAttributeType;
    private final String language;
    private String script;
    private final boolean selectedOnly;
    private final boolean completeWithSchema;

    private static final Logger LOGGER = Logger.getLogger(AttributeCalculatorPlugin.class.getName());

    public AttributeCalculatorPlugin(final GraphElementType elementType, final String editAttribute, final String editAttributeType, final String language, final String script, boolean selectedOnly, boolean completeWithSchema) {
        this.elementType = elementType;
        this.editAttribute = editAttribute;
        this.editAttributeType = editAttributeType;
        this.language = language;
        this.script = script;
        this.selectedOnly = selectedOnly;
        this.completeWithSchema = completeWithSchema;
    }

    @Override
    public String getName() {
        return "Attribute Calculator";
    }

    private static void importModules(ScriptEngine engine) throws ScriptException {

        final String moduleImport = "import ";
        final String allImportStart = "from ";
        final String allImportEnd = " import *";

        for (String module : PYTHON_MODULES_TO_IMPORT) {
            String importScript = moduleImport + module;
            ((Compilable) engine).compile(importScript).eval();
        }

        for (String module : PYTHON_MODULES_TO_IMPORT_ALL_SYMBOLS_FROM) {
            String importScript = allImportStart + module + allImportEnd;
            ((Compilable) engine).compile(importScript).eval();
        }

        Reader r = null;
        try {
            InputStream f = AttributeCalculatorPlugin.class.getResource("resources/obliterator.py").openStream();
            LOGGER.log(Level.INFO, "input stream={0}", f);
            r = new InputStreamReader(f, StandardCharsets.UTF_8.name());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ((Compilable) engine).compile(r).eval();

        AbstractCalculatorValue.the_obliterator = ((PyObject) ((Compilable) engine).compile("Obliterator()").eval());

    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByMimeType(language);
        final Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        final CalculatorContextManager calculatorContext = new CalculatorContextManager(graph, elementType);

        final Map<Integer, Object> editedAttributeValues = new HashMap<>();

        preprocessScriptAndBindObjects(graph, bindings, calculatorContext);
        LOGGER.log(Level.INFO, "processedScript::{0}", script);
        int editAttributeId = graph.getAttribute(elementType, editAttribute);
        if (editAttributeId == Graph.NOT_FOUND) {
            editAttributeId = graph.addAttribute(elementType, editAttributeType, editAttribute, "", null, null);
        }

        int selectedAttr = selectedOnly ? graph.getAttribute(elementType, "selected") : Graph.NOT_FOUND;

        try {

            // Import any desired modules before trying to do anything with the script.
            importModules(engine);

            CompiledScript compiledScript;
            CompiledScript functionWrapperScript = null;
            final CompiledScript testExpressionScript = ((Compilable) engine).compile("ast.parse(" + __builtin__.repr(new PyString(script)) + ", mode='eval')"); // shiiii, such a ridic line of code
            try {
                testExpressionScript.eval();
                compiledScript = ((Compilable) engine).compile(script);
            } catch (ScriptException e) {
                final String functionWrappedScript = "def __script__():\n " + script.replace("\n", "\n ") + "\n";
                LOGGER.log(Level.INFO, "processedScript::{0}", functionWrappedScript);
                compiledScript = ((Compilable) engine).compile(functionWrappedScript);
                functionWrapperScript = ((Compilable) engine).compile("__script__()");
            }

            final int elementCount = elementType == GraphElementType.VERTEX ? graph.getVertexCount() : graph.getTransactionCount();

            // Compute the values for the desired attribute
            for (int i = 0; i < elementCount; i++) {
                final int elementId = elementType == GraphElementType.VERTEX ? graph.getVertex(i) : graph.getTransaction(i);
                if (selectedAttr == Graph.NOT_FOUND || graph.getBooleanValue(selectedAttr, elementId)) {
                    calculatorContext.enter(elementId);
                    Object result = compiledScript.eval();
                    if (functionWrapperScript != null) {
                        result = functionWrapperScript.eval();
                    }
                    if (result == AbstractCalculatorValue.the_obliterator) {
                        result = null;
                    }
                    editedAttributeValues.put(elementId, result);
                    calculatorContext.exit();
                }
            }

            // Edit the actual attribute values for the desired attribute
            for (int id : editedAttributeValues.keySet()) {
                graph.setObjectValue(editAttributeId, id, editedAttributeValues.get(id));
                if (!completeWithSchema) {
                    // do nothing
                } else if (elementType == GraphElementType.VERTEX) {
                    if (graph.getSchema() != null) {
                        graph.getSchema().completeVertex(graph, id);
                    }
                } else {
                    if (graph.getSchema() != null) {
                        graph.getSchema().completeTransaction(graph, id);
                    }
                }
            }

        } catch (ScriptException ex) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Attribute Calculator Error: " + ex.getMessage());
        }
    }

    // This is the world's dodgiest method. It preprocesses the python script and converts the lists of method names
    // provided by calculator utility classes from "method_name(query)" to "utility_object_name.method_name(lambda : query)"
    // This is in essence parsing and translating a very simple "domain specific language" for the attribute calculator
    // into python calls to the relevant java utility methods.
    private void preprocessScriptAndBindObjects(GraphReadMethods graph, Bindings bindings, CalculatorContextManager calculatorContext) {

        // Get the list of utilities for the attribute calculator, set their context manager to be this attribute calculator's context, and bind the objects appropriately
        Collection<AbstractCalculatorUtilities> utilities = AbstractCalculatorUtilities.getAllUtilities();
        for (AbstractCalculatorUtilities utility : utilities) {
            utility.setContextManager(calculatorContext);
            bindings.put(utility.getScriptingName(), utility);
        }

        // Process utility method calls
        for (AbstractCalculatorUtilities utility : utilities) {

            bindings.put(utility.getScriptingName(), utility);
            final String utilityScriptName = utility.getScriptingName();

            for (String methodName : utility.getUtilityMethodNames()) {

                final String adjustedMethodCall = utilityScriptName + SeparatorConstants.PERIOD + methodName;
                // Match any non-word character or the start of the script, followed by the method name, followed
                // by any amount of space or tabs, and finally a left parenthesis (which is not part of the match since we need this parenthesis to match the non-word character of a succeeding nested method call).
                final String pattern = "(\\A|\\W)(" + methodName + "[ ]*)(?=\\()";
                // Construct a matcher for the given pattern and script
                Matcher m = Pattern.compile(pattern).matcher(script);
                // Replace all matches, noting that we need to leave the first matched character (start of script or any non-word character) and hence can't use the default replaceAll method.
                StringBuffer result = new StringBuffer();
                SortedSet<Integer> methodEndPositions = new TreeSet<>();
                while (m.find()) {
                    if (!insidePythonString(script, m.start(2))) {
                        m.appendReplacement(result, m.group(1) + adjustedMethodCall);
                        methodEndPositions.add(result.length() + 1); //Note the method end position is one more than the result string buffer as the parenthesis which follows the method name has not been added to the string buffer yet.
                    }
                }
                m.appendTail(result);
                script = result.toString();

                int addedChars = 0;
                for (int endPos : methodEndPositions) {

                    // Find all the positions of the arguments to the given function
                    List<Integer> argumentPositions = new ArrayList<>();
                    int argumentPos = endPos + addedChars;
                    while (argumentPos >= 0) {
                        argumentPositions.add(argumentPos);
                        argumentPos = findNextPythonArgumentStart(script, argumentPos);
                    }

//                    LOGGER.info(""+methodName+"::"+endPos+"::"+argumentPositions.size());
                    // Ask the utility which arguments are meant to be python functions
                    Set<Integer> functionalArgumentIndices = utility.getFunctionalArgumentIndices(methodName, argumentPositions.size());

                    // Prepend the functional arguments with a lambda
                    final String functionalArgumentPrefix = "lambda:";
                    int argAddedChars = 0;
                    for (int i = 0; i < argumentPositions.size(); i++) {
                        if (functionalArgumentIndices.contains(i)) {
                            script = script.substring(0, argumentPositions.get(i) + argAddedChars) + functionalArgumentPrefix + script.substring(argumentPositions.get(i) + argAddedChars, script.length());
                            argAddedChars += functionalArgumentPrefix.length();
                        }
                    }
                    addedChars += argAddedChars;
                }
            }
        }

//        int attributeCount = graph.getAttributeCount(elementType);
        final String attributeUsageSuffix = ".val()";
        final String[] transactionPrefixes = {""};
        final String[] vertexPrefixes = {"source_", "dest_", ""};
        final Set<String> transactionContextOnlyPrefices = new HashSet<>();
        transactionContextOnlyPrefices.add("source_");
        transactionContextOnlyPrefices.add("dest_");
//        final String[] prefixes = elementType == GraphElementType.VERTEX ? vertexPrefixes : transactionPrefixes;

        // Form a sorted set of attribute names, with longer strings occuring first. This prevents attribute names which are substrings of other attribute names from matching when the attribute with a larger name should match first
        SortedSet<String> attributeNames = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() == o2.length() ? o1.compareTo(o2) : Integer.compare(o2.length(), o1.length());
            }
        });
        // Describes whether attribute names represent attributes for vertices, transactions, or both (eg. selected)
        Map<String, GraphElementType> attributeNameElementTypes = new HashMap<>();
//        for (int i = 0; i < attributeCount; i++) {
//            attributeNames.add(graph.getAttributeName(graph.getAttribute(elementType, i)));
//        }
        for (int i = 0; i < graph.getAttributeCount(GraphElementType.VERTEX); i++) {
            final String vertAttrLabel = graph.getAttributeName(graph.getAttribute(GraphElementType.VERTEX, i));
            attributeNames.add(vertAttrLabel);
            attributeNameElementTypes.put(vertAttrLabel, GraphElementType.VERTEX);
        }
        for (int i = 0; i < graph.getAttributeCount(GraphElementType.TRANSACTION); i++) {
            final String transAttrLabel = graph.getAttributeName(graph.getAttribute(GraphElementType.TRANSACTION, i));
            if (attributeNames.contains(transAttrLabel)) {
                attributeNameElementTypes.put(transAttrLabel, null);
            } else {
                attributeNames.add(transAttrLabel);
                attributeNameElementTypes.put(transAttrLabel, GraphElementType.TRANSACTION);
            }
        }

        // Process attribute name calls
        for (String attributeName : attributeNames) {
            final String[] prefixes = attributeNameElementTypes.get(attributeName) == GraphElementType.TRANSACTION ? transactionPrefixes : vertexPrefixes;
            for (String prefix : prefixes) {
                final String adjustedAttributeName = escapeAttributeName(prefix + attributeName);
                final String regexifiedAttributeName = regexifyAttributeName(attributeName);
                final String adjustedAttributeUsage = adjustedAttributeName + attributeUsageSuffix;
                // Match any non-word character or the start of the script, followed by the prefix and the attribute name,
                // followed by any non-word character or the end of the script
                final String pattern = "(\\A|\\W)(" + prefix + regexifiedAttributeName + ")(\\W|\\z)";
                // Construct a matcher for the given pattern and script
                Matcher m = Pattern.compile(pattern).matcher(script);
                // Replace all matches, noting that we need to leave the first matched character (start of script or any non-word character) and hence can't use the default replaceAll method.
                StringBuffer result = new StringBuffer();
                boolean attrFound = false;
                while (m.find()) {
                    if (!insidePythonString(script, m.start(2))) {
                        attrFound = true;
                        m.appendReplacement(result, m.group(1) + adjustedAttributeUsage + m.group(3));
                    }
                }
                m.appendTail(result);
                script = result.toString();

                // Bind the adjusted string in the script to the attribute value and add the attribute value to the calculator's context.
                if (attrFound) {
                    CalculatorAttributeValue attrValue = new CalculatorAttributeValue(attributeName, prefix, transactionContextOnlyPrefices.contains(prefix) ? GraphElementType.TRANSACTION : attributeNameElementTypes.get(attributeName));
                    calculatorContext.addDependantValue(attrValue);
                    bindings.put(adjustedAttributeName, attrValue);
                }
            }
        }

        // Process graph property name calls
        for (CalculatorVariable variable : CalculatorVariable.values()) {
//            if (variable.getElementType() == null || variable.getElementType() == elementType) {
            final String variableName = variable.getVariableName();
            // Note that we don't need to escape here as all calculator variables are intentionally named as valid python identifiers
            final String adjustedVariableUsage = variableName + ".val()";
            // Match any non-word character or the start of the script, followed by the variable name, followed
            // by any non-word character or the end of the script.
            final String pattern = "(\\A|\\W)(" + variableName + ")(\\W|\\z)";
            // Construct a matcher for the given pattern and script
            Matcher m = Pattern.compile(pattern).matcher(script);
            // Replace all matches, noting that we need to leave the first matched character (start of script or any non-word character) and hence can't use the default replaceAll method.
            StringBuffer result = new StringBuffer();
            boolean propertyFound = false;
            while (m.find()) {
                if (!insidePythonString(script, m.start(2))) {
                    propertyFound = true;
                    m.appendReplacement(result, m.group(1) + adjustedVariableUsage + m.group(3));
                }
            }
            m.appendTail(result);
            script = result.toString();

            // Initialise the calculator variable, bind the adjusted string in the script to the variable value and add the variable value to the calculator's context.
            if (propertyFound) {
                variable.init(graph);
                CalculatorVariableValue variableValue = new CalculatorVariableValue(variable);
                calculatorContext.addDependantValue(variableValue);
                bindings.put(variableName, variableValue);
            }
//            }
        }
    }

    public static boolean insidePythonString(String s, int position) {

        // Split up the components of the graph labels and decorators string by toSplitOn, checking for escaped toSplitOns in attribute names.
        boolean insideSingle = false, insideDouble = false, insideThreeSingle = false, insideThreeDouble = false;
        int currentPos = 0;
        int currentNumSlashes = 0;
        while (currentPos <= position) {

            // If there is an unescaped single quote and we are not inside a double quote context
            if (s.charAt(currentPos) == '\'' && currentNumSlashes % 2 == 0 && !(insideDouble || insideThreeDouble)) {
                // Three single quotes when not inside a single quote context
                if (!insideSingle && s.charAt(currentPos + 1) == '\'' && s.charAt(currentPos + 2) == '\'') {
                    currentPos += 2;
                    insideThreeSingle = !insideThreeSingle;
                } else {
                    insideSingle = !insideSingle;
                }
            } // If there is an unescaped double quote and we are not inside a single quote context
            else if (s.charAt(currentPos) == '"' && currentNumSlashes % 2 == 0 && !(insideSingle || insideThreeSingle)) {
                // Three single quotes when not inside a single quote context
                if (!insideDouble && s.charAt(currentPos + 1) == '"' && s.charAt(currentPos + 2) == '"') {
                    currentPos += 2;
                    insideThreeDouble = !insideThreeDouble;
                } else {
                    insideDouble = !insideDouble;
                }
            }
            if (s.charAt(currentPos) == '\\') {
                currentNumSlashes++;
            } else {
                currentNumSlashes = 0;
            }
            currentPos++;
        }
        return insideSingle || insideDouble || insideThreeSingle || insideThreeDouble;
    }

    private static int findNextPythonArgumentStart(String s, int startPosition) {
        int braceCount = 0, parenCount = 0, bracketCount = 0;
        int position = startPosition;
        while (position < s.length() && parenCount >= 0) {
            if (!insidePythonString(s, position)) {
                if (s.charAt(position) == '(') {
                    parenCount++;
                } else if (s.charAt(position) == ')') {
                    parenCount--;
                } else if (s.charAt(position) == '[') {
                    bracketCount++;
                } else if (s.charAt(position) == ']') {
                    bracketCount--;
                } else if (s.charAt(position) == '{') {
                    braceCount++;
                } else if (s.charAt(position) == '}') {
                    braceCount--;
                } else if (s.charAt(position) == ',' && parenCount == 0 && braceCount == 0 && bracketCount == 0) {
                    return position + 1;
                }
            }
            position++;
        }
        return -1;
    }

    private static final String REGEX_CONTROL_CHARACTERS = "\\.[]{}()?*+|^$:";

    // Prepends any regex control characters with a double slash.
    private static String regexifyAttributeName(String attrName) {
        StringBuilder result = new StringBuilder();
        for (char c : attrName.toCharArray()) {
            if (REGEX_CONTROL_CHARACTERS.contains(Character.toString(c))) {
                result.append("\\");
            }
            result.append(c);
        }
        return result.toString();
    }

    private static String escapeAttributeName(String attrName) {

        // Replace all underscores with double undescores since underscore will be our escape character
        attrName = attrName.replaceAll(SeparatorConstants.UNDERSCORE, "__");
        int addedChars = 0;
        final int length = attrName.length();

        // Escape any characters which are not valid python identifiers - these are the same as valid java identifier charcaters excluding currency symbols.
        for (int i = 0; i < length; i++) {
            final char c = attrName.charAt(i + addedChars);
            if (!Character.isJavaIdentifierPart(c) || Character.getType(c) == Character.CURRENCY_SYMBOL) {
                String escape = "_x" + Integer.toString(c);
                attrName = attrName.substring(0, i + addedChars) + escape + attrName.substring(i + addedChars + 1, attrName.length());
                addedChars += escape.length() - 1;
            }
        }

        // Prepend the identifier with an underscore if its first character is a number
        final char firstChar = attrName.charAt(0);
        if (Character.isDigit(firstChar)) {
            attrName = SeparatorConstants.UNDERSCORE + attrName;
            addedChars++;
        }
        return attrName;
    }
}
