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
package au.gov.asd.tac.constellation.views.conversationview;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.utilities.GraphIndexUtilities;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * The DefaultConversationMessageProvider creates messages based on the current selection in a number of ways: 1) If a
 * single vertex is selected then messages are created for all adjacent transactions. 2) If 2 vertices are selected then
 * messages are created for all transactions between them 3) If transactions are selected and there is a common vertex
 * in all of them then messages are created for each.
 *
 * @author sirius
 */
public class DefaultConversationMessageProvider implements ConversationMessageProvider {

    private int totalMessageCount = 0;
    private int currentVertex = -1;

    @Override
    public void getMessages(final GraphReadMethods graph, final List<ConversationMessage> messages) {
        assert !SwingUtilities.isEventDispatchThread();
        messages.clear();
        if (graph == null) {
            return; // Null graph means no messages.
        }
        // Get content when only one vertex is selected 
        final int vertexSelectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (vertexSelectedAttribute != Graph.NOT_FOUND) {
            totalMessageCount = 0;
            final GraphIndexResult selectedVertices = GraphIndexUtilities.filterElements(graph, vertexSelectedAttribute, true);
            final int vertex = selectedVertices.getNextElement();
            if (vertex != Graph.NOT_FOUND) {
                currentVertex = vertex;
                final int secondVertex = selectedVertices.getNextElement();
                if (secondVertex == Graph.NOT_FOUND) {
                    final int transactionCount = graph.getVertexTransactionCount(vertex);
                    for (int position = 0; position < transactionCount; position++) {
                        final int transaction = graph.getVertexTransaction(vertex, position);
                        if (graph.getTransactionDirection(transaction) != Graph.UNDIRECTED) {
                            final int sender = graph.getTransactionSourceVertex(transaction);
                            final ConversationSide conversationSide = sender == vertex ? ConversationSide.LEFT : ConversationSide.RIGHT;
                            final ConversationMessage message = new ConversationMessage(transaction, sender, conversationSide);
                            
                            totalMessageCount++;
                            messages.add(message);
                        }
                    }
                    return;
                }
            } 
        }

        // Get content when only one transaction is selected
        final int transactionSelectedAttribute = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        if (transactionSelectedAttribute != Graph.NOT_FOUND) {
            totalMessageCount = 0;
            final GraphIndexResult transactionResult = GraphIndexUtilities.filterElements(graph, transactionSelectedAttribute, true);

            int transactionCount = 0;
            int[] transactionPositions = new int[graph.getTransactionCount()];

            int vertexA = -1;
            int vertexB = -1;
            int transaction = transactionResult.getNextElement();
            while (transaction != Graph.NOT_FOUND) {
                if (graph.getTransactionDirection(transaction) != Graph.FLAT) {
                    final int src = graph.getTransactionSourceVertex(transaction);
                    final int dst = graph.getTransactionDestinationVertex(transaction);
                    if (vertexA < 0 && vertexB < 0) {
                        vertexA = src;
                        vertexB = dst;
                    } else {
                        if (vertexA != src && vertexA != dst) {
                            vertexA = -1;
                        }
                        if (vertexB != src && vertexB != dst) {
                            vertexB = -1;
                        }
                        if (vertexA < 0 && vertexB < 0) {
                            return;
                        }
                    }
                    transactionPositions[transactionCount++] = graph.getTransactionPosition(transaction);
                }
                transaction = transactionResult.getNextElement();
            }

            final int leftSender = Math.max(vertexA, vertexB);
            totalMessageCount = transactionCount;
            for (int i = 0; i < transactionCount; i++) {
                transaction = graph.getTransaction(transactionPositions[i]);
                if (graph.getTransactionDirection(transaction) != Graph.UNDIRECTED) {
                    final int sender = graph.getTransactionSourceVertex(transaction);
                    final ConversationSide conversationSide = sender == leftSender ? ConversationSide.LEFT : ConversationSide.RIGHT;
                    final ConversationMessage message = new ConversationMessage(transaction, sender, conversationSide);
                    messages.add(message);
                }
            }
        }
    }

    @Override
    public int getTotalMessageCount() {
        return totalMessageCount;
    }
}
