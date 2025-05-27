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

import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginType;
import static au.gov.asd.tac.constellation.plugins.importexport.geospatial.AbstractGeoExportPlugin.OUTPUT_PARAMETER_ID;
import static au.gov.asd.tac.constellation.plugins.importexport.geospatial.AbstractGeoExportPlugin.SPATIAL_REFERENCE_PARAMETER_ID;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.geospatial.Shape;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javafx.stage.FileChooser.ExtensionFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Export a graph to a GeoPackage file.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@NbBundle.Messages("ExportToGeoPackagePlugin=Export to GeoPackage")
public class ExportToGeoPackagePlugin extends AbstractGeoExportPlugin {

    final PluginParameters parameters = new PluginParameters();

    @Override
    public PluginParameters createParameters() {

        PluginParameters parametersCreated = super.createParameters();

        // Add "listener" on output parameter to check file overwrite
        parametersCreated.addController(OUTPUT_PARAMETER_ID, (master, params, change) -> {
            final String output = params.get(master.getId()).getStringValue();
            final File file = new File(output);
            FileParameterType.FileParameterValue fileParamValue = (FileParameterType.FileParameterValue) params.get(master.getId()).getParameterValue();
            // do a doesFileExist check if value changed, path is valid and filechooser dialog not previously opened
            if (change == ParameterChange.VALUE && super.isValidPath(file) && !fileParamValue.isFileChooserSelected()) {
                if (doesFileExist(new File(output))) {
                    String msg = String.format("The file %s already exists. Do you want to replace the existing file?", file.getName());
                    final NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Overwrite file", NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                    if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                        return;
                    } else {
                        params.get(master.getId()).setError(String.format("The file %s already exists.", file.getName()));
                    }
                }
            } else if (change == ParameterChange.ERROR) {
                params.get(master.getId()).getParameterValue().setStringValue(output);
            }
        });
        return parametersCreated;
    }

    @Override
    protected ExtensionFilter getExportType() {
        return new ExtensionFilter("GeoPackage", FileExtensionConstants.GEO_PACKAGE);
    }

    @Override
    protected void exportGeo(final PluginParameters parameters, final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes, final File output) throws IOException {
        final ParameterValue spatialReferencePV = parameters.getSingleChoice(SPATIAL_REFERENCE_PARAMETER_ID);
        if (spatialReferencePV instanceof SpatialReferenceParameterValue spatialReferenceParameterValue) {
            final Shape.SpatialReference spatialReference = spatialReferenceParameterValue.getSpatialReference();
            Shape.generateGeoPackage(uuid, shapes, attributes, output, spatialReference);
        }
    }

    @Override
    protected boolean includeSpatialReference() {
        return true;
    }
}
