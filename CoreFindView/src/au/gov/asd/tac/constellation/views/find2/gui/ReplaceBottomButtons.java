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

/**
 * This class is a child class of BottomButtonsPane. It contains the relevant
 * buttons that appear at the bottom of the find view when the replace tab is
 * selected
 *
 * @author Atlas139mkm
 */
public class ReplaceBottomButtons extends BottomButtonsPane {

    private Button replace = new Button("Replace");
    private Button replaceAll = new Button("Replace All");

    public ReplaceBottomButtons(FindViewPane parentComponent) {
        super(parentComponent);

        this.getChildren().addAll(replace, replaceAll);

    }

}
