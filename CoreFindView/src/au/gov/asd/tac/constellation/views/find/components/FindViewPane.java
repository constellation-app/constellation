/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.find.components;

import au.gov.asd.tac.constellation.views.find.FindViewTopComponent;
import javafx.scene.layout.BorderPane;

/**
 * This class is the overarching BorderPane for the find view. It contains the
 * tab pane and the various button layouts specific to each tab. It is the main
 * UI object that is connected to the Top Component.
 *
 * @author Atlas139mkm
 */
public class FindViewPane extends BorderPane {

    private final FindViewTopComponent parentComponent;
    private final FindViewTabs findViewTabs;

    public FindViewPane(final FindViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;
        this.findViewTabs = new FindViewTabs(this);
        setCenter(findViewTabs);
    }

    /**
     * Get the findViewPanes parent component
     *
     * @return find view Top Component
     */
    public FindViewTopComponent getParentComponent() {
        return parentComponent;
    }

    /**
     * Get the Find view Tabs
     *
     * @return findViewTabs
     */
    public FindViewTabs getTabs() {
        return findViewTabs;
    }

}
