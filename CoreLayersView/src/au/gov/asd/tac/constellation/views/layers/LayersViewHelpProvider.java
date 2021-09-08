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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the layers view * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("LayersViewHelpProvider=Layers View Help Provider")
public class LayersViewHelpProvider extends HelpPageProvider {

    @Override
    public List<String> getHelpPages() {
        final List<String> filePaths = new ArrayList<>();
        return filePaths;
    }

    @Override
    public List<String> getHelpResources() {
        final String userDir = System.getProperty("user.dir");
        final String sep = File.separator;
        final int count = userDir.length() - 13;
        final String substr = userDir.substring(count);
        final String layersViewPath;
        if ("constellation".equals(substr)) {
            layersViewPath = userDir + sep + "CoreLayersView" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "layers";
        } else {
            layersViewPath = userDir + sep + ".." + sep + "CoreLayersView" + sep + "src" + sep + "au" + sep
                    + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "layers";
        }

        final File dir = new File(layersViewPath);
        final String[] extensions = new String[]{"png"};
        final List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        final List<String> filePaths = new ArrayList<>();
        for (final File file : files) {
            filePaths.add(file.getPath());
        }
        return filePaths;
    }

    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String layersModulePath = ".." + sep + "constellation" + sep + "CoreLayersView" + sep + "src" + sep + "au" + sep
                + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "layers" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.views.layers.LayersViewTopComponent", layersModulePath + "layers-view.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String layersViewPath = "constellation" + sep + "CoreLayersView" + sep + "src" + sep + "au" + sep
                + "gov" + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "views" + sep + "layers" + sep + "docs" + sep + "layers-toc.xml";

        return layersViewPath;
    }
}
