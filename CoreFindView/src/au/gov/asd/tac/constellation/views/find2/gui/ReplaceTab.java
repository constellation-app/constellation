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

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * This class contains all the UI elements for the replace Tab
 *
 * @author Atlas139mkm
 */
public class ReplaceTab extends BasicFindTab {

    private final Label replaceLabel = new Label("Replace With:");
    private final TextField replaceTextField = new TextField();

    private final Button replaceNextButton = new Button("Replace Next");
    private final Button replaceAllButton = new Button("Replace All");

    public ReplaceTab(FindViewTabs parentComponent) {
        super(parentComponent);
        this.setText("Replace");
        setReplaceGridContent();
    }

    /**
     * Adds the UI changes for the replace tab
     */
    private void setReplaceGridContent() {
        getTextGrid().add(replaceLabel, 0, 1);
        getTextGrid().add(replaceTextField, 1, 1);

        getButtonsHBox().getChildren().clear();
        getButtonsHBox().getChildren().addAll(replaceNextButton, replaceAllButton);
    }

    public void updateButtons() {
        buttonsHBox.getChildren().clear();
        buttonsHBox.getChildren().addAll(replaceNextButton, replaceAllButton, searchAllGraphs);
        getParentComponent().getParentComponent().setBottom(buttonsVBox);

    }

}
