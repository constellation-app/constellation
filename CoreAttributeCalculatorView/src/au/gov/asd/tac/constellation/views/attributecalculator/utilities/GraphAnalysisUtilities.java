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
package au.gov.asd.tac.constellation.views.attributecalculator.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import org.openide.util.lookup.ServiceProvider;
import org.python.core.PyFunction;

/**
 * Used to allow neighbour vertex analysis in the attribute calculator.
 *
 * The attribute calculator plugin constructs a VertexNeighbourContext object
 * with the relevant graph, engine and bindings. This object is then bound to
 * the name 'neighbours' from the perspective of users coding in python. The
 * current element id being processed by the attribute calculator must be
 * updated in this object. Users can then call neighbours.has_neighbour() for
 * example to perform analysis on the neighbours of each node in the graph. The
 * public methods in this class are intentionally named with underscores against
 * convention as these names must match the names visible to the user which
 * should be pythonic.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AbstractCalculatorUtilities.class)
public class GraphAnalysisUtilities extends AbstractCalculatorUtilities {

    private static final String SCRIPTING_NAME = "graphAnalysis";
    private CalculatorContextManager context;

    @Override
    public void setContextManager(final CalculatorContextManager context) {
        this.context = context;
    }

    @Override
    public String getScriptingName() {
        return SCRIPTING_NAME;
    }

    public boolean graph_has_node(final PyFunction condition) throws ScriptException {
        for (int i = 0; i < context.graph.getVertexCount(); i++) {
            final int elementId = context.graph.getVertex(i);
            context.enter(elementId, GraphElementType.VERTEX);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.exit();
            if (conditionMatches) {
                return true;
            }
        }
        return false;
    }

    public int graph_count_nodes(final PyFunction condition, final int distance) throws ScriptException {
        int count = 0;
        for (int i = 0; i < context.graph.getVertexCount(); i++) {
            final int elementId = context.graph.getVertex(i);
            context.enter(elementId, GraphElementType.VERTEX);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.exit();
            if (conditionMatches) {
                count++;
            }
        }
        return count;
    }

    public List<Object> graph_for_nodes(final PyFunction computation) throws ScriptException {
        return graph_for_nodes(null, computation);
    }

    public List<Object> graph_for_nodes(final PyFunction condition, final PyFunction computation) throws ScriptException {
        final List<Object> results = new ArrayList<>();
        for (int i = 0; i < context.graph.getVertexCount(); i++) {
            final int elementId = context.graph.getVertex(i);
            context.enter(elementId, GraphElementType.VERTEX);
            if (condition == null || isTrueValue(condition.__call__())) {
                final Object result = computation.__call__();
                if (!nullCheck(result)) {
                    results.add(result);
                }
            }
            context.exit();
        }
        return results;
    }

    public boolean graph_has_transaction(final PyFunction condition) throws ScriptException {
        for (int i = 0; i < context.graph.getTransactionCount(); i++) {
            final int elementId = context.graph.getTransaction(i);
            context.enter(elementId, GraphElementType.TRANSACTION);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.exit();
            if (conditionMatches) {
                return true;
            }
        }
        return false;
    }

    public int graph_count_transactions(final PyFunction condition, final int distance) throws ScriptException {
        int count = 0;
        for (int i = 0; i < context.graph.getTransactionCount(); i++) {
            final int elementId = context.graph.getTransaction(i);
            context.enter(elementId, GraphElementType.TRANSACTION);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.exit();
            if (conditionMatches) {
                count++;
            }
        }
        return count;
    }

    public List<Object> graph_for_transactions(final PyFunction computation) throws ScriptException {
        return graph_for_transactions(null, computation);
    }

    public List<Object> graph_for_transactions(final PyFunction condition, final PyFunction computation) throws ScriptException {
        final List<Object> results = new ArrayList<>();
        for (int i = 0; i < context.graph.getTransactionCount(); i++) {
            final int elementId = context.graph.getTransaction(i);
            context.enter(elementId, GraphElementType.TRANSACTION);
            if (condition == null || isTrueValue(condition.__call__())) {
                final Object result = computation.__call__();
                if (!nullCheck(result)) {
                    results.add(result);
                }
            }
            context.exit();
        }
        return results;
    }

}
