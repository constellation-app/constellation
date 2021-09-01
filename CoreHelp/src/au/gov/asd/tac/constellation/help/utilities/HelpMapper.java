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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.collections4.MapUtils;
import org.openide.util.Lookup;

/**
 *
 * @author aldebaran30701
 */
public class HelpMapper {

    private final static Map<String, String> mappings = new HashMap<>();
    private static List<String> filePaths = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(HelpMapper.class.getName());

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
     * Uses the address of the markdown file to get the classname
     *
     * @param address
     * @return the location of the class
     */
    public static String getHelpFilePath(final String address) {
        String className = "";
        if (mappings.containsKey(address)) {
            for (String path : filePaths) {
                if (path.contains(address)) {
                    className = path;
                }
            }
        }
        return className;
    }

    /**
     * Gets the cached map of help mappings. Will refresh mappings if the cache
     * is empty.
     *
     * @return a Map with key String classname, value String filepath
     */
    public static Map<String, String> getMappings() {
        if (MapUtils.isEmpty(mappings)) {
            updateMappings();
        }
        return mappings;
    }

    /**
     * Update the cached mappings via calls to lookup all active providers
     *
     */
    public static void updateMappings() {
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            mappings.putAll(provider.getHelpMap());
            filePaths.addAll(provider.getHelpPages());
        });
    }
}
