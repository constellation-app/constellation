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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the arrangement plugins module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("ArrangementsPluginsHelpProvider=Arrangements Plugins Help Provider")
public class ArrangementPluginsHelpProvider extends HelpPageProvider {

    @Override
    public List<String> getHelpPages() {
        List<String> filePaths = new ArrayList<>();
        return filePaths;
    }

    @Override
    public List<String> getHelpResources() {
        List<String> filePaths = new ArrayList<>();
        return filePaths;
    }

    @Override
    public Map<String, String> getHelpMap() {
        Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String arrangementModulePath = ".." + sep + "constellation" + sep + "CoreArrangementPlugins" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + "constellation" + sep + "plugins" + sep + "arrangements" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.plugins.arrangements.grid.ArrangeInGridAction", arrangementModulePath + "grid.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.grid.ArrangeInGridGeneralPlugin", arrangementModulePath + "grid-general.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.hierarchical.ArrangeInHierarchyAction", arrangementModulePath + "hierarchical.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.tree.ArrangeInTreesAction", arrangementModulePath + "tree.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.circle.ArrangeInCircleAction", arrangementModulePath + "circle.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.scatter.ArrangeInScatter3dAction", arrangementModulePath + "scatter3d.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.circle.ArrangeInSphereAction", arrangementModulePath + "sphere.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.utilities.FlattenZFieldAction", arrangementModulePath + "flatten-z-field.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.resize.ContractGraphAction", arrangementModulePath + "contract-graph.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.resize.ExpandGraphAction", arrangementModulePath + "expand-graph.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.time.LayerByTimeAction", arrangementModulePath + "layer-by-time.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.trees.BubbleTreeAction", arrangementModulePath + "bubble-tree.md");
        map.put("au.gov.asd.tac.constellation.plugins.arrangements.clustersOnHilbertCurve", arrangementModulePath + "hilbert.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final int count = userDir.length() - 13;
        final String substr = userDir.substring(count);
        final String arrangementsPath;
        if ("constellation".equals(substr)) {
            arrangementsPath = userDir + sep + "CoreArrangementPlugins" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "plugins" + sep + "arrangements" + sep + "docs" + sep + "arrangements-toc.xml";

        } else {
            arrangementsPath = userDir + sep + ".." + sep + "CoreArrangementPlugins" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "plugins" + sep + "arrangements" + sep + "docs" + sep + "arrangements-toc.xml";
        }

        return arrangementsPath;
    }
}
