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
package au.gov.asd.tac.constellation.utilities.icon;

/**
 * Allows handling of user defined icons within CONSTELLATION. Only the highest
 * priority implementation of CustomIconProvider (ie. the implementation with
 * the lowest 'priority' in its {@link org.openide.util.lookup.ServiceProvider}
 * annotation) will be used.
 *
 * @author cygnus_x-1
 */
public interface CustomIconProvider extends ConstellationIconProvider {

    /**
     * Defines how handle adding (or storing) a user-defined
     * {@link ConstellationIcon} for use in CONSTELLATION.
     *
     * @param icon The {@link ConstellationIcon} to add.
     * @return A boolean value representing whether the operation was successful
     * or not.
     */
    public boolean addIcon(ConstellationIcon icon);

    /**
     * Defines how to handle removing (or deleting) a user-defined
     * {@link ConstellationIcon} for use in CONSTELLATION.
     *
     * @param iconName A {@link String} representing the name of a previously
     * added {@link ConstellationIcon}.
     * @return A boolean value representing whether the operation was successful
     * or not.
     */
    public boolean removeIcon(String iconName);
}
