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
package au.gov.asd.tac.constellation.views.tableview.api;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Interface for table preferences.
 * @author Andromeda-224
 */
public interface TablePreferences {

    final String ALL_COLUMNS = "Show All Columns";
    final String DEFAULT_COLUMNS = "Show Default Columns";
    final String KEY_COLUMNS = "Show Key Columns";
    final String NO_COLUMNS = "Show No Columns";
    final Set<GraphAttribute> defaultColumns = new HashSet<>();
    
    /**
     * Set default values, excluding default table columns.
     * graph.
     */
    public void setDefaults();
    
    /**
     * Set the default table columns that will be used with default column
     * visibility setting and new tables.
     * @param graph current graph
     */
    public void setDefaultColumns(Graph graph);
    
    /**
     * Gets the default table columns.
     * @return List of default GraphAttribute table columns.
     */
    public List<GraphAttribute> getDefaultColumns();
    
    
    /**
     * Gets the preferred column visibility.
     * @return the preferred column visibility setting
     */
    public String getColumnVisibility();
    
    /**
     * Set preferred column visibility.
     * @param aColumnSetting the columnVisibility to set
     */
    public void setColumnVisibility(String aColumnSetting);

    /**
     * Gets the column to sort by.
     * @return
     */
    public Pair<String, TableColumn.SortType> getSortByColumn();
    
    /**
     * Sets the sort by column.
     * @param sortByColumn 
     */
    public void setSortByColumn(final Pair<String, TableColumn.SortType> sortByColumn);

    /**
     * Gets just the sort column in a thread safe way, or null if no sort is
     * set.
     *
     * @return the sort column or null if no sort is set
     */
    public String getSortColumn();

    /**
     * Gets just the sort direction in a thread safe way, or null if no sort is
     * set.
     *
     * @return the sort direction or null if no sort is set
     */
    public TableColumn.SortType getSortDirection();
    
    /**
     * Set the columns order.
     * @param columnOrder list of strings of columns in the order preferred.
     */
    public void setColumnOrder(final List<String> columnOrder);

    /**
     * Gets the column order.
     * @return list of columns in the order required.
     */
    public List<String> getColumnOrder();
    
    /**
     * Set the table page size (max number of rows per page).
     * @param pageSize int of rows per page
     */
    public void setMaxRowsPerPage(final int pageSize);
    
    /**
     * Get the table page size (max rows per page).
     * @return int of rows per page
     */
    public int getMaxRowsPerPage();

    /**
     * Thread safe update of the max rows per page. Performs a check to see if
     * the new value is different to the current one and if it is performs the
     * update.
     *
     * @param pageSize the new page size to set
     * @return true if the page size was changed, false otherwise
     */
    boolean updateMaxRowsPerPage(final int pageSize);
    
    /**
     * Set key columns to display when preferred column visibility is set to
     * KEY_COLUMNS.
     * @param keyColumns set of key columns
     */
    public void setKeyColumns(final Set<GraphAttribute> keyColumns);
    
    /**
     * Get the key columns to display when preferred column visibility is set to
     * KEY_COLUMNS.
     * @return set of key GraphAttributes.
     */
    public Set<GraphAttribute> getKeyColumns();
    
}
