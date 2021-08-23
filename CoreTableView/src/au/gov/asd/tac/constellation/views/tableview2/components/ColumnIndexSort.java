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
package au.gov.asd.tac.constellation.views.tableview2.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import java.util.Comparator;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

/**
 *
 * @author formalhaunt
 */
public class ColumnIndexSort implements Comparator<ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>>> {
    
    private final TableViewState state;
    
    public ColumnIndexSort(final TableViewState state) {
        this.state = state;
    }
    
    @Override
    public int compare(final ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>> columnTuple1,
                       final ThreeTuple<String, Attribute, TableColumn<ObservableList<String>, String>> columnTuple2) {
        final int c1Index = state.getColumnAttributes()
                        .indexOf(Tuple.create(columnTuple1.getFirst(), columnTuple1.getSecond()));
        final int c2Index = state.getColumnAttributes()
                .indexOf(Tuple.create(columnTuple2.getFirst(), columnTuple2.getSecond()));
        final String c1Type = columnTuple1.getFirst();
        final String c2Type = columnTuple2.getFirst();

        if (c1Index != -1 && c2Index != -1) {
            return Integer.compare(c1Index, c2Index);
        } else if (c1Index != -1 && c2Index == -1) {
            return -1;
        } else if (c1Index == -1 && c2Index != -1) {
            return 1;
        } else if (c1Type.equals(GraphRecordStoreUtilities.SOURCE) && c2Type.equals(GraphRecordStoreUtilities.TRANSACTION)
                || c1Type.equals(GraphRecordStoreUtilities.SOURCE) && c2Type.equals(GraphRecordStoreUtilities.DESTINATION)
                || c1Type.equals(GraphRecordStoreUtilities.TRANSACTION) && c2Type.equals(GraphRecordStoreUtilities.DESTINATION)) {
            return -1;
        } else if (c1Type.equals(GraphRecordStoreUtilities.DESTINATION) && c2Type.equals(GraphRecordStoreUtilities.TRANSACTION)
                || c1Type.equals(GraphRecordStoreUtilities.DESTINATION) && c2Type.equals(GraphRecordStoreUtilities.SOURCE)
                || c1Type.equals(GraphRecordStoreUtilities.TRANSACTION) && c2Type.equals(GraphRecordStoreUtilities.SOURCE)) {
            return 1;
        } else {
            final String c1Name = columnTuple1.getSecond().getName();
            final String c2Name = columnTuple2.getSecond().getName();
            return c1Name.compareTo(c2Name);
        }
    }
    
}
