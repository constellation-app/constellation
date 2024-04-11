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
import au.gov.asd.tac.constellation.views.tableview.components.ProgressBar;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;

/**
 * Triggers an update of the rows that the table is currently displaying using
 * the elements in the graph as the source of truth. Depending on the selected
 * only mode status and element type settings different groups of elements will
 * be added to the table.
 *
 * @author formalhaunt
 * @see Table#updateData(Graph, TableViewState, ProgressBar)
 */
public class TriggerDataUpdateTask implements Runnable {

    private final TablePane tablePane;
    private final Graph graph;
    private final TableViewState tableViewState;

    /**
     * Creates a new trigger data update task.
     *
     * @param tablePane the pane that holds the table
     * @param graph the current graph
     * @param tableViewState the current table state
     */
    public TriggerDataUpdateTask(final TablePane tablePane, final Graph graph, final TableViewState tableViewState) {
        this.tablePane = tablePane;
        this.graph = graph;
        this.tableViewState = tableViewState;
    }

    @Override
    public void run() {
        tablePane.getTable().updateData(graph, tableViewState, tablePane.getProgressBar());
    }
}
