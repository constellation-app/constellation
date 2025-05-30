/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.table.TableFilter;

/**
 * A {@link Runnable} that updates the table with the new rows, triggers a
 * pagination update and displays the resulting new page.
 *
 * @author formalhaunt
 */
public class UpdateDataTask implements Runnable {

    private final Table table;

    private final List<ObservableList<String>> rows;

    private final CountDownLatch updateDataLatch;

    private boolean interrupted = false;

    /**
     * Creates a new update data task.
     *
     * @param table the table being updated
     * @param rows the new rows to update the table with
     */
    public UpdateDataTask(final Table table, final List<ObservableList<String>> rows) {
        this.table = table;
        this.rows = rows;

        this.updateDataLatch = new CountDownLatch(1);
    }

    /**
     * Updates the backing row list with the new list of rows and then sets them
     * to the table. Adds the filter and then triggers a pagination update that
     * will cause the new rows to be displayed.
     * <p/>
     * Whilst the update is happening the table listeners are removed and then
     * once the update is complete, they are re-added.
     * <p/>
     * Once complete the update latch will be decremented indicating the data
     * update has completed.
     */
    @Override
    public void run() {
        if (isInterrupted()) {
            return;
        }

        // remove listeners so they are not triggered during the update
        table.getSelectedProperty().removeListener(table.getTableSelectionListener());
        table.getTableView().getSelectionModel().getSelectedItems()
                .removeListener(table.getSelectedOnlySelectionListener());
        getActiveTableReference().getSortedRowList().comparatorProperty().unbind();

        // set the new rows to the backing list
        getActiveTableReference().setSortedRowList(new SortedList<>(FXCollections.observableArrayList(rows)));

        // need to set the table items to the whole list here so that the filter
        // picks up the full list of options to filter before we paginate
        table.getTableView().setItems(FXCollections.observableArrayList(getActiveTableReference().getSortedRowList()));

        // add user defined filter to the table
        final TableFilter<ObservableList<String>> filter = TableFilter.forTableView(table.getTableView()).lazy(true).apply();
        filter.setSearchStrategy((filterTerm, cellText) -> {
            try {
                return StringUtils.startsWithIgnoreCase(cellText, filterTerm);
            } catch (final Exception ex) {
                return false;
            }
        });
        filter.getFilteredList().predicateProperty().addListener((v, o, n) -> {
            getActiveTableReference().getSortedRowList().comparatorProperty().unbind();
            final List<ObservableList<String>> filteredRowList = new FilteredList<>(
                    FXCollections.observableArrayList(rows),
                    filter.getFilteredList().getPredicate()
            );
            getActiveTableReference().setSortedRowList(new SortedList<>(
                    FXCollections.observableArrayList(filteredRowList)));

            getActiveTableReference().updatePagination(getActiveTableReference().getUserTablePreferences().getMaxRowsPerPage(),
                    getActiveTableReference().getSortedRowList(), table.getParentComponent());
        });

        // Trigger a pagination update so the table only shows the current page. This
        // is an expensive task so it should not be done if a new update has come in
        // and superseeded/cancelled this one.
        if (!isInterrupted()) {
            getActiveTableReference().updatePagination(getActiveTableReference().getUserTablePreferences().getMaxRowsPerPage(),
                    getActiveTableReference().getSortedRowList(), table.getParentComponent());
        }
        updateDataLatch.countDown();

        // restore listeners now the update is complete
        table.getTableView().getSelectionModel().getSelectedItems()
                .addListener(table.getSelectedOnlySelectionListener());
        table.getSelectedProperty().addListener(table.getTableSelectionListener());
    }

    /**
     * Gets the tasks update latch. When this latch reaches zero it means that
     * the data update is complete.
     *
     * @return the tasks update latch
     */
    public CountDownLatch getUpdateDataLatch() {
        return updateDataLatch;
    }

    /**
     * Gets the new rows that will be added to the table. These are ALL the rows
     * not just the ones that will be displayed on the current page.
     *
     * @return all the rows in the table
     */
    public List<ObservableList<String>> getRows() {
        return rows;
    }

    /**
     * Convenience method for accessing the active table reference.
     *
     * @return the active table reference
     */
    private ActiveTableReference getActiveTableReference() {
        return table.getParentComponent().getActiveTableReference();
    }

    public synchronized boolean isInterrupted() {
        return interrupted;
    }

    public synchronized void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }
}
