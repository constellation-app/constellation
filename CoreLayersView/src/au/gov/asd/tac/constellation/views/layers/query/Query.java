/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.query;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.value.Operators;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionCompiler;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser.SequenceExpression;
import au.gov.asd.tac.constellation.graph.value.expression.VariableProvider;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Query element that holds either a vertex or transaction query for each layer 
 *
 * @author sirius
 */
public class Query {

    private final GraphElementType elementType;
    private final String queryString;

    private int[] attributeIds = null;

    public Query(final GraphElementType elementType, final String queryString) {
        this.elementType = elementType;
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    public GraphElementType getElementType() {
        return elementType;
    }

    public Object compile(final GraphReadMethods graph, final IntReadable index) {
        final SequenceExpression expression = ExpressionParser.parse(queryString);

        final GraphVariableProvider variableProvider = new GraphVariableProvider(graph, elementType);

        final Object result = ExpressionCompiler.compileSequenceExpression(expression, variableProvider, index, Operators.getDefault());

        attributeIds = variableProvider.getAttributeIds();
        long[] valueModificationCounters = new long[attributeIds.length];
        for (int i = 0; i < attributeIds.length; i++) {
            valueModificationCounters[i] = graph.getValueModificationCounter(attributeIds[i]);
        }

        return result;
    }

    public int[] getAttributeIds() {
        return attributeIds;
    }

    private static final class GraphVariableProvider implements VariableProvider {

        final GraphReadMethods graphReadMethods;
        final GraphElementType elementType;
        final Map<String, Object> variables = new HashMap<>();
        final int[] attributeIds;
        int attributeCount = 0;

        public GraphVariableProvider(GraphReadMethods graphWriteMethods, GraphElementType elementType) {
            this.graphReadMethods = graphWriteMethods;
            this.elementType = elementType;
            attributeIds = new int[graphWriteMethods.getAttributeCapacity()];
        }

        public int[] getAttributeIds() {
            return Arrays.copyOf(attributeIds, attributeCount);
        }

        @Override
        public Object getVariable(String name, IntReadable indexReadable) {
            Object variable = variables.get(name);
            if (variable != null) {
                return variable;
            }
            final int attribute = graphReadMethods.getAttribute(elementType, name);
            if (attribute == Graph.NOT_FOUND) {
                return null;
            }
            attributeIds[attributeCount++] = attribute;

            variable = graphReadMethods.createReadAttributeObject(attribute, indexReadable);
            variables.put(name, variable);
            return variable;
        }
    }
}
