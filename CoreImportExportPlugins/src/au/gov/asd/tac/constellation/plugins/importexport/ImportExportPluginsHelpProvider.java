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
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreImportExportPlugins" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        //Import Plugins
        map.put("au.gov.asd.tac.constellation.plugins.importexport.delimited.DelimitedImportPane", MODULE_PATH + "import-from-file.md");        
        map.put("au.gov.asd.tac.constellation.plugins.importexport.jdbc.JDBCImportPane", MODULE_PATH + "import-from-database.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.hashmod.HashmodPanel", MODULE_PATH + "add-hashmod.md");
        
        //Export Plugins
        map.put("au.gov.asd.tac.constellation.plugins.importexport.export", MODULE_PATH + "export-from-constellation.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoJsonPlugin", MODULE_PATH + "export-to-geojson.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoPackagePlugin", MODULE_PATH + "export-to-geopackage.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToImagePlugin", MODULE_PATH + "export-to-png.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToJsonPlugin", MODULE_PATH + "export-to-json.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToKmlPlugin", MODULE_PATH + "export-to-kml.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToShapefilePlugin", MODULE_PATH + "export-to-shapefile.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.svg.ExportToSVGPlugin", MODULE_PATH + "export-to-svg.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.svg.ExportToCSV", MODULE_PATH + "export-to-csv.md");
        map.put("au.gov.asd.tac.constellation.plugins.importexport.svg.ExportToXLSX", MODULE_PATH + "export-to-xlsx.md");       
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "importexport-toc.xml";
    }
}
