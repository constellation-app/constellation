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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.script.ScriptException;
import org.openide.util.lookup.ServiceProvider;
import org.python.core.PyFunction;
import org.python.core.PyList;

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
public class VertexTransactionUtilities extends AbstractCalculatorUtilities {

    private static final String SCRIPTING_NAME = "vertexTransactions";
    private CalculatorContextManager context;

    @Override
    public void setContextManager(final CalculatorContextManager context) {
        this.context = context;
    }

    @Override
    public String getScriptingName() {
        return SCRIPTING_NAME;
    }

    private boolean checkTransactions(final PyFunction condition, final int direction) {
        return checkTransactions(condition, null, direction);
    }

    private boolean checkTransactions(final PyFunction condition, final PyFunction nodeCondition, final int direction) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> transactions = getTransactions(context.current(), direction);
        for (int elementId : transactions.keySet()) {
            context.enter(elementId, GraphElementType.TRANSACTION);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.enter(transactions.get(elementId), GraphElementType.VERTEX);
            boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
            context.exit();
            context.exit();
            if (conditionMatches && otherNodeMatches) {
                return true;
            }
        }
        return false;
    }

    public boolean has_transaction(final PyFunction condition) throws ScriptException {
        return checkTransactions(condition, Graph.NOT_FOUND);
    }

    public boolean has_outgoing_transaction(final PyFunction condition) throws ScriptException {
        return checkTransactions(condition, Graph.OUTGOING);
    }

    public boolean has_incoming_transaction(final PyFunction condition) throws ScriptException {
        return checkTransactions(condition, Graph.INCOMING);
    }

    public boolean has_undirected_transaction(final PyFunction condition) throws ScriptException {
        return checkTransactions(condition, Graph.UNDIRECTED);
    }

    public boolean has_transaction(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return checkTransactions(condition, nodeCondition, Graph.NOT_FOUND);
    }

    public boolean has_outgoing_transaction(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return checkTransactions(condition, nodeCondition, Graph.OUTGOING);
    }

    public boolean has_incoming_transaction(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return checkTransactions(condition, nodeCondition, Graph.INCOMING);
    }

    public boolean has_undirected_transaction(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return checkTransactions(condition, nodeCondition, Graph.UNDIRECTED);
    }

    public boolean has_parallel_transaction(final PyFunction condition) throws ScriptException {
        context.require(GraphElementType.TRANSACTION);
        final Set<Integer> transactions = getParallelTransactions(context.current());
        for (int transId : transactions) {
            context.enter(transId, GraphElementType.TRANSACTION);
            boolean conditionMatches = isTrueAndContainsNonNulls(condition.__call__());
            context.exit();
            if (conditionMatches) {
                return true;
            }
        }
        return false;
    }

    public boolean has_edge(final PyFunction condition) {
        return has_edge(condition, null);
    }

    public boolean has_edge(final PyFunction condition, final PyFunction nodeCondition) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> edges = getEdges(context.current());
        for (int elementId : edges.keySet()) {
            context.enter(elementId, GraphElementType.EDGE);
            boolean conditionMatches = isTrueAndContainsNonNulls(condition.__call__());
            context.enter(edges.get(elementId), GraphElementType.VERTEX);
            boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
            context.exit();
            context.exit();
            if (conditionMatches && otherNodeMatches) {
                return true;
            }
        }
        return false;
    }

    public boolean has_link(final PyFunction condition) {
        return has_link(condition, null);
    }

    public boolean has_link(final PyFunction condition, final PyFunction nodeCondition) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> links = getLinks(context.current());
        for (int elementId : links.keySet()) {
            context.enter(elementId, GraphElementType.LINK);
            boolean conditionMatches = isTrueAndContainsNonNulls(condition.__call__());
            context.enter(links.get(elementId), GraphElementType.VERTEX);
            boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
            context.exit();
            context.exit();
            if (conditionMatches && otherNodeMatches) {
                return true;
            }
        }
        return false;
    }

    private int countTransactions(final PyFunction condition, final int direction) {
        return countTransactions(condition, null, direction);
    }

    private int countTransactions(final PyFunction condition, final PyFunction nodeCondition, final int direction) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> transactions = getTransactions(context.current(), direction);
        int count = 0;
        for (int elementId : transactions.keySet()) {
            context.enter(elementId, GraphElementType.TRANSACTION);
            boolean conditionMatches = isTrueValue(condition.__call__());
            context.enter(transactions.get(elementId), GraphElementType.VERTEX);
            boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
            context.exit();
            context.exit();
            if (conditionMatches && otherNodeMatches) {
                count++;
            }
        }
        return count;
    }

    public int count_transactions(final PyFunction condition) throws ScriptException {
        return countTransactions(condition, Graph.NOT_FOUND);
    }

    public int count_outgoing_transactions(final PyFunction condition) throws ScriptException {
        return countTransactions(condition, Graph.OUTGOING);
    }

    public int count_incoming_transactions(final PyFunction condition) throws ScriptException {
        return countTransactions(condition, Graph.INCOMING);
    }

    public int count_undirected_transactions(final PyFunction condition) throws ScriptException {
        return countTransactions(condition, Graph.UNDIRECTED);
    }

    public int count_transactions(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return countTransactions(condition, nodeCondition, Graph.NOT_FOUND);
    }

    public int count_outgoing_transactions(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return countTransactions(condition, nodeCondition, Graph.OUTGOING);
    }

    public int count_incoming_transactions(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return countTransactions(condition, nodeCondition, Graph.INCOMING);
    }

    public int count_undirected_transactions(final PyFunction condition, final PyFunction nodeCondition) throws ScriptException {
        return countTransactions(condition, nodeCondition, Graph.UNDIRECTED);
    }

    public int count_parallel_transactions(final PyFunction condition) throws ScriptException {
        context.require(GraphElementType.TRANSACTION);
        final Set<Integer> transactions = getParallelTransactions(context.current());
        int count = 0;
        for (int transId : transactions) {
            context.enter(transId, GraphElementType.TRANSACTION);
            boolean conditionMatches = isTrueAndContainsNonNulls(condition.__call__());
            context.exit();
            if (conditionMatches) {
                count++;
            }
        }
        return count;
    }

    public int count_edges(final PyFunction condition) {
        return count_edges(condition, null);
    }

    public int count_edges(final PyFunction condition, final PyFunction nodeCondition) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> edges = getEdges(context.current());
        int count = 0;
        for (int elementId : edges.keySet()) {
            context.enter(elementId, GraphElementType.EDGE);
            boolean conditionMatches = isTrueAndContainsNonNulls(condition.__call__());
            context.enter(edges.get(elementId), GraphElementType.VERTEX);
            boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
            context.exit();
            context.exit();
            if (conditionMatches && otherNodeMatches) {
                count++;
            }
        }
        return count;
    }

    public int count_links(final PyFunction condition) {
        return count_links(condition, null);
    }

    public int count_links(final PyFunction condition, final PyFunction nodeCondition) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> links = getLinks(context.current());
        int count = 0;
        for (int elementId : links.keySet()) {
            context.enter(elementId, GraphElementType.LINK);
            boolean conditionMatches = isTrueAndContainsNonNulls(condition.__call__());
            context.enter(links.get(elementId), GraphElementType.VERTEX);
            boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
            context.exit();
            context.exit();
            if (conditionMatches && otherNodeMatches) {
                count++;
            }
        }
        return count;
    }

    private PyList forTransactions(final PyFunction computation, final int direction) {
        return forTransactions(null, computation, direction);
    }

    private PyList forTransactions(final PyFunction condition, PyFunction computation, final int direction) {
        return forTransactions(condition, null, computation, direction);
    }

    private PyList forTransactions(final PyFunction condition, PyFunction nodeCondition, PyFunction computation, final int direction) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> transactions = getTransactions(context.current(), direction);
        final List<Object> results = new ArrayList<>();
        for (int elementId : transactions.keySet()) {
            context.enter(elementId, GraphElementType.TRANSACTION);
            if (condition == null || isTrueValue(condition.__call__())) {
                context.enter(transactions.get(elementId), GraphElementType.VERTEX);
                boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
                context.exit();
                if (otherNodeMatches) {
                    Object result = computation.__call__();
                    if (!nullCheck(result)) {
                        results.add(result);
                    }
                }
            }
            context.exit();
        }
        return new PyList(results);
    }

    public PyList for_transactions(final PyFunction computation) throws ScriptException {
        return forTransactions(computation, Graph.NOT_FOUND);
    }

    public PyList for_outgoing_transactions(final PyFunction computation) throws ScriptException {
        return forTransactions(computation, Graph.OUTGOING);
    }

    public PyList for_incoming_transactions(final PyFunction computation) throws ScriptException {
        return forTransactions(computation, Graph.INCOMING);
    }

    public PyList for_undirected_transactions(final PyFunction computation) throws ScriptException {
        return forTransactions(computation, Graph.UNDIRECTED);
    }

    public PyList for_transactions(final PyFunction condition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, computation, Graph.NOT_FOUND);
    }

    public PyList for_outgoing_transactions(final PyFunction condition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, computation, Graph.OUTGOING);
    }

    public PyList for_incoming_transactions(final PyFunction condition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, computation, Graph.INCOMING);
    }

    public PyList for_undirected_transactions(final PyFunction condition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, computation, Graph.UNDIRECTED);
    }

    public PyList for_transactions(final PyFunction condition, final PyFunction nodeCondition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, nodeCondition, computation, Graph.NOT_FOUND);
    }

    public PyList for_outgoing_transactions(final PyFunction condition, final PyFunction nodeCondition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, nodeCondition, computation, Graph.OUTGOING);
    }

    public PyList for_incoming_transactions(final PyFunction condition, final PyFunction nodeCondition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, nodeCondition, computation, Graph.INCOMING);
    }

    public PyList for_undirected_transactions(final PyFunction condition, final PyFunction nodeCondition, final PyFunction computation) throws ScriptException {
        return forTransactions(condition, nodeCondition, computation, Graph.UNDIRECTED);
    }

    public PyList for_parallel_transactions(final PyFunction computation) throws ScriptException {
        return for_parallel_transactions(null, computation);
    }

    public PyList for_parallel_transactions(final PyFunction condition, final PyFunction computation) throws ScriptException {
        context.require(GraphElementType.TRANSACTION);
        final Set<Integer> transactions = getParallelTransactions(context.current());
        final List<Object> results = new ArrayList<>();
        for (int transId : transactions) {
            context.enter(transId, GraphElementType.TRANSACTION);
            if (condition == null || isTrueValue(condition.__call__())) {
                final Object result = computation.__call__();
                if (!nullCheck(result)) {
                    results.add(result);
                }
            }
            context.exit();
        }
        return new PyList(results);
    }

    // returns a map which maps the IDs of transactions from the supplied vertex to the IDs of the vertices at the other end of the transaction
    private Map<Integer, Integer> getTransactions(final int vxId, final int direction) {
        if (direction == Graph.NOT_FOUND) {
            return getTransactions(vxId);
        }
        final Map<Integer, Integer> transactions = new HashMap<>();
        final int numTransactions = context.graph.getVertexTransactionCount(vxId, direction);
        for (int i = 0; i < numTransactions; i++) {
            final int transId = context.graph.getVertexTransaction(vxId, direction, i);
            final int sourceVxId = context.graph.getTransactionSourceVertex(transId);
            final int otherVxId = sourceVxId == vxId ? context.graph.getTransactionDestinationVertex(transId) : sourceVxId;
            transactions.put(transId, otherVxId);
        }
        return transactions;
    }

    // returns a map which maps the IDs of transactions from the supplied vertex to the IDs of the vertices at the other end of the transaction
    private Map<Integer, Integer> getTransactions(final int vxId) {
        final Map<Integer, Integer> transactions = new HashMap<>();
        final int numTransactions = context.graph.getVertexTransactionCount(vxId);
        for (int i = 0; i < numTransactions; i++) {
            final int transId = context.graph.getVertexTransaction(vxId, i);
            final int sourceVxId = context.graph.getTransactionSourceVertex(transId);
            final int otherVxId = sourceVxId == vxId ? context.graph.getTransactionDestinationVertex(transId) : sourceVxId;
            transactions.put(transId, otherVxId);
        }
        return transactions;
    }

    // returns a map which maps the IDs of edges from the supplied vertex to the IDs of the vertices at the other end of the edge
    private Map<Integer, Integer> getEdges(final int vxId) {
        final Map<Integer, Integer> edges = new HashMap<>();
        final int numEdges = context.graph.getVertexEdgeCount(vxId);
        for (int i = 0; i < numEdges; i++) {
            final int edgeId = context.graph.getVertexEdge(vxId, i);
            final int sourceVxId = context.graph.getEdgeSourceVertex(edgeId);
            final int otherVxId = sourceVxId == vxId ? context.graph.getEdgeDestinationVertex(edgeId) : sourceVxId;
            edges.put(edgeId, otherVxId);
        }
        return edges;
    }

    // returns a map which maps the IDs of links from the supplied vertex to the IDs of the vertices at the other end of the link
    private Map<Integer, Integer> getLinks(final int vxId) {
        final Map<Integer, Integer> links = new HashMap<>();
        final int numLinks = context.graph.getVertexLinkCount(vxId);
        for (int i = 0; i < numLinks; i++) {
            final int linkId = context.graph.getVertexLink(vxId, i);
            final int sourceVxId = context.graph.getLinkLowVertex(linkId);
            final int otherVxId = sourceVxId == vxId ? context.graph.getLinkHighVertex(linkId) : sourceVxId;
            links.put(linkId, otherVxId);
        }
        return links;
    }

    // Returns a set which contains the IDs of transactions which are parallel (share the same source and destination, possibly switched) to the supplied transaction. Note that a transaction is considered parallel to itself.
    private Set<Integer> getParallelTransactions(final int txId) {
        Set<Integer> transactions = new HashSet<>();
        final int lnId = context.graph.getTransactionLink(txId);
        for (int i = 0; i < context.graph.getLinkTransactionCount(lnId); i++) {
            final int transId = context.graph.getLinkTransaction(lnId, i);
            transactions.add(transId);
        }
        return transactions;
    }

    public PyList for_edges(final PyFunction computation) {
        return for_edges(null, computation);
    }

    public PyList for_edges(final PyFunction condition, PyFunction computation) {
        return for_edges(condition, null, computation);
    }

    public PyList for_edges(final PyFunction condition, PyFunction nodeCondition, PyFunction computation) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> edges = getEdges(context.current());
        final List<Object> results = new ArrayList<>();
        for (int elementId : edges.keySet()) {
            context.enter(elementId, GraphElementType.EDGE);
            if (condition == null || isTrueAndContainsNonNulls(condition.__call__())) {
                context.enter(edges.get(elementId), GraphElementType.VERTEX);
                boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
                context.exit();
                if (otherNodeMatches) {
                    final Object result = computation.__call__();
                    if (!nullCheck(result)) {
                        results.add(result);
                    }
                }
            }
            context.exit();
        }
        return new PyList(results);
    }

    public PyList for_links(final PyFunction computation) {
        return for_links(null, computation);
    }

    public PyList for_links(final PyFunction condition, PyFunction computation) {
        return for_links(condition, null, computation);
    }

    public PyList for_links(final PyFunction condition, PyFunction nodeCondition, PyFunction computation) {
        context.require(GraphElementType.VERTEX);
        final Map<Integer, Integer> links = getLinks(context.current());
        final List<Object> results = new ArrayList<>();
        for (int elementId : links.keySet()) {
            context.enter(elementId, GraphElementType.LINK);
            if (condition == null || isTrueAndContainsNonNulls(condition.__call__())) {
                context.enter(links.get(elementId), GraphElementType.VERTEX);
                boolean otherNodeMatches = nodeCondition == null || isTrueValue(nodeCondition.__call__());
                context.exit();
                if (otherNodeMatches) {
                    final Object result = computation.__call__();
                    if (!nullCheck(result)) {
                        results.add(result);
                    }
                }
            }
            context.exit();
        }
        return new PyList(results);
    }

}
