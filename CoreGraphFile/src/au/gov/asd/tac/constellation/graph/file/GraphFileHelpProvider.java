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
package au.gov.asd.tac.constellation.graph.file;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages from the graph file module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 300)
@NbBundle.Messages("GraphFileHelpProvider=Graph File Help Provider")
public class GraphFileHelpProvider extends HelpPageProvider {
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreGraphFile" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("au.gov.asd.tac.constellation.graph.file.autosave", MODULE_PATH + "autosave.md");
        map.put("au.gov.asd.tac.constellation.graph.file.openGraph", MODULE_PATH + "open-graph.md");
        map.put("au.gov.asd.tac.constellation.graph.file.newGraph", MODULE_PATH + "new-graph.md");
        map.put("au.gov.asd.tac.constellation.graph.file.saveGraph", MODULE_PATH + "save-graph.md");
        map.put("au.gov.asd.tac.constellation.graph.file.nebula", MODULE_PATH + "nebula.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "file-toc.xml";
    }
}
