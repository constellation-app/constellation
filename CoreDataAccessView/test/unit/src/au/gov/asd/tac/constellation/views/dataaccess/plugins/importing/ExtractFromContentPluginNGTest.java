/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginRegistry;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extract From Content Plugin Test.
 *
 * @author canis_majorus
 */
public class ExtractFromContentPluginNGTest {

    private StoreGraph graph;

    public ExtractFromContentPluginNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() {
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of class ExtractFromContentPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContent() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "word");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .executeNow(graph);

        // Assert that there are now 3 nodes on the graph and one of them has the name word and type Word
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "word");
        assertEquals(graph.getObjectValue(vertexTypeAttributeId, graph.getVertex(2)), AnalyticConcept.VertexType.WORD);
    }

    /**
     * Test of class ExtractFromContentPlugin with word list parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentWordListParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "#Word1 #Word2");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "word1")
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "word1");
    }

    /**
     * Test of class ExtractFromContentPlugin with regex parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentRegexParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "bag beg big bog bug");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "b[eo]g")
                .withParameter(ExtractWordsFromTextPlugin.USE_REGEX_PARAMETER_ID, true)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "beg");
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)), "bog");
    }

    /**
     * Test of class ExtractFromContentPlugin with minimum word length
     * parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentWordLengthParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "Word1 Word11 Word111");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.MIN_WORD_LENGTH_PARAMETER_ID, 6)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "word11");
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)), "word111");
    }

    /**
     * Test of class ExtractFromContentPlugin with whole word only parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentWholeWordOnlyParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "word or word");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "or")
                .withParameter(ExtractWordsFromTextPlugin.WHOLE_WORDS_ONLY_PARAMETER_ID, true)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "or");
    }

    /**
     * Test of class ExtractFromContentPlugin with whole word only parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentWholeWordOnlyFalseParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "word1 or word2");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "or")
                .withParameter(ExtractWordsFromTextPlugin.WHOLE_WORDS_ONLY_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 5);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "word1");
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)), "or");
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(4)), "word2");
    }

    /**
     * Test of class ExtractFromContentPlugin with schema types parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentSchemaTypesParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "1234567890");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.SCHEMA_TYPES_PARAMETER_ID, true)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "1234567890");
        assertEquals(graph.getObjectValue(vertexTypeAttributeId, graph.getVertex(2)), AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER);
    }

    /**
     * Test of class ExtractFromContentPlugin with case sensitive parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentCaseSensitiveParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "#Word1 $Word2");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.CASE_INSENSITIVE_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "Word1");
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)), "Word2");
    }

    /**
     * Test of class ExtractFromContentPlugin with ignore special characters
     * parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentIgnoreCharsParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "#Word1 $Word2");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.REMOVE_SPECIAL_CHARS_PARAMETER_ID, false)
                .executeNow(graph);

        // Assert that only one node was added, word1
        assertEquals(graph.getVertexCount(), 4);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "#word1");
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)), "$word2");
    }

    /**
     * Test of class ExtractFromContentPlugin.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentSelectedParameter() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId1 = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId1, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId1, "word1");

        final int txId2 = graph.addTransaction(dstId, srcId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId2, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId2, "word2");
        graph.setBooleanValue(transactionSelectedAttributeId, txId2, true);

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.SELECTED_ONLY_PARAMETER_ID, true)
                .executeNow(graph);

        // Assert that there are now 3 nodes on the graph and one of them has the name word and type Word
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "word2");
    }

    /**
     * Test of class ExtractFromContentPlugin with an empty transaction type
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractWordFromContentWithEmptyTransactionType() throws Exception {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int vertexTypeAttributeId = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        final int transactionSelectedAttributeId = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Test1");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Test2");

        final int txId1 = graph.addTransaction(srcId, dstId, true);
        graph.setStringValue(transactionContentAttributeId, txId1, "word1");

        final int txId2 = graph.addTransaction(dstId, srcId, true);
        graph.setStringValue(transactionContentAttributeId, txId2, "word2");
        graph.setBooleanValue(transactionSelectedAttributeId, txId2, true);

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.SELECTED_ONLY_PARAMETER_ID, true)
                .executeNow(graph);

        // Assert that there are now 3 nodes on the graph and one of them has the name word and type Word
        assertEquals(graph.getVertexCount(), 3);
        assertEquals(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)), "word2");
    }
}
