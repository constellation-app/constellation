/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.scripting.graph;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * A representation of a graph for use with scripting.
 *
 * @author algol
 * @author cygnus_x-1
 */
public class SGraph {

    private static final Logger LOGGER = Logger.getLogger(SGraph.class.getName());

    /**
     * A reference to the 'Graph' element type.
     */
    public static final GraphElementType GRAPH = GraphElementType.GRAPH;
    /**
     * A reference to the 'Vertex' element type.
     */
    public static final GraphElementType VERTEX = GraphElementType.VERTEX;
    /**
     * A reference to the 'Transaction' element type.
     */
    public static final GraphElementType TRANSACTION = GraphElementType.TRANSACTION;
    /**
     * A reference to the 'Edge' element type.
     */
    public static final GraphElementType EDGE = GraphElementType.EDGE;
    /**
     * A reference to the 'Link' element type.
     */
    public static final GraphElementType LINK = GraphElementType.LINK;

    private ScriptEngine engine;
    private final Graph graphObject;

    private final List<SReadableGraph> readableGraphs;
    private final List<SWritableGraph> writableGraphs;

    public SGraph(final ScriptEngine engine, final Graph graph) {
        this.engine = engine;
        this.graphObject = graph;
        this.readableGraphs = new ArrayList<>();
        this.writableGraphs = new ArrayList<>();
    }

    public SGraph(final Graph graph) {
        this(null, graph);
    }

    /**
     * Get the scripting engine currently being used to evaluate scripts.
     *
     * @return the current scripting engine.
     */
    public ScriptEngine getEngine() {
        return engine;
    }

    /**
     * Set a new scripting engine to be used for evaluating scripts.
     *
     * @param engine the scripting engine to use.
     */
    public void setEngine(final ScriptEngine engine) {
        this.engine = engine;
    }

    /**
     * Get the actual graph on which scripts are being executed.
     *
     * @return the graph on which scripts are being executed.
     */
    public Graph getGraph() {
        return graphObject;
    }

    /**
     * Clean up any graphs created by scripting which remain in memory.
     * <p>
     * After a script completes, we have no way of reliably knowing if the
     * script has called .release(), .commit(), or .rollback() on the graphs it
     * used. It is undesirable to leave graphs remaining in memory if they are
     * no longer in use. To address this issue, we keep track of any graphs used
     * by scripting and offer this method to attempt to force them out of memory
     * if they still exist.
     */
    public void cleanup() {
        for (final SReadableGraph readableGraph : readableGraphs) {
            while (true) {
                try {
                    LOGGER.log(Level.INFO, "attempting to release {0}", readableGraph);
                    readableGraph.release();
                } catch (final IllegalMonitorStateException ex) {
                    LOGGER.log(Level.WARNING, "error releasing {0}: {1}", new Object[]{readableGraph, ex.getMessage()});
                    break;
                }
            }
        }

        for (final SWritableGraph writableGraph : writableGraphs) {
            while (true) {
                try {
                    LOGGER.log(Level.INFO, "attempting to rollback {0}", writableGraph);
                    writableGraph.rollback();
                } catch (final IllegalMonitorStateException ex) {
                    LOGGER.log(Level.WARNING, "error rolling back {0}: {1}", new Object[]{writableGraph, ex.getMessage()});
                    break;
                }
            }
        }
    }

    /**
     * Get a read lock on the graph so that its data can be interrogated.
     * <p>
     * Note: If this method is called in a Python script using the 'with'
     * statement, a context manager will be created for you to automatically
     * handle releases as appropriate.
     *
     * @return a graph with an active read lock.
     * @throws InterruptedException
     */
    public SReadableGraph readableGraph() throws InterruptedException {
        final SReadableGraph readableGraph = new SReadableGraph(this, graphObject.getReadableGraph());
        readableGraphs.add(readableGraph);
        return readableGraph;
    }

    /**
     * Evaluate a function against a read lock on the graph with releases
     * handled automatically.
     * <p>
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback the function to evaluate.
     * @throws ScriptException
     * @throws InterruptedException
     */
    public void withReadableGraph(final Object callback) throws ScriptException, InterruptedException {
        final SReadableGraph readableGraph = readableGraph();
        LOGGER.log(Level.INFO, "creating context for {0}", readableGraph);
        boolean ok = false;
        try {
            final ScriptContext context = engine.getContext();
            context.setAttribute("__func", callback, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("__p1", readableGraph, ScriptContext.ENGINE_SCOPE);
            engine.eval("__func(__p1)");
            ok = true;
        } catch (final ScriptException ex) {
            readableGraph.release();
            throw ex;
        } finally {
            LOGGER.log(Level.WARNING, "context successful = {0}", ok);
            if (ok) {
                readableGraph.release();
                LOGGER.log(Level.WARNING, "releasing {0}", readableGraph);
            }
        }
    }

    /**
     * Get a write lock on the graph so that edits can be made to its data.
     * <p>
     * Note: If this method is called in a Python script using the 'with'
     * statement, a context manager will be created for you to automatically
     * handle commits and rollbacks as appropriate.
     *
     * @param editName a name for the edit operation.
     * @return a graph with an active write lock.
     * @throws InterruptedException
     */
    public SWritableGraph writableGraph(final String editName) throws InterruptedException {
        final SWritableGraph writableGraph = new SWritableGraph(this, graphObject.getWritableGraph(editName, true));
        writableGraphs.add(writableGraph);
        return writableGraph;
    }

    /**
     * Evaluate a function against a write lock on the graph with commits or
     * rollbacks handled automatically.
     * <p>
     * Note: This method will only work for Python scripts as it makes use of
     * Python specific syntax.
     *
     * @param callback the function to evaluate.
     * @throws ScriptException
     * @throws InterruptedException
     */
    public void withWritableGraph(final Object callback) throws ScriptException, InterruptedException {
        final SWritableGraph writableGraph = writableGraph("Scripting View Context Manager");
        LOGGER.log(Level.INFO, "creating context for {0}", writableGraph);
        boolean ok = false;
        try {
            final ScriptContext context = engine.getContext();
            context.setAttribute("__func", callback, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("__p1", writableGraph, ScriptContext.ENGINE_SCOPE);
            engine.eval("__func(__p1)");
            ok = true;
        } catch (final ScriptException ex) {
            writableGraph.rollback();
            throw ex;
        } finally {
            LOGGER.log(Level.WARNING, "context successful = {0}", ok);
            if (ok) {
                writableGraph.commit();
                LOGGER.log(Level.WARNING, "commiting {0}", writableGraph);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("[%s: %d]", this.getClass().getSimpleName(), graphObject.hashCode());
    }
}
