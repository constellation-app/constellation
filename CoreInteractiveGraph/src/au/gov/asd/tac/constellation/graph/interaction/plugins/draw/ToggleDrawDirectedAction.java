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
import au.gov.asd.tac.constellation.graph.node.plugins.SimplePluginAction;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Component;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * A stateful (non-NetBeans) sidebar action.
 *
 * @author algol
 */
@Messages("CTL_ToggleDrawDirectedAction=Toggle Draw Directed Transactions")
public final class ToggleDrawDirectedAction extends SimplePluginAction implements Presenter.Toolbar {

    private static final Icon DIRECTED_ICON = UserInterfaceIconProvider.DIRECTED.buildIcon(16);
    private static final Icon UNDIRECTED_ICON = UserInterfaceIconProvider.UNDIRECTED.buildIcon(16);
    
    private final ButtonGroup buttonGroup;

    /**
     * Construct a new action.
     *
     * @param context Graph Node.
     * @param buttonGroup The button group to which this action belongs.
     */
    public ToggleDrawDirectedAction(final GraphNode context, final ButtonGroup buttonGroup) {
        super(context, InteractiveGraphPluginRegistry.TOGGLE_DRAW_DIRECTED);
        
        this.buttonGroup = buttonGroup;
        
        final boolean drawDirected;
        try (final ReadableGraph rg = context.getGraph().getReadableGraph()) {
            final int drawDirectedAttribute = VisualConcept.GraphAttribute.DRAW_DIRECTED_TRANSACTIONS.get(rg);
            drawDirected = drawDirectedAttribute != Graph.NOT_FOUND ? rg.getBooleanValue(drawDirectedAttribute, 0) : VisualGraphDefaults.DEFAULT_DRAWING_DIRECTED_TRANSACTIONS;
        }
        putValue(Action.SMALL_ICON, drawDirected ? DIRECTED_ICON : UNDIRECTED_ICON);
        putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_ToggleDrawDirectedAction());
        putValue(Action.SELECTED_KEY, drawDirected);
    }

    @Override
    public Component getToolbarPresenter() {
        final JToggleButton tb = new JToggleButton(this);
        buttonGroup.add(tb);

        return tb;
    }
}
