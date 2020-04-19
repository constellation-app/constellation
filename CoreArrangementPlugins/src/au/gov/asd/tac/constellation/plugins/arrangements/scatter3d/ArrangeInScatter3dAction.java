/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.scatter3d;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import static au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dGeneralPlugin.SCATTER3D_X_ATTRIBUTE;
import static au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dGeneralPlugin.SCATTER3D_X_LOGARITHMIC;
import static au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dGeneralPlugin.SCATTER3D_Y_ATTRIBUTE;
import static au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dGeneralPlugin.SCATTER3D_Y_LOGARITHMIC;
import static au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dGeneralPlugin.SCATTER3D_Z_ATTRIBUTE;
import static au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dGeneralPlugin.SCATTER3D_Z_LOGARITHMIC;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

/**
 * Arranging vertexes in a scatter3d
 *
 * @author CrucisGamma
 */
@ActionID(category = "Arrange", id = "au.gov.asd.tac.constellation.plugins.arrangements.scatter3d.ArrangeInScatter3dAction")
@ActionRegistration(displayName = "#CTL_ArrangeInScatter3dAction", iconBase = "au/gov/asd/tac/constellation/plugins/arrangements/scatter3d/resources/scatter3d.png", surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Arrange", position = 0),
    @ActionReference(path = "Toolbars/Arrange", position = 0),
    @ActionReference(path = "Shortcuts", name = "S-3")
})
@Messages("CTL_ArrangeInScatter3dAction=Scatter3d")

public final class ArrangeInScatter3dAction extends AbstractAction implements Presenter.Toolbar, GraphManagerListener {

    private static final String SCATTER3D_ACTIONS_ICON = "au/gov/asd/tac/constellation/plugins/arrangements/scatter3d/resources/scatter3d.png";
    private static final String DIMENSION_SELECTED = "Select Attributes";

    private JPanel panel = null;
    private JMenuBar menuBar = null;
    private JMenu menu = null;
    private Graph graph = null;
    private JButton chooseDimensions = null;

    public ArrangeInScatter3dAction() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuBar = new JMenuBar();
        menuBar.setOpaque(true);

        menu = new JMenu();
        menu.setIcon(ImageUtilities.loadImageIcon(SCATTER3D_ACTIONS_ICON, false));
        menu.setToolTipText("Scatter3D Controls");
        menu.setEnabled(false);

        chooseDimensions = new ButtonMenuItem("Choose 3 Dimensions");
        chooseDimensions.setActionCommand(DIMENSION_SELECTED);
        chooseDimensions.setText("Choose 3 Dimensions");
        chooseDimensions.addActionListener(ArrangeInScatter3dAction.this);
        menu.add(chooseDimensions);

        menuBar.add(menu);
        panel.add(menuBar, BorderLayout.CENTER);

