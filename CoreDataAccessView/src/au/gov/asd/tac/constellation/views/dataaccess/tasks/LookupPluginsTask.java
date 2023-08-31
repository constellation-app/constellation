/*
 * Copyright 2010-2022 Australian Signals Directorate
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 * Looks up all available data access plugins and populates a map with them
 * grouped by plugin type.
 *
 * @author formalhaunt
 */
public class LookupPluginsTask implements Supplier<Map<String, Pair<Integer, List<DataAccessPlugin>>>> {
    private static final Preferences PREFS = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);
    public static final String DAV_CATS = PREFS.get(DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW, DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW_DEFAULT);
    public static final String VISIBLE_CATS = PREFS.get(DataAccessViewPreferenceKeys.VISIBLE_DA_VIEW, DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW_DEFAULT);
    private static final List<Pair<Integer, String>> DEFAULT_ORDER = new ArrayList<>();

    public static List<Pair<Integer, String>> getDefaultOrder() {
        return Collections.unmodifiableList(DEFAULT_ORDER);
    }
    
    @Override
    public Map<String, Pair<Integer, List<DataAccessPlugin>>> get() {
        // Creates a map with the key set being every available data access plugin type.
        final Map<String, List<DataAccessPlugin>> plugins = DataAccessUtilities.getAllPlugins();

        final Map<String, Pair<Integer, List<DataAccessPlugin>>> pluginsWithOrder = new HashMap<>();

        // Remove hidden data access categories
        if (StringUtils.isNotBlank(DAV_CATS)) {
            final String[] arrayOfcategory = addCategoryToList(DAV_CATS);
            if (arrayOfcategory.length > 0) {
                for (int i = 0; i < arrayOfcategory.length; i++) {
                    plugins.remove(arrayOfcategory[i].trim());
                }
            }
        }

        if (StringUtils.isNotBlank(VISIBLE_CATS)) {
            final String[] visibleCategoriesArray = addCategoryToList(VISIBLE_CATS);
            if (visibleCategoriesArray.length > 0) {
                plugins.entrySet().forEach(entry -> {
                    for (int i = 0; i < visibleCategoriesArray.length; i++) {
                        if (entry.getKey().equals(visibleCategoriesArray[i].trim())) {
                            final Pair<Integer, List<DataAccessPlugin>> p = new Pair<>(i, entry.getValue());
                            pluginsWithOrder.put(entry.getKey(), p);
                            break;
                        }
                    }
                });
            }
        } else if (StringUtils.isBlank(DAV_CATS)) {
            plugins.entrySet().forEach(entry -> {
                int position = Integer.MAX_VALUE;
                for (final String pluginType : DataAccessPluginType.getTypeWithPosition().keySet()) {
                    if (pluginType.equals(entry.getKey())) {
                        position = DataAccessPluginType.getTypeWithPosition().get(pluginType);
                    }
                }

                pluginsWithOrder.put(entry.getKey(), new Pair(position, entry.getValue()));
                DEFAULT_ORDER.add(new Pair<>(position, entry.getKey()));
            });
            sortDefaultOrder();
        }

        return pluginsWithOrder;
    }

    public static String[] addCategoryToList(final String categories) {
        if (StringUtils.isNotBlank(categories)) {
            final String hiddenCategory = categories.replace("[", "");
            final String hiddenCategoryFinal = hiddenCategory.replace("]", "");
            return hiddenCategoryFinal.split(SeparatorConstants.COMMA);
        }
        return new String[0];
    }

    private void sortDefaultOrder() {
        Collections.sort(DEFAULT_ORDER, (p1, p2) -> p1.getKey() > p2.getKey() ? 1 : -1);
    }
}
