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

import au.gov.asd.tac.constellation.views.find2.FindViewTopComponent;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewPane extends BorderPane {

    private final FindViewTopComponent parentComponent;

    private final FindViewTabs findViewTabs;
    private final BottomButtonsPane basicFindButtons;
    private final BottomButtonsPane replaceButtons;
    private final BottomButtonsPane advancedFindButtons;

    public FindViewPane(final FindViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;

        this.findViewTabs = new FindViewTabs(this);
        setCenter(findViewTabs.getFindViewTabs());

        this.basicFindButtons = new BasicFindBottomButtons(this);
        this.replaceButtons = new ReplaceBottomButtons(this);
        this.advancedFindButtons = new BottomButtonsPane(this);

        setBottom(basicFindButtons);

    }

    public BottomButtonsPane getBasicFindButtons() {
        return basicFindButtons;
    }

    public BottomButtonsPane getReplaceButtons() {
        return replaceButtons;
    }

    public BottomButtonsPane getAdvancedFindButtons() {
        return advancedFindButtons;
    }

}
