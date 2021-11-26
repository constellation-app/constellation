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
package au.gov.asd.tac.constellation.graph.interaction.plugins.display;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * Toggle the graph visibility preference and toolbar icon.
 * <p>
 * A stateful (non-NetBeans) sidebar action.
 *
 * @author arcturus
 */
@Messages("CTL_ToggleGraphVisibilityAction=Toggle Graph Visibility Threshold")
public final class ToggleGraphVisibilityAction extends AbstractAction implements Presenter.Toolbar {

    private static final Icon HIDDEN_ICON = UserInterfaceIconProvider.HIDDEN.buildIcon(16, Color.BLACK);
    private static final Icon VISIBLE_ICON = UserInterfaceIconProvider.VISIBLE.buildIcon(16, Color.BLACK);
    private final GraphNode context;

    /**
     * Construct a new action.
     *
     * @param context The context Node.
     */
    public ToggleGraphVisibilityAction(final GraphNode context) {
        this.context = context;

        final boolean visibleAboveThreshold = isCurrentVisibility(context.getGraph());
        putValue(Action.SMALL_ICON, visibleAboveThreshold ? VISIBLE_ICON : HIDDEN_ICON);
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ToggleGraphVisibilityAction.class, "CTL_ToggleGraphVisibilityAction"));
        putValue(Action.SELECTED_KEY, true);
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final boolean visibleAboveThreshold = isCurrentVisibility(context.getGraph());
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.SET_VISIBLE_ABOVE_THRESHOLD)
                .withParameter(SetVisibleAboveThresholdPlugin.FLAG_PARAMETER_ID, !visibleAboveThreshold)
                .executeLater(this.context.getGraph());
    }

    @Override
    public Component getToolbarPresenter() {
        return new JToggleButton(this);
    }

    private boolean isCurrentVisibility(final Graph graph) {
        boolean visibleAboveThreshold = VisualGraphDefaults.DEFAULT_GRAPH_VISIBILITY;
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int visibleAboveThresholdAttr = VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD.get(rg);
            if (visibleAboveThresholdAttr != Graph.NOT_FOUND) {
                visibleAboveThreshold = rg.getBooleanValue(visibleAboveThresholdAttr, 0);
            }
        } finally {
            rg.release();
        }
        return visibleAboveThreshold;
    }
}
