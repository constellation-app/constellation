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
package au.gov.asd.tac.constellation.graph.visual.plugins.dim;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/*
 * toolbar options containing set of dim actions
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.dim.DimActions")
@ActionRegistration(displayName = "", lazy = false)
@ActionReference(path = "Toolbars/Display", position = 300)
@Messages("CTL_DimAction=Dim")
public final class DimActions extends AbstractAction implements Presenter.Toolbar, GraphManagerListener {

    @StaticResource
    private static final String DIM_ACTIONS_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/dim_actions.png";
    @StaticResource
    private static final String DIM_SELECTED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/dim_selected.png";
    @StaticResource
    private static final String DIM_UNSELECTED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/dim_unselected.png";
    @StaticResource
    private static final String DIM_ALL_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/dim_all.png";
    @StaticResource
    private static final String UNDIM_SELECTED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/undim_selected.png";
    @StaticResource
    private static final String UNDIM_UNSELECTED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/undim_unselected.png";
    @StaticResource
    private static final String UNDIM_ALL_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/undim_all.png";
    @StaticResource
    private static final String SELECT_DIMMED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/select_dimmed.png";
    @StaticResource
    private static final String SELECT_UNDIMMED_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/dim/resources/select_undimmed.png";

    private static final String DIM_SELECTED_ACTION = "Dim_Selected";
    private static final String DIM_UNSELECTED_ACTION = "Dim_Unselected";
    private static final String DIM_ALL_ACTION = "Dim_All";
    private static final String UNDIM_SELECTED_ACTION = "Undim_Selected";
    private static final String UNDIM_UNSELECTED_ACTION = "Undim_Unselected";
    private static final String UNDIM_ALL_ACTION = "Undim_all";
    private static final String SELECT_DIMMED_ACTION = "Select_Dimmed";
    private static final String SELECT_UNDIMMED_ACTION = "Select_Undimmed";

    private final JPanel panel;
    private final JMenuBar menuBar;
    private final JMenu menu;
    private Graph graph;

    public DimActions() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuBar = new JMenuBar();
        menuBar.setOpaque(true);

        menu = new JMenu();
        menu.setIcon(ImageUtilities.loadImageIcon(DIM_ACTIONS_ICON, false));
        menu.setToolTipText("Dim Controls");
        menu.setEnabled(false);

        final JMenuItem dimSelectedItem = new JMenuItem("Dim Selected");
        dimSelectedItem.setIcon(ImageUtilities.loadImageIcon(DIM_SELECTED_ICON, false));
        dimSelectedItem.setActionCommand(DIM_SELECTED_ACTION);
        dimSelectedItem.addActionListener(DimActions.this);
        menu.add(dimSelectedItem);

        final JMenuItem dimUnselectedItem = new JMenuItem("Dim Unselected");
        dimUnselectedItem.setIcon(ImageUtilities.loadImageIcon(DIM_UNSELECTED_ICON, false));
        dimUnselectedItem.setActionCommand(DIM_UNSELECTED_ACTION);
        dimUnselectedItem.addActionListener(DimActions.this);
        menu.add(dimUnselectedItem);

        final JMenuItem dimAllItem = new JMenuItem("Dim All");
        dimAllItem.setIcon(ImageUtilities.loadImageIcon(DIM_ALL_ICON, false));
        dimAllItem.setActionCommand(DIM_ALL_ACTION);
        dimAllItem.addActionListener(DimActions.this);
        menu.add(dimAllItem);

        menu.add(new JSeparator(SwingConstants.HORIZONTAL));

        final JMenuItem undimSelectedItem = new JMenuItem("Undim Selected");
        undimSelectedItem.setIcon(ImageUtilities.loadImageIcon(UNDIM_SELECTED_ICON, false));
        undimSelectedItem.setActionCommand(UNDIM_SELECTED_ACTION);
        undimSelectedItem.addActionListener(DimActions.this);
        menu.add(undimSelectedItem);

        final JMenuItem undimUnselectedItem = new JMenuItem("Undim Unselected");
        undimUnselectedItem.setIcon(ImageUtilities.loadImageIcon(UNDIM_UNSELECTED_ICON, false));
        undimUnselectedItem.setActionCommand(UNDIM_UNSELECTED_ACTION);
        undimUnselectedItem.addActionListener(DimActions.this);
        menu.add(undimUnselectedItem);

        final JMenuItem undimAllItem = new JMenuItem("Undim All");
        undimAllItem.setIcon(ImageUtilities.loadImageIcon(UNDIM_ALL_ICON, false));
        undimAllItem.setActionCommand(UNDIM_ALL_ACTION);
        undimAllItem.addActionListener(DimActions.this);
        menu.add(undimAllItem);

        menu.add(new JSeparator(SwingConstants.HORIZONTAL));

        final JMenuItem selectDimmed = new JMenuItem("Select Dimmed");
        selectDimmed.setIcon(ImageUtilities.loadImageIcon(SELECT_DIMMED_ICON, false));
        selectDimmed.setActionCommand(SELECT_DIMMED_ACTION);
        selectDimmed.addActionListener(DimActions.this);
        menu.add(selectDimmed);

        final JMenuItem selectUndimmed = new JMenuItem("Select Undimmed");
        selectUndimmed.setIcon(ImageUtilities.loadImageIcon(SELECT_UNDIMMED_ICON, false));
        selectUndimmed.setActionCommand(SELECT_UNDIMMED_ACTION);
        selectUndimmed.addActionListener(DimActions.this);
        menu.add(selectUndimmed);

        menuBar.add(menu);
        panel.add(menuBar, BorderLayout.CENTER);

        GraphManager.getDefault().addGraphManagerListener(DimActions.this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String command = e.getActionCommand();

        switch (command) {
            case DIM_SELECTED_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.DIM_SELECTED).executeLater(graph);
                break;
            case DIM_UNSELECTED_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.DIM_UNSELECTED).executeLater(graph);
                break;
            case DIM_ALL_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.DIM_ALL).executeLater(graph);
                break;
            case UNDIM_SELECTED_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.UNDIM_SELECTED).executeLater(graph);
                break;
            case UNDIM_UNSELECTED_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.UNDIM_UNSELECTED).executeLater(graph);
                break;
            case UNDIM_ALL_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.UNDIM_ALL).executeLater(graph);
                break;
            case SELECT_DIMMED_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_DIMMED).executeLater(graph);
                break;
            case SELECT_UNDIMMED_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_UNDIMMED).executeLater(graph);
                break;
            default:
                break;
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
