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

        final BitSet vxCopied = getSelectedGraphElements(GraphElementType.VERTEX, rg);
        final BitSet txCopied = getSelectedGraphElements(GraphElementType.TRANSACTION, rg);

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
     * Get the given graph elements selected on the graph
     *
     * @param elementType the graph element type
     * @param rg the graph to read from
     * 
     * @return A BitSet of selected graph elements.
     */
    private static BitSet getSelectedGraphElements(final GraphElementType elementType, final GraphReadMethods rg) {
        final int elementCount = elementType.getElementCount(rg);
        final BitSet selectedGraphElements = new BitSet(elementCount);
        
        final int selectedAttribute = rg.getAttribute(elementType, "selected");
        if (selectedAttribute != Graph.NOT_FOUND) {
            for (int i = 0; i < elementCount; i++) {
                final int currElement = elementType.getElement(rg, i);
                if (rg.getBooleanValue(selectedAttribute, currElement)) {
                    selectedGraphElements.set(currElement, true);
                }
            }
        }
        return selectedGraphElements;
    }
}
