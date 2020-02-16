/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality.select.structure;

import au.gov.asd.tac.constellation.functionality.CorePluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.pluginframework.SimplePluginAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Action for Select Pendants plugin.
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.select.structure.SelectPendantsAction")
@ActionRegistration(displayName = "#CTL_SelectPendantsAction", iconBase = "au/gov/asd/tac/constellation/functionality/select/structure/resources/pendant.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 800),
    @ActionReference(path = "Toolbars/Selection", position = 100)
})
@Messages("CTL_SelectPendantsAction=Select Pendants")
public final class SelectPendantsAction extends SimplePluginAction {

    public SelectPendantsAction(final GraphNode context) {
        super(context, CorePluginRegistry.SELECT_PENDANTS);
    }
}
