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
package au.gov.asd.tac.constellation.plugins.algorithms;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the algorithm plugins module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 2500)
@NbBundle.Messages("AlgorithmPluginsHelpProvider=Algorithm Plugins Help Provider")
public class AlgorithmPluginsHelpProvider extends HelpPageProvider {
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreAlgorithmPlugins" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("au.gov.asd.tac.constellation.plugins.algorithms.clustering.chinesewhispers.ChineseWhispers", MODULE_PATH + "chinese-whispers.md");
        map.put("au.gov.asd.tac.constellation.plugins.algorithms.clustering.ktruss.KTruss", MODULE_PATH + "k-truss.md");
        map.put("au.gov.asd.tac.constellation.plugins.algorithms.paths.ShortestPathsPlugin", MODULE_PATH + "shortest-paths.md");
        map.put("au.gov.asd.tac.constellation.plugins.algorithms.clustering.hierarchical.Hierarchical", MODULE_PATH + "hierarchical-clustering.md");
        map.put("au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.InfoMap", MODULE_PATH + "info-map-clustering.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "algorithms-toc.xml";
    }
}
