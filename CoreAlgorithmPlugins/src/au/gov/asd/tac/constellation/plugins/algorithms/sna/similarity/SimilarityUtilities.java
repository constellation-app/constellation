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
package au.gov.asd.tac.constellation.plugins.algorithms.sna.similarity;

import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import java.util.Map;

/**
 * Utilities for adding similarity scores between vertex pairs to the graph.
 *
 * @author canis_majoris
 */
public class SimilarityUtilities {

    private static GraphWriteMethods graph;
    private static int uniqueIdAttribute;
    private static int typeAttribute;
    private static int similarityAttribute;
    
    private SimilarityUtilities() {
        throw new IllegalStateException("Utility class");
    }

    public static void setGraphAndEnsureAttributes(final GraphWriteMethods graph, final SchemaAttribute schemaSimilarityAttribute) {
        SimilarityUtilities.graph = graph;
        uniqueIdAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
        typeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        similarityAttribute = schemaSimilarityAttribute.ensure(graph);
    }

    /**
     * Adds similarity scores to the graph while ensuring there is only ever a
     * single similarity transactions between any pair of nodes.
     *
     * @param graph - graph to add scores to
     * @param scores - the scores of each vertex pair
     * @param schemaSimilarityAttribute - similarity schema attribute to change
     */
    public static void addScoresToGraph(final Map<Tuple<Integer, Integer>, Float> scores) {
        scores.forEach((pair, score) -> addScoreToGraph(pair.getFirst(), pair.getSecond(), score));
    }

    /**
     * Adds a similarity score to the graph while ensuring there is only ever a
     * single similarity transactions between any pair of nodes.
     *
     * @param graph - graph to add scores to
     * @param vertexOne - id of the first vertex
     * @param vertexTwo - id of the second vertex
     * @param score - score to add
     * @param schemaSimilarityAttribute - similarity schema attribute to change
     */
    public static void addScoreToGraph(final int vertexOne, final int vertexTwo, final float score) {
        final int linkId = graph.getLink(vertexOne, vertexTwo);
        if (linkId == GraphConstants.NOT_FOUND) {
            final int transactionId = graph.addTransaction(vertexOne, vertexTwo, false);
            graph.setStringValue(uniqueIdAttribute, transactionId, vertexOne + " == similarity == " + vertexTwo);
            graph.setObjectValue(typeAttribute, transactionId, AnalyticConcept.TransactionType.SIMILARITY);
            graph.setFloatValue(similarityAttribute, transactionId, score);
        } else {
            int similarityTransactionId = GraphConstants.NOT_FOUND;
            for (int transactionPosition = 0; transactionPosition < graph.getLinkTransactionCount(linkId); transactionPosition++) {
                final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
                if (AnalyticConcept.TransactionType.SIMILARITY.equals(graph.getObjectValue(typeAttribute, transactionId))) {
                    similarityTransactionId = transactionId;
                    break;
                }
            }

            if (similarityTransactionId == GraphConstants.NOT_FOUND) {
                similarityTransactionId = graph.addTransaction(vertexOne, vertexTwo, false);
                graph.setStringValue(uniqueIdAttribute, similarityTransactionId, vertexOne + " == similarity == " + vertexTwo);
                graph.setObjectValue(typeAttribute, similarityTransactionId, AnalyticConcept.TransactionType.SIMILARITY);
            }

            graph.setFloatValue(similarityAttribute, similarityTransactionId, score);
        }
    }

    /**
     * Checks for non-similarity transactions on a link for inclusion in
     * analytics.
     *
     * @param graph the graph to check
     * @param linkId the link id to check
     * @return the true if other transaction types found
     */
    public static boolean checkLinkTypes(final GraphWriteMethods graph, final int linkId) {
        final int typeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        boolean found = false;
        for (int transactionPosition = 0; transactionPosition < graph.getLinkTransactionCount(linkId); transactionPosition++) {
            final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
            if (!AnalyticConcept.TransactionType.SIMILARITY.equals(graph.getObjectValue(typeAttribute, transactionId))) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Checks for non-similarity transactions on an edge for inclusion in
     * analytics.
     *
     * @param graph the graph to check
     * @param edgeId the edge id to check
     * @return the true if other transaction types found
     */
    public static boolean checkEdgeTypes(final GraphWriteMethods graph, final int edgeId) {
        final int typeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        boolean found = false;
        for (int transactionPosition = 0; transactionPosition < graph.getEdgeTransactionCount(edgeId); transactionPosition++) {
            final int transactionId = graph.getEdgeTransaction(edgeId, transactionPosition);
            if (!AnalyticConcept.TransactionType.SIMILARITY.equals(graph.getObjectValue(typeAttribute, transactionId))) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Counts similarity transactions on a link.
     *
     * @param graph the graph to check
     * @param linkId the link id to check
     * @return the number of similarity transactions found
     */
    public static int countLinkSimilarityTransactions(final GraphWriteMethods graph, final int linkId) {
        final int typeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        int found = 0;
        for (int transactionPosition = 0; transactionPosition < graph.getLinkTransactionCount(linkId); transactionPosition++) {
            final int transactionId = graph.getLinkTransaction(linkId, transactionPosition);
            if (AnalyticConcept.TransactionType.SIMILARITY.equals(graph.getObjectValue(typeAttribute, transactionId))) {
                found += 1;
            }
        }
        return found;
    }

    /**
     * Counts similarity transactions on an edge.
     *
     * @param graph the graph to check
     * @param edgeId the edge id to check
     * @return the number of similarity transactions found
     */
    public static int countEdgeSimilarityTransactions(final GraphWriteMethods graph, final int edgeId) {
        final int typeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
        int found = 0;
        for (int transactionPosition = 0; transactionPosition < graph.getEdgeTransactionCount(edgeId); transactionPosition++) {
            final int transactionId = graph.getEdgeTransaction(edgeId, transactionPosition);
            if (AnalyticConcept.TransactionType.SIMILARITY.equals(graph.getObjectValue(typeAttribute, transactionId))) {
                found += 1;
            }
        }
        return found;
    }
}
