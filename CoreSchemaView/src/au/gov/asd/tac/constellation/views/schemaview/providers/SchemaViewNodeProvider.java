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
package au.gov.asd.tac.constellation.views.schemaview.providers;

import javafx.scene.control.Tab;

/**
 * A class that provides JavaFX Node instances to the type viewer.
 *
 * @author algol
 */
public interface SchemaViewNodeProvider {

    /**
     * A Tab to put content into.
     * <p>
     * The content may be anything that the provider wants to display.
     * <p>
     * This is called on a non-FX thread so the individual providers have the
     * opportunity to create their data structures non-serially.
     *
     * @param tab The tab into which the content will be set (using
     * tab.setContent()).
     */
    public void setContent(final Tab tab);

    /**
     * Discard the Node.
     * <p>
     * When the viewer is closed, it may discard the provider to save memory.
     */
    public void discardNode();

    /**
     * Text to display in this Pane's tab.
     *
     * @return Text to display in this Pane's tab.
     */
    public String getText();
}
