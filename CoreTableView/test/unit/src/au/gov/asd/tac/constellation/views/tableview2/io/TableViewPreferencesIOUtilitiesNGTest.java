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
package au.gov.asd.tac.constellation.views.tableview2.io;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import au.gov.asd.tac.constellation.views.tableview2.api.UserTablePreferences;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.commons.lang3.tuple.ImmutablePair;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.NbPreferences;
import static org.testng.AssertJUnit.assertEquals;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableViewPreferencesIOUtilitiesNGTest {

    private static MockedStatic<JsonIO> jsonIOStaticMock;
    private static MockedStatic<NbPreferences> nbPreferencesStaticMock;
    private static MockedStatic<ApplicationPreferenceKeys> applicationPrefKeysStaticMock;

    public TableViewPreferencesIOUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();

        jsonIOStaticMock = Mockito.mockStatic(JsonIO.class);
        nbPreferencesStaticMock = Mockito.mockStatic(NbPreferences.class);
        applicationPrefKeysStaticMock = Mockito.mockStatic(ApplicationPreferenceKeys.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
        jsonIOStaticMock.close();
        nbPreferencesStaticMock.close();
        applicationPrefKeysStaticMock.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        jsonIOStaticMock.reset();
        nbPreferencesStaticMock.reset();
        applicationPrefKeysStaticMock.reset();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void getPreferencesMultiplePrefsPicksLast() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/vertex-preferences.json").getPath())
        );

        jsonIOStaticMock.when(() -> JsonIO.loadJsonPreferences("TableViewPreferences", "vertex-"))
                .thenReturn(jsonNode);

        final UserTablePreferences tablePreferences
                = TableViewPreferencesIoProvider.getPreferences(GraphElementType.VERTEX);

        final UserTablePreferences expected = new UserTablePreferences();
        expected.setColumnOrder(List.of("ABC", "DEF"));
        expected.setSortByColumn(ImmutablePair.of("DEF", TableColumn.SortType.DESCENDING));
        expected.setMaxRowsPerPage(500);

        assertEquals(expected, tablePreferences);
    }

    @Test
    public void getPreferencesSinglePreference() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/transaction-preferences.json").getPath())
        );

        jsonIOStaticMock.when(() -> JsonIO.loadJsonPreferences("TableViewPreferences", "transaction-"))
                .thenReturn(jsonNode);

        
        final UserTablePreferences tablepreferences
                = TableViewPreferencesIoProvider.getPreferences(GraphElementType.TRANSACTION);

        final UserTablePreferences expected = new UserTablePreferences();
        expected.setColumnOrder(List.of("ABC", "DEF", "JKL"));
        expected.setSortByColumn(ImmutablePair.of("DEF", TableColumn.SortType.ASCENDING));
        expected.setMaxRowsPerPage(5);

        assertEquals(expected, tablepreferences);
    }

    @Test
    public void getPreferencesNullPrefs() throws IOException {
        jsonIOStaticMock.when(() -> JsonIO.loadJsonPreferences("TableViewPreferences", "vertex-"))
                .thenReturn(null);

        final UserTablePreferences tablepreferences
                = TableViewPreferencesIoProvider.getPreferences(GraphElementType.VERTEX);

        final UserTablePreferences expected = new UserTablePreferences();
        expected.setColumnOrder(Collections.emptyList());
        expected.setSortByColumn(ImmutablePair.of("", TableColumn.SortType.ASCENDING));
        expected.setMaxRowsPerPage(500);

        assertEquals(expected, tablepreferences);
    }

    @Test
    public void savePreferences() throws IOException {
        nbPreferencesStaticMock.when(() -> NbPreferences.forModule(ApplicationPreferenceKeys.class))
                .thenReturn(null);
        applicationPrefKeysStaticMock.when(() -> ApplicationPreferenceKeys.getUserDir(null))
                .thenReturn(System.getProperty("java.io.tmpdir"));

        final TableColumn<ObservableList<String>, ? extends Object> column1 = mock(TableColumn.class);
        final TableColumn<ObservableList<String>, ? extends Object> column2 = mock(TableColumn.class);
        final TableColumn<ObservableList<String>, ? extends Object> column3 = mock(TableColumn.class);
        final TableColumn<ObservableList<String>, ? extends Object> column4 = mock(TableColumn.class);

        when(column1.isVisible()).thenReturn(true);
        when(column1.getText()).thenReturn("ABC");

        when(column2.isVisible()).thenReturn(true);
        when(column2.getText()).thenReturn("DEF");
        when(column2.getSortType()).thenReturn(TableColumn.SortType.ASCENDING);

        when(column3.isVisible()).thenReturn(false);
        when(column3.getText()).thenReturn("GHI");

        when(column4.isVisible()).thenReturn(true);
        when(column4.getText()).thenReturn("JKL");

        final TableView<ObservableList<String>> tableView = mock(TableView.class);
        when(tableView.getColumns()).thenReturn(FXCollections.observableList(List.of(column1, column2, column3, column4)));
        when(tableView.getSortOrder()).thenReturn(FXCollections.observableList(List.of(column2)));

        TableViewPreferencesIoProvider.savePreferences(GraphElementType.TRANSACTION, tableView, 5);

        final ObjectMapper objectMapper = new ObjectMapper();
        final ArrayNode expectedJsonTree = (ArrayNode) objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/transaction-preferences.json").getPath())
        );

        jsonIOStaticMock.verify(() -> JsonIO.saveJsonPreferences(
                eq("TableViewPreferences"),
                any(ObjectMapper.class),
                eq(expectedJsonTree),
                eq("transaction-")
        ));
    }
}
