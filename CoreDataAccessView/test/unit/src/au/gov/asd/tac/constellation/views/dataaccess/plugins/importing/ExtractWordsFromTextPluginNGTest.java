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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ContentConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginRegistry;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractWordsFromTextPlugin.USE_REGEX_PARAMETER_ID;
import static au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extract Words From Text Plugin Test.
 *
 * @author canis_majorus
 */
public class ExtractWordsFromTextPluginNGTest {

    private StoreGraph graph;

    @BeforeMethod
    public void setUpMethod() {
        graph = new StoreGraph(SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema());
    }

    /**
     * Test of getType method, of class ExtractWordFromTextPlugin.
     */
    @Test
    public void testGetType() {
        ExtractWordsFromTextPlugin instance = new ExtractWordsFromTextPlugin();
        String expResult = "Import";
        String result = instance.getType();
        assertEquals(result, expResult);
    }

    /**
     * Test of getPosition method, of class ExtractWordsFromTextPlugin.
     */
    @Test
    public void testGetPosition() {
        ExtractWordsFromTextPlugin instance = new ExtractWordsFromTextPlugin();
        int expResult = 1001;
        int result = instance.getPosition();
        assertEquals(result, expResult);
    }

    /**
     * Test of getDescription method, of class ExtractWordFromTextPlugin.
     */
    @Test
    public void testGetDescription() {
        ExtractWordsFromTextPlugin instance = new ExtractWordsFromTextPlugin();
        String expResult = "Extract words from text and add them to the graph";
        String result = instance.getDescription();
        assertEquals(result, expResult);
    }

    /**
     * Test of class ExtractFromContentPlugin.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContent() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentWordListParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentRegexParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentWordLengthParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentWholeWordOnlyParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentWholeWordOnlyFalseParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentSchemaTypesParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentCaseSensitiveParameter() throws InterruptedException, PluginException {
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
                .withParameter(ExtractWordsFromTextPlugin.LOWER_CASE_PARAMETER_ID, false)
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentIgnoreCharsParameter() throws InterruptedException, PluginException {
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentSelectedParameter() throws InterruptedException, PluginException {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
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
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentWithEmptyTransactionType() throws InterruptedException, PluginException {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
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

    /**
     * Test of class ExtractFromContentPlugin with regexOnly parameter.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentRegexOnlyParameter() throws InterruptedException, PluginException {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Node0");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Node1");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "Can you do this?\nHave you  done  that?");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "you\\s+(\\w+)")
                .withParameter(ExtractWordsFromTextPlugin.REGEX_ONLY_PARAMETER_ID, true)
                .executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        final Set<String> newNodes = new HashSet<>();
        newNodes.add(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)));
        newNodes.add(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)));
        assertTrue(newNodes.contains("do"));
        assertTrue(newNodes.contains("done"));
    }

    /**
     * Test of class ExtractFromContentPlugin with regexOnly + lower-case true
     * parameters.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentRegexOnlyLowerCaseTrueParameter() throws InterruptedException, PluginException {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Node0");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Node1");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "Can you do this?\nWill you  DO  that?");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "you\\s+(\\w+)")
                .withParameter(ExtractWordsFromTextPlugin.REGEX_ONLY_PARAMETER_ID, true)
                .withParameter(ExtractWordsFromTextPlugin.LOWER_CASE_PARAMETER_ID, true)
                .executeNow(graph);

        assertEquals(graph.getVertexCount(), 3);
        final Set<String> newNodes = new HashSet<>();
        newNodes.add(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)));
        assertTrue(newNodes.contains("do"));
    }

    /**
     * Test of class ExtractFromContentPlugin with regexOnly + lower-case false
     * parameters.
     *
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testExtractWordFromContentRegexOnlyLowerCaseFalseParameter() throws InterruptedException, PluginException {
        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int transactionContentAttributeId = ContentConcept.TransactionAttribute.CONTENT.ensure(graph);
        final int transactionTypeAttributeId = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);

        graph.getSchema().newGraph(graph);
        final int srcId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, srcId, "Node0");

        final int dstId = graph.addVertex();
        graph.setStringValue(vertexIdentifierAttributeId, dstId, "Node1");

        final int txId = graph.addTransaction(srcId, dstId, true);
        graph.setObjectValue(transactionTypeAttributeId, txId, AnalyticConcept.TransactionType.COMMUNICATION);
        graph.setStringValue(transactionContentAttributeId, txId, "Can you do this?\nWill you  DO  that?");

        PluginExecution.withPlugin(DataAccessPluginRegistry.EXTRACT_WORDS_FROM_TEXT)
                .withParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID, ContentConcept.TransactionAttribute.CONTENT.getName())
                .withParameter(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID, "you\\s+(\\w+)")
                .withParameter(ExtractWordsFromTextPlugin.REGEX_ONLY_PARAMETER_ID, true)
                .withParameter(ExtractWordsFromTextPlugin.LOWER_CASE_PARAMETER_ID, false)
                .executeNow(graph);

        assertEquals(graph.getVertexCount(), 4);
        final Set<String> newNodes = new HashSet<>();
        newNodes.add(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(2)));
        newNodes.add(graph.getStringValue(vertexIdentifierAttributeId, graph.getVertex(3)));
        assertTrue(newNodes.contains("do"));
        assertTrue(newNodes.contains("DO"));
    }

    /**
     * Test updateParameters
     *
     */
    @Test
    public void testUpdateParameters() {
        ExtractWordsFromTextPlugin instance = new ExtractWordsFromTextPlugin();
        PluginParameters parameters = new PluginParameters();

        final PluginParameter<SingleChoiceParameterValue> attributeType = SingleChoiceParameterType.build(ATTRIBUTE_PARAMETER_ID);
        parameters.addParameter(attributeType);

        final PluginParameter<StringParameterValue> textParameter = StringParameterType.build(WORDS_PARAMETER_ID);
        textParameter.setStringValue("text");
        parameters.addParameter(textParameter);

        final PluginParameter<BooleanParameterValue> useRegexParameter = BooleanParameterType.build(USE_REGEX_PARAMETER_ID);
        useRegexParameter.setBooleanValue(true);
        parameters.addParameter(useRegexParameter);

        Graph graph1 = new DualGraph(graph.getSchema(), graph);
        instance.updateParameters(graph1, parameters);

        assertEquals(parameters.getParameters().size(), 3);
        assertTrue(parameters.hasParameter(ExtractWordsFromTextPlugin.ATTRIBUTE_PARAMETER_ID));
        assertEquals(parameters.getParameters().get(ExtractWordsFromTextPlugin.WORDS_PARAMETER_ID).getStringValue(), "text");
        assertEquals(parameters.getParameters().get(ExtractWordsFromTextPlugin.USE_REGEX_PARAMETER_ID).getBooleanValue(), true);
        assertFalse(parameters.hasParameter(ExtractWordsFromTextPlugin.SELECTED_ONLY_PARAMETER_ID));

        parameters = instance.createParameters();
        instance.updateParameters(graph1, parameters);

        assertEquals(parameters.getParameters().size(), 11);
        assertTrue(parameters.hasParameter(ExtractWordsFromTextPlugin.SELECTED_ONLY_PARAMETER_ID));
    }
}
