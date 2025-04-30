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
package au.gov.asd.tac.constellation.preferences;

import java.util.Collections;
import java.util.Map;
import static java.util.Map.entry;
import java.util.TreeMap;

/**
 * Keys used to access View preferences.
 *
 * @author sol695510
 */
public class ViewPreferenceKeys {

    // View names match what is passed in setName() in each view's TopComponent class.
    public static final Map<String, Boolean> DEFAULT_VIEW_OPTIONS = Collections.unmodifiableMap(new TreeMap<>(Map.ofEntries(
            entry("Analytic View", Boolean.FALSE),
            entry("Attribute Editor", Boolean.FALSE),
            entry("Conversation View", Boolean.FALSE),
            entry("Data Access View", Boolean.FALSE),
            entry("Error Report", Boolean.FALSE),
            entry("Find and Replace", Boolean.TRUE),
            entry("Histogram", Boolean.FALSE),
            entry("Layers View", Boolean.FALSE),
            entry("Map View", Boolean.FALSE),
            entry("Named Selections", Boolean.FALSE),
            entry("Notes View", Boolean.FALSE),
            entry("Plugin Reporter", Boolean.FALSE),
            entry("Quality Control View", Boolean.FALSE),
            entry("Scatter Plot", Boolean.FALSE),
            entry("Schema View", Boolean.FALSE),
            entry("Scripting View", Boolean.FALSE),
            entry("Table View", Boolean.FALSE),
            entry("Timeline", Boolean.FALSE),
            entry("Simple Graph", Boolean.FALSE),
            entry("Perspective Bookmarks", Boolean.FALSE),
            entry("Plane Manager", Boolean.FALSE),
            entry("Memory Manager", Boolean.FALSE),
            entry("Word Cloud View", Boolean.FALSE)
    )));
}
