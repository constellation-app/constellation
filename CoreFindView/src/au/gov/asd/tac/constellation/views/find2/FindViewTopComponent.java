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

import au.gov.asd.tac.constellation.views.JavaFxTopComponent;
import au.gov.asd.tac.constellation.views.find2.gui.FindViewPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

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
        mode = "output",
        openAtStartup = false
)
@ActionID(
        category = "Window",
        id = "au.gov.asd.tac.constellation.views.find2.FindTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/Views", position = 3000)
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
    private final FindViewController findViewController;

    public FindViewTopComponent() {
        setName(Bundle.CTL_FindViewTopComponent2());
        setToolTipText(Bundle.HINT_FindViewTopComponent2());

        initComponents();
        this.pane = new FindViewPane(this);
        findViewController = FindViewController.getDefault().init(this);

        initContent();
    }

    @Override
    protected FindViewPane createContent() {
        return pane;
    }

    @Override
    protected String createStyle() {
        return "resources/find-view.css";
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
