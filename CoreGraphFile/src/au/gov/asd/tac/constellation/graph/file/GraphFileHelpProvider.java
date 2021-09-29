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
package au.gov.asd.tac.constellation.graph.file;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
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

    @Override
    public Map<String, String> getHelpMap() {
        Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String graphFileModulePath = ".." + sep + "constellation" + sep + "CoreGraphFile" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + "constellation" + sep + "graph" + sep + "file" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.graph.file.autosave", graphFileModulePath + "autosave.md");
        map.put("au.gov.asd.tac.constellation.graph.file.openGraph", graphFileModulePath + "open-graph.md");
        map.put("au.gov.asd.tac.constellation.graph.file.newGraph", graphFileModulePath + "new-graph.md");
        map.put("au.gov.asd.tac.constellation.graph.file.saveGraph", graphFileModulePath + "save-graph.md");
        map.put("au.gov.asd.tac.constellation.graph.file.nebula", graphFileModulePath + "nebula.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String graphFilePath;
        graphFilePath = "constellation" + sep + "CoreGraphFile" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac" + sep
                + "constellation" + sep + "graph" + sep + "file" + sep + "docs" + sep + "file-toc.xml";
        return graphFilePath;
    }
}
