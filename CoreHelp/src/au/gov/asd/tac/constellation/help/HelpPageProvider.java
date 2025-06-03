/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.help;

import java.util.Collections;
import java.util.Map;

/**
 * Provider to get help pages from each module
 *
 * @author Delphinus8821
 */
public abstract class HelpPageProvider {
    
    protected HelpPageProvider() {
        // Intentionally left blank
    }

    /**
     * Provides a map of all the help files Maps the file name to the md file
     * name
     *
     * @return Map of the file names vs md file names
     */
    public Map<String, String> getHelpMap() {
        return Collections.emptyMap();
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    public String getHelpTOC() {
        return null;
    }
}
