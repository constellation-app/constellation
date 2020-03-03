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
import org.openide.util.NbBundle;

/**
 * This class creates the UI buttons that invokes the "Select Loops" Tool
 * perform loops service on graph
 *
 * @author aquila
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.select.structure.SelectSinksAction")
@ActionRegistration(displayName = "#CTL_SelectSinksAction", iconBase = "au/gov/asd/tac/constellation/functionality/select/structure/resources/sink.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1100),
    @ActionReference(path = "Toolbars/Selection", position = 400)
})
@NbBundle.Messages("CTL_SelectSinksAction=Select Sinks")
public class SelectSinksAction extends SimplePluginAction {

    public SelectSinksAction(final GraphNode context) {
        super(context, CorePluginRegistry.SELECT_SINKS);
    }
}
