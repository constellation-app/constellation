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
package au.gov.asd.tac.constellation.graph.schema.visual.plugins;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaPluginRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Completes the graph with its schema.
 *
 * @author twinkle2_little
 */
@ActionID(category = "Schema", id = "au.gov.asd.tac.constellation.graph.schema.visual.plugin.CompleteSchemaAction")
@ActionRegistration(displayName = "#CTL_CompleteSchemaAction",
        iconBase = "au/gov/asd/tac/constellation/graph/schema/visual/plugins/completeWithSchema.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 500, separatorBefore = 499),
    @ActionReference(path = "Shortcuts", name = "F5")
})
@Messages("CTL_CompleteSchemaAction=Complete with Schema")
public final class CompleteSchemaAction extends SimplePluginAction {

    public CompleteSchemaAction(final GraphNode node) {
        super(node, VisualSchemaPluginRegistry.COMPLETE_SCHEMA);
    }
}
