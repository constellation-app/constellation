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
package au.gov.asd.tac.constellation.views.tableview.utilities;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.io.Serializable;
import java.util.Comparator;

/**
 * A {@link Comparator} for the {@link Table#columnIndex} sorted list.
 *
 * @author formalhaunt
 */
public class ColumnIndexSort implements Comparator<Column>, Serializable {

    private static final long serialVersionUID = 1;

    private final TableViewState state;

    /**
     * Creates a new comparator for the column index list.
     *
     * @param state the current table state
     */
    public ColumnIndexSort(final TableViewState state) {
        this.state = state;
    }

    /**
     * Compares two columns from the column index. Ordering is done as follows.
     * <ul>
     * <li>
     * If both columns are in the state and known then they are ordered based on
     * the states column order.
     * </li>
     * If one is known to the state and the other is not then the one that is
     * know is placed before the one that is unknown.
     * </li>
     * <li>
     * If neither are know to state then SOURCE columns are before TRANSACTION
     * columns and TRANSACTION columns are before DESTINATION columns.
     * </li>
     * <li>
     * If neither are known by state and at least one of the types is NOT
     * SOURCE, TRANSACTION or DESTINATION, then they are ordered by the column
     * name.
     * </li>
     * </ul>
     *
     * @param column1 the first column to be compared
     * @param column2 the second column to be compared
     * @return a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(final Column column1, final Column column2) {
        final int column1Index = state.getColumnAttributes()
                .indexOf(Tuple.create(column1.getAttributeNamePrefix(), column1.getAttribute()));
        final int column2Index = state.getColumnAttributes()
                .indexOf(Tuple.create(column2.getAttributeNamePrefix(), column2.getAttribute()));

        final String column1AttrPrefix = column1.getAttributeNamePrefix();
        final String column2AttrPrefix = column2.getAttributeNamePrefix();

        if (column1Index != -1 && column2Index != -1) {
            return Integer.compare(column1Index, column2Index);
        } else if (column1Index != -1 && column2Index == -1) {
            return -1;
        } else if (column1Index == -1 && column2Index != -1) {
            return 1;
        } else if (column1AttrPrefix.equals(GraphRecordStoreUtilities.SOURCE) && column2AttrPrefix.equals(GraphRecordStoreUtilities.TRANSACTION)
                || column1AttrPrefix.equals(GraphRecordStoreUtilities.SOURCE) && column2AttrPrefix.equals(GraphRecordStoreUtilities.DESTINATION)
                || column1AttrPrefix.equals(GraphRecordStoreUtilities.TRANSACTION) && column2AttrPrefix.equals(GraphRecordStoreUtilities.DESTINATION)) {
            return -1;
        } else if (column1AttrPrefix.equals(GraphRecordStoreUtilities.DESTINATION) && column2AttrPrefix.equals(GraphRecordStoreUtilities.TRANSACTION)
                || column1AttrPrefix.equals(GraphRecordStoreUtilities.DESTINATION) && column2AttrPrefix.equals(GraphRecordStoreUtilities.SOURCE)
                || column1AttrPrefix.equals(GraphRecordStoreUtilities.TRANSACTION) && column2AttrPrefix.equals(GraphRecordStoreUtilities.SOURCE)) {
            return 1;
        } else {
            final String column1Name = column1.getAttribute().getName();
            final String column2Name = column2.getAttribute().getName();

            return column1Name.compareTo(column2Name);
        }
    }

}
