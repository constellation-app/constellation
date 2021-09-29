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
package au.gov.asd.tac.constellation.views.find2.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewTabs {

    private final FindViewPane pane;

    private final TabPane tabPane;

    private final BasicFindTab basicFindTab = new BasicFindTab(this);
    private final ReplaceTab replaceTab = new ReplaceTab(this);
    private final Tab advancedFindTab = new Tab("Advanced Find");

    public FindViewTabs(FindViewPane pane) {
        this.pane = pane;

        tabPane = new TabPane();
        setTabContent();

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
                if (newTab.equals(basicFindTab)) {
                    pane.setBottom(pane.getBasicFindButtons());
                } else if (newTab.equals(replaceTab)) {
                    pane.setBottom(pane.getReplaceButtons());
                } else {
                    pane.setBottom(pane.getAdvancedFindButtons());
                }
            }
        }
        );
    }

    public void setTabContent() {
        basicFindTab.setClosable(false);
        replaceTab.setClosable(false);
        advancedFindTab.setClosable(false);

        tabPane.getTabs().add(basicFindTab);
        tabPane.getTabs().add(replaceTab);
        tabPane.getTabs().add(advancedFindTab);
    }

    public TabPane getFindViewTabs() {
        return tabPane;
    }

}
