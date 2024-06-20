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
package au.gov.asd.tac.constellation.graph.visual.plugins.select;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.operations.SetBooleanValuesOperation;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Properties;

/**
 * Class for shared functionality of selection plugins
 *
 * @author Quasar985
 */
public class SelectUtilities {

    private SelectUtilities() {
    }

    /**
     * Function for selecting all vertices in a given graph
     *
     * @param graph the graph to select vertices in
     * @param properties properties that will have parameter be set to number of vertices selected
     * @throws InterruptedException
     */
    public static void selectVertices(final GraphWriteMethods graph, final Properties properties) throws InterruptedException {
        final int vxSelected = VisualConcept.VertexAttribute.SELECTED.get(graph);
        if (vxSelected == Graph.NOT_FOUND) {
            return;
        }

        final SetBooleanValuesOperation selectVerticesOperation = new SetBooleanValuesOperation(graph, GraphElementType.VERTEX, vxSelected);
        final int vertexCount = graph.getVertexCount();
        for (int position = 0; position < vertexCount; position++) {
            final int vertex = graph.getVertex(position);
            selectVerticesOperation.setValue(vertex, true);
        }
        graph.executeGraphOperation(selectVerticesOperation);
        properties.setProperty("vsize", String.valueOf(selectVerticesOperation.size()));
    }

    /**
     * Function for selecting all transactions in a given graph
     *
     * @param graph the graph to select transactions in
     * @param properties properties that will have parameter be set to number of transactions selected
     * @throws InterruptedException
     */
    public static void selectTransactions(final GraphWriteMethods graph, final Properties properties) throws InterruptedException {
        final int txSelected = VisualConcept.TransactionAttribute.SELECTED.get(graph);
        if (txSelected == Graph.NOT_FOUND) {
            return;
        }

        final SetBooleanValuesOperation selectTransactionsOperation = new SetBooleanValuesOperation(graph, GraphElementType.TRANSACTION, txSelected);
        final int transactionCount = graph.getTransactionCount();
        for (int position = 0; position < transactionCount; position++) {
            final int transaction = graph.getTransaction(position);
            selectTransactionsOperation.setValue(transaction, true);
        }
        graph.executeGraphOperation(selectTransactionsOperation);
        properties.setProperty("tsize", String.valueOf(selectTransactionsOperation.size()));
    }

}
