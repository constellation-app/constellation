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
package au.gov.asd.tac.constellation.views.tableview.components;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.views.tableview.api.TableDefaultColumns;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test set up of TableDefaultColumns to override the one in the app.
 * @author Andromeda-224
 */
@ServiceProvider(service = TableDefaultColumns.class, position = 1)
public class TestTableDefaultColumnsProvider implements TableDefaultColumns {

    @Override
    public List<GraphAttribute> getDefaultAttributes(Graph graph) {
        List<GraphAttribute> defaultList = new ArrayList<>();
        
        if (graph != null && graph.getSchema() != null) {
            Attribute attribute2 = new GraphAttribute(graph.getReadableGraph(), 2);
            when(graph.getReadableGraph().getAttributeName(2)).thenReturn("Number of Visitors");
            TableColumn<ObservableList<String>, String> column2 = mock(TableColumn.class);
            when(column2.getText()).thenReturn("Text from Column 2");
            defaultList.add((GraphAttribute)attribute2);

            Attribute attribute5 = new GraphAttribute(graph.getReadableGraph(), 5);
            when(graph.getReadableGraph().getAttributeName(5)).thenReturn("personal notes");
            TableColumn<ObservableList<String>, String> column5 = mock(TableColumn.class);
            when(column5.getText()).thenReturn("Text from Column 5");
            defaultList.add((GraphAttribute)attribute5);
        }
        return defaultList;
    }
}
