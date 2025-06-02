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
package au.gov.asd.tac.constellation.views.tableview.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.TableColumn;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class UserTablePreferencesNGTest {

    private static final String JSON_RESOURCE = "resources/table-preferences.json";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void serialization() throws IOException {
        final JsonNode serialization = OBJECT_MAPPER.valueToTree(fixture());
        final JsonNode expected = OBJECT_MAPPER.readTree(
                new FileInputStream(getClass().getResource(JSON_RESOURCE).getPath()));
        
        assertThat(expected).usingRecursiveComparison().isEqualTo(serialization);
    }

    @Test
    public void deserialization() throws IOException {
        final UserTablePreferences deserialization = OBJECT_MAPPER.readValue(new FileInputStream(getClass().getResource(JSON_RESOURCE).getPath()),
                UserTablePreferences.class);

        assertEquals(fixture(), deserialization);
    }

    @Test
    public void equality() {
        final Pair<String, TableColumn.SortType> black = ImmutablePair.of("BLACK", TableColumn.SortType.ASCENDING);
        final Pair<String, TableColumn.SortType> red = ImmutablePair.of("RED", TableColumn.SortType.ASCENDING);

        EqualsVerifier.forClass(UserTablePreferences.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .withPrefabValues(Pair.class, red, black)
                .verify();
    }

    @Test
    public void getSortColumn() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setSortByColumn(ImmutablePair.of("DEF", TableColumn.SortType.DESCENDING));

        assertEquals("DEF", userTablePreferences.getSortColumn());

        userTablePreferences.setSortByColumn(null);

        assertNull(userTablePreferences.getSortColumn());
    }

    @Test
    public void getSortDirection() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setSortByColumn(ImmutablePair.of("DEF", TableColumn.SortType.DESCENDING));

        assertEquals(TableColumn.SortType.DESCENDING, userTablePreferences.getSortDirection());

        userTablePreferences.setSortByColumn(null);

        assertNull(userTablePreferences.getSortDirection());
    }

    @Test
    public void updateMaxRowsPerPage() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setMaxRowsPerPage(42);

        assertFalse(userTablePreferences.updateMaxRowsPerPage(42));

        assertEquals(42, userTablePreferences.getMaxRowsPerPage());

        assertTrue(userTablePreferences.updateMaxRowsPerPage(150));

        assertEquals(150, userTablePreferences.getMaxRowsPerPage());
    }

    /**
     * Get the user table preferences object that matches what is in the
     * resource JSON file.
     *
     * @return the user table preference fixture
     */
    private UserTablePreferences fixture() {
        final UserTablePreferences userTablePreferences = new UserTablePreferences();

        userTablePreferences.setColumnOrder(List.of("ABC", "DEF"));
        userTablePreferences.setMaxRowsPerPage(42);
        userTablePreferences.setSortByColumn(ImmutablePair.of("GHI", TableColumn.SortType.ASCENDING));

        return userTablePreferences;
    }
}
