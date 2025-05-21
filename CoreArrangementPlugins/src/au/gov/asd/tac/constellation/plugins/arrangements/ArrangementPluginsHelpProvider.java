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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
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
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreArrangementPlugins" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("au.gov.asd.tac.constellation.plugins.arrangements.grid", MODULE_PATH + "grid-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.line", MODULE_PATH + "line-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.hierarchy", MODULE_PATH + "hierarchy-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.tree", MODULE_PATH + "tree-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.circle", MODULE_PATH + "circle-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.scatter3d", MODULE_PATH + "scatter3d-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.sphere", MODULE_PATH + "sphere-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.flattenZField", MODULE_PATH + "flatten-z-field.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.contractGraph", MODULE_PATH + "contract-graph.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.expandGraph", MODULE_PATH + "expand-graph.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.layerByTime", MODULE_PATH + "layer-by-time.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.nodeAttribute", MODULE_PATH + "node-attribute-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.bubbleTree3d", MODULE_PATH + "bubble-tree-3d-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.proximity", MODULE_PATH + "proximity-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.spectral", MODULE_PATH + "spectral-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.hde", MODULE_PATH + "hde-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.uncollide", MODULE_PATH + "uncollide-arrangement.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.pinUnpin", MODULE_PATH + "pin-unpin-nodes.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "arrangements-toc.xml";
    }
}
