/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.json;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonWriter;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@NbBundle.Messages("ExportToJsonPlugin=Export to JSON")
public class ExportToJsonPlugin extends SimpleReadPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(ExportToJsonPlugin.class.getName());

    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ExportToJsonPlugin.class, "filename");

    @Override
    public void read(final GraphReadMethods rg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String filename = parameters.getParameters().get(FILE_NAME_PARAMETER_ID).getStringValue();

        try {
            new GraphJsonWriter().writeGraphFile(rg, filename, new HandleIoProgress("Exporting..."));
            ConstellationLoggerHelper.exportPropertyBuilder(
                    this,
                    GraphRecordStoreUtilities.getVertices(rg, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                    new File(filename),
                    ConstellationLoggerHelper.SUCCESS
            );
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> fnamParam = StringParameterType.build(FILE_NAME_PARAMETER_ID);
        fnamParam.setName(FILE_NAME_PARAMETER_ID);
        fnamParam.setDescription("The filename");
        parameters.addParameter(fnamParam);

        return parameters;
    }
}
