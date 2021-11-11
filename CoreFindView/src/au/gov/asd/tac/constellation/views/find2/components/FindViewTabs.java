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
package au.gov.asd.tac.constellation.views.find2.components;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * This class manages the tabs within the find view. It handles the change of
 * content when a users switches tabs.
 *
 * @author Atlas139mkm
 */
public class FindViewTabs extends TabPane {

    private final FindViewPane parentComponent;

    private final BasicFindTab basicFindTab;
    private final ReplaceTab replaceTab;
    private final Tab advancedFindTab;

    public FindViewTabs(final FindViewPane parentComponent) {
        this.parentComponent = parentComponent;
        basicFindTab = new BasicFindTab(this);
        replaceTab = new ReplaceTab(this);
        advancedFindTab = new Tab("Advanced Find");
        setTabContent();

        /**
         * Logic for what occurs when the user changes tabs. This should update
         * the buttons at the bottom of the pane to match the tab selected.
         */
        getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) -> {
            if (newTab.equals(getBasicFindTab())) {
                getBasicFindTab().updateButtons();
            } else if (newTab.equals(getReplaceTab())) {
                getReplaceTab().updateButtons();
            } else {
                // place holder for advanced tab
            }
        });
    }

    /**
     * Adds the tabs into the tabPane and sets some tab specific attributes.
     */
    private void setTabContent() {
        // Ensure all 3 tabs can not be closed.
        getBasicFindTab().setClosable(false);
        getReplaceTab().setClosable(false);
        advancedFindTab.setClosable(false);

        // Add all 3 tabs to the tabPane
        getTabs().add(getBasicFindTab());
        getTabs().add(getReplaceTab());
        getTabs().add(advancedFindTab);

        // Update the buttons based on the currently selected tab
        switch (getSelectionModel().getSelectedIndex()) {
            case 0:
                getBasicFindTab().updateButtons();
                break;
            case 1:
                getReplaceTab().updateButtons();
                break;
            case 2:
                break;
        }

    }

    /**
     * Gets the findViewTabs
     *
     * @return the findViewTabs
     */
    public TabPane getFindViewTabs() {
        return this;
    }

    /**
     * Gets the parent component
     *
     * @return FindViewPane
     */
    public FindViewPane getParentComponent() {
        return parentComponent;
    }

    /**
     * Gets the BasicFindTab
     *
     * @return BasicFindTab
     */
    public BasicFindTab getBasicFindTab() {
        return basicFindTab;
    }

    /**
     * Gets the ReplaceTab
     *
     * @return ReplaceTab
     */
    public ReplaceTab getReplaceTab() {
        return replaceTab;
    }

}
