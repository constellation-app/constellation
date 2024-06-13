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
package au.gov.asd.tac.constellation.plugins.importexport.geospatial;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.importexport.ImportExportPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Export to GeoPackage.
 *
 * @author cygnus_x-1
 */
@ActionID(
        category = "File",
        id = "au.gov.asd.tac.constellation.plugins.importexport.geospatial.ExportToGeoPackage")
@ActionRegistration(
        displayName = "#CTL_ExportToGeoPackageAction",
        iconBase = "au/gov/asd/tac/constellation/plugins/importexport/geospatial/exportToGeoPackage.png",
        surviveFocusChange = true)
@ActionReference(
        path = "Menu/File/Export",
        position = 300)
@NbBundle.Messages("CTL_ExportToGeoPackageAction=To GeoPackage...")
public class ExportToGeoPackageAction implements ActionListener {

    private final GraphNode context;

    public ExportToGeoPackageAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        PluginExecution.withPlugin(ImportExportPluginRegistry.EXPORT_GEOPACKAGE)
                .interactively(true)
                .executeLater(context.getGraph());
    }
}
