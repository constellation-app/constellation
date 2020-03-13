/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.plugins.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.visual.VisualGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.visual.utilities.BlazeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginRegistry;
import au.gov.asd.tac.constellation.plugins.parameters.DefaultPluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.BitSet;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;

/*
 * toolbar options containing blaze controls
 */
@ActionID(category = "Selection", id = "au.gov.asd.tac.constellation.functionality.blaze.BlazeActions")
@ActionRegistration(displayName = "", lazy = false)
@ActionReference(path = "Toolbars/Visualisation", position = 400)
@Messages("CTL_BlazeAction=Blaze")
public final class BlazeActions extends AbstractAction implements Presenter.Toolbar, GraphManagerListener {

    @StaticResource
    private static final String BLAZE_ACTIONS_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/blaze_actions.png";
    @StaticResource
    private static final String SELECT_BLAZES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/selectblazes.png";
    @StaticResource
    private static final String DESELECT_BLAZES_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/blaze.png";
    @StaticResource
    private static final String ADD_BLUE_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_blue.png";
    @StaticResource
    private static final String ADD_RED_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_red.png";
    @StaticResource
    private static final String ADD_YELLOW_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_yellow.png";
    @StaticResource
    private static final String ADD_CUSTOM_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/addblaze_custom.png";
    @StaticResource
    private static final String REMOVE_BLAZE_ICON = "au/gov/asd/tac/constellation/graph/visual/plugins/blaze/resources/removeblaze.png";

    private static final String SELECT_BLAZES_ACTION = "Select_Blazes";
    private static final String DESELECT_BLAZES_ACTION = "DeSelect_Blazes";
    private static final String ADD_BLUE_BLAZE_ACTION = "Add_Blaze_LightBlue";
    private static final String ADD_RED_BLAZE_ACTION = "Add_Blaze_Red";
    private static final String ADD_YELLOW_BLAZE_ACTION = "Add_Blaze_Yellow";
    private static final String ADD_CUSTOM_BLAZE_ACTION = "Add_Custom_Blaze";
    private static final String REMOVE_BLAZES_ACTION = "Remove_Blazes";

    private final JPanel panel;
    private final JMenuBar menuBar;
    private final JMenu menu;
    private Graph graph;
    private final SliderMenuItem sizeSlider, opacitySlider;
    private final ChangeListener sliderChangeListener;

    public BlazeActions() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuBar = new JMenuBar();
        menuBar.setOpaque(true);

        menu = new JMenu();
        menu.setIcon(ImageUtilities.loadImageIcon(BLAZE_ACTIONS_ICON, false));
        menu.setToolTipText("Blaze Controls");
        menu.addChangeListener((e) -> {
            if (graph != null) {
                updateSliders(graph);
            }
        });
        menu.setEnabled(false);

        final JMenuItem selectBlazesItem = new JMenuItem("Select Blazes");
        selectBlazesItem.setIcon(ImageUtilities.loadImageIcon(SELECT_BLAZES_ICON, false));
        selectBlazesItem.setActionCommand(SELECT_BLAZES_ACTION);
        selectBlazesItem.addActionListener(this);
        menu.add(selectBlazesItem);

        final JMenuItem deselectBlazesItem = new JMenuItem("Deselect Blazes");
        deselectBlazesItem.setIcon(ImageUtilities.loadImageIcon(DESELECT_BLAZES_ICON, false));
        deselectBlazesItem.setActionCommand(DESELECT_BLAZES_ACTION);
        deselectBlazesItem.addActionListener(this);
        menu.add(deselectBlazesItem);

