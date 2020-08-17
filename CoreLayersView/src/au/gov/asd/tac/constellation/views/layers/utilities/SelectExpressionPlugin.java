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
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.value.IndexedReadable;
import au.gov.asd.tac.constellation.graph.value.Readable;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionFilter;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import au.gov.asd.tac.constellation.graph.value.expression.IndexedReadableProvider;
import au.gov.asd.tac.constellation.graph.value.readables.Assign;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntValue;
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
        
        final IndexedReadableProvider indexedReadableProvider = new GraphIndexedReadableProvider(graph, elementType);
        
        final int resultAttribute = graph.getAttribute(elementType, attributeName);
        if (resultAttribute == Graph.NOT_FOUND) {
            throw new IllegalArgumentException("Unknown " + elementType.getShortLabel() + " attribute: " + attributeName);
        }
        
        final IntValue indexValue = new IntValue();
        final Readable<Object> expressionReadable = (Readable<Object>)ExpressionFilter.createExpressionReadable(expression, indexedReadableProvider, indexValue, ConverterRegistry.getDefault());
        
        final var expressionResult = expressionReadable.createValue();
        
        final var resultValue = graph.createAttributeValue(resultAttribute);
        
        final Assign assign = ConverterRegistry.getDefault().convert(resultValue, expressionResult, Assign.class);
        if (assign == null) {
            throw new IllegalArgumentException("Selection attribute cannot be assigned from expression result");
        }
        
        final int elementCount = elementType.getElementCount(graph);
        for (int position = 0; position < elementCount; position++) {
            final int id = elementType.getElement(graph, position);
            indexValue.writeInt(id);
            expressionReadable.read(expressionResult);
            assign.assign();
            graph.writeAttributeValue(resultAttribute, id, resultValue);
        }
    }
    
    private static final class GraphIndexedReadableProvider implements IndexedReadableProvider {

        final GraphReadMethods graphReadMethods;
        final GraphElementType elementType;

        public GraphIndexedReadableProvider(GraphReadMethods graphReadMethods, GraphElementType elementType) {
            this.graphReadMethods = graphReadMethods;
            this.elementType = elementType;
        }
        
        @Override
        public IndexedReadable<?> getIndexedReadable(String name) {
            return new GraphIndexedReadable(graphReadMethods, elementType, name);
        }
    }
    
    private static final class GraphIndexedReadable<V> implements IndexedReadable<V> {

        private final GraphReadMethods graphReadMethods;
        private final int attribute;
        
        public GraphIndexedReadable(GraphReadMethods graphReadMethods, GraphElementType elementType, String attributeName) {
            this.graphReadMethods = graphReadMethods;
            this.attribute = graphReadMethods.getAttribute(elementType, attributeName);
            if (attribute == Graph.NOT_FOUND) {
                throw new IllegalArgumentException("Unknown " + elementType + " attribute: " + attributeName);
            }
        }
        
        @Override
        public V createValue() {
            return graphReadMethods.createAttributeValue(attribute);
        }

        @Override
        public void read(int id, V value) {
            graphReadMethods.readAttributeValue(attribute, id, value);
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
