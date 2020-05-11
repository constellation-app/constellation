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
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;
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
        final Hashmod hashmod;
        hashmod = new Hashmod();

        final HashmodPanel hashmodPanel = new HashmodPanel(hashmod);
        final DialogDescriptor dialog = new DialogDescriptor(hashmodPanel, Bundle.MSG_Title(), true, e -> {
            if (e.getActionCommand().equals("OK")) {
                final Hashmod hashmod1 = hashmodPanel.getHashmod();
                hashmodPanel.setAttributeNames(hashmod1.getCSVKey(), hashmod1.getCSVHeader(1), hashmod1.getCSVHeader(2));

                PluginExecution.withPlugin(new SimpleEditPlugin(Bundle.CTL_HashmodAction()) {
                    @Override
                    public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                        if (hashmod1 != null) {
                            final int hashmodKeyAttr = wg.addAttribute(GraphElementType.META, Hashmod.ATTRIBUTE_NAME, Hashmod.ATTRIBUTE_NAME, Hashmod.ATTRIBUTE_NAME, null, null);
                            wg.setObjectValue(hashmodKeyAttr, 0, hashmod1);
                            HashmodAction.run(wg);
                        }
                    }
                }).executeLater(graph);
            }
        });
        DialogDisplayer.getDefault().notify(dialog);
    }

    private static void run(final GraphWriteMethods wg) throws InterruptedException {
        final Hashmod hashmod;
        final String key;
        final String value1;
        final String value2;
        final int hashmodAttr;
        if (wg != null) {
            hashmodAttr = wg.getAttribute(GraphElementType.META, Hashmod.ATTRIBUTE_NAME);
            hashmod = wg.getObjectValue(hashmodAttr, 0);
            if (hashmod != null) {
                key = hashmod.getCSVKey();
                value1 = hashmod.getCSVHeader(1);
                value2 = hashmod.getCSVHeader(2);

                if (key == null || value1 == null || value2 == null) {
                    return;
                }
            } else {
                return;
            }
        } else {
            return;
        }

        final int keyAttribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, key);
        final int value1Attribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, value1);
        final int value2Attribute = wg.getSchema().getFactory().ensureAttribute(wg, GraphElementType.VERTEX, value2);

        if (keyAttribute == Graph.NOT_FOUND || value1Attribute == Graph.NOT_FOUND || value2Attribute == Graph.NOT_FOUND) {
            return;
        }

        final int vxCount = wg.getVertexCount();

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

            String keyValue;
            String value1Value;
            String value2Value;

            for (int i = 0; i < vxPos; i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                final int vxId = vxOrder[i];

                keyValue = wg.getObjectValue(keyAttribute, vxId);
                if (hashmod.doesKeyExist(keyValue)) {
                    value1Value = hashmod.getValueFromKey(keyValue, 1);
                    value2Value = hashmod.getValueFromKey(keyValue, 2);

                    if (value1Value != null) {
                        wg.setStringValue(value1Attribute, i, value1Value);
                    }
                    if (value2Value != null) {
                        wg.setStringValue(value2Attribute, i, value2Value);
                    }
                }
            }
        }
    }
}
