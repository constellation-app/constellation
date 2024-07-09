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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the import export plugins module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 2700)
@NbBundle.Messages("ImportExportPluginsHelpProvider=Import Export Plugins Help Provider")
public class ImportExportPluginsHelpProvider extends HelpPageProvider {

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
        final String importExportModulePath = ".." + sep + "ext" + sep + "docs" + sep + "CoreImportExportPlugins" + sep + "src" + sep + "au" + sep + "gov"
                + sep + "asd" + sep + "tac" + sep + CODEBASE_NAME + sep + "plugins" + sep + "importexport" + sep;

        //Import Plugins
        map.put("au.gov.asd.tac.constellation.plugins.importexport.delimited.DelimitedImportPane", importExportModulePath + "import-from-file.md");        
        map.put("au.gov.asd.tac.constellation.plugins.importexport.jdbc.JDBCImportPane", importExportModulePath + "import-from-database.md");
        
        //Export Plugins
        map.put("au.gov.asd.tac.constellation.plugins.importexport.export", importExportModulePath + "export-from-constellation.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoJsonPlugin", importExportModulePath + "export-to-geojson.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoPackagePlugin", importExportModulePath + "export-to-geopackage.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToImagePlugin", importExportModulePath + "export-to-png.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToJsonPlugin", importExportModulePath + "export-to-json.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToKmlPlugin", importExportModulePath + "export-to-kml.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToShapefilePlugin", importExportModulePath + "export-to-shapefile.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.svg.ExportToSVGPlugin", importExportModulePath + "export-to-svg.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.svg.ExportToCSV", importExportModulePath + "export-to-csv.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.svg.ExportToXLSX", importExportModulePath + "export-to-xlsx.md");
        
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
        final String importExportPath;
        importExportPath = "ext" + sep + "docs" + sep + "CoreImportExportPlugins" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac"
                + sep + CODEBASE_NAME + sep + "plugins" + sep + "importexport" + sep + "importexport-toc.xml";
        return importExportPath;
    }
}
