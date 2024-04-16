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
package au.gov.asd.tac.constellation.graph.visual.plugins.hop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/*
 * toolbar options containing hop controls
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.hop.HopActions")
@ActionRegistration(displayName = "", lazy = false)
@ActionReference(path = "Toolbars/Display", position = 500)
@Messages("CTL_HopActions=Hop")
public final class HopActions extends AbstractAction implements Presenter.Toolbar, GraphManagerListener {

    private static final Icon HOP_OUT_HALF_ICON = UserInterfaceIconProvider.HOP_HALF.buildIcon(16);
    private static final Icon HOP_OUT_ONE_ICON = UserInterfaceIconProvider.HOP_ONE.buildIcon(16);
    private static final Icon HOP_OUT_FULL_ICON = UserInterfaceIconProvider.HOP_FULL.buildIcon(16);

    private static final String HOP_OUT_HALF_ACTION = "Hop_Half";
    private static final String HOP_OUT_ONE_ACTION = "Hop_One";
    private static final String HOP_OUT_FULL_ACTION = "Hop_Full";

    private final JPanel panel;
    private final JMenuBar menuBar;
    private final JMenu menu;
    private Graph graph;
    private final JCheckBox outgoing;
    private final JCheckBox incoming;
    private final JCheckBox undirected;

    public HopActions() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuBar = new JMenuBar();
        menuBar.setOpaque(true);

        menu = new JMenu();
        menu.setIcon(HOP_OUT_ONE_ICON);
        menu.setToolTipText("Hop Controls");
        menu.setEnabled(false);

        final JMenuItem hopOutHalfItem = new JMenuItem("Hop Out Half");
        hopOutHalfItem.setIcon(HOP_OUT_HALF_ICON);
        hopOutHalfItem.setActionCommand(HOP_OUT_HALF_ACTION);
        hopOutHalfItem.addActionListener(HopActions.this);
        menu.add(hopOutHalfItem);

        final JMenuItem hopOutOneItem = new JMenuItem("Hop Out One");
        hopOutOneItem.setIcon(HOP_OUT_ONE_ICON);
        hopOutOneItem.setActionCommand(HOP_OUT_ONE_ACTION);
        hopOutOneItem.addActionListener(HopActions.this);
        menu.add(hopOutOneItem);

        final JMenuItem hopOutFullItem = new JMenuItem("Hop Out Full");
        hopOutFullItem.setIcon(HOP_OUT_FULL_ICON);
        hopOutFullItem.setActionCommand(HOP_OUT_FULL_ACTION);
        hopOutFullItem.addActionListener(HopActions.this);
        menu.add(hopOutFullItem);

        final JPanel directionPanel = new JPanel();
        directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.Y_AXIS));
        directionPanel.setBorder(new TitledBorder("Direction"));

        outgoing = new JCheckBox("Outgoing", true);
        outgoing.setToolTipText("Hop Along Outgoing Transactions");
        directionPanel.add(outgoing);

        incoming = new JCheckBox("Incoming", true);
        incoming.setToolTipText("Hop Along Incoming Transactions");
        directionPanel.add(incoming);

        undirected = new JCheckBox("Undirected", true);
        undirected.setToolTipText("Hop Along Undirected Transactions");
        directionPanel.add(undirected);

        final JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        optionsPanel.setLayout(new BorderLayout());
        optionsPanel.add(directionPanel, BorderLayout.CENTER);

        menu.add(optionsPanel);

        menuBar.add(menu);
        panel.add(menuBar, BorderLayout.CENTER);

        GraphManager.getDefault().addGraphManagerListener(HopActions.this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String command = e.getActionCommand();
        final Plugin plugin;
        final PluginParameters parameters;

        switch (command) {
            case HOP_OUT_HALF_ACTION -> {
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.HOP_OUT);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(HopOutPlugin.HOPS_PARAMETER_ID).setIntegerValue(HopUtilities.HOP_OUT_HALF);
                parameters.getParameters().get(HopOutPlugin.OUTGOING_PARAMETER_ID).setBooleanValue(outgoing.isSelected());
                parameters.getParameters().get(HopOutPlugin.INCOMING_PARAMETER_ID).setBooleanValue(incoming.isSelected());
                parameters.getParameters().get(HopOutPlugin.UNDIRECTED_PARAMETER_ID).setBooleanValue(undirected.isSelected());
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
            }
            case HOP_OUT_ONE_ACTION -> {
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.HOP_OUT);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(HopOutPlugin.HOPS_PARAMETER_ID).setIntegerValue(HopUtilities.HOP_OUT_ONE);
                parameters.getParameters().get(HopOutPlugin.OUTGOING_PARAMETER_ID).setBooleanValue(outgoing.isSelected());
                parameters.getParameters().get(HopOutPlugin.INCOMING_PARAMETER_ID).setBooleanValue(incoming.isSelected());
                parameters.getParameters().get(HopOutPlugin.UNDIRECTED_PARAMETER_ID).setBooleanValue(undirected.isSelected());
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
            }
            case HOP_OUT_FULL_ACTION -> {
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.HOP_OUT);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(HopOutPlugin.HOPS_PARAMETER_ID).setIntegerValue(HopUtilities.HOP_OUT_FULL);
                parameters.getParameters().get(HopOutPlugin.OUTGOING_PARAMETER_ID).setBooleanValue(outgoing.isSelected());
                parameters.getParameters().get(HopOutPlugin.INCOMING_PARAMETER_ID).setBooleanValue(incoming.isSelected());
                parameters.getParameters().get(HopOutPlugin.UNDIRECTED_PARAMETER_ID).setBooleanValue(undirected.isSelected());
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
            }
            default -> {
                // Do nothing
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        return panel;
    }

    @Override
    public void graphOpened(final Graph graph) {
        // Required for GraphManagerListener, intentionally left blank
    }

    @Override
    public void graphClosed(final Graph graph) {
        // Required for GraphManagerListener, intentionally left blank
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        if (this.graph != graph) {
            this.graph = graph;
            menu.setEnabled(graph != null);
        }
    }
}
