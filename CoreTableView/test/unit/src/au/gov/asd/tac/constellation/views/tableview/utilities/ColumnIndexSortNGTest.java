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
package au.gov.asd.tac.constellation.views.tableview.utilities;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.views.tableview.api.Column;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ColumnIndexSortNGTest {

    @Test
    public void compare() {
        final TableViewState state = new TableViewState();

        final Attribute attribute1 = mock(Attribute.class);
        final Attribute attribute2 = mock(Attribute.class);
        final Attribute attribute3 = mock(Attribute.class);
        final Attribute attribute4 = mock(Attribute.class);
        final Attribute attribute5 = mock(Attribute.class);
        final Attribute attribute6 = mock(Attribute.class);
        final Attribute attribute7 = mock(Attribute.class);

        final Tuple<String, Attribute> column1 = Tuple.create(GraphRecordStoreUtilities.SOURCE, attribute1);
        final Tuple<String, Attribute> column2 = Tuple.create(GraphRecordStoreUtilities.SOURCE, attribute2);
        final Tuple<String, Attribute> column3 = Tuple.create(GraphRecordStoreUtilities.SOURCE, attribute3);
        final Tuple<String, Attribute> column4 = Tuple.create(GraphRecordStoreUtilities.TRANSACTION, attribute4);
        final Tuple<String, Attribute> column5 = Tuple.create(GraphRecordStoreUtilities.DESTINATION, attribute5);
        final Tuple<String, Attribute> column6 = Tuple.create("random", attribute6);
        final Tuple<String, Attribute> column7 = Tuple.create("random", attribute7);

        state.setColumnAttributes(List.of(column1, column2));

        final ColumnIndexSort sort = new ColumnIndexSort(state);

        // Both columns are in the state. Order based on index position in the state column attrs
        assertTrue(sort.compare(createColumn(column1), createColumn(column2)) < 0);
        assertTrue(sort.compare(createColumn(column2), createColumn(column1)) > 0);

        // Only one column is in the state
        assertTrue(sort.compare(createColumn(column1), createColumn(column3)) < 0);
        assertTrue(sort.compare(createColumn(column3), createColumn(column1)) > 0);

        // Neither in the state - source is before everything, transaction before destination
        assertTrue(sort.compare(createColumn(column3), createColumn(column4)) < 0);
        assertTrue(sort.compare(createColumn(column3), createColumn(column5)) < 0);
        assertTrue(sort.compare(createColumn(column4), createColumn(column5)) < 0);

        assertTrue(sort.compare(createColumn(column5), createColumn(column3)) > 0);
        assertTrue(sort.compare(createColumn(column5), createColumn(column4)) > 0);
        assertTrue(sort.compare(createColumn(column4), createColumn(column3)) > 0);

        // Neither are in state and neither type is known. Default to the column name
        when(attribute6.getName()).thenReturn("a");
        when(attribute7.getName()).thenReturn("b");

        assertTrue(sort.compare(createColumn(column6), createColumn(column7)) < 0);
        assertTrue(sort.compare(createColumn(column7), createColumn(column6)) > 0);
    }

    /**
     * The table column is not used in the comparator so can be set to null.
     *
     * @param column the column whose first part will be the attributeNamePrefix
     * and the second part, the attribute
     * @return the new column
     */
    private Column createColumn(final Tuple<String, Attribute> column) {
        return new Column(column.getFirst(), column.getSecond(), null);
    }
}
