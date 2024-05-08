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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.plugins.PluginException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * SelectionContextMenu Tests. Simulate the user right clicking on different objects
 * in the graph.  Should only get a non empty response when GRAPH is simulated
 *
 * @author CrucisGamma
 */
public class SelectionContextMenuNGTest {

    private StoreGraph graph;
    private DualGraph dgraph;

    public SelectionContextMenuNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    private void generateData() throws InterruptedException {
        graph = new StoreGraph();
    }

    @Test
    public void simulateSelectionContextMenuTest() throws InterruptedException, PluginException {
        generateData();

        final SelectionContextMenu menu = new SelectionContextMenu();

        assertEquals(graph.getVertexCount(), 0);
        assertEquals(graph.getTransactionCount(), 0);

        assertFalse(menu.getItems(graph, GraphElementType.GRAPH, 0).isEmpty());
        assertTrue(menu.getItems(graph, GraphElementType.EDGE, 0).isEmpty());
        assertTrue(menu.getItems(graph, GraphElementType.LINK, 0).isEmpty());
        assertTrue(menu.getItems(graph, GraphElementType.META, 0).isEmpty());
        assertTrue(menu.getItems(graph, GraphElementType.TRANSACTION, 0).isEmpty());

        assertFalse(menu.getIcons(graph, GraphElementType.GRAPH, 0).isEmpty());
        assertTrue(menu.getIcons(graph, GraphElementType.EDGE, 0).isEmpty());
        assertTrue(menu.getIcons(graph, GraphElementType.LINK, 0).isEmpty());
        assertTrue(menu.getIcons(graph, GraphElementType.META, 0).isEmpty());
        assertTrue(menu.getIcons(graph, GraphElementType.TRANSACTION, 0).isEmpty());
    }
}
