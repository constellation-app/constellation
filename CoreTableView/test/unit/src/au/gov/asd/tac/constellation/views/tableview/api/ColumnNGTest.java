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

import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ColumnNGTest {

    @Test
    public void init() {
        final Column column = new Column();

        assertNull(column.getAttributeNamePrefix());
        assertNull(column.getAttribute());
        assertNull(column.getTableColumn());

        final String prefix = "source.";
        final GraphAttribute attribute = new GraphAttribute(GraphElementType.VERTEX, "string",
                "COLUMN_A", "Describes all things A's");
        final TableColumn tableColumn = new TableColumn<>("source.COLUMN_A");

        column.setAttributeNamePrefix(prefix);
        column.setAttribute(attribute);
        column.setTableColumn(tableColumn);

        assertSame(prefix, column.getAttributeNamePrefix());
        assertSame(attribute, column.getAttribute());
        assertSame(tableColumn, column.getTableColumn());
    }

    @Test
    public void equality() {
        final TableColumn<ObservableList<String>, String> black
                = new TableColumn<>("BLACK");
        final TableColumn<ObservableList<String>, String> red
                = new TableColumn<>("RED");

        EqualsVerifier.forClass(Column.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .withPrefabValues(TableColumn.class, red, black)
                .verify();
    }

}
