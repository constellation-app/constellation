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
package au.gov.asd.tac.constellation.views.dataaccess.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.Lookup;

/**
 * DataAccessPlugin types.
 * <p>
 * Each DataAccessPlugin has a type. A PositionalDataAccessPluginGroup defines
 * the position of a type. The data access view will display the plugin types
 * from top down in ascending order of position.
 * <p>
 * Implementations of this class should provide a public static String for each
 * type it defines; plugins that use the type can reference it via that String.
 *
 * @author algol
 */
public interface DataAccessPluginType {

    /**
     * The data access plugin groups defined by this class.
     *
     * @return The data access plugin groups defined by this class.
     */
    public abstract List<PositionalDataAccessPluginType> getPluginTypeList();

    /**
     * Gather the data access plugin types and return them in the correct order.
     *
     * @return A List&lt;String&gt; ordered by ascending position.
     */
    public static List<String> getTypes() {
        return Collections.unmodifiableList(
                getTypesOrderedByPosition().stream()
                        .map(PositionalDataAccessPluginType::getType)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Gather the data access plugin types and their position
     *
     * @return A Map of {@link DataAccessPluginType} names to their position
     */
    public static Map<String, Integer> getTypeWithPosition() {
        return getTypesOrderedByPosition().stream()
                .collect(Collectors.toMap(
                        PositionalDataAccessPluginType::getType,
                        PositionalDataAccessPluginType::getPosition
                ));
    }

    /**
     * Get a list of {@link PositionalDataAccessPluginType} ordered by their
     * position in ascending order
     *
     * @return A List of {@link PositionalDataAccessPluginType} in ascending
     * order
     */
    private static List<PositionalDataAccessPluginType> getTypesOrderedByPosition() {
        return Lookup.getDefault().lookupAll(DataAccessPluginType.class).stream()
                .map(DataAccessPluginType::getPluginTypeList)
                .flatMap(Collection::stream)
                .sorted((pt1, pt2) -> Integer.compare(pt1.getPosition(), pt2.getPosition()))
                .collect(Collectors.toList());
    }

    /**
     * A DataAccessPluginGroup with a position.
     */
    public static class PositionalDataAccessPluginType {
        private final String type;
        private final int position;

        public PositionalDataAccessPluginType(final String type,
                                              final int position) {
            this.type = type;
            this.position = position;
        }

        public String getType() {
            return type;
        }

        public int getPosition() {
            return position;
        }
    }
}
