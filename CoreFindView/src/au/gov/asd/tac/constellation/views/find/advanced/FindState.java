/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import java.util.ArrayList;
import org.openide.awt.UndoRedo;

/**
 * This class holds the state variables used by the Find services.
 * <p>
 * It also implements AbstractGraphIOProvider so it can read and write itself.
 * However, under the covers it passes these duties off to a helper class to
 * avoid the clutter here.
 *
 * @author betelgeuse
 */
public final class FindState {

    /**
     * The name of the graph attribute used to hold the instance.
     */
    public static final String ATTRIBUTE_NAME = "find_state";
    private GraphElementType type;
    private boolean isAny = false;
    private ArrayList<FindRule> rules;
    private boolean isHeld = false;

    /**
     * Construct a new FindState.
     */
    public FindState() {
        rules = new ArrayList<>();
    }

    /**
     * Construct a new FindState from an existing FindState using a deep copy.
     *
     * @param state The original FindState.
     */
    public FindState(final FindState state) {
        final FindState fs;
        if (state == null) {
            fs = new FindState();
        } else {
            fs = state;
        }

        rules = fs.rules;
    }

    /**
     * Adds a set of rules to this FindState.
     *
     * @param rules The rules to be added.
     */
    public void addRules(final ArrayList<FindRule> rules) {
        this.rules = rules;
    }

    /**
     * Adds an individual rule to this FindState
     *
     * @param rule The rule to be added.
     */
    public void addRule(final FindRule rule) {
        rules.add(rule);
    }

    /**
     * Returns the rules from this FindState.
     *
     * @return List of rules.
     */
    public ArrayList<FindRule> getRules() {
        return rules;
    }

    /**
     * Sets whether rules are to be applied in 'any' OR, or 'all' AND modes.
     *
     * @param isAny True for 'any' (OR) mode.
     */
    public void setAny(final boolean isAny) {
        this.isAny = isAny;
    }

    /**
     * Returns whether or not rules are to be applied in 'any' OR, or 'all' AND
     * modes.
     *
     * @return True for 'any' mode (OR).
     */
    public boolean isAny() {
        return isAny;
    }

    /**
     * Sets whether or not the currently selected vertices on the graph should
     * be cleared.
     *
     * @param isHeld True for hold selection, False for clear.
     */
    public void setHeld(final boolean isHeld) {
        this.isHeld = isHeld;
    }

    /**
     * Returns whether or not the currently selected nodes on the graph should
     * be cleared.
     *
     * @return True to hold the currently selected vertices on the graph, false
     * to clear.
     */
    public boolean isHeld() {
        return isHeld;
    }

    /**
     * Sets the type of GraphElement that is being searched in the current
     * state.
     *
     * @param type VERTEX, LINK, EDGE or TRANSACTION.
     */
    public void setGraphElementType(final GraphElementType type) {
        this.type = type;
    }

    /**
     * Returns this state's GraphElementType.
     *
     * @return VERTEX, LINK, EDGE or TRANSACTION.
     */
    public GraphElementType getGraphElementType() {
        return type;
    }

    /**
     * Update the find state META attribute in an undoable way.
     * <p>
     * This calls the other update() with isSignificant=true.
     *
     * @param graph The graph to update.
     * @param undoRedoManager The UndoRedo.Manager for the graph.
     * @param stateAttr The attribute id of the find_state attribute.
     * @param name The name of the undoable edit.
     *
     * @throws InterruptedException if the process is canceled.
     */
    public void update(final Graph graph, final UndoRedo.Manager undoRedoManager,
            final int stateAttr, final String name) throws InterruptedException {
        update(graph, undoRedoManager, stateAttr, name, false);
    }

    /**
     * Update a graph's find state meta-attribute with this instance in an
     * undoable way.
     * <p>
     * To make find state undoable, the graph must be updated within a
     * GraphUndoableEdit. This is a convenience method to do that.
     *
     * @param graph The graph to update.
     * @param undoRedoManager The UndoRedo.Manager for the graph.
     * @param stateAttr The attribute id of the find_state attribute.
     * @param name The name of the undoable edit.
     * @param isSignificant Is this a significant edit?
     *
     * @throws InterruptedException if the process is canceled.
     */
    public void update(final Graph graph, final UndoRedo.Manager undoRedoManager, final int stateAttr, final String name, final boolean isSignificant) throws InterruptedException {
        WritableGraph wg = graph.getWritableGraph(name, isSignificant);
        try {
            wg.setObjectValue(stateAttr, 0, this);
        } finally {
            wg.commit();
        }
    }

    @Override
    public String toString() {
        return "FindState{" + "type=" + type + ", isAny=" + isAny + ", rules=" + rules + ", isHeld=" + isHeld + '}';
    }

}
