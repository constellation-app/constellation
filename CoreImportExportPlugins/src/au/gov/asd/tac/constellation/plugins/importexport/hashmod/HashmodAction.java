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
package au.gov.asd.tac.constellation.plugins.importexport.hashmod;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/*
 * Action to allow the user to set a hashmod for a graph window
 */
@ActionID(
        category = "Edit",
        id = "au.gov.asd.tac.constellation.graph.utilities.hashmod.HashmodAction")
@ActionRegistration(displayName = "#CTL_HashmodAction", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Tools", position = 0)
@Messages({
    "CTL_HashmodAction=Add Hashmod",
    "MSG_Title=Edit graph with a hashmod",
    "MSG_Text=Hashmod text"
})
public final class HashmodAction implements ActionListener {

    private final GraphNode context;
    private static final Logger LOGGER = Logger.getLogger(HashmodAction.class.getName());

    public HashmodAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Graph graph = context.getGraph();
        final Hashmod hashmod = new Hashmod();

        final HashmodPanel hashmodPanel = new HashmodPanel(hashmod);
        final DialogDescriptor dialog = new DialogDescriptor(hashmodPanel, Bundle.MSG_Title(), true, e -> {
            if ("OK".equals(e.getActionCommand())) {
                final Hashmod hashmod1 = hashmodPanel.getHashmod();
                final boolean isChainedHashmods = hashmodPanel.isChainedHashmods();
                final boolean createAttributes = hashmodPanel.isCreateAttributesSelected();
                final Hashmod[] chainedHashmods = hashmodPanel.getChainedHashmods();
                final int numChainedHashmods = hashmodPanel.numChainedHashmods();
                final boolean createVertices = hashmodPanel.isCreateVerticesSelected();
                final boolean createTransactions = hashmodPanel.isCreateTransactionsSelected();

                hashmodPanel.setAttributeNames(hashmod1.getCSVKey(), hashmod1.getCSVHeader(1), hashmod1.getCSVHeader(2));

                PluginExecution.withPlugin(
                        new AddHashmodPlugin(isChainedHashmods, createAttributes, createVertices,
                                createTransactions, chainedHashmods, numChainedHashmods, hashmod1)).executeLater(graph);      
            }
        });
        DialogDisplayer.getDefault().notify(dialog);
    }

    private static void run(final GraphWriteMethods wg, final PluginInteraction interaction, final Hashmod hashmod, final boolean createVertices, final boolean createTransactions, final boolean setPrimary, final boolean createAttributes) throws InterruptedException, PluginException {

        if (wg != null && hashmod != null) {
            if (hashmod.getNumberCSVDataColumns() < 2) {
                interaction.notify(PluginNotificationLevel.ERROR, "CSV file requires at least one key and one value attribute");
                return;
            }
        } else {
            return;
        }

        final int[] attributeValues = new int[hashmod.getNumberCSVDataColumns() + 1];
        final int[] csvValues = new int[hashmod.getNumberCSVDataColumns() + 1];
        final int[] transactionAttributeValues = new int[hashmod.getNumberCSVTransactionColumns() + 1];
        final int[] fromNodeValues = new int[hashmod.getNumberCSVTransactionColumns() + 1];
        final int[] transactionCSVValues = new int[hashmod.getNumberCSVTransactionColumns() + 1];
        String nextAttr;
        int i = 0;
        int attrCount = 0;
        int transAttrCount = 0;
        while ((nextAttr = hashmod.getCSVHeader(i)) != null) {
            final int nextAttribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, nextAttr);

            if ((createVertices || createAttributes || createTransactions) && StringUtils.isNotBlank(nextAttr)) {
                final String[] attributeName = nextAttr.split("\\.");
                String newAttributeType = StringAttributeDescription.ATTRIBUTE_NAME;

                if (attributeName.length >= 2 && nextAttribute != Graph.NOT_FOUND) {
                    newAttributeType = wg.getAttributeType(nextAttribute);
                }

                if (attributeName.length >= 2 && AttributeRegistry.getDefault().getAttributes().get(newAttributeType) == null) {
                    newAttributeType = attributeName[attributeName.length - 1];
                }

                if (createVertices || createAttributes) {
                    //check if it's an existing attribute
                    final int existingAttribute = wg.getAttribute(GraphElementType.VERTEX, nextAttr);

                    // do not add new attribute if not createAttributes and
                    // does not exist in graph
                    if (!createAttributes && existingAttribute == Graph.NOT_FOUND) {
                        LOGGER.log(Level.WARNING, "Attribute {0} not added", nextAttr);
                    } else {
                        final int newAttribute = wg.addAttribute(GraphElementType.VERTEX, newAttributeType, nextAttr, nextAttr, "", null);
                        attributeValues[attrCount] = newAttribute;
                        csvValues[attrCount] = i;
                        attrCount++;
                    }
                }

                if (createTransactions && StringUtils.isNotEmpty(hashmod.getTransactionAttribute(nextAttr))) {
                    final String transactionAttributeName = hashmod.getTransactionAttribute(nextAttr);
                    final int newTransactionAttribute = wg.addAttribute(GraphElementType.TRANSACTION, newAttributeType, transactionAttributeName, transactionAttributeName, "", null);
                    if (newTransactionAttribute != Graph.NOT_FOUND) {
                        transactionAttributeValues[transAttrCount] = newTransactionAttribute;
                        transactionCSVValues[transAttrCount] = i;
                        fromNodeValues[transAttrCount] = wg.getAttribute(GraphElementType.VERTEX, transactionAttributeName);
                        transAttrCount++;
                    }
                }
            }
            i++;
        }

        if (createAttributes && attrCount < 1) {
            interaction.notify(PluginNotificationLevel.ERROR, "Requires at least one key and one value attributes in the header of the CSV file.  Check upper/lower case and for typos");
            return;
        }

        final int vxCount = wg.getVertexCount();
        final Map<String, Integer> keys = hashmod.getCSVKeys();
        String keyValue;
        int numberSuccessful = 0;

        final int[] vxOrder = new int[vxCount];
        int vxPos = 0;
        if (vxCount > 0 && createVertices) {
            final BitSet vertices = HashmodUtilities.vertexBits(wg);
            for (int vxId = vertices.nextSetBit(0); vxId >= 0; vxId = vertices.nextSetBit(vxId + 1)) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                vxOrder[vxPos++] = vxId;
                vertices.clear(vxId);
            }

            for (int j = 0; j < vxPos; j++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int vxId = vxOrder[j];

                keyValue = wg.getObjectValue(attributeValues[0], vxId);
                if (keys.containsKey(keyValue.toUpperCase())) {
                    numberSuccessful++;
                    for (i = 1; i < attrCount; i++) {
                        // do not add attributes if value is default 0
                        if (attributeValues[i] != 0) {
                            wg.setStringValue(attributeValues[i], j, hashmod.getValueFromKeyAndIndex(keyValue, csvValues[i], keys.get(keyValue.toUpperCase())));
                        }
                    }

                    if (createVertices) {
                        keys.put(keyValue, 1);
                    }
                }
            }

            interaction.notify(PluginNotificationLevel.WARNING, "Successfully updated " + numberSuccessful + "/" + vxPos + " nodes");
        }

        if (createVertices) {
            numberSuccessful = 0;
            for (final Entry<String, Integer> entry : keys.entrySet()) {
                final int newVertexId = wg.addVertex();

                for (i = 0; i < attrCount; i++) {
                    // do not add attributes if value is default 0
                    if (attributeValues[i] != 0) {
                        wg.setStringValue(attributeValues[i], newVertexId, hashmod.getValueFromKeyAndIndex(entry.getKey(), csvValues[i], entry.getValue()));
                    }
                }

                numberSuccessful++;
            }
            if (setPrimary) {
                wg.setPrimaryKey(GraphElementType.VERTEX, attributeValues[0]);
            }

            interaction.notify(PluginNotificationLevel.WARNING, "Successfully added in " + numberSuccessful + " new nodes");
        }

        if (createTransactions) {
            final BitSet vertices = HashmodUtilities.vertexBits(wg);
            vxPos = 0;

            final int vxCount2 = wg.getVertexCount();
            final int[] vxOrder2 = new int[vxCount2];
            for (int vxId = vertices.nextSetBit(0); vxId >= 0; vxId = vertices.nextSetBit(vxId + 1)) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                vxOrder2[vxPos++] = vxId;
                vertices.clear(vxId);
            }

            numberSuccessful = 0;
            for (int transaction = 0; transaction < hashmod.getNumberCSVTransactionColumns(); transaction++) {
                String attribute1 = hashmod.getFirstColumnOfTransaction(transaction);
                String attribute2 = hashmod.getSecondColumnOfTransaction(transaction);

                final int transaction1Attribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, attribute1);
                final int transaction2Attribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, attribute2);

                if (transaction1Attribute != Graph.NOT_FOUND && transaction2Attribute != Graph.NOT_FOUND) {
                    for (int j = 0; j < vxPos; j++) {
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                        final int vxId = vxOrder2[j];

                        String attr1Value = wg.getObjectValue(transaction1Attribute, vxId);
                        for (int k = 0; k < vxPos && attr1Value != null && attr1Value.length() > 0; k++) {
                            if (Thread.interrupted()) {
                                throw new InterruptedException();
                            }
                            final int vx2Id = vxOrder2[k];

                            String attr2Value = wg.getObjectValue(transaction2Attribute, vx2Id);
                            if (attr1Value.equals(attr2Value) && vxId != vx2Id) {
                                final int newTransactionId = wg.addTransaction(vxId, vx2Id, false);

                                for (i = 0; i < transAttrCount; i++) {
                                    final String theVal = wg.getStringValue(fromNodeValues[i], vxId);
                                    wg.setStringValue(transactionAttributeValues[i], newTransactionId, theVal);
                                }

                                numberSuccessful++;
                            }
                        }
                    }
                }
            }

            interaction.notify(PluginNotificationLevel.WARNING, "Successfully added in " + numberSuccessful + " new transactions");
        }
        PluginExecutor.startWith(ArrangementPluginRegistry.GRID_COMPOSITE)
                        .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                        .executeNow(wg);
    }

    /**
     * Plugin to create and add a hashmod to the graph
     */
    @PluginInfo(pluginType = PluginType.CREATE, tags = {PluginTags.CREATE})
    public static class AddHashmodPlugin extends SimpleEditPlugin {

        final boolean isChainedHashmods;
        final boolean createAttributes;
        final boolean createVertices;
        final boolean createTransactions;
        final Hashmod[] chainedHashmods;
        final int numChainedHashmods;
        final Hashmod hashmod1;

        public AddHashmodPlugin(final boolean isChainedHashmods, final boolean createAttributes, final boolean createVertices,
                final boolean createTransactions, final Hashmod[] chainedHashmods, final int numChainedHashmods, final Hashmod hashmod1) {

            this.isChainedHashmods = isChainedHashmods;
            this.createAttributes = createAttributes;
            this.createVertices = createVertices;
            this.createTransactions = createTransactions;
            this.chainedHashmods = chainedHashmods;
            this.numChainedHashmods = numChainedHashmods;
            this.hashmod1 = hashmod1;
        }

        @Override
        public String getName() {
            return "Add Hashmod";
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            if (hashmod1 != null) {
                HashmodAction.run(wg, interaction, hashmod1, createVertices, createTransactions, true, createAttributes);
            }
            if (isChainedHashmods && numChainedHashmods >= 2) {
                for (int i = 1; i < numChainedHashmods; i++) {
                    HashmodAction.run(wg, interaction, chainedHashmods[i], false, false, false, createAttributes);
                }
            }
        }
    }
}
