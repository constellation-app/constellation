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

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * A stateful (non-NetBeans) sidebar action.
 *
 * @author algol
 */
@Messages("CTL_DrawConnectionsAction=Draw Connections")
public final class DrawConnectionsAction extends AbstractAction implements Presenter.Toolbar {

    private final GraphNode context;

    /**
     * Construct a new action.
     *
     * @param context The context Node.
     */
    public DrawConnectionsAction(final GraphNode context) {
        this.context = context;
        putValue(Action.SMALL_ICON, UserInterfaceIconProvider.CONNECTIONS.buildIcon(16));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(DrawConnectionsAction.class, "CTL_DrawConnectionsAction"));
        putValue(Action.SELECTED_KEY, true);
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.TOGGLE_DRAW_FLAG)
                .withParameter(ToggleDrawFlagPlugin.FLAG_PARAMETER_ID, DrawFlags.CONNECTIONS)
                .executeLater(context.getGraph());
    }

    @Override
    public Component getToolbarPresenter() {
        return new JToggleButton(this);
    }
}
