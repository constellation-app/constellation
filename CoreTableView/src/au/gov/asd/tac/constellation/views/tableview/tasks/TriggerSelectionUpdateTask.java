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
package au.gov.asd.tac.constellation.views.tableview.tasks;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;

/**
 * Triggers an update of the selected rows in the table using the current
 * selection in the graph as the source of truth. If the table is in "Selected
 * Only" mode this will do nothing.
 *
 * @author formalhaunt
 * @see Table#updateSelection(Graph, TableViewState)
 */
public class TriggerSelectionUpdateTask implements Runnable {

    private final TablePane tablePane;
    private final Graph graph;
    private final TableViewState tableViewState;

    /**
     * Creates a new trigger selection update task.
     *
     * @param tablePane the pane that holds the table
     * @param graph the current graph
     * @param tableViewState the current table state
     */
    public TriggerSelectionUpdateTask(final TablePane tablePane, final Graph graph, final TableViewState tableViewState) {
        this.tablePane = tablePane;
        this.graph = graph;
        this.tableViewState = tableViewState;
    }

    @Override
    public void run() {
        tablePane.getTable().updateSelection(graph, tableViewState);
    }
}
