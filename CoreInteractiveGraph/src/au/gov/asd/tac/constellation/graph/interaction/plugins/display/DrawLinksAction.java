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

import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * This class is responsible for drawing in links mode
 *
 */
@Messages("CTL_DrawLinksAction=Links (all transactions merged)")
public final class DrawLinksAction extends AbstractAction implements Presenter.Toolbar {

    private final GraphNode context;
    private final ButtonGroup buttonGroup;

    /**
     * Construct a new action.
     *
     * @param context Graph Node.
     * @param buttonGroup The button group to which this action belongs.
     */
    public DrawLinksAction(final GraphNode context, final ButtonGroup buttonGroup) {
        this.context = context;
        this.buttonGroup = buttonGroup;
        putValue(Action.SMALL_ICON, UserInterfaceIconProvider.LINKS.buildIcon(16));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(this.getClass(), "CTL_DrawLinksAction"));
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.SET_CONNECTION_MODE)
                .withParameter(SetConnectionModePlugin.CONNECTION_MODE_PARAMETER_ID, ConnectionMode.LINK)
                .executeLater(context.getGraph());
    }

    @Override
    public Component getToolbarPresenter() {
        final JToggleButton tb = new JToggleButton(this);
        buttonGroup.add(tb);
        return tb;
    }
}