        final JMenuItem addBlueBlazeItem = new JMenuItem("Add Blue Blazes");
        addBlueBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_BLUE_BLAZE_ICON, false));
        addBlueBlazeItem.setActionCommand(ADD_BLUE_BLAZE_ACTION);
        addBlueBlazeItem.addActionListener(BlazeActions.this);
        menu.add(addBlueBlazeItem);

        final JMenuItem addRedBlazeItem = new JMenuItem("Add Red Blazes");
        addRedBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_RED_BLAZE_ICON, false));
        addRedBlazeItem.setActionCommand(ADD_RED_BLAZE_ACTION);
        addRedBlazeItem.addActionListener(BlazeActions.this);
        menu.add(addRedBlazeItem);

        final JMenuItem addYellowBlazeItem = new JMenuItem("Add Yellow Blazes");
        addYellowBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_YELLOW_BLAZE_ICON, false));
        addYellowBlazeItem.setActionCommand(ADD_YELLOW_BLAZE_ACTION);
        addYellowBlazeItem.addActionListener(BlazeActions.this);
        menu.add(addYellowBlazeItem);

        final JMenuItem colorBlazeItem = new JMenuItem("Add Custom Blazes");
        colorBlazeItem.setIcon(ImageUtilities.loadImageIcon(ADD_CUSTOM_BLAZE_ICON, false));
        colorBlazeItem.setActionCommand(ADD_CUSTOM_BLAZE_ACTION);
        colorBlazeItem.addActionListener(BlazeActions.this);
        menu.add(colorBlazeItem);

        final JMenuItem removeBlazeItem = new JMenuItem("Remove Blazes");
        removeBlazeItem.setIcon(ImageUtilities.loadImageIcon(REMOVE_BLAZE_ICON, false));
        removeBlazeItem.setActionCommand(REMOVE_BLAZES_ACTION);
        removeBlazeItem.addActionListener(BlazeActions.this);
        menu.add(removeBlazeItem);

        this.sizeSlider = new SliderMenuItem("Size");
        sizeSlider.setValue((NbPreferences.forModule(GraphPreferenceKeys.class)
                .getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT)));
        menu.add(sizeSlider);

        this.opacitySlider = new SliderMenuItem("Opacity");
        opacitySlider.setValue((NbPreferences.forModule(GraphPreferenceKeys.class)
                .getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT)));
        menu.add(opacitySlider);

        this.sliderChangeListener = e
                -> setBlazeProperties(sizeSlider.getValue() / 100f, opacitySlider.getValue() / 100f);
        sizeSlider.addChangeListener(sliderChangeListener);
        opacitySlider.addChangeListener(sliderChangeListener);

        menuBar.add(menu);
        panel.add(menuBar, BorderLayout.CENTER);

        GraphManager.getDefault().addGraphManagerListener(BlazeActions.this);
    }

    private void setBlazeProperties(final float size, final float opacity) {
        if (graph != null) {
            PluginExecution.withPlugin(VisualGraphPluginRegistry.UPDATE_BLAZE_SIZE_OPACITY)
                    .withParameter(UpdateBlazeSizeOpacityPlugin.SIZE_PARAMETER_ID, size)
                    .withParameter(UpdateBlazeSizeOpacityPlugin.OPACITY_PARAMETER_ID, opacity)
                    .executeLater(graph);
        }
    }

    private void updateSliders(final Graph graph) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            Preferences preferences = NbPreferences.forModule(GraphPreferenceKeys.class);

            final int blazeSizeAttributeId = VisualConcept.GraphAttribute.BLAZE_SIZE.get(rg);
            final float blazeSize = blazeSizeAttributeId == Graph.NOT_FOUND
                    ? (preferences.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT)) / 100f
                    : rg.getFloatValue(blazeSizeAttributeId, 0);

            final int blazeOpacityAttributeId = VisualConcept.GraphAttribute.BLAZE_OPACITY.get(rg);
            final float blazeOpacity = blazeOpacityAttributeId == Graph.NOT_FOUND
                    ? (preferences.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT)) / 100f
                    : rg.getFloatValue(blazeOpacityAttributeId, 0);

            sizeSlider.removeChangeListener(sliderChangeListener);
            sizeSlider.setValue((int) (blazeSize * 100));
            sizeSlider.addChangeListener(sliderChangeListener);
            opacitySlider.removeChangeListener(sliderChangeListener);
            opacitySlider.setValue((int) (blazeOpacity * 100));
            opacitySlider.addChangeListener(sliderChangeListener);
        } finally {
            rg.release();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String command = e.getActionCommand();
        final Plugin plugin;
        final PluginParameters parameters;
        final Pair<BitSet, ConstellationColor> selectionResult = BlazeUtilities.getSelection(graph, null);

        switch (command) {
            case ADD_BLUE_BLAZE_ACTION:
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                parameters.getParameters().get(BlazeUtilities.COLOR_PARAMETER_ID).setColorValue(ConstellationColor.LIGHT_BLUE);
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                break;
            case ADD_RED_BLAZE_ACTION:
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                parameters.getParameters().get(BlazeUtilities.COLOR_PARAMETER_ID).setColorValue(ConstellationColor.RED);
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                break;
            case ADD_YELLOW_BLAZE_ACTION:
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                parameters.getParameters().get(BlazeUtilities.COLOR_PARAMETER_ID).setColorValue(ConstellationColor.YELLOW);
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                break;
            case ADD_CUSTOM_BLAZE_ACTION:
                final Pair<Boolean, ConstellationColor> colorResult = BlazeUtilities.colorDialog(selectionResult.getValue());
                if (colorResult.getKey()) {
                    plugin = PluginRegistry.get(VisualGraphPluginRegistry.ADD_CUSTOM_BLAZE);
                    parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                    parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                    parameters.getParameters().get(BlazeUtilities.COLOR_PARAMETER_ID).setColorValue(colorResult.getValue());
                    PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                }
                break;
            case SELECT_BLAZES_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.SELECT_BLAZES).executeLater(graph);
                break;
            case DESELECT_BLAZES_ACTION:
                PluginExecution.withPlugin(VisualGraphPluginRegistry.DESELECT_BLAZES).executeLater(graph);
                break;
            case REMOVE_BLAZES_ACTION:
                plugin = PluginRegistry.get(VisualGraphPluginRegistry.REMOVE_BLAZE);
                parameters = DefaultPluginParameters.getDefaultParameters(plugin);
                parameters.getParameters().get(BlazeUtilities.VERTEX_IDS_PARAMETER_ID).setObjectValue(selectionResult.getKey());
                PluginExecution.withPlugin(plugin).withParameters(parameters).executeLater(graph);
                break;
        }
    }

    @Override
    public Component getToolbarPresenter() {
        return panel;
    }

    @Override
    public void graphOpened(final Graph graph) {
    }

    @Override
    public void graphClosed(final Graph graph) {
    }

    @Override
    public void newActiveGraph(final Graph graph) {
        if (this.graph != graph) {
            this.graph = graph;
            if (graph == null) {
                menu.setEnabled(false);
            } else {
                updateSliders(graph);
                menu.setEnabled(true);
            }
        }
    }

    private class SliderMenuItem extends JSlider implements MenuElement {

        public SliderMenuItem(final String title) {
            setBorder(new CompoundBorder(new TitledBorder(title), new EmptyBorder(3, 5, 3, 5)));
            setMaximum(100);
            setMinimum(10);
        }

        @Override
        public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            processMouseMotionEvent(event);
            processMouseEvent(event);
        }

        @Override
        public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {
        }

        @Override
        public void menuSelectionChanged(boolean isIncluded) {
        }

        @Override
        public MenuElement[] getSubElements() {
            return new MenuElement[0];
        }

        @Override
        public Component getComponent() {
            return this;
        }
    }
}
