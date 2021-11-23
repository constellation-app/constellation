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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.BitSet;

/**
 *
 * @author arcturus
 */
public class GraphCopyUtilities {

    private GraphCopyUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Copies the selected graph nodes and transactions, placing them on the
     * CONSTELLATION specific clipboard.
     *
     * @param rg The graph to copy elements from.
     * @return An array of length two containing a BitSet of the copied vertex
     * ids, followed by the copied transaction ids.
     */
    public static BitSet[] copySelectedGraphElementsToClipboard(final GraphReadMethods rg) {
        // Copy the selected elements of the graph.
        final BitSet transactionEndPoints = new BitSet();
        final RecordStore copy = GraphRecordStoreUtilities.copySelectedTransactions(rg, null, transactionEndPoints);
        GraphRecordStoreUtilities.copySelectedVertices(rg, copy, transactionEndPoints);

        // Put the copy on the clipboard.
        final Transferable transferable = new RecordStoreTransferable(copy);
        final Clipboard cb = ConstellationClipboardOwner.getConstellationClipboard();
        cb.setContents(transferable, ConstellationClipboardOwner.getOwner());

        final GraphElementType nodes = GraphElementType.VERTEX;
        final int nodesCount = nodes.getElementCount(rg);
        final int nodeSelectedAttribute = rg.getAttribute(nodes, "selected");
        final GraphElementType transactions = GraphElementType.TRANSACTION;
        final int transactionsCount = transactions.getElementCount(rg);
        final int transactionSelectedAttribute = rg.getAttribute(transactions, "selected");

        final BitSet vxCopied = getVxCopied(nodesCount, nodeSelectedAttribute, nodes, rg);
        final BitSet txCopied = getSelectedTransactions(transactionsCount, transactionSelectedAttribute, transactions, rg);

        return new BitSet[]{vxCopied, txCopied};
    }

    /**
     * Copy text from the graph and place it on the system clipboard.
     * <p>
     * //The values of a chosen attribute by the schema. Each schema decides
     * which attribute to use as its alias.
     *
     * @param rg The graph to copy text from.
     *
     * @return The "\n" separated values of the attributes of the selected
     * vertices.
     */
    public static String copyGraphTextToSystemClipboard(final GraphReadMethods rg) {
        final StringBuilder buf = new StringBuilder();
        final int aliasAttr = rg.getSchema().getVertexAliasAttribute(rg);
        //make sure the schema returns an attribute that exists
        if (aliasAttr != Graph.NOT_FOUND) {
            final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(rg);
            //make sure selected attr exists
            if (selectedAttr != Graph.NOT_FOUND) {
                for (int i = 0; i < rg.getVertexCount(); i++) {
                    // if the current vertex is selected
                    int vxId = rg.getVertex(i);
                    if (rg.getBooleanValue(selectedAttr, vxId)) {
                        buf.append(rg.getStringValue(aliasAttr, vxId));
                        buf.append(SeparatorConstants.NEWLINE);
                    }
                }
            }
        }

        final String text = buf.toString();
        final StringSelection ss = new StringSelection(text);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(ss, ConstellationClipboardOwner.getOwner());
        return text;
    }

    /**
     * Get the vertices selected on the graph
     *
     * @param nodesCount
     * @param nodeSelectedAttribute
     * @param nodes
     * @param rg
     * @return A BitSet of selected vertices.
     */
    private static BitSet getVxCopied(int nodesCount, int nodeSelectedAttribute, GraphElementType nodes, final GraphReadMethods rg) {
        BitSet vxCopied = new BitSet(nodesCount);
        if (nodeSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < nodesCount; i++) {
                int currElement = nodes.getElement(rg, i);
                boolean selected = rg.getBooleanValue(nodeSelectedAttribute, currElement);
                if (selected) {
                    vxCopied.set(currElement, true);
                }

            }
        }
        return vxCopied;
    }

    /**
     * Get the transactions selected on the graph.
     *
     * @param transactionsCount
     * @param transactionSelectedAttribute
     * @param transactions
     * @param rg
     * @return A BitSet of selected transactions.
     */
    private static BitSet getSelectedTransactions(int transactionsCount, int transactionSelectedAttribute, GraphElementType transactions, final GraphReadMethods rg) {
        BitSet txCopied = new BitSet(transactionsCount);
        if (transactionSelectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < transactionsCount; i++) {
                int currElement = transactions.getElement(rg, i);
                boolean selected = rg.getBooleanValue(transactionSelectedAttribute, currElement);
                if (selected) {
                    txCopied.set(currElement, true);
                }

            }
        }
        return txCopied;
    }

}
