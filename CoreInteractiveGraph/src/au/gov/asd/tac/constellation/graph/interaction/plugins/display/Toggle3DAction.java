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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * A stateful (non-NetBeans) sidebar action.
 *
 * @author algol
 */
@NbBundle.Messages("CTL_Toggle3DAction=Toggle 3D")
public final class Toggle3DAction extends AbstractAction implements Presenter.Toolbar {

    private static final Icon MODE_2D_ICON = UserInterfaceIconProvider.MODE_2D.buildIcon(16);
    private static final Icon MODE_3D_ICON = UserInterfaceIconProvider.MODE_3D.buildIcon(16);

    private final GraphNode context;
    private final ButtonGroup buttonGroup;

    /**
     * Construct a new action.
     *
     * @param context Graph Node.
     * @param buttonGroup The button group to which this action belongs.
     */
    public Toggle3DAction(final GraphNode context, final ButtonGroup buttonGroup) {
        this.context = context;
        this.buttonGroup = buttonGroup;
        final ReadableGraph rg = context.getGraph().getReadableGraph();
        final boolean isDisplayMode3D;
        try {
            final int displayMode3DAttribute = VisualConcept.GraphAttribute.DISPLAY_MODE_3D.get(rg);
            isDisplayMode3D = displayMode3DAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(displayMode3DAttribute, 0) : VisualGraphDefaults.DEFAULT_DISPLAY_MODE_3D;
        } finally {
            rg.release();
        }
        putValue(
                Action.SMALL_ICON,
                isDisplayMode3D ? MODE_3D_ICON : MODE_2D_ICON
        );
        putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_Toggle3DAction());
        putValue(Action.SELECTED_KEY, isDisplayMode3D);
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.TOGGLE_DISPLAY_MODE)
                .executeLater(context.getGraph());
    }

    @Override
    public Component getToolbarPresenter() {
        final JToggleButton tb = new JToggleButton(this);
        buttonGroup.add(tb);
        return tb;
    }
}
