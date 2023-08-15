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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the arrangement plugins module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 2600)
@NbBundle.Messages("ArrangementsPluginsHelpProvider=Arrangements Plugins Help Provider")
public class ArrangementPluginsHelpProvider extends HelpPageProvider {

    private static final String CODEBASE_NAME = "constellation";

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String arrangementModulePath = ".." + sep + "ext" + sep + "docs" + sep + "CoreArrangementPlugins" + sep + "src" + sep + "au" + sep + "gov"
                + sep + "asd" + sep + "tac" + sep + CODEBASE_NAME + sep + "plugins" + sep + "arrangements" + sep;

        map.put("au.gov.asd.tac.constellation.plugins.arrangements.grid", arrangementModulePath + "grid.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.line", arrangementModulePath + "line.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.hierarchy", arrangementModulePath + "hierarchy.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.tree", arrangementModulePath + "tree.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.circle", arrangementModulePath + "circle.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.scatter3d", arrangementModulePath + "scatter3d.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.sphere", arrangementModulePath + "sphere.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.flattenZField", arrangementModulePath + "flatten-z-field.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.contractGraph", arrangementModulePath + "contract-graph.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.expandGraph", arrangementModulePath + "expand-graph.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.layerByTime", arrangementModulePath + "layer-by-time.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.nodeAttribute", arrangementModulePath + "node-attribute.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String arrangementsPath;
        arrangementsPath = "ext" + sep + "docs" + sep + "CoreArrangementPlugins" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac"
                + sep + CODEBASE_NAME + sep + "plugins" + sep + "arrangements" + sep + "arrangements-toc.xml";

        return arrangementsPath;
    }
}
