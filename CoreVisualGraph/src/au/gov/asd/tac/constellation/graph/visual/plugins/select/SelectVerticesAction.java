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

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Select all action.
 *
 * @author Quasar985
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.select.SelectVerticesAction")
@ActionRegistration(displayName = "#CTL_SelectVerticesAction", iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/selectNodes.png", surviveFocusChange = false)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 1),})
@Messages("CTL_SelectVerticesAction=Select All Nodes")
public final class SelectVerticesAction extends SimplePluginAction {

    /**
     * Construct a new action.
     *
     * @param context GraphNode context.
     */
    public SelectVerticesAction(final GraphNode context) {
        super(context, VisualGraphPluginRegistry.SELECT_VERTICES);
    }
}
