/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.tasks;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewPreferenceKeys;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginType;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 * Looks up Data Access View plugins according to the preferences.
 *
 * @author formalhaunt
 * @author sol695510
 */
public class LookupPluginsTask implements Supplier<Map<String, Pair<Integer, List<DataAccessPlugin>>>> {

    private static final Preferences PREFERENCES = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);
    private static final String VISIBLE_CATEGORIES = PREFERENCES.get(DataAccessViewPreferenceKeys.VISIBLE_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV);
    private static final String HIDDEN_CATEGORIES = PREFERENCES.get(DataAccessViewPreferenceKeys.HIDDEN_DAV, DataAccessViewPreferenceKeys.DEFAULT_DAV);

    @Override
    public Map<String, Pair<Integer, List<DataAccessPlugin>>> get() {
        final Map<String, List<DataAccessPlugin>> allPlugins = DataAccessUtilities.getAllPlugins();
        final Map<String, Pair<Integer, List<DataAccessPlugin>>> orderedPlugins = new LinkedHashMap<>();

        final List<String> availableCategories = new ArrayList<>(allPlugins
                .entrySet()
                .stream()
                .filter(category -> !category.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .keySet());

        // Remove hidden categories if any exist in the preferences.
        if (StringUtils.isNotBlank(HIDDEN_CATEGORIES)) {
            final String[] hiddenCategories = (HIDDEN_CATEGORIES.replace("[", "").replace("]", "")).split(SeparatorConstants.COMMA);

            for (final String hiddenCategory : hiddenCategories) {
                availableCategories.remove(hiddenCategory.trim());
            }
        }

        // Add visible categories if any exist in the preferences.
        if (StringUtils.isNotBlank(VISIBLE_CATEGORIES)) {
            final String[] visibleCategories = (VISIBLE_CATEGORIES.replace("[", "").replace("]", "")).split(SeparatorConstants.COMMA);

            if (visibleCategories.length > 0) {
                for (int i = 0; i < visibleCategories.length; i++) {
                    orderedPlugins.put(visibleCategories[i].trim(), new Pair<>(i, allPlugins.get(visibleCategories[i].trim())));
                }
            }
        } else { // Add available categories if no visible categories exist in the preferences.
            final List<String> orderedCategories = new ArrayList<>(allPlugins.keySet());
            orderedCategories.retainAll(availableCategories);

            orderedCategories.forEach(category -> {
                final int position = DataAccessPluginType.getTypeWithPosition().getOrDefault(category, Integer.MAX_VALUE);
                orderedPlugins.put(category, new Pair<>(position, allPlugins.get(category)));
            });
        }

        return orderedPlugins;
    }
}
