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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.find2.components.FindViewPane;
import au.gov.asd.tac.constellation.views.find2.plugins.ResetStatePlugin;
import java.awt.Dimension;
import java.awt.Window;
import javafx.stage.Screen;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Find View Top Component.
 *
 * @author Atlas139mkm
 */
@TopComponent.Description(
        preferredID = "FindViewTopComponent2",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "properties",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.find2.FindTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Experimental/Views", position = 3000),
    @ActionReference(path = "Shortcuts", name = "C-F")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_FindView2Action",
        preferredID = "FindViewTopComponent2"
)
@NbBundle.Messages({
    "CTL_FindView2Action=Find View",
    "CTL_FindViewTopComponent2=Find View",
    "HINT_FindViewTopComponent2=Find View"
})

public final class FindViewTopComponent extends JavaFxTopComponent<FindViewPane> {

    private final FindViewPane pane;

    public FindViewTopComponent() {
        setName(Bundle.CTL_FindViewTopComponent2());
        setToolTipText(Bundle.HINT_FindViewTopComponent2());
        FindViewController.getDefault().init(this);

        initComponents();
        this.pane = new FindViewPane(this);

        initContent();
        WindowManager.getDefault().setTopComponentFloating(this, true);

        disableFindView();

        /**
         * This is called whenever a node or transaction is added or deleted. It
         * resets the searching index back to the default to avoid index out of
         * bounds issues when trying to find a node or transaction that no
         * longer exists.
         */
        addStructureChangeHandler(graph -> {
            final ResetStatePlugin resetState = new ResetStatePlugin();
            PluginExecution.withPlugin(resetState).executeLater(graph);
        });

        /**
         * This is updates the attribute list UI element when a new attribute is
         * added to the
         */
        addAttributeCountChangeHandler(graph -> {
            UpdateUI();
        });

    }

    @Override
    protected FindViewPane createContent() {
        return pane;
    }

    @Override
    protected String createStyle() {
        return "resources/find-view.css";
    }

    @Override
    protected void handleComponentClosed() {
        super.handleComponentClosed();
        UpdateUI();
        disableFindView();
    }

    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        UpdateUI();
        disableFindView();
        focusFindTextField();
        WindowManager.getDefault().setTopComponentFloating(this, true);

        this.setRequestFocusEnabled(true);
        /**
         * NOTE - This is by no means a flawless solution. Loop through all
         * active windows. All view windows will have a default name of
         * dialog0,1,2,3 etc. This code works of the basis that the only dialog
         * window in float mode is the find view. It is assumed all other
         * windows will remain docked. I have not found a way to capture the
         * exact window that contains the find view hence this is the only
         * solution I could think of at the current time.
         */
        for (Window window : Window.getWindows()) {
            if (window.getName().contains("dialog")) {
                window.setMinimumSize(new Dimension(485, 285));
                window.setLocation((int) Screen.getPrimary().getBounds().getMaxX() - 510, 110);
            }
        }
    }

    @Override
    protected void handleGraphOpened(final Graph graph) {
        super.handleGraphOpened(graph);
        disableFindView();
    }

    @Override
    protected void handleGraphClosed(final Graph graph) {
        super.handleGraphClosed(graph);
        disableFindView();
    }

    @Override
    protected void handleNewGraph(final Graph graph) {
        super.handleNewGraph(graph);
        UpdateUI();
    }

    public FindViewPane getFindViewPane() {
        return pane;
    }

    public void disableFindView() {
        pane.setDisable(GraphManager.getDefault().getAllGraphs().isEmpty());
    }

    public void focusFindTextField() {
        pane.getTabs().getBasicFindTab().requestTextFieldFocus();
    }

    public void UpdateUI() {
        final GraphElementType basicFindType = GraphElementType.getValue(getFindViewPane().getTabs().getBasicFindTab().getLookForChoiceBox().getSelectionModel().getSelectedItem());
        getFindViewPane().getTabs().getBasicFindTab().saveSelected(basicFindType);
        getFindViewPane().getTabs().getBasicFindTab().populateAttributes(basicFindType);
        getFindViewPane().getTabs().getBasicFindTab().updateSelectedAttributes(getFindViewPane().getTabs().getBasicFindTab().getMatchingAttributeList(basicFindType));

        final GraphElementType replaceType = GraphElementType.getValue(getFindViewPane().getTabs().getReplaceTab().getLookForChoiceBox().getSelectionModel().getSelectedItem());
        getFindViewPane().getTabs().getReplaceTab().saveSelected(replaceType);
        getFindViewPane().getTabs().getReplaceTab().populateAttributes(replaceType);
        getFindViewPane().getTabs().getReplaceTab().updateSelectedAttributes(getFindViewPane().getTabs().getReplaceTab().getMatchingAttributeList(replaceType));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
