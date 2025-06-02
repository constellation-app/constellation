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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the graph framework module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 200)
@NbBundle.Messages("GraphFrameworkHelpProvider=Graph Framework Help Provider")
public class GraphFrameworkHelpProvider extends HelpPageProvider {
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreGraphFramework" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();      

        map.put("au.gov.asd.tac.constellation.graph.about", MODULE_PATH + "graph-model.md");
        map.put("au.gov.asd.tac.constellation.graph.attributes", MODULE_PATH + "attributes.md");
        map.put("au.gov.asd.tac.constellation.graph.types", MODULE_PATH + "types.md");
        map.put("au.gov.asd.tac.constellation.graph.schema.Schema", MODULE_PATH + "schemas.md");
        map.put("au.gov.asd.tac.constellation.graph.expressions", MODULE_PATH + "expressions-framework.md");
        map.put("au.gov.asd.tac.constellation.graph.io.fileformat", MODULE_PATH + "constellation-file-format.md");
        map.put("au.gov.asd.tac.constellation.graph.io.pythonreader", MODULE_PATH + "example-file-reader-python.md");
        map.put("au.gov.asd.tac.constellation.graph.io.pythonwriter", MODULE_PATH + "example-file-writer-python.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "graph-toc.xml";
    }
}
