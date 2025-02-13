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
 * de-select all transactions in the graph
 *
 * @author algol
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.select.DeselectTransactionsAction")
@ActionRegistration(displayName = "#CTL_DeselectTransactionsAction",
        iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/select/resources/deselectTransactions.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Selection", position = 300)
})
@Messages("CTL_DeselectTransactionsAction=Deselect Transactions")
public final class DeselectTransactionsAction extends SimplePluginAction {

    public DeselectTransactionsAction(final GraphNode context) {
        super(context, VisualGraphPluginRegistry.DESELECT_TRANSACTIONS);
    }
}
