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
package au.gov.asd.tac.constellation.plugins.arrangements.group;

import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.graph.utilities.widgets.AttributeSelectionPanel;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.group.ArrangeByGroupAction")
@ActionRegistration(displayName = "#CTL_ArrangeByGroupAction", surviveFocusChange = true)
//@ActionReference(path = "Menu/Arrange", position = 500)
@Messages("CTL_ArrangeByGroupAction=By Group")
public final class ArrangeByGroupAction implements ActionListener {

    private final GraphNode context;

    public ArrangeByGroupAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final Set<GraphElementType> et = Collections.singleton(GraphElementType.VERTEX);

        // Which attribute to determine group by?
        final Set<String> dataTypes = new HashSet<>();
        dataTypes.add("boolean");
        dataTypes.add("color");
        dataTypes.add("date");
        dataTypes.add("icon");
        dataTypes.add("integer");
        dataTypes.add("object");
        dataTypes.add("string");

        final AttributeSelectionPanel asp = new AttributeSelectionPanel("Select an attribute to determine the groups");
        asp.setGraph(context.getGraph(), et, dataTypes, null);
        final DialogDescriptor dd = new DialogDescriptor(asp, Bundle.CTL_ArrangeByGroupAction());
        final Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == DialogDescriptor.OK_OPTION) {
            PluginExecutor.startWith(ArrangementPluginRegistry.GROUP)
                    .set(ArrangeByGroupPlugin.ATTRIBUTE_LABEL_PARAMETER_ID, asp.getAttributeName())
                    .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeByGroupAction());
        }
    }
}
