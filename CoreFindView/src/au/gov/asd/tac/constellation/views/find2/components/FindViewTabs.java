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

import javafx.beans.value.ChangeListener;
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

//    private final TabPane tabPane;
    private final BasicFindTab basicFindTab;
    private final ReplaceTab replaceTab;
    private final Tab advancedFindTab;

    public FindViewTabs(final FindViewPane parentComponent) {
        this.parentComponent = parentComponent;
        basicFindTab = new BasicFindTab(this);
        replaceTab = new ReplaceTab(this);
        advancedFindTab = new Tab("Advanced Find");
        setTabContent();

        getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
                if (newTab.equals(basicFindTab)) {
                    basicFindTab.updateButtons();
                } else if (newTab.equals(replaceTab)) {
                    replaceTab.updateButtons();
                } else {

                }
            }
        }
        );
    }

    /**
     * Adds the tabs into the tabPane and sets some tab specific attributes.
     */
    private void setTabContent() {
        basicFindTab.setClosable(false);
        replaceTab.setClosable(false);
        advancedFindTab.setClosable(false);

        getTabs().add(basicFindTab);
        getTabs().add(replaceTab);
        getTabs().add(advancedFindTab);

        switch (getSelectionModel().getSelectedIndex()) {
            case 0:
                basicFindTab.updateButtons();
                break;
            case 1:
                replaceTab.updateButtons();
                break;
            case 2:
                break;
        }

    }

    public TabPane getFindViewTabs() {
        return this;
    }

    public FindViewPane getParentComponent() {
        return parentComponent;
    }

    public BasicFindTab getBasicFindTab() {
        return basicFindTab;
    }

    public ReplaceTab getReplaceTab() {
        return replaceTab;
    }

}
