/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public abstract class DataAccessPluginType {

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
        final List<PositionalDataAccessPluginType> ptypeList = getTypesOrderedByPosition();

        final List<String> typeList = new ArrayList<>();
        ptypeList.stream().forEach((ppp) -> {
            typeList.add(ppp.type);
        });

        return Collections.unmodifiableList(typeList);
    }

    /**
     * Gather the data access plugin types and their position
     *
     * @return A Map of {@link DataAccessPluginType} names to their position
     */
    public static Map<String, Integer> getTypeWithPosition() {
        final List<PositionalDataAccessPluginType> ptypeList = getTypesOrderedByPosition();

        final Map<String, Integer> typesWithPosition = new HashMap<>();
        ptypeList.stream().forEach((plugin) -> {
            typesWithPosition.put(plugin.type, plugin.position);
        });

        return typesWithPosition;
    }

    /**
     * Get a list of {@link PositionalDataAccessPluginType} ordered by their
     * position in ascending order
     *
     * @return A List of {@link PositionalDataAccessPluginType} in ascending
     * order
     */
    private static List<PositionalDataAccessPluginType> getTypesOrderedByPosition() {
        final List<PositionalDataAccessPluginType> ptypeList = new ArrayList<>();
        Lookup.getDefault().lookupAll(DataAccessPluginType.class).stream().forEach((type) -> {
            ptypeList.addAll(type.getPluginTypeList());
        });

        ptypeList.sort((PositionalDataAccessPluginType pt1, PositionalDataAccessPluginType pt2) -> {
            return Integer.compare(pt1.position, pt2.position);
        });

        return ptypeList;
    }

    /**
     * A DataAccessPluginGroup with a position.
     */
    public static class PositionalDataAccessPluginType {

        public final String type;
        public final int position;

        public PositionalDataAccessPluginType(final String type, final int position) {
            this.type = type;
            this.position = position;
        }
    }
}
