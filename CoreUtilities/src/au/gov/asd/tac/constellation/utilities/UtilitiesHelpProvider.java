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
package au.gov.asd.tac.constellation.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the utilities module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 600)
@NbBundle.Messages("UtilitiesHelpProvider=Utilities Help Provider")
public class UtilitiesHelpProvider extends HelpPageProvider {

    private static final String CODEBASE_NAME = "constellation";
    private static final String SEP = File.separator;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String utilitiesModulePath = ".." + SEP + "ext" + SEP + "docs" + SEP + "CoreUtilities" + SEP + "src" + SEP + "au" + SEP + "gov" + SEP + "asd"
                + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "utilities" + SEP;

        map.put("au.gov.asd.tac.constellation.utilities.icons", utilitiesModulePath + "icons.md");
        map.put("au.gov.asd.tac.constellation.utilities.decorators", utilitiesModulePath + "decorators.md");
        map.put("au.gov.asd.tac.constellation.utilities.jupyter", utilitiesModulePath + "about-jupyter-notebook-server.md");
        map.put("au.gov.asd.tac.constellation.utilities.rest", utilitiesModulePath + "about-rest-server.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return "ext" + SEP + "docs" + SEP + "CoreUtilities" + SEP + "src" + SEP + "au" + SEP + "gov" + SEP + "asd" + 
                SEP + "tac" + SEP + CODEBASE_NAME + SEP + "utilities" + SEP + "utilities-toc.xml";
    }
}
