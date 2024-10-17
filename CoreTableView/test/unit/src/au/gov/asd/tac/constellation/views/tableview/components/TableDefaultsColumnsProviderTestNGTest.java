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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.views.tableview.api.TableDefaultColumns;
import au.gov.asd.tac.constellation.views.tableview.api.TableDefaultsColumnsProvider;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.util.Lookup;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class TableDefaultsColumnsProviderTestNGTest {

    private Graph graph;
    private ReadableGraph readableGraph;
    private Schema schema;

    public TableDefaultsColumnsProviderTestNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = mock(Graph.class);
        readableGraph = mock(ReadableGraph.class);
        schema = mock(Schema.class);

        when(graph.getReadableGraph()).thenReturn(readableGraph);
        when(readableGraph.getPrimaryKey(GraphElementType.VERTEX)).thenReturn(new int[]{1});
        when(readableGraph.getPrimaryKey(GraphElementType.TRANSACTION)).thenReturn(new int[]{2});

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test when passing empty graph
     */
    @Test
    public void testEmptyGraph() {

        final TableDefaultColumns tableDefaultColumnsProvider = Lookup.getDefault().lookup(TableDefaultColumns.class);

        List<GraphAttribute> ids = tableDefaultColumnsProvider.getDefaultAttributes(null).stream().toList();

        assertTrue(ids.isEmpty());
    }

    /**
     * Test when passing in graph with empty schema
     */
    @Test
    public void testEmptyGraphSchema() {

        final TableDefaultsColumnsProvider tableDefaultColumnsProvider = 
                Lookup.getDefault().lookup(TableDefaultsColumnsProvider.class);

        when(graph.getSchema()).thenReturn(null);
        List<GraphAttribute> ids = 
                tableDefaultColumnsProvider.getDefaultAttributes(graph).stream().toList();

        assertTrue(ids.isEmpty());
    }

    /**
     * Test when passing in graph with empty schema
     */
    @Test
    public void testGraphAttributeCounts() {

        final TableDefaultsColumnsProvider tableDefaultColumnsProvider = 
                Lookup.getDefault().lookup(TableDefaultsColumnsProvider.class);
        when(graph.getSchema()).thenReturn(schema);
        List<GraphAttribute> ids = 
                tableDefaultColumnsProvider.getDefaultAttributes(graph).stream().toList();

        assertTrue(ids.size() == 2);
    }
}
