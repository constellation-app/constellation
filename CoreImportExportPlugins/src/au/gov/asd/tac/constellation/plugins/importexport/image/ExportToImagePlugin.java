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
package au.gov.asd.tac.constellation.plugins.importexport.image;

import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.io.File;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Export to image.
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
@Messages("ExportToImagePlugin=Export to Image")
public class ExportToImagePlugin extends SimplePlugin {

    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ExportToImagePlugin.class, "filename");

    @Override
    protected void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final String fnam = parameters.getParameters().get(FILE_NAME_PARAMETER_ID).getStringValue();
        final File imageFile = new File(fnam);
        final GraphNode graphNode = GraphNode.getGraphNode(graphs.getGraph());

        final VisualManager visualManager = graphNode.getVisualManager();
        if (visualManager != null) {
            visualManager.exportToImage(imageFile);

            final ReadableGraph rg = graphs.getGraph().getReadableGraph();
            try {
                ConstellationLoggerHelper.exportPropertyBuilder(
                        this,
                        GraphRecordStoreUtilities.getVertices(rg, false, false, false).getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.LABEL),
                        new File(fnam),
                        ConstellationLoggerHelper.SUCCESS
                );
            } finally {
                rg.release();
            }
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> imageFileParam = StringParameterType.build(FILE_NAME_PARAMETER_ID);
        imageFileParam.setDescription("File to write to");
        parameters.addParameter(imageFileParam);

        return parameters;
    }
}
