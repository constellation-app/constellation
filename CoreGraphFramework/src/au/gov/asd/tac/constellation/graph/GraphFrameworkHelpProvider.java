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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
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
        final String graphFrameworkModulePath = ".." + SEP + "ext" + SEP + "docs" + SEP + "CoreGraphFramework" + SEP + "src" + SEP + "au" + SEP + "gov"
                + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "graph" + SEP;

        map.put("au.gov.asd.tac.constellation.graph.about", graphFrameworkModulePath + "graph-model.md");
        map.put("au.gov.asd.tac.constellation.graph.attributes", graphFrameworkModulePath + "attributes.md");
        map.put("au.gov.asd.tac.constellation.graph.types", graphFrameworkModulePath + "types.md");
        map.put("au.gov.asd.tac.constellation.graph.schema.Schema", graphFrameworkModulePath + "schemas.md");
        map.put("au.gov.asd.tac.constellation.graph.expressions", graphFrameworkModulePath + "expressions-framework.md");
        map.put("au.gov.asd.tac.constellation.graph.io.fileformat", graphFrameworkModulePath + "constellation-file-format.md");
        map.put("au.gov.asd.tac.constellation.graph.io.pythonreader", graphFrameworkModulePath + "example-file-reader-python.md");
        map.put("au.gov.asd.tac.constellation.graph.io.pythonwriter", graphFrameworkModulePath + "example-file-writer-python.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return "ext" + SEP + "docs" + SEP + "CoreGraphFramework" + SEP + "src" + SEP + "au" + SEP
                + "gov" + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "graph" + SEP + "graph-toc.xml";
    }
}
