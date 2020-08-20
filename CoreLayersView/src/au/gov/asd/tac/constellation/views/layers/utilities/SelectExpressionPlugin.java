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
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.value.Operators;
import au.gov.asd.tac.constellation.graph.value.Updatable;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionCompiler;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import au.gov.asd.tac.constellation.graph.value.expression.VariableProvider;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import au.gov.asd.tac.constellation.graph.value.values.IntValue;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openide.util.Exceptions;

/**
 *
 * @author sirius
 */
public class SelectExpressionPlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final String attributeName;
    private final String expressionString;
    private final SequenceExpression expression;

    public SelectExpressionPlugin(GraphElementType elementType, String expressionString, String attributeName) {
        super("Calculate by expression: " + elementType.getShortLabel());
        this.elementType = elementType;
        this.attributeName = attributeName;
        this.expressionString = expressionString;
        this.expression = ExpressionParser.parse(expressionString);
        this.expression.normalize();
    }
    
    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        
        final VariableProvider variableProvider = new GraphVariableProvider(graph, elementType);
        
        final IntValue indexValue = new IntValue();
        
        final var compiledExpression = (Updatable)ExpressionCompiler.compileSequenceExpression(expression, variableProvider, indexValue, Operators.getDefault());
        
        final int elementCount = elementType.getElementCount(graph);
        for (int position = 0; position < elementCount; position++) {
            final int id = elementType.getElement(graph, position);
            indexValue.writeInt(id);
            compiledExpression.update();
        }
    }
    
    private static final class GraphVariableProvider implements VariableProvider {

        final GraphWriteMethods graphWriteMethods;
        final GraphElementType elementType;

        public GraphVariableProvider(GraphWriteMethods graphWriteMethods, GraphElementType elementType) {
            this.graphWriteMethods = graphWriteMethods;
            this.elementType = elementType;
        }

        @Override
        public Object getVariable(String name, IntReadable indexReadable) {
            final int attribute = graphWriteMethods.getAttribute(elementType, name);
            if (attribute == Graph.NOT_FOUND) {
                return null;
            }
            return graphWriteMethods.createWriteAttributeObject(attribute, indexReadable);
        }
    }
    
    public static void run(Graph graph, GraphElementType elementType, String expressionString, String attributeName) {
        final Plugin plugin = new SelectExpressionPlugin(elementType, expressionString, attributeName);
        final Future<?> f = PluginExecution.withPlugin(plugin).executeLater(graph);
        try {
            f.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