        GraphManager.getDefault().addGraphManagerListener(ArrangeInScatter3dAction.this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final String command = e.getActionCommand();
        final Plugin plugin;

        switch (command) {
            case DIMENSION_SELECTED:
                PluginParameters parameters = setupParameters();
                final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(Bundle.CTL_ArrangeInScatter3dAction(), parameters);
                dialog.showAndWait();

                if (PluginParametersSwingDialog.OK.equals(dialog.getResult())) {
                    String xDimension = parameters.getParameters().get(SCATTER3D_X_ATTRIBUTE).getStringValue();
                    String yDimension = parameters.getParameters().get(SCATTER3D_Y_ATTRIBUTE).getStringValue();
                    String zDimension = parameters.getParameters().get(SCATTER3D_Z_ATTRIBUTE).getStringValue();
                    Boolean xLogarithmic = parameters.getParameters().get(SCATTER3D_X_LOGARITHMIC).getBooleanValue();
                    Boolean yLogarithmic = parameters.getParameters().get(SCATTER3D_Y_LOGARITHMIC).getBooleanValue();
                    Boolean zLogarithmic = parameters.getParameters().get(SCATTER3D_Z_LOGARITHMIC).getBooleanValue();

                    if (xDimension != null
                            && yDimension != null
                            && zDimension != null
                            && graph != null) {
                        PluginExecutor.startWith(ArrangementPluginRegistry.SCATTER3D)
                                .set(ArrangeInScatter3dGeneralPlugin.SCATTER3D_X_ATTRIBUTE, xDimension)
                                .set(ArrangeInScatter3dGeneralPlugin.SCATTER3D_Y_ATTRIBUTE, yDimension)
                                .set(ArrangeInScatter3dGeneralPlugin.SCATTER3D_Z_ATTRIBUTE, zDimension)
                                .set(ArrangeInScatter3dGeneralPlugin.SCATTER3D_X_LOGARITHMIC, xLogarithmic)
                                .set(ArrangeInScatter3dGeneralPlugin.SCATTER3D_Y_LOGARITHMIC, yLogarithmic)
                                .set(ArrangeInScatter3dGeneralPlugin.SCATTER3D_Z_LOGARITHMIC, zLogarithmic)
                                .followedBy(InteractiveGraphPluginRegistry.RESET_VIEW)
                                .executeWriteLater(this.graph, Bundle.CTL_ArrangeInScatter3dAction());
                    }
                }
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
                menu.setEnabled(true);
            }
        }
    }

    public PluginParameters setupParameters() {
        final PluginParameters parameters = new PluginParameters();

        if (graph == null) {
            return parameters;
        }

        // Get the list of non-default attributes
        final ReadableGraph rg = graph.getReadableGraph();
        Map<String, Integer> nonDefault = null;
        try {
            nonDefault = AttributeUtilities.getNonDefaultAttributes(rg, 0);
        } finally {
            rg.release();
        }

        final List<String> keys = new ArrayList<>(nonDefault.keySet());

        final PluginParameter<SingleChoiceParameterValue> xdimension = SingleChoiceParameterType.build(SCATTER3D_X_ATTRIBUTE);
        xdimension.setName("xdimension For Graph");
        xdimension.setDescription("The attribute used for the X dimension");
        SingleChoiceParameterType.setOptions(xdimension, keys);
        parameters.addParameter(xdimension);

        final PluginParameter<SingleChoiceParameterValue> ydimension = SingleChoiceParameterType.build(SCATTER3D_Y_ATTRIBUTE);
        ydimension.setName("ydimension For Graph");
        ydimension.setDescription("The attribute used for the Y dimension");
        SingleChoiceParameterType.setOptions(ydimension, keys);
        parameters.addParameter(ydimension);

        final PluginParameter<SingleChoiceParameterValue> zdimension = SingleChoiceParameterType.build(SCATTER3D_Z_ATTRIBUTE);
        zdimension.setName("zdimension For Graph");
        zdimension.setDescription("The attribute used for the Z dimension");
        SingleChoiceParameterType.setOptions(zdimension, keys);
        parameters.addParameter(zdimension);

        final PluginParameter<BooleanParameterValue> xLogarithmic = BooleanParameterType.build(SCATTER3D_X_LOGARITHMIC);
        xLogarithmic.setName("Use Logarithmic Scaling");
        xLogarithmic.setDescription("Use Logarithmic Scaling for X dimension");
        parameters.addParameter(xLogarithmic);

        final PluginParameter<BooleanParameterValue> yLogarithmic = BooleanParameterType.build(SCATTER3D_Y_LOGARITHMIC);
        yLogarithmic.setName("Use Logarithmic Scaling");
        yLogarithmic.setDescription("Use Logarithmic Scaling for Y dimension");
        parameters.addParameter(yLogarithmic);

        final PluginParameter<BooleanParameterValue> zLogarithmic = BooleanParameterType.build(SCATTER3D_Z_LOGARITHMIC);
        zLogarithmic.setName("Use Logarithmic Scaling");
        zLogarithmic.setDescription("Use Logarithmic Scaling for Z dimension");
        parameters.addParameter(zLogarithmic);

        return parameters;
    }

    private class ButtonMenuItem extends JButton implements MenuElement {

        public ButtonMenuItem(String s) {
            super(s);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        @Override
        public void processMouseEvent(MouseEvent me, MenuElement[] element, MenuSelectionManager msm) {
            if (me.getID() == MouseEvent.MOUSE_CLICKED
                    || me.getID() == MouseEvent.MOUSE_RELEASED) {
                msm.setSelectedPath(null);
                doClick();
            } else {
                msm.setSelectedPath(null);
            }
        }

        @Override
        public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {
            processKeyEvent(event);
        }

        @Override
        public void menuSelectionChanged(boolean isIncluded) {
            repaint();
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
