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
package au.gov.asd.tac.constellation.graph.reporting;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 *
 * @author Auriga2
 */
public class UndoRedoReport {

    private final String graphId;
    private final long startTime;
    private String actionType;
    private String actionDescription;


    /**
     * Creates a new UndoRedoReport with the specified graph.
     *
     * @param graph the graph
     */
    public UndoRedoReport(final Graph graph) {
        this(graph.getId());
    }

    /**
     * Creates a new UndoRedoReport with the specified graph id.
     *
     * @param graphId the graphId.
     */
    public UndoRedoReport(final String graphId) {
        this.graphId = graphId;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Returns the time that this undo or redo action performed.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the graphId of the graph associated with this UndoRedoReport.
     */
    public String getGraphId() {
        return graphId;
    }

    /**
     * Returns the action type, whether it's undo or redo
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Sets the action type, whether it's undo or redo
     */
    public void setActionType(final String actionType) {
        this.actionType = actionType;
    }

    /**
     * Returns the action description of this undo or redo event
     */
    public String getActionDescription() {
        return actionDescription;
    }

    /**
     * Sets the action description of this undo or redo event
     */
    public void setActionDescription(final String actionDescription) {
        this.actionDescription = actionDescription;
    }
}
