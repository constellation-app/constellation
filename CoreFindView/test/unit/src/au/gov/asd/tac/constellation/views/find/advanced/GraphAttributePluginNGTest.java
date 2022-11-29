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
package au.gov.asd.tac.constellation.views.find.advanced;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.util.ArrayList;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the Graph Attribute Plugin Class
 *
 * @author Delphinus8821
 */
public class GraphAttributePluginNGTest {

    private final ArrayList<Attribute> attributes = new ArrayList<>();
    private long attributeModificationCounter;
    private final GraphElementType type = GraphElementType.VERTEX;
    private int attrX, attrY;
    private int vAttrId, tAttrId;
    private StoreGraph graph;

    public GraphAttributePluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        graph = new StoreGraph();

        attrX = VisualConcept.VertexAttribute.X.ensure(graph);
        attrY = VisualConcept.VertexAttribute.Y.ensure(graph);
        vAttrId = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        tAttrId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of read method, of class GraphAttributePlugin.
     *
     * @throws InterruptedException
     * @throws PluginException
     * @throws Exception
     */
    @Test
    public void testRead() throws InterruptedException, PluginException, Exception {
        GraphAttributePlugin instance = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
        PluginExecution.withPlugin(instance).executeNow(graph);
        assertEquals(instance.getAttributes(), attributes);
        assertEquals(instance.getAttributeModificationCounter(), 4);
    }

    /**
     * Test of getAttributes method, of class GraphAttributePlugin.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testGetAttributes() throws InterruptedException, PluginException {
        GraphAttributePlugin instance = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
        PluginExecution.withPlugin(instance).executeNow(graph);
        ArrayList result = instance.getAttributes();
        assertEquals(result, attributes);
    }

    /**
     * Test of getAttributeModificationCounter method, of class
     * GraphAttributePlugin.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testGetAttributeModificationCounter() throws InterruptedException, PluginException {
        GraphAttributePlugin instance = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
        PluginExecution.withPlugin(instance).executeNow(graph);
        long result = instance.getAttributeModificationCounter();
        assertEquals(result, 4);
    }

}
