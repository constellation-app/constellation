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
package au.gov.asd.tac.constellation.plugins.arrangements.group;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Arrange by Attribute action.
 *
 * @author canis_majoris
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.group.ArrangeByNodeAttributeAction")
@ActionRegistration(displayName = "#CTL_ArrangeByNodeAttributeAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/group/resources/arrangeByNode.png",
        surviveFocusChange = true)
@ActionReference(path = "Menu/Arrange", position = 500)
@Messages("CTL_ArrangeByNodeAttributeAction=Node Attribute")
public final class ArrangeByNodeAttributeAction implements ActionListener {

    private final GraphNode context;

    public ArrangeByNodeAttributeAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecutor.startWith(ArrangementPluginRegistry.ATTRIBUTE, true)
                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeByNodeAttributeAction());
    }
}
