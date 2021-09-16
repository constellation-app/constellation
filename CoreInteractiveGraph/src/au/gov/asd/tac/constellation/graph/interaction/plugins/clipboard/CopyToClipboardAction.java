/*
 * Copyright 2010-2021 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import org.openide.util.NbBundle.Messages;

/**
 * Implement copying selected elements of a graph to the clipboard.
 * <p>
 * There are two pieces of functionality being implemented: copying the graph to
 * the local clipboard, and copying text from the graph to the system clipboard.
 *
 * @author algol
 */
@Messages({
    "# {0} - nodes copied",
    "# {1} - transactions copied",
    "MSG_Copied=Nodes copied: {0}; Transactions copied {1}."
})
public final class CopyToClipboardAction extends SimplePluginAction {

    public CopyToClipboardAction(final GraphNode context) {
        super(context, InteractiveGraphPluginRegistry.COPY);
    }

}
