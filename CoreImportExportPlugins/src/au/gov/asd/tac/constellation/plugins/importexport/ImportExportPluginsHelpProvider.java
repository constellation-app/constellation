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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the import export plugins module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("ImportExportPluginsHelpProvider=Import Export Plugins Help Provider")
public class ImportExportPluginsHelpProvider extends HelpPageProvider {

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
        final String importExportModulePath = ".." + sep + "constellation" + sep + "CoreImportExportPlugins" + sep + "src" + sep + "au" + sep + "gov"
                + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "plugins" + sep + "importexport" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.plugins.importexport.delimited.DelimitedImportPane", importExportModulePath + "import-from-delimited-file.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.export", importExportModulePath + "export-from-constellation.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.jdbc.JDBCImportPane", importExportModulePath + "import-from-jdbc.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String importExportPath;
        importExportPath = "constellation" + sep + "CoreImportExportPlugins" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac"
                + sep + "constellation" + sep + "plugins" + sep + "importexport" + sep + "docs" + sep + "importexpot-toc.xml";
        return importExportPath;
    }
}
