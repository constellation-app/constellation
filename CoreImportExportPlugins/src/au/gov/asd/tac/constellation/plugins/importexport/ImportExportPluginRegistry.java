/*
 * Copyright 2010-2020 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.importexport.delimited.ImportDelimitedPlugin;
import au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoJsonPlugin;
import au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoPackagePlugin;
import au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToKmlPlugin;
import au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToShapefilePlugin;
import au.gov.asd.tac.constellation.plugins.importexport.image.ExportToImagePlugin;
import au.gov.asd.tac.constellation.plugins.importexport.json.ExportToJsonPlugin;
import au.gov.asd.tac.constellation.plugins.importexport.text.ExportToTextPlugin;

/**
 * A registry of import and export plugins
 *
 * @author algol
 */
public class ImportExportPluginRegistry {

    // import
    public static final String IMPORT_DELIMITED = ImportDelimitedPlugin.class.getName();

    // export
    public static final String EXPORT_GEOJSON = ExportToGeoJsonPlugin.class.getName();
    public static final String EXPORT_GEOPACKAGE = ExportToGeoPackagePlugin.class.getName();
    public static final String EXPORT_IMAGE = ExportToImagePlugin.class.getName();
    public static final String EXPORT_JSON = ExportToJsonPlugin.class.getName();
    public static final String EXPORT_KML = ExportToKmlPlugin.class.getName();
    public static final String EXPORT_TEXT = ExportToTextPlugin.class.getName();
    public static final String EXPORT_SHAPEFILE = ExportToShapefilePlugin.class.getName();
}
