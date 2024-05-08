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
package au.gov.asd.tac.constellation.graph.utilities.attribute;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Schema Attribute Utilities Test.
 *
 * @author arcturus
 */
public class AttributeUtilitiesNGTest {

    public AttributeUtilitiesNGTest() {
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

    /**
     * Test of getRegisteredAttributeIdsFromGraph method, of class
     * AttributeUtilities.
     */
    @Test
    public void testGetRegisteredAttributeIdsFromGraphWithZeroNodes() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final GraphElementType graphElementType = GraphElementType.VERTEX;
        final Map<String, Integer> expResult = new HashMap<>();
        final Map<String, Integer> result = AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, graphElementType);
        assertEquals(result, expResult);
    }

    @Test
    public void testGetRegisteredAttributeIdsFromGraphWithOneNode() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int vx0 = graph.addVertex();
        graph.getSchema().completeVertex(graph, vx0);

        final GraphElementType graphElementType = GraphElementType.VERTEX;
        final Map<String, Integer> expResult = new HashMap<>();
        expResult.put(VisualConcept.VertexAttribute.LABEL.getName(), graph.getAttribute(graphElementType, VisualConcept.VertexAttribute.LABEL.getName()));
        expResult.put(VisualConcept.VertexAttribute.IDENTIFIER.getName(), graph.getAttribute(graphElementType, VisualConcept.VertexAttribute.IDENTIFIER.getName()));
        expResult.put(AnalyticConcept.VertexAttribute.RAW.getName(), graph.getAttribute(graphElementType, AnalyticConcept.VertexAttribute.RAW.getName()));
        expResult.put(AnalyticConcept.VertexAttribute.TYPE.getName(), graph.getAttribute(graphElementType, AnalyticConcept.VertexAttribute.TYPE.getName()));
        expResult.put(VisualConcept.VertexAttribute.COLOR.getName(), graph.getAttribute(graphElementType, VisualConcept.VertexAttribute.COLOR.getName()));
        expResult.put(VisualConcept.VertexAttribute.FOREGROUND_ICON.getName(), graph.getAttribute(graphElementType, VisualConcept.VertexAttribute.FOREGROUND_ICON.getName()));
        expResult.put(VisualConcept.VertexAttribute.BACKGROUND_ICON.getName(), graph.getAttribute(graphElementType, VisualConcept.VertexAttribute.BACKGROUND_ICON.getName()));
        expResult.put(VisualConcept.VertexAttribute.COLORBLIND_LAYER.getName(), graph.getAttribute(graphElementType, VisualConcept.VertexAttribute.COLORBLIND_LAYER.getName()));

        final Map<String, Integer> result = AttributeUtilities.getRegisteredAttributeIdsFromGraph(graph, graphElementType);
        assertEquals(result, expResult);
    }

    @Test
    public void testGetDateTimeAttributes() {
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());

        final Set<String> typesUsedByGraph = AttributeUtilities.getDateTimeAttributes(new DualGraph(graph, false), GraphElementType.TRANSACTION);
        assertEquals(typesUsedByGraph.size(), 5);
        assertTrue(typesUsedByGraph.contains(TemporalConcept.TransactionAttribute.FIRST_SEEN.getName()));
        assertTrue(typesUsedByGraph.contains(TemporalConcept.TransactionAttribute.LAST_SEEN.getName()));
        assertTrue(typesUsedByGraph.contains(TemporalConcept.TransactionAttribute.DATETIME.getName()));
        assertTrue(typesUsedByGraph.contains(TemporalConcept.TransactionAttribute.CREATED.getName()));
        assertTrue(typesUsedByGraph.contains(TemporalConcept.TransactionAttribute.MODIFIED.getName()));
    }

    @Test
    public void testGetTypesUsedByGraph() {
        final int vx0, vx1, vx2, vx3;
        final StoreGraph graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
        final int typeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);

        vx0 = graph.addVertex();
        graph.setObjectValue(typeId, vx0, AnalyticConcept.VertexType.ORGANISATION);

        vx1 = graph.addVertex();
        graph.setObjectValue(typeId, vx1, AnalyticConcept.VertexType.ORGANISATION);

        vx2 = graph.addVertex();
        graph.setObjectValue(typeId, vx2, AnalyticConcept.VertexType.DOCUMENT);

        vx3 = graph.addVertex();
        graph.setStringValue(typeId, vx3, "Foo");

        final Set<String> typesUsedByGraph = AttributeUtilities.getTypesUsedByGraph(new DualGraph(graph, false));
        assertEquals(typesUsedByGraph.size(), 3);
        assertTrue(typesUsedByGraph.contains(AnalyticConcept.VertexType.ORGANISATION.toString()));
        assertTrue(typesUsedByGraph.contains(AnalyticConcept.VertexType.DOCUMENT.toString()));
        assertTrue(typesUsedByGraph.contains("Foo"));
    }
}
