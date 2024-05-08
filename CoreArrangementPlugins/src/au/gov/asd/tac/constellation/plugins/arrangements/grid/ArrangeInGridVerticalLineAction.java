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
package au.gov.asd.tac.constellation.plugins.arrangements.grid;

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Arranging components in a grid of vertical line.
 *
 * @author serpens24
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.grid.ArrangeInGridVerticalLineAction")
@ActionRegistration(displayName = "#CTL_ArrangeInGridVerticalLineAction", iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/grid/resources/gridvert.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 60),
    @ActionReference(path = "Toolbars/Arrange", position = 60),
    @ActionReference(path = "Shortcuts", name = "C-A-V")
})
@Messages("CTL_ArrangeInGridVerticalLineAction=Vertical Line")
public final class ArrangeInGridVerticalLineAction extends AbstractAction {

    private final GraphNode context;

    /**
     * Construct a new instance.
     *
     * @param context GraphNode context.
     */
    public ArrangeInGridVerticalLineAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecutor.startWith(ArrangementPluginRegistry.GRID_GENERAL)
                .set(ArrangeInGridGeneralPlugin.GRID_CHOICE_PARAMETER_ID, GridChoice.VERTICAL_LINE.toString())
                .set(ArrangeInGridGeneralPlugin.SIZE_GAIN_PARAMETER_ID, 1.25F)
                .set(ArrangeInGridGeneralPlugin.VERTICAL_GAP_PARAMETER_ID, 1)
                .set(ArrangeInGridGeneralPlugin.OFFSET_ROWS_PARAMETER_ID, false)
                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                .executeWriteLater(context.getGraph(), Bundle.CTL_ArrangeInGridVerticalLineAction());
    }
}
