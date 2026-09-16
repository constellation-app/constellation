/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.tree;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Tree arrangement
 *
 * @author algol
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.tree.NewArrangeInTreesAction")
@ActionRegistration(
        displayName = "#CTL_NewArrangeInTreesAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/tree/resources/arrangeInTree.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 200),
    @ActionReference(path = "Toolbars/Arrange", position = 100), //@ActionReference(path = "Shortcuts", name = "C-T")
})
@Messages("CTL_NewArrangeInTreesAction=Trees")
public final class NewArrangeInTreesAction extends AbstractAction {

    private final GraphNode context;
    private static final Dimension size = new Dimension(750, 300);

    public NewArrangeInTreesAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

        final Plugin plugin = PluginRegistry.get(ArrangementPluginRegistry.NEW_TREES);
        final PluginParameters params = plugin.createParameters();
        final Graph graph = context.getGraph();
        plugin.updateParameters(graph, params);

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_NewArrangeInTreesAction(), params);
        dialog.setSize(size);
        dialog.showAndWait();
        if (PluginParametersDialog.OK.equals(dialog.getResult())) {
            PluginExecutor.startWith(ArrangementPluginRegistry.NEW_TREES)
                    .set(params)
                    .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                    .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeInTreesAction());
        }
    }
}
