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
package au.gov.asd.tac.constellation.views.tableview2.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TableColumn;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openide.util.Pair;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TablePreferencesNGTest {
    private static final String JSON_RESOURCE = "resources/table-preferences.json";
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Test
    public void serialization() throws IOException {
        final JsonNode serialization = OBJECT_MAPPER.valueToTree(fixture());
        
        final JsonNode expected = OBJECT_MAPPER.readTree(
                new FileInputStream(getClass().getResource(JSON_RESOURCE).getPath()));
        
        assertEquals(expected, serialization);
    }
    
    @Test
    public void deserialization() throws IOException {
        final TablePreferences deserialization = OBJECT_MAPPER.readValue(
                new FileInputStream(getClass().getResource(JSON_RESOURCE).getPath()), TablePreferences.class);

        assertEquals(fixture(), deserialization);
    }
    
    @Test
    public void equality() {
        EqualsVerifier.forClass(TablePreferences.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
    
    @Test
    public void getSortColumn() {
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setSortByColumn(Map.of(
                "ABC", TableColumn.SortType.ASCENDING,
                "DEF", TableColumn.SortType.DESCENDING
        ));
        
        assertEquals("DEF", tablePreferences.getSortColumn());
        
        tablePreferences.setSortByColumn(Collections.emptyMap());
        
        assertNull(tablePreferences.getSortColumn());
    }
    
    @Test
    public void getSortDirection() {
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setSortByColumn(Map.of(
                "ABC", TableColumn.SortType.ASCENDING,
                "DEF", TableColumn.SortType.DESCENDING
        ));
        
        assertEquals(TableColumn.SortType.DESCENDING, tablePreferences.getSortDirection());
        
        tablePreferences.setSortByColumn(Collections.emptyMap());
        
        assertNull(tablePreferences.getSortDirection());
    }
    
    @Test
    public void getSort() {
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setSortByColumn(Map.of(
                "ABC", TableColumn.SortType.ASCENDING,
                "DEF", TableColumn.SortType.DESCENDING
        ));
        
        assertEquals(ImmutablePair.of("DEF", TableColumn.SortType.DESCENDING), tablePreferences.getSort());
        
        tablePreferences.setSortByColumn(Collections.emptyMap());
        
        assertNull(tablePreferences.getSort());
    }
    
    private TablePreferences fixture() {
        final TablePreferences tablePreferences = new TablePreferences();
        
        tablePreferences.setColumnOrder(List.of("ABC", "DEF"));
        tablePreferences.setMaxRowsPerPage(42);
        tablePreferences.setSortByColumn(Map.of("GHI", TableColumn.SortType.ASCENDING));
        
        return tablePreferences;
    }
}
