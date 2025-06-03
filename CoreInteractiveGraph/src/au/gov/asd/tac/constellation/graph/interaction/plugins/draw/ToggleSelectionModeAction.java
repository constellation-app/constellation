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
package au.gov.asd.tac.constellation.graph.interaction.plugins.draw;

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
@NbBundle.Messages("CTL_ToggleSelectionModeAction=Toggle Selection Mode")
public final class ToggleSelectionModeAction extends AbstractAction implements Presenter.Toolbar {

    private static final Icon DRAWING_MODE_ICON = UserInterfaceIconProvider.DRAW_MODE.buildIcon(16);
    private static final Icon SELECT_MODE_ICON = UserInterfaceIconProvider.SELECT_MODE.buildIcon(16);
    private final GraphNode context;
    private final ButtonGroup buttonGroup;

    /**
     * Construct a new action.
     *
     * @param context Graph Node.
     * @param buttonGroup The button group to which this action belongs.
     */
    public ToggleSelectionModeAction(final GraphNode context, final ButtonGroup buttonGroup) {
        this.context = context;
        this.buttonGroup = buttonGroup;

        final ReadableGraph rg = context.getGraph().getReadableGraph();
        final boolean drawingMode;
        try {
            final int drawingModeAttribute = VisualConcept.GraphAttribute.DRAWING_MODE.get(rg);
            drawingMode = drawingModeAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(drawingModeAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAWING_MODE;
        } finally {
            rg.release();
        }
        putValue(
                Action.SMALL_ICON,
                drawingMode ? DRAWING_MODE_ICON : SELECT_MODE_ICON
        );
        putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_ToggleSelectionModeAction());
        putValue(Action.SELECTED_KEY, drawingMode);
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.TOGGLE_SELECTION_MODE)
                .executeLater(context.getGraph());
    }

    @Override
    public Component getToolbarPresenter() {
        final JToggleButton tb = new JToggleButton(this);
        buttonGroup.add(tb);

        return tb;
    }
}
