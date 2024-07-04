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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.openide.util.Lookup;

/**
 * Maps the class name of a help page to the address of the page.
 *
 * @author aldebaran30701
 */
public class HelpMapper {

    private HelpMapper() {
        // Intentionally left blank
    }

    private static final Map<String, String> mappings = new HashMap<>();

    /**
     * Uses the classname to find the location of the markdown file.
     *
     * @param className
     * @return the location of the markdown file within the project structure
     */
    public static String getHelpAddress(final String className) {
        String address = "";
        if (mappings.containsKey(className)) {
            address = mappings.get(className);
        }
        return address;
    }

    /**
     * Gets the cached map of help mappings. Will refresh mappings if the cache
     * is empty. Uses lazy instantiation for mappings.
     *
     * @return a Map with key String classname, value String filepath
     */
    public static Map<String, String> getMappings() {
        if (MapUtils.isEmpty(mappings)) {
            updateMappings();
        }
        return Collections.unmodifiableMap(mappings);
    }

    /**
     * Update the cached mappings via calls to lookup all active providers
     *
     */
    public static void updateMappings() {
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> mappings.putAll(provider.getHelpMap()));
    }
}
