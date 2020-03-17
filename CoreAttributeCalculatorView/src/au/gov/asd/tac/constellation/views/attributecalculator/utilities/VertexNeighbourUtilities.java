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
import java.util.BitSet;
import java.util.LinkedList;
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
public class VertexNeighbourUtilities extends AbstractCalculatorUtilities {

    private static final String SCRIPTING_NAME = "neighbours";
    private CalculatorContextManager context;

    @Override
    public void setContextManager(final CalculatorContextManager context) {
        this.context = context;
    }

    @Override
    public String getScriptingName() {
        return SCRIPTING_NAME;
    }

    public boolean has_node_at_distance(final PyFunction condition, final int distance) throws ScriptException {
        context.require(GraphElementType.VERTEX);
        final List<Integer> elementsAtDistance = new LinkedList<>();
        getAtDistance(distance, context.current(), elementsAtDistance, new BitSet(context.graph.getVertexCount()));
        for (int elementId : elementsAtDistance) {
            context.enter(elementId);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.exit();
            if (conditionMatches) {
                return true;
            }
        }
        return false;
    }

    public int count_nodes_at_distance(final PyFunction condition, final int distance) throws ScriptException {
        context.require(GraphElementType.VERTEX);
        final List<Integer> elementsAtDistance = new LinkedList<>();
        getAtDistance(distance, context.current(), elementsAtDistance, new BitSet(context.graph.getVertexCount()));
        int count = 0;
        for (int elementId : elementsAtDistance) {
            context.enter(elementId);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.exit();
            if (conditionMatches) {
                count++;
            }
        }
        return count;
    }

    public List<Integer> get_node_distances(final PyFunction condition) throws ScriptException {
        context.require(GraphElementType.VERTEX);
        final List<Integer> elementsAtDistance = new LinkedList<>();
        final List<Integer> results = new ArrayList<>();
        // Check the condition in the current context (for this node) and add distance 0
        if (isTrueValue(condition.__call__())) {
            results.add(0);
        }
        int currentDistance = 1;
        final BitSet visitedNodes = new BitSet(context.graph.getVertexCount());
        getAtDistance(currentDistance, context.current(), elementsAtDistance, visitedNodes);
        while (!elementsAtDistance.isEmpty()) {
            for (int elementId : elementsAtDistance) {
                context.enter(elementId);
                boolean conditionMatches = isTrueValue(condition.__call__());
                context.exit();
                if (conditionMatches) {
                    results.add(currentDistance);
                    break;
                }
            }
            currentDistance++;
            elementsAtDistance.clear();
            visitedNodes.clear();
            getAtDistance(currentDistance, context.current(), elementsAtDistance, visitedNodes);
        }
        // The node distances should be a list containing -1 if a node meeting the condition was never found.
        if (results.isEmpty()) {
            results.add(-1);
        }
        return results;
    }

    public List<Object> for_nodes_at_distance(final PyFunction computation, final int distance) throws ScriptException {
        return for_nodes_at_distance(null, computation, distance);
    }

    public List<Object> for_nodes_at_distance(final PyFunction condition, final PyFunction computation, final int distance) throws ScriptException {
        context.require(GraphElementType.VERTEX);
        final List<Integer> elementsAtDistance = new LinkedList<>();
        final List<Object> results = new ArrayList<>();
        getAtDistance(distance, context.current(), elementsAtDistance, new BitSet(context.graph.getVertexCount()));
        for (int elementId : elementsAtDistance) {
            context.enter(elementId);
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

    private void getAtDistance(final int distance, final int elementId, final List<Integer> elements, final BitSet visitedNodes) {
        visitedNodes.set(context.graph.getVertexPosition(elementId));
        if (distance == 0) {
            elements.add(elementId);
            return;
        }
        for (int i = 0; i < context.graph.getVertexNeighbourCount(elementId); i++) {
            final int neighbourId = context.graph.getVertexNeighbour(elementId, i);
//            // skip loops
//            if (neighbourId == elementId) {
//                continue;
//            }
            // skip vertices already found (prevent cycling)
            if (visitedNodes.get(context.graph.getVertexPosition(neighbourId))) {
                continue;
            }

            getAtDistance(distance - 1, neighbourId, elements, visitedNodes);
        }
    }

    public boolean has_neighbour(final PyFunction condition) throws ScriptException {
        return has_node_at_distance(condition, 1);
    }

    public int count_neighbours(final PyFunction condition) throws ScriptException {
        return count_nodes_at_distance(condition, 1);
    }

    public List<Object> for_neighbours(final PyFunction computation) throws ScriptException {
        return for_nodes_at_distance(computation, 1);
    }

    public List<Object> for_neighbours(final PyFunction condition, final PyFunction computation) throws ScriptException {
        return for_nodes_at_distance(condition, computation, 1);
    }

}
