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
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessUtilities;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 * Looks up all available data access plugins and populates a map with them
 * grouped by plugin type.
 *
 * @author formalhaunt
 */
public class LookupPluginsTask implements Supplier<Map<String, List<DataAccessPlugin>>> {

    private static final Preferences PREFS = NbPreferences.forModule(DataAccessViewPreferenceKeys.class);
    public static final String DAV_CATS = PREFS.get(DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW, DataAccessViewPreferenceKeys.HIDDEN_DA_VIEW_DEFAULT);

    @Override
    public Map<String, List<DataAccessPlugin>> get() {
        // Creates a map with the key set being every available data access plugin type.
        final Map<String, List<DataAccessPlugin>> plugins = DataAccessUtilities.getAllPlugins();
        // Remove hidden data access categories
        if (StringUtils.isNotBlank(DAV_CATS)) {
            final String[] arrayOfcategory = addCategoryToList(DAV_CATS);
            if (arrayOfcategory.length > 0) {
                for (int i = 0; i < arrayOfcategory.length; i++) {
                    plugins.remove(arrayOfcategory[i].trim());
                }
            }
        }
        return plugins;
    }

    public static String[] addCategoryToList(final String categories) {
        if (StringUtils.isNotBlank(categories)) {
            final String hiddenCategory = categories.replace("[", "");
            final String hiddenCategoryFinal = hiddenCategory.replace("]", "");
            return hiddenCategoryFinal.replace(" ", "").split(SeparatorConstants.COMMA);
        }
        return new String[0];
    }
}
