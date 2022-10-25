/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.find2.components.advanced.AdvancedCriteriaBorderPane;
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
    @ActionReference(path = "Shortcuts", name = "C-B")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_FindView2Action",
        preferredID = "FindViewTopComponent2"
)
@NbBundle.Messages({
    "CTL_FindView2Action=Find View",
    "CTL_FindViewTopComponent2=Find and Replace",
    "HINT_FindViewTopComponent2=Find and Replace"
})

public final class FindViewTopComponent extends JavaFxTopComponent<FindViewPane> {

    private final FindViewPane pane;

    public FindViewTopComponent() {
        setName(Bundle.CTL_FindViewTopComponent2());
        setToolTipText(Bundle.HINT_FindViewTopComponent2());

        /**
         * initialize the FindViewController, initialize the Components of the
         * topComponenet, set pane to a new FindViewPane and initialize the
         * content.
         */
        FindViewController.getDefault().init(this);
        initComponents();
        this.pane = new FindViewPane(this);
        initContent();

        // Set the findView window to float
        WindowManager.getDefault().setTopComponentFloating(this, true);

        // View will be disable if no graphs are opened, enabled if otherwise
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
         * This updates the attribute list UI element when a attribute is added
         * or removed from the graph.
         */
        addAttributeCountChangeHandler(graph -> UpdateUI());

    }

    /**
     * Sets the top components content to the findViewPane.
     *
     * @return
     */
    @Override
    protected FindViewPane createContent() {
        return pane;
    }

    /**
     * Sets the css Styling
     *
     * @return
     */
    @Override
    protected String createStyle() {
        return "resources/find-view.css";
    }

    /**
     * Handles what occurs when the find view is closed. This updates the UI, to
     * ensure its current and toggles the findview to set it to enabled or
     * disabled based on if a graph is open.
     */
    @Override
    protected void handleComponentClosed() {
        super.handleComponentClosed();
        UpdateUI();
        disableFindView();
    }

    /**
     * Handles what occurs when the component is opened. This updates the UI to
     * ensure its current, toggles the find view to set it to enabled or
     * disabled based on if a graph is open, focuses the findTextBox for UX
     * quality, ensures the view window is floating. It also sets the size and
     * location of the view to be in the top right of the users screen.
     */
    @Override
    protected void handleComponentOpened() {
        super.handleComponentOpened();
        UpdateUI();
        disableFindView();
        focusFindTextField();
        WindowManager.getDefault().setTopComponentFloating(this, true);

        this.setRequestFocusEnabled(true);

        /**
         * This loops through all the current windows and compares this top
         * components top level ancestor with the windows parent. If they match
         * the window is the find view so we set the location and the size of
         * the window.
         */
        for (final Window window : Window.getWindows()) {
            if (this.getTopLevelAncestor() != null && this.getTopLevelAncestor().getName().equals(window.getName())) {
                window.setMinimumSize(new Dimension(485, 285));
                window.setLocation((int) Screen.getPrimary().getBounds().getMaxX() - 510, 110);
            }
        }
    }

    /**
     * When a graph is opened handle toggling the find view disabled state
     *
     * @param graph
     */
    @Override
    protected void handleGraphOpened(final Graph graph) {
        super.handleGraphOpened(graph);
        disableFindView();
    }

    /**
     * When a graph is closed handle toggling the find view disabled state
     *
     * @param graph
     */
    @Override
    protected void handleGraphClosed(final Graph graph) {
        super.handleGraphClosed(graph);
        disableFindView();
    }

    /**
     * When a new graph is created handle updating the UI
     *
     * @param graph
     */
    @Override
    protected void handleNewGraph(final Graph graph) {
        super.handleNewGraph(graph);
        UpdateUI();
    }

    /**
     * Get the findViewPane
     *
     * @return the findViewPane
     */
    public FindViewPane getFindViewPane() {
        return pane;
    }

    /**
     * Toggles the disabled state of the findView based on if any graphs are
     * open.
     */
    public void disableFindView() {
        getFindViewPane().setDisable(GraphManager.getDefault().getAllGraphs().isEmpty());
    }

    /**
     * Requests focus of the basic find text box.
     */
    public void focusFindTextField() {
        getFindViewPane().getTabs().getBasicFindTab().requestTextFieldFocus();
    }

    /**
     * This calls all the necessary functions for each tab to update the
     * attributes list based on what attributes are available for the user.
     */
    public void UpdateUI() {
        // Update the basic find tab
        final GraphElementType basicFindType = GraphElementType.getValue(getFindViewPane().getTabs().getBasicFindTab().getLookForChoiceBox().getSelectionModel().getSelectedItem());
        getFindViewPane().getTabs().getBasicFindTab().saveSelected(basicFindType);
        getFindViewPane().getTabs().getBasicFindTab().populateAttributes(basicFindType);
        getFindViewPane().getTabs().getBasicFindTab().updateSelectedAttributes(getFindViewPane().getTabs().getBasicFindTab().getMatchingAttributeList(basicFindType));

        // Update the replace tab
        final GraphElementType replaceType = GraphElementType.getValue(getFindViewPane().getTabs().getReplaceTab().getLookForChoiceBox().getSelectionModel().getSelectedItem());
        getFindViewPane().getTabs().getReplaceTab().saveSelected(replaceType);
        getFindViewPane().getTabs().getReplaceTab().populateAttributes(replaceType);
        getFindViewPane().getTabs().getReplaceTab().updateSelectedAttributes(getFindViewPane().getTabs().getReplaceTab().getMatchingAttributeList(replaceType));

        // Update each of the advanced find tabs criteria panes
        final GraphElementType advancedType = GraphElementType.getValue(getFindViewPane().getTabs().getAdvancedFindTab().getLookForChoiceBox().getSelectionModel().getSelectedItem());
        for (final AdvancedCriteriaBorderPane criteriaPane : getFindViewPane().getTabs().getAdvancedFindTab().getCorrespondingCriteriaList(advancedType)) {
            /**
             * set the updateUI variable to true. This avoids the change
             * criteria pane function from occurring when re selecting the
             * currently selected element after updating the attribute list
             */
            criteriaPane.setUpdateUI(true);
            criteriaPane.updateAttributesList(advancedType);
            // set the updateUI variable back to false to maintian normal
            // functionality for the change criteriapane function
            criteriaPane.setUpdateUI(false);
        }

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
