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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.algorithms.AlgorithmPluginRegistry;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author canis_majorus
 */
public class LevenshteinDistancePluginNGTest {

    private StoreGraph graph;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() {
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of class LevenshteinDistancePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLevenshteinDistance() throws Exception {

        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionLDAttributeId = SnaConcept.TransactionAttribute.LEVENSHTEIN_DISTANCE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final LevenshteinDistancePlugin instance = new LevenshteinDistancePlugin();
        PluginExecution.withPlugin(instance)
                .withParameter(LevenshteinDistancePlugin.ATTRIBUTE_PARAMETER_ID, graph.getAttributeName(vertexIdentifierAttributeId))
                .withParameter(LevenshteinDistancePlugin.MAXIMUM_DISTANCE_PARAMETER_ID, 1)
                .withParameter(LevenshteinDistancePlugin.CASE_INSENSITIVE_PARAMETER_ID, false)
                .withParameter(LevenshteinDistancePlugin.SELECTED_ONLY_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert there is a similarity link between the nodes with distance = 1
        assertEquals(graph.getTransactionCount(), 1);
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getTransaction(0)), AnalyticConcept.TransactionType.SIMILARITY.getName());
        assertEquals(graph.getIntValue(transactionLDAttributeId, graph.getTransaction(0)), 1);
    }

    /**
     * Test of class LevenshteinDistancePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLevenshteinDistanceMaxDistance() throws Exception {

        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "test2");

        final LevenshteinDistancePlugin instance = new LevenshteinDistancePlugin();
        PluginExecution.withPlugin(instance)
                .withParameter(LevenshteinDistancePlugin.ATTRIBUTE_PARAMETER_ID, graph.getAttributeName(vertexIdentifierAttributeId))
                .withParameter(LevenshteinDistancePlugin.MAXIMUM_DISTANCE_PARAMETER_ID, 1)
                .withParameter(LevenshteinDistancePlugin.CASE_INSENSITIVE_PARAMETER_ID, false)
                .withParameter(LevenshteinDistancePlugin.SELECTED_ONLY_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert there is a similarity link between the nodes with distance = 1
        assertEquals(graph.getTransactionCount(), 0);
    }

    /**
     * Test of class LevenshteinDistancePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLevenshteinDistanceMaxDistanceZero() throws Exception {

        final int vertexLabelAttributeId = VisualConcept.VertexAttribute.LABEL.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexLabelAttributeId, srcId, "Test");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexLabelAttributeId, dstId, "Test");

        final LevenshteinDistancePlugin instance = new LevenshteinDistancePlugin();
        PluginExecution.withPlugin(instance)
                .withParameter(LevenshteinDistancePlugin.ATTRIBUTE_PARAMETER_ID, graph.getAttributeName(vertexLabelAttributeId))
                .withParameter(LevenshteinDistancePlugin.MAXIMUM_DISTANCE_PARAMETER_ID, 0)
                .withParameter(LevenshteinDistancePlugin.CASE_INSENSITIVE_PARAMETER_ID, false)
                .withParameter(LevenshteinDistancePlugin.SELECTED_ONLY_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert there is a similarity link between the nodes with distance = 1
        assertEquals(graph.getTransactionCount(), 1);
    }

    /**
     * Test of class LevenshteinDistancePlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLevenshteinDistanceCaseInsensitive() throws Exception {

        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionLDAttributeId = SnaConcept.TransactionAttribute.LEVENSHTEIN_DISTANCE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "test2");

        PluginExecution.withPlugin(AlgorithmPluginRegistry.LEVENSHTEIN_DISTANCE)
                .withParameter(LevenshteinDistancePlugin.ATTRIBUTE_PARAMETER_ID, graph.getAttributeName(vertexIdentifierAttributeId))
                .withParameter(LevenshteinDistancePlugin.MAXIMUM_DISTANCE_PARAMETER_ID, 1)
                .withParameter(LevenshteinDistancePlugin.CASE_INSENSITIVE_PARAMETER_ID, true)
                .withParameter(LevenshteinDistancePlugin.SELECTED_ONLY_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert there is a similarity link between the nodes with distance = 1
        assertEquals(graph.getTransactionCount(), 1);
        assertEquals(graph.getStringValue(transactionTypeAttributeId, graph.getTransaction(0)), AnalyticConcept.TransactionType.SIMILARITY.getName());
        assertEquals(graph.getIntValue(transactionLDAttributeId, graph.getTransaction(0)), 1);
    }
}
