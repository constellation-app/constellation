/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.layers.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.value.IndexedReadable;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionFilter;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import au.gov.asd.tac.constellation.graph.value.expression.IndexedReadableProvider;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanReadable;
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
 * @author darren
 */
public class SelectExpressionPlugin extends SimpleEditPlugin {

    private final GraphElementType elementType;
    private final String expressionString;
    private final SequenceExpression expression;

    public SelectExpressionPlugin(GraphElementType elementType, String expressionString) {
        super("Select by expression: " + elementType.getShortLabel());
        this.elementType = elementType;
        this.expressionString = expressionString;
        this.expression = ExpressionParser.parse(expressionString);
        this.expression.normalize();
    }
    
    @Override
    protected void edit(GraphWriteMethods graph, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        
        final IndexedReadableProvider indexedReadableProvider = new GraphIndexedReadableProvider(graph, elementType);
        
        final int selectedAttribute;
        switch (elementType) {
            case VERTEX:
                selectedAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
                break;
            case TRANSACTION:
                selectedAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
            default:
                throw new IllegalArgumentException("Unsupported graph element type: " + elementType);
        }
        
        final IndexedReadable expressionReadable = ExpressionFilter.createExpressionReadable(expression, indexedReadableProvider, ConverterRegistry.getDefault());
        final Object expressionResult = expressionReadable.createValue();
        final BooleanReadable expressionBooleanReadable = ConverterRegistry.getDefault().convert(expressionResult, BooleanReadable.class);
        if (expressionBooleanReadable == null) {
            throw new IllegalArgumentException("Expression does not create a boolean result");
        }
        
        final int elementCount = elementType.getElementCount(graph);
        for (int position = 0; position < elementCount; position++) {
            final int id = elementType.getElement(graph, position);
            expressionReadable.read(id, expressionResult);
            graph.setBooleanValue(selectedAttribute, id, expressionBooleanReadable.readBoolean());
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
    
    public static void run(Graph graph, GraphElementType elementType, String expressionString) {
        final Plugin plugin = new SelectExpressionPlugin(elementType, expressionString);
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
