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
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.genericjsonio.JsonIO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.NbPreferences;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.SkipException;
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
        jsonIOStaticMock = Mockito.mockStatic(JsonIO.class);
        nbPreferencesStaticMock = Mockito.mockStatic(NbPreferences.class);
        applicationPrefKeysStaticMock = Mockito.mockStatic(ApplicationPreferenceKeys.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
        
        final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> preferences
                = TableViewPreferencesIOUtilities.getPreferences(GraphElementType.VERTEX, 2);
        
        final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> expected = ThreeTuple.create(
                List.of("ABC", "DEF", "ABC", "DEF"), // <- This seems wrong??
                Tuple.create("DEF", TableColumn.SortType.DESCENDING),
                2
        );
        
        assertEquals(expected, preferences);
    }
    
    @Test
    public void getPreferencesSinglePreference() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(
                new FileInputStream(getClass().getResource("resources/transaction-preferences.json").getPath())
        );
        
        jsonIOStaticMock.when(() -> JsonIO.loadJsonPreferences("TableViewPreferences", "transaction-"))
                .thenReturn(jsonNode);
        
        final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> preferences
                = TableViewPreferencesIOUtilities.getPreferences(GraphElementType.TRANSACTION, 2);
        
        final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> expected = ThreeTuple.create(
                List.of("ABC", "DEF"),
                Tuple.create("DEF", TableColumn.SortType.ASCENDING),
                5
        );
        
        assertEquals(expected, preferences);
    }
    
    @Test
    public void getPreferencesNullPrefs() throws IOException {       
        jsonIOStaticMock.when(() -> JsonIO.loadJsonPreferences("TableViewPreferences", "vertex-"))
                .thenReturn(null);
        
        final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> preferences
                = TableViewPreferencesIOUtilities.getPreferences(GraphElementType.VERTEX, 2);
        
        final ThreeTuple<List<String>, Tuple<String, TableColumn.SortType>, Integer> expected = ThreeTuple.create(
                Collections.emptyList(),
                Tuple.create("", TableColumn.SortType.ASCENDING),
                500
        );
        
        assertEquals(expected, preferences);
    }
    
    @Test
    public void savePreferences() throws IOException {
        // TODO Find a better solution for this. Because of this limitation these tests
        //      will not be run on the CI server.
        if (!GraphicsEnvironment.isHeadless()) {
            // Interestingly once you throw the skip exception it doesn't call the tear down class
            // so we need to instantiate the static mocks only once we know we will be running the
            // tests.
            new JFXPanel();
        
            nbPreferencesStaticMock.when(() -> NbPreferences.forModule(ApplicationPreferenceKeys.class))
                    .thenReturn(null);
            applicationPrefKeysStaticMock.when(() -> ApplicationPreferenceKeys.getUserDir(null))
                    .thenReturn(System.getProperty("java.io.tmpdir"));

            final ObservableList<TableColumn<ObservableList<String>, ? extends Object>> columns = mock(ObservableList.class);
            when(columns.size()).thenReturn(4);

            final ObservableList<TableColumn<ObservableList<String>, ? extends Object>> sortOrder = mock(ObservableList.class);
            when(sortOrder.isEmpty()).thenReturn(Boolean.FALSE);

            final TableColumn<ObservableList<String>, ? extends Object> column1 = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, ? extends Object> column2 = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, ? extends Object> column3 = mock(TableColumn.class);
            final TableColumn<ObservableList<String>, ? extends Object> column4 = mock(TableColumn.class);

            when(column1.isVisible()).thenReturn(Boolean.TRUE);
            when(column1.getText()).thenReturn("ABC");

            when(column2.isVisible()).thenReturn(Boolean.TRUE);
            when(column2.getText()).thenReturn("DEF");
            when(column2.getSortType()).thenReturn(TableColumn.SortType.ASCENDING);

            when(column3.isVisible()).thenReturn(Boolean.FALSE);
            when(column3.getText()).thenReturn("GHI");

            when(column4.isVisible()).thenReturn(Boolean.TRUE); // <- This seem wrong. Its dropped.
            when(column4.getText()).thenReturn("JKL");

            doReturn(column1).when(columns).get(0);
            doReturn(column2).when(columns).get(1);
            doReturn(column3).when(columns).get(2);
            doReturn(column4).when(columns).get(3);

            doReturn(column2).when(sortOrder).get(0);

            final TableView<ObservableList<String>> tableView = mock(TableView.class);
            when(tableView.getColumns()).thenReturn(columns);
            when(tableView.getSortOrder()).thenReturn(sortOrder);

            TableViewPreferencesIOUtilities.savePreferences(GraphElementType.TRANSACTION, tableView, 5);

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
}
