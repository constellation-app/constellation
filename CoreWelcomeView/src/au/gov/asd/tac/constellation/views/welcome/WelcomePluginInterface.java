/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.welcome;

import javafx.scene.control.Button;

/**
 *
 * Sets how the welcome page plugins are laid out
 *
 * @author Delphinus8821
 */
public interface WelcomePluginInterface {

    /**
     * Get a unique reference that is used to identify the plugin
     *
     * @return a unique reference
     */
    public String getName();

    /**
     * This method describes what action should be taken when the link is
     * clicked on the Welcome Page
     *
     */
    public void run();

    /**
     * Creates the button object to represent this plugin
     *
     * @return the button object
     */
    public Button getButton();

}
