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
package au.gov.asd.tac.constellation.views.welcome;

import java.util.Collections;
import java.util.List;

/**
 * A plugin designed to be supported by the Welcome Page.
 *
 * @author Delphinus8821
 */
public abstract class WelcomePageLayoutProvider {

    /**
     * Gets the plugins for the top part of the welcome page
     *
     * @return ArrayList of plugins
     */
    public List<WelcomePluginInterface> getTopPlugins() {
        return Collections.emptyList();
    }

    /**
     * Gets the plugins for the side of the welcome page
     *
     * @return ArrayList of plugins
     */
    public List<WelcomePluginInterface> getSidePlugins() {
        return Collections.emptyList();
    }
}
