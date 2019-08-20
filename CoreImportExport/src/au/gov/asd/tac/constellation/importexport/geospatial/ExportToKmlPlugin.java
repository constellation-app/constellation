/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.importexport.geospatial;

import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginInfo;
import au.gov.asd.tac.constellation.pluginframework.PluginType;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Export a graph as a Google Earth compatible KML file.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {"EXPORT"})
@NbBundle.Messages("ExportToKmlPlugin=Export to KML")
public class ExportToKmlPlugin extends AbstractGeoExportPlugin {

    @Override
    protected ExtensionFilter getExportType() {
        return new FileChooser.ExtensionFilter("KML", "*.kml");
    }

    @Override
    protected void exportGeo(final PluginParameters parameters, final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes, final File output) throws IOException {
        final String kml = Shape.generateKml(uuid, shapes, attributes);
        try (final FileWriter writer = new FileWriter(output)) {
            writer.write(kml);
        }
    }
}
