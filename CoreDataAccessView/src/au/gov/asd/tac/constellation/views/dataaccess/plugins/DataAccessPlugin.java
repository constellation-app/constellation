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
package au.gov.asd.tac.constellation.views.dataaccess.plugins;

import au.gov.asd.tac.constellation.plugins.Plugin;

/**
 * A data access plugin should appear in the data access panel.
 * <p>
 * Plugins of each type are ordered based on {@link #getPosition()}.
 *
 * @author ruby_crucis
 */
public interface DataAccessPlugin extends Plugin {

    /**
     * What type of data access plugin is this?
     * <p>
     * Note that data access plugins cannot return any old string as a type. The
     * type string must be defined by a {@link DataAccessPluginType} instance as
     * returned by {@link DataAccessPluginType#getPluginTypeList()}.
     *
     * @return The type of data access plugin.
     */
    public String getType();

    /**
     * The position of this data access plugin relative to other data access
     * plugins.
     * <p>
     * The lowest position is Integer.MAX_VALUE.
     *
     * @return The position of this data access plugin.
     */
    public int getPosition();

    /**
     * True if this plugin is enabled, false otherwise.
     * <p>
     * If a plugin is disabled, it will be ignored and take no part in
     * proceedings (and therefore will not be displayed). Disabled plugins
     * should not do significant work in their constructors.
     *
     * @return True if this plugin is enabled, false otherwise.
     */
    default boolean isEnabled() {
        return true;
    }
}
