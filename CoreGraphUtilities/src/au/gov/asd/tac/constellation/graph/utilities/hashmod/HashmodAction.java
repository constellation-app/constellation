/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.hashmod;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AttributeRegistry;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;


/*
 * action to allow the user to set a hashmod for a graph window
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

    public HashmodAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Graph graph = context.getGraph();
        final Hashmod hashmod = new Hashmod();

        final HashmodPanel hashmodPanel = new HashmodPanel(hashmod);
        final DialogDescriptor dialog = new DialogDescriptor(hashmodPanel, Bundle.MSG_Title(), true, e -> {
            if (e.getActionCommand().equals("OK")) {
                final Hashmod hashmod1 = hashmodPanel.getHashmod();
                final Boolean isChainedHashmods = hashmodPanel.isChainedHashmods();
                final boolean createAttributes = hashmodPanel.getCreateAttributes();
                final Hashmod[] chainedHashmods = hashmodPanel.getChainedHashmods();
                final int numChainedHashmods = hashmodPanel.numChainedHashmods();
                final Boolean createNonMatchingKeysVertexes = hashmodPanel.getCreateVertexes();
                hashmodPanel.setAttributeNames(hashmod1.getCSVKey(), hashmod1.getCSVHeader(1), hashmod1.getCSVHeader(2));

                PluginExecution.withPlugin(new SimpleEditPlugin(Bundle.CTL_HashmodAction()) {
                    @Override
                    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                        if (hashmod1 != null) {
                            HashmodAction.run(wg, interaction, hashmod1, createNonMatchingKeysVertexes, true, createAttributes);
                        }
                        if (isChainedHashmods && numChainedHashmods >= 2) {
                            for (int i = 1; i < numChainedHashmods; i++) {
                                HashmodAction.run(wg, interaction, chainedHashmods[i], false, false, createAttributes);
                            }
                        }
                    }
                }).executeLater(graph);
            }
        });
        DialogDisplayer.getDefault().notify(dialog);
    }

    private static void run(final GraphWriteMethods wg, final PluginInteraction interaction, final Hashmod hashmod, final Boolean createAllKeys, final Boolean setPrimary, final Boolean createAttributes) throws InterruptedException {

        if (wg != null && hashmod != null) {
            if (hashmod.getNumberCSVColumns() < 2) {
                interaction.notify(PluginNotificationLevel.ERROR, "CSV file requires at least one key and one value attribute");
                return;
            }
        } else {
            return;
        }

        final int[] attributeValues = new int[hashmod.getNumberCSVColumns() + 1];
        final int[] csvValues = new int[hashmod.getNumberCSVColumns() + 1];
        String nextAttr;
        int i = 0;
        int attrCount = 0;
        while ((nextAttr = hashmod.getCSVHeader(i)) != null) {
            final int nextAttribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, nextAttr);
            if (nextAttribute != Graph.NOT_FOUND) {
                attributeValues[attrCount] = nextAttribute;
                csvValues[attrCount] = i;
                attrCount++;
            } else if (createAttributes && StringUtils.isNotBlank(nextAttr)) {
                final String[] attributeName = nextAttr.split("\\.");
                String newAttributeType = StringAttributeDescription.ATTRIBUTE_NAME;

                if (attributeName.length >= 2) {
                    if (AttributeRegistry.getDefault().getAttributes().get(attributeName[attributeName.length - 1]) != null) {
                        newAttributeType = attributeName[attributeName.length - 1];
                    }
                }

                final int newAttribute = wg.addAttribute(GraphElementType.VERTEX, newAttributeType, nextAttr, nextAttr, "", null);
                if (newAttribute != Graph.NOT_FOUND) {
                    attributeValues[attrCount] = newAttribute;
                    csvValues[attrCount] = i;
                    attrCount++;
                }
            }
            i++;
        }

        if (attrCount < 2) {
            interaction.notify(PluginNotificationLevel.ERROR, "Requires at least one key and one value attributes in the header of the CSV file.  Check upper/lower case and for typos");
            return;
        }

        final int vxCount = wg.getVertexCount();
        final HashMap<String, Integer> keys = hashmod.getCSVKeys();
        String keyValue;
        int numberSuccessful = 0;

        if (vxCount > 0) {
            final BitSet vertices = HashmodUtilities.vertexBits(wg);
            int vxPos = 0;
            final int[] vxOrder = new int[vxCount];
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
                        wg.setStringValue(attributeValues[i], j, hashmod.getValueFromKey(keyValue, csvValues[i]));
                    }

                    if (createAllKeys) {
                        keys.put(keyValue, 1);
                    }
                }
            }

            interaction.notify(PluginNotificationLevel.WARNING, "Successfully updated " + numberSuccessful + "/" + vxPos + " nodes");
        }

        if (createAllKeys) {
            numberSuccessful = 0;
            for (String keyVal : keys.keySet()) {
                if (keys.get(keyVal) == 0) {
                    int newVertex = wg.addVertex();

                    for (i = 0; i < attrCount; i++) {
                        wg.setStringValue(attributeValues[i], newVertex, hashmod.getValueFromKey(keyVal, csvValues[i]));
                    }
                    numberSuccessful++;
                }
            }

            if (setPrimary) {
                wg.setPrimaryKey(GraphElementType.VERTEX, attributeValues[0]);
            }
            interaction.notify(PluginNotificationLevel.WARNING, "Successfully added in " + numberSuccessful + " new nodes");
        }
    }
}
